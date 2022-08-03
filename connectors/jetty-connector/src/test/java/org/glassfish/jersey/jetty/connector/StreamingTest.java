/*
 * Copyright (c) 2013, 2019 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.jetty.connector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.glassfish.jersey.CommonProperties;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;

public class StreamingTest extends JerseyTest {
    private static final Logger LOGGER = Logger.getLogger(StreamingTest.class.getName());
    private static final String PATH = "streaming";

    /**
     * Streaming test resource.
     */
    @Path(PATH)
    public static class StreamingResource {


        /**
         * Long-running streaming request
         *
         * @param count number of packets send
         * @param pauseMillis pause between each packets
         */
        @GET
        @Path("stream")
        public Response streamsWithDelay(@QueryParam("start") @DefaultValue("0") int startMillis, @QueryParam("count") int count,
        @QueryParam("pauseMillis") int pauseMillis) {
            StreamingOutput streamingOutput = streamSlowly(startMillis, count, pauseMillis);

            return Response.ok(streamingOutput)
                    .build();
        }

    }

    private static StreamingOutput streamSlowly(int startMillis, int count, int pauseMillis) {

        return output -> {
            try {
                TimeUnit.MILLISECONDS.sleep(startMillis);
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            output.write("begin\n".getBytes(StandardCharsets.UTF_8));
            output.flush();
            for (int i = 0; i < count; i++) {
                try {
                    TimeUnit.MILLISECONDS.sleep(pauseMillis);
                }
                catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                output.write(("message " + i + "\n").getBytes(StandardCharsets.UTF_8));
                output.flush();
            }
            output.write("end".getBytes(StandardCharsets.UTF_8));
        };
    }
            @Override
    protected Application configure() {
        return new ResourceConfig(StreamingResource.class)
                .register(new LoggingFeature(LOGGER, LoggingFeature.Verbosity.PAYLOAD_ANY));
    }

    @Override
    protected void configureClient(ClientConfig config) {
        // TODO: fails with true on request - should be fixed by resolving JERSEY-2273
        config.register(new LoggingFeature(LOGGER, LoggingFeature.Verbosity.HEADERS_ONLY));
        config.connectorProvider(new JettyConnectorProvider());
    }

    /**
     * Test accessing an operation that is streaming slowly
     *
     * @throws ProcessingException in case of a test error.
     */
    @Test
    public void testSlowlyStreamedContentDoesNotTimeout() throws Exception {

        int count = 5;
        int pauseMillis = 50;

        final Response response = target(PATH)
                .property(ClientProperties.READ_TIMEOUT, 100L)
                .property(CommonProperties.OUTBOUND_CONTENT_LENGTH_BUFFER_SERVER, "-1")
                .path("stream")
                .queryParam("count", count)
                .queryParam("pauseMillis", pauseMillis)
                .request().get();

            assertTrue(response.readEntity(String.class).contains("end"));
    }

    /**
     * Test accessing an operation that is streaming slowly
     *
     * @throws ProcessingException in case of a test error.
     */
    @Test
    public void testSlowToStartStreamedContentTimeout() throws Exception {

        int start = 150;
        int count = 5;
        int pauseMillis = 50;

        try {
            final Response response = target(PATH)
                    .property(ClientProperties.READ_TIMEOUT, 100L)
                    .property(CommonProperties.OUTBOUND_CONTENT_LENGTH_BUFFER_SERVER, "-1")
                    .path("stream")
                    .queryParam("start", start)
                    .queryParam("count", count)
                    .queryParam("pauseMillis", pauseMillis)
                    .request().get();
            fail("This operation should trigger idle timeout");
        } catch (ProcessingException e) {
            assertEquals(TimeoutException.class, e.getCause().getClass());
        }


    }
}
