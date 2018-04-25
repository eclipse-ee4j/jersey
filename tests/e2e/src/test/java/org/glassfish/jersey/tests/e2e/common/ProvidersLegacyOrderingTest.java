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
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.MessageBodyWriter;

import org.glassfish.jersey.message.MessageBodyWorkers;
import org.glassfish.jersey.message.MessageProperties;
import org.glassfish.jersey.message.internal.MediaTypes;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Pavel Bucek (pavel.bucek at oracle.com)
 */
public class ProvidersLegacyOrderingTest extends JerseyTest {

    @Produces("application/xml")
    public static class MyMBW5 implements MessageBodyWriter<Object> {

        @Override
        public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return false;
        }

        @Override
        public long getSize(Object myType, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return -1;
        }

        @Override
        public void writeTo(Object myType,
                            Class<?> type,
                            Type genericType,
                            Annotation[] annotations,
                            MediaType mediaType,
                            MultivaluedMap<String, Object> httpHeaders,
                            OutputStream entityStream) throws IOException, WebApplicationException {
        }
    }

    @Produces("application/*")
    public static class MyMBW6 implements MessageBodyWriter<Object> {

        @Override
        public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return false;
        }

        @Override
        public long getSize(Object myType, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return -1;
        }

        @Override
        public void writeTo(Object myType,
                            Class<?> type,
                            Type genericType,
                            Annotation[] annotations,
                            MediaType mediaType,
                            MultivaluedMap<String, Object> httpHeaders,
                            OutputStream entityStream) throws IOException, WebApplicationException {
        }
    }

    @Path("/")
    public static class MyResource {

        @Context
        MessageBodyWorkers messageBodyWorkers;

        @GET
        public String getMyType() {

            final Map<MediaType, List<MessageBodyWriter>> writers = messageBodyWorkers.getWriters(MediaType.APPLICATION_XML_TYPE);
            final List<MessageBodyWriter> messageBodyWriters1 = writers.get(MediaType.APPLICATION_XML_TYPE);
            final List<MessageBodyWriter> messageBodyWriters2 = writers
                    .get(MediaTypes.getTypeWildCart(MediaType.APPLICATION_XML_TYPE));

            // custom provider has priority - it has to be first
            assertTrue(messageBodyWriters1.get(0) instanceof MyMBW5);
            assertTrue(messageBodyWriters2.get(0) instanceof MyMBW6);

            return "";
        }
    }

    @Override
    protected Application configure() {
        final ResourceConfig resourceConfig = new ResourceConfig(MyResource.class, MyMBW5.class, MyMBW6.class);
        resourceConfig.property(MessageProperties.LEGACY_WORKERS_ORDERING, true);
        return resourceConfig;
    }

    @Test
    public void orderingTest() {
        try {
            Response response = target().request("application/test1").get(Response.class);

            assertNotNull(response);
            assertEquals("Request was not handled correctly, most likely fault in MessageBodyWorker selection.",
                    200, response.getStatus());
        } catch (Exception e) {
            fail("Request was not handled correctly, most likely fault in MessageBodyWorker selection.");
        }
    }
}
