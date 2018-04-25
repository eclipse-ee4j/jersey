/*
 * Copyright (c) 2016, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.netty.connector.internal;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import javax.inject.Provider;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.stream.ChunkedInput;

/**
 * Netty {@link ChunkedInput} implementation which also serves as an output
 * stream to Jersey {@link javax.ws.rs.container.ContainerResponseContext}.
 *
 * @author Pavel Bucek (pavel.bucek at oracle.com)
 */
public class JerseyChunkedInput extends OutputStream implements ChunkedInput<ByteBuf>, ChannelFutureListener {

    private static final ByteBuffer VOID = ByteBuffer.allocate(0);
    private static final int CAPACITY = 8;
    // TODO this needs to be configurable, see JERSEY-3228
    private static final int WRITE_TIMEOUT = 10000;
    private static final int READ_TIMEOUT = 10000;

    private final LinkedBlockingDeque<ByteBuffer> queue = new LinkedBlockingDeque<>(CAPACITY);
    private final Channel ctx;
    private final ChannelFuture future;

    private volatile boolean open = true;
    private volatile long offset = 0;

    public JerseyChunkedInput(Channel ctx) {
        this.ctx = ctx;
        this.future = ctx.closeFuture();
        this.future.addListener(this);
    }

    @Override
    public boolean isEndOfInput() throws Exception {
        if (!open) {
            return true;
        }

        ByteBuffer peek = queue.peek();

        if ((peek != null && peek == VOID)) {
            queue.remove(); // VOID from the top.
            open = false;
            removeCloseListener();
            return true;
        }

        return false;
    }

    @Override
    public void operationComplete(ChannelFuture f) throws Exception {
        // forcibly closed connection.
        open = false;
        queue.clear();

        close();
        removeCloseListener();
    }

    private void removeCloseListener() {
        if (future != null) {
            future.removeListener(this);
        }
    }

    @Override
    @Deprecated
    public ByteBuf readChunk(ChannelHandlerContext ctx) throws Exception {
        return readChunk(ctx.alloc());
    }

    @Override
    public ByteBuf readChunk(ByteBufAllocator allocator) throws Exception {

        if (!open) {
            return null;
        }

        ByteBuffer top = queue.poll(READ_TIMEOUT, TimeUnit.MILLISECONDS);

        if (top == null) {
            // returning empty buffer instead of null causes flush (which is needed for BroadcasterTest and others..).
            return Unpooled.EMPTY_BUFFER;
        }

        if (top == VOID) {
            open = false;
            return null;
        }

        int topRemaining = top.remaining();
        ByteBuf buffer = allocator.buffer(topRemaining);

        buffer.setBytes(0, top);
        buffer.setIndex(0, topRemaining);

        if (top.remaining() > 0) {
            queue.addFirst(top);
        }

        offset += topRemaining;

        return buffer;
    }

    @Override
    public long length() {
        return -1;
    }

    @Override
    public long progress() {
        return offset;
    }

    @Override
    public void close() throws IOException {

        if (queue.size() == CAPACITY) {
            boolean offer = false;

            try {
                offer = queue.offer(VOID, WRITE_TIMEOUT, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                // ignore.
            }

            if (!offer) {
                queue.removeLast();
                queue.add(VOID);
            }
        } else {
            queue.add(VOID);
        }

        ctx.flush();
    }

    @Override
    public void write(final int b) throws IOException {

        write(new Provider<ByteBuffer>() {
            @Override
            public ByteBuffer get() {
                return ByteBuffer.wrap(new byte[]{(byte) b});
            }
        });
    }

    @Override
    public void write(final byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    @Override
    public void write(final byte[] b, final int off, final int len) throws IOException {

        final byte[] bytes = new byte[len];
        System.arraycopy(b, off, bytes, 0, len);

        write(new Provider<ByteBuffer>() {
            @Override
            public ByteBuffer get() {
                return ByteBuffer.wrap(bytes);
            }
        });
    }

    @Override
    public void flush() throws IOException {
        ctx.flush();
    }

    private void write(Provider<ByteBuffer> bufferSupplier) throws IOException {

        checkClosed();

        try {
            boolean queued = queue.offer(bufferSupplier.get(), WRITE_TIMEOUT, TimeUnit.MILLISECONDS);
            if (!queued) {
                throw new IOException("Buffer overflow.");
            }

        } catch (InterruptedException e) {
            throw new IOException(e);
        }
    }

    private void checkClosed() throws IOException {
        if (!open) {
            throw new IOException("Stream already closed.");
        }
    }
}
