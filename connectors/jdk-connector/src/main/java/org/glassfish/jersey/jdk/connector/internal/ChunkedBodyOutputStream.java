/*
 * Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.jdk.connector.internal;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;


/**
 * Body stream that can operate either synchronously or asynchronously. See {@link BodyOutputStream} for details.
 *
 * @author Petr Janouch (petr.janouch at oracle.com)
 */
class ChunkedBodyOutputStream extends BodyOutputStream {

    private static final ByteBuffer EMPTY_BUFFER = ByteBuffer.allocate(0);

    private final int chunkSize;
    private final int encodedFullChunkSize;

    // this stream is buffering by default; it has pending data up to dataBuffer.capacity()
    private final ByteBuffer dataBuffer;

    // in sync. mode, the write operations will block until the stream is opened for data
    private final CountDownLatch initialBlockingLatch = new CountDownLatch(1);

    private volatile Filter<ByteBuffer, ?, ?, ?> downstreamFilter;
    private volatile WriteListener writeListener = null;
    // an internal listener, so the connector can be notified when the stream has been closed (=body has been sent)
    private volatile Listener closeListener;
    // mode this stream operates in
    private volatile Mode mode = Mode.UNDECIDED;
    private volatile boolean ready = false;
    // flag to make sure that a listener is called only for the first time or after isReady() returned false
    private volatile boolean callListener = true;

    private volatile boolean closed = false;

    ChunkedBodyOutputStream(int chunkSize) {
        this.chunkSize = chunkSize;
        this.dataBuffer = ByteBuffer.allocate(chunkSize);
        this.encodedFullChunkSize = HttpRequestEncoder.getChunkSize(chunkSize);
    }

    @Override
    public synchronized void setWriteListener(WriteListener writeListener) {
        if (this.writeListener != null) {
            throw new IllegalStateException(LocalizationMessages.WRITE_LISTENER_SET_ONLY_ONCE());
        }

        assertAsynchronousOperation();
        this.writeListener = writeListener;
        commitToMode();

        if (ready && callListener) {
            callOnWritePossible();
        }
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        commitToMode();

        // input validation borrowed from OutputStream
        if (b == null) {
            throw new NullPointerException();
        } else if ((off < 0) || (off > b.length) || (len < 0)
                || ((off + len) > b.length) || ((off + len) < 0)) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return;
        }

        assertValidState();
        doInitialBlocking();

        if (len < dataBuffer.remaining()) {
            // if the data fit into the buffer, use write per byte
            for (int i = off; i < off + len; i++) {
                write(b[i]);
            }
        } else {
            // if the data overflow the buffer, send a multiple of the buffer size and buffer the remainder
            int currentDataLength = dataBuffer.position() + len;
            int remainder = currentDataLength % dataBuffer.capacity();
            // buffer that will be send
            ByteBuffer buffer = ByteBuffer.allocate(currentDataLength - remainder);
            dataBuffer.flip();
            // put currently buffered data
            buffer.put(dataBuffer);
            // fill the rest with passed data
            buffer.put(b, off, len - remainder);
            buffer.flip();
            dataBuffer.clear();
            // buffer remaining data
            dataBuffer.put(b, off + len - remainder, remainder);
            // send the to-be-written buffer
            write(buffer);
        }
    }

    @Override
    public void flush() throws IOException {
        super.flush();
        if (mode == Mode.UNDECIDED) {
            // if we are not committed to any mode, any of the write operations has not been invoked yet
            return;
        }

        if (mode == Mode.ASYNCHRONOUS) {
            assertValidState();
        }

        if (dataBuffer.position() == 0) {
            // there is nothing buffered, so don't bother
            return;
        }

        dataBuffer.flip();
        write(dataBuffer);
    }

    @Override
    public void write(int b) throws IOException {
        commitToMode();
        assertValidState();
        doInitialBlocking();

        dataBuffer.put((byte) b);
        if (!dataBuffer.hasRemaining()) {
            // send the buffer if we have just filled it.
            dataBuffer.flip();
            write(dataBuffer);
        }
    }

    @Override
    public boolean isReady() {
        // TODO we might support this in synchronous mode too
        assertAsynchronousOperation();

        if (!ready) {
            callListener = true;
        }

        return ready;
    }

    private void assertValidState() {
        if (closed) {
            throw new IllegalStateException(LocalizationMessages.STREAM_CLOSED());
        }

        if (mode == Mode.ASYNCHRONOUS && !ready) {
            // we are in asynchronous mode, but the user called write when the stream in non-ready state
            throw new IllegalStateException(LocalizationMessages.WRITE_WHEN_NOT_READY());
        }
    }

    protected void write(final ByteBuffer byteBuffer) throws IOException {
        // do transport encoding on the raw data
        ByteBuffer httpChunk = encodeToHttp(byteBuffer);

        if (mode == Mode.SYNCHRONOUS) {
            final CountDownLatch writeLatch = new CountDownLatch(1);
            final AtomicReference<Throwable> error = new AtomicReference<>();
            downstreamFilter.write(httpChunk, new CompletionHandler<ByteBuffer>() {
                @Override
                public void completed(ByteBuffer result) {
                    writeLatch.countDown();
                }

                @Override
                public void failed(Throwable t) {
                    error.set(t);
                    writeLatch.countDown();
                }
            });

            try {
                // block until the operation has completed
                writeLatch.await();
            } catch (InterruptedException e) {
                throw new IOException(LocalizationMessages.WRITING_FAILED(), e);
            }

            byteBuffer.clear();

            Throwable t = error.get();
            // check fo any errors
            if (t != null) {
                throw new IOException(LocalizationMessages.WRITING_FAILED(), t);
            }
        } else {
            ready = false;
            downstreamFilter.write(httpChunk, new CompletionHandler<ByteBuffer>() {

                @Override
                public void completed(ByteBuffer result) {
                    ready = true;
                    byteBuffer.clear();
                    if (callListener) {
                        callOnWritePossible();
                    }
                }

                @Override
                public void failed(Throwable throwable) {
                    ready = false;
                    writeListener.onError(throwable);
                }
            });
        }
    }

    synchronized void open(Filter<ByteBuffer, ?, ?, ?> downstreamFilter) {
        this.downstreamFilter = downstreamFilter;
        initialBlockingLatch.countDown();
        ready = true;

        if (mode == Mode.ASYNCHRONOUS && writeListener != null) {
            callOnWritePossible();
        }
    }

    protected void doInitialBlocking() throws IOException {
        if (mode != Mode.SYNCHRONOUS || downstreamFilter != null) {
            return;
        }

        try {
            initialBlockingLatch.await();
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
    }

    protected synchronized void commitToMode() {
        // return if the mode has already been committed
        if (mode != Mode.UNDECIDED) {
            return;
        }

        // go asynchronous, if the user has made any move suggesting asynchronous mode
        if (writeListener != null) {
            mode = Mode.ASYNCHRONOUS;
            return;
        }

        // go synchronous, if the user has not made any suggesting asynchronous mode
        mode = Mode.SYNCHRONOUS;
    }

    private void assertAsynchronousOperation() {
        if (mode == Mode.SYNCHRONOUS) {
            throw new UnsupportedOperationException(LocalizationMessages.ASYNC_OPERATION_NOT_SUPPORTED());
        }
    }

    private void callOnWritePossible() {
        callListener = false;
        try {
            writeListener.onWritePossible();
        } catch (IOException e) {
            writeListener.onError(e);
        }
    }

    /**
     * Set a close listener which will be called when the user closes the stream.
     * <p/>
     * This is used to indicate that the body has been completely written.
     *
     * @param closeListener close listener.
     */
    synchronized void setCloseListener(Listener closeListener) {
        this.closeListener = closeListener;
    }

    /**
     * Transform raw application data into HTTP body.
     *
     * @param byteBuffer application data.
     * @return http body part.
     */
    protected ByteBuffer encodeToHttp(ByteBuffer byteBuffer) {
        // we expect the size of the buffer to be either a multiple of chunkSize
        // or smaller than chunkSize in case of the last content-carrying chunk and closing chunk (the one sent by close())
        if (byteBuffer.remaining() < chunkSize) {
            return HttpRequestEncoder.encodeChunk(byteBuffer);
        }

        if (byteBuffer.remaining() % chunkSize != 0) {
            // the buffer is neither a multiple of chunkSize nor smaller than chunkSize
            throw new IllegalStateException(LocalizationMessages.BUFFER_INCORRECT_LENGTH());
        }

        int numberOfChunks = byteBuffer.remaining() / chunkSize;
        ByteBuffer encodedChunks = ByteBuffer.allocate(numberOfChunks * encodedFullChunkSize);

        for (int i = 0; i < numberOfChunks; i++) {
            byteBuffer.position(i * chunkSize);
            byteBuffer.limit(i * chunkSize + chunkSize);
            ByteBuffer encodeChunk = HttpRequestEncoder.encodeChunk(byteBuffer);
            encodedChunks.put(encodeChunk);
        }

        encodedChunks.flip();
        return encodedChunks;
    }

    @Override
    public void close() throws IOException {
        if (closed) {
            return;
        }

            commitToMode();
            // just in case close is invoked without any data being written
            doInitialBlocking();
            flush();
            // chunk-encoded message is finished with an empty chunk
            write(EMPTY_BUFFER);
            super.close();

        closed = true;
        synchronized (this) {
            if (closeListener != null) {
                closeListener.onClosed();
            }
        }
    }

    /**
     * Set a close listener which will be called when the user closes the stream.
     * <p/>
     * This is used to indicate that the body has been completely written.
     */
    interface Listener {

        void onClosed();
    }

    private enum Mode {
        SYNCHRONOUS,
        ASYNCHRONOUS,
        UNDECIDED
    }
}
