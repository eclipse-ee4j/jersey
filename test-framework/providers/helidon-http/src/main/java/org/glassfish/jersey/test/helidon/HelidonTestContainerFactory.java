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

package org.glassfish.jersey.test.helidon;

import io.helidon.webserver.WebServer;
import jakarta.ws.rs.core.UriBuilder;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.helidon.HelidonHttpContainerBuilder;
import org.glassfish.jersey.test.DeploymentContext;
import org.glassfish.jersey.test.spi.TestContainer;
import org.glassfish.jersey.test.spi.TestContainerFactory;
import org.glassfish.jersey.test.spi.TestHelper;

import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HelidonTestContainerFactory implements TestContainerFactory {

    private static class HelidonTestContainer implements TestContainer {

        private static final Logger LOGGER = Logger.getLogger(HelidonTestContainer.class.getName());

        private URI baseUri;

        private final WebServer server;

        private HelidonTestContainer(final URI baseUri, final DeploymentContext context) {
            this.baseUri = UriBuilder.fromUri(baseUri).path(context.getContextPath()).build();

            if (LOGGER.isLoggable(Level.INFO)) {
                LOGGER.info("Creating HelidonTestContainer configured at the base URI "
                        + TestHelper.zeroPortToAvailablePort(baseUri));
            }

            final HelidonHttpContainerBuilder builder = HelidonHttpContainerBuilder.builder()
                    .application(context.getResourceConfig())
                    .uri(this.baseUri);
            if (context.getSslContext().isPresent() && context.getSslParameters().isPresent()) {
                builder.sslParameters(context.getSslParameters().get());
                builder.sslContext(context.getSslContext().get());
            }
            this.server = builder.build();

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
            if (!server.isRunning()) {
                server.start();
            }
        }

        @Override
        public void stop() {
            if (server.isRunning()) {
                server.stop();
            }
        }
    }
    @Override
    public TestContainer create(URI baseUri, DeploymentContext deploymentContext) {
        return new HelidonTestContainer(baseUri, deploymentContext);
    }
}
