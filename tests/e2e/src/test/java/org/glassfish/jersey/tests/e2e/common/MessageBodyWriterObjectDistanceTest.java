/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Object.class needs special handling when computing type distance - it should be always further than any other
 * implemented interface.
 *
 * @author Pavel Bucek (pavel.bucek at oracle.com)
 */
@SuppressWarnings("WeakerAccess")
public class MessageBodyWriterObjectDistanceTest extends JerseyTest {

    public interface InterfaceA {

    }

    public interface InterfaceB extends InterfaceA {

    }

    @Provider
    @Produces("application/test")
    public static class ObjectWriter implements MessageBodyWriter<Object> {

        @Override
        public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return true;
        }

        @Override
        public long getSize(Object o, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return -1;
        }

        @Override
        public void writeTo(Object o, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
                            MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException,
                WebApplicationException {
            entityStream.write("object".getBytes("UTF-8"));
        }
    }

    @Provider
    @Produces("application/test")
    public static class InterfaceAWriter implements MessageBodyWriter<InterfaceA> {
        @Override
        public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return InterfaceA.class.isAssignableFrom(type);
        }

        @Override
        public long getSize(InterfaceA interfaceA, Class<?> type, Type genericType, Annotation[] annotations,
                            MediaType mediaType) {
            return -1;
        }

        @Override
        public void writeTo(InterfaceA interfaceA, Class<?> type, Type genericType, Annotation[] annotations,
                            MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
                throws IOException, WebApplicationException {
            entityStream.write(InterfaceA.class.getSimpleName().getBytes("UTF-8"));
        }
    }

    @Path("resource")
    public static class Resource {

        @GET
        @Produces("application/test")
        public InterfaceB getStringModel() {
            return new InterfaceB() {
            };
        }
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(Resource.class)
                .register(ObjectWriter.class)
                .register(InterfaceAWriter.class);
    }

    @Test
    public void testMessageBodyWriterObjectDistance() throws Exception {
        assertEquals(InterfaceA.class.getSimpleName(),
                     target().path("resource").request("application/test").get(String.class));
    }
}
