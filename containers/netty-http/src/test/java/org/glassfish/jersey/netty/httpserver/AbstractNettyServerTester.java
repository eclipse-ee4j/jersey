/*
 * Copyright (c) 2020, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.netty.httpserver;

import io.netty.channel.Channel;

import java.net.URI;
import java.security.AccessController;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.glassfish.jersey.internal.util.PropertiesHelper;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.After;

/**
 * Abstract Netty Server unit tester.
 */
public abstract class AbstractNettyServerTester {

    private static final Logger LOGGER = Logger.getLogger(AbstractNettyServerTester.class.getName());

    public static final String CONTEXT = "";
    private static final int DEFAULT_PORT = 9994;

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

    private volatile Channel server;
    private String uri;

    public void startServer() {
        startServer(null);
    }

    public void startServer(String uri) {
        this.uri = getUri(uri);
        ResourceConfig config = new ResourceConfig(SimpleResource.class, Resource.class);
        config.register(new LoggingFeature(LOGGER, LoggingFeature.Verbosity.PAYLOAD_ANY));
        final URI baseUri = getBaseUri(this.uri);
        server = NettyHttpContainerProvider.createHttp2Server(baseUri, config, null);
        LOGGER.log(Level.INFO, "Netty-http server started on base uri: " + baseUri);
    }

    public String getUri(String uri) {
        return uri == null ? "http://localhost/" : uri;
    }

    public URI getBaseUri(final String uri) {
        return UriBuilder.fromUri(uri).port(getPort()).build();
    }

    public UriBuilder getUri() {
        return UriBuilder.fromUri(uri).port(getPort()).path(CONTEXT);
    }

    @Path("resource")
    public static class Resource {

        @GET
        @Path("ping")
        public Response get() {
            return Response.ok(1).build();
        }

        @GET
        public Response get1() {
            return Response.ok(2).build();
        }
    }

    @Path("/")
    public static class SimpleResource {

        @GET
        @Path("ping")
        public Response get() {
            return Response.ok(3).build();
        }
    }

    public void stopServer() {
        try {
            server.close();
            server = null;
            LOGGER.log(Level.INFO, "Netty-http server stopped.");
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
