/*
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

package org.glassfish.jersey.jdkhttp;

import static java.lang.Boolean.TRUE;
import static jakarta.ws.rs.JAXRS.Configuration.SSLClientAuthentication.MANDATORY;
import static jakarta.ws.rs.JAXRS.Configuration.SSLClientAuthentication.OPTIONAL;

import java.net.URI;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import javax.net.ssl.SSLContext;
import jakarta.ws.rs.JAXRS;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.UriBuilder;

import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.server.spi.Server;

import com.sun.net.httpserver.HttpServer;

/**
 * Jersey {@code Server} implementation based on JDK {@link HttpServer}.
 *
 * @author Markus KARG (markus@headcrashing.eu)
 * @since 2.30
 */
public final class JdkHttpServer implements Server {

    private final JdkHttpHandlerContainer container;

    private final HttpServer httpServer;

    JdkHttpServer(final Application application, final JAXRS.Configuration configuration) {
        final String protocol = configuration.protocol();
        final String host = configuration.host();
        final int port = configuration.port();
        final String rootPath = configuration.rootPath();
        final SSLContext sslContext = configuration.sslContext();
        final JAXRS.Configuration.SSLClientAuthentication sslClientAuthentication = configuration
                .sslClientAuthentication();
        final boolean autoStart = Optional.ofNullable((Boolean) configuration.property(ServerProperties.AUTO_START))
                .orElse(TRUE);
        final URI uri = UriBuilder.newInstance().scheme(protocol.toLowerCase()).host(host).port(port).path(rootPath)
                .build();

        this.container = new JdkHttpHandlerContainer(application);
        this.httpServer = JdkHttpServerFactory.createHttpServer(uri, this.container,
                "HTTPS".equals(protocol) ? sslContext : null, sslClientAuthentication == OPTIONAL,
                sslClientAuthentication == MANDATORY, autoStart);
    }

    @Override
    public final JdkHttpHandlerContainer container() {
        return this.container;
    }

    @Override
    public final int port() {
        return this.httpServer.getAddress().getPort();
    }

    @Override
    public final CompletableFuture<Void> start() {
        return CompletableFuture.runAsync(this.httpServer::start);
    }

    @Override
    public final CompletableFuture<Void> stop() {
        return CompletableFuture.runAsync(() -> this.httpServer.stop(0));
    }

    @Override
    public final <T> T unwrap(final Class<T> nativeClass) {
        return nativeClass.cast(this.httpServer);
    }

}
