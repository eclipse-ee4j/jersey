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

package org.glassfish.jersey.tests.e2e.client;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.client.ClientResponseContext;
import jakarta.ws.rs.client.ClientResponseFilter;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.MessageBodyReader;

import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.client.JerseyWebTarget;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test pre initialization of the client.
 *
 * @author Miroslav Fuksa
 */
public class ClientPreInitTest extends JerseyTest {
    @Override
    protected Application configure() {
        return new ResourceConfig(Resource.class);
    }

    @Path("resource")
    public static class Resource {
        @GET
        public String get(@HeaderParam("filter-request") String header) {
            return "resource:" + (header == null ? "<null>" : header);
        }

        @GET
        @Path("child")
        public String getChild(@HeaderParam("filter-request") String header) {
            return "child:" + (header == null ? "<null>" : header);
        }
    }

    public static class MyRequestFilter implements ClientRequestFilter {

        @Override
        public void filter(ClientRequestContext requestContext) throws IOException {
            requestContext.getHeaders().add("filter-request", "called");
        }
    }

    public static class MyResponseFilter implements ClientResponseFilter {

        @Override
        public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) throws IOException {
            responseContext.getHeaders().add("filter-response", "called");
        }
    }

    public static class TestReader implements MessageBodyReader<Integer> {
        public static boolean initialized;

        public TestReader() {
            TestReader.initialized = true;
        }

        @Override
        public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return false;
        }

        @Override
        public Integer readFrom(Class<Integer> type, Type genericType, Annotation[] annotations, MediaType mediaType,
                                MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
                throws IOException, WebApplicationException {
            return null;
        }
    }

    @Before
    public void before() {
        TestReader.initialized = false;
    }

    @Test
    public void testNonInitialized() {
        Client client = ClientBuilder.newClient();
        client.register(MyResponseFilter.class);
        client.register(TestReader.class);
        final WebTarget target = client.target(super.getBaseUri()).path("resource");
        assertFalse(TestReader.initialized);
        final Response resourceResponse = target.request().get();
        checkResponse(resourceResponse, "resource:<null>");
        assertTrue(TestReader.initialized);
    }

    @Test
    public void testSimplePreinitialize() {
        Client client = ClientBuilder.newClient();
        final WebTarget target = client.target(super.getBaseUri()).path("resource");
        target.register(MyResponseFilter.class);
        final WebTarget childTarget = target.path("child");
        ((JerseyWebTarget) childTarget).preInitialize();
        final Response response = childTarget.request().get();
        checkResponse(response, "child:<null>");

        final Response resourceResponse = target.request().get();
        checkResponse(resourceResponse, "resource:<null>");
    }

    @Test
    public void testReusingPreinitializedConfig() {
        Client client = ClientBuilder.newClient();
        client.register(TestReader.class);
        final WebTarget target = client.target(super.getBaseUri()).path("resource");
        target.register(MyResponseFilter.class);
        ((JerseyWebTarget) target).preInitialize();
        assertTrue(TestReader.initialized);
        final WebTarget childTarget = target.path("child");
        final Response response = childTarget.request().get();

        checkResponse(response, "child:<null>");

        final Response resourceResponse = target.request().get();
        checkResponse(resourceResponse, "resource:<null>");
    }

    @Test
    public void testReusingPreinitializedConfig2() {
        Client client = ClientBuilder.newClient();
        client.register(TestReader.class);
        client.register(MyResponseFilter.class);

        assertFalse(TestReader.initialized);
        ((JerseyClient) client).preInitialize();
        assertTrue(TestReader.initialized);

        final WebTarget target = client.target(super.getBaseUri()).path("resource");
        final WebTarget childTarget = target.path("child");
        final Response response = childTarget.request().get();
        checkResponse(response, "child:<null>");

        final Response resourceResponse = target.request().get();
        checkResponse(resourceResponse, "resource:<null>");
    }

    private void checkResponse(Response response, String entity) {
        assertEquals("called", response.getHeaders().get("filter-response").get(0));
        assertEquals(200, response.getStatus());
        assertEquals(entity, response.readEntity(String.class));
    }

    @Test
    public void testRegisterOnPreinitialized1() {
        Client client = ClientBuilder.newClient();
        final WebTarget target = client.target(super.getBaseUri()).path("resource");
        target.register(MyRequestFilter.class);
        ((JerseyWebTarget) target).preInitialize();
        target.register(MyResponseFilter.class);
        final Response response = target.request().get();
        checkResponse(response, "resource:called");
    }

    @Test
    public void testRegisterOnPreinitialized2() {
        Client client = ClientBuilder.newClient();
        final WebTarget target = client.target(super.getBaseUri()).path("resource");
        target.register(MyResponseFilter.class);
        ((JerseyWebTarget) target).preInitialize();
        final WebTarget child = target.path("child");
        child.register(MyRequestFilter.class);

        final Response response = target.request().get();
        checkResponse(response, "resource:<null>");

        final Response childResponse = child.request().get();
        checkResponse(childResponse, "child:called");
    }
}
