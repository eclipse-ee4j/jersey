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

package org.glassfish.jersey.client.filter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.concurrent.Future;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import static javax.ws.rs.core.HttpHeaders.ACCEPT_ENCODING;
import static javax.ws.rs.core.HttpHeaders.CONTENT_ENCODING;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.ClientRequest;
import org.glassfish.jersey.client.ClientResponse;
import org.glassfish.jersey.client.spi.AsyncConnectorCallback;
import org.glassfish.jersey.client.spi.Connector;
import org.glassfish.jersey.client.spi.ConnectorProvider;
import org.glassfish.jersey.message.DeflateEncoder;
import org.glassfish.jersey.message.GZipEncoder;

import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Client-side content encoding filter unit tests.
 *
 * @author Martin Matula
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public class EncodingFilterTest {
    @Test
    public void testAcceptEncoding() {
        Client client = ClientBuilder.newClient(new ClientConfig(
                EncodingFilter.class,
                GZipEncoder.class,
                DeflateEncoder.class
        ).connectorProvider(new TestConnector()));
        Invocation.Builder invBuilder = client.target(UriBuilder.fromUri("/").build()).request();
        Response r = invBuilder.get();
        assertEquals("deflate,gzip,x-gzip", r.getHeaderString(ACCEPT_ENCODING));
        assertNull(r.getHeaderString(CONTENT_ENCODING));
    }

    @Test
    public void testContentEncoding() {
        Client client = ClientBuilder.newClient(new ClientConfig(
                EncodingFilter.class,
                GZipEncoder.class,
                DeflateEncoder.class
        ).property(ClientProperties.USE_ENCODING, "gzip").connectorProvider(new TestConnector()));
        Invocation.Builder invBuilder = client.target(UriBuilder.fromUri("/").build()).request();
        Response r = invBuilder.post(Entity.entity("Hello world", MediaType.TEXT_PLAIN_TYPE));
        assertEquals("deflate,gzip,x-gzip", r.getHeaderString(ACCEPT_ENCODING));
        assertEquals("gzip", r.getHeaderString(CONTENT_ENCODING));
    }

    @Test
    public void testContentEncodingViaFeature() {
        Client client = ClientBuilder.newClient(new ClientConfig()
                .connectorProvider(new TestConnector())
                .register(new EncodingFeature("gzip", GZipEncoder.class, DeflateEncoder.class)));
        Invocation.Builder invBuilder = client.target(UriBuilder.fromUri("/").build()).request();
        Response r = invBuilder.post(Entity.entity("Hello world", MediaType.TEXT_PLAIN_TYPE));
        assertEquals("deflate,gzip,x-gzip", r.getHeaderString(ACCEPT_ENCODING));
        assertEquals("gzip", r.getHeaderString(CONTENT_ENCODING));
    }

    @Test
    public void testContentEncodingSkippedForNoEntity() {
        Client client = ClientBuilder.newClient(new ClientConfig(
                EncodingFilter.class,
                GZipEncoder.class,
                DeflateEncoder.class
        ).property(ClientProperties.USE_ENCODING, "gzip").connectorProvider(new TestConnector()));
        Invocation.Builder invBuilder = client.target(UriBuilder.fromUri("/").build()).request();
        Response r = invBuilder.get();
        assertEquals("deflate,gzip,x-gzip", r.getHeaderString(ACCEPT_ENCODING));
        assertNull(r.getHeaderString(CONTENT_ENCODING));
    }

    @Test
    public void testUnsupportedContentEncoding() {
        Client client = ClientBuilder.newClient(new ClientConfig(
                EncodingFilter.class,
                GZipEncoder.class,
                DeflateEncoder.class
        ).property(ClientProperties.USE_ENCODING, "non-gzip").connectorProvider(new TestConnector()));
        Invocation.Builder invBuilder = client.target(UriBuilder.fromUri("/").build()).request();
        Response r = invBuilder.get();
        assertEquals("deflate,gzip,x-gzip", r.getHeaderString(ACCEPT_ENCODING));
        assertNull(r.getHeaderString(CONTENT_ENCODING));
    }

    /**
     * Reproducer for JERSEY-2028.
     *
     * @see #testClosingClientResponseStreamRetrievedByValueOnError
     */
    @Test
    public void testClosingClientResponseStreamRetrievedByResponseOnError() {
        final TestInputStream responseStream = new TestInputStream();

        Client client = ClientBuilder.newClient(new ClientConfig()
                .connectorProvider(new TestConnector() {
                    @Override
                    public ClientResponse apply(ClientRequest requestContext) throws ProcessingException {
                        final ClientResponse responseContext = new ClientResponse(Response.Status.OK, requestContext);
                        responseContext.header(CONTENT_ENCODING, "gzip");
                        responseContext.setEntityStream(responseStream);
                        return responseContext;
                    }
                })
                .register(new EncodingFeature(GZipEncoder.class, DeflateEncoder.class)));

        final Response response = client.target(UriBuilder.fromUri("/").build()).request().get();
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals("gzip", response.getHeaderString(CONTENT_ENCODING));

        try {
            response.readEntity(String.class);
            fail("Exception caused by invalid gzip stream expected.");
        } catch (ProcessingException ex) {
            assertTrue("Response input stream not closed when exception is thrown.", responseStream.isClosed);
        }
    }

    /**
     * Reproducer for JERSEY-2028.
     *
     * @see #testClosingClientResponseStreamRetrievedByResponseOnError
     */
    @Test
    public void testClosingClientResponseStreamRetrievedByValueOnError() {
        final TestInputStream responseStream = new TestInputStream();

        Client client = ClientBuilder.newClient(new ClientConfig()
                .connectorProvider(new TestConnector() {
                    @Override
                    public ClientResponse apply(ClientRequest requestContext) throws ProcessingException {
                        final ClientResponse responseContext = new ClientResponse(Response.Status.OK, requestContext);
                        responseContext.header(CONTENT_ENCODING, "gzip");
                        responseContext.setEntityStream(responseStream);
                        return responseContext;
                    }
                })
                .register(new EncodingFeature(GZipEncoder.class, DeflateEncoder.class)));

        try {
            client.target(UriBuilder.fromUri("/").build()).request().get(String.class);
            fail("Exception caused by invalid gzip stream expected.");
        } catch (ProcessingException ex) {
            assertTrue("Response input stream not closed when exception is thrown.", responseStream.isClosed);
        }
    }

    private static class TestConnector implements Connector, ConnectorProvider {

        @Override
        public Connector getConnector(Client client, Configuration runtimeConfig) {
            return this;
        }

        @Override
        public ClientResponse apply(ClientRequest requestContext) {
            final ClientResponse responseContext = new ClientResponse(
                    Response.Status.OK, requestContext);

            String headerValue = requestContext.getHeaderString(ACCEPT_ENCODING);
            if (headerValue != null) {
                responseContext.header(ACCEPT_ENCODING, headerValue);
            }
            headerValue = requestContext.getHeaderString(CONTENT_ENCODING);
            if (headerValue != null) {
                responseContext.header(CONTENT_ENCODING, headerValue);
            }
            return responseContext;
        }

        @Override
        public Future<?> apply(ClientRequest clientRequest, AsyncConnectorCallback callback) {
            throw new UnsupportedOperationException("Asynchronous execution not supported.");
        }

        @Override
        public void close() {
            // do nothing
        }

        @Override
        public String getName() {
            return "test-connector";
        }
    }

    private static class TestInputStream extends ByteArrayInputStream {

        private boolean isClosed;

        private TestInputStream() {
            super("test".getBytes());
        }

        @Override
        public void close() throws IOException {
            isClosed = true;
            super.close();
        }
    }
}
