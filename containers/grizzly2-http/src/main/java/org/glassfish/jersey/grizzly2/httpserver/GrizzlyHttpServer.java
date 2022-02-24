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

package org.glassfish.jersey.grizzly2.httpserver;

import static jakarta.ws.rs.SeBootstrap.Configuration.SSLClientAuthentication.MANDATORY;
import static jakarta.ws.rs.SeBootstrap.Configuration.SSLClientAuthentication.OPTIONAL;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import javax.net.ssl.SSLContext;
import jakarta.ws.rs.SeBootstrap;
import jakarta.ws.rs.core.Application;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;
import org.glassfish.jersey.server.JerseySeBootstrapConfiguration;
import org.glassfish.jersey.server.spi.WebServer;

/**
 * Jersey {@code Server} implementation based on Grizzly {@link HttpServer}.
 *
 * @author Markus KARG (markus@headcrashing.eu)
 * @since 3.1.0
 */
final class GrizzlyHttpServer implements WebServer {

    private final GrizzlyHttpContainer container;

    private final HttpServer httpServer;

    GrizzlyHttpServer(final Application application, final JerseySeBootstrapConfiguration configuration) {
        this(new GrizzlyHttpContainer(application), configuration);
    }

    GrizzlyHttpServer(final Class<? extends Application> applicationClass,
                      final JerseySeBootstrapConfiguration configuration) {
        this(new GrizzlyHttpContainer(applicationClass), configuration);
    }

    private GrizzlyHttpServer(final GrizzlyHttpContainer container, final JerseySeBootstrapConfiguration configuration) {
        final SSLContext sslContext = configuration.sslContext();
        final SeBootstrap.Configuration.SSLClientAuthentication sslClientAuthentication = configuration
                .sslClientAuthentication();

        this.container = container;
        this.httpServer = GrizzlyHttpServerFactory.createHttpServer(
                configuration.uri(true),
                this.container,
                configuration.isHttps(),
                configuration.isHttps() ? new SSLEngineConfigurator(sslContext, false,
                        sslClientAuthentication == MANDATORY,
                        sslClientAuthentication == OPTIONAL) : null,
                configuration.autoStart());
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
