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

package org.glassfish.jersey.jetty;

import static java.lang.Boolean.TRUE;
import static javax.ws.rs.JAXRS.Configuration.SSLClientAuthentication.MANDATORY;
import static javax.ws.rs.JAXRS.Configuration.SSLClientAuthentication.OPTIONAL;

import java.net.URI;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import javax.net.ssl.SSLContext;
import javax.ws.rs.JAXRS;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.UriBuilder;

import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.glassfish.jersey.server.ContainerFactory;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.server.spi.Server;

/**
 * Jersey {@code Server} implementation based on Jetty
 * {@link org.eclipse.jetty.server.Server Server}.
 *
 * @author Markus KARG (markus@headcrashing.eu)
 * @since 2.28
 */
public final class JettyHttpServer implements Server {

    private final JettyHttpContainer container;

    private final org.eclipse.jetty.server.Server httpServer;

    JettyHttpServer(final Application application, final JAXRS.Configuration configuration) {
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

        final SslContextFactory sslContextFactory;
        if ("https".equals(uri.getScheme())) {
            sslContextFactory = new SslContextFactory();
            sslContextFactory.setSslContext(sslContext);
            sslContextFactory.setWantClientAuth(sslClientAuthentication == OPTIONAL);
            sslContextFactory.setNeedClientAuth(sslClientAuthentication == MANDATORY);
        } else {
            sslContextFactory = null;
        }
        this.container = ContainerFactory.createContainer(JettyHttpContainer.class, application);
        this.httpServer = JettyHttpContainerFactory.createServer(uri, sslContextFactory, this.container, autoStart);
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
