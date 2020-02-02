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

package org.glassfish.jersey.tests.e2e.server.monitoring;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.container.AsyncResponse;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.container.Suspended;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;

import org.glassfish.jersey.server.ManagedAsync;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.model.ResourceMethod;
import org.glassfish.jersey.server.monitoring.ApplicationEvent;
import org.glassfish.jersey.server.monitoring.ApplicationEventListener;
import org.glassfish.jersey.server.monitoring.RequestEvent;
import org.glassfish.jersey.server.monitoring.RequestEventListener;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Assert;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * @author Miroslav Fuksa
 */
public class EventListenerTest extends JerseyTest {

    private static final String APPLICATION_NAME = "MyApplication";
    private static AppEventListener applicationEventListener;

    @Override
    protected Application configure() {
        applicationEventListener = new AppEventListener();
        final ResourceConfig resourceConfig = new ResourceConfig(MyResource.class);
        resourceConfig.register(applicationEventListener);
        resourceConfig.register(RequestFilter.class);
        resourceConfig.register(PreMatchingRequestFilter.class);
        resourceConfig.register(ResponseFilter.class);
        resourceConfig.register(MyExceptionMapper.class);
        resourceConfig.setApplicationName(APPLICATION_NAME);
        return resourceConfig;
    }

    public static class AppEventListener implements ApplicationEventListener {
        private ApplicationEvent appEventInitStart;
        private ApplicationEvent appEventInitFinished;
        private RequestEvent newRequestEvent;
        private volatile int resourceMethodEventCount = 0;
        private volatile CountDownLatch finishedCalled = new CountDownLatch(1);

        @Override
        public void onEvent(ApplicationEvent event) {
            switch (event.getType()) {
                case INITIALIZATION_START:
                    this.appEventInitStart = event;
                    break;
                case INITIALIZATION_APP_FINISHED:
                    this.appEventInitFinished = event;
                    break;
            }
        }

        @Override
        public RequestEventListener onRequest(RequestEvent newRequestEvent) {
            this.newRequestEvent = newRequestEvent;
            if ("POST".equals(newRequestEvent.getContainerRequest().getMethod())) {
                return null;
            }

            return new ReqEventListener(this);
        }
    }

    public static class ReqEventListener implements RequestEventListener {
        private final AppEventListener appEventListener;

        public final MultivaluedMap<String, String> eventData = new MultivaluedHashMap<String, String>();

        public ReqEventListener(AppEventListener appEventListener) {
            this.appEventListener = appEventListener;
        }

        private int index = 1;

        @Override
        public void onEvent(RequestEvent event) {
            switch (event.getType()) {
                case REQUEST_MATCHED:
                    eventData.add("R.REQ_FILTERS_START.order", String.valueOf(index++));
                    break;
                case REQUEST_FILTERED:
                    eventData.add("R.REQ_FILTERS_FINISHED.order", String.valueOf(index++));
                    break;
                case LOCATOR_MATCHED:
                    eventData.add("R.MATCHED_LOCATOR.order", String.valueOf(index++));
                    final List<ResourceMethod> locators = event.getUriInfo().getMatchedResourceLocators();
                    String msg = String.valueOf(locators.size())
                            + ":" + locators.get(0).getInvocable().getHandlingMethod().getName();
                    eventData.add("R.MATCHED_LOCATOR", msg);
                    break;
                case SUBRESOURCE_LOCATED:
                    eventData.add("R.MATCHED_SUB_RESOURCE.order", String.valueOf(index++));
                    break;
                case RESOURCE_METHOD_START:
                    eventData.add("R.RESOURCE_METHOD_START.order", String.valueOf(index++));
                    this.appEventListener.resourceMethodEventCount++;
                    final ResourceMethod resourceMethod = event.getUriInfo().getMatchedResourceMethod();
                    eventData.add("R.RESOURCE_METHOD_START.method", resourceMethod
                            .getInvocable().getHandlingMethod().getName());
                    break;
                case RESOURCE_METHOD_FINISHED:
                    eventData.add("R.RESOURCE_METHOD_FINISHED.order", String.valueOf(index++));
                    eventData.add("R.RESOURCE_METHOD_FINISHED", "ok");
                    break;
                case RESP_FILTERS_START:
                    eventData.add("R.RESP_FILTERS_START.order", String.valueOf(index++));
                    break;
                case EXCEPTION_MAPPER_FOUND:
                    eventData.add("R.EXCEPTION_MAPPER_FOUND.order", String.valueOf(index++));
                    eventData.add("R.EXCEPTION_MAPPER_FOUND.exception", event.getException().getMessage());
                    break;
                case RESP_FILTERS_FINISHED:
                    eventData.add("R.RESP_FILTERS_FINISHED.order", String.valueOf(index++));
                    for (Map.Entry<String, List<String>> entry : eventData.entrySet()) {
                        event.getContainerResponse().getHeaders().addAll(entry.getKey(), entry.getValue());
                    }
                    break;
                case FINISHED:
                    Assert.assertNotNull(event.getContainerResponse());
                    this.appEventListener.finishedCalled.countDown();
                    break;
            }
        }
    }

    public static class MyMappableException extends RuntimeException {
        public MyMappableException(String message) {
            super(message);
        }
    }

    public static class MyExceptionMapper implements ExceptionMapper<MyMappableException> {

        @Override
        public Response toResponse(MyMappableException exception) {
            return Response.ok("mapped").build();
        }
    }

    public static class RequestFilter implements ContainerRequestFilter {

        @Override
        public void filter(ContainerRequestContext requestContext) throws IOException {
        }
    }

    @PreMatching
    public static class PreMatchingRequestFilter implements ContainerRequestFilter {

        @Override
        public void filter(ContainerRequestContext requestContext) throws IOException {
        }
    }


    public static class ResponseFilter implements ContainerResponseFilter {

        @Override
        public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        }
    }

    @Path("resource")
    public static class MyResource {

        @GET
        public String getMethod() {
            return "get";
        }

        @POST
        public void post(String entity) {
        }

        @Path("locator")
        public SubResource locator() {
            return new SubResource();
        }

        @GET
        @Path("async")
        @ManagedAsync
        public void getAsync(@Suspended AsyncResponse asyncResponse) {
            asyncResponse.resume(Response.ok("async").build());
        }

        /**
         * This works in the async way but it is served by only one thread.
         * @param asyncResponse
         */
        @GET
        @Path("asyncOneThread")
        public void getAsyncOneThread(@Suspended AsyncResponse asyncResponse) {
            asyncResponse.resume(Response.ok("async").build());
        }
    }


    public static class SubResource {
        @GET
        public String get() {
            return "sub";
        }

        @GET
        @Path("exception")
        public String getException() {
            throw new MyMappableException("test-error");
        }
    }

    @Test
    public void testApplicationEvents() {
        assertNotNull(applicationEventListener.appEventInitStart);
        assertNotNull(applicationEventListener.appEventInitFinished);
        assertEquals(APPLICATION_NAME, applicationEventListener.appEventInitStart.getResourceConfig().getApplicationName());
        assertNull(applicationEventListener.newRequestEvent);
        final Response response = target().path("resource").request().get();
        assertEquals(200, response.getStatus());
        assertNotNull(applicationEventListener.newRequestEvent);

    }

    @Test
    public void testSimpleRequestEvent() {
        assertEquals(0, applicationEventListener.resourceMethodEventCount);
        assertNotNull(applicationEventListener.appEventInitStart);
        assertNull(applicationEventListener.newRequestEvent);
        Response response = target().path("resource").request().post(Entity.entity("entity",
                MediaType.TEXT_PLAIN_TYPE));
        assertEquals(204, response.getStatus());
        assertNotNull(applicationEventListener.newRequestEvent);
        assertEquals(0, applicationEventListener.resourceMethodEventCount);
        response = target().path("resource").request().get();
        assertEquals(200, response.getStatus());
        assertEquals(1, applicationEventListener.resourceMethodEventCount);
    }

    @Test
    public void testMatchedLocator() {
        final Response response = target().path("resource/locator").request().get();
        assertEquals(200, response.getStatus());
        assertEquals("sub", response.readEntity(String.class));
        assertEquals("[1:locator]", response.getHeaderString("R.MATCHED_LOCATOR"));
    }

    @Test
    public void testMatchedMethod() {
        final Response response = target().path("resource").request().get();
        assertEquals(200, response.getStatus());
        assertEquals("get", response.readEntity(String.class));
        assertEquals("[getMethod]", response.getHeaderString("R.RESOURCE_METHOD_START.method"));
        assertEquals("[ok]", response.getHeaderString("R.RESOURCE_METHOD_FINISHED"));
    }

    @Test
    public void testException() {
        final Response response = target().path("resource/locator/exception").request().get();
        assertEquals(200, response.getStatus());
        assertEquals("mapped", response.readEntity(String.class));
        assertEquals("[org.glassfish.jersey.tests.e2e.server.monitoring.EventListenerTest$MyMappableException: test-error]",
                response.getHeaderString("R.EXCEPTION_MAPPER_FOUND.exception"));
    }


    @Test
    public void testSimpleProcessing() {
        final Response response = target().path("resource").request().get();
        assertEquals(200, response.getStatus());

        assertEquals("get", response.readEntity(String.class));


        int i = 1;
        System.out.println(response.getHeaders());
        assertEquals("[" + i++ + "]", response.getHeaderString("R.REQ_FILTERS_START.order"));
        assertEquals("[" + i++ + "]", response.getHeaderString("R.REQ_FILTERS_FINISHED.order"));
        assertEquals("[" + i++ + "]", response.getHeaderString("R.RESOURCE_METHOD_START.order"));
        assertEquals("[" + i++ + "]", response.getHeaderString("R.RESOURCE_METHOD_FINISHED.order"));
        assertEquals("[" + i++ + "]", response.getHeaderString("R.RESP_FILTERS_START.order"));
        assertEquals("[" + i + "]", response.getHeaderString("R.RESP_FILTERS_FINISHED.order"));
    }


    @Test
    public void testLocatorProcessing() {
        final Response response = target().path("resource/locator").request().get();
        assertEquals(200, response.getStatus());

        assertEquals("sub", response.readEntity(String.class));


        int i = 1;
        System.out.println(response.getHeaders());
        assertEquals("[" + i++ + "]", response.getHeaderString("R.MATCHED_LOCATOR.order"));
        assertEquals("[" + i++ + "]", response.getHeaderString("R.MATCHED_SUB_RESOURCE.order"));
        assertEquals("[" + i++ + "]", response.getHeaderString("R.REQ_FILTERS_START.order"));
        assertEquals("[" + i++ + "]", response.getHeaderString("R.REQ_FILTERS_FINISHED.order"));
        assertEquals("[" + i++ + "]", response.getHeaderString("R.RESOURCE_METHOD_START.order"));
        assertEquals("[" + i++ + "]", response.getHeaderString("R.RESOURCE_METHOD_FINISHED.order"));
        assertEquals("[" + i++ + "]", response.getHeaderString("R.RESP_FILTERS_START.order"));
        assertEquals("[" + i + "]", response.getHeaderString("R.RESP_FILTERS_FINISHED.order"));
    }

    @Test
    public void testExceptionProcessing() {
        final Response response = target().path("resource/locator/exception").request().get();
        assertEquals(200, response.getStatus());

        assertEquals("mapped", response.readEntity(String.class));


        int i = 1;
        System.out.println(response.getHeaders());
        assertEquals("[" + i++ + "]", response.getHeaderString("R.MATCHED_LOCATOR.order"));
        assertEquals("[" + i++ + "]", response.getHeaderString("R.MATCHED_SUB_RESOURCE.order"));
        assertEquals("[" + i++ + "]", response.getHeaderString("R.REQ_FILTERS_START.order"));
        assertEquals("[" + i++ + "]", response.getHeaderString("R.REQ_FILTERS_FINISHED.order"));
        assertEquals("[" + i++ + "]", response.getHeaderString("R.RESOURCE_METHOD_START.order"));
        assertEquals("[" + i++ + "]", response.getHeaderString("R.RESOURCE_METHOD_FINISHED.order"));
        assertEquals("[" + i++ + "]", response.getHeaderString("R.EXCEPTION_MAPPER_FOUND.order"));
        assertEquals("[" + i++ + "]", response.getHeaderString("R.RESP_FILTERS_START.order"));
        assertEquals("[" + i + "]", response.getHeaderString("R.RESP_FILTERS_FINISHED.order"));
    }

    @Test
    public void testAsyncProcessing() throws InterruptedException {
        final Response response = target().path("resource/async").request().get();
        assertEquals(200, response.getStatus());
        assertEquals("async", response.readEntity(String.class));

        int i = 1;
        System.out.println(response.getHeaders());
        assertEquals("[" + i++ + "]", response.getHeaderString("R.REQ_FILTERS_START.order"));
        assertEquals("[" + i++ + "]", response.getHeaderString("R.REQ_FILTERS_FINISHED.order"));
        assertEquals("[" + i++ + "]", response.getHeaderString("R.RESOURCE_METHOD_START.order"));
        assertEquals("[" + i++ + "]", response.getHeaderString("R.RESP_FILTERS_START.order"));
        assertEquals("[" + i + "]", response.getHeaderString("R.RESP_FILTERS_FINISHED.order"));
        final boolean success = applicationEventListener.finishedCalled.await(3 * getAsyncTimeoutMultiplier(),
                TimeUnit.SECONDS);
        Assert.assertTrue(success);
    }

    @Test
    public void testAsyncProcessingWithOneThread() throws InterruptedException {
        final Response response = target().path("resource/asyncOneThread").request().get();
        assertEquals(200, response.getStatus());
        assertEquals("async", response.readEntity(String.class));

        int i = 1;
        System.out.println(response.getHeaders());
        assertEquals("[" + i++ + "]", response.getHeaderString("R.REQ_FILTERS_START.order"));
        assertEquals("[" + i++ + "]", response.getHeaderString("R.REQ_FILTERS_FINISHED.order"));
        assertEquals("[" + i++ + "]", response.getHeaderString("R.RESOURCE_METHOD_START.order"));
        assertEquals("[" + i++ + "]", response.getHeaderString("R.RESP_FILTERS_START.order"));
        assertEquals("[" + i + "]", response.getHeaderString("R.RESP_FILTERS_FINISHED.order"));

        final boolean success = applicationEventListener.finishedCalled.await(3 * getAsyncTimeoutMultiplier(),
                TimeUnit.SECONDS);
        Assert.assertTrue(success);
    }
}
