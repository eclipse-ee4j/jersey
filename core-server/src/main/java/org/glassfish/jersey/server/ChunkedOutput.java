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

package org.glassfish.jersey.server;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import jakarta.ws.rs.container.ConnectionCallback;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.ext.WriterInterceptor;

import jakarta.inject.Provider;

import org.glassfish.jersey.process.internal.RequestContext;
import org.glassfish.jersey.process.internal.RequestScope;
import org.glassfish.jersey.server.internal.LocalizationMessages;
import org.glassfish.jersey.server.internal.process.MappableException;

/**
 * Used for sending messages in "typed" chunks. Useful for long running processes,
 * which needs to produce partial responses.
 *
 * @param <T> chunk type.
 * @author Pavel Bucek
 * @author Martin Matula
 * @author Marek Potociar
 */
// TODO:  something like prequel/sequel - usable for EventChannelWriter and XML related writers
public class ChunkedOutput<T> extends GenericType<T> implements Closeable {
    private static final byte[] ZERO_LENGTH_DELIMITER = new byte[0];

    private final BlockingDeque<T> queue;
    private final byte[] chunkDelimiter;
    private final AtomicBoolean resumed = new AtomicBoolean(false);
    private final Lock lock = new ReentrantLock();

    // the following flushing and touchingEntityStream variables are used in a synchronized block exclusively
    private boolean flushing = false;
    private boolean touchingEntityStream = false;

    private volatile boolean closed = false;

    private volatile AsyncContext asyncContext;

    private volatile RequestScope requestScope;
    private volatile RequestContext requestScopeContext;
    private volatile ContainerRequest requestContext;
    private volatile ContainerResponse responseContext;
    private volatile ConnectionCallback connectionCallback;

    /**
     * Create new {@code ChunkedOutput}.
     */
    protected ChunkedOutput() {
        this.chunkDelimiter = ZERO_LENGTH_DELIMITER;
        queue = new LinkedBlockingDeque<>();
    }

    /**
     * Create new {@code ChunkedOutput} based on builder.
     *
     * @param builder the builder to use
     */
    protected ChunkedOutput(Builder<T> builder) {
        super();
        if (builder.queueCapacity > 0) {
            queue = new LinkedBlockingDeque<>(builder.queueCapacity);
        } else {
            queue = new LinkedBlockingDeque<>();
        }
        if (builder.chunkDelimiter != null) {
            this.chunkDelimiter = new byte[builder.chunkDelimiter.length];
            System.arraycopy(builder.chunkDelimiter, 0, this.chunkDelimiter, 0, builder.chunkDelimiter.length);
        } else {
            this.chunkDelimiter = ZERO_LENGTH_DELIMITER;
        }
        if (builder.asyncContextProvider != null) {
            this.asyncContext = builder.asyncContextProvider.get();
        }
    }

    /**
     * Create new {@code ChunkedOutput} based on builder.
     *
     * @param builder the builder to use
     */
    private ChunkedOutput(TypedBuilder<T> builder) {
        super(builder.chunkType);

        if (builder.queueCapacity > 0) {
            queue = new LinkedBlockingDeque<>(builder.queueCapacity);
        } else {
            queue = new LinkedBlockingDeque<>();
        }
        if (builder.chunkDelimiter != null) {
            this.chunkDelimiter = new byte[builder.chunkDelimiter.length];
            System.arraycopy(builder.chunkDelimiter, 0, this.chunkDelimiter, 0, builder.chunkDelimiter.length);
        } else {
            this.chunkDelimiter = ZERO_LENGTH_DELIMITER;
        }
        if (builder.asyncContextProvider != null) {
            this.asyncContext = builder.asyncContextProvider.get();
        }
    }

    /**
     * Create {@code ChunkedOutput} with specified type.
     *
     * @param chunkType chunk type. Must not be {code null}.
     */
    public ChunkedOutput(final Type chunkType) {
        super(chunkType);
        this.chunkDelimiter = ZERO_LENGTH_DELIMITER;
        queue = new LinkedBlockingDeque<>();
    }

    /**
     * Create new {@code ChunkedOutput} with a custom chunk delimiter.
     *
     * @param chunkDelimiter custom chunk delimiter bytes. Must not be {code null}.
     * @since 2.4.1
     */
    protected ChunkedOutput(final byte[] chunkDelimiter) {
        if (chunkDelimiter.length > 0) {
            this.chunkDelimiter = new byte[chunkDelimiter.length];
            System.arraycopy(chunkDelimiter, 0, this.chunkDelimiter, 0, chunkDelimiter.length);
        } else {
            this.chunkDelimiter = ZERO_LENGTH_DELIMITER;
        }
        queue = new LinkedBlockingDeque<>();
    }

    /**
     * Create new {@code ChunkedOutput} with a custom chunk delimiter.
     *
     * @param chunkDelimiter custom chunk delimiter bytes. Must not be {code null}.
     * @since 2.4.1
     */
    protected ChunkedOutput(final byte[] chunkDelimiter, Provider<AsyncContext> asyncContextProvider) {
        if (chunkDelimiter.length > 0) {
            this.chunkDelimiter = new byte[chunkDelimiter.length];
            System.arraycopy(chunkDelimiter, 0, this.chunkDelimiter, 0, chunkDelimiter.length);
        } else {
            this.chunkDelimiter = ZERO_LENGTH_DELIMITER;
        }

        this.asyncContext = asyncContextProvider == null ? null : asyncContextProvider.get();
        queue = new LinkedBlockingDeque<>();
    }

    /**
     * Create new {@code ChunkedOutput} with a custom chunk delimiter.
     *
     * @param chunkType      chunk type. Must not be {code null}.
     * @param chunkDelimiter custom chunk delimiter bytes. Must not be {code null}.
     * @since 2.4.1
     */
    public ChunkedOutput(final Type chunkType, final byte[] chunkDelimiter) {
        super(chunkType);
        if (chunkDelimiter.length > 0) {
            this.chunkDelimiter = new byte[chunkDelimiter.length];
            System.arraycopy(chunkDelimiter, 0, this.chunkDelimiter, 0, chunkDelimiter.length);
        } else {
            this.chunkDelimiter = ZERO_LENGTH_DELIMITER;
        }
        queue = new LinkedBlockingDeque<>();
    }

    /**
     * Create new {@code ChunkedOutput} with a custom chunk delimiter.
     *
     * @param chunkDelimiter custom chunk delimiter string. Must not be {code null}.
     * @since 2.4.1
     */
    protected ChunkedOutput(final String chunkDelimiter) {
        if (chunkDelimiter.isEmpty()) {
            this.chunkDelimiter = ZERO_LENGTH_DELIMITER;
        } else {
            this.chunkDelimiter = chunkDelimiter.getBytes();
        }
        queue = new LinkedBlockingDeque<>();
    }

    /**
     * Create new {@code ChunkedOutput} with a custom chunk delimiter.
     *
     * @param chunkType      chunk type. Must not be {code null}.
     * @param chunkDelimiter custom chunk delimiter string. Must not be {code null}.
     * @since 2.4.1
     */
    public ChunkedOutput(final Type chunkType, final String chunkDelimiter) {
        super(chunkType);
        if (chunkDelimiter.isEmpty()) {
            this.chunkDelimiter = ZERO_LENGTH_DELIMITER;
        } else {
            this.chunkDelimiter = chunkDelimiter.getBytes();
        }
        queue = new LinkedBlockingDeque<>();
    }

    /**
     * Returns a builder to create a ChunkedOutput with custom configuration.
     *
     * @return builder
     */
    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    /**
     * Returns a builder to create a ChunkedOutput with custom configuration.
     *
     * @param chunkType      chunk type. Must not be {code null}.
     * @return builder
     */
    public static <T> TypedBuilder<T> builder(Type chunkType) {
        return new TypedBuilder<>(chunkType);
    }

    /**
     * Write a chunk.
     *
     * @param chunk a chunk instance to be written.
     * @throws IOException if this response is closed or when encountered any problem during serializing or writing a chunk.
     */
    public void write(final T chunk) throws IOException {
        if (closed) {
            throw new IOException(LocalizationMessages.CHUNKED_OUTPUT_CLOSED());
        }

        if (chunk != null) {
            try {
                queue.put(chunk);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException(e);
            }
        }

        flushQueue();
    }

    protected void flushQueue() throws IOException {
        if (resumed.compareAndSet(false, true) && asyncContext != null) {
            asyncContext.resume(this);
        }

        if (requestScopeContext == null || requestContext == null || responseContext == null) {
            return;
        }

        Exception ex = null;
        try {
            requestScope.runInScope(requestScopeContext, new Callable<Void>() {
                @Override
                public Void call() throws IOException {
                    boolean shouldClose;
                    T t;

                    lock.lock();
                    try {
                        if (flushing) {
                            // if another thread is already flushing the queue, we don't have to do anything
                            return null;
                        }
                        // remember the closed flag before polling the queue
                        // (if we did it after, we could miss the last chunk as some other thread may add a chunk
                        // and set closed to true right after we have polled the queue (i.e. we'd think the queue is empty),
                        // but before we check if we should close - so we would close the stream leaving the last chunk
                        // undelivered)
                        shouldClose = closed;
                        t = queue.poll();
                        if (t != null || shouldClose) {
                            // no other thread is flushing this queue at the moment and it is not empty and/or we should close ->
                            // set the flushing flag so that other threads know it is already being taken care of
                            // and they don't have to bother
                            flushing = true;
                        }
                    } finally {
                        lock.unlock();
                    }

                    while (t != null) {
                        try {
                            lock.lock();
                            touchingEntityStream = true;
                            lock.unlock();

                            final OutputStream origStream = responseContext.getEntityStream();
                            final OutputStream writtenStream = requestContext.getWorkers().writeTo(
                                    t,
                                    t.getClass(),
                                    getType(),
                                    responseContext.getEntityAnnotations(),
                                    responseContext.getMediaType(),
                                    responseContext.getHeaders(),
                                    requestContext.getPropertiesDelegate(),
                                    origStream,
                                    // The output stream stored in the response context for this chunked output
                                    // is already intercepted as a whole (if there are any interceptors);
                                    // no need to intercept the individual chunks.
                                    Collections.<WriterInterceptor>emptyList());

                            //noinspection ArrayEquality
                            if (chunkDelimiter != ZERO_LENGTH_DELIMITER) {
                                // if the chunked output is configured with a custom delimiter, use it
                                writtenStream.write(chunkDelimiter);
                            }

                            // flush the chunk (some writers do it, but some don't)
                            writtenStream.flush();

                            if (origStream != writtenStream) {
                                // if MBW replaced the stream, let's make sure to set it in the response context.
                                responseContext.setEntityStream(writtenStream);
                            }
                        } catch (final IOException | UncheckedIOException ioe) {
                            connectionCallback.onDisconnect(asyncContext);
                            throw ioe;
                        } catch (final MappableException mpe) {
                            if (mpe.getCause() instanceof IOException || mpe.getCause() instanceof UncheckedIOException) {
                                connectionCallback.onDisconnect(asyncContext);
                            }
                            throw mpe;
                        } finally {
                           lock.lock();
                           touchingEntityStream = false;
                           lock.unlock();
                        }

                        t = queue.poll();
                        if (t == null) {
                            lock.lock();
                            try {
                                // queue seems empty
                                // check again in the synchronized block before clearing the flushing flag
                                // first remember the closed flag (this has to be before polling the queue,
                                // otherwise we could miss the last chunk)
                                shouldClose = closed;
                                t = queue.poll();
                                if (t == null) {
                                    // ok, it is really empty - if anyone adds a chunk while we are here,
                                    // other thread will take care of it -> flush the stream and unset
                                    // the flushing flag at the very end (to make sure it is unset only if no
                                    // exception is thrown)
                                    responseContext.commitStream();
                                    // if closing, we keep the "flushing" flag set, since no other thread needs to flush
                                    // this queue anymore - finally clause will take care of closing the stream
                                    flushing = shouldClose;
                                    break;
                                }
                            } finally {
                                lock.unlock();
                            }
                        }
                    }
                    return null;
                }
            });
        } catch (final Exception e) {
            closed = true;
            // remember the exception (it will get rethrown from finally clause, once it does it's work)
            ex = e;
            onClose(e);
        } finally {
            if (closed) {
                lock.lock();
                try {
                    if (!touchingEntityStream) {
                        responseContext.close();
                    } // else the next thread will close responseContext
                } catch (final Exception e) {
                    // if no exception remembered before, remember this one
                    // otherwise the previously remembered exception (from catch clause) takes precedence
                    ex = ex == null ? e : ex;
                } finally {
                    lock.unlock();
                }


                requestScopeContext.release();

                // rethrow remembered exception (if any)
                if (ex instanceof IOException) {
                    //noinspection ThrowFromFinallyBlock
                    throw (IOException) ex;
                } else if (ex instanceof RuntimeException) {
                    //noinspection ThrowFromFinallyBlock
                    throw (RuntimeException) ex;
                }
            }
        }
    }

    /**
     * Close this response - it will be finalized and underlying connections will be closed
     * or made available for another response.
     */
    @Override
    public void close() throws IOException {
        closed = true;
        flushQueue();
    }

    /**
     * Get state information.
     * Please note that {@code ChunkedOutput} can be closed by the client side - client can close connection
     * from its side.
     *
     * @return true when closed, false otherwise.
     */
    public boolean isClosed() {
        return closed;
    }

    /**
     * Executed only in case of close being triggered by client.
     *
     * @param e Exception causing the close
     */
    protected void onClose(Exception e) {
        // drain queue when an exception occurs to prevent deadlocks
        queue.clear();
    }

    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    @Override
    public boolean equals(final Object obj) {
        return this == obj;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + queue.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "ChunkedOutput<" + getType() + ">";
    }

    /**
     * Set context used for writing chunks.
     *
     * @param requestScope             request scope.
     * @param requestScopeContext      current request context instance.
     * @param requestContext           request context.
     * @param responseContext          response context.
     * @param connectionCallbackRunner connection callback.
     * @throws IOException when encountered any problem during serializing or writing a chunk.
     */
    void setContext(final RequestScope requestScope,
                    final RequestContext requestScopeContext,
                    final ContainerRequest requestContext,
                    final ContainerResponse responseContext,
                    final ConnectionCallback connectionCallbackRunner) throws IOException {
        this.requestScope = requestScope;
        this.requestScopeContext = requestScopeContext;
        this.requestContext = requestContext;
        this.responseContext = responseContext;
        this.connectionCallback = connectionCallbackRunner;
        flushQueue();
    }

    /**
     * Builder that allows to create a new ChunkedOutput based on the given configuration options.
     *
     * @param <Y>
     */
    public static class Builder<Y> {
        byte[] chunkDelimiter;
        int queueCapacity = -1;
        Provider<AsyncContext> asyncContextProvider;

        private Builder() {
            // hide constructor
        }

        /**
         * Set the chunk delimiter, in bytes.
         * @param chunkDelimiter the chunk delimiter in bytes
         * @return builder
         */
        public Builder<Y> chunkDelimiter(byte[] chunkDelimiter) {
            this.chunkDelimiter = chunkDelimiter;
            return this;
        }

        /**
         * Set the queue capacity. If greater than 0, the queue is bounded and will block when full.
         * @param queueCapacity the queue capacity
         * @return builder
         */
        public Builder<Y> queueCapacity(int queueCapacity) {
            this.queueCapacity = queueCapacity;
            return this;
        }

        /**
         * Set the async context provider.
         * @param asyncContextProvider the async context provider
         * @return builder
         */
        public Builder<Y> asyncContextProvider(Provider<AsyncContext> asyncContextProvider) {
            this.asyncContextProvider = asyncContextProvider;
            return this;
        }

        /**
         * Build the ChunkedOutput based on the given configuration.
         * @return the ChunkedOutput
         */
        public ChunkedOutput<Y> build() {
            return new ChunkedOutput<>(this);
        }
    }

    /**
     * Builder that allows to create a new ChunkedOutput based on the given configuration options.
     *
     * @param <Y>
     */
    public static class TypedBuilder<Y> extends Builder<Y> {
        private Type chunkType;

        private TypedBuilder(Type chunkType) {
            this.chunkType = chunkType;
        }

        /**
         * Build the ChunkedOutput based on the given configuration.
         * @return the ChunkedOutput
         */
        public ChunkedOutput<Y> build() {
            return new ChunkedOutput<>(this);
        }
    }
}
