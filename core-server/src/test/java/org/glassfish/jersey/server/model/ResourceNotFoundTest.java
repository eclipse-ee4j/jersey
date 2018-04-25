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

package org.glassfish.jersey.server.model;

import java.util.concurrent.ExecutionException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.process.Inflector;
import org.glassfish.jersey.server.ApplicationHandler;
import org.glassfish.jersey.server.ContainerResponse;
import org.glassfish.jersey.server.RequestContextBuilder;
import org.glassfish.jersey.server.ResourceConfig;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * test for JERSEY-938
 *
 * @author Jakub Podlesak
 */
public class ResourceNotFoundTest {

    ApplicationHandler application;

    private ApplicationHandler createApplication(Class<?>... classes) {
        return new ApplicationHandler(new ResourceConfig(classes));
    }


    public static class MyInflector implements Inflector<ContainerRequestContext, Response> {
        @Override
        public Response apply(ContainerRequestContext data) {
            return Response.ok("dynamic", MediaType.TEXT_PLAIN).build();
        }
    }

    @Path("/foo")
    public static class FooResource {
        @Produces("text/plain")
        @GET
        public String getFoo() {
            return "foo";
        }

        @Path("bar")
        @Produces("text/plain")
        @GET
        public String getBar() {
            return "bar";
        }

        @Path("content-type")
        @GET
        public Response getSpecialContentType() {
            return Response.status(Response.Status.NOT_FOUND).type("application/something").build();
        }

    }

    @Test
    public void testExistingDeclarativeResources() throws Exception {
        ApplicationHandler app = createApplication(FooResource.class);

        ContainerResponse response;

        response = app.apply(RequestContextBuilder.from("/foo", "GET").accept("text/plain").build()).get();
        assertEquals(200, response.getStatus());
        assertEquals("foo", response.getEntity());

        response = app.apply(RequestContextBuilder.from("/foo/bar", "GET").accept("text/plain").build()).get();
        assertEquals(200, response.getStatus());
        assertEquals("bar", response.getEntity());
    }

    @Test
    public void testMissingDeclarativeResources() throws Exception {
        ApplicationHandler app = createApplication(FooResource.class);

        ContainerResponse response;

        response = app.apply(RequestContextBuilder.from("/foe", "GET").accept("text/plain").build()).get();
        assertEquals(404, response.getStatus());

        response = app.apply(RequestContextBuilder.from("/fooe", "GET").accept("text/plain").build()).get();
        assertEquals(404, response.getStatus());

        response = app.apply(RequestContextBuilder.from("/foo/baz", "GET").accept("text/plain").build()).get();
        assertEquals(404, response.getStatus());

        response = app.apply(RequestContextBuilder.from("/foo/bar/baz", "GET").accept("text/plain").build()).get();
        assertEquals(404, response.getStatus());
    }

    private ApplicationHandler createMixedApp() {
        ResourceConfig rc = new ResourceConfig(FooResource.class);

        Resource.Builder rb;

        rb = Resource.builder("/dynamic");
        rb.addMethod("GET").handledBy(new MyInflector());
        rc.registerResources(rb.build());

        rb = Resource.builder("/foo/dynamic");
        rb.addMethod("GET").handledBy(new MyInflector());
        rc.registerResources(rb.build());

        return new ApplicationHandler(rc);
    }

    @Test
    public void testExistingMixedResources() throws Exception {

        ApplicationHandler app = createMixedApp();

        ContainerResponse response;

        response = app.apply(RequestContextBuilder.from("/foo", "GET").accept("text/plain").build()).get();
        assertEquals(200, response.getStatus());
        assertEquals("foo", response.getEntity());

        response = app.apply(RequestContextBuilder.from("/dynamic", "GET").accept("text/plain").build()).get();
        assertEquals(200, response.getStatus());
        assertEquals("dynamic", response.getEntity());

        response = app.apply(RequestContextBuilder.from("/foo/bar", "GET").accept("text/plain").build()).get();
        assertEquals(200, response.getStatus());
        assertEquals("bar", response.getEntity());

        response = app.apply(RequestContextBuilder.from("/foo/dynamic", "GET").accept("text/plain").build()).get();
        assertEquals(200, response.getStatus());
        assertEquals("dynamic", response.getEntity());
    }


    @Test
    public void testMissingMixedResources() throws Exception {

        ApplicationHandler app = createMixedApp();

        ContainerResponse response;

        response = app.apply(RequestContextBuilder.from("/foe", "GET").accept("text/plain").build()).get();
        assertEquals(404, response.getStatus());

        response = app.apply(RequestContextBuilder.from("/fooe", "GET").accept("text/plain").build()).get();
        assertEquals(404, response.getStatus());

        response = app.apply(RequestContextBuilder.from("/dynamical", "GET").accept("text/plain").build()).get();
        assertEquals(404, response.getStatus());

        response = app.apply(RequestContextBuilder.from("/foo/baz", "GET").accept("text/plain").build()).get();
        assertEquals(404, response.getStatus());

        response = app.apply(RequestContextBuilder.from("/foo/bar/baz", "GET").accept("text/plain").build()).get();
        assertEquals(404, response.getStatus());

        response = app.apply(RequestContextBuilder.from("/foo/dynamic/baz", "GET").accept("text/plain").build()).get();
        assertEquals(404, response.getStatus());
    }

    @Test
    public void testCustomContentTypeAndNoEntity() throws ExecutionException, InterruptedException {
        ApplicationHandler app = createApplication(FooResource.class);
        final ContainerResponse response = app.apply(RequestContextBuilder.from("/foo/content-type", "GET")
                .build()).get();
        assertEquals(404, response.getStatus());
        assertEquals("application/something", response.getMediaType().toString());
    }
}
