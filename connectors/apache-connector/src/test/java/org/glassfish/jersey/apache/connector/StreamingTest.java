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

package org.glassfish.jersey.apache.connector;

import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.inject.Singleton;

import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.server.ChunkedOutput;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * @author Petr Janouch (petr.janouch at oracle.com)
 */
public class StreamingTest extends JerseyTest {
    private PoolingHttpClientConnectionManager connectionManager;

    /**
     * Test that a data stream can be terminated from the client side.
     */
    @Test
    public void clientCloseTest() throws IOException {
        // start streaming
        InputStream inputStream = target().path("/streamingEndpoint").request()
                .property(ClientProperties.READ_TIMEOUT, 1_000).get(InputStream.class);

        WebTarget sendTarget = target().path("/streamingEndpoint/send");
        // trigger sending 'A' to the stream; OK is sent if everything on the server was OK
        assertEquals("OK", sendTarget.request().get().readEntity(String.class));
        // check 'A' has been sent
        assertEquals('A', inputStream.read());
        // closing the stream should tear down the connection
        inputStream.close();
        // trigger sending another 'A' to the stream; it should fail
        // (indicating that the streaming has been terminated on the server)
        assertEquals("NOK", sendTarget.request().get().readEntity(String.class));
    }

    /**
     * Tests that closing a response after completely reading the entity reuses the connection
     */
    @Test
    public void reuseConnectionTest() throws IOException {
        Response response = target().path("/streamingEndpoint/get").request().get();
        InputStream is = response.readEntity(InputStream.class);
        byte[] buf = new byte[8192];
        is.read(buf);
        is.close();
        response.close();

        assertEquals(1, connectionManager.getTotalStats().getAvailable());
        assertEquals(0, connectionManager.getTotalStats().getLeased());
    }

    /**
     * Tests that closing a request without reading the entity does not throw an exception.
     */
    @Test
    public void clientCloseThrowsNoExceptionTest() throws IOException {
        Response response = target().path("/streamingEndpoint/get").request().get();
        response.close();
    }

    @Override
    protected void configureClient(ClientConfig config) {
        connectionManager = new PoolingHttpClientConnectionManager();
        config.property(ApacheClientProperties.CONNECTION_MANAGER, connectionManager);
        config.connectorProvider(new ApacheConnectorProvider());
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(StreamingEndpoint.class);
    }

    @Singleton
    @Path("streamingEndpoint")
    public static class StreamingEndpoint {

        private final ChunkedOutput<String> output = new ChunkedOutput<>(String.class);

        @GET
        @Path("send")
        public String sendEvent() {
            try {
                output.write("A");
            } catch (IOException e) {
                return "NOK";
            }

            return "OK";
        }

        @GET
        @Produces(MediaType.TEXT_PLAIN)
        public ChunkedOutput<String> get() {
            return output;
        }

        @GET
        @Path("get")
        @Produces(MediaType.TEXT_PLAIN)
        public String getString() {
            return "OK";
        }
    }
}
