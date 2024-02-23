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

import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.core.Application;
import org.glassfish.jersey.jetty.internal.LocalizationMessages;
import org.glassfish.jersey.server.ContainerFactory;
import org.glassfish.jersey.server.JerseySeBootstrapConfiguration;
import org.glassfish.jersey.server.spi.WebServer;

import java.util.concurrent.CompletableFuture;

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
        throw new ProcessingException(LocalizationMessages.NOT_SUPPORTED());
    }

    @Override
    public final JettyHttpContainer container() {
        throw new ProcessingException(LocalizationMessages.NOT_SUPPORTED());
    }

    @Override
    public final int port() {
        throw new ProcessingException(LocalizationMessages.NOT_SUPPORTED());
    }

    @Override
    public final CompletableFuture<Void> start() {
        throw new ProcessingException(LocalizationMessages.NOT_SUPPORTED());
    }

    @Override
    public final CompletableFuture<Void> stop() {
        throw new ProcessingException(LocalizationMessages.NOT_SUPPORTED());
    }

    @Override
    public final <T> T unwrap(final Class<T> nativeClass) {
        throw new ProcessingException(LocalizationMessages.NOT_SUPPORTED());
    }

}
