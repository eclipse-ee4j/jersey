/*
 * Copyright (c) 2016, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.netty.httpserver;

import java.net.URI;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;

import javax.ws.rs.ProcessingException;

import org.glassfish.jersey.server.ResourceConfig;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.kqueue.KQueue;
import io.netty.channel.kqueue.KQueueEventLoopGroup;
import io.netty.channel.kqueue.KQueueServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.ssl.SslContext;

/**
 * A builder for netty jersey server.
 * <p>
 * Usage:
 * <pre>
 *       final AtomicInteger workerThreadCounter = new AtomicInteger();
 *       Channel server = NettyJerseyServerBuilder.newBuilder(baseUri, resourceConfig)
 *           .listenHost("localhost")
 *           .listenPort(8080)
 *           .bossThreads(1)
 *           .workerThreads(5)
 *           .workerThreadFactory(r -> {
 *               Thread t = new Thread(r);
 *               t.setName("NettyWorkerThread-" + workerThreadCounter.getAndIncrease();
 *               return t;
 *           })
 *           .http2();
 *           ....
 *           .build();
 * </pre>
 * </p>
 *
 * @author Wei Gao (wei at gaofamily.org)
 */
public class NettyJerseyServerBuilder {
    private final URI baseUri;
    private final ResourceConfig config;
    private String listenHost;
    private int listenPort;
    private SslContext sslContext;
    private Executor bossThreadPool;
    private Executor workerThreadPool;
    private ThreadFactory bossThreadFactory;
    private ThreadFactory workerThreadFactory;
    private int bossThreads = 1;
    private int workerThreads = 0;
    private boolean http2;

    private NettyJerseyServerBuilder(URI baseUri, ResourceConfig config) {
        this.baseUri = baseUri;
        this.config = config;
    }

    public static NettyJerseyServerBuilder newBuilder(URI baseUri, ResourceConfig config) {
        return new NettyJerseyServerBuilder(baseUri, config);
    }

    public NettyJerseyServerBuilder listenHost(String listenHost) {
        this.listenHost = listenHost;
        return this;
    }

    public NettyJerseyServerBuilder listenPort(int listenPort) {
        this.listenPort = listenPort;
        return this;
    }

    public NettyJerseyServerBuilder sslContext(SslContext sslContext) {
        this.sslContext = sslContext;
        return this;
    }

    public NettyJerseyServerBuilder bossThreadPool(Executor bossThreadPool) {
        this.bossThreadPool = bossThreadPool;
        return this;
    }

    public NettyJerseyServerBuilder workerThreadPool(Executor workerThreadPool) {
        this.workerThreadPool = workerThreadPool;
        return this;
    }

    public NettyJerseyServerBuilder bossThreadFactory(ThreadFactory bossThreadFactory) {
        this.bossThreadFactory = bossThreadFactory;
        return this;
    }

    public NettyJerseyServerBuilder workerThreadFactory(ThreadFactory workerThreadFactory) {
        this.workerThreadFactory = workerThreadFactory;
        return this;
    }

    public NettyJerseyServerBuilder bossThreads(int bossThreads) {
        this.bossThreads = bossThreads;
        return this;
    }

    public NettyJerseyServerBuilder workerThreads(int workerThreads) {
        this.workerThreads = workerThreads;
        return this;
    }

    public NettyJerseyServerBuilder http2() {
        return http2(true);
    }

    public NettyJerseyServerBuilder http2(boolean http2) {
        this.http2 = http2;
        return this;
    }

    public Channel buildNettyJerseyServer() throws ProcessingException {
        int port = listenPort;
        if (port == 0) {
            port = getPort(baseUri);
        }

        // Configure the server.
        final EventLoopGroup bossGroup =
                createEventLoopGroup(bossThreads, bossThreadPool, bossThreadFactory);
        final EventLoopGroup workerGroup =
                createEventLoopGroup(workerThreads, workerThreadPool, workerThreadFactory);
        final Class<? extends ServerSocketChannel> channelClass = getServerSocketChannel();
        final NettyHttpContainer container = new NettyHttpContainer(config);

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.option(ChannelOption.SO_BACKLOG, 1024);
            b.childOption(ChannelOption.SO_KEEPALIVE, true);
            b.group(bossGroup, workerGroup)
                    .channel(channelClass)
                    .childHandler(
                            new JerseyServerInitializer(baseUri, sslContext, container, http2));

            ChannelFuture channelFuture;
            if (listenHost != null) {
                channelFuture = b.bind(listenHost, port);
            } else {
                channelFuture = b.bind(port);
            }

            Channel ch = channelFuture.sync().channel();

            ch.closeFuture().addListener(future -> {
                container.getApplicationHandler().onShutdown(container);

                bossGroup.shutdownGracefully();
                workerGroup.shutdownGracefully();
            });

            return ch;
        } catch (InterruptedException e) {
            throw new ProcessingException(e);
        }
    }

    private static int getPort(URI uri) {
        if (uri.getPort() == -1) {
            if ("http".equalsIgnoreCase(uri.getScheme())) {
                return 80;
            } else if ("https".equalsIgnoreCase(uri.getScheme())) {
                return 443;
            }

            throw new IllegalArgumentException("URI scheme must be 'http' or 'https'.");
        }

        return uri.getPort();
    }

    public static EventLoopGroup createEventLoopGroup(int threads,
                                                      Executor threadPool,
                                                      ThreadFactory threadFactory) {
        EventLoopGroup group = null;
        if (Epoll.isAvailable()) {
            if (threads > 0) {
                if (threadPool != null) {
                    group = new EpollEventLoopGroup(threads, threadPool);
                } else if (threadFactory != null) {
                    group = new EpollEventLoopGroup(threads, threadFactory);
                } else {
                    group = new EpollEventLoopGroup(threads);
                }
            } else {
                group = new EpollEventLoopGroup();
            }
        } else if (KQueue.isAvailable()) {
            if (threads > 0) {
                if (threadPool != null) {
                    group = new KQueueEventLoopGroup(threads, threadPool);
                } else if (threadFactory != null) {
                    group = new KQueueEventLoopGroup(threads, threadFactory);
                } else {
                    group = new KQueueEventLoopGroup(threads);
                }
            } else {
                group = new KQueueEventLoopGroup();
            }
        } else {
            if (threads > 0) {
                if (threadPool != null) {
                    group = new NioEventLoopGroup(threads, threadPool);
                } else if (threadFactory != null) {
                    group = new NioEventLoopGroup(threads, threadFactory);
                } else {
                    group = new NioEventLoopGroup(threads);
                }
            } else {
                group = new NioEventLoopGroup();
            }
        }
        return group;
    }

    public static Class<? extends ServerSocketChannel> getServerSocketChannel() {
        return Epoll.isAvailable() ? EpollServerSocketChannel.class :
                KQueue.isAvailable() ? KQueueServerSocketChannel.class : NioServerSocketChannel.class;
    }
}
