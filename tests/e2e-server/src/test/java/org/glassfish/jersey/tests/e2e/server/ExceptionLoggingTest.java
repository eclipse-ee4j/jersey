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

package org.glassfish.jersey.tests.e2e.server;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.logging.Level;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Make sure exceptions, that are not mapped to responses get logged.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
public class ExceptionLoggingTest extends JerseyTest {

    private static class MyCheckedException extends Exception {}

    private static class MyRuntimeException extends RuntimeException {}

    @Path("/")
    public static class ExceptionResource {

        @Path("runtime")
        public String runtimeException() {
            throw new MyRuntimeException();
        }

        @Path("checked")
        public String checkedException() throws MyCheckedException {
            throw new MyCheckedException();
        }
    }

    @Override
    protected Application configure() {
        set(TestProperties.RECORD_LOG_LEVEL, Level.FINE.intValue());

        return new ResourceConfig(ExceptionResource.class, Writer.class, Resource.class);
    }

    @Test
    public void testRuntime() throws Exception {
        final Response response = target().path("runtime").request().get();
        assertEquals(500, response.getStatus());
        assertEquals(getLastLoggedRecord().getThrown().getClass(), MyRuntimeException.class);
    }

    @Test
    public void testChecked() throws Exception {
        final Response response = target().path("checked").request().get();
        assertEquals(500, response.getStatus());
        assertEquals(getLastLoggedRecord().getThrown().getClass(), MyCheckedException.class);
    }

    @Provider
    @Produces(MediaType.WILDCARD)
    public static class Writer implements MessageBodyWriter<ExceptionLoggingTestPOJO> {

        @Override
        public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return type == ExceptionLoggingTestPOJO.class;
        }

        @Override
        public long getSize(ExceptionLoggingTestPOJO entityForReader, Class<?> type, Type genericType,
                            Annotation[] annotations, MediaType mediaType) {
            return 0;
        }

        @Override
        public void writeTo(ExceptionLoggingTestPOJO entityForReader, Class<?> type, Type genericType,
                            Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders,
                            OutputStream entityStream) throws IOException, WebApplicationException {
            throw new RuntimeException("test");
        }
    }

    @Path("resource")
    public static class Resource {

        @Path("entity")
        @GET
        public ExceptionLoggingTestPOJO entity() {
            return new ExceptionLoggingTestPOJO();
        }
    }

    @Test
    public void testReaderFails() throws Exception {
        final Response response = target().path("resource/entity").request().get();
        assertEquals(500, response.getStatus());

        assertEquals(getLastLoggedRecord().getThrown().getMessage(), "test");
    }

    static class ExceptionLoggingTestPOJO {

    }
}
