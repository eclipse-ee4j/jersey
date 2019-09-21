/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.net.URI;
import java.security.AccessController;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.core.UriBuilder;

import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.internal.util.PropertiesHelper;
import org.glassfish.jersey.server.ResourceConfig;

import org.eclipse.jetty.server.Server;
import org.junit.After;

/**
 * Abstract Jetty Server unit tester.
 *
 * @author Paul Sandoz
 * @author Arul Dhesiaseelan (aruld at acm.org)
 * @author Miroslav Fuksa
 */
public abstract class AbstractJettyServerTester {

    private static final Logger LOGGER = Logger.getLogger(AbstractJettyServerTester.class.getName());

    public static final String CONTEXT = "";
    private static final int DEFAULT_PORT = 9998;

    /**
     * Get the port to be used for test application deployments.
     *
     * @return The HTTP port of the URI
     */
    protected final int getPort() {
        final String value = AccessController
                .doPrivileged(PropertiesHelper.getSystemProperty("jersey.config.test.container.port"));
        if (value != null) {

            try {
                final int i = Integer.parseInt(value);
                if (i <= 0) {
                    throw new NumberFormatException("Value not positive.");
                }
                return i;
            } catch (NumberFormatException e) {
                LOGGER.log(Level.CONFIG,
                        "Value of 'jersey.config.test.container.port'"
                                + " property is not a valid positive integer [" + value + "]."
                                + " Reverting to default [" + DEFAULT_PORT + "].",
                        e);
            }
        }
        return DEFAULT_PORT;
    }

    private volatile Server server;

    public UriBuilder getUri() {
        return UriBuilder.fromUri("http://localhost").port(getPort()).path(CONTEXT);
    }

    public void startServer(Class... resources) {
        ResourceConfig config = new ResourceConfig(resources);
        config.register(new LoggingFeature(LOGGER, LoggingFeature.Verbosity.PAYLOAD_ANY));
        final URI baseUri = getBaseUri();
        server = JettyHttpContainerFactory.createServer(baseUri, config);
        LOGGER.log(Level.INFO, "Jetty-http server started on base uri: " + baseUri);
    }

    public void startServer(ResourceConfig config) {
        final URI baseUri = getBaseUri();
        server = JettyHttpContainerFactory.createServer(baseUri, config);
        LOGGER.log(Level.INFO, "Jetty-http server started on base uri: " + baseUri);
    }

    public URI getBaseUri() {
        return UriBuilder.fromUri("http://localhost/").port(getPort()).build();
    }

    public void stopServer() {
        try {
            server.stop();
            server = null;
            LOGGER.log(Level.INFO, "Jetty-http server stopped.");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @After
    public void tearDown() {
        if (server != null) {
            stopServer();
        }
    }
}
