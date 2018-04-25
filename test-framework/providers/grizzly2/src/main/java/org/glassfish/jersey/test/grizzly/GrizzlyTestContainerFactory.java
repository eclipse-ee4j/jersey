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

package org.glassfish.jersey.test.grizzly;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.core.UriBuilder;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.test.DeploymentContext;
import org.glassfish.jersey.test.spi.TestContainer;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.glassfish.jersey.test.spi.TestContainerFactory;
import org.glassfish.jersey.test.spi.TestHelper;

import org.glassfish.grizzly.http.server.HttpServer;

/**
 * Jersey test framework container factory implementation based on Grizzly 2.x HTTP server.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public class GrizzlyTestContainerFactory implements TestContainerFactory {

    private static class GrizzlyTestContainer implements TestContainer {

        private static final Logger LOGGER = Logger.getLogger(GrizzlyTestContainer.class.getName());

        private URI baseUri;

        private final HttpServer server;

        private GrizzlyTestContainer(final URI baseUri, final DeploymentContext context) {
            this.baseUri = UriBuilder.fromUri(baseUri).path(context.getContextPath()).build();

            if (LOGGER.isLoggable(Level.INFO)) {
                LOGGER.info("Creating GrizzlyTestContainer configured at the base URI "
                        + TestHelper.zeroPortToAvailablePort(baseUri));
            }

            this.server = GrizzlyHttpServerFactory.createHttpServer(this.baseUri, context.getResourceConfig(), false);
        }

        @Override
        public ClientConfig getClientConfig() {
            return null;
        }

        @Override
        public URI getBaseUri() {
            return baseUri;
        }

        @Override
        public void start() {
            if (server.isStarted()) {
                LOGGER.log(Level.WARNING, "Ignoring start request - GrizzlyTestContainer is already started.");

            } else {
                LOGGER.log(Level.FINE, "Starting GrizzlyTestContainer...");
                try {
                    server.start();

                    if (baseUri.getPort() == 0) {
                        baseUri = UriBuilder.fromUri(baseUri)
                                .port(server.getListener("grizzly").getPort())
                                .build();
                        LOGGER.log(Level.INFO, "Started GrizzlyTestContainer at the base URI " + baseUri);
                    }
                } catch (final IOException ioe) {
                    throw new TestContainerException(ioe);
                }
            }
        }

        @Override
        public void stop() {
            if (server.isStarted()) {
                LOGGER.log(Level.FINE, "Stopping GrizzlyTestContainer...");
                this.server.shutdownNow();
            } else {
                LOGGER.log(Level.WARNING, "Ignoring stop request - GrizzlyTestContainer is already stopped.");
            }
        }
    }

    @Override
    public TestContainer create(final URI baseUri, final DeploymentContext context) {
        return new GrizzlyTestContainer(baseUri, context);
    }
}
