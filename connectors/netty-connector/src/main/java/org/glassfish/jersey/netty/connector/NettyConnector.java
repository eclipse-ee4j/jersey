/*
 * Copyright (c) 2016, 2025 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package org.glassfish.jersey.netty.connector;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Configuration;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpChunkedInput;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.ssl.ApplicationProtocolConfig;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.IdentityCipherSuiteFilter;
import io.netty.handler.ssl.JdkSslContext;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.resolver.NoopAddressResolverGroup;
import io.netty.util.concurrent.GenericFutureListener;
import org.glassfish.jersey.client.ClientRequest;
import org.glassfish.jersey.client.ClientResponse;
import org.glassfish.jersey.client.innate.ClientProxy;
import org.glassfish.jersey.client.innate.http.SSLParamConfigurator;
import org.glassfish.jersey.client.spi.AsyncConnectorCallback;
import org.glassfish.jersey.client.spi.Connector;
import org.glassfish.jersey.innate.VirtualThreadUtil;
import org.glassfish.jersey.internal.util.collection.LazyValue;
import org.glassfish.jersey.internal.util.collection.Value;
import org.glassfish.jersey.internal.util.collection.Values;
import org.glassfish.jersey.message.internal.OutboundMessageContext;
import org.glassfish.jersey.netty.connector.internal.NettyEntityWriter;

/**
 * Netty connector implementation.
 *
 * @author Pavel Bucek
 */
class NettyConnector implements Connector {

    final ExecutorService executorService;
    final EventLoopGroup group;
    final Client client;
    final HashMap<String, ArrayList<Channel>> connections = new HashMap<>();
    final NettyConnectorProvider.Config.RW connectorConfiguration;

    private static final LazyValue<String> NETTY_VERSION = Values.lazy(
        (Value<String>) () -> {
            String nettyVersion = null;
            try {
                nettyVersion = io.netty.util.Version.identify().values().iterator().next().artifactVersion();
            } catch (Throwable t) {
                nettyVersion = "4.1.x";
            }
            return "Netty " + nettyVersion;
        });

    static final String INACTIVE_POOLED_CONNECTION_HANDLER = "inactive_pooled_connection_handler";
    private static final String PRUNE_INACTIVE_POOL = "prune_inactive_pool";
    private static final String READ_TIMEOUT_HANDLER = "read_timeout_handler";
    private static final String REQUEST_HANDLER = "request_handler";
    private static final String EXPECT_100_CONTINUE_HANDLER = "expect_100_continue_handler";

    NettyConnector(Client client, NettyConnectorProvider.Config.RW connectorConfiguration) {
        this.client = client;
        this.connectorConfiguration = connectorConfiguration.fromClient(client);

        final Configuration configuration = client.getConfiguration();
        final Integer threadPoolSize = this.connectorConfiguration.asyncThreadPoolSize();
        if (threadPoolSize != null && threadPoolSize > 0) {
            executorService = VirtualThreadUtil
                                .withConfig(connectorConfiguration.prefixedConfiguration(configuration))
                                .newFixedThreadPool(threadPoolSize);
            this.group = new NioEventLoopGroup(threadPoolSize);
        } else {
            executorService = VirtualThreadUtil
                                .withConfig(connectorConfiguration.prefixedConfiguration(configuration))
                                .newCachedThreadPool();
            this.group = new NioEventLoopGroup();
        }
    }

    @Override
    public ClientResponse apply(ClientRequest jerseyRequest) {
        try {
            CompletableFuture<ClientResponse> response = new CompletableFuture<>();
            execute(jerseyRequest, new HashSet<>(), response);
            return response.join();
        } catch (CompletionException cex) {
            final Throwable t = cex.getCause() == null ? cex : cex.getCause();
            throw new ProcessingException(t.getMessage(), t);
        } catch (Exception ex) {
            throw new ProcessingException(ex.getMessage(), ex);
        }
    }

    @Override
    public Future<?> apply(final ClientRequest jerseyRequest, final AsyncConnectorCallback jerseyCallback) {
        CompletableFuture<ClientResponse> response = new CompletableFuture<>();
        response.whenCompleteAsync((r, th) -> {
            if (th == null) {
                jerseyCallback.response(r);
            } else {
                jerseyCallback.failure(th);
            }
        }, executorService);
        execute(jerseyRequest, new HashSet<>(), response);
        return response;
    }

    protected void execute(final ClientRequest jerseyRequest, final Set<URI> redirectUriHistory,
            final CompletableFuture<ClientResponse> responseAvailable) {
        final NettyConnectorProvider.Config.RW requestConfiguration =
                connectorConfiguration
                        .fromRequest(jerseyRequest)
                        .readTimeout(jerseyRequest)
                        .expect100ContinueTimeout(jerseyRequest);
        final int readTimeout = requestConfiguration.readTimeout();
        if (readTimeout < 0) {
            throw new ProcessingException(LocalizationMessages.WRONG_READ_TIMEOUT(readTimeout));
        }

        final CompletableFuture<?> responseDone = new CompletableFuture<>();

        final URI requestUri = jerseyRequest.getUri();
        final String host = requestUri.getHost();
        final int port = requestUri.getPort() != -1
                ? requestUri.getPort()
                : "https".equalsIgnoreCase(requestUri.getScheme()) ? 443 : 80;

        try {
            final SSLParamConfigurator sslConfig = SSLParamConfigurator.builder()
                    .request(jerseyRequest).setSNIAlways(true).setSNIHostName(jerseyRequest).build();

            final String key = requestConfiguration
                                .connectionController()
                                .getConnectionGroup(jerseyRequest, requestUri, sslConfig.getSNIHostName(), port);
            ArrayList<Channel> conns;
            synchronized (connections) {
               conns = connections.get(key);
               if (conns == null) {
                  conns = new ArrayList<>(0);
                  connections.put(key, conns);
               }
            }

            Channel chan = null;
            synchronized (conns) {
               while (chan == null && !conns.isEmpty()) {
                  chan = conns.remove(conns.size() - 1);
                  try {
                      chan.pipeline().remove(INACTIVE_POOLED_CONNECTION_HANDLER);
                      chan.pipeline().remove(PRUNE_INACTIVE_POOL);
                  } catch (NoSuchElementException e) {
                      /*
                       *  Eat it.
                       *  It could happen that the channel was closed, pipeline cleared,
                       *  and then it will fail to remove the names with this exception.
                       */
                  }
                  if (!chan.isOpen()) {
                      chan = null;
                  }
               }
            }

            final JerseyExpectContinueHandler expect100ContinueHandler = new JerseyExpectContinueHandler();

            if (chan == null) {
               requestConfiguration.connectTimeout(jerseyRequest);
               Bootstrap b = new Bootstrap();

               // http proxy
               final Optional<ClientProxy> handlerProxy = requestConfiguration.proxy(jerseyRequest, requestUri);
               handlerProxy.ifPresent(clientProxy -> {
                   b.resolver(NoopAddressResolverGroup.INSTANCE); // request hostname resolved by the HTTP proxy
               });

               b.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                     ChannelPipeline p = ch.pipeline();

                     Configuration config = jerseyRequest.getConfiguration();

                     // http proxy
                     handlerProxy.ifPresent(clientProxy -> {
                         p.addLast(requestConfiguration.createProxyHandler(clientProxy, jerseyRequest));
                     });

                     // Enable HTTPS if necessary.
                     if ("https".equals(requestUri.getScheme())) {
                         // making client authentication optional for now; it could be extracted to configurable property
                         JdkSslContext jdkSslContext = new JdkSslContext(
                                 requestConfiguration.sslContext(client, jerseyRequest),
                                 true,
                                 (Iterable) null,
                                 IdentityCipherSuiteFilter.INSTANCE,
                                 (ApplicationProtocolConfig) null,
                                 ClientAuth.NONE,
                                 (String[]) null, /* enable default protocols */
                                 false /* true if the first write request shouldn't be encrypted */
                         );

                         final int port = requestUri.getPort();

                         final SslHandler sslHandler = jdkSslContext.newHandler(
                                 ch.alloc(), sslConfig.getSNIHostName(), port <= 0 ? 443 : port, executorService
                         );
                         if (requestConfiguration.isSslHostnameVerificationEnabled(config.getProperties())) {
                             sslConfig.setEndpointIdentificationAlgorithm(sslHandler.engine());
                         }

                         sslConfig.setSNIServerName(sslHandler.engine());

                         p.addLast(sslHandler);
                     }

                     p.addLast(requestConfiguration.createHttpClientCodec(config.getProperties()));
                     p.addLast(EXPECT_100_CONTINUE_HANDLER, expect100ContinueHandler);
                     p.addLast(new ChunkedWriteHandler());
                     p.addLast(new HttpContentDecompressor());
                    }
                });

               // connect timeout
               if (requestConfiguration.connectTimeout() > 0) {
                   b.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, requestConfiguration.connectTimeout());
               }

               // Make the connection attempt.
                try {
                    chan = b.connect(host, port).sync().channel();
                } catch (Exception e) {
                    responseAvailable.completeExceptionally(e);
                    return;
                }
            }

            // assert: clientHandler will always notify responseDone: either normally, or exceptionally
            // assert: clientHandler may notify responseAvailable, if sufficient parts of response are detected to construct
            //         a valid ClientResponse
            // assert: responseAvailable completion may be racing against responseDone completion
            // assert: it is ok to abort the entire response, if responseDone is completed exceptionally - in particular, nothing
            //         will leak
            final Channel ch = chan;
            JerseyClientHandler clientHandler = new JerseyClientHandler(
                    jerseyRequest, responseAvailable, responseDone, redirectUriHistory, this, requestConfiguration);

            // read timeout makes sense really as an inactivity timeout
            ch.pipeline().addLast(READ_TIMEOUT_HANDLER,
                                  new IdleStateHandler(0, 0, requestConfiguration.readTimeout(), TimeUnit.MILLISECONDS));
            ch.pipeline().addLast(REQUEST_HANDLER, clientHandler);

            responseDone.whenComplete((_r, th) -> {
               ch.pipeline().remove(READ_TIMEOUT_HANDLER);
               ch.pipeline().remove(clientHandler);

               if (th == null) {
                  ch.pipeline().addLast(INACTIVE_POOLED_CONNECTION_HANDLER,
                          new IdleStateHandler(0, 0, requestConfiguration.maxPoolIdle.get()));
                  ch.pipeline().addLast(PRUNE_INACTIVE_POOL, new PruneIdlePool(connections, key));
                  boolean added = true;
                  synchronized (connections) {
                     ArrayList<Channel> conns1 = connections.get(key);
                     if (conns1 == null) {
                        conns1 = new ArrayList<>(1);
                        conns1.add(ch);
                        connections.put(key, conns1);
                     } else {
                        synchronized (conns1) {
                           if ((requestConfiguration.maxPoolSizeTotal.get() == 0
                                   || connections.size() < requestConfiguration.maxPoolSizeTotal.get())
                                   && conns1.size() < requestConfiguration.maxPoolSize.get()) {
                              conns1.add(ch);
                           } else { // else do not add the Channel to the idle pool
                              added = false;
                           }
                        }
                     }
                  }

                  if (!added) {
                      ch.close();
                  }
               } else {
                  ch.close();
                  // if responseAvailable has been completed, no-op: jersey will encounter IOException while reading response body
                  // if responseAvailable has not been completed, abort
                  responseAvailable.completeExceptionally(th);
               }
            });

            HttpRequest nettyRequest;
            String pathWithQuery = buildPathWithQueryParameters(requestUri);

            if (jerseyRequest.hasEntity()) {
                nettyRequest = new DefaultHttpRequest(HttpVersion.HTTP_1_1,
                                                      HttpMethod.valueOf(jerseyRequest.getMethod()),
                                                      pathWithQuery);
            } else {
                nettyRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1,
                                                          HttpMethod.valueOf(jerseyRequest.getMethod()),
                                                          pathWithQuery);
            }

            // headers
            if (!jerseyRequest.hasEntity()) {
                setHeaders(jerseyRequest, nettyRequest.headers(), false);
                setHostHeader(jerseyRequest, nettyRequest);
            }

            if (jerseyRequest.hasEntity()) {
                // guard against prematurely closed channel
                final GenericFutureListener<io.netty.util.concurrent.Future<? super Void>> closeListener =
                    new GenericFutureListener<io.netty.util.concurrent.Future<? super Void>>() {
                        @Override
                        public void operationComplete(io.netty.util.concurrent.Future<? super Void> future) throws Exception {
                            if (!responseDone.isDone()) {
                                responseDone.completeExceptionally(new IOException("Channel closed."));
                            }
                        }
                    };
                ch.closeFuture().addListener(closeListener);

                final NettyEntityWriter entityWriter = nettyEntityWriter(jerseyRequest, ch, requestConfiguration);
                switch (entityWriter.getType()) {
                    case CHUNKED:
                        HttpUtil.setTransferEncodingChunked(nettyRequest, true);
                        break;
                    case PRESET:
                        nettyRequest.headers().set(HttpHeaderNames.CONTENT_LENGTH, jerseyRequest.getLengthLong());
                        break;
//                  case DELAYED:
//                      // Set later after the entity is "written"
//                      break;
                }

                final CountDownLatch headersSet = new CountDownLatch(1);
                final CountDownLatch contentLengthSet = new CountDownLatch(1);


                jerseyRequest.setStreamProvider(new OutboundMessageContext.StreamProvider() {
                    @Override
                    public OutputStream getOutputStream(int contentLength) throws IOException {
                        try {
                            replaceHeaders(jerseyRequest, nettyRequest.headers()); // WriterInterceptor changes
                            setHostHeader(jerseyRequest, nettyRequest);
                        } catch (Exception e) {
                            responseDone.completeExceptionally(e);
                            throw new IOException(e);
                        } finally {
                            headersSet.countDown();
                        }
                        return entityWriter.getOutputStream();
                    }
                });

                executorService.execute(new Runnable() {
                    @Override
                    public void run() {
                        // close listener is not needed any more.
                        ch.closeFuture().removeListener(closeListener);

                        try {
                            jerseyRequest.writeEntity();
                            if (entityWriter.getType() == NettyEntityWriter.Type.DELAYED) {
                                nettyRequest.headers().set(HttpHeaderNames.CONTENT_LENGTH, entityWriter.getLength());
                                contentLengthSet.countDown();
                            }

                        } catch (Exception e) {
                            if (entityWriter.getChunkedInput() != null) {
                                try {
                                    entityWriter.getChunkedInput().close();
                                } catch (Exception ex) {
                                    // Ignore ex in favor of e
                                }
                            }
                            responseDone.completeExceptionally(e);
                        }
                    }
                });

                headersSet.await();
                new Expect100ContinueConnectorExtension(requestConfiguration).invoke(jerseyRequest, nettyRequest);

                boolean continueExpected = HttpUtil.is100ContinueExpected(nettyRequest);
                boolean expectationsFailed  = false;

                if (continueExpected) {
                    final CountDownLatch expect100ContinueLatch = new CountDownLatch(1);
                    expect100ContinueHandler.attachCountDownLatch(expect100ContinueLatch);
                    //send expect request, sync and wait till either response or timeout received
                    entityWriter.writeAndFlush(nettyRequest);
                    expect100ContinueLatch.await(requestConfiguration.expect100ContTimeout.get(), TimeUnit.MILLISECONDS);
                    try {
                        expect100ContinueHandler.processExpectationStatus();
                    } catch (TimeoutException e) {
                        //Expect:100-continue allows timeouts by the spec
                        //so, send request directly without Expect header.
                        expectationsFailed = true;
                    } finally {
                        //restore request and handler to the original state.
                        HttpUtil.set100ContinueExpected(nettyRequest, false);
                        expect100ContinueHandler.resetHandler();
                    }
                }

                if (!continueExpected || expectationsFailed) {
                    if (expectationsFailed) {
                        ch.pipeline().writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT).sync();
                    }
                    entityWriter.writeAndFlush(nettyRequest);
                }
                if (HttpUtil.isTransferEncodingChunked(nettyRequest)) {
                    entityWriter.write(new HttpChunkedInput(entityWriter.getChunkedInput()));
                } else {
                    entityWriter.write(entityWriter.getChunkedInput());
                }

                if (entityWriter.getType() == NettyEntityWriter.Type.DELAYED) {
                    contentLengthSet.await();
                }
                entityWriter.flush();
            } else {
                // Send the HTTP request.
                ch.writeAndFlush(nettyRequest);
            }

        } catch (IOException | InterruptedException e) {
            responseDone.completeExceptionally(e);
        }
    }

    /* package */ NettyEntityWriter nettyEntityWriter(
            ClientRequest clientRequest, Channel channel, NettyConnectorProvider.Config.RW requestConfiguration) {
        return NettyEntityWriter
                .getInstance(clientRequest, channel, () -> requestConfiguration.requestEntityProcessing(clientRequest));
    }

    private String buildPathWithQueryParameters(URI requestUri) {
        if (requestUri.getRawQuery() != null) {
            return String.format("%s?%s", requestUri.getRawPath(), requestUri.getRawQuery());
        } else {
            return requestUri.getRawPath();
        }
    }

    @Override
    public String getName() {
        return NETTY_VERSION.get();
    }

    @Override
    public void close() {
        group.shutdownGracefully();
        executorService.shutdown();
    }

    protected static class PruneIdlePool extends ChannelDuplexHandler {
       HashMap<String, ArrayList<Channel>> connections;
       String key;

       public PruneIdlePool(HashMap<String, ArrayList<Channel>> connections, String key) {
          this.connections = connections;
          this.key = key;
       }

       @Override
       public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
          if (evt instanceof IdleStateEvent) {
             IdleStateEvent e = (IdleStateEvent) evt;
             if (e.state() == IdleState.ALL_IDLE) {
                ctx.close();
                synchronized (connections) {
                   ArrayList<Channel> chans = connections.get(key);
                   synchronized (chans) {
                      chans.remove(ctx.channel());
                      if (chans.isEmpty()) {
                         connections.remove(key);
                      }
                   }
                }
             }
          } else {
              super.userEventTriggered(ctx, evt);
          }
       }
    }

    /* package */ static HttpHeaders setHeaders(ClientRequest jerseyRequest, HttpHeaders headers, boolean proxyOnly) {
        for (final Map.Entry<String, List<String>> e : jerseyRequest.getStringHeaders().entrySet()) {
            final String key = e.getKey();
            if (!proxyOnly || JerseyClientHandler.ProxyHeaders.INSTANCE.test(key) || additionalProxyHeadersToKeep(key)) {
                headers.add(key, e.getValue());
            }
        }
        return headers;
    }

    private static HttpHeaders replaceHeaders(ClientRequest jerseyRequest, HttpHeaders headers) {
        for (final Map.Entry<String, List<String>> e : jerseyRequest.getStringHeaders().entrySet()) {
            headers.set(e.getKey(), e.getValue());
        }
        return headers;
    }

    /*
     * Keep all X- headers (X-Forwarded-For,...) for proxy
     */
    private static boolean additionalProxyHeadersToKeep(String key) {
        return key.length() > 2 && (key.charAt(0) == 'x' || key.charAt(0) == 'X') && (key.charAt(1) == '-');
    }

    private static void setHostHeader(ClientRequest jerseyRequest, HttpRequest nettyRequest) {
        // host header - http 1.1
        if (!nettyRequest.headers().contains(HttpHeaderNames.HOST)) {
            int requestPort = jerseyRequest.getUri().getPort();
            final String hostHeader;
            if (requestPort != -1 && requestPort != 80 && requestPort != 443) {
                hostHeader = jerseyRequest.getUri().getHost() + ":" + requestPort;
            } else {
                hostHeader = jerseyRequest.getUri().getHost();
            }
            nettyRequest.headers().add(HttpHeaderNames.HOST, hostHeader);
        }
    }
}
