/*
 * Copyright (c) 2015, 2019 Oracle and/or its affiliates. All rights reserved.
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
import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;

import org.glassfish.jersey.internal.util.collection.ByteBufferInputStream;

/**
 * TODO Some of the operations added for async. support (e.g.) can be also supported in sync. mode
 * <p/>
 * Body stream that can operate either synchronously or asynchronously. See {@link BodyInputStream} for details.
 *
 * @author Petr Janouch
 */
class AsynchronousBodyInputStream extends BodyInputStream {

    // marker of the end of data stream
    private static final ByteBuffer EOF = ByteBuffer.wrap(new byte[] {});
    // marker of an error in the data stream
    private static final ByteBuffer ERROR = ByteBuffer.wrap(new byte[] {});

    // mode this stream operates in
    private Mode mode = Mode.UNDECIDED;
    private ReadListener readListener = null;
    // read listener is not called always when data become available
    // it must be called for the first time or after isReady returned false
    private boolean callReadListener = false;
    // exception stored until we come to ERROR marker in the input stream
    private Throwable t = null;
    // marker that the stream does not admit more data/errors/stream-end notifications
    private boolean closedForInput = false;
    // by default readListener is invoked on IO/worker threads
    // this might deadlock the entire connector if a blocking operations are used inside the listener implementations
    // the readListener will be invoked using this executor if present
    private ExecutorService listenerExecutor = null;
    // a listener used internally by the connector
    private StateChangeLister stateChangeLister;

    // if in synchronous mode, this stream delegates to synchronousStream
    private ByteBufferInputStream synchronousStream = null;
    // data to be read
    private Deque<ByteBuffer> data = new LinkedList<>();

    synchronized void setListenerExecutor(ExecutorService listenerExecutor) {
        assertAsynchronousOperation();
        this.listenerExecutor = listenerExecutor;
        commitToMode();
    }

    @Override
    public synchronized boolean isReady() {
        assertAsynchronousOperation();

        // return false if this stream has not been initialised
        if (mode == Mode.UNDECIDED) {
            return false;
        }

        ByteBuffer headBuffer = data.peek();
        boolean ready = true;

        if (headBuffer == null) {
            ready = false;
        }

        if (headBuffer == ERROR) {
            ready = false;
            callOnError(t);
        }

        if (headBuffer == EOF) {
            ready = false;
            callOnAllDataRead();
        }

        if (!ready) {
            // returning false automatically enables listener
            callReadListener = true;
        }

        return ready;
    }

    @Override
    public synchronized void setReadListener(ReadListener readListener) {
        if (this.readListener != null) {
            throw new IllegalStateException(LocalizationMessages.READ_LISTENER_SET_ONLY_ONCE());
        }

        // make sure we are not already in synchronous mode
        assertAsynchronousOperation();

        this.readListener = readListener;
        commitToMode();

        // if there is an error or EOF at the head of the data queue, isReady will handle it
        if (isReady()) {
            callDataAvailable();
        }
    }

    @Override
    public int read() throws IOException {
        commitToMode();

        if (mode == Mode.SYNCHRONOUS) {
            return synchronousStream.read();
        }

        validateState();
        return doRead();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        commitToMode();
        if (mode == Mode.SYNCHRONOUS) {
            return synchronousStream.read(b, off, len);
        }

        // some validation borrowed from InputStream
        if (b == null) {
            throw new NullPointerException();
        } else if (off < 0 || len < 0 || len > b.length - off) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return 0;
        }

        validateState();

        for (int i = 0; i < len; i++) {
            if (!hasDataToRead()) {
                return i;
            }

            b[off + i] = doRead();
        }

        // if we are here we were able to fill the entire buffer
        return len;
    }

    private synchronized byte doRead() {
        // if we are here we passed all the validation, so there must be something to read
        ByteBuffer headBuffer = data.peek();
        byte b = headBuffer.get();

        if (!headBuffer.hasRemaining()) {
            // remove empty buffer
            data.poll();
        }

        return b;
    }

    @Override
    public int available() throws IOException {
        commitToMode();
        // TODO this could be also supported in async mode
        assertSynchronousOperation();
        return synchronousStream.available();
    }

    @Override
    public long skip(long n) throws IOException {
        commitToMode();
        // TODO this could be also supported in async mode
        assertSynchronousOperation();
        return synchronousStream.skip(n);
    }

    @Override
    public int tryRead() throws IOException {
        commitToMode();
        assertSynchronousOperation();
        return synchronousStream.tryRead();
    }

    @Override
    public int tryRead(byte[] b) throws IOException {
        commitToMode();
        assertSynchronousOperation();
        return synchronousStream.tryRead(b);
    }

    @Override
    public int tryRead(byte[] b, int off, int len) throws IOException {
        commitToMode();
        assertSynchronousOperation();
        return synchronousStream.tryRead(b, off, len);
    }

    synchronized void notifyDataAvailable(ByteBuffer availableData) {
        assertClosedForInput();

        if (!availableData.hasRemaining()) {
            return;
        }

        if (mode == Mode.SYNCHRONOUS) {
            try {
                synchronousStream.put(availableData);
            } catch (InterruptedException e) {
                synchronousStream.closeQueue(e);
            }
            return;
        }

        data.add(availableData);

        if (readListener != null && callReadListener) {
            callDataAvailable();
        }
    }

    @Override
    public void close() throws IOException {
        if (mode == Mode.SYNCHRONOUS) {
            synchronousStream.close();
        }
    }

    synchronized void notifyError(Throwable t) {
        assertClosedForInput();

        if (stateChangeLister != null) {
            stateChangeLister.onError(t);
        }

        closedForInput = true;

        if (mode == Mode.SYNCHRONOUS) {
            synchronousStream.closeQueue(t);
            return;
        }

        // we store the error and put a marker in the stream, so that the user can read all data that
        // were successfully received up to the error.
        this.t = t;
        data.add(ERROR);

        if (mode == Mode.ASYNCHRONOUS && callReadListener) {
            callOnError(t);
        }
    }

    synchronized void notifyAllDataRead() {
        assertClosedForInput();

        if (stateChangeLister != null) {
            stateChangeLister.onAllDataRead();
        }

        if (mode == Mode.SYNCHRONOUS) {
            synchronousStream.closeQueue();
            return;
        }

        data.add(EOF);

        if (mode == Mode.ASYNCHRONOUS && callReadListener) {
            callOnAllDataRead();
        }
    }

    private synchronized void commitToMode() {
        // return if the mode has already been committed
        if (mode != Mode.UNDECIDED) {
            return;
        }

        // go asynchronous, if the user has made any move suggesting asynchronous mode
        if (readListener != null || listenerExecutor != null) {
            mode = Mode.ASYNCHRONOUS;
            return;
        }

        // go synchronous, if the user has not made any move suggesting asynchronous mode
        mode = Mode.SYNCHRONOUS;
        synchronousStream = new ByteBufferInputStream();
        // move all buffered data to synchronous stream
        for (ByteBuffer b : data) {
            if (b == EOF) {
                synchronousStream.closeQueue();
            } else if (b == ERROR) {
                synchronousStream.closeQueue(t);
            } else {
                try {
                    synchronousStream.put(b);
                } catch (InterruptedException e) {
                    synchronousStream.closeQueue(e);
                }
            }
        }
    }

    private void assertAsynchronousOperation() {
        if (mode == Mode.SYNCHRONOUS) {
            throw new UnsupportedOperationException(LocalizationMessages.ASYNC_OPERATION_NOT_SUPPORTED());
        }
    }

    private void assertSynchronousOperation() {
        if (mode == Mode.ASYNCHRONOUS) {
            throw new UnsupportedOperationException(LocalizationMessages.SYNC_OPERATION_NOT_SUPPORTED());
        }
    }

    private void validateState() {
        if (mode == Mode.ASYNCHRONOUS && !hasDataToRead()) {
            throw new IllegalStateException(LocalizationMessages.WRITE_WHEN_NOT_READY());
        }
    }

    private void assertClosedForInput() {
        if (closedForInput) {
            throw new IllegalStateException(LocalizationMessages.STREAM_CLOSED_FOR_INPUT());
        }
    }

    private boolean hasDataToRead() {
        ByteBuffer headBuffer = data.peek();
        if (headBuffer == null || headBuffer == EOF || headBuffer == ERROR || !headBuffer.hasRemaining()) {
            return false;
        }

        return true;
    }

    private void callDataAvailable() {
        callReadListener = false;
        if (listenerExecutor == null) {

            try {
                readListener.onDataAvailable();
            } catch (IOException e) {
                readListener.onError(e);
            }
        } else {
            listenerExecutor.submit(() -> {
                try {
                    readListener.onDataAvailable();
                } catch (IOException e) {
                    readListener.onError(e);
                }
            });
        }
    }

    private void callOnError(final Throwable t) {
        if (listenerExecutor == null) {
            readListener.onError(t);
        } else {
            listenerExecutor.submit(() -> readListener.onError(t));
        }
    }

    private void callOnAllDataRead() {
        if (listenerExecutor == null) {
            try {
                readListener.onAllDataRead();
            } catch (IOException e) {
                readListener.onError(e);
            }
        } else {
            listenerExecutor.submit(() -> {
                try {
                    readListener.onAllDataRead();
                } catch (IOException e) {
                    readListener.onError(e);
                }
            });
        }
    }

    synchronized void setStateChangeLister(StateChangeLister stateChangeLister) {
        this.stateChangeLister = stateChangeLister;

        if (!data.isEmpty() && data.getLast() == EOF) {
            stateChangeLister.onAllDataRead();
        }

        if (!data.isEmpty() && data.getLast() == ERROR) {
            stateChangeLister.onError(t);
        }
    }

    private enum Mode {
        SYNCHRONOUS,
        ASYNCHRONOUS,
        UNDECIDED
    }

    /**
     * Internal listener, so that the connection pool knows when the body has been read,
     * so it can reuse/close the connection.
     */
    interface StateChangeLister {

        void onError(Throwable t);

        void onAllDataRead();
    }
}
