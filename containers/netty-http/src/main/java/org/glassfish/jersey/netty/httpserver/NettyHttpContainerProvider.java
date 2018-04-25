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

import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.Application;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.glassfish.jersey.Beta;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.spi.ContainerProvider;

/**
 * Netty implementation of {@link ContainerProvider}.
 * <p>
 * There is also a few "factory" methods for creating Netty server.
 *
 * @author Pavel Bucek (pavel.bucek at oracle.com)
 * @since 2.24
 */
@Beta
public class NettyHttpContainerProvider implements ContainerProvider {

    @Override
    public <T> T createContainer(Class<T> type, Application application) throws ProcessingException {
        if (NettyHttpContainer.class == type) {
            return type.cast(new NettyHttpContainer(application));
        }

        return null;
    }

    /**
     * Create and start Netty server.
     *
     * @param baseUri       base uri.
     * @param configuration Jersey configuration.
     * @param sslContext    Netty SSL context (can be null).
     * @param block         when {@code true}, this method will block until the server is stopped. When {@code false}, the
     *                      execution will
     *                      end immediately after the server is started.
     * @return Netty channel instance.
     * @throws ProcessingException when there is an issue with creating new container.
     */
    public static Channel createServer(final URI baseUri, final ResourceConfig configuration, SslContext sslContext,
                                       final boolean block)
            throws ProcessingException {

        // Configure the server.
        final EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        final EventLoopGroup workerGroup = new NioEventLoopGroup();
        final NettyHttpContainer container = new NettyHttpContainer(configuration);

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.option(ChannelOption.SO_BACKLOG, 1024);
            b.group(bossGroup, workerGroup)
             .channel(NioServerSocketChannel.class)
             .childHandler(new JerseyServerInitializer(baseUri, sslContext, container));

            int port = getPort(baseUri);

            Channel ch = b.bind(port).sync().channel();

            ch.closeFuture().addListener(new GenericFutureListener<Future<? super Void>>() {
                @Override
                public void operationComplete(Future<? super Void> future) throws Exception {
                    container.getApplicationHandler().onShutdown(container);

                    bossGroup.shutdownGracefully();
                    workerGroup.shutdownGracefully();
                }
            });

            if (block) {
                ch.closeFuture().sync();
                return ch;
            } else {
                return ch;
            }
        } catch (InterruptedException e) {
            throw new ProcessingException(e);
        }
    }

    /**
     * Create and start Netty server.
     *
     * @param baseUri       base uri.
     * @param configuration Jersey configuration.
     * @param block         when {@code true}, this method will block until the server is stopped. When {@code false}, the
     *                      execution will
     *                      end immediately after the server is started.
     * @return Netty channel instance.
     * @throws ProcessingException when there is an issue with creating new container.
     */
    public static Channel createServer(final URI baseUri, final ResourceConfig configuration, final boolean block)
            throws ProcessingException {

        return createServer(baseUri, configuration, null, block);
    }

    /**
     * Create and start Netty HTTP/2 server.
     * <p>
     * The server is capable of connection upgrade to HTTP/2. HTTP/1.x request will be server as they were used to.
     * <p>
     * Note that this implementation cannot be more experimental. Any contributions / feedback is welcomed.
     *
     * @param baseUri       base uri.
     * @param configuration Jersey configuration.
     * @param sslContext    Netty {@link SslContext}.
     * @return Netty channel instance.
     * @throws ProcessingException when there is an issue with creating new container.
     */
    public static Channel createHttp2Server(final URI baseUri, final ResourceConfig configuration, SslContext sslContext) throws
            ProcessingException {

        final EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        final EventLoopGroup workerGroup = new NioEventLoopGroup();
        final NettyHttpContainer container = new NettyHttpContainer(configuration);

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.option(ChannelOption.SO_BACKLOG, 1024);
            b.group(bossGroup, workerGroup)
             .channel(NioServerSocketChannel.class)
             .childHandler(new JerseyServerInitializer(baseUri, sslContext, container, true));

            int port = getPort(baseUri);

            Channel ch = b.bind(port).sync().channel();

            ch.closeFuture().addListener(new GenericFutureListener<Future<? super Void>>() {
                @Override
                public void operationComplete(Future<? super Void> future) throws Exception {
                    container.getApplicationHandler().onShutdown(container);

                    bossGroup.shutdownGracefully();
                    workerGroup.shutdownGracefully();
                }
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
}
