/*
 * Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved.
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

import static jakarta.ws.rs.SeBootstrap.Configuration.SSLClientAuthentication.MANDATORY;
import static jakarta.ws.rs.SeBootstrap.Configuration.SSLClientAuthentication.OPTIONAL;

import java.util.concurrent.CompletableFuture;

import jakarta.ws.rs.SeBootstrap;
import jakarta.ws.rs.core.Application;

import org.glassfish.jersey.server.JerseySeBootstrapConfiguration;
import org.glassfish.jersey.server.spi.WebServer;

import com.sun.net.httpserver.HttpServer;

/**
 * Jersey {@code Server} implementation based on JDK {@link HttpServer}.
 *
 * @author Markus KARG (markus@headcrashing.eu)
 * @since 3.1.0
 */
final class JdkHttpServer implements WebServer {

    private final JdkHttpHandlerContainer container;

    private final HttpServer httpServer;

    JdkHttpServer(final Application application, final JerseySeBootstrapConfiguration configuration) {
        final SeBootstrap.Configuration.SSLClientAuthentication sslClientAuthentication = configuration
                .sslClientAuthentication();

        this.container = new JdkHttpHandlerContainer(application);
        this.httpServer = JdkHttpServerFactory.createHttpServer(
                configuration.uri(false),
                this.container,
                configuration.sslContext(),
                sslClientAuthentication == OPTIONAL,
                sslClientAuthentication == MANDATORY,
                configuration.autoStart());
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
