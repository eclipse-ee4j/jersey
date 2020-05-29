/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.helidon.connector;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ReadableByteChannel;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

class OutputStreamChannel extends OutputStream implements ReadableByteChannel {

    private ReentrantLock lock = new ReentrantLock();
    private static final ByteBuffer VOID = ByteBuffer.allocate(0);
    private static final int CAPACITY = Integer.getInteger("jersey.helidon.connector.osc.capacity", 8);
    private static final int WRITE_TIMEOUT = Integer.getInteger("jersey.helidon.connector.osc.read.timeout", 10000);
    private static final int READ_TIMEOUT = Integer.getInteger("jersey.helidon.connector.osc.write.timeout", 10000);
    private final int bufferSize;

    OutputStreamChannel(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    private final LinkedBlockingDeque<ByteBuffer> queue = new LinkedBlockingDeque<>(CAPACITY);

    private volatile boolean open = true;
    private ByteBuffer remainingByteBuffer;

    @Override
    public int read(ByteBuffer dst) throws IOException {
        if (!open) {
            throw new ClosedChannelException();
        }

        int sum = 0;

        do {
            ByteBuffer top;
            try {
                top = poll(READ_TIMEOUT, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                open = false;
                throw new ClosedByInterruptException();
            }

            if (top == null) {
                return sum;
            }

            if (top == VOID) {
                if (sum == 0) {
                    open = false;
                    return -1;
                } else {
                    queue.addFirst(top);
                    return sum;
                }
            }

            final int topSize = top.remaining();
            final int dstAvailable = dst.remaining();
            final int minSize = Math.min(topSize, dstAvailable);

            if (top.hasArray()) {
                dst.put(top.array(), top.arrayOffset() + top.position(), minSize);
                top.position(top.position() + minSize);
            } else {
                while (dst.hasRemaining() && top.hasRemaining()) {
                    dst.put(top.get());
                }
            }

            sum += minSize;

            if (top.hasRemaining()) {
                remainingByteBuffer = top;
            }
        } while (dst.hasRemaining());

        return sum;
    }

    private ByteBuffer poll(long timeout, TimeUnit unit) throws InterruptedException {
        if (remainingByteBuffer != null) {
            final ByteBuffer remaining = remainingByteBuffer;
            remainingByteBuffer = null;
            return remaining;
        } else {
            // do not modify head
            lock.lock();
            final ByteBuffer peek = queue.poll(timeout, unit);
            // can modify head
            lock.unlock();
            return peek;
        }
    }

    @Override
    public void write(int b) throws IOException {
        write(new byte[]{(byte) b}, 0, 1);
    }

    @Override
    public void write(byte[] b) throws IOException {
        super.write(b, 0, b.length);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        checkClosed();

        if (lock.tryLock()) {
            if (len < bufferSize && queue.size() > 0) {
                final ByteBuffer buffer = queue.getLast();
                if (buffer != null && (buffer.capacity() - buffer.limit()) > len) {
                    //set for write
                    buffer.position(buffer.limit());
                    buffer.limit(buffer.capacity());
                    buffer.put(b, off, len);
                    //set for read
                    buffer.flip();
                    lock.unlock();
                    return;
                }
            }
            lock.unlock();
        }

        final int maxLen = Math.max(len, bufferSize);
        final byte[] bytes = new byte[maxLen];
        System.arraycopy(b, off, bytes, 0, len);

        final ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.limit(len);
        buffer.position(0);

        write(buffer);
    }

    private void write(ByteBuffer buffer) throws IOException {
        try {
            boolean queued = queue.offer(buffer, WRITE_TIMEOUT, TimeUnit.MILLISECONDS);
            if (!queued) {
                throw new IOException("Buffer overflow.");
            }

        } catch (InterruptedException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void close() throws IOException {
        boolean offer = false;

        try {
            offer = queue.offer(VOID, WRITE_TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            // ignore.
        }

        if (!offer) {
            lock.lock();
            queue.removeLast();
            queue.add(VOID);
            lock.unlock();
        }
    }


    @Override
    public boolean isOpen() {
        return open;
    }

    private void checkClosed() throws IOException {
        if (!open) {
            throw new IOException("Stream already closed.");
        }
    }
}
