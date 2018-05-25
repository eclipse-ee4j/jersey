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

package org.glassfish.jersey.grizzly2.httpserver;

import static java.lang.Boolean.TRUE;
import static javax.ws.rs.JAXRS.Configuration.SSLClientAuthentication.MANDATORY;
import static javax.ws.rs.JAXRS.Configuration.SSLClientAuthentication.OPTIONAL;

import java.io.IOException;
import java.net.URI;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import javax.net.ssl.SSLContext;
import javax.ws.rs.JAXRS;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.UriBuilder;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.server.spi.Server;

/**
 * Jersey {@code Server} implementation based on Grizzly {@link HttpServer}.
 *
 * @author Markus KARG (markus@headcrashing.eu)
 * @since 2.28
 */
public final class GrizzlyHttpServer implements Server {

    private final GrizzlyHttpContainer container;

    private final HttpServer httpServer;

    GrizzlyHttpServer(final Application application, final JAXRS.Configuration configuration) {
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

        this.container = new GrizzlyHttpContainer(application);
        this.httpServer = GrizzlyHttpServerFactory.createHttpServer(uri, this.container, "HTTPS".equals(protocol),
                new SSLEngineConfigurator(sslContext, false, sslClientAuthentication == OPTIONAL,
                        sslClientAuthentication == MANDATORY),
                autoStart);
    }

    @Override
    public final GrizzlyHttpContainer container() {
        return this.container;
    }

    @Override
    public final int port() {
        return this.httpServer.getListener("grizzly").getPort();
    }

    @Override
    public final CompletableFuture<Void> start() {
        return CompletableFuture.runAsync(() -> {
            try {
                this.httpServer.start();
            } catch (final IOException e) {
                throw new CompletionException(e);
            }
        });
    }

    @Override
    public final CompletableFuture<Void> stop() {
        return CompletableFuture.runAsync(this.httpServer::shutdownNow);
    }

    @Override
    public final <T> T unwrap(final Class<T> nativeClass) {
        return nativeClass.cast(this.httpServer);
    }

}
