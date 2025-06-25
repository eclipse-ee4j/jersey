/*
 * Copyright (c) 2024, 2025 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.server;


import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;
import org.glassfish.jersey.internal.MapPropertiesDelegate;
import org.glassfish.jersey.io.spi.FlushedCloseable;
import org.glassfish.jersey.message.MessageBodyWorkers;
import org.glassfish.jersey.server.RequestContextBuilder.TestContainerRequest;
import org.glassfish.jersey.server.spi.ContainerResponseWriter;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ContainerResponseWriterNoFlushTest {
    private static final String RESPONSE = "RESPONSE";
    private static AtomicInteger flushCounter = new AtomicInteger(0);
    private static class TestResponseOutputStream extends ByteArrayOutputStream implements FlushedCloseable {
        private boolean closed = false;
        @Override
        public void close() throws IOException {
            if (!closed) {
                closed = true;
                flush();
                super.close();
            }
        }

        @Override
        public void flush() throws IOException {
            flushCounter.incrementAndGet();
        }
    }

    private static class TestContainerWriter implements ContainerResponseWriter {
        TestResponseOutputStream outputStream;
        private final boolean buffering;

        private TestContainerWriter(boolean buffering) {
            this.buffering = buffering;
        }

        @Override
        public OutputStream writeResponseStatusAndHeaders(long contentLength, ContainerResponse responseContext)
                throws ContainerException {
            outputStream = new TestResponseOutputStream();
            return outputStream;
        }

        @Override
        public boolean suspend(long timeOut, TimeUnit timeUnit, TimeoutHandler timeoutHandler) {
            return false;
        }

        @Override
        public void setSuspendTimeout(long timeOut, TimeUnit timeUnit) throws IllegalStateException {
        }

        @Override
        public void commit() {
        }

        @Override
        public void failure(Throwable error) {
            throw new RuntimeException(error);
        }

        @Override
        public boolean enableResponseBuffering() {
            return buffering;
        }
    }

    @Path("/test")
    public static class StreamResource {

        @GET
        @Path(value = "/stream")
        @Produces(MediaType.TEXT_PLAIN)
        public Response stream() {

            StreamingOutput stream = output -> {
                output.write(RESPONSE.getBytes(StandardCharsets.UTF_8));
            };
            return Response.ok(stream).build();
        }
    }

    @Test
    public void testWriterBuffering() {
        TestContainerWriter writer = new TestContainerWriter(true);
        testWriter(writer);
    }

    @Test
    public void testWriterNoBuffering() {
        TestContainerWriter writer = new TestContainerWriter(false);
        testWriter(writer);
    }

    private void testWriter(TestContainerWriter writer) {
        flushCounter.set(0);
        RequestContextBuilder rcb = RequestContextBuilder.from("/test/stream", "GET");

        TestContainerRequest request = rcb.new TestContainerRequest(
                null, URI.create("/test/stream"), "GET", null, new MapPropertiesDelegate()) {
            @Override
            public void setWorkers(MessageBodyWorkers workers) {
                if (workers != null) {
                    setWriter(writer);
                }
                super.setWorkers(workers);
            }
        };

        ApplicationHandler applicationHandler = new ApplicationHandler(new ResourceConfig(StreamResource.class));
        Future<ContainerResponse> future = applicationHandler.apply(request);
        MatcherAssert.assertThat(writer.outputStream.toString(), Matchers.is(RESPONSE));
        MatcherAssert.assertThat(flushCounter.get(), Matchers.is(1));
    }
}