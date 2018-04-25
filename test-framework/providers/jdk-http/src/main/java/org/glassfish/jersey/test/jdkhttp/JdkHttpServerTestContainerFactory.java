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

package org.glassfish.jersey.test.jdkhttp;

import java.net.URI;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.core.UriBuilder;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.test.DeploymentContext;
import org.glassfish.jersey.test.spi.TestContainer;
import org.glassfish.jersey.test.spi.TestContainerFactory;
import org.glassfish.jersey.test.spi.TestHelper;

import com.sun.net.httpserver.HttpServer;

/**
 * Factory for testing {@link org.glassfish.jersey.jdkhttp.JdkHttpHandlerContainer}.
 *
 * @author Miroslav Fuksa
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public class JdkHttpServerTestContainerFactory implements TestContainerFactory {

    private static class JdkHttpServerTestContainer implements TestContainer {

        private URI baseUri;
        private final HttpServer server;
        private final AtomicBoolean started = new AtomicBoolean(false);
        private static final Logger LOGGER = Logger.getLogger(JdkHttpServerTestContainer.class.getName());

        private JdkHttpServerTestContainer(final URI baseUri, final DeploymentContext context) {
            this.baseUri = UriBuilder.fromUri(baseUri).path(context.getContextPath()).build();

            if (LOGGER.isLoggable(Level.INFO)) {
                LOGGER.info("Creating JdkHttpServerTestContainer configured at the base URI "
                        + TestHelper.zeroPortToAvailablePort(baseUri));
            }

            this.server = JdkHttpServerFactory.createHttpServer(this.baseUri, context.getResourceConfig(), false);
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
            if (started.compareAndSet(false, true)) {
                LOGGER.log(Level.FINE, "Starting JdkHttpServerTestContainer...");
                server.start();

                if (baseUri.getPort() == 0) {
                    baseUri = UriBuilder.fromUri(baseUri)
                            .port(server.getAddress().getPort())
                            .build();
                    LOGGER.log(Level.INFO, "Started JdkHttpServerTestContainer at the base URI " + baseUri);
                }
            } else {
                LOGGER.log(Level.WARNING, "Ignoring start request - JdkHttpServerTestContainer is already started.");
            }
        }

        @Override
        public void stop() {
            if (started.compareAndSet(true, false)) {
                LOGGER.log(Level.FINE, "Stopping JdkHttpServerTestContainer...");
                this.server.stop(3);
            } else {
                LOGGER.log(Level.WARNING, "Ignoring stop request - JdkHttpServerTestContainer is already stopped.");
            }
        }
    }

    @Override
    public TestContainer create(final URI baseUri, final DeploymentContext context) throws IllegalArgumentException {
        return new JdkHttpServerTestContainer(baseUri, context);
    }
}
