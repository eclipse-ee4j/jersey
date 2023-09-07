/*
 * Copyright (c) 2021, 2023 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.jetty;

import static jakarta.ws.rs.SeBootstrap.Configuration.SSLClientAuthentication.MANDATORY;
import static jakarta.ws.rs.SeBootstrap.Configuration.SSLClientAuthentication.OPTIONAL;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import jakarta.ws.rs.SeBootstrap;
import jakarta.ws.rs.core.Application;

import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.glassfish.jersey.server.ContainerFactory;
import org.glassfish.jersey.server.JerseySeBootstrapConfiguration;
import org.glassfish.jersey.server.spi.WebServer;

/**
 * Jersey {@code Server} implementation based on Jetty
 * {@link org.eclipse.jetty.server.Server Server}.
 *
 * @author Markus KARG (markus@headcrashing.eu)
 * @since 3.1.0
 */
final class JettyHttpServer implements WebServer {

    private final JettyHttpContainer container;

    private final org.eclipse.jetty.server.Server httpServer;

    JettyHttpServer(final Application application, final JerseySeBootstrapConfiguration configuration) {
        this(ContainerFactory.createContainer(JettyHttpContainer.class, application), configuration);
    }

    JettyHttpServer(final Class<? extends Application> applicationClass,
                    final JerseySeBootstrapConfiguration configuration) {
        this(new JettyHttpContainer(applicationClass), configuration);
    }

    JettyHttpServer(final JettyHttpContainer container, final JerseySeBootstrapConfiguration configuration) {
        final SeBootstrap.Configuration.SSLClientAuthentication sslClientAuthentication = configuration
                .sslClientAuthentication();
        final SslContextFactory.Server sslContextFactory;
        if (configuration.isHttps()) {
            sslContextFactory = new SslContextFactory.Server();
            sslContextFactory.setSslContext(configuration.sslContext());
            sslContextFactory.setWantClientAuth(sslClientAuthentication == OPTIONAL);
            sslContextFactory.setNeedClientAuth(sslClientAuthentication == MANDATORY);
        } else {
            sslContextFactory = null;
        }
        this.container = container;
        this.httpServer = JettyHttpContainerFactory.createServer(
                configuration.uri(true),
                sslContextFactory,
                this.container,
                configuration.autoStart());
    }

    @Override
    public final JettyHttpContainer container() {
        return this.container;
    }

    @Override
    public final int port() {
        final ServerConnector serverConnector = (ServerConnector) this.httpServer.getConnectors()[0];
        final int configuredPort = serverConnector.getPort();
        final int localPort = serverConnector.getLocalPort();
        return localPort < 0 ? configuredPort : localPort;
    }

    @Override
    public final CompletableFuture<Void> start() {
        return CompletableFuture.runAsync(() -> {
            try {
                this.httpServer.start();
            } catch (final Exception e) {
                throw new CompletionException(e);
            }
        });
    }

    @Override
    public final CompletableFuture<Void> stop() {
        return CompletableFuture.runAsync(() -> {
            try {
                this.httpServer.stop();
            } catch (final Exception e) {
                throw new CompletionException(e);
            }
        });
    }

    @Override
    public final <T> T unwrap(final Class<T> nativeClass) {
        return nativeClass.cast(this.httpServer);
    }

}
