/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.test.external;

import java.net.URI;
import java.security.AccessController;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.core.UriBuilder;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.internal.util.PropertiesHelper;
import org.glassfish.jersey.test.DeploymentContext;
import org.glassfish.jersey.test.ServletDeploymentContext;
import org.glassfish.jersey.test.spi.TestContainer;
import org.glassfish.jersey.test.spi.TestContainerFactory;

/**
 * A Web-based test container factory for creating test container instances
 * when the Web application is independently deployed in a separate JVM to that
 * of the tests. For example, the application may be deployed to the
 * Glassfish v2 or v3 application server.
 * <P>
 * If you would like to run your tests on a staging server, just set the machine's
 * IP address or fully-qualified domain name to the System Property <I>jersey.test.host</I>.
 *
 * @author Srinivas Bhimisetty
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public class ExternalTestContainerFactory implements TestContainerFactory {

    /**
     * Specifies the active test container host address where application is deployed.
     * The value of the property must be a valid host name or IP address.
     * <p />
     * There is no default value.
     * <p />
     * The name of the configuration property is <tt>{@value}</tt>.
     */
    // TODO rename to jersey.config.test.external.container.host
    public static final String JERSEY_TEST_HOST = "jersey.test.host";

    @Override
    public TestContainer create(final URI baseUri, final DeploymentContext context) throws IllegalArgumentException {
        return new ExternalTestContainer(getBaseURI(baseUri), context);
    }

    private URI getBaseURI(final URI baseUri) {
        String stagingHostName = AccessController.doPrivileged(PropertiesHelper.getSystemProperty(JERSEY_TEST_HOST));
        if (stagingHostName != null) {
            return UriBuilder.fromUri(baseUri).host(stagingHostName).build();
        }

        return baseUri;
    }

    /**
     * Class which helps running tests on an external container. It assumes that
     * the container is started/stopped explicitly and also that the application is
     * pre-deployed.
     */
    private static class ExternalTestContainer implements TestContainer {
        private static final Logger LOGGER = Logger.getLogger(ExternalTestContainer.class.getName());

        private final URI baseUri;

        private ExternalTestContainer(final URI baseUri, final DeploymentContext context) {
            final UriBuilder uriBuilder = UriBuilder.fromUri(baseUri).path(context.getContextPath());
            if (context instanceof ServletDeploymentContext) {
                uriBuilder.path(((ServletDeploymentContext) context).getServletPath());
            }

            this.baseUri = uriBuilder.build();

            if (LOGGER.isLoggable(Level.INFO)) {
                LOGGER.info("Creating ExternalTestContainer configured at the base URI " + this.baseUri);
            }
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
            // do nothing
        }

        @Override
        public void stop() {
            // do nothing
        }

    }

}
