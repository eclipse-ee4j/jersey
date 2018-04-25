/*
 * Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.jdk.connector.internal;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.RequestEntityProcessing;
import org.glassfish.jersey.jdk.connector.JdkConnectorProvider;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * @author Petr Janouch (petr.janouch at oracle.com)
 */
public class ModifyHeaderInBodyWriterTest extends JerseyTest {

    private static final String WRITE_BYTES = "write bytes";
    private static final String WRITE_BYTE = "byte";
    private static final String HEADER_NAME = "myHeader";
    private static final String HEADER_VALUE = "myHeaderValue";

    @Override
    protected Application configure() {
        return new ResourceConfig(Resource.class, HeaderModifyingWriter.class);
    }

    @Override
    protected void configureClient(ClientConfig config) {
        config.register(HeaderModifyingWriter.class);
        config.connectorProvider(new JdkConnectorProvider());
    }

    @Test
    public void testBufferedWriteBytes() {
        doTestWriteBytes(RequestEntityProcessing.BUFFERED);
    }

    @Test
    public void testChunkedWriteBytes() {
        doTestWriteBytes(RequestEntityProcessing.CHUNKED);
    }

    private void doTestWriteBytes(RequestEntityProcessing requestEntityProcessing) {
        Response response = target("echo").request().property(ClientProperties.REQUEST_ENTITY_PROCESSING, requestEntityProcessing)
                .post(Entity.entity(WRITE_BYTES, MediaType.TEXT_PLAIN));
        assertEquals(200, response.getStatus());
        assertEquals(HEADER_VALUE, response.getHeaderString(HEADER_NAME));
        assertEquals(WRITE_BYTES, response.readEntity(String.class));
    }

    @Test
    public void testBufferedWriteByte() {
        doTestWriteByte(RequestEntityProcessing.BUFFERED);
    }

    @Test
    public void testChunkedWriteByte() {
        doTestWriteByte(RequestEntityProcessing.CHUNKED);
    }

    private void doTestWriteByte(RequestEntityProcessing requestEntityProcessing) {
        Response response = target("echo").request().property(ClientProperties.REQUEST_ENTITY_PROCESSING, requestEntityProcessing)
                .post(Entity.entity(WRITE_BYTE, MediaType.TEXT_PLAIN));
        assertEquals(200, response.getStatus());
        assertEquals(HEADER_VALUE, response.getHeaderString(HEADER_NAME));
        assertEquals(WRITE_BYTE, response.readEntity(String.class));
    }

    @Test
    public void testBufferedWriteNothing() {
        doTestWriteNothing(RequestEntityProcessing.BUFFERED);
    }

    @Test
    public void testChunkedWriteNothing() {
        doTestWriteNothing(RequestEntityProcessing.CHUNKED);
    }

    private void doTestWriteNothing(RequestEntityProcessing requestEntityProcessing) {
        Response response = target("echo").request().property(ClientProperties.REQUEST_ENTITY_PROCESSING, requestEntityProcessing)
                .post(Entity.entity("", MediaType.TEXT_PLAIN));
        assertEquals(200, response.getStatus());
        assertEquals(HEADER_VALUE, response.getHeaderString(HEADER_NAME));
        assertEquals("", response.readEntity(String.class));
    }

    @Provider
    @Produces("text/plain")
    public static class HeaderModifyingWriter implements MessageBodyWriter<String> {

        @Override
        public boolean isWriteable(
                final Class<?> type,
                final Type genericType,
                final Annotation[] annotations,
                final MediaType mediaType) {
            return type == String.class;
        }

        @Override
        public long getSize(
                final String t,
                final Class<?> type,
                final Type genericType,
                final Annotation[] annotations,
                final MediaType mediaType) {
            return -1;
        }

        @Override
        public void writeTo(
                final String t,
                final Class<?> type,
                final Type genericType,
                final Annotation[] annotations,
                final MediaType mediaType,
                final MultivaluedMap<String, Object> httpHeaders,
                final OutputStream entityStream) throws IOException, WebApplicationException {

            httpHeaders.putSingle(HEADER_NAME, HEADER_VALUE);

            if (WRITE_BYTES.equals(t)) {
                entityStream.write(t.getBytes());
            }

            if (WRITE_BYTE.equals(t)) {
                for (byte b : t.getBytes()) {
                    entityStream.write(b);
                }
            }
        }
    }

    @Path("/")
    public static class Resource {

        @Path("echo")
        @Produces("text/html")
        @POST
        public Response echo(String msg, @HeaderParam(HEADER_NAME) String header) {
            return Response.ok().entity(msg).header(HEADER_NAME, HEADER_VALUE).build();
        }
    }
}
