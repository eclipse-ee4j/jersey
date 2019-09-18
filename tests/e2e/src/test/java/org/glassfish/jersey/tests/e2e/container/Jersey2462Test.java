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

package org.glassfish.jersey.tests.e2e.container;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.Response;

import javax.inject.Inject;
import javax.inject.Provider;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.DeploymentContext;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.grizzly.GrizzlyTestContainerFactory;
import org.glassfish.jersey.test.jetty.JettyTestContainerFactory;
import org.glassfish.jersey.test.simple.SimpleTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.glassfish.jersey.test.spi.TestContainerFactory;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import static org.junit.Assert.assertEquals;

/**
 * Reproducer tests for JERSEY-2462 on Grizzly, Jetty and Simple HTTP server.
 *
 * @author Marek Potociar
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({Jersey2462Test.GrizzlyContainerTest.class,
        Jersey2462Test.JettyContainerTest.class,
        Jersey2462Test.SimpleContainerTest.class})
public class Jersey2462Test {
    private static final String REQUEST_NUMBER = "request-number";

    /**
     * Test resource.
     *
     * @author Marek Potociar
     */
    @Path("echo")
    public static class EchoResource {

        /**
         * Echoes the input message.
         *
         * @param message input message.
         * @return echoed input message.
         */
        @POST
        @Consumes("text/plain")
        @Produces("text/plain")
        public String echo(String message) {
            return message + "" + this.getClass().getPackage().getName();
        }
    }

    /**
     * Filter testing Grizzly request/response injection support into singleton providers.
     */
    @PreMatching
    public static class GrizzlyRequestFilter implements ContainerRequestFilter {
        @Inject
        private Provider<org.glassfish.grizzly.http.server.Request> grizzlyRequest;
        @Inject
        private org.glassfish.grizzly.http.server.Response grizzlyResponse;

        @Override
        public void filter(ContainerRequestContext ctx) throws IOException {
            StringBuilder sb = new StringBuilder();

            // First, make sure there are no null injections.
            if (grizzlyRequest == null) {
                sb.append("Grizzly Request is null.\n");
            }
            if (grizzlyResponse == null) {
                sb.append("Grizzly Response is null.\n");
            }

            if (sb.length() > 0) {
                ctx.abortWith(Response.serverError().entity(sb.toString()).build());
            }

            // let's also test some method calls
            int flags = 0;

            if ("/jersey-2462".equals(grizzlyRequest.get().getContextPath())) {
                flags += 1;
            }
            if (!grizzlyResponse.isCommitted()) {
                flags += 10;
            }
            final String header = grizzlyRequest.get().getHeader(REQUEST_NUMBER);

            ctx.setEntityStream(new ByteArrayInputStream(("filtered-" + flags + "-" + header).getBytes()));
        }
    }

    public static class GrizzlyContainerTest extends JerseyTest {
        @Override
        protected DeploymentContext configureDeployment() {
            return DeploymentContext.builder(new ResourceConfig(EchoResource.class, GrizzlyRequestFilter.class))
                    .contextPath("jersey-2462")
                    .build();
        }

        @Override
        protected TestContainerFactory getTestContainerFactory() throws TestContainerException {
            return new GrizzlyTestContainerFactory();
        }

        /**
         * Reproducer for JERSEY-2462 on Grizzly container.
         */
        @Test
        public void testReqestReponseInjectionIntoSingletonProvider() {
            Jersey2462Test.testReqestReponseInjectionIntoSingletonProvider(target());
        }
    }

    /**
     * Filter testing Jetty request/response injection support into singleton providers.
     */
    @PreMatching
    public static class JettyRequestFilter implements ContainerRequestFilter {
        @Inject
        private Provider<org.eclipse.jetty.server.Request> jettyRequest;
        @Inject
        private Provider<org.eclipse.jetty.server.Response> jettyResponse;

        @Override
        public void filter(ContainerRequestContext ctx) throws IOException {
            StringBuilder sb = new StringBuilder();

            // First, make sure there are no null injections.
            if (jettyRequest == null) {
                sb.append("Jetty Request is null.\n");
            }
            if (jettyResponse == null) {
                sb.append("Jetty Response is null.\n");
            }

            if (sb.length() > 0) {
                ctx.abortWith(Response.serverError().entity(sb.toString()).build());
            }

            // let's also test some method calls
            int flags = 0;

            if ("/echo".equals(jettyRequest.get().getPathInfo())) {
                flags += 1;
            }
            if (!jettyResponse.get().isCommitted()) {
                flags += 10;
            }
            final String header = jettyRequest.get().getHeader(REQUEST_NUMBER);

            ctx.setEntityStream(new ByteArrayInputStream(("filtered-" + flags + "-" + header).getBytes()));
        }
    }


    public static class JettyContainerTest extends JerseyTest {
        @Override
        protected DeploymentContext configureDeployment() {
            return DeploymentContext.builder(new ResourceConfig(EchoResource.class, JettyRequestFilter.class))
                    .build();
        }

        @Override
        protected TestContainerFactory getTestContainerFactory() throws TestContainerException {
            return new JettyTestContainerFactory();
        }

        /**
         * Reproducer for JERSEY-2462 on Grizzly container.
         */
        @Test
        public void testReqestReponseInjectionIntoSingletonProvider() {
            Jersey2462Test.testReqestReponseInjectionIntoSingletonProvider(target());
        }
    }

    /**
     * Filter testing Simple framework request/response injection support into singleton providers.
     */
    @PreMatching
    public static class SimpleRequestFilter implements ContainerRequestFilter {
        @Inject
        private org.simpleframework.http.Request simpleRequest;
        @Inject
        private org.simpleframework.http.Response simpleResponse;

        @Override
        public void filter(ContainerRequestContext ctx) throws IOException {
            StringBuilder sb = new StringBuilder();

            // First, make sure there are no null injections.
            if (simpleRequest == null) {
                sb.append("Simple HTTP framework Request is null.\n");
            }
            if (simpleResponse == null) {
                sb.append("Simple HTTP framework Response is null.\n");
            }

            if (sb.length() > 0) {
                ctx.abortWith(Response.serverError().entity(sb.toString()).build());
            }

            // let's also test some method calls
            int flags = 0;

            if ("/echo".equals(simpleRequest.getAddress().getPath().getPath())) {
                flags += 1;
            }
            if (!simpleResponse.isCommitted()) {
                flags += 10;
            }
            final String header = simpleRequest.getValue(REQUEST_NUMBER);

            ctx.setEntityStream(new ByteArrayInputStream(("filtered-" + flags + "-" + header).getBytes()));
        }
    }

    public static class SimpleContainerTest extends JerseyTest {
        @Override
        protected DeploymentContext configureDeployment() {
            return DeploymentContext.builder(new ResourceConfig(EchoResource.class, SimpleRequestFilter.class))
                    .build();
        }

        @Override
        protected TestContainerFactory getTestContainerFactory() throws TestContainerException {
            return new SimpleTestContainerFactory();
        }

        /**
         * Reproducer for JERSEY-2462 on Grizzly container.
         */
        @Test
        public void testReqestReponseInjectionIntoSingletonProvider() {
            Jersey2462Test.testReqestReponseInjectionIntoSingletonProvider(target());
        }
    }

    /**
     * Reproducer method for JERSEY-2462.
     */
    public static void testReqestReponseInjectionIntoSingletonProvider(WebTarget target) {
        for (int i = 0; i < 10; i++) {
            String response = target.path("echo").request().header(REQUEST_NUMBER, i)
                    .post(Entity.text("test"), String.class);
            // Assert that the request has been filtered and processed by the echo method.
            assertEquals(new EchoResource().echo("filtered-11-" + i), response);
        }
    }
}
