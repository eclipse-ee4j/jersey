/*
 * Copyright (c) 2025 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.helidon;

import io.helidon.webserver.WebServer;
import jakarta.ws.rs.RuntimeType;
import jakarta.ws.rs.core.UriBuilder;
import org.glassfish.jersey.internal.util.PropertiesHelper;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.jupiter.api.AfterEach;

import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class AbstractHelidonServerTester {
    private static final Logger LOGGER = Logger.getLogger(AbstractHelidonServerTester.class.getName());

    public static final String CONTEXT = "";
    private static final int DEFAULT_PORT = 0; // rather Helidon choose than 9998

    /**
     * Get the port to be used for test application deployments.
     *
     * @return The HTTP port of the URI
     */
    protected final int getPort() {
        if (server != null) {
            return server.port();
        }

        final String value = PropertiesHelper.getSystemProperty("jersey.config.test.container.port").run();
        if (value != null) {

            try {
                final int i = Integer.parseInt(value);
                if (i < 0) {
                    throw new NumberFormatException("Value is negative.");
                }
                return i;
            } catch (NumberFormatException e) {
                LOGGER.log(Level.CONFIG,
                        "Value of 'jersey.config.test.container.port'"
                                + " property is not a valid non-negative integer [" + value + "]."
                                + " Reverting to default [" + DEFAULT_PORT + "].",
                        e);
            }
        }
        return DEFAULT_PORT;
    }

    private final int getPort(RuntimeType runtimeType) {
        switch (runtimeType) {
            case SERVER:
                return getPort();
            case CLIENT:
                return server.port();
            default:
                throw new IllegalStateException("Unexpected runtime type");
        }
    }

    private volatile WebServer server;

    public UriBuilder getUri() {
        return UriBuilder.fromUri("http://localhost").port(getPort(RuntimeType.CLIENT)).path(CONTEXT);
    }

    public void startServer(Class<?>... resources) {
        ResourceConfig config = new ResourceConfig(resources);
        config.register(new LoggingFeature(LOGGER, LoggingFeature.Verbosity.PAYLOAD_ANY));
        startServer(config);
    }

    public void startServer(ResourceConfig config) {
        final URI baseUri = getBaseUri();
        if (server == null) {
            server = HelidonHttpContainerBuilder.builder()
                    .uri(baseUri)
                    .application(config)
                    .build();
        }
        server.start();
        LOGGER.log(Level.INFO, "Helidon-http server started on base uri: " + getBaseUri());
    }

    public URI getBaseUri() {
        return UriBuilder.fromUri("http://localhost/").port(getPort(RuntimeType.SERVER)).build();
    }

    public void stopServer() {
        try {
            server.stop();
            server = null;
            LOGGER.log(Level.INFO, "Helidon-http server stopped.");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @AfterEach
    public void tearDown() {
        if (server != null) {
            stopServer();
        }
    }

}
