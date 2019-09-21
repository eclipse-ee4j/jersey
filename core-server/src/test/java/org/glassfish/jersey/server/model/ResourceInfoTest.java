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

package org.glassfish.jersey.server.model;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.concurrent.ExecutionException;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;

import org.glassfish.jersey.server.ApplicationHandler;
import org.glassfish.jersey.server.ContainerResponse;
import org.glassfish.jersey.server.RequestContextBuilder;
import org.glassfish.jersey.server.ResourceConfig;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Test cases for injected {@link ResourceInfo} in filters.
 *
 * @author Miroslav Fuksa
 */
public class ResourceInfoTest {


    public static class MyRequestFilter implements ContainerRequestFilter {
        @Context
        ResourceInfo resourceInfo;

        @Override
        public void filter(ContainerRequestContext requestContext) throws IOException {
            requestContext.getHeaders().add("MyRequestFilter-called", "called");
            requestContext.getHeaders().add("MyRequestFilter-class", resourceInfo.getResourceClass().getSimpleName());
            requestContext.getHeaders().add("MyRequestFilter-method", resourceInfo.getResourceMethod().getName());
        }
    }


    public static class MyResponseFilter implements ContainerResponseFilter {
        @Context
        ResourceInfo resourceInfo;

        @Override
        public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
            responseContext.getHeaders().add("MyResponseFilter-called", "called");
            final Class<?> resourceClass = resourceInfo.getResourceClass();
            final Method resourceMethod = resourceInfo.getResourceMethod();
            responseContext.getHeaders().add("MyResponseFilter-class", resourceClass == null ? "<null>"
                    : resourceClass.getSimpleName());
            responseContext.getHeaders().add("MyResponseFilter-method", resourceMethod == null ? "<null>"
                    : resourceMethod.getName());
        }
    }

    @PreMatching
    public static class MyPrematchingFilter implements ContainerRequestFilter {
        @Context
        ResourceInfo resourceInfo;

        @Override
        public void filter(ContainerRequestContext requestContext) throws IOException {
            assertNull(resourceInfo.getResourceClass());
            assertNull(resourceInfo.getResourceMethod());
        }
    }


    @Path("resource")
    public static class MyResource {
        @GET
        public String get(@Context ContainerRequestContext request) {
            assertEquals("called", request.getHeaderString("MyRequestFilter-called"));
            final String className = "MyResource";
            final String methodName = "get";
            assertRequestHeader(request, className, methodName);
            return "get";
        }

        @GET
        @Path("get-child")
        public String getChild(@Context ContainerRequestContext request) {
            assertRequestHeader(request, "MyResource", "getChild");
            return "get-child";
        }


        @POST
        public String post(@Context ContainerRequestContext request) {
            assertRequestHeader(request, "MyResource", "post");
            return "post";
        }

        @Path("locator")
        public Class<SubResource> getSubResource() {
            return SubResource.class;
        }
    }

    @Path("info")
    public static class ResourceTestingInfo {
        @Context
        ResourceInfo resourceInfo;

        @GET
        public String getInfo(@Context ContainerRequestContext request) {
            assertRequestHeader(request, "ResourceTestingInfo", "getInfo");
            return "get-info";
        }

        @GET
        @Path("child")
        public String getChildInfo(@Context ContainerRequestContext request) {
            assertRequestHeader(request, "ResourceTestingInfo", "getChildInfo");
            return "get-info-child";

        }
    }

    private static void assertRequestHeader(ContainerRequestContext request, String className, String methodName) {
        assertEquals("called", request.getHeaderString("MyRequestFilter-called"));
        assertEquals(className, request.getHeaderString("MyRequestFilter-class"));
        assertEquals(methodName, request.getHeaderString("MyRequestFilter-method"));
    }

    public static class SubResource {
        @GET
        public String getFromSubResource(@Context ContainerRequestContext request) {
            assertRequestHeader(request, "SubResource", "getFromSubResource");
            return "get-sub-resource";
        }
    }

    @Path("resource-another")
    public static class MyAnotherResource {

        @GET
        public String getAnother(@Context ContainerRequestContext request) {
            assertRequestHeader(request, "MyAnotherResource", "getAnother");
            return "get-another";
        }
    }

    @Test
    public void testGet() throws ExecutionException, InterruptedException {
        ApplicationHandler handler = getApplication();
        final ContainerResponse response = handler.apply(RequestContextBuilder.from("/resource", "GET").build()).get();
        assertEquals(200, response.getStatus());
        assertEquals("get", response.getEntity());
        assertResponseHeaders(response, "MyResource", "get");
    }


    @Test
    public void testGetMultiple() throws ExecutionException, InterruptedException {
        ApplicationHandler handler = getApplication();
        ContainerResponse response = handler.apply(RequestContextBuilder.from("/resource", "GET").build()).get();
        assertEquals(200, response.getStatus());
        assertEquals("get", response.getEntity());
        assertResponseHeaders(response, "MyResource", "get");
        response = handler.apply(RequestContextBuilder.from("/resource/get-child", "GET").build()).get();
        assertEquals(200, response.getStatus());
        assertEquals("get-child", response.getEntity());
        assertResponseHeaders(response, "MyResource", "getChild");
    }

    private ApplicationHandler getApplication() {
        return new ApplicationHandler(new ResourceConfig(MyResource.class, MyAnotherResource.class, MyRequestFilter.class,
                MyResponseFilter.class, ResourceTestingInfo.class, MyPrematchingFilter.class));
    }

    @Test
    public void testGetChild() throws ExecutionException, InterruptedException {
        ApplicationHandler handler = getApplication();
        final ContainerResponse response = handler.apply(RequestContextBuilder.from("/resource/get-child", "GET").build()).get();
        assertEquals(200, response.getStatus());
        assertEquals("get-child", response.getEntity());

        final String className = "MyResource";
        final String methodName = "getChild";
        assertResponseHeaders(response, className, methodName);

    }

    private void assertResponseHeaders(ContainerResponse response, String className, String methodName) {
        assertEquals("called", response.getHeaders().get("MyResponseFilter-called").get(0));
        assertEquals(className, response.getHeaders().get("MyResponseFilter-class").get(0));
        assertEquals(methodName, response.getHeaders().get("MyResponseFilter-method").get(0));
    }

    @Test
    public void testPost() throws ExecutionException, InterruptedException {
        ApplicationHandler handler = getApplication();
        final ContainerResponse response = handler.apply(RequestContextBuilder.from("/resource", "POST").entity("entity")
                .build()).get();
        assertEquals(200, response.getStatus());
        assertEquals("post", response.getEntity());
        assertResponseHeaders(response, "MyResource", "post");
    }

    @Test
    public void testGetAnotherResource() throws ExecutionException, InterruptedException {
        ApplicationHandler handler = getApplication();
        final ContainerResponse response = handler.apply(RequestContextBuilder.from("/resource-another", "GET").build()).get();
        assertEquals(200, response.getStatus());
        assertEquals("get-another", response.getEntity());
        assertResponseHeaders(response, "MyAnotherResource", "getAnother");
    }

    @Test
    public void testGetSubResource() throws ExecutionException, InterruptedException {
        ApplicationHandler handler = getApplication();
        final ContainerResponse response = handler.apply(RequestContextBuilder.from("/resource/locator", "GET").build()).get();
        assertEquals(200, response.getStatus());
        assertEquals("get-sub-resource", response.getEntity());
        assertResponseHeaders(response, "SubResource", "getFromSubResource");
    }


    @Test
    public void testInfoGet() throws ExecutionException, InterruptedException {
        ApplicationHandler handler = getApplication();
        final ContainerResponse response = handler.apply(RequestContextBuilder.from("/info", "GET").build()).get();
        assertEquals(200, response.getStatus());
        assertEquals("get-info", response.getEntity());
        assertResponseHeaders(response, "ResourceTestingInfo", "getInfo");
    }

    @Test
    public void testInfoGetChild() throws ExecutionException, InterruptedException {
        ApplicationHandler handler = getApplication();
        final ContainerResponse response = handler.apply(RequestContextBuilder.from("/info/child", "GET").build()).get();
        assertEquals(200, response.getStatus());
        assertEquals("get-info-child", response.getEntity());
        assertResponseHeaders(response, "ResourceTestingInfo", "getChildInfo");
    }

    @Test
    public void test404() throws ExecutionException, InterruptedException {
        ApplicationHandler handler = getApplication();
        final ContainerResponse response = handler.apply(RequestContextBuilder.from("/NOT_FOUND", "GET").build()).get();
        assertEquals(404, response.getStatus());
        assertResponseHeaders(response, "<null>", "<null>");
    }

}
