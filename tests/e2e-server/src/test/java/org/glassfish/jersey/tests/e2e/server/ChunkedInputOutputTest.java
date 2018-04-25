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

import javax.ws.rs.GET;
import javax.ws.rs.NameBinding;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;

import org.glassfish.jersey.client.ChunkedInput;
import org.glassfish.jersey.server.ChunkedOutput;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Test;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * Chunked input/output tests.
 *
 * @author Pavel Bucek (pavel.bucek at oracle.com)
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public class ChunkedInputOutputTest extends JerseyTest {
    private static final Logger LOGGER = Logger.getLogger(ChunkedInputOutputTest.class.getName());

    /**
     * Test resource.
     */
    @Path("/test")
    public static class TestResource {
        /**
         * Get chunk stream.
         *
         * @return chunk stream.
         */
        @GET
        public ChunkedOutput<String> get() {
            final ChunkedOutput<String> output = new ChunkedOutput<>(String.class, "\r\n");

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

        assertEquals("Unexpected value of chunked response unmarshalled as a single string.",
                "test\r\ntest\r\ntest\r\n", response);
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
            assertEquals("Unexpected value of chunk " + counter, "test", chunk);
            counter++;
        }

        assertEquals("Unexpected numbed of received chunks.", 3, counter);
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
            assertEquals("Unexpected value of chunk " + counter, "test", chunk);
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
