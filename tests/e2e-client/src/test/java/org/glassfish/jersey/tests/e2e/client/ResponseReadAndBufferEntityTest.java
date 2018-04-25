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

package org.glassfish.jersey.tests.e2e.client;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;

import org.junit.Test;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * Buffered response entity tests.
 *
 * @author Michal Gajdos
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public class ResponseReadAndBufferEntityTest extends JerseyTest {

    private static final Logger LOGGER = Logger.getLogger(ResponseReadAndBufferEntityTest.class.getName());

    public static class CorruptableInputStream extends InputStream {

        private final AtomicInteger closeCounter = new AtomicInteger(0);

        private boolean corruptClose = false;
        private boolean corruptRead = false;

        private final ByteArrayInputStream delegate;

        public CorruptableInputStream() {
            this.delegate = new ByteArrayInputStream(Resource.ENTITY.getBytes());
        }

        @Override
        public synchronized int read() throws IOException {
            if (corruptRead) {
                corrupt();
            }
            return delegate.read();
        }

        @Override
        public int read(final byte[] b) throws IOException {
            if (corruptRead) {
                corrupt();
            }
            return delegate.read(b);
        }

        @Override
        public int read(final byte[] b, final int off, final int len) throws IOException {
            if (corruptRead) {
                corrupt();
            }
            return delegate.read(b, off, len);
        }

        @Override
        public long skip(final long n) throws IOException {
            if (corruptRead) {
                corrupt();
            }
            return delegate.skip(n);
        }

        @Override
        public int available() throws IOException {
            if (corruptRead) {
                corrupt();
            }
            return delegate.available();
        }

        @Override
        public boolean markSupported() {
            return delegate.markSupported();
        }

        @Override
        public void mark(final int readAheadLimit) {
            delegate.mark(readAheadLimit);
        }

        @Override
        public void reset() {
            closeCounter.set(0);
            delegate.reset();
        }

        @Override
        public void close() throws IOException {
            closeCounter.incrementAndGet();
            if (corruptClose) {
                corrupt();
            }
            delegate.close();
        }

        public void setCorruptRead(final boolean corruptRead) {
            this.corruptRead = corruptRead;
        }

        public void setCorruptClose(final boolean corruptClose) {
            this.corruptClose = corruptClose;
        }

        public int getCloseCount() {
            return closeCounter.get();
        }

        private static void corrupt() throws IOException {
            throw new IOException("Apocalypse Now");
        }
    }

    @Path("response")
    public static class Resource {

        public static final String ENTITY = "ENtiTy";

        @GET
        @Path("corrupted")
        public CorruptableInputStream corrupted() {
            return new CorruptableInputStream();
        }

        @GET
        @Path("string")
        public String string() {
            return ENTITY;
        }
    }

    @Override
    protected Application configure() {
        enable(TestProperties.DUMP_ENTITY);
        enable(TestProperties.LOG_TRAFFIC);

        return new ResourceConfig(Resource.class)
                .registerInstances(new LoggingFeature(LOGGER, LoggingFeature.Verbosity.PAYLOAD_ANY));
    }

    @Test
    public void testBufferEntityReadsOriginalStreamTest() throws Exception {
        final WebTarget target = target("response/corrupted");
        final CorruptableInputStream cis = new CorruptableInputStream();
        target.register(new ClientResponseFilter() {

            @Override
            public void filter(final ClientRequestContext requestContext, final ClientResponseContext responseContext) {
                responseContext.setEntityStream(cis);
            }

        });
        final Response response = target.request().buildGet().invoke();

        try {
            cis.setCorruptRead(true);
            response.bufferEntity();
            fail("ProcessingException expected.");
        } catch (ProcessingException pe) {
            // expected
            assertThat(pe.getCause(), instanceOf(IOException.class));
        }
    }

    @Test
    // See JERSEY-1340
    public void testSecondUnbufferedRead() throws Exception {
        final Response response = target("response/string").request(MediaType.TEXT_PLAIN).get();
        String entity = response.readEntity(String.class);
        assertEquals(Resource.ENTITY, entity);

        try {
            response.readEntity(Reader.class);
            fail("IllegalStateException expected to be thrown.");
        } catch (IllegalStateException expected) {
            // passed.
        }
    }

    @Test
    // See JERSEY-1339
    public void testSecondBufferedRead() throws Exception {
        final Response response = target("response/string").request(MediaType.TEXT_PLAIN).get();
        response.bufferEntity();

        String entity;

        entity = response.readEntity(String.class);
        assertEquals(Resource.ENTITY, entity);

        entity = response.readEntity(String.class);
        assertEquals(Resource.ENTITY, entity);

        BufferedReader buffered = new BufferedReader(response.readEntity(Reader.class));
        String line = buffered.readLine();
        assertEquals(Resource.ENTITY, line);

        byte[] buffer = new byte[0];
        buffer = response.readEntity(buffer.getClass());
        String entityFromBytes = new String(buffer);
        assertEquals(Resource.ENTITY, entityFromBytes);
    }

    /**
     * This method tests behavior of input stream operations in case the underlying input stream throws an exception when closed.
     * Reproducer for JRFCAF-1344.
     * <p>
     * UC-1 : Read unbuffered entity and then try to close the context
     */
    @Test
    public void testReadUnbufferedEntityFromStreamThatFailsToClose() throws Exception {

        final CorruptableInputStream entityStream = new CorruptableInputStream();
        final WebTarget target = target("response/corrupted");
        target.register(new ClientResponseFilter() {

            @Override
            public void filter(final ClientRequestContext requestContext, final ClientResponseContext responseContext) {
                responseContext.setEntityStream(entityStream);
            }

        });
        final Response response = target.request().buildGet().invoke();
        entityStream.setCorruptClose(true);

        // Read entity should not fail - we silently consume the underlying IOException from closed input stream.
        final String entity = response.readEntity(String.class, null);
        assertThat("Unexpected response.", entity.toString(), equalTo(Resource.ENTITY));
        assertEquals("Close not invoked on underlying input stream.", 1, entityStream.getCloseCount());

        // Close should not fail and should be idempotent
        response.close();
        response.close();
        response.close();
        assertEquals("Close invoked too many times on underlying input stream.", 1, entityStream.getCloseCount());

        try {
            // UC-1.1 : Try to read an unbuffered entity from a closed context
            response.readEntity(String.class, null);
            fail("IllegalStateException expected when reading from a closed context.");
            // UC-1.1 : END
        } catch (IllegalStateException ise) {
            // expected
        }
    }

    /**
     * This method tests behavior of input stream operations in case the underlying input stream throws an exception when closed.
     * Reproducer for JRFCAF-1344.
     * <p>
     * UC-2 : Read buffered entity multiple times and then try to close the context
     */
    @Test
    public void testReadBufferedEntityMultipleTimesFromStreamThatFailsToClose() throws Exception {
        final CorruptableInputStream entityStream = new CorruptableInputStream();
        final WebTarget target = target("response/corrupted");
        target.register(new ClientResponseFilter() {

            @Override
            public void filter(final ClientRequestContext requestContext, final ClientResponseContext responseContext) {
                responseContext.setEntityStream(entityStream);
            }

        });
        final Response response = target.request().buildGet().invoke();
        entityStream.setCorruptClose(true);

        response.bufferEntity();
        assertEquals("Close not invoked on underlying input stream.", 1, entityStream.getCloseCount());

        String entity;
        entity = response.readEntity(String.class, null);
        assertThat("Unexpected response.", entity.toString(), equalTo(Resource.ENTITY));
        entity = response.readEntity(String.class, null);
        assertThat("Unexpected response.", entity.toString(), equalTo(Resource.ENTITY));

        // Close should not fail and should be idempotent
        response.close();
        response.close();
        response.close();
        assertEquals("Close invoked too many times on underlying input stream.", 1, entityStream.getCloseCount());

        try {
            // UC-2.1 : Try to read a buffered entity from a closed context
            response.readEntity(String.class, null);
            fail("IllegalStateException expected when reading from a closed buffered context.");
            // UC-2.1 : END
        } catch (IllegalStateException ise) {
            // expected
        }
        // UC-2 : END

        entityStream.reset();

    }

    /**
     * This method tests behavior of input stream operations in case the underlying input stream throws an exception when closed.
     * Reproducer for JRFCAF-1344.
     * <p>
     * UC-3 : Try to close the response - underlying exception should be reported.
     */
    @Test
    public void testCloseUnreadResponseWithEntityStreamThatFailsToClose() throws Exception {
        final CorruptableInputStream entityStream = new CorruptableInputStream();
        final WebTarget target = target("response/corrupted");
        target.register(new ClientResponseFilter() {

            @Override
            public void filter(final ClientRequestContext requestContext, final ClientResponseContext responseContext) {
                responseContext.setEntityStream(entityStream);
            }

        });
        final Response response = target.request().buildGet().invoke();
        entityStream.setCorruptClose(true);

        try {
            response.close();
            fail("ProcessingException expected when closing the context and underlying stream throws an IOException.");
        } catch (ProcessingException pe) {
            assertThat(pe.getCause(), instanceOf(IOException.class));
        }
    }

}
