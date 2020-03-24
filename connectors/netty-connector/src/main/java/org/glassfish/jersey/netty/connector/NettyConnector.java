/*
 * Copyright (c) 2016, 2020 Oracle and/or its affiliates. All rights reserved.
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
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Configuration;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
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
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.proxy.HttpProxyHandler;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.JdkSslContext;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.concurrent.GenericFutureListener;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.ClientRequest;
import org.glassfish.jersey.client.ClientResponse;
import org.glassfish.jersey.client.spi.AsyncConnectorCallback;
import org.glassfish.jersey.client.spi.Connector;
import org.glassfish.jersey.message.internal.OutboundMessageContext;
import org.glassfish.jersey.netty.connector.internal.JerseyChunkedInput;

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

    NettyConnector(Client client) {

        final Object threadPoolSize = client.getConfiguration().getProperties().get(ClientProperties.ASYNC_THREADPOOL_SIZE);

        if (threadPoolSize != null && threadPoolSize instanceof Integer && (Integer) threadPoolSize > 0) {
            executorService = Executors.newFixedThreadPool((Integer) threadPoolSize);
            this.group = new NioEventLoopGroup((Integer) threadPoolSize);
        } else {
            executorService = Executors.newCachedThreadPool();
            this.group = new NioEventLoopGroup();
        }

        this.client = client;
    }

    @Override
    public ClientResponse apply(ClientRequest jerseyRequest) {
        try {
            CompletableFuture<ClientResponse> resultFuture = execute(jerseyRequest);

            Integer timeout = jerseyRequest.resolveProperty(ClientProperties.READ_TIMEOUT, 0);

            return (timeout != null && timeout > 0) ? resultFuture.get(timeout, TimeUnit.MILLISECONDS)
                                                    : resultFuture.get();
        } catch (ExecutionException ex) {
            Throwable e = ex.getCause() == null ? ex : ex.getCause();
            throw new ProcessingException(e.getMessage(), e);
        } catch (Exception ex) {
            throw new ProcessingException(ex.getMessage(), ex);
        }
    }

    @Override
    public Future<?> apply(final ClientRequest jerseyRequest, final AsyncConnectorCallback jerseyCallback) {
        return execute(jerseyRequest).whenCompleteAsync((r, th) -> {
                  if (th == null) jerseyCallback.response(r);
                  else jerseyCallback.failure(th);
               }, executorService);
    }

    protected CompletableFuture<ClientResponse> execute(final ClientRequest jerseyRequest) {
        final CompletableFuture<ClientResponse> responseAvailable = new CompletableFuture<>();
        final CompletableFuture<?> responseDone = new CompletableFuture<>();

        final URI requestUri = jerseyRequest.getUri();
        String host = requestUri.getHost();
        int port = requestUri.getPort() != -1 ? requestUri.getPort() : "https".equals(requestUri.getScheme()) ? 443 : 80;

        try {
            String key = requestUri.getScheme() + "://" + host + ":" + port;
            ArrayList<Channel> conns;
            synchronized (connections) {
               conns = connections.get(key);
               if (conns == null) {
                  conns = new ArrayList<>(0);
                  connections.put(key, conns);
               }
            }

            Channel chan;
            synchronized (conns) {
               chan = conns.size() == 0 ? null : conns.remove(conns.size() - 1);
            }

            if (chan == null) {
               Bootstrap b = new Bootstrap();
               b.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                     ChannelPipeline p = ch.pipeline();

                     // Enable HTTPS if necessary.
                     if ("https".equals(requestUri.getScheme())) {
                         // making client authentication optional for now; it could be extracted to configurable property
                         JdkSslContext jdkSslContext = new JdkSslContext(client.getSslContext(), true, ClientAuth.NONE);
                         p.addLast(jdkSslContext.newHandler(ch.alloc()));
                     }

                     // http proxy
                     Configuration config = jerseyRequest.getConfiguration();
                     final Object proxyUri = config.getProperties().get(ClientProperties.PROXY_URI);
                     if (proxyUri != null) {
                         final URI u = getProxyUri(proxyUri);

                         final String userName = ClientProperties.getValue(
                                 config.getProperties(), ClientProperties.PROXY_USERNAME, String.class);
                         final String password = ClientProperties.getValue(
                                 config.getProperties(), ClientProperties.PROXY_PASSWORD, String.class);

                         p.addLast(new HttpProxyHandler(new InetSocketAddress(u.getHost(),
                                                                              u.getPort() == -1 ? 8080 : u.getPort()),
                                                        userName, password));
                     }

                     p.addLast(new HttpClientCodec());
                     p.addLast(new ChunkedWriteHandler());
                     p.addLast(new HttpContentDecompressor());
                    }
                });

               // connect timeout
               Integer connectTimeout = jerseyRequest.resolveProperty(ClientProperties.CONNECT_TIMEOUT, 0);
               if (connectTimeout > 0) {
                   b.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeout);
               }

               // Make the connection attempt.
               chan = b.connect(host, port).sync().channel();
            }

            // assert: clientHandler will always notify responseDone: either normally, or exceptionally
            // assert: clientHandler may notify responseAvailable, if sufficient parts of response are detected to construct
            //         a valid ClientResponse
            // assert: responseAvailable completion may be racing against responseDone completion
            // assert: it is ok to abort the entire response, if responseDone is completed exceptionally - in particular, nothing
            //         will leak
            final Channel ch = chan;
            JerseyClientHandler clientHandler = new JerseyClientHandler(jerseyRequest, responseAvailable, responseDone);
            ch.pipeline().addLast(clientHandler);

            responseDone.whenComplete((_r, th) -> {
               ch.pipeline().remove(clientHandler);

               if (th == null) {
                  synchronized (connections) {
                     ArrayList<Channel> conns1 = connections.get(key);
                     synchronized (conns1) {
                        conns1.add(ch);
                     }
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
            for (final Map.Entry<String, List<String>> e : jerseyRequest.getStringHeaders().entrySet()) {
                nettyRequest.headers().add(e.getKey(), e.getValue());
            }

            // host header - http 1.1
            nettyRequest.headers().add(HttpHeaderNames.HOST, jerseyRequest.getUri().getHost());

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
                if (jerseyRequest.getLengthLong() == -1) {
                    HttpUtil.setTransferEncodingChunked(nettyRequest, true);
                } else {
                    nettyRequest.headers().add(HttpHeaderNames.CONTENT_LENGTH, jerseyRequest.getLengthLong());
                }

                // Send the HTTP request.
                ch.writeAndFlush(nettyRequest);

                final JerseyChunkedInput jerseyChunkedInput = new JerseyChunkedInput(ch);
                jerseyRequest.setStreamProvider(new OutboundMessageContext.StreamProvider() {
                    @Override
                    public OutputStream getOutputStream(int contentLength) throws IOException {
                        return jerseyChunkedInput;
                    }
                });

                if (HttpUtil.isTransferEncodingChunked(nettyRequest)) {
                    ch.write(new HttpChunkedInput(jerseyChunkedInput));
                } else {
                    ch.write(jerseyChunkedInput);
                }

                executorService.execute(new Runnable() {
                    @Override
                    public void run() {
                        // close listener is not needed any more.
                        ch.closeFuture().removeListener(closeListener);

                        try {
                            jerseyRequest.writeEntity();
                        } catch (IOException e) {
                            responseDone.completeExceptionally(e);
                        }
                    }
                });

                ch.flush();
            } else {
                // Send the HTTP request.
                ch.writeAndFlush(nettyRequest);
            }

        } catch (InterruptedException e) {
            responseDone.completeExceptionally(e);
        }

        return responseAvailable;
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
        return "Netty 4.1.x";
    }

    @Override
    public void close() {
        group.shutdownGracefully();
        executorService.shutdown();
    }

    @SuppressWarnings("ChainOfInstanceofChecks")
    private static URI getProxyUri(final Object proxy) {
        if (proxy instanceof URI) {
            return (URI) proxy;
        } else if (proxy instanceof String) {
            return URI.create((String) proxy);
        } else {
            throw new ProcessingException(LocalizationMessages.WRONG_PROXY_URI_TYPE(ClientProperties.PROXY_URI));
        }
    }
}
