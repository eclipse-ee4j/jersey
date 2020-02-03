/*
 * Copyright (c) 2012, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.e2e.common;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Logger;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientResponseContext;
import jakarta.ws.rs.client.ClientResponseFilter;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.DynamicFeature;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.FeatureContext;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.ext.ReaderInterceptor;
import jakarta.ws.rs.ext.ReaderInterceptorContext;

import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author Michal Gajdos
 */
public class ContentTypeTest extends JerseyTest {

    private static final Logger LOGGER = Logger.getLogger(ContentTypeTest.class.getName());

    @Path(value = "/ContentType")
    public static class ContentTypeResource {

        @POST
        @Produces("text/plain")
        @SuppressWarnings("UnusedParameters")
        public void postTest(final String str) {
            // Ignore to generate response 204 - NoContent.
        }

        @POST
        @Path("changeTest")
        @Produces("foo/bar")
        @CtFix(ContainerRequestFilter.class)
        public String changeContentTypeTest(String echo) {
            return echo;
        }

        @POST
        @Path("null-content")
        public String process(@HeaderParam(HttpHeaders.CONTENT_TYPE) String mediaType, @Context ContainerRequest request) {
            return mediaType + "-" + request.hasEntity();
        }

    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.METHOD})
    @interface CtFix {

        Class<? super ContentTypeFixProvider> value();
    }

    public static class ContentTypeFixProvider
            implements ReaderInterceptor, ContainerRequestFilter, ClientResponseFilter {

        @Override
        public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) throws IOException {
            System.out.println("filter1");
            if (responseContext.getMediaType().toString().equals("foo/bar")) {
                responseContext.getHeaders().putSingle(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN);
            }
        }

        @Override
        public void filter(ContainerRequestContext context) throws IOException {
            System.out.println("filter2");
            if (context.getMediaType().toString().equals("foo/bar")) {
                context.getHeaders().putSingle(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN);
            }
        }

        @Override
        public Object aroundReadFrom(ReaderInterceptorContext context) throws IOException, WebApplicationException {
            System.out.println("reader");
            if (context.getMediaType().toString().equals("foo/bar")) {
                context.getHeaders().putSingle(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN);
            }

            return context.proceed();
        }
    }

    public static class ContentTypeFixFeature implements DynamicFeature {

        @Override
        public void configure(ResourceInfo resourceInfo, FeatureContext context) {
            final CtFix annotation = resourceInfo.getResourceMethod().getAnnotation(CtFix.class);
            if (annotation != null) {
                context.register(ContentTypeFixProvider.class, annotation.value());
            }
        }
    }

    @Produces("foo/bar")
    public static class FooBarStringWriter implements MessageBodyWriter<String> {

        @Override
        public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return type == String.class && mediaType.toString().equals("foo/bar");
        }

        @Override
        public long getSize(String s, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return s.length();
        }

        @Override
        public void writeTo(String s,
                            Class<?> type,
                            Type genericType,
                            Annotation[] annotations,
                            MediaType mediaType,
                            MultivaluedMap<String, Object> httpHeaders,
                            OutputStream entityStream) throws IOException, WebApplicationException {

            entityStream.write(s.getBytes());
        }
    }

    @Override
    protected Application configure() {
        enable(TestProperties.DUMP_ENTITY);
        enable(TestProperties.LOG_TRAFFIC);

        return new ResourceConfig()
                .registerClasses(ContentTypeResource.class, ContentTypeFixFeature.class, FooBarStringWriter.class)
                .registerInstances(new LoggingFeature(LOGGER, LoggingFeature.Verbosity.PAYLOAD_ANY));
    }

    @Test
    public void testContentTypeHeaderForNoContentResponse() {
        final Response response = target().path("ContentType").request().post(Entity.entity("", MediaType.TEXT_PLAIN_TYPE));

        assertEquals(204, response.getStatus());
        assertNull(response.getHeaderString("Content-Type"));
    }

    @Test
    public void testInvalidContentTypeHeader() throws Exception {
        final URL url = new URL(getBaseUri().toString() + "ContentType");
        final HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("POST");
        connection.setRequestProperty("Accept", "text/plain");
        connection.setRequestProperty("Content-Type", "^^^");

        connection.setDoOutput(true);
        connection.connect();

        final OutputStream outputStream = connection.getOutputStream();
        outputStream.write("HelloWorld!".getBytes());
        outputStream.write('\n');
        outputStream.flush();

        assertEquals(400, connection.getResponseCode());
    }

    @Test
    public void testChangeContentTypeHeader() throws Exception {
        WebTarget target;
        Response response;

        // filter test
        target = target().path("ContentType").path("changeTest");
        target.register(ContentTypeFixProvider.class, ClientResponseFilter.class)
                .register(FooBarStringWriter.class);

        response = target
                .request().post(Entity.entity("test", "foo/bar"));

        assertEquals(200, response.getStatus());
        assertEquals(MediaType.TEXT_PLAIN_TYPE, response.getMediaType());
        assertEquals("test", response.readEntity(String.class));
        assertEquals(MediaType.TEXT_PLAIN_TYPE, response.getMediaType());

        // interceptor test
        target = target().path("ContentType").path("changeTest");
        target.register(ContentTypeFixProvider.class, ReaderInterceptor.class)
                .register(FooBarStringWriter.class);

        response = target
                .request().post(Entity.entity("test", "foo/bar"));

        assertEquals(200, response.getStatus());
        assertEquals(MediaType.valueOf("foo/bar"), response.getMediaType());
        assertEquals("test", response.readEntity(String.class));
        assertEquals(MediaType.TEXT_PLAIN_TYPE, response.getMediaType());
    }

    @Test
    public void testEmptyPostRequestWithContentType() {
        String response = target().path("ContentType/null-content").request("text/plain")
                .post(Entity.entity(null, "foo/bar"), String.class);

        assertEquals("foo/bar-false", response);
    }
}
