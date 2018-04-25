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

package org.glassfish.jersey.tests.api;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.RuntimeType;
import javax.ws.rs.ServerErrorException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.message.internal.MediaTypes;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Test;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * Various MessageBodyWriter tests.
 *
 * @author Michal Gajdos
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public class MessageBodyWriterTest extends JerseyTest {

    private static final String HEADER_NAME = "MessageBodyWriterTestHeader";
    private static final String HEADER_VALUE_CLIENT = "Client";
    private static final String HEADER_VALUE_SERVER = "Server";


    @Override
    protected Application configure() {
        return new ResourceConfig(Resource.class, OverridingStringProvider.class, HtmlStringProvider.class);
    }

    @Override
    protected void configureClient(final ClientConfig config) {
        config.register(OverridingStringProvider.class);
    }

    @Provider
    @Produces("text/plain")
    public static class OverridingStringProvider implements MessageBodyWriter<String> {

        @Context
        private Configuration config;

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
            // Underlying stream should not be closed and Jersey is preventing from closing it.
            entityStream.close();

            httpHeaders.putSingle(HEADER_NAME,
                    config.getRuntimeType() == RuntimeType.SERVER ? HEADER_VALUE_SERVER : HEADER_VALUE_CLIENT);

            entityStream.write(t.getBytes());
        }
    }

    @Provider
    @Produces("text/html")
    public static class HtmlStringProvider implements MessageBodyWriter<String> {

        @Context
        private Configuration config;

        @Override
        public boolean isWriteable(
                final Class<?> type,
                final Type genericType,
                final Annotation[] annotations,
                final MediaType mediaType) {

            if (MediaTypes.typeEqual(MediaType.TEXT_HTML_TYPE, mediaType)) {
                final String charset = mediaType.getParameters().get("charset");
                if (charset == null || !"utf-8".equalsIgnoreCase(charset)) {
                    throw new ServerErrorException("Charset not received in isWritable()", 501);
                }
            }

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
                final String entity,
                final Class<?> type,
                final Type genericType,
                final Annotation[] annotations,
                final MediaType mediaType,
                final MultivaluedMap<String, Object> httpHeaders,
                final OutputStream entityStream) throws IOException, WebApplicationException {

            final String charset = mediaType.getParameters().get("charset");
            if (charset == null || !"utf-8".equalsIgnoreCase(charset)) {
                throw new ServerErrorException("Charset not received in writeTo()", 502);
            }

            String html = "<html><body>" + entity + "</body></html>";
            entityStream.write(html.getBytes());
        }
    }

    /**
     * Test resource.
     */
    @Path("/")
    public static class Resource {

        @POST
        public String post(@HeaderParam(HEADER_NAME) final String header, final String post) {
            assertEquals(HEADER_VALUE_CLIENT, header);
            return post;
        }

        @GET
        @Produces("text/html;charset=utf-8")
        @Path("html-charset-in-produces")
        public String getHtmlForCharsetInProduces() {
            return "foo";
        }

        @GET
        @Path("html-charset-explicit-in-response")
        public Response getHtmlForExplicitCharsetInResponse() {
            return Response.ok("foo", MediaType.TEXT_HTML_TYPE.withCharset("utf-8")).build();
        }

        @GET
        @Produces("text/html")
        @Path("html-charset-in-accepts")
        public String getHtmlForCharsetInAccepts() {
            return "foo";
        }

    }

    /**
     * Test that it's possible to override default MessageBodyWriter instances.
     */
    @Test
    public void testOverride() {
        final Response response = target().request("text/plain").post(Entity.text("content"));

        assertEquals("content", response.readEntity(String.class));
        assertEquals(HEADER_VALUE_SERVER, response.getHeaderString(HEADER_NAME));
    }

    /**
     * Test that media type parameters specified in @Produces or explicitly in a server response are received
     * in the selected message body writer.
     */
    @Test
    public void testMediaTypeParametersInMessageBodyWriter() {
        Response response;

        response = target().path("html-charset-in-produces").request("text/html").get();
        assertThat("MediaType charset parameter in @Produces failed to propagate into MessageBodyWriter",
                response.getStatus(), equalTo(200));
        assertThat("Unexpected response content returned from server.",
                response.readEntity(String.class), equalTo("<html><body>foo</body></html>"));

        response = target().path("html-charset-explicit-in-response").request("text/html").get();
        assertThat("MediaType charset parameter explicitly set in Response failed to propagate into MessageBodyWriter",
                response.getStatus(), equalTo(200));
        assertThat("Unexpected response content returned from server.",
                response.readEntity(String.class), equalTo("<html><body>foo</body></html>"));

        response = target().path("html-charset-in-accepts").request(MediaType.TEXT_HTML_TYPE.withCharset("utf-8")).get();
        assertThat("MediaType charset parameter sent with request in Accept header failed to propagate into MessageBodyWriter",
                response.getStatus(), equalTo(200));
        assertThat("Unexpected response content returned from server.",
                response.readEntity(String.class), equalTo("<html><body>foo</body></html>"));
    }
}
