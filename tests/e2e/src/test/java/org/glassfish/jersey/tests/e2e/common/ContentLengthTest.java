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

package org.glassfish.jersey.tests.e2e.common;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Pavel Bucek (pavel.bucek at oracle.com)
 */
public class ContentLengthTest extends JerseyTest {

    public static String STR = "string";

    @Override
    protected Application configure() {
        ResourceConfig rc = new ResourceConfig();
        rc.registerClasses(MyTypeResource.class, MyTypeWriter.class, ResourceGetByteNoHead.class);
        return rc;
    }

    public static class MyType {

        public String s = STR;
    }

    @Path("/")
    public static class MyTypeResource {

        @GET
        public MyType getMyType() {
            return new MyType();
        }
    }

    @Provider
    public static class MyTypeWriter implements MessageBodyWriter<MyType> {

        @Override
        public boolean isWriteable(Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType) {
            return aClass.equals(MyType.class);
        }

        @Override
        public long getSize(MyType myType, Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType) {
            return myType.s.length();
        }

        @Override
        public void writeTo(MyType myType,
                            Class<?> aClass,
                            Type type,
                            Annotation[] annotations,
                            MediaType mediaType,
                            MultivaluedMap<String, Object> stringObjectMultivaluedMap,
                            OutputStream outputStream) throws IOException, WebApplicationException {
            outputStream.write(myType.s.getBytes());
        }
    }

    @Test
    public void testGetContentLengthCustomWriter() throws Exception {
        Response response = target().request().get(Response.class);
        assertEquals(200, response.getStatus());
        assertEquals(STR.length(), Integer.parseInt(response.getHeaderString(HttpHeaders.CONTENT_LENGTH)));
        assertTrue(response.hasEntity());
    }

    @Test
    public void testHeadContentLengthCustomWriter() throws Exception {
        Response response = target().request().head();
        assertEquals(200, response.getStatus());
        assertEquals(STR.length(), Integer.parseInt(response.getHeaderString(HttpHeaders.CONTENT_LENGTH)));
        assertFalse(response.hasEntity());
    }

    @Path("/byte")
    public static class ResourceGetByteNoHead {

        @GET
        public byte[] get() {
            return "GET".getBytes();
        }
    }

    @Test
    public void testGetByte() throws Exception {
        Response response = target().path("byte").request().get(Response.class);
        assertEquals(200, response.getStatus());
        assertEquals(3, Integer.parseInt(response.getHeaderString(HttpHeaders.CONTENT_LENGTH)));
        assertTrue(response.hasEntity());
    }

    @Test
    public void testHeadByte() throws Exception {
        Response response = target().path("byte").request().head();
        assertEquals(200, response.getStatus());
        assertEquals(3, Integer.parseInt(response.getHeaderString(HttpHeaders.CONTENT_LENGTH)));
        assertFalse(response.hasEntity());
    }
}
