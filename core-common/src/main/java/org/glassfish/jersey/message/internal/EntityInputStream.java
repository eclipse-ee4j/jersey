/*
 * Copyright (c) 2012, 2025 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.message.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;

import jakarta.ws.rs.ProcessingException;

import org.glassfish.jersey.innate.io.StreamListener;
import org.glassfish.jersey.innate.io.StreamListenerCouple;
import org.glassfish.jersey.innate.io.InputStreamWrapper;
import org.glassfish.jersey.internal.LocalizationMessages;

/**
 * Entity input stream customized for entity message processing:
 * <ul>
 * <li>contains {@link #isEmpty()} method.</li>
 * <li>{@link #close()} method throws Jersey-specific runtime exception in case of an IO error.</li>
 * </ul>
 *
 * @author Marek Potociar
 */
public class EntityInputStream extends InputStreamWrapper {

    private InputStream input;

    private StreamListener listener;

    private boolean closed = false;

    /**
     * Create an entity input stream instance wrapping the original input stream.
     * <p/>
     * In case the original entity stream is already of type {@code EntityInputStream},
     * the stream is returned without wrapping.
     *
     * @param inputStream input stream.
     * @return entity input stream.
     */
    public static EntityInputStream create(InputStream inputStream) {
        if (inputStream instanceof EntityInputStream) {
            return (EntityInputStream) inputStream;
        }

        return new EntityInputStream(inputStream);
    }

    /**
     * Extension constructor.
     *
     * @param input underlying wrapped input stream.
     */
    public EntityInputStream(InputStream input) {
        this.input = input;
    }


    /**
     * {@inheritDoc}
     * <p>
     * The method is customized to not throw an {@link IOException} if the reset operation fails. Instead,
     * a runtime {@link jakarta.ws.rs.ProcessingException} is thrown.
     * </p>
     *
     * @throws jakarta.ws.rs.ProcessingException in case the reset operation on the underlying entity input stream failed.
     */
    @Override
    public void reset() {
        try {
            input.reset();
        } catch (IOException ex) {
            throw new ProcessingException(LocalizationMessages.MESSAGE_CONTENT_BUFFER_RESET_FAILED(), ex);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * The method is customized to not throw an {@link IOException} if the close operation fails. Instead,
     * a warning message is logged.
     * </p>
     */
    @Override
    public void close() throws ProcessingException {
        if (input == null) {
            return;
        }
        if (!closed) {
            try {
                input.close();
            } catch (IOException ex) {
                // This e.g. means that the underlying socket stream got closed by other thread somehow...
                throw new ProcessingException(LocalizationMessages.MESSAGE_CONTENT_INPUT_STREAM_CLOSE_FAILED(), ex);
            } finally {
                closed = true;
            }
        }
    }

    /**
     * Check if the underlying entity stream is empty.
     * <p>
     * Note that the operation may need to block until a first byte (or EOF) is available in the stream.
     * </p>
     *
     * @return {@code true} if the entity stream is empty, {@code false} otherwise.
     */
    public boolean isEmpty() {
        ensureNotClosed();
        if (input == null) {
            return true;
        }

        try {
            // Try #markSupported first - #available on WLS waits until socked timeout is reached when chunked encoding is used.
            if (input.markSupported()) {
                input.mark(1);
                int i = input.read();
                input.reset();
                return i == -1;
            } else {
                int availableBytes = 0;
                try {
                    availableBytes = input.available();
                } catch (IOException ioe) {
                    // NOOP. Try other approaches as this can fail on WLS.
                }

                if (availableBytes > 0) {
                    return false;
                }

                if (listener != null) {
                    try {
                        if (!listener.isReady()) {
                            return false;
                        }
                        return listener.isEmpty();
                    } catch (IllegalStateException ex) {
                        // NOOP. Listener failed to process the emptiness, the final method to be applied
                    }
                }

                final PushbackInputStream in = (input instanceof PushbackInputStream) ? (PushbackInputStream) input
                       : new PushbackInputStream(input);
                int i = in.read();
                in.unread(i);
                input = in;
                return i == -1;
            }
        } catch (IOException ex) {
            throw new ProcessingException(ex);
        }
    }

    /**
     * Check that the entity input stream has not been closed yet.
     *
     * @throws IllegalStateException in case the entity input stream has been closed.
     */
    public void ensureNotClosed() throws IllegalStateException {
        if (closed) {
            throw new IllegalStateException(LocalizationMessages.ERROR_ENTITY_STREAM_CLOSED());
        }
    }

    /**
     * Get the closed status of this input stream.
     *
     * @return {@code true} if the stream has been closed, {@code false} otherwise.
     */
    public boolean isClosed() {
        return closed;
    }

    /**
     * Get the wrapped input stream instance.
     *
     * @return wrapped input stream instance.
     */
    public final InputStream getWrappedStream() {
        return getWrapped();
    }

    /**
     * Set the wrapped input stream instance.
     *
     * @param wrapped new input stream instance to be wrapped.
     */
    public final void setWrappedStream(InputStream wrapped) {
        input = wrapped;
    }

    @Override
    public InputStream getWrapped() {
        return input;
    }

    /**
     * Decomposes existing {@link EntityInputStream} into this input stream
     * @param stream instance of the {@link EntityInputStream}
     */
    public void wrapExternalStream(StreamListenerCouple stream) {
        input = new InputStreamWrapper() {
            @Override
            public InputStream getWrapped() {
                return stream.getExternalStream();
            }
        };
        listener = stream.getListener();
    }
}
