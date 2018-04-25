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

package org.glassfish.jersey.internal.util.collection;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.glassfish.jersey.internal.LocalizationMessages;

/**
 * An {@link java.io.InputStream} backed by a queue of {@link java.nio.ByteBuffer byte buffers}
 * to be read.
 * <p>
 * This input stream serves as a bridging inbound I/O component between a blocking upper I/O layer
 * and an underlying non-blocking I/O layer. In addition to the blocking {@code InputStream.read} operations,
 * this input stream provides the non-blocking {@code tryRead} counterparts.
 * </p>
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public final class ByteBufferInputStream extends NonBlockingInputStream {

    /**
     * Constant buffer indicating EOF.
     */
    private static final ByteBuffer EOF = ByteBuffer.wrap(new byte[] {});

    /**
     * Read-side EOF flag. Does not have to be volatile, it is transient and only accessed from the reader thread.
     */
    private boolean eof = false;
    /**
     * Currently read byte buffer.
     */
    private ByteBuffer current;
    /**
     * Queue of byte buffers to be read.
     */
    private final BlockingQueue<ByteBuffer> buffers;
    /**
     * Content represents the buffer queue status.
     *
     * null = open, POISON_PILL = closed, Throwable = closed with error
     */
    private final AtomicReference<Object> queueStatus = new AtomicReference<Object>(null);
    /**
     * Closed flag.
     */
    private final AtomicBoolean closed = new AtomicBoolean(false);

    /**
     * Create a new input stream that is backed by a a queue of {@link java.nio.ByteBuffer byte buffers}
     * to be read.
     */
    public ByteBufferInputStream() {
        this.buffers = DataStructures.createLinkedTransferQueue();
        this.current = null;
    }

    private boolean fetchChunk(final boolean block) throws InterruptedException {
        if (eof) {
            return false;
        }

        // Read until no buffers available (poll returned null)
        // or until a non-empty buffer or EOF is reached.
        do {
            if (closed.get()) {
                current = EOF;
                break;
            }

            current = (block) ? buffers.take() : buffers.poll();
        } while (current != null && current != EOF && !current.hasRemaining());

        eof = current == EOF;
        return !eof;
    }

    private void checkNotClosed() throws IOException {
        if (closed.get()) {
            throw new IOException(LocalizationMessages.INPUT_STREAM_CLOSED());
        }
    }

    private void checkThrowable() throws IOException {
        final Object o = queueStatus.get();
        if (o != null && o != EOF) { // should be faster than instanceof
            // if not null or EOF, then it must be Throwable
            if (queueStatus.compareAndSet(o, EOF)) {
                // clear throwable flag and throw exception
                try {
                    throw new IOException((Throwable) o);
                } finally {
                    close();
                }
            }
        }
    }

    @Override
    public int available() throws IOException {
        if (eof || closed.get()) {
            checkThrowable();
            return 0;
        }

        int available = 0;
        if (current != null && current.hasRemaining()) {
            available = current.remaining();
        }
        for (final ByteBuffer buffer : buffers) {
            if (buffer == EOF) {
                break;
            }
            available += buffer.remaining();
        }

        checkThrowable();
        return closed.get() ? 0 : available;
    }

    @Override
    public int read() throws IOException {
        return tryRead(true);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return tryRead(b, off, len, true);
    }

    public int tryRead() throws IOException {
        return tryRead(false);
    }

    @Override
    public int tryRead(final byte[] b) throws IOException {
        return tryRead(b, 0, b.length);
    }

    @Override
    public int tryRead(final byte[] b, final int off, final int len) throws IOException {
        return tryRead(b, off, len, false);
    }

    @Override
    public void close() throws IOException {
        if (closed.compareAndSet(false, true)) {
            closeQueue();
            // we can now safely clear the queue - any blocking read waiting for a buffer
            // has been resumed by the EOF buffer
            buffers.clear();
        }
        checkThrowable();
    }

    /**
     * Put the {@code ByteBuffer} to the internal queue to be available for reading from the stream.
     *
     * <p>
     * If the sink is open, the method {@link BlockingQueue#put(Object) puts the buffer} into an internal
     * byte buffer read queue , waiting if necessary for space to become available. Then the method returns
     * {@code true} to indicate the buffer has been successfully queued. In case the internal read queue has been
     * {@link #closeQueue() closed} already, the method simply returns {@code false} without registering
     * the buffer in the closed queue.
     * </p>
     *
     * @param src the source buffer to be registered in the byte buffer read queue.
     * @return {@code true} if the byte buffer has been successfully put in the read queue,
     *         {@code false} if the read queue has been closed.
     * @throws InterruptedException in case the put operation has been interrupted.
     */
    public boolean put(final ByteBuffer src) throws InterruptedException {
        if (queueStatus.get() == null) {
            buffers.put(src);
            return true;
        }
        return false;
    }

    /**
     * Closes the byte buffer sink of this input stream to indicate that writing to the stream
     * has been finished.
     * <p>
     * If the sink has already been closed then this method returns immediately. Otherwise the
     * sink is marked as closed and no more data can be written to it.
     * </p>
     */
    public void closeQueue() {
        if (queueStatus.compareAndSet(null, EOF)) {
            try {
                buffers.put(EOF);
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Closes the byte buffer sink of this input stream to indicate that writing to the stream
     * has been finished due to a throwable.
     *
     * The throwable set by this method will be propagated to the reader thread when a new attempt
     * to read bytes is made.
     * <p>
     * If the sink has already been closed then this method only sets the throwable in the stream and
     * then returns immediately. Otherwise the sink is also marked as closed and no more data can be
     * written to it.
     * </p>
     *
     * @param throwable throwable that is set in the stream. It will be thrown by the stream in case
     *                  an attempt to read more data or check available bytes is made.
     */
    public void closeQueue(final Throwable throwable) {
        if (queueStatus.compareAndSet(null, throwable)) {
            try {
                buffers.put(EOF);
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private int tryRead(final byte[] b, final int off, final int len, boolean block) throws IOException {
        checkThrowable();
        checkNotClosed();

        if (b == null) {
            throw new NullPointerException();
        } else if (off < 0 || len < 0 || len > b.length - off) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return 0;
        }

        if (eof) {
            return -1;
        }

        int i = 0;
        while (i < len) {
            if (current != null && current.hasRemaining()) {
                final int available = current.remaining();
                if (available < len - i) {
                    current.get(b, off + i, available);
                    i += available;
                } else {
                    current.get(b, off + i, len - i);
                    return len;
                }
            } else {
                try {
                    if (!fetchChunk(block) || current == null) {
                        break;  // eof or no data
                    }
                } catch (final InterruptedException e) {
                    Thread.currentThread().interrupt();
                    if (block) {
                        throw new IOException(e);
                    }
                }
            }
        }

        return i == 0 && eof ? -1 : i;
    }

    private int tryRead(boolean block) throws IOException {
        checkThrowable();
        checkNotClosed();

        if (eof) {
            return -1;
        }

        if (current != null && current.hasRemaining()) {
            return current.get() & 0xFF;
        }

        try {
            // try to fetch, but don't block && check if something has been fetched
            if (fetchChunk(block) && current != null) {
                return current.get() & 0xFF;
            } else if (block) {
                return -1;
            }
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            if (block) {
                throw new IOException(e);
            }
        }

        return (eof) ? -1 : NOTHING;
    }
}
