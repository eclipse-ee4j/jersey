/*
 * Copyright (c) 2012, 2024 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.e2e.server;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.NameBinding;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.ext.WriterInterceptor;
import jakarta.ws.rs.ext.WriterInterceptorContext;

import org.glassfish.jersey.client.ChunkedInput;
import org.glassfish.jersey.server.ChunkedOutput;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.jupiter.api.Test;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Chunked input/output tests.
 *
 * @author Pavel Bucek
 * @author Marek Potociar
 */
public class ChunkedInputOutputTest extends JerseyTest {
    private static final Logger LOGGER = Logger.getLogger(ChunkedInputOutputTest.class.getName());

    /**
     * Test resource.
     */
    @Path("/test")
    public static class TestResource {
        /**
         * Get chunk stream with a queue capacity of 2.
         *
         * @return chunk stream.
         */
        @GET
        @Path("/testWithBuilder")
        public ChunkedOutput<String> getWithBuilder() {
            return getOutput(ChunkedOutput.<String>builder(String.class).queueCapacity(2)
                             .chunkDelimiter("\r\n".getBytes()).build());
        }

        /**
         * Get chunk stream.
         *
         * @return chunk stream.
         */
        @GET
        public ChunkedOutput<String> get() {
            return getOutput(new ChunkedOutput<>(String.class, "\r\n"));
        }

        /**
         * Get chunk stream.
         *
         * @return chunk stream.
         */
        private ChunkedOutput<String> getOutput(ChunkedOutput<String> output) {

            new Thread() {
                @Override
                public void run() {
                    try {
                        output.write("test");
                        output.write("test");
                        output.write("test");
                    } catch (final IOException e) {
                        LOGGER.log(Level.SEVERE, "Error writing chunk.", e);
                    } finally {
                        try {
                            output.close();
                        } catch (final IOException e) {
                            LOGGER.log(Level.INFO, "Error closing chunked output.", e);
                        }
                    }
                }
            }.start();

            return output;
        }

        /**
         * Get chunk stream with an attached interceptor.
         *
         * @return intercepted chunk stream.
         */
        @GET
        @Path("intercepted")
        @Intercepted
        public ChunkedOutput<String> interceptedGet() {
            return get();
        }
    }

    /**
     * Test interceptor binding.
     */
    @NameBinding
    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface Intercepted {

    }

    /**
     * Test interceptor - counts number of interception as well as number of wrapper output stream method calls.
     */
    @Intercepted
    public static class TestWriterInterceptor implements WriterInterceptor {

        private static final AtomicInteger interceptCounter = new AtomicInteger(0);
        private static final AtomicInteger writeCounter = new AtomicInteger(0);
        private static final AtomicInteger flushCounter = new AtomicInteger(0);
        private static final AtomicInteger closeCounter = new AtomicInteger(0);

        @Override
        public void aroundWriteTo(final WriterInterceptorContext context) throws IOException, WebApplicationException {
            interceptCounter.incrementAndGet();
            final OutputStream out = context.getOutputStream();
            context.setOutputStream(new OutputStream() {
                @Override
                public void write(final int b) throws IOException {
                    writeCounter.incrementAndGet();
                    out.write(b);
                }

                @Override
                public void write(final byte[] b) throws IOException {
                    writeCounter.incrementAndGet();
                    out.write(b);
                }

                @Override
                public void write(final byte[] b, final int off, final int len) throws IOException {
                    writeCounter.incrementAndGet();
                    out.write(b, off, len);
                }

                @Override
                public void flush() throws IOException {
                    flushCounter.incrementAndGet();
                    out.flush();
                }

                @Override
                public void close() throws IOException {
                    closeCounter.incrementAndGet();
                    out.close();
                }
            });
            context.proceed();
        }
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(TestResource.class, TestWriterInterceptor.class);
    }

    /**
     * Test retrieving chunked response stream as a single response string.
     *
     * @throws Exception in case of a failure during the test execution.
     */
    @Test
    public void testChunkedOutputToSingleString() throws Exception {
        final String response = target().path("test").request().get(String.class);

        assertEquals("test\r\ntest\r\ntest\r\n", response,
                "Unexpected value of chunked response unmarshalled as a single string.");
    }

    /**
     * Test retrieving chunked response stream as a single response string, when a builder with capacity is used.
     *
     * @throws Exception in case of a failure during the test execution.
     */
    @Test
    public void testChunkedOutputToSingleStringWithBuilder() throws Exception {
        final String response = target().path("test/testWithBuilder").request().get(String.class);

        assertEquals("test\r\ntest\r\ntest\r\n", response,
                "Unexpected value of chunked response unmarshalled as a single string.");
    }

    /**
     * Test retrieving chunked response stream sequentially as individual chunks using chunked input.
     *
     * @throws Exception in case of a failure during the test execution.
     */
    @Test
    public void testChunkedOutputToChunkInput() throws Exception {
        final ChunkedInput<String> input = target().path("test").request().get(new GenericType<ChunkedInput<String>>() {
        });

        int counter = 0;
        String chunk;
        while ((chunk = input.read()) != null) {
            assertEquals("test", chunk, "Unexpected value of chunk " + counter);
            counter++;
        }

        assertEquals(3, counter, "Unexpected numbed of received chunks.");
    }

    /**
     * Test retrieving intercepted chunked response stream sequentially as individual chunks using chunked input.
     *
     * @throws Exception in case of a failure during the test execution.
     */
    @Test
    public void testInterceptedChunkedOutputToChunkInput() throws Exception {
        final ChunkedInput<String> input = target().path("test/intercepted")
                .request().get(new GenericType<ChunkedInput<String>>() {
                });

        int counter = 0;
        String chunk;
        while ((chunk = input.read()) != null) {
            assertEquals("test", chunk, "Unexpected value of chunk " + counter);
            counter++;
        }

        assertThat("Unexpected numbed of received chunks.",
                counter, equalTo(3));

        assertThat("Unexpected number of chunked output interceptions.",
                TestWriterInterceptor.interceptCounter.get(), equalTo(1));
        assertThat("Unexpected number of intercepted output write calls.",
                TestWriterInterceptor.writeCounter.get(), greaterThanOrEqualTo(1));
        assertThat("Unexpected number of intercepted output flush calls.",
                TestWriterInterceptor.flushCounter.get(), greaterThanOrEqualTo(3));
        assertThat("Unexpected number of intercepted output close calls.",
                TestWriterInterceptor.closeCounter.get(), equalTo(1));
    }
}
