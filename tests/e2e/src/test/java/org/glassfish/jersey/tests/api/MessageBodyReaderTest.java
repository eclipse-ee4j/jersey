/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.message.internal.ReaderWriter;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * @author Jan Supol (jan.supol at oracle.com)
 * @author Michal Gajdos
 */
public class MessageBodyReaderTest extends JerseyTest {

    @Path("resource")
    public static class Resource {

        @Context
        private HttpHeaders headers;

        @POST
        @Path("plain")
        public String plain(final EntityForReader entity) {
            return entity.getValue() + ";" + headers.getHeaderString(HttpHeaders.CONTENT_TYPE);
        }
    }

    @Provider
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    public static class AppOctetReader implements MessageBodyReader<EntityForReader> {

        @Override
        public boolean isReadable(final Class<?> type, final Type genericType, final Annotation[] annotations,
                                  final MediaType mediaType) {
            return MediaType.APPLICATION_OCTET_STREAM_TYPE.equals(mediaType);
        }

        @Override
        public EntityForReader readFrom(final Class<EntityForReader> type,
                                        final Type genericType,
                                        final Annotation[] annotations,
                                        final MediaType mediaType,
                                        final MultivaluedMap<String, String> httpHeaders,
                                        final InputStream entityStream) throws IOException, WebApplicationException {
            // Underlying stream should not be closed and Jersey is preventing from closing it.
            entityStream.close();

            return new EntityForReader(ReaderWriter.readFromAsString(entityStream, mediaType));
        }
    }

    public static class EntityForReader {

        private String value;

        public EntityForReader(final String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(Resource.class).register(LoggingFeature.class).register(AppOctetReader.class);
    }

    /**
     * Test whether the default {@link MediaType} ({@value MediaType#APPLICATION_OCTET_STREAM}) is passed to a reader if no
     * {@value HttpHeaders#CONTENT_TYPE} value is provided in a request.
     */
    @Test
    public void testDefaultContentTypeForReader() throws Exception {
        final HttpPost httpPost = new HttpPost(UriBuilder.fromUri(getBaseUri()).path("resource/plain").build());
        httpPost.setEntity(new ByteArrayEntity("value".getBytes()));
        httpPost.removeHeaders("Content-Type");

        final HttpClient httpClient = HttpClientBuilder.create().build();
        final HttpResponse response = httpClient.execute(httpPost);

        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals("value;null", ReaderWriter.readFromAsString(response.getEntity().getContent(), null));
    }
}
