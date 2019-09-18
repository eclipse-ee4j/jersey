/*
 * Copyright (c) 2019 Oracle and/or its affiliates. All rights reserved.
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

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.ReaderInterceptor;
import javax.ws.rs.ext.ReaderInterceptorContext;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class HttpHeadersInjectionTest extends JerseyTest {
    public static final String HEADER_NAME = "UserHeader";

    @Override
    protected Application configure() {
        return new ResourceConfig(Resource.class);
    }

    @Path("/")
    public static class Resource {
        @Context
        HttpHeaders headers;

        @POST
        public Response get(int i) {
            return Response.ok(i).header(HEADER_NAME, String.valueOf(i + 1)).build();
        }
    }

    public static class HttpHeadersFilter implements ClientRequestFilter {

        @Context
        HttpHeaders headers;

        @Override
        public void filter(ClientRequestContext requestContext) throws IOException {
            requestContext.abortWith(Response.ok(headers.getHeaderString(HEADER_NAME)).build());
        }
    }

    public static class HttpHeadersResponseFilter implements ClientResponseFilter {

        @Context
        HttpHeaders headers;

        private static int headerValue = 0;

        public int getHeaderValue() {
            return headerValue;
        }

        @Override
        public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) throws IOException {
            headerValue = Integer.parseInt(headers.getHeaderString(HEADER_NAME));
        }
    }

    @Produces(MediaType.TEXT_PLAIN)
    public static class StringWriter implements MessageBodyWriter<String> {
        @Context
        HttpHeaders headers;

        @Override
        public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return true;
        }

        @Override
        public void writeTo(String s, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
                            MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException,
                            WebApplicationException {
            entityStream.write(headers.getHeaderString(HEADER_NAME).getBytes());
            entityStream.flush();
        }
    }

    @Consumes({MediaType.TEXT_PLAIN})
    public static class StringReader implements MessageBodyReader<String> {

        @Context
        HttpHeaders headers;

        @Override
        public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return true;
        }

        @Override
        public String readFrom(Class<String> type, Type genericType, Annotation[] annotations, MediaType mediaType,
                               MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException,
                                    WebApplicationException {
            return headers.getHeaderString(HEADER_NAME);
        }
    }

    public static class HttpHeadersReaderInterceptor implements ReaderInterceptor {

        @Context
        HttpHeaders headers;

        @Override
        public Object aroundReadFrom(ReaderInterceptorContext context) throws IOException, WebApplicationException {
            context.getInputStream().close();
            context.setInputStream(new ByteArrayInputStream(headers.getHeaderString(HEADER_NAME).getBytes()));
            return context.proceed();
        }
    }

    public static class HttpHeadersWriterInterceptor implements WriterInterceptor {

        @Context
        HttpHeaders headers;

        @Override
        public void aroundWriteTo(WriterInterceptorContext context) throws IOException, WebApplicationException {
            OutputStreamWriter osw = new OutputStreamWriter(context.getOutputStream());
            osw.append(headers.getHeaderString(HEADER_NAME));
            osw.flush();
            context.proceed();
        }
    }

    @Test
    public void testHttpHeadersInjectionInRequestFilter() {
        final String value = "headerValue";
        final String response = target().register(HttpHeadersFilter.class).request().header(HEADER_NAME, value)
                .buildGet().invoke(String.class);
        assertThat("the HttpHeaders was not injected to RequestFilter", response, is(value));
    }

    @Test
    public void testHttpHeadersInjectionInResponseFilter() {
        final String value = "1";
        final HttpHeadersResponseFilter filter = new HttpHeadersResponseFilter();
        final String response = target().register(HttpHeadersResponseFilter.class).request().header(HEADER_NAME, value)
                .buildPost(Entity.entity(value, MediaType.TEXT_PLAIN_TYPE)).invoke(String.class);
        assertThat(response, is(value));
        assertThat("the HttpHeaders was not injected to ResponseFilter", filter.getHeaderValue(), is(1));
    }

    @Test
    public void testHttpHeadersInjectionInMBW() {
        final String value = "1";
        final String response = target().register(StringWriter.class).request().header(HEADER_NAME, value)
                .buildPost(Entity.entity("something", MediaType.TEXT_PLAIN_TYPE)).invoke(String.class);
        assertThat("the HttpHeaders was not injected to MessageBodyWriter", response, is(value));
    }

    @Test
    public void testHttpHeadersInjectionInMBR() {
        final String value = "1";
        final String header = "100";
        final String response = target().register(StringReader.class).request().header(HEADER_NAME, header)
                .buildPost(Entity.entity(value, MediaType.TEXT_PLAIN_TYPE)).invoke(String.class);
        assertThat("the HttpHeaders was not injected to MessageBodyReader", response, is(header));
    }

    @Test
    public void testHttpHeadersInjectionInReaderInterceptor() {
        final String header = "100";
        final String response = target().register(HttpHeadersReaderInterceptor.class).request().header(HEADER_NAME, header)
                .buildPost(Entity.entity("0", MediaType.TEXT_PLAIN_TYPE)).invoke(String.class);
        assertThat("the HttpHeaders was not injected to Reader Interceptor", response, is(header));
    }

    @Test
    public void testHttpHeadersInjectionInWriterInterceptor() {
        final String header = "100";
        final Response response = target().register(HttpHeadersWriterInterceptor.class).request().header(HEADER_NAME, header)
                .buildPost(Entity.entity("0", MediaType.TEXT_PLAIN_TYPE)).invoke();
        assertThat("the HttpHeaders was not injected to Reader Interceptor", response.readEntity(String.class),
                is("1000"));
        assertThat(response.getHeaderString(HEADER_NAME), is("1001")); //100 interceptor, 0 entity = 1000, +1 in resource
        response.close();
    }
}
