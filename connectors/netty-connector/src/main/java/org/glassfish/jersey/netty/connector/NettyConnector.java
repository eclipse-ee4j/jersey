/*
 * Copyright (c) 2016, 2024 Oracle and/or its affiliates. All rights reserved.
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
import java.net.InetSocketAddress;
import java.net.SocketAddress;
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

import javax.net.ssl.SSLContext;

import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.core.Configuration;

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
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpChunkedInput;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.proxy.HttpProxyHandler;
import io.netty.handler.proxy.ProxyHandler;
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
import org.glassfish.jersey.client.ClientProperties;
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

    // If HTTP keepalive is enabled the value of "http.maxConnections" determines the maximum number
    // of idle connections that will be simultaneously kept alive, per destination.
    private static final String HTTP_KEEPALIVE_STRING = System.getProperty("http.keepAlive");
    // http.keepalive (default: true)
    private static final Boolean HTTP_KEEPALIVE =
            HTTP_KEEPALIVE_STRING == null ? Boolean.TRUE : Boolean.parseBoolean(HTTP_KEEPALIVE_STRING);

    // http.maxConnections (default: 5)
    private static final int DEFAULT_MAX_POOL_SIZE = 5;
    private static final int MAX_POOL_SIZE = Integer.getInteger("http.maxConnections", DEFAULT_MAX_POOL_SIZE);
    private static final int DEFAULT_MAX_POOL_IDLE = 60; // seconds
    private static final int DEFAULT_MAX_POOL_SIZE_TOTAL = 60; // connections


    private final Integer maxPoolSize; // either from system property, or from Jersey config, or default
    private final Integer maxPoolSizeTotal; //either from Jersey config, or default
    private final Integer maxPoolIdle; // either from Jersey config, or default

    static final String INACTIVE_POOLED_CONNECTION_HANDLER = "inactive_pooled_connection_handler";
    private static final String PRUNE_INACTIVE_POOL = "prune_inactive_pool";
    private static final String READ_TIMEOUT_HANDLER = "read_timeout_handler";
    private static final String REQUEST_HANDLER = "request_handler";
    private static final String EXPECT_100_CONTINUE_HANDLER = "expect_100_continue_handler";

    NettyConnector(Client client) {

        final Configuration configuration = client.getConfiguration();
        final Map<String, Object> properties = configuration.getProperties();
        final Object threadPoolSize = properties.get(ClientProperties.ASYNC_THREADPOOL_SIZE);

        if (threadPoolSize != null && threadPoolSize instanceof Integer && (Integer) threadPoolSize > 0) {
            executorService = VirtualThreadUtil.withConfig(configuration).newFixedThreadPool((Integer) threadPoolSize);
            this.group = new NioEventLoopGroup((Integer) threadPoolSize);
        } else {
            executorService = VirtualThreadUtil.withConfig(configuration).newCachedThreadPool();
            this.group = new NioEventLoopGroup();
        }

        this.client = client;

        final Object maxPoolSizeTotalProperty = properties.get(NettyClientProperties.MAX_CONNECTIONS_TOTAL);
        final Object maxPoolIdleProperty = properties.get(NettyClientProperties.IDLE_CONNECTION_PRUNE_TIMEOUT);
        final Object maxPoolSizeProperty = properties.get(NettyClientProperties.MAX_CONNECTIONS);

        maxPoolSizeTotal = maxPoolSizeTotalProperty != null ? (Integer) maxPoolSizeTotalProperty : DEFAULT_MAX_POOL_SIZE_TOTAL;
        maxPoolIdle = maxPoolIdleProperty != null ? (Integer) maxPoolIdleProperty : DEFAULT_MAX_POOL_IDLE;
        maxPoolSize = maxPoolSizeProperty != null
                ? (Integer) maxPoolSizeProperty
                : (HTTP_KEEPALIVE ? MAX_POOL_SIZE : DEFAULT_MAX_POOL_SIZE);

        if (maxPoolSizeTotal < 0) {
            throw new ProcessingException(LocalizationMessages.WRONG_MAX_POOL_TOTAL(maxPoolSizeTotal));
        }

        if (maxPoolSize < 0) {
            throw new ProcessingException(LocalizationMessages.WRONG_MAX_POOL_SIZE(maxPoolSize));
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
        Integer timeout = jerseyRequest.resolveProperty(ClientProperties.READ_TIMEOUT, 0);
        final Integer expect100ContinueTimeout = jerseyRequest.resolveProperty(
                NettyClientProperties.EXPECT_100_CONTINUE_TIMEOUT,
                NettyClientProperties.DEFAULT_EXPECT_100_CONTINUE_TIMEOUT_VALUE);
        if (timeout == null || timeout < 0) {
            throw new ProcessingException(LocalizationMessages.WRONG_READ_TIMEOUT(timeout));
        }

        final CompletableFuture<?> responseDone = new CompletableFuture<>();

        final URI requestUri = jerseyRequest.getUri();
        String host = requestUri.getHost();
        int port = requestUri.getPort() != -1 ? requestUri.getPort() : "https".equals(requestUri.getScheme()) ? 443 : 80;

        try {
            final SSLParamConfigurator sslConfig = SSLParamConfigurator.builder()
                    .request(jerseyRequest).setSNIAlways(true).setSNIHostName(jerseyRequest).build();

            String key = requestUri.getScheme() + "://" + sslConfig.getSNIHostName() + ":" + port;
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
                       *  It could happen that the channel was closed, pipeline cleared and
                       *  then it will fail to remove the names with this exception.
                       */
                  }
                  if (!chan.isOpen()) {
                      chan = null;
                  }
               }
            }

            if (chan == null) {
               Integer connectTimeout = jerseyRequest.resolveProperty(ClientProperties.CONNECT_TIMEOUT, 0);
               Bootstrap b = new Bootstrap();

               // http proxy
               Optional<ClientProxy> proxy = ClientProxy.proxyFromRequest(jerseyRequest);
               if (!proxy.isPresent()) {
                   proxy = ClientProxy.proxyFromProperties(requestUri);
               }
               proxy.ifPresent(clientProxy -> {
                   b.resolver(NoopAddressResolverGroup.INSTANCE); // request hostname resolved by the HTTP proxy
               });

               final Optional<ClientProxy> handlerProxy = proxy;

               b.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                     ChannelPipeline p = ch.pipeline();

                     Configuration config = jerseyRequest.getConfiguration();

                     // http proxy
                     handlerProxy.ifPresent(clientProxy -> {
                         final URI u = clientProxy.uri();
                         InetSocketAddress proxyAddr = new InetSocketAddress(u.getHost(),
                                 u.getPort() == -1 ? 8080 : u.getPort());
                         ProxyHandler proxy1 = createProxyHandler(jerseyRequest, proxyAddr,
                                 clientProxy.userName(), clientProxy.password(), connectTimeout);
                         p.addLast(proxy1);
                     });

                     // Enable HTTPS if necessary.
                     if ("https".equals(requestUri.getScheme())) {
                         // making client authentication optional for now; it could be extracted to configurable property
                         JdkSslContext jdkSslContext = new JdkSslContext(
                                 getSslContext(client, jerseyRequest),
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
                         if (ClientProperties.getValue(config.getProperties(),
                                                       NettyClientProperties.ENABLE_SSL_HOSTNAME_VERIFICATION, true)) {
                             sslConfig.setEndpointIdentificationAlgorithm(sslHandler.engine());
                         }

                         sslConfig.setSNIServerName(sslHandler.engine());

                         p.addLast(sslHandler);
                     }

                     final Integer maxHeaderSize = ClientProperties.getValue(config.getProperties(),
                                NettyClientProperties.MAX_HEADER_SIZE,
                                NettyClientProperties.DEFAULT_HEADER_SIZE);
                     final Integer maxChunkSize = ClientProperties.getValue(config.getProperties(),
                                NettyClientProperties.MAX_CHUNK_SIZE,
                                NettyClientProperties.DEFAULT_CHUNK_SIZE);
                     final Integer maxInitialLineLength = ClientProperties.getValue(config.getProperties(),
                                NettyClientProperties.MAX_INITIAL_LINE_LENGTH,
                                NettyClientProperties.DEFAULT_INITIAL_LINE_LENGTH);

                     p.addLast(new HttpClientCodec(maxInitialLineLength, maxHeaderSize, maxChunkSize));
                     p.addLast(new ChunkedWriteHandler());
                     p.addLast(new HttpContentDecompressor());
                    }
                });

               // connect timeout
               if (connectTimeout > 0) {
                   b.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeout);
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
            JerseyClientHandler clientHandler =
                    new JerseyClientHandler(jerseyRequest, responseAvailable, responseDone, redirectUriHistory, this);
            final JerseyExpectContinueHandler expect100ContinueHandler = new JerseyExpectContinueHandler();
            // read timeout makes sense really as an inactivity timeout
            ch.pipeline().addLast(READ_TIMEOUT_HANDLER,
                                  new IdleStateHandler(0, 0, timeout, TimeUnit.MILLISECONDS));
            ch.pipeline().addLast(EXPECT_100_CONTINUE_HANDLER, expect100ContinueHandler);
            ch.pipeline().addLast(REQUEST_HANDLER, clientHandler);

            responseDone.whenComplete((_r, th) -> {
               ch.pipeline().remove(READ_TIMEOUT_HANDLER);
               ch.pipeline().remove(clientHandler);

               if (th == null) {
                  ch.pipeline().addLast(INACTIVE_POOLED_CONNECTION_HANDLER, new IdleStateHandler(0, 0, maxPoolIdle));
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
                           if ((maxPoolSizeTotal == 0 || connections.size() < maxPoolSizeTotal) && conns1.size() < maxPoolSize) {
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

                // host header - http 1.1
                if (!nettyRequest.headers().contains(HttpHeaderNames.HOST)) {
                    nettyRequest.headers().add(HttpHeaderNames.HOST, jerseyRequest.getUri().getHost());
                }
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

                final NettyEntityWriter entityWriter = nettyEntityWriter(jerseyRequest, ch);
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
                try {
                    expect100ContinueHandler.processExpect100ContinueRequest(nettyRequest, jerseyRequest,
                            ch, expect100ContinueTimeout);
                } catch (ExecutionException e) {
                    responseDone.completeExceptionally(e);
                } catch (TimeoutException e) {
                    //Expect:100-continue allows timeouts by the spec
                    //just removing the pipeline from processing
                    if (ch.pipeline().context(JerseyExpectContinueHandler.class) != null) {
                        ch.pipeline().remove(EXPECT_100_CONTINUE_HANDLER);
                    }
                }

                final CountDownLatch headersSet = new CountDownLatch(1);
                final CountDownLatch contentLengthSet = new CountDownLatch(1);

                jerseyRequest.setStreamProvider(new OutboundMessageContext.StreamProvider() {
                    @Override
                    public OutputStream getOutputStream(int contentLength) throws IOException {
                        replaceHeaders(jerseyRequest, nettyRequest.headers()); // WriterInterceptor changes
                        if (!nettyRequest.headers().contains(HttpHeaderNames.HOST)) {
                            nettyRequest.headers().add(HttpHeaderNames.HOST, jerseyRequest.getUri().getHost());
                        }
                        headersSet.countDown();

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

                        } catch (IOException e) {
                            responseDone.completeExceptionally(e);
                        }
                    }
                });

                headersSet.await();
                if (!expect100ContinueHandler.isExpected()) {
                    // Send the HTTP request. Expect:100-continue processing is not applicable
                    // in this case.
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

    /* package */ NettyEntityWriter nettyEntityWriter(ClientRequest clientRequest, Channel channel) {
        return NettyEntityWriter.getInstance(clientRequest, channel);
    }

    private SSLContext getSslContext(Client client, ClientRequest request) {
        Supplier<SSLContext> supplier = request.resolveProperty(ClientProperties.SSL_CONTEXT_SUPPLIER, Supplier.class);
        return supplier == null ? client.getSslContext() : supplier.get();
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

    private static ProxyHandler createProxyHandler(ClientRequest jerseyRequest, SocketAddress proxyAddr,
                                                   String userName, String password, long connectTimeout) {
        final Boolean filter = jerseyRequest.resolveProperty(NettyClientProperties.FILTER_HEADERS_FOR_PROXY, Boolean.TRUE);
        HttpHeaders httpHeaders = setHeaders(jerseyRequest, new DefaultHttpHeaders(), Boolean.TRUE.equals(filter));

        ProxyHandler proxy = userName == null ? new HttpProxyHandler(proxyAddr, httpHeaders)
                : new HttpProxyHandler(proxyAddr, userName, password, httpHeaders);
        if (connectTimeout > 0) {
            proxy.setConnectTimeoutMillis(connectTimeout);
        }

        return proxy;
    }

    private static HttpHeaders setHeaders(ClientRequest jerseyRequest, HttpHeaders headers, boolean proxyOnly) {
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
}
