/*
 * Copyright (c) 2011, 2019 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.test.inmemory;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.server.ApplicationHandler;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.spi.Container;
import org.glassfish.jersey.test.DeploymentContext;
import org.glassfish.jersey.test.spi.TestContainer;
import org.glassfish.jersey.test.spi.TestContainerFactory;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * In-memory test container factory.
 *
 * @author Pavel Bucek
 * @author Marek Potociar
 */
public class InMemoryTestContainerFactory implements TestContainerFactory {

    private static class InMemoryTestContainer implements TestContainer {

        private final URI baseUri;
        private final ApplicationHandler appHandler;
        private final InMemoryContainer container;
        private final AtomicBoolean started = new AtomicBoolean(false);
        private static final Logger LOGGER = Logger.getLogger(InMemoryTestContainer.class.getName());

        private InMemoryTestContainer(final URI baseUri, final DeploymentContext context) {
            this.baseUri = UriBuilder.fromUri(baseUri).path(context.getContextPath()).build();

            if (LOGGER.isLoggable(Level.INFO)) {
                LOGGER.info("Creating InMemoryTestContainer configured at the base URI " + this.baseUri);
            }

            this.appHandler = new ApplicationHandler(context.getResourceConfig());
            this.container = new InMemoryContainer();
        }

        @Override
        public ClientConfig getClientConfig() {
            return new ClientConfig().connectorProvider(new InMemoryConnector.Provider(baseUri, appHandler));
        }

        @Override
        public URI getBaseUri() {
            return baseUri;
        }

        @Override
        public void start() {
            if (started.compareAndSet(false, true)) {
                LOGGER.log(Level.FINE, "Starting InMemoryContainer...");
                appHandler.onStartup(container);
            } else {
                LOGGER.log(Level.WARNING, "Ignoring start request - InMemoryTestContainer is already started.");
            }
        }

        @Override
        public void stop() {
            if (started.compareAndSet(true, false)) {
                LOGGER.log(Level.FINE, "Stopping InMemoryContainer...");
                appHandler.onShutdown(container);
            } else {
                LOGGER.log(Level.WARNING, "Ignoring stop request - InMemoryTestContainer is already stopped.");
            }
        }


        private class InMemoryContainer implements Container {
            @Override
            public ResourceConfig getConfiguration() {
                return appHandler.getConfiguration();
            }

            @Override
            public ApplicationHandler getApplicationHandler() {
                return appHandler;
            }

            /**
             * @throws UnsupportedOperationException because {@link org.glassfish.jersey.test.spi.TestContainer}
             * doesn't have reload method.
             */
            @Override
            public void reload() throws UnsupportedOperationException {
                throw new UnsupportedOperationException();
            }

            /**
             * @throws UnsupportedOperationException because {@link org.glassfish.jersey.test.spi.TestContainer}
             * doesn't have reload method.
             */
            @Override
            public void reload(ResourceConfig configuration) throws UnsupportedOperationException {
                throw new UnsupportedOperationException();
            }
        }
    }

    @Override
    public TestContainer create(final URI baseUri, final DeploymentContext context) throws IllegalArgumentException {
        return new InMemoryTestContainer(baseUri, context);
    }
}
