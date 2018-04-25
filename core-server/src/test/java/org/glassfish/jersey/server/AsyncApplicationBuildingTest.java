/*
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.server;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.MessageBodyReader;

import javax.inject.Inject;
import javax.inject.Provider;

import org.glassfish.jersey.process.Inflector;
import org.glassfish.jersey.server.model.Resource;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit test for creating an application with asynchronously handled request processing
 * via {@link Resource}'s programmatic API.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public class AsyncApplicationBuildingTest {

    private static final String BASE_URI = "http://localhost:8080/base/";

    private static class AsyncInflector implements Inflector<ContainerRequestContext, Response> {

        @Inject
        private Provider<AsyncContext> asyncContextProvider;
        private final String responseContent;

        public AsyncInflector() {
            this.responseContent = "DEFAULT";
        }

        public AsyncInflector(String responseContent) {
            this.responseContent = responseContent;
        }

        @Override
        public Response apply(final ContainerRequestContext req) {
            // Suspend current request
            final AsyncContext asyncContext = asyncContextProvider.get();
            asyncContext.suspend();

            Executors.newSingleThreadExecutor().submit(new Runnable() {

                @Override
                public void run() {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace(System.err);
                    }

                    // Returning will enter the suspended request
                    asyncContext.resume(Response.ok().entity(responseContent).build());
                }
            });

            return null;
        }
    }

    public ApplicationHandler setupApplication1() {
        final ResourceConfig rc = new ResourceConfig();

        Resource.Builder rb;

        rb = Resource.builder("a/b/c");
        rb.addMethod("GET").handledBy(new AsyncInflector("A-B-C"));
        rc.registerResources(rb.build());

        rb = Resource.builder("a/b/d");
        rb.addMethod("GET").handledBy(new AsyncInflector("A-B-D"));
        rc.registerResources(rb.build());

        return new ApplicationHandler(rc);
    }

    @Test
    public void testAsyncApp1() throws InterruptedException, ExecutionException {
        ContainerRequest req = RequestContextBuilder.from(
                BASE_URI, BASE_URI + "a/b/c", "GET").build();

        Future<ContainerResponse> res = setupApplication1().apply(req);

        assertEquals("A-B-C", res.get().getEntity());
    }

    @Test
    public void testAsyncApp2() throws InterruptedException, ExecutionException {
        ContainerRequest req = RequestContextBuilder.from(
                BASE_URI, BASE_URI + "a/b/d", "GET").build();

        Future<ContainerResponse> res = setupApplication1().apply(req);

        assertEquals("A-B-D", res.get().getEntity());
    }

    @Path("/")
    public static class ResourceA {

        @GET
        public String get() {
            return "get!";
        }
    }

    @Test
    public void testappBuilderClasses() throws InterruptedException, ExecutionException {
        final ResourceConfig resourceConfig = new ResourceConfig(ResourceA.class);
        final ApplicationHandler application = new ApplicationHandler(resourceConfig);

        ContainerRequest req = RequestContextBuilder.from(BASE_URI, BASE_URI, "GET").build();

        assertEquals("get!", application.apply(req).get().getEntity());
    }

    @Test
    public void testEmptyAppCreationPasses() throws InterruptedException, ExecutionException {
        final ResourceConfig resourceConfig = new ResourceConfig();
        final ApplicationHandler application = new ApplicationHandler(resourceConfig);

        ContainerRequest req = RequestContextBuilder.from(BASE_URI, BASE_URI, "GET").build();

        assertEquals(404, application.apply(req).get().getStatus());
    }

    @Test
    public void testAppBuilderJaxRsApplication() throws InterruptedException, ExecutionException {
        Application jaxRsApplication = new Application() {
            @Override
            public Set<Class<?>> getClasses() {
                HashSet<Class<?>> set = new HashSet<Class<?>>();
                set.add(ResourceA.class);
                return set;
            }

            @Override
            public Set<Object> getSingletons() {
                return super.getSingletons();
            }
        };

        final ApplicationHandler application = new ApplicationHandler(jaxRsApplication);

        ContainerRequest req = RequestContextBuilder.from(BASE_URI, BASE_URI, "GET").build();

        assertEquals("get!", application.apply(req).get().getEntity());
    }

    @Path("/")
    public static class ResourceB {

        @Context
        javax.ws.rs.core.Application application;

        @GET
        public String get() {
            assertTrue(application != null);
            assertTrue(application.getClasses().contains(ResourceB.class));
            assertTrue(application.getSingletons().size() > 0);
            assertTrue(application.getSingletons().iterator().next().getClass().equals(ResourceAReader.class));
            return "get!";
        }
    }

    public static class ResourceAReader implements MessageBodyReader<ResourceA> {

        @Override
        public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return false;
        }

        @Override
        public ResourceA readFrom(Class<ResourceA> type, Type genericType, Annotation[] annotations,
                MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
                throws IOException, WebApplicationException {
            return null;
        }
    }

    @Test
    public void testJaxrsApplicationInjection() throws InterruptedException, ExecutionException {
        final ResourceConfig resourceConfig = new ResourceConfig(ResourceB.class)
                .registerInstances(new ResourceAReader());

        final ApplicationHandler application = new ApplicationHandler(resourceConfig);

        ContainerRequest req = RequestContextBuilder.from(BASE_URI, BASE_URI, "GET").build();

        assertEquals("get!", application.apply(req).get().getEntity());
    }

    @Path("/")
    @Consumes("text/plain")
    public static class ErrornousResource {

        @POST
        @Produces("text/plain")
        public String postOne(String s) {
            return "One";
        }

        @POST
        @Produces("text/plain")
        public String postTwo(String s) {
            return "Two";
        }
    }

    @Test
    public void testDeploymentFailsForAmbiguousResource() {
        final ResourceConfig resourceConfig = new ResourceConfig(ErrornousResource.class);
        try {
            ApplicationHandler server = new ApplicationHandler(resourceConfig);
            assertTrue("Jersey server initialization should have failed: " + server, false);
        } catch (Exception e) {
        }
    }
}
