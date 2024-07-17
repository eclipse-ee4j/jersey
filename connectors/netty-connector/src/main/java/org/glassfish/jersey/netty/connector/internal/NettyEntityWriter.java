/*
 * Copyright (c) 2023, 2024 Oracle and/or its affiliates. All rights reserved.
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

import io.netty.channel.Channel;
import io.netty.handler.stream.ChunkedInput;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.ClientRequest;
import org.glassfish.jersey.client.RequestEntityProcessing;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The Entity Writer is used to write entity in Netty. One implementation is delayed,
 * so that the complete message length can be set to Content-Length header.
 */
public interface NettyEntityWriter {

    /**
     * Type of the entity writer. {@code CHUNKED} is used for chunked data. {@code PRESET} is for buffered data, but the
     * content length was pre-set by the customer. {@code DELAYED} is for buffered data where the content-length is unknown.
     * The headers must not be written before the entity is provided by MessageBodyWriter to know the exact length.
     */
    enum Type {
        CHUNKED,
        PRESET,
        DELAYED
    }

    /**
     * Writes the Object to the channel
     * @param object object to be written
     */
    void write(Object object);

    /**
     * Writes the Object to the channel and flush.
     * @param object object to be written
     */
    void writeAndFlush(Object object);

    /**
     * Flushes the writen objects. Can throw IOException.
     * @throws IOException
     */
    void flush() throws IOException;

    /**
     * Get the netty Chunked Input to be written.
     * @return The Chunked input instance
     */
    ChunkedInput getChunkedInput();

    /**
     * Get the {@link OutputStream} used to write an entity
     * @return the OutputStream to write an entity
     */
    OutputStream getOutputStream();

    /**
     * Get the length of the entity written to the {@link OutputStream}
     * @return
     */
    long getLength();

    /**
     * Return Type of
     * @return
     */
    Type getType();

    static NettyEntityWriter getInstance(ClientRequest clientRequest, Channel channel) {
        final long lengthLong = clientRequest.getLengthLong();
        final RequestEntityProcessing entityProcessing = clientRequest.resolveProperty(
                ClientProperties.REQUEST_ENTITY_PROCESSING, RequestEntityProcessing.class);

        if ((entityProcessing == null && lengthLong == -1) || entityProcessing == RequestEntityProcessing.CHUNKED) {
            return new DirectEntityWriter(channel, Type.CHUNKED);
        } else if (lengthLong != -1) {
            return new DirectEntityWriter(channel, Type.PRESET);
        } else {
            return new DelayedEntityWriter(channel, Type.DELAYED);
        }
    }

    class DirectEntityWriter implements NettyEntityWriter {
        private final Channel channel;
        private final JerseyChunkedInput stream;
        private final Type type;

        public DirectEntityWriter(Channel channel, Type type) {
            this.channel = channel;
            stream = new JerseyChunkedInput(channel);
            this.type = type;
        }

        @Override
        public void write(Object object) {
            channel.write(object);
        }

        @Override
        public void writeAndFlush(Object object) {
            channel.writeAndFlush(object);
        }

        @Override
        public void flush() {
            channel.flush();
        }

        @Override
        public ChunkedInput getChunkedInput() {
            return stream;
        }

        @Override
        public OutputStream getOutputStream() {
            return stream;
        }

        @Override
        public long getLength() {
            return stream.progress();
        }

        @Override
        public Type getType() {
            return type;
        }
    }

    class DelayedEntityWriter implements NettyEntityWriter {
        private final List<Runnable> delayedOps;
        private final DirectEntityWriter writer;
        private final DelayedOutputStream outputStream;

        private boolean flushed = false;
        private boolean closed = false;

        public DelayedEntityWriter(Channel channel, Type type) {
            this.writer = new DirectEntityWriter(channel, type);
            this.delayedOps = new LinkedList<>();
            this.outputStream = new DelayedOutputStream();
        }


        @Override
        public void write(Object object) {
            if (!flushed) {
                delayedOps.add(() -> writer.write(object));
            } else {
                writer.write(object);
            }
        }

        @Override
        public void writeAndFlush(Object object) {
            if (!flushed) {
                delayedOps.add(() -> writer.writeAndFlush(object));
            } else {
                writer.writeAndFlush(object);
            }
        }

        @Override
        public void flush() throws IOException {
            _flush();
            if (!closed) {
                closed = true;
                writer.getOutputStream().close(); // Jersey automatically closes DelayedOutputStream not this one!
            }
            writer.flush();
        }

        private void _flush() throws IOException {
            if (!flushed) {
                flushed = true;
                for (Runnable runnable : delayedOps) {
                    runnable.run();
                }
                outputStream._flush();
            }
        }

        @Override
        public ChunkedInput getChunkedInput() {
            return writer.getChunkedInput();
        }

        @Override
        public OutputStream getOutputStream() {
            return outputStream;
        }


        @Override
        public long getLength() {
            return outputStream.writeLen;
        }

        @Override
        public Type getType() {
            return writer.getType();
        }

        private class DelayedOutputStream extends OutputStream {
            private final List<WriteAction> actions = new ArrayList<>();
            private int writeLen = 0;
            private AtomicBoolean streamFlushed = new AtomicBoolean(false);

            @Override
            public void write(int b) throws IOException {
                write(new byte[]{(byte) (b & 0xFF)}, 0, 1);
            }

            @Override
            public void write(byte[] b) throws IOException {
                write(b, 0, b.length);
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                if (!flushed) {
                    actions.add(new WriteAction(b, off, len));
                    writeLen += len;
                } else {
                    _flush();
                    writer.getOutputStream().write(b, off, len);
                    writer.getOutputStream().flush();
                }
            }

            public void _flush() throws IOException {
                if (streamFlushed.compareAndSet(false, true)) {
                    DelayedEntityWriter.this._flush();
                    for (WriteAction action : actions) {
                        action.run();
                    }
                    actions.clear();
                }
            }
        }

        private class WriteAction {
            private final byte[] b;

            private WriteAction(byte[] b, int off, int len) {
                this.b = new byte[len]; // b passed in can be reused
                System.arraycopy(b, off, this.b, 0, len);
            }

            public void run() throws IOException {
                writer.getOutputStream().write(b, 0, b.length);
                writer.getOutputStream().flush();
            }
        }
    }
}
