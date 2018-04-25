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
import java.util.EnumSet;
import java.util.EventListener;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.UriBuilder;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletRegistration;
import javax.servlet.http.HttpServlet;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpContainer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.test.DeploymentContext;
import org.glassfish.jersey.test.ServletDeploymentContext;
import org.glassfish.jersey.test.spi.TestContainer;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.glassfish.jersey.test.spi.TestContainerFactory;
import org.glassfish.jersey.test.spi.TestHelper;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.servlet.WebappContext;

/**
 * A Servlet-based test container factory for creating test container instances using Grizzly.
 *
 * @author Srinivas Bhimisetty
 * @author Pavel Bucek (pavel.bucek at oracle.com)
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public class GrizzlyWebTestContainerFactory implements TestContainerFactory {

    @Override
    public TestContainer create(final URI baseUri, final DeploymentContext context) {
        if (!(context instanceof ServletDeploymentContext)) {
            throw new IllegalArgumentException("The deployment context must be an instance of ServletDeploymentContext.");
        }

        return new GrizzlyWebTestContainer(baseUri, (ServletDeploymentContext) context);
    }

    /**
     * This class has methods for instantiating, starting and stopping the Grizzly 2 Web
     * Server.
     */
    private static class GrizzlyWebTestContainer implements TestContainer {

        private static final Logger LOGGER = Logger.getLogger(GrizzlyWebTestContainer.class.getName());

        private URI baseUri;

        private final ServletDeploymentContext deploymentContext;

        private HttpServer server;

        private GrizzlyWebTestContainer(final URI baseUri, final ServletDeploymentContext context) {
            this.baseUri = UriBuilder.fromUri(baseUri)
                    .path(context.getContextPath())
                    .path(context.getServletPath())
                    .build();

            LOGGER.info("Creating GrizzlyWebTestContainer configured at the base URI "
                    + TestHelper.zeroPortToAvailablePort(baseUri));

            this.deploymentContext = context;
            instantiateGrizzlyWebServer();
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
                LOGGER.log(Level.WARNING, "Ignoring start request - GrizzlyWebTestContainer is already started.");

            } else {
                LOGGER.log(Level.FINE, "Starting GrizzlyWebTestContainer...");
                try {
                    server.start();

                    if (baseUri.getPort() == 0) {
                        baseUri = UriBuilder.fromUri(baseUri)
                                .port(server.getListener("grizzly").getPort())
                                .build();
                        LOGGER.log(Level.INFO, "Started GrizzlyWebTestContainer at the base URI " + baseUri);
                    }
                } catch (final IOException ioe) {
                    throw new TestContainerException(ioe);
                }
            }
        }

        @Override
        public void stop() {
            if (server.isStarted()) {
                LOGGER.log(Level.FINE, "Stopping GrizzlyWebTestContainer...");
                this.server.shutdownNow();
            } else {
                LOGGER.log(Level.WARNING, "Ignoring stop request - GrizzlyWebTestContainer is already stopped.");
            }
        }

        private void instantiateGrizzlyWebServer() {

            String contextPathLocal = deploymentContext.getContextPath();
            if (!contextPathLocal.isEmpty() && !contextPathLocal.startsWith("/")) {
                contextPathLocal = "/" + contextPathLocal;
            }

            String servletPathLocal = deploymentContext.getServletPath();
            if (!servletPathLocal.startsWith("/")) {
                servletPathLocal = "/" + servletPathLocal;
            }
            if (servletPathLocal.endsWith("/")) {
                servletPathLocal += "*";
            } else {
                servletPathLocal += "/*";
            }

            final WebappContext context = new WebappContext("TestContext", contextPathLocal);

            // servlet class and servlet instance can be both null or one of them is specified exclusively.
            final HttpServlet servletInstance = deploymentContext.getServletInstance();
            final Class<? extends HttpServlet> servletClass = deploymentContext.getServletClass();
            if (servletInstance != null || servletClass != null) {
                final ServletRegistration registration;
                if (servletInstance != null) {
                    registration = context.addServlet(servletInstance.getClass().getName(), servletInstance);
                } else {
                    registration = context.addServlet(servletClass.getName(), servletClass);
                }
                registration.setInitParameters(deploymentContext.getInitParams());
                registration.addMapping(servletPathLocal);
            }

            for (final Class<? extends EventListener> eventListener : deploymentContext.getListeners()) {
                context.addListener(eventListener);
            }

            final Map<String, String> contextParams = deploymentContext.getContextParams();
            for (final String contextParamName : contextParams.keySet()) {
                context.addContextInitParameter(contextParamName, contextParams.get(contextParamName));
            }

            // Filter support
            if (deploymentContext.getFilters() != null) {
                for (final ServletDeploymentContext.FilterDescriptor filterDescriptor : deploymentContext.getFilters()) {

                    final FilterRegistration filterRegistration =
                            context.addFilter(filterDescriptor.getFilterName(), filterDescriptor.getFilterClass());

                    filterRegistration.setInitParameters(filterDescriptor.getInitParams());
                    filterRegistration.addMappingForUrlPatterns(
                            grizzlyDispatcherTypes(filterDescriptor.getDispatcherTypes()),
                            true,
                            servletPathLocal);
                }
            }

            try {
                server = GrizzlyHttpServerFactory.createHttpServer(baseUri, (GrizzlyHttpContainer) null, false, null, false);
                context.deploy(server);
            } catch (final ProcessingException ex) {
                throw new TestContainerException(ex);
            }
        }

        private EnumSet<DispatcherType> grizzlyDispatcherTypes(final Set<DispatcherType> dispatcherTypes) {
            final Set<DispatcherType> grizzlyDispatcherTypes = new HashSet<>();
            for (final javax.servlet.DispatcherType servletDispatchType : dispatcherTypes) {
                grizzlyDispatcherTypes.add(DispatcherType.valueOf(servletDispatchType.name()));
            }
            return EnumSet.copyOf(grizzlyDispatcherTypes);
        }
    }
}
