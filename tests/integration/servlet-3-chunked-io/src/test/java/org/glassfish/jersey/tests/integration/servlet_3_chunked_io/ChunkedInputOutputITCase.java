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

package org.glassfish.jersey.tests.integration.servlet_3_chunked_io;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URLConnection;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.glassfish.jersey.client.ChunkedInput;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.grizzly.connector.GrizzlyConnectorProvider;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.external.ExternalTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.glassfish.jersey.test.spi.TestContainerFactory;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Chunked I/O integration tests.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public class ChunkedInputOutputITCase extends JerseyTest {

    private static final int MAX_LISTENERS = 5;

    @Override
    protected Application configure() {
        return new Application();
    }

    @Override
    protected TestContainerFactory getTestContainerFactory() throws TestContainerException {
        return new ExternalTestContainerFactory();
    }

    @Override
    protected void configureClient(final ClientConfig config) {
        config.register(App.createMoxyJsonResolver());

        config.property(ClientProperties.CONNECT_TIMEOUT, 15000)
                .property(ClientProperties.READ_TIMEOUT, 5000)
                .property(ClientProperties.ASYNC_THREADPOOL_SIZE, MAX_LISTENERS + 1)
                .connectorProvider(new GrizzlyConnectorProvider());
    }

    @Override
    protected URI getBaseUri() {
        final UriBuilder baseUriBuilder = UriBuilder.fromUri(super.getBaseUri());
        final boolean externalFactoryInUse = getTestContainerFactory() instanceof ExternalTestContainerFactory;
        return externalFactoryInUse ? baseUriBuilder.path("resources").build() : baseUriBuilder.build();
    }

    /**
     * Test retrieving string-based chunked stream as a single response string.
     *
     * @throws Exception in case of a failure during the test execution.
     */
    @Test
    public void testChunkedOutputToSingleString() throws Exception {
        final String response = target().path("test/from-string").request(MediaType.APPLICATION_JSON_TYPE).get(String.class);

        assertEquals("Unexpected value of chunked response unmarshalled as a single string.",
                "{\"id\":0,\"data\":\"test\"}\r\n"
                        + "{\"id\":1,\"data\":\"test\"}\r\n"
                        + "{\"id\":2,\"data\":\"test\"}\r\n",
                response);
    }

    /**
     * Test retrieving string-based chunked stream sequentially as individual chunks using chunked input.
     *
     * @throws Exception in case of a failure during the test execution.
     */
    @Test
    public void testChunkedOutputToChunkInputFromString() throws Exception {
        final ChunkedInput<Message> input = target().path("test/from-string").request(MediaType.APPLICATION_JSON_TYPE)
                .get(new GenericType<ChunkedInput<Message>>() {
                });

        int counter = 0;
        Message chunk;
        while ((chunk = input.read()) != null) {
            assertEquals("Unexpected value of chunk " + counter, new Message(counter, "test"), chunk);
            counter++;
        }

        assertEquals("Unexpected numbed of received chunks.", 3, counter);
    }

    /**
     * Test retrieving POJO-based chunked stream sequentially as individual chunks using chunked input.
     *
     * @throws Exception in case of a failure during the test execution.
     */
    @Test
    public void testChunkedOutputToChunkInputFromPojo() throws Exception {
        final ChunkedInput<Message> input = target().path("test/from-pojo").request(MediaType.APPLICATION_JSON_TYPE)
                .get(new GenericType<ChunkedInput<Message>>() {
                });

        int counter = 0;
        Message chunk;
        while ((chunk = input.read()) != null) {
            assertEquals("Unexpected value of chunk " + counter, new Message(counter, "test"), chunk);
            counter++;
        }

        assertEquals("Unexpected numbed of received chunks.", 3, counter);
    }

    /**
     * Test combination of AsyncResponse and ChunkedOutput.
     */
    @Test
    public void chunkedOutputWithAsyncResponse() throws Exception {
        final ChunkedInput<Message> input = target().path("test/chunked-async").request(MediaType.APPLICATION_JSON_TYPE)
                .get(new GenericType<ChunkedInput<Message>>() {
                });

        int counter = 0;
        Message chunk;
        while ((chunk = input.read()) != null) {
            assertEquals("Unexpected value of chunk " + counter, new Message(counter, "test"), chunk);
            counter++;
        }

        assertEquals("Unexpected numbed of received chunks.", 3, counter);
    }

    /**
     * Reproducer for JERSEY-2558. Checking that the connection is properly closed even when the
     * {@link org.glassfish.jersey.server.ChunkedOutput#close()} is called before the response is processed by the runtime.
     */
    @Test
    public void checkConnectionIsClosedUrlConnection() throws Exception {
        final URI uri = UriBuilder.fromUri(super.getBaseUri()).path("test/close-before-return").build();
        final URLConnection connection = uri.toURL().openConnection();

        connection.setConnectTimeout(15000);
        connection.setReadTimeout(15000);
        connection.connect();

        final BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

        String line;
        int counter = 0;
        while ((line = reader.readLine()) != null) {
            assertEquals("Unexpected value of chunk " + counter, new Message(counter, "test").toString(), line);
            counter++;
        }

        assertEquals("Unexpected numbed of received chunks.", 3, counter);
    }
}
