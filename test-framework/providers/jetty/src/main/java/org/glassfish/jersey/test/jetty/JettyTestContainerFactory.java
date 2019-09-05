/*
 * Copyright (c) 2013, 2019 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.test.jetty;

import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.core.UriBuilder;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jetty.JettyHttpContainerFactory;
import org.glassfish.jersey.test.DeploymentContext;
import org.glassfish.jersey.test.spi.TestContainer;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.glassfish.jersey.test.spi.TestContainerFactory;
import org.glassfish.jersey.test.spi.TestHelper;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;

/**
 * Factory for testing {@link org.glassfish.jersey.jetty.JettyHttpContainer}.
 *
 * @author Arul Dhesiaseelan (aruld@acm.org)
 * @author Marek Potociar
 */
public class JettyTestContainerFactory implements TestContainerFactory {

    private static class JettyTestContainer implements TestContainer {

        private static final Logger LOGGER = Logger.getLogger(JettyTestContainer.class.getName());

        private URI baseUri;
        private final Server server;

        private JettyTestContainer(final URI baseUri, final DeploymentContext context) {
            final URI base = UriBuilder.fromUri(baseUri).path(context.getContextPath()).build();

            if (!"/".equals(base.getRawPath())) {
                throw new TestContainerException(String.format(
                        "Cannot deploy on %s. Jetty HTTP container only supports deployment on root path.",
                        base.getRawPath()));
            }

            this.baseUri = base;

            if (LOGGER.isLoggable(Level.INFO)) {
                LOGGER.info("Creating JettyTestContainer configured at the base URI "
                        + TestHelper.zeroPortToAvailablePort(baseUri));
            }

            this.server = JettyHttpContainerFactory.createServer(this.baseUri, context.getResourceConfig(), false);
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
                LOGGER.log(Level.WARNING, "Ignoring start request - JettyTestContainer is already started.");
            } else {
                LOGGER.log(Level.FINE, "Starting JettyTestContainer...");
                try {
                    server.start();

                    if (baseUri.getPort() == 0) {
                        int port = 0;
                        for (final Connector connector : server.getConnectors()) {
                            if (connector instanceof ServerConnector) {
                                port = ((ServerConnector) connector).getLocalPort();
                                break;
                            }
                        }

                        baseUri = UriBuilder.fromUri(baseUri).port(port).build();

                        LOGGER.log(Level.INFO, "Started JettyTestContainer at the base URI " + baseUri);
                    }
                } catch (Exception e) {
                    throw new TestContainerException(e);
                }
            }
        }

        @Override
        public void stop() {
            if (server.isStarted()) {
                LOGGER.log(Level.FINE, "Stopping JettyTestContainer...");
                try {
                    this.server.stop();
                } catch (Exception ex) {
                    LOGGER.log(Level.WARNING, "Error Stopping JettyTestContainer...", ex);
                }
            } else {
                LOGGER.log(Level.WARNING, "Ignoring stop request - JettyTestContainer is already stopped.");
            }
        }
    }

    @Override
    public TestContainer create(final URI baseUri, final DeploymentContext context) throws IllegalArgumentException {
        return new JettyTestContainer(baseUri, context);
    }
}
