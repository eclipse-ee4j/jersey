/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.e2e.server.mvc;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import javax.ws.rs.Path;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import javax.annotation.Priority;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.mvc.MvcFeature;
import org.glassfish.jersey.server.mvc.Template;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.glassfish.jersey.tests.e2e.server.mvc.provider.TestViewProcessor;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author Paul Sandoz
 * @author Michal Gajdos
 */
public class ImplicitViewWithResourceFilterTest extends JerseyTest {

    @Override
    protected Application configure() {
        enable(TestProperties.DUMP_ENTITY);
        enable(TestProperties.LOG_TRAFFIC);

        return new ResourceConfig(ImplicitTemplate.class)
                .register(MvcFeature.class)
                .register(FilterOne.class)
                .register(FilterTwo.class)
                .register(TestViewProcessor.class);
    }

    @Path("/")
    @Template
    public static class ImplicitTemplate {

        public String toString() {
            return "ImplicitTemplate";
        }
    }

    @Priority(10)
    public static class FilterOne implements ContainerRequestFilter, ContainerResponseFilter {

        @Override
        public void filter(final ContainerRequestContext requestContext) throws IOException {
            List<String> xTest = requestContext.getHeaders().get("X-TEST");
            assertNull(xTest);

            requestContext.getHeaders().add("X-TEST", "one");
        }

        @Override
        public void filter(final ContainerRequestContext requestContext, final ContainerResponseContext responseContext) throws
                IOException {
            List<String> rxTest = requestContext.getHeaders().get("X-TEST");
            assertEquals(2, rxTest.size());
            assertEquals("one", rxTest.get(0));
            assertEquals("two", rxTest.get(1));

            List<Object> xTest = responseContext.getHeaders().get("X-TEST");
            assertEquals(1, xTest.size());
            assertEquals("two", xTest.get(0));

            assertNull(responseContext.getHeaders().get("Y-TEST"));
            responseContext.getHeaders().add("Y-TEST", "one");
        }
    }

    @Priority(20)
    public static class FilterTwo implements ContainerRequestFilter, ContainerResponseFilter {

        @Override
        public void filter(final ContainerRequestContext requestContext) throws IOException {
            List<String> xTest = requestContext.getHeaders().get("X-TEST");
            assertEquals(1, xTest.size());
            assertEquals("one", xTest.get(0));

            requestContext.getHeaders().add("X-TEST", "two");
        }

        @Override
        public void filter(final ContainerRequestContext requestContext, final ContainerResponseContext responseContext) throws
                IOException {
            List<String> rxTest = requestContext.getHeaders().get("X-TEST");
            assertEquals(2, rxTest.size());
            assertEquals("one", rxTest.get(0));
            assertEquals("two", rxTest.get(1));

            assertNull(responseContext.getHeaders().get("X-TEST"));
            assertNull(responseContext.getHeaders().get("Y-TEST"));

            responseContext.getHeaders().add("X-TEST", "two");
        }
    }

    @Test
    public void testImplicitTemplate() throws IOException {
        final Invocation.Builder request = target("/").request();

        Response cr = request.get(Response.class);
        assertEquals(200, cr.getStatus());
        List<Object> xTest = cr.getMetadata().get("X-TEST");
        assertEquals(1, xTest.size());
        assertEquals("two", xTest.get(0));

        List<Object> yTest = cr.getMetadata().get("Y-TEST");
        assertEquals(1, yTest.size());
        assertEquals("one", yTest.get(0));

        Properties p = new Properties();
        p.load(cr.readEntity(InputStream.class));
        assertEquals("/org/glassfish/jersey/tests/e2e/server/mvc/ImplicitViewWithResourceFilterTest/ImplicitTemplate/index.testp",
                p.getProperty("path"));
        assertEquals("ImplicitTemplate", p.getProperty("model"));
    }
}
