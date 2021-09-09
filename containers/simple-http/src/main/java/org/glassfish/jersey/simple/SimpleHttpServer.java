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

package org.glassfish.jersey.simple;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import jakarta.ws.rs.core.Application;

import org.glassfish.jersey.server.JerseySeBootstrapConfiguration;
import org.glassfish.jersey.server.spi.WebServer;

/**
 * Jersey {@code Server} implementation based on Simple framework
 * {@link SimpleServer}.
 *
 * @author Markus KARG (markus@headcrashing.eu)
 * @since 3.1.0
 */
final class SimpleHttpServer implements WebServer {

    private final SimpleContainer container;

    private final SimpleServer simpleServer;

    SimpleHttpServer(final Application application, final JerseySeBootstrapConfiguration configuration) {
        this.container = new SimpleContainer(application);
        this.simpleServer = SimpleContainerFactory.create(
                configuration.uri(false),
                configuration.sslContext(),
                configuration.sslClientAuthentication(),
                this.container,
                configuration.autoStart());
    }

    @Override
    public final SimpleContainer container() {
        return this.container;
    }

    @Override
    public final int port() {
        return this.simpleServer.getPort();
    }

    @Override
    public final CompletableFuture<Void> start() {
        return CompletableFuture.runAsync(() -> {
            try {
                this.simpleServer.start();
            } catch (final Exception e) {
                throw new CompletionException(e);
            }
        });
    }

    @Override
    public final CompletableFuture<Void> stop() {
        return CompletableFuture.runAsync(() -> {
            try {
                this.simpleServer.close();
            } catch (final Exception e) {
                throw new CompletionException(e);
            }
        });
    }

    @Override
    public final <T> T unwrap(final Class<T> nativeClass) {
        return nativeClass.cast(this.simpleServer);
    }

}
