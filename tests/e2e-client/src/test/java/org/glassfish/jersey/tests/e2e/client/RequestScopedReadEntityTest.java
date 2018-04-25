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

package org.glassfish.jersey.tests.e2e.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import javax.inject.Inject;
import javax.inject.Provider;

import org.glassfish.jersey.client.ClientRequest;
import org.glassfish.jersey.message.internal.AbstractMessageReaderWriterProvider;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * TODO: javadoc.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public class RequestScopedReadEntityTest extends JerseyTest {

    public static class Message {

        private final String text;

        public Message(String text) {
            this.text = text;
        }
    }

    @Path("simple")
    public static class SimpleResource {

        @GET
        @Produces("text/plain")
        public String getIt() {
            return "passed";
        }
    }

    @Produces("text/plain")
    public static class ScopedMessageEntityProvider extends AbstractMessageReaderWriterProvider<Message> {

        @Inject
        private Provider<ClientRequest> clientRequestProvider;

        @Override
        public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return type == Message.class && mediaType.equals(MediaType.TEXT_PLAIN_TYPE);
        }

        @Override
        public Message readFrom(
                Class<Message> type,
                Type genericType, Annotation[] annotations,
                MediaType mediaType,
                MultivaluedMap<String, String> httpHeaders,
                InputStream entityStream) throws IOException, WebApplicationException {
            return clientRequestProvider.get() != null
                    ? new Message(readFromAsString(entityStream, mediaType)) : new Message("failed");
        }

        @Override
        public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return type == Message.class && mediaType.equals(MediaType.TEXT_PLAIN_TYPE);
        }

        @Override
        public void writeTo(
                Message message,
                Class<?> type,
                Type genericType,
                Annotation[] annotations,
                MediaType mediaType,
                MultivaluedMap<String, Object> httpHeaders,
                OutputStream entityStream) throws IOException, WebApplicationException {
            writeToAsString((clientRequestProvider.get() != null) ? message.text : "failed", entityStream, mediaType);
        }
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(SimpleResource.class);
    }

    @Test
    public void testReadAfterClose() {
        final Invocation.Builder request = target().path("simple").register(ScopedMessageEntityProvider.class).request();

        final Response response = request.get(Response.class);
        // reading entity "out-of-scope"
        final Message msg = response.readEntity(Message.class);
        assertEquals("passed", msg.text);
    }
}
