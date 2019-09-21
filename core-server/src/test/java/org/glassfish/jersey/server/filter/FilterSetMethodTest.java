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

package org.glassfish.jersey.server.filter;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.ExecutionException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import javax.annotation.Priority;

import org.glassfish.jersey.server.ApplicationHandler;
import org.glassfish.jersey.server.ContainerResponse;
import org.glassfish.jersey.server.RequestContextBuilder;
import org.glassfish.jersey.server.ResourceConfig;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Tests calling {@link ContainerRequestContext#setMethod(String)} in different request/response phases.
 *
 * @author Miroslav Fuksa
 */
public class FilterSetMethodTest {

    @Test
    public void testResponseFilter() throws ExecutionException, InterruptedException {
        ApplicationHandler handler = new ApplicationHandler(new ResourceConfig(Resource.class, ResponseFilter.class));
        ContainerResponse res = handler.apply(RequestContextBuilder.from("", "/resource/setMethod", "GET").build()).get();
        assertEquals(200, res.getStatus());
    }

    @Test
    public void testPreMatchingFilter() throws ExecutionException, InterruptedException {
        ApplicationHandler handler = new ApplicationHandler(new ResourceConfig(Resource.class, PreMatchFilter.class));
        ContainerResponse res = handler.apply(RequestContextBuilder.from("", "/resource/setMethod", "GET").build()).get();
        assertEquals(200, res.getStatus());
    }

    @Test
    public void testPostMatchingFilter() throws ExecutionException, InterruptedException {
        ApplicationHandler handler = new ApplicationHandler(new ResourceConfig(Resource.class, PostMatchFilter.class));
        ContainerResponse res = handler.apply(RequestContextBuilder.from("", "/resource/setMethod", "GET").build()).get();
        assertEquals(200, res.getStatus());
    }

    @Test
    public void testResource() throws ExecutionException, InterruptedException {
        ApplicationHandler handler = new ApplicationHandler(new ResourceConfig(Resource.class, PostMatchFilter.class));
        ContainerResponse res = handler.apply(RequestContextBuilder.from("", "/resource/setMethodInResource",
                "GET").build()).get();
        assertEquals(200, res.getStatus());
    }

    @Test
    public void testSubResourceLocator() throws ExecutionException, InterruptedException {
        ApplicationHandler handler = new ApplicationHandler(new ResourceConfig(AnotherResource.class));
        ContainerResponse res = handler.apply(RequestContextBuilder.from("", "/another/locator",
                "GET").build()).get();
        assertEquals(200, res.getStatus());
    }

    @Test
    public void testResourceUri() throws ExecutionException, InterruptedException {
        ApplicationHandler handler = new ApplicationHandler(new ResourceConfig(ResourceChangeUri.class,
                PreMatchChangingUriFilter.class));
        ContainerResponse res = handler.apply(RequestContextBuilder.from("", "/resourceChangeUri/first",
                "GET").build()).get();
        assertEquals(200, res.getStatus());
        assertEquals("ok", res.getEntity());
    }

    @Path("resourceChangeUri")
    public static class ResourceChangeUri {

        @Path("first")
        @GET
        public String first() {
            fail("should not be called.");
            return "fail";
        }

        @Path("first/a")
        @GET
        public String a() {
            return "ok";
        }
    }

    @Provider
    @Priority(500)
    @PreMatching
    public static class PreMatchChangingUriFilter implements ContainerRequestFilter {
        @Override
        public void filter(ContainerRequestContext requestContext) throws IOException {
            final URI requestUri = requestContext.getUriInfo().getRequestUriBuilder().path("a").build();
            requestContext.setRequestUri(requestUri);
        }
    }


    @Provider
    @Priority(500)
    public static class ResponseFilter implements ContainerResponseFilter {
        @Override
        public void filter(ContainerRequestContext requestContext,
                           ContainerResponseContext responseContext) throws IOException {
            checkExceptionThrown(new SetMethodClosure(requestContext));
            checkExceptionThrown(new SetUriClosure(requestContext));
        }
    }


    @Provider
    @Priority(500)
    @PreMatching
    public static class PreMatchFilter implements ContainerRequestFilter {
        @Override
        public void filter(ContainerRequestContext requestContext) throws IOException {
            new SetMethodClosure(requestContext).f();
            new SetUriClosure(requestContext).f();
            // Should not throw IllegalArgumentException exception in pre match filter.
        }
    }

    @Provider
    @Priority(500)
    public static class PostMatchFilter implements ContainerRequestFilter {
        @Override
        public void filter(final ContainerRequestContext requestContext) throws IOException {
            checkExceptionThrown(new SetMethodClosure(requestContext));
            checkExceptionThrown(new SetUriClosure(requestContext));
        }
    }

    @Path("resource")
    public static class Resource {
        @GET
        @Path("setMethod")
        public Response setMethod() {
            Response response = Response.ok().build();
            return response;
        }

        @GET
        @Path("setMethodInResource")
        public Response setMethodInResource(@Context ContainerRequestContext request) {
            checkExceptionThrown(new SetMethodClosure(request));
            checkExceptionThrown(new SetUriClosure(request));
            return Response.ok().build();
        }


    }

    @Path("another")
    public static class AnotherResource {

        @Path("locator")
        public SubResource methodInSubResource(@Context ContainerRequestContext request) {
            checkExceptionThrown(new SetMethodClosure(request));
            checkExceptionThrown(new SetUriClosure(request));
            return new SubResource();
        }

        public static class SubResource {
            @GET
            public Response get(@Context ContainerRequestContext request) {
                checkExceptionThrown(new SetMethodClosure(request));
                checkExceptionThrown(new SetUriClosure(request));
                return Response.ok().build();
            }
        }
    }


    public static interface Closure {
        void f();
    }

    private static void checkExceptionThrown(Closure f) {
        try {
            f.f();
            fail("Should throw IllegalArgumentException exception.");
        } catch (IllegalStateException exception) {
            // ok - should throw IllegalArgumentException
        }
    }

    public static class SetMethodClosure implements Closure {
        final ContainerRequestContext requestContext;

        public SetMethodClosure(ContainerRequestContext requestContext) {
            this.requestContext = requestContext;
        }

        @Override
        public void f() {
            requestContext.setMethod("OPTIONS");
        }

    }

    public static class SetUriClosure implements Closure {
        final ContainerRequestContext requestContext;

        public SetUriClosure(ContainerRequestContext requestContext) {
            this.requestContext = requestContext;
        }

        @Override
        public void f() {
            requestContext.setRequestUri(requestContext.getUriInfo().getRequestUri());
        }

    }

}
