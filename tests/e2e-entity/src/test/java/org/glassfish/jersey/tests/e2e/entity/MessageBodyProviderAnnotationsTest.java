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

package org.glassfish.jersey.tests.e2e.entity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Type;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Tests annotations passed to message body provider.
 *
 * @author Miroslav Fuksa
 */
public class MessageBodyProviderAnnotationsTest extends JerseyTest {

    @Override
    protected Application configure() {
        return new ResourceConfig(ContainerReaderWriter.class, Resource.class);
    }

    @Test
    public void testWriter() {
        String get = target().path("test").request("test/text").get(String.class);
        assertEquals("get-ok", get);
    }

    @Test
    public void testReader() {
        String get = target().path("test").request("text/plain").post(Entity.entity("test", "test/text"), String.class);
        assertEquals("ok", get);
    }


    @Produces("test/text")
    @Consumes("test/text")
    public static class ContainerReaderWriter implements MessageBodyWriter<String>, MessageBodyReader<Bean> {

        @Override
        public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return type == String.class;
        }

        @Override
        public long getSize(String s, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return -1;
        }

        @Override
        public void writeTo(String s, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
                            MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException,
                WebApplicationException {
            OutputStreamWriter osw = new OutputStreamWriter(entityStream);

            if (compareAnnotations(new Class<?>[]{MyAnnotation.class, Produces.class, GET.class}, annotations)) {
                osw.write(s + "-ok");
            } else {
                osw.write(s + "-fail");
            }
            osw.flush();
        }

        @Override
        public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return type == Bean.class;
        }

        @Override
        public Bean readFrom(Class<Bean> type, Type genericType, Annotation[] annotations, MediaType mediaType,
                             MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException,
                WebApplicationException {
            if (compareAnnotations(new Class<?>[]{MyAnnotation.class}, annotations)) {
                return new Bean("ok");
            } else {
                return new Bean("fail");
            }
        }
    }

    public static class Bean {
        private final String value;

        public Bean(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    private static boolean compareAnnotations(Class[] expected, Annotation[] actual) {
        if (expected.length != actual.length) {
            return false;
        }

        for (Class<?> e : expected) {
            boolean found = false;
            for (Annotation a : actual) {
                if (e.equals(a.annotationType())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }
        return true;
    }

    public static final Annotation ANNOTATION;

    @Retention(RetentionPolicy.RUNTIME)
    public static @interface MyAnnotation {
    }

    static {
        @MyAnnotation
        class TempClass {
        }
        ;
        ANNOTATION = TempClass.class.getAnnotation(MyAnnotation.class);
    }

    @Path("test")
    public static class Resource {
        @GET
        @Produces("test/text")
        public Response get() {
            Response response = Response.ok().entity("get", new Annotation[]{ANNOTATION}).build();
            return response;
        }

        @POST
        @Consumes("test/text")
        @Produces("text/plain")
        public String post(@MyAnnotation Bean entity) {
            return entity.getValue();
        }
    }
}
