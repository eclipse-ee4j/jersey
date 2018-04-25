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

import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;

import org.glassfish.jersey.server.ApplicationHandler;
import org.glassfish.jersey.server.ContainerResponse;
import org.glassfish.jersey.server.RequestContextBuilder;
import org.glassfish.jersey.server.ResourceConfig;

import org.junit.Test;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

/**
 * @author Paul Sandoz
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
public class HeadTest {

    private ApplicationHandler app;

    private void initiateWebApplication(Class<?>... classes) {
        app = new ApplicationHandler(new ResourceConfig(classes));
    }

    @Path("/")
    public static class ResourceGetNoHead {

        @GET
        public String get() {
            return "GET";
        }
    }

    @Test
    public void testGetNoHead() throws Exception {
        initiateWebApplication(ResourceGetNoHead.class);

        ContainerResponse response = app.apply(RequestContextBuilder.from("/", "HEAD").build()).get();

        assertEquals(200, response.getStatus());
        String length = response.getHeaderString(HttpHeaders.CONTENT_LENGTH);
        assertNotNull(length);
        assertEquals(3, Integer.parseInt(length));
        assertEquals(MediaType.TEXT_PLAIN_TYPE, response.getMediaType());
        assertFalse(response.hasEntity());
    }

    @Path("/")
    public static class ResourceGetWithHead {

        @HEAD
        public Response head() {
            return Response.ok().header("X-TEST", "HEAD").build();
        }

        @GET
        public Response get() {
            return Response.ok("GET").header("X-TEST", "GET").build();
        }
    }

    @Test
    public void testGetWithHead() throws Exception {
        initiateWebApplication(ResourceGetWithHead.class);

        ContainerResponse response = app.apply(RequestContextBuilder.from("/", "HEAD").build()).get();
        assertEquals(200, response.getStatus());
        assertFalse(response.hasEntity());
        assertEquals("HEAD", response.getHeaders().getFirst("X-TEST"));
    }

    @Path("/")
    public static class ResourceGetWithProduceNoHead {

        @GET
        @Produces("application/foo")
        public String getFoo() {
            return "FOO";
        }

        @GET
        @Produces("application/bar")
        public String getBar() {
            return "BAR";
        }
    }

    @Test
    public void testGetWithProduceNoHead() throws Exception {
        initiateWebApplication(ResourceGetWithProduceNoHead.class);

        MediaType foo = MediaType.valueOf("application/foo");
        ContainerResponse response = app.apply(RequestContextBuilder.from("/", "HEAD").accept(foo).build()).get();
        assertEquals(200, response.getStatus());
        assertFalse(response.hasEntity());
        assertEquals(foo, response.getMediaType());

        MediaType bar = MediaType.valueOf("application/bar");
        response = app.apply(RequestContextBuilder.from("/", "HEAD").accept(bar).build()).get();
        assertEquals(200, response.getStatus());
        assertFalse(response.hasEntity());
        assertEquals(bar, response.getMediaType());
    }

    @Path("/")
    public static class ResourceGetWithProduceWithHead {

        @HEAD
        @Produces("application/foo")
        public Response headFoo() {
            return Response.ok().header("X-TEST", "FOO-HEAD").build();
        }

        @GET
        @Produces("application/foo")
        public Response getFoo() {
            return Response.ok("GET", "application/foo").header("X-TEST", "FOO-GET").build();
        }

        @HEAD
        @Produces("application/bar")
        public Response headBar() {
            return Response.ok().header("X-TEST", "BAR-HEAD").build();
        }

        @GET
        @Produces("application/bar")
        public Response getBar() {
            return Response.ok("GET").header("X-TEST", "BAR-GET").build();
        }
    }

    @Test
    public void testGetWithProduceWithHead() throws Exception {
        initiateWebApplication(ResourceGetWithProduceWithHead.class);

        MediaType foo = MediaType.valueOf("application/foo");
        ContainerResponse response = app.apply(RequestContextBuilder.from("/", "HEAD").accept(foo).build()).get();
        assertEquals(200, response.getStatus());
        assertFalse(response.hasEntity());
        assertEquals(foo, response.getMediaType());
        assertEquals("FOO-HEAD", response.getHeaders().getFirst("X-TEST").toString());

        MediaType bar = MediaType.valueOf("application/bar");
        response = app.apply(RequestContextBuilder.from("/", "HEAD").accept(bar).build()).get();
        assertEquals(200, response.getStatus());
        assertFalse(response.hasEntity());
        assertEquals(bar, response.getMediaType());
        assertEquals("BAR-HEAD", response.getHeaders().getFirst("X-TEST").toString());
    }

    @Path("/")
    public static class ResourceGetByteNoHead {

        @GET
        public byte[] get() {
            return "GET".getBytes();
        }
    }

    @Test
    public void testGetByteNoHead() throws Exception {
        initiateWebApplication(ResourceGetByteNoHead.class);

        ContainerResponse response = app.apply(RequestContextBuilder.from("/", "HEAD").build()).get();
        assertEquals(200, response.getStatus());
        String length = response.getHeaderString(HttpHeaders.CONTENT_LENGTH);
        assertNotNull(length);
        assertEquals(3, Integer.parseInt(length));
        assertEquals(MediaType.APPLICATION_OCTET_STREAM_TYPE, response.getMediaType());
        assertFalse(response.hasEntity());
    }

    @Path("/")
    public static class ResourceGetWithNoProduces {

        @GET
        public Response getPlain() {
            return Response.ok("text").header("x-value", "text")
                    .build();
        }

        @GET
        @Produces("text/html")
        public Response getHtml() {
            return Response.ok("html").header("x-value", "html")
                    .build();
        }
    }

    @Test
    public void testResourceXXX() throws Exception {
        initiateWebApplication(ResourceGetWithNoProduces.class);

        ContainerResponse response = app.apply(RequestContextBuilder.from("/", "HEAD").accept("text/plain").build()).get();
        assertEquals(200, response.getStatus());
        assertEquals("text", response.getHeaderString("x-value"));

        response = app.apply(RequestContextBuilder.from("/", "HEAD").accept("text/html").build()).get();
        assertEquals(200, response.getStatus());
        assertEquals("html", response.getHeaderString("x-value"));
    }

    @Path("/")
    public static class InputStreamResource {

        private static boolean INPUT_STREAM_CLOSED = false;

        @GET
        public InputStream testInputStream() {
            return new InputStream() {

                @Override
                public int read() throws IOException {
                    return -1;
                }

                @Override
                public void close() throws IOException {
                    INPUT_STREAM_CLOSED = true;
                }
            };
        }
    }

    @Provider
    public static class InputStreamWriterInterceptor implements WriterInterceptor {

        private static boolean INPUT_STREAM_CLOSED = false;

        @Override
        public void aroundWriteTo(final WriterInterceptorContext context) throws IOException, WebApplicationException {
            final InputStream inputStream = (InputStream) context.getEntity();

            context.setEntity(new InputStream() {

                @Override
                public int read() throws IOException {
                    return inputStream.read();
                }

                @Override
                public void close() throws IOException {
                    INPUT_STREAM_CLOSED = true;

                    inputStream.close();
                }
            });

            context.proceed();
        }
    }

    /**
     * Test whether an input stream returned from resource method is properly closed when the method is invoked to handle HTTP
     * HEAD request.
     * <p/>
     * JERSEY-1922 reproducer.
     */
    @Test
    public void testHeadWithInputStream() throws Exception {
        initiateWebApplication(InputStreamResource.class, InputStreamWriterInterceptor.class);

        final ContainerResponse response = app.apply(RequestContextBuilder.from("/", "HEAD").build()).get();

        assertThat(response.getStatus(), equalTo(200));
        assertThat(InputStreamResource.INPUT_STREAM_CLOSED, is(true));
        assertThat(InputStreamWriterInterceptor.INPUT_STREAM_CLOSED, is(true));
    }
}
