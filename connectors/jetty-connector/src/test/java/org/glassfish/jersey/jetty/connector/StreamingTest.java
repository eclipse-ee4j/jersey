/*
 * Copyright (c) 2013, 2022 Oracle and/or its affiliates. All rights reserved.
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
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
    public static final String FIELD_CONTENT = "a field";

    @Path("/test")
    public static class StreamingResource {

        /**
         * Long-running streaming request
         *
         * @param count       number of packets send
         * @param pauseMillis pause between each packets
         */
        @GET
        @Produces(MediaType.TEXT_PLAIN)
        @Path("stream")
        public Response streamsWithDelay(@QueryParam("start") @DefaultValue("0") int startMillis, @QueryParam("count") int count,
                @QueryParam("pauseMillis") int pauseMillis) {
            StreamingOutput streamingOutput = streamSlowly(startMillis, count, pauseMillis);

            return Response.ok(streamingOutput)
                    .build();
        }

        @GET
        @Produces(MediaType.APPLICATION_JSON)
        @Path("json")
        public AnObject json() {
            return new AnObject(FIELD_CONTENT, 42);
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
        ResourceConfig config = new ResourceConfig(StreamingResource.class);
        config.register(new LoggingFeature(LOGGER, LoggingFeature.Verbosity.PAYLOAD_ANY));
        return config;
    }

    @Override
    protected void configureClient(ClientConfig config) {
        config.connectorProvider(new JettyConnectorProvider());
    }

    /**
     * Test accessing an operation that is streaming slowly
     *
     * @throws ProcessingException in case of a test error.
     */
    @Test
    public void testDataStreamedASAP() throws Exception {

        int count = 5;
        int pauseMillis = 1000;

        long start = System.currentTimeMillis();
        final Future<Response> future = target("test")
                .property(CommonProperties.OUTBOUND_CONTENT_LENGTH_BUFFER_SERVER, "-1")
                .path("stream")
                .queryParam("count", count)
                .queryParam("pauseMillis", pauseMillis)
                .request()
                .async()
                .get();

        Response response = future.get();
        StreamingStatistics stats = computeOutputStatistics(() -> response.readEntity(InputStream.class), start);

        assertThat("Listening the stream for bytes starts after headers are received",
                stats.timeToStartReadingBytes, lessThan(500L));
        assertThat("The first bytes are forwarded ASAP", stats.timeToFirstByte, lessThan(500L));
        assertThat("Last bytes come way after the start due to the streaming pauses", stats.timeToLastByte, greaterThan(5000L));
        assertThat("Data should be complete", stats.data, endsWith("end"));
    }

    /**
     * Test accessing an operation that is streaming slowly
     *
     * @throws ProcessingException in case of a test error.
     */
    @Test
    public void testJettyThreadShouldNotDeadlock() throws Exception {

        /**
         * This test fails due to a deadlock when reading the entity in org.glassfish.jersey.client.JerseyInvocation#translate
         * The entity reading seems to be triggered by calling the response callback, but since this is done in the Jetty thread
         * that is also pushing contents to the buffer, the result is a deadlock.
         *
         * The observed behavior is a timeout.
         */
        AnObject result = target("test")
                .property(ClientProperties.READ_TIMEOUT, "10000") // remove this timeout to deadlock indefinitely
                .property(CommonProperties.OUTBOUND_CONTENT_LENGTH_BUFFER_SERVER, "-1")
                .path("json")
                .request()
                .async()
                .get(AnObject.class)
                .get();

        assertThat( result.getaField(), equalTo(FIELD_CONTENT));
    }

    private StreamingStatistics computeOutputStatistics(Supplier<InputStream> stream, long httpCallStart) throws IOException {
        AtomicLong timeToStartReading = new AtomicLong(System.currentTimeMillis() - httpCallStart);
        AtomicLong timeToFirstBytes = new AtomicLong(0);
        AtomicLong timeToLastBytes = new AtomicLong(0);
        AtomicReference<String> data = new AtomicReference<>("none");

        try (InputStream in = stream.get()) {

            byte[] buffer = new byte[4];
            StringBuffer stringBuffer = new StringBuffer();
            int consumed = -1;

            while ((consumed = in.read(buffer)) != -1) {
                timeToFirstBytes.compareAndSet(0L, System.currentTimeMillis() - httpCallStart); // initialize on first iteration
                String message = new String(buffer, 0, consumed, StandardCharsets.UTF_8);
                stringBuffer.append(message);
                LOGGER.log(Level.INFO, "got {0} after {1}ms", new Object[] {message, System.currentTimeMillis() - httpCallStart});
            }

            timeToLastBytes.compareAndSet(0L, System.currentTimeMillis() - httpCallStart);
            data.set(stringBuffer.toString());
        }

        return new StreamingStatistics(timeToStartReading.get(), timeToFirstBytes.get(), timeToLastBytes.get(), data.get());
    }

    public static class StreamingStatistics {

        private final Long timeToStartReadingBytes;
        private final Long timeToFirstByte;

        private final Long timeToLastByte;
        private final String data;

        public StreamingStatistics(Long timeToReadingBytes, Long timeToFirstByte, Long timeToLastByte, String data) {
            this.timeToStartReadingBytes = timeToReadingBytes;
            this.timeToFirstByte = timeToFirstByte;
            this.timeToLastByte = timeToLastByte;
            this.data = data;
        }
    }

    public static class AnObject {
        private String aField;
        private int anotherField;

        public AnObject() {
            //empty constructor for jackson
        }

        public AnObject(String aField, int anotherField) {
            this.aField = aField;
            this.anotherField = anotherField;
        }

        public String getaField() {
            return aField;
        }

        public void setaField(String aField) {
            this.aField = aField;
        }

        public int getAnotherField() {
            return anotherField;
        }

        public void setAnotherField(int anotherField) {
            this.anotherField = anotherField;
        }
    }
}
