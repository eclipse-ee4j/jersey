/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.media.sse.internal;

import java.io.Flushable;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.sse.OutboundSseEvent;
import javax.ws.rs.sse.SseEventSink;

import javax.inject.Provider;

import org.glassfish.jersey.internal.jsr166.Flow;
import org.glassfish.jersey.media.sse.LocalizationMessages;
import org.glassfish.jersey.server.AsyncContext;
import org.glassfish.jersey.server.ChunkedOutput;

/**
 * Server-side SSE subscriber.
 * <p>
 * The reference should be obtained via injection into the resource method.
 *
 * @author Adam Lindenthal (adam.lindenthal at oracle.com)]
 */
class JerseyEventSink extends ChunkedOutput<OutboundSseEvent>
        implements SseEventSink, Flushable, Flow.Subscriber<OutboundSseEvent> {

    private static final Logger LOGGER = Logger.getLogger(JerseyEventSink.class.getName());
    private static final byte[] SSE_EVENT_DELIMITER = "\n".getBytes(Charset.forName("UTF-8"));
    private Flow.Subscription subscription = null;

    JerseyEventSink(Provider<AsyncContext> asyncContextProvider) {
        super(SSE_EVENT_DELIMITER, asyncContextProvider);
    }

    @Override
    public void onSubscribe(final Flow.Subscription subscription) {
        checkClosed();
        if (subscription == null) {
            throw new NullPointerException(LocalizationMessages.PARAM_NULL("subscription"));
        }
        this.subscription = subscription;
        subscription.request(Long.MAX_VALUE);
    }


    @Override
    public void onNext(final OutboundSseEvent item) {
        checkClosed();
        if (item == null) {
            throw new NullPointerException(LocalizationMessages.PARAM_NULL("outboundSseEvent"));
        }
        try {
            write(item);
        } catch (final IOException e) {
            onError(e);
        }
    }

    @Override
    public void onError(final Throwable throwable) {
        checkClosed();
        if (throwable == null) {
            throw new NullPointerException(LocalizationMessages.PARAM_NULL("throwable"));
        }
        subscription.cancel();
    }

    @Override
    public void close() {
        try {
            super.close();
        } catch (IOException e) {
            LOGGER.log(Level.INFO, LocalizationMessages.EVENT_SINK_CLOSE_FAILED(), e);
        }
    }

    @Override
    public CompletionStage<?> send(OutboundSseEvent event) {
        checkClosed();
        try {
            this.write(event);
            return CompletableFuture.completedFuture(null);
        } catch (IOException e) {
            return CompletableFuture.completedFuture(e);
        }
    }

    /**
     * Flush the headers.
     *
     * When SseEventSink instance is returned from the resource method and there are no event written,
     * http headers need to be "flushed" - sent to the client, which is waiting for response headers.
     *
     * @throws IOException when there is a I/O issue during response processing.
     */
    @Override
    public void flush() throws IOException {
        super.flushQueue();
    }

    public void onComplete() {
        checkClosed();
        subscription.cancel();
        close();
    }

    private void checkClosed() {
        if (isClosed()) {
            throw new IllegalStateException(LocalizationMessages.EVENT_SOURCE_ALREADY_CLOSED());
        }
    }
}
