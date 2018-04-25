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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.concurrent.ExecutionException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.glassfish.jersey.server.ApplicationHandler;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.RequestContextBuilder;
import org.glassfish.jersey.server.ResourceConfig;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Taken from Jersey 1: jersey-tests:com.sun.jersey.impl.resource.AcceptWriterTest.java
 *
 * @author Paul Sandoz
 */
public class AcceptWriterTest {


    public static class StringWrapper {
        public final String s;

        public StringWrapper(String s) {
            this.s = s;
        }
    }

    @Provider
    @Produces("application/foo")
    public static class FooStringWriter implements MessageBodyWriter<StringWrapper> {
        MediaType foo = new MediaType("application", "foo");

        @Override
        public boolean isWriteable(
                Class<?> type,
                Type genericType,
                Annotation[] annotations,
                MediaType mediaType) {
           return type == StringWrapper.class && foo.isCompatible(mediaType);
        }

        @Override
        public long getSize(
                StringWrapper t,
                Class<?> type,
                Type genericType,
                Annotation[] annotations,
                MediaType mediaType) {
            return -1;
        }

        @Override
        public void writeTo(
                StringWrapper t,
                Class<?> type,
                Type genericType,
                Annotation[] annotations,
                MediaType mediaType,
                MultivaluedMap<String, Object> httpHeaders,
                OutputStream entityStream) throws IOException, WebApplicationException {
            String s = "foo: " + t.s;
            entityStream.write(s.getBytes());
        }
    }

    @Provider
    @Produces("application/bar")
    public static class BarStringWriter implements MessageBodyWriter<StringWrapper> {
        MediaType bar = new MediaType("application", "bar");

        @Override
        public boolean isWriteable(
                Class<?> type,
                Type genericType,
                Annotation[] annotations,
                MediaType mediaType) {
           return type == StringWrapper.class && bar.isCompatible(mediaType);
        }

        @Override
        public long getSize(
                StringWrapper t,
                Class<?> type,
                Type genericType,
                Annotation[] annotations,
                MediaType mediaType) {
            return -1;
        }

        @Override
        public void writeTo(
                StringWrapper t,
                Class<?> type,
                Type genericType,
                Annotation[] annotations,
                MediaType mediaType,
                MultivaluedMap<String, Object> httpHeaders,
                OutputStream entityStream) throws IOException, WebApplicationException {
            String s = "bar: " + t.s;
            entityStream.write(s.getBytes());
        }
    }

    @Path("/")
    public static class Resource {
        @GET
        public StringWrapper get() {
            return new StringWrapper("content");
        }
    }

    private void _test(ApplicationHandler app, String expected, String method, Object entity, String mediaType, String... accept)
            throws ExecutionException, InterruptedException {
        RequestContextBuilder requestContextBuilder = RequestContextBuilder.from("/", method);
        if (entity != null) {
            requestContextBuilder.entity(entity).type(mediaType);
        }
        ContainerRequest requestContext = requestContextBuilder.accept(accept).build();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        app.apply(requestContext, baos);
        assertEquals(expected, baos.toString());
    }

    @Test
    public void testAcceptGet() throws Exception {

        final ResourceConfig resourceConfig = new ResourceConfig(Resource.class, FooStringWriter.class, BarStringWriter.class);
        final ApplicationHandler app = new ApplicationHandler(resourceConfig);

        _test(app, "foo: content", "GET", null, null, "application/foo");
        _test(app, "foo: content", "GET", null, null, "applcation/baz, application/foo;q=0.8");
        _test(app, "bar: content", "GET", null, null, "application/bar");
        _test(app, "bar: content", "GET", null, null, "applcation/baz, application/bar;q=0.8");
    }
}
