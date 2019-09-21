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

package org.glassfish.jersey.tests.e2e.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ReaderInterceptor;
import javax.ws.rs.ext.ReaderInterceptorContext;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;

import javax.annotation.Priority;

import org.glassfish.jersey.CommonProperties;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.inmemory.InMemoryTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.glassfish.jersey.test.spi.TestContainerFactory;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Test determining of the content length.
 *
 * @author Miroslav Fuksa
 */
public class DetermineContentLengthTest extends JerseyTest {

    public static final int BUFFER_SIZE = 20;

    public static final String STR0 = "";
    public static final String STR6 = "123456";
    public static final String STR12 = "123456789ABC";

    @Path("root")
    public static class Resource {

        @GET
        @Path("6")
        @Produces("text/plain")
        public String get6() {
            return STR6;
        }


        @GET
        @Path("0")
        @Produces("text/plain")
        public String get0() {
            return STR0;
        }


        @GET
        @Produces("text/plain")
        @Path("12")
        public String get5() {
            return STR12;
        }

        @Path("test")
        @POST
        @Consumes("text/plain")
        @Produces("text/plain")
        public String testLength(String entity, @DefaultValue("-1") @HeaderParam(HttpHeaders.CONTENT_LENGTH) String length) {
            return length;
        }
    }

    @Priority(300)
    public static class DoubleInterceptor implements WriterInterceptor, ReaderInterceptor {

        @Override
        public void aroundWriteTo(WriterInterceptorContext context) throws IOException, WebApplicationException {
            final OutputStream old = context.getOutputStream();
            context.setOutputStream(new OutputStream() {

                @Override
                public void write(int b) throws IOException {
                    old.write(b);
                    old.write(b);
                }
            });
            context.proceed();
            context.setOutputStream(old);
        }


        @Override
        public Object aroundReadFrom(ReaderInterceptorContext context) throws IOException, WebApplicationException {
            final InputStream old = context.getInputStream();
            try {
                context.setInputStream(new InputStream() {

                    @Override
                    public int read() throws IOException {
                        old.read();
                        return old.read();
                    }
                });

                return context.proceed();
            } finally {
                context.setInputStream(old);
            }

        }
    }

    @Override
    protected TestContainerFactory getTestContainerFactory() throws TestContainerException {
        return new InMemoryTestContainerFactory();
    }

    @Override
    protected Application configure() {
        final ResourceConfig resourceConfig = new ResourceConfig(Resource.class, DoubleInterceptor.class);
        resourceConfig.property(CommonProperties.OUTBOUND_CONTENT_LENGTH_BUFFER, BUFFER_SIZE);
        return resourceConfig;
    }

    @Override
    protected void configureClient(ClientConfig config) {
        config.property(CommonProperties.OUTBOUND_CONTENT_LENGTH_BUFFER, BUFFER_SIZE);
        config.register(DoubleInterceptor.class);
    }

    @Test
    public void test0() {
        final Response response = target().path("root/0").request().get();
        assertEquals(200, response.getStatus());
        final Object headerContentLength = Integer.valueOf(response.getHeaderString(HttpHeaders.CONTENT_LENGTH));
        assertEquals(STR0.length() * 2, headerContentLength);
        assertEquals(STR0, response.readEntity(String.class));
    }

    @Test
    public void testHead0() {
        final Response response = target().path("root/0").request().head();
        assertEquals(200, response.getStatus());
        final Object headerContentLength = Integer.valueOf(response.getHeaderString(HttpHeaders.CONTENT_LENGTH));
        assertEquals(STR0.length() * 2, headerContentLength);
    }

    @Test
    public void test6() {
        final Response response = target().path("root/6").request().get();
        assertEquals(200, response.getStatus());
        final Object headerContentLength = Integer.valueOf(response.getHeaderString(HttpHeaders.CONTENT_LENGTH));
        assertEquals(STR6.length() * 2, headerContentLength);
        assertEquals(STR6, response.readEntity(String.class));
    }

    @Test
    public void testHead6() {
        final Response response = target().path("root/6").request().head();
        assertEquals(200, response.getStatus());
        final Object headerContentLength = Integer.valueOf(response.getHeaderString(HttpHeaders.CONTENT_LENGTH));
        assertEquals(STR6.length() * 2, headerContentLength);
    }

    @Test
    public void test12() {
        final Response response = target().path("root/12").request().get();
        assertEquals(200, response.getStatus());
        checkEmptyContentLength(response);
        assertEquals(STR12, response.readEntity(String.class));
    }

    @Test
    public void testHead12() {
        final Response response = target().path("root/12").request().head();
        assertEquals(200, response.getStatus());
        checkEmptyContentLength(response);
    }

    private void checkEmptyContentLength(Response response) {
        final String headerString = response.getHeaderString(HttpHeaders.CONTENT_LENGTH);
        if (headerString != null) {
            final Object headerContentLength = Integer.valueOf(headerString);
            assertEquals(-1, headerContentLength);
        }
    }

    @Test
    public void testClientLength0() {
        final Response response = target().path("root/test").request().post(Entity.entity(STR0, MediaType.TEXT_PLAIN_TYPE));
        assertEquals(200, response.getStatus());
        assertEquals(STR0.length() * 2, response.readEntity(Integer.class).intValue());
    }

    @Test
    public void testClientLength6() {
        final Response response = target().path("root/test").request().post(Entity.entity(STR6, MediaType.TEXT_PLAIN_TYPE));
        assertEquals(200, response.getStatus());
        assertEquals(STR6.length() * 2, response.readEntity(Integer.class).intValue());
    }

    @Test
    public void testClientLength12() {
        final Response response = target().path("root/test").request().post(Entity.entity(STR12, MediaType.TEXT_PLAIN_TYPE));
        assertEquals(200, response.getStatus());
        assertEquals(-1, response.readEntity(Integer.class).intValue());
    }
}
