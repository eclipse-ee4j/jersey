/*
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.net.URI;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.core.UriBuilder;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.server.ApplicationHandler;
import org.glassfish.jersey.test.DeploymentContext;
import org.glassfish.jersey.test.spi.TestContainer;
import org.glassfish.jersey.test.spi.TestContainerFactory;

/**
 * In-memory test container factory.
 *
 * @author Pavel Bucek (pavel.bucek at oracle.com)
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public class InMemoryTestContainerFactory implements TestContainerFactory {

    private static class InMemoryTestContainer implements TestContainer {

        private final URI baseUri;
        private final ApplicationHandler appHandler;
        private final AtomicBoolean started = new AtomicBoolean(false);
        private static final Logger LOGGER = Logger.getLogger(InMemoryTestContainer.class.getName());

        private InMemoryTestContainer(final URI baseUri, final DeploymentContext context) {
            this.baseUri = UriBuilder.fromUri(baseUri).path(context.getContextPath()).build();

            if (LOGGER.isLoggable(Level.INFO)) {
                LOGGER.info("Creating InMemoryTestContainer configured at the base URI " + this.baseUri);
            }

            this.appHandler = new ApplicationHandler(context.getResourceConfig());
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
            } else {
                LOGGER.log(Level.WARNING, "Ignoring start request - InMemoryTestContainer is already started.");
            }
        }

        @Override
        public void stop() {
            if (started.compareAndSet(true, false)) {
                LOGGER.log(Level.FINE, "Stopping InMemoryContainer...");
            } else {
                LOGGER.log(Level.WARNING, "Ignoring stop request - InMemoryTestContainer is already stopped.");
            }
        }
    }

    @Override
    public TestContainer create(final URI baseUri, final DeploymentContext context) throws IllegalArgumentException {
        return new InMemoryTestContainer(baseUri, context);
    }
}
