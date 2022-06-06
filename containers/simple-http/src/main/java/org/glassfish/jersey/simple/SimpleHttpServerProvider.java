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

package org.glassfish.jersey.simple;

import jakarta.ws.rs.SeBootstrap;
import jakarta.ws.rs.core.Application;

import org.glassfish.jersey.server.JerseySeBootstrapConfiguration;
import org.glassfish.jersey.server.spi.WebServer;
import org.glassfish.jersey.server.spi.WebServerProvider;

/**
 * Server provider for servers based on Simple framework {@link SimpleServer}.
 *
 * @author Markus KARG (markus@headcrashing.eu)
 * @since 3.1.0
 */
public final class SimpleHttpServerProvider implements WebServerProvider {

    @Override
    public <T extends WebServer> T createServer(final Class<T> type, final Application application,
                                                      final SeBootstrap.Configuration configuration) {
        return WebServerProvider.isSupportedWebServer(SimpleHttpServer.class, type, configuration)
                ? type.cast(new SimpleHttpServer(application, JerseySeBootstrapConfiguration.from(configuration)))
                : null;
    }

    @Override
    public <T extends WebServer> T createServer(final Class<T> type, final Class<? extends Application> applicationClass,
                                                final SeBootstrap.Configuration configuration) {
        return WebServerProvider.isSupportedWebServer(SimpleHttpServer.class, type, configuration)
                ? type.cast(new SimpleHttpServer(applicationClass, JerseySeBootstrapConfiguration.from(configuration)))
                : null;
    }
}
