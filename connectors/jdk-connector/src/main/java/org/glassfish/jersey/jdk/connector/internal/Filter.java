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

import java.net.SocketAddress;

/**
 * A filter can add functionality to JDK client transport. Filters are composed together to
 * create JDK client transport.
 *
 * @author Petr Janouch (petr.janouch at oracle.com)
 * @author Pavel Bucek (pavel.bucek at oracle.com)
 */
class Filter<UP_IN, UP_OUT, DOWN_OUT, DOWN_IN> {

    protected volatile Filter<?, ?, UP_IN, UP_OUT> upstreamFilter = null;
    protected final Filter<DOWN_OUT, DOWN_IN, ?, ?> downstreamFilter;

    /**
     * Constructor.
     *
     * @param downstreamFilter downstream filter. Accessible directly as {@link #downstreamFilter} protected field.
     */
    Filter(Filter<DOWN_OUT, DOWN_IN, ?, ?> downstreamFilter) {
        this.downstreamFilter = downstreamFilter;
    }

    /**
     * Perform write operation for this filter and invokes write method on the next filter in the filter chain.
     *
     * @param data              on which write operation is performed.
     * @param completionHandler will be invoked when the write operation is completed or has failed.
     */
    void write(UP_IN data, CompletionHandler<UP_IN> completionHandler) {
    }

    /**
     * Close the filter, invokes close operation on the next filter in the filter chain.
     * <p/>
     * The filter is expected to clean up any allocated resources and pass the invocation to downstream filter.
     */
    void close() {
        if (downstreamFilter != null) {
            downstreamFilter.close();
        }
    }

    /**
     * Signal to turn on SSL, it is passed on in the filter chain until a filter responsible for SSL is reached.
     */
    void startSsl() {
        if (downstreamFilter != null) {
            downstreamFilter.startSsl();
        }
    }

    /**
     * Initiate connect.
     * <p/>
     * If the {@link Filter} needs to do something during this phase, it must implement {@link
     * #handleConnect(SocketAddress, Filter)}
     * method.
     *
     * @param address        an address where to connect (server or proxy).
     * @param upstreamFilter a filter positioned upstream.
     */
    void connect(SocketAddress address, Filter<?, ?, UP_IN, UP_OUT> upstreamFilter) {
        this.upstreamFilter = upstreamFilter;

        handleConnect(address, upstreamFilter);

        if (downstreamFilter != null) {
            downstreamFilter.connect(address, this);
        }
    }

    /**
     * An event listener that is called when a connection is set up.
     * This event travels up in the filter chain.
     * <p/>
     * If the {@link Filter} needs to process this event, it must implement {@link #processConnect()} method.
     */
    void onConnect() {
        processConnect();

        if (upstreamFilter != null) {
            upstreamFilter.onConnect();
        }
    }

    /**
     * An event listener that is called when some data is read.
     * <p/>
     * If the {@link Filter} needs to process this event, it must implement {@link #onRead(Object)} ()} method.
     * If the method returns {@code true}, the processing will continue with upstream filters; if the method invocation
     * returns {@code false}, the processing won't continue.
     *
     * @param data that has been read.
     */
    @SuppressWarnings("unchecked")
    final void onRead(DOWN_IN data) {
        if (processRead(data)) {
            if (upstreamFilter != null) {
                UP_OUT _data;
                try {
                    _data = (UP_OUT) data;
                } catch (Exception e) {
                    throw new IllegalStateException("Cannot pass message of different type from filter input to filter output");
                }
                upstreamFilter.onRead(_data);
            }
        }
    }

    /**
     * An event listener that is called when the connection is closed by the peer.
     * <p/>
     * If the {@link Filter} needs to process this event, it must implement {@link #processConnectionClosed()} method.
     */
    final void onConnectionClosed() {
        processConnectionClosed();

        if (upstreamFilter != null) {
            upstreamFilter.onConnectionClosed();
        }
    }

    /**
     * An event listener that is called, when SSL completes its handshake.
     * <p/>
     * If the {@link Filter} needs to process this event, it must implement {@link #processSslHandshakeCompleted()} method.
     */
    final void onSslHandshakeCompleted() {
        processSslHandshakeCompleted();

        if (upstreamFilter != null) {
            upstreamFilter.onSslHandshakeCompleted();
        }
    }

    /**
     * An event listener that is called when an error has occurred.
     * <p/>
     * Errors travel in direction from downstream filter to upstream filter.
     * <p/>
     * If the {@link Filter} needs to process this event, it must implement {@link #processError(Throwable)} method.
     *
     * @param t an error that has occurred.
     */
    final void onError(Throwable t) {
        processError(t);

        if (upstreamFilter != null) {
            upstreamFilter.onError(t);
        }
    }

    /**
     * Handle {@link #connect(SocketAddress, Filter)}.
     *
     * @param address        an address where to connect (server or proxy).
     * @param upstreamFilter a filter positioned upstream.
     * @see #connect(SocketAddress, Filter)
     */
    void handleConnect(SocketAddress address, Filter upstreamFilter) {
    }

    /**
     * Process {@link #onConnect()}.
     *
     * @see #onConnect()
     */
    void processConnect() {
    }

    /**
     * Process {@link #onRead(Object)}.
     *
     * @param data read data.
     * @return {@code true} if the data should be sent to processing to upper filter in the chain, {@code false} otherwise.
     * @see #onRead(Object).
     */
    boolean processRead(DOWN_IN data) {
        return true;
    }

    /**
     * Process {@link #onConnectionClosed()}.
     *
     * @see #onConnectionClosed()
     */
    void processConnectionClosed() {
    }

    /**
     * Process {@link #onSslHandshakeCompleted()}.
     *
     * @see #onSslHandshakeCompleted()
     */
    void processSslHandshakeCompleted() {
    }

    /**
     * Process {@link #onError(Throwable)}.
     *
     * @param t an error that has occurred.
     * @see #onError(Throwable)
     */
    void processError(Throwable t) {
    }
}

