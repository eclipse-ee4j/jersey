/*
 * Copyright (c) 2021, 2022 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2018 Markus KARG. All rights reserved.
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

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import javax.net.ssl.SSLContext;
import jakarta.ws.rs.SeBootstrap;
import jakarta.ws.rs.core.Application;

import org.glassfish.jersey.server.JerseySeBootstrapConfiguration;
import org.glassfish.jersey.server.spi.WebServer;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.JdkSslContext;

/**
 * Jersey {@code Server} implementation based on Netty {@link Channel}.
 *
 * @author Markus KARG (markus@headcrashing.eu)
 * @since 3.1.0
 */
final class NettyHttpServer implements WebServer {

    private final NettyHttpContainer container;

    private final ServerBootstrap serverBootstrap;

    private volatile Channel channel;

    private final int port;

    NettyHttpServer(final Application application, final JerseySeBootstrapConfiguration configuration) {
        this(new NettyHttpContainer(application), configuration);
    }

    NettyHttpServer(final Class<? extends Application> applicationClass,
                    final JerseySeBootstrapConfiguration configuration) {
        this(new NettyHttpContainer(applicationClass), configuration);
    }

    NettyHttpServer(final NettyHttpContainer container, final JerseySeBootstrapConfiguration configuration) {
        final SSLContext sslContext = configuration.sslContext();
        final SeBootstrap.Configuration.SSLClientAuthentication sslClientAuthentication = configuration
                .sslClientAuthentication();

        final URI uri = configuration.uri(true);
        this.port = NettyHttpContainerProvider.getPort(uri);

        this.container = container;
        this.serverBootstrap = NettyHttpContainerProvider.createServerBootstrap(
                uri,
                this.container,
                configuration.isHttps()
                        ? new JdkSslContext(sslContext, false, nettyClientAuth(sslClientAuthentication))
                        : null
        );

        if (configuration.autoStart()) {
            this.channel = NettyHttpContainerProvider.startServer(this.port, this.container, this.serverBootstrap, false);
        }
    }

    private static final ClientAuth nettyClientAuth(
            final SeBootstrap.Configuration.SSLClientAuthentication sslClientAuthentication) {
        switch (sslClientAuthentication) {
        case MANDATORY:
            return ClientAuth.REQUIRE;
        case OPTIONAL:
            return ClientAuth.OPTIONAL;
        default:
            return ClientAuth.NONE;
        }
    }

    @Override
    public final NettyHttpContainer container() {
        return this.container;
    }

    @Override
    public final int port() {
        return this.channel == null ? this.port : ((InetSocketAddress) this.channel.localAddress()).getPort();
    }

    @Override
    public final CompletableFuture<Object> start() {
        return this.channel != null ? CompletableFuture.completedFuture(this.channel)
                : CompletableFuture.supplyAsync(() -> {
                    try {
                        this.channel = NettyHttpContainerProvider.startServer(this.port, this.container,
                                this.serverBootstrap, false);
                        return this.channel;
                    } catch (final Exception e) {
                        throw new CompletionException(e);
                    }
                });
    }

    @Override
    public final CompletableFuture<Void> stop() {
        return this.channel == null ? CompletableFuture.completedFuture(null) : CompletableFuture.supplyAsync(() -> {
            try {
                return this.channel.close().get();
            } catch (final Exception e) {
                throw new CompletionException(e);
            }
        });
    }

    @Override
    public final <T> T unwrap(final Class<T> nativeClass) {
        return nativeClass.cast(this.channel == null ? this.serverBootstrap : this.channel);
    }

}
