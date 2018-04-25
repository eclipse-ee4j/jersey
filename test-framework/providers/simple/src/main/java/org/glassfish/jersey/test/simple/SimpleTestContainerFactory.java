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

package org.glassfish.jersey.test.simple;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.UriBuilder;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.simple.SimpleContainerFactory;
import org.glassfish.jersey.simple.SimpleServer;
import org.glassfish.jersey.test.DeploymentContext;
import org.glassfish.jersey.test.spi.TestContainer;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.glassfish.jersey.test.spi.TestContainerFactory;

/**
 * Factory for testing {@link org.glassfish.jersey.simple.SimpleContainer}.
 *
 * @author Arul Dhesiaseelan (aruld@acm.org)
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public class SimpleTestContainerFactory implements TestContainerFactory {

    private static class SimpleTestContainer implements TestContainer {

        private static final Logger LOGGER = Logger.getLogger(SimpleTestContainer.class.getName());

        private final DeploymentContext deploymentContext;

        private URI baseUri;
        private SimpleServer server;

        private SimpleTestContainer(final URI baseUri, final DeploymentContext context) {
            final URI base = UriBuilder.fromUri(baseUri).path(context.getContextPath()).build();

            if (!"/".equals(base.getRawPath())) {
                throw new TestContainerException(String.format(
                        "Cannot deploy on %s. Simple framework container only supports deployment on root path.",
                        base.getRawPath()));
            }

            this.baseUri = base;

            if (LOGGER.isLoggable(Level.INFO)) {
                LOGGER.info("Creating SimpleTestContainer configured at the base URI " + this.baseUri);
            }
            this.deploymentContext = context;
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
            LOGGER.log(Level.FINE, "Starting SimpleTestContainer...");

            try {
                server = SimpleContainerFactory.create(baseUri, deploymentContext.getResourceConfig());

                if (baseUri.getPort() == 0) {
                    baseUri = UriBuilder.fromUri(baseUri)
                            .port(server.getPort())
                            .build();

                    LOGGER.log(Level.INFO, "Started SimpleTestContainer at the base URI " + baseUri);
                }
            } catch (ProcessingException e) {
                throw new TestContainerException(e);
            }
        }

        @Override
        public void stop() {
            LOGGER.log(Level.FINE, "Stopping SimpleTestContainer...");
            try {
                this.server.close();
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, "Error Stopping SimpleTestContainer...", ex);
            } finally {
                this.server = null;
            }
        }
    }

    @Override
    public TestContainer create(final URI baseUri, final DeploymentContext context) throws IllegalArgumentException {
        return new SimpleTestContainer(baseUri, context);
    }
}
