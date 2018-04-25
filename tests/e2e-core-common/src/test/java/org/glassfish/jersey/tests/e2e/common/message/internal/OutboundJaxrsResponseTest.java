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

package org.glassfish.jersey.tests.e2e.common.message.internal;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.RuntimeDelegate;

import org.glassfish.jersey.message.internal.OutboundJaxrsResponse;
import org.glassfish.jersey.message.internal.OutboundMessageContext;
import org.glassfish.jersey.tests.e2e.common.TestRuntimeDelegate;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * OutboundJaxrsResponse unit tests.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public class OutboundJaxrsResponseTest {

    private static class TestInputStream extends ByteArrayInputStream {

        private boolean isRead;
        private boolean isClosed;

        private TestInputStream() {
            super("test".getBytes());
        }

        @Override
        public synchronized int read() {
            final int read = super.read();
            isRead = read == -1;
            return read;
        }

        @Override
        public synchronized int read(byte[] b, int off, int len) {
            final int read = super.read(b, off, len);
            isRead = read == -1;
            return read;
        }

        @Override
        public int read(byte[] b) throws IOException {
            final int read = super.read(b);
            isRead = read == -1;
            return read;
        }

        @Override
        public void close() throws IOException {
            isClosed = true;
            super.close();
        }
    }

    private static final OutboundMessageContext.StreamProvider TEST_PROVIDER
            = new OutboundMessageContext.StreamProvider() {
        @Override
        public OutputStream getOutputStream(int contentLength) throws IOException {
            return new ByteArrayOutputStream();
        }
    };

    private Response.ResponseBuilder rb;

    /**
     * Create test class.
     */
    public OutboundJaxrsResponseTest() {
        RuntimeDelegate.setInstance(new TestRuntimeDelegate());
    }

    @Before
    public void setUp() {
        rb = new OutboundJaxrsResponse.Builder(new OutboundMessageContext()).status(Response.Status.OK);
    }


    /**
     * Test of empty entity buffering.
     */
    @Test
    public void testBufferEmptyEntity() {
        final OutboundJaxrsResponse r = OutboundJaxrsResponse.from(rb.build());
        r.getContext().setStreamProvider(TEST_PROVIDER);

        assertFalse("Buffer entity should return 'false' if no entity.", r.bufferEntity());
    }

    /**
     * Test of non-stream entity buffering.
     */
    @Test
    public void testBufferNonStreamEntity() {
        final OutboundJaxrsResponse r = OutboundJaxrsResponse.from(rb.entity(new Object()).build());
        r.getContext().setStreamProvider(TEST_PROVIDER);

        assertFalse("Buffer entity should return 'false' for non-stream entity.", r.bufferEntity());
    }

    /**
     * Test of stream entity buffering.
     */
    @Test
    public void testBufferStreamEntity() {
        TestInputStream tis = new TestInputStream();
        final OutboundJaxrsResponse r = OutboundJaxrsResponse.from(rb.entity(tis).build());
        r.getContext().setStreamProvider(TEST_PROVIDER);

        assertTrue("Buffer entity should return 'true' for stream entity.", r.bufferEntity());
        assertTrue("Second call to buffer entity should return 'true' for stream entity.", r.bufferEntity()); // second call
        assertTrue("Buffered stream has not been fully read.", tis.isRead);
        assertTrue("Buffered stream has not been closed after buffering.", tis.isClosed);
    }

    /**
     * Test of closing response with empty entity.
     */
    @Test
    public void testCloseEmptyEntity() {
        final OutboundJaxrsResponse r = OutboundJaxrsResponse.from(rb.build());
        r.getContext().setStreamProvider(TEST_PROVIDER);

        r.close();
        try {
            r.bufferEntity();
            fail("IllegalStateException expected when buffering entity on closed response.");
        } catch (IllegalStateException ex) {
            // ok
        }
        r.close(); // second call should pass
    }

    /**
     * Test of closing response with non-stream entity.
     */
    @Test
    public void testCloseNonStreamEntity() {
        final OutboundJaxrsResponse r = OutboundJaxrsResponse.from(rb.entity(new Object()).build());
        r.getContext().setStreamProvider(TEST_PROVIDER);

        r.close();
        try {
            r.bufferEntity();
            fail("IllegalStateException expected when buffering entity on closed response.");
        } catch (IllegalStateException ex) {
            // ok
        }
        r.close(); // second call should pass
    }

    /**
     * Test of closing response with stream entity.
     */
    @Test
    public void testCloseStreamEntity() {
        TestInputStream tis = new TestInputStream();
        final OutboundJaxrsResponse r = OutboundJaxrsResponse.from(rb.entity(tis).build());
        r.getContext().setStreamProvider(TEST_PROVIDER);

        r.close();
        try {
            r.bufferEntity();
            fail("IllegalStateException expected when buffering entity on closed response.");
        } catch (IllegalStateException ex) {
            // ok
        }
        r.close(); // second call should pass

        assertFalse("Unbuffered closed response stream entity should not be read.", tis.isRead);
        assertTrue("Closed response stream entity should have been closed.", tis.isClosed);
    }

    /**
     * Test of closing response with empty entity.
     */
    @Test
    public void testCloseEmptyEntityNoStreamProvider() {
        final OutboundJaxrsResponse r = OutboundJaxrsResponse.from(rb.build());
        r.close();
        try {
            r.bufferEntity();
            fail("IllegalStateException expected when buffering entity on closed response.");
        } catch (IllegalStateException ex) {
            // ok
        }
        r.close(); // second call should pass
    }

    /**
     * Test of closing response with non-stream entity.
     */
    @Test
    public void testCloseNonStreamEntityNoStreamProvider() {
        final OutboundJaxrsResponse r = OutboundJaxrsResponse.from(rb.entity(new Object()).build());
        r.close();
        try {
            r.bufferEntity();
            fail("IllegalStateException expected when buffering entity on closed response.");
        } catch (IllegalStateException ex) {
            // ok
        }
        r.close(); // second call should pass
    }

    /**
     * Test of closing response with stream entity.
     */
    @Test
    public void testCloseStreamEntityNoStreamProvider() {
        TestInputStream tis = new TestInputStream();
        final OutboundJaxrsResponse r = OutboundJaxrsResponse.from(rb.entity(tis).build());
        r.close();
        try {
            r.bufferEntity();
            fail("IllegalStateException expected when buffering entity on closed response.");
        } catch (IllegalStateException ex) {
            // ok
        }
        r.close(); // second call should pass

        assertFalse("Unbuffered closed response stream entity should not be read.", tis.isRead);
        assertTrue("Closed response stream entity should have been closed.", tis.isClosed);
    }

}
