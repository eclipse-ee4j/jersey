/*
 * Copyright (c) 2014, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.server.filter;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.ws.rs.GET;
import javax.ws.rs.NameBinding;
import javax.ws.rs.Path;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;

import javax.annotation.Priority;

import org.glassfish.jersey.server.ApplicationHandler;
import org.glassfish.jersey.server.ContainerResponse;
import org.glassfish.jersey.server.RequestContextBuilder;
import org.glassfish.jersey.server.ResourceConfig;

import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Tests layering of filters applied on appropriate methods (using named bindings) on resource method, sub-method,
 * sub-resource locator, sub-resource method. Jersey 2 does not support full functionality of Jersey 1 speaking about
 * filter layering. Jersey 2 implementation is JAX-RS compliant.
 * <p/>
 * But it could be implemented as Jersey specific extension - JERSEY-2414.
 * Please un-ignore tests whenever JERSEY-2414 fixed.
 *
 * @author Paul Sandoz
 * @author Libor Kramolis (libor.kramolis at oracle.com)
 */
public class LayeredFiltersTest {

    @Path("/")
    public static class ResourceWithSubresourceLocator {
        @Path("sub")
        @One
        public Object get() {
            return new ResourceWithMethod();
        }
    }

    @Path("/")
    public static class ResourceWithMethod {
        @GET
        @Two
        public String get(@Context HttpHeaders hh) {
            List<String> xTest = hh.getRequestHeaders().get("X-TEST");
            assertEquals(2, xTest.size());
            return xTest.get(0) + xTest.get(1);
        }

        @GET
        @Path("submethod")
        @Two
        public String getSubmethod(@Context HttpHeaders hh) {
            List<String> xTest = hh.getRequestHeaders().get("X-TEST");
            assertEquals(2, xTest.size());
            return xTest.get(0) + xTest.get(1);
        }
    }

    @NameBinding
    @Retention(RetentionPolicy.RUNTIME)
    public @interface One {
    }

    @One
    @Priority(Priorities.USER + 1)
    public static class FilterOne implements ContainerRequestFilter, ContainerResponseFilter {
        public void filter(ContainerRequestContext requestContext) throws IOException {
            List<String> xTest = requestContext.getHeaders().get("X-TEST");
            assertNull(xTest);

            requestContext.getHeaders().add("X-TEST", "one");
        }

        public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
                throws IOException {
            List<Object> xTest = responseContext.getHeaders().get("X-TEST");
            assertEquals(1, xTest.size());
            assertEquals("two", xTest.get(0));

            responseContext.getHeaders().add("X-TEST", "one");
        }
    }

    @NameBinding
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Two {
    }

    @Two
    @Priority(Priorities.USER + 2)
    public static class FilterTwo implements ContainerRequestFilter, ContainerResponseFilter {
        public void filter(ContainerRequestContext requestContext) throws IOException {
            List<String> xTest = requestContext.getHeaders().get("X-TEST");
            assertNotNull("FilterOne not called", xTest);
            assertEquals(1, xTest.size());
            assertEquals("one", xTest.get(0));

            requestContext.getHeaders().add("X-TEST", "two");
        }

        public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
                throws IOException {
            List<Object> xTest = responseContext.getHeaders().get("X-TEST");
            assertNull(xTest);

            responseContext.getHeaders().add("X-TEST", "two");
        }
    }

    @Test
    @Ignore("JERSEY-2414 - not yet implemented")
    public void testResourceMethod() throws ExecutionException, InterruptedException {
        final ResourceConfig resourceConfig = new ResourceConfig(ResourceWithSubresourceLocator.class)
                .register(FilterOne.class).register(FilterTwo.class);
        final ApplicationHandler application = new ApplicationHandler(resourceConfig);
        final ContainerResponse response = application.apply(RequestContextBuilder.from("/sub", "GET").build()).get();

        assertEquals(200, response.getStatus());
        assertEquals("onetwo", response.getEntity());
        List<Object> xTest = response.getHeaders().get("X-TEST");
        assertEquals(2, xTest.size());
        assertEquals("two", xTest.get(0));
        assertEquals("one", xTest.get(1));
    }

    @Test
    @Ignore("JERSEY-2414 - not yet implemented")
    public void testResourceSubresourceMethod() throws ExecutionException, InterruptedException {
        final ResourceConfig resourceConfig = new ResourceConfig(ResourceWithSubresourceLocator.class)
                .register(FilterOne.class).register(FilterTwo.class);
        final ApplicationHandler application = new ApplicationHandler(resourceConfig);
        final ContainerResponse response = application.apply(RequestContextBuilder.from("/sub/submethod", "GET")
                .build()).get();

        assertEquals(200, response.getStatus());
        assertEquals("onetwo", response.getEntity());
        List<Object> xTest = response.getHeaders().get("X-TEST");
        assertEquals(2, xTest.size());
        assertEquals("two", xTest.get(0));
        assertEquals("one", xTest.get(1));
    }


    @Path("/")
    @One
    public static class ResourceWithSubresourceLocatorOnClass {
        @Path("sub")
        public Object get() {
            return new ResourceWithMethodOnClass();
        }
    }

    @Path("/")
    @Two
    public static class ResourceWithMethodOnClass {
        @GET
        public String get(@Context HttpHeaders hh) {
            List<String> xTest = hh.getRequestHeaders().get("X-TEST");
            assertEquals(2, xTest.size());
            return xTest.get(0) + xTest.get(1);
        }

        @GET
        @Path("submethod")
        public String getSubmethod(@Context HttpHeaders hh) {
            List<String> xTest = hh.getRequestHeaders().get("X-TEST");
            assertEquals(2, xTest.size());
            return xTest.get(0) + xTest.get(1);
        }
    }

    @Test
    @Ignore("JERSEY-2414 - not yet implemented")
    public void testResourceMethodOnClass() throws ExecutionException, InterruptedException {
        final ResourceConfig resourceConfig = new ResourceConfig(ResourceWithSubresourceLocatorOnClass.class)
                .register(FilterOne.class).register(FilterTwo.class);
        final ApplicationHandler application = new ApplicationHandler(resourceConfig);
        final ContainerResponse response = application.apply(RequestContextBuilder.from("/sub", "GET").build()).get();

        assertEquals(200, response.getStatus());
        assertEquals("onetwo", response.getEntity());
        List<Object> xTest = response.getHeaders().get("X-TEST");
        assertEquals(2, xTest.size());
        assertEquals("two", xTest.get(0));
        assertEquals("one", xTest.get(1));
    }

    @Test
    @Ignore("JERSEY-2414 - not yet implemented")
    public void testResourceSubresourceMethodOnClass() throws ExecutionException, InterruptedException {
        final ResourceConfig resourceConfig = new ResourceConfig(ResourceWithSubresourceLocatorOnClass.class)
                .register(FilterOne.class).register(FilterTwo.class);
        final ApplicationHandler application = new ApplicationHandler(resourceConfig);
        final ContainerResponse response = application.apply(RequestContextBuilder.from("/sub/submethod", "GET").build()).get();

        assertEquals(200, response.getStatus());
        assertEquals("onetwo", response.getEntity());
        List<Object> xTest = response.getHeaders().get("X-TEST");
        assertEquals(2, xTest.size());
        assertEquals("two", xTest.get(0));
        assertEquals("one", xTest.get(1));
    }

    @Path("/")
    public static class ResourceWithMethodMultiple {
        @GET
        @One
        @Two
        public String get(@Context HttpHeaders hh) {
            List<String> xTest = hh.getRequestHeaders().get("X-TEST");
            assertEquals(2, xTest.size());
            return xTest.get(0) + xTest.get(1);
        }

        @GET
        @Path("submethod")
        @One
        @Two
        public String getSubmethod(@Context HttpHeaders hh) {
            List<String> xTest = hh.getRequestHeaders().get("X-TEST");
            assertEquals(2, xTest.size());
            return xTest.get(0) + xTest.get(1);
        }
    }

    @Test
    public void testResourceMethodMultiple() throws ExecutionException, InterruptedException {
        final ResourceConfig resourceConfig = new ResourceConfig(ResourceWithMethodMultiple.class)
                .register(FilterOne.class).register(FilterTwo.class);
        final ApplicationHandler application = new ApplicationHandler(resourceConfig);
        final ContainerResponse response = application.apply(RequestContextBuilder.from("/", "GET").build()).get();

        assertEquals(200, response.getStatus());
        assertEquals("onetwo", response.getEntity());
        List<Object> xTest = response.getHeaders().get("X-TEST");
        assertEquals(2, xTest.size());
        assertEquals("two", xTest.get(0));
        assertEquals("one", xTest.get(1));
    }

    @Test
    public void testResourceSubresourceMethodMultiple() throws ExecutionException, InterruptedException {
        final ResourceConfig resourceConfig = new ResourceConfig(ResourceWithMethodMultiple.class)
                .register(FilterOne.class).register(FilterTwo.class);
        final ApplicationHandler application = new ApplicationHandler(resourceConfig);
        final ContainerResponse response = application.apply(RequestContextBuilder.from("/submethod", "GET").build()).get();

        assertEquals(200, response.getStatus());
        assertEquals("onetwo", response.getEntity());
        List<Object> xTest = response.getHeaders().get("X-TEST");
        assertEquals(2, xTest.size());
        assertEquals("two", xTest.get(0));
        assertEquals("one", xTest.get(1));
    }

}
