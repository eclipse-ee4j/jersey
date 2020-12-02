/*
 * Copyright (c) 2017, 2020 Oracle and/or its affiliates. All rights reserved.
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
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Provider;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.sse.OutboundSseEvent;
import javax.ws.rs.sse.SseEventSink;

import org.glassfish.jersey.internal.jsr166.Flow;
import org.glassfish.jersey.internal.jsr166.JerseyFlowSubscriber;
import org.glassfish.jersey.media.sse.LocalizationMessages;
import org.glassfish.jersey.media.sse.OutboundEvent;
import org.glassfish.jersey.server.AsyncContext;
import org.glassfish.jersey.server.ChunkedOutput;

/**
 * Server-side SSE subscriber.
 * <p>
 * The reference should be obtained via injection into the resource method.
 *
 * @author Adam Lindenthal
 */
class JerseyEventSink extends ChunkedOutput<OutboundSseEvent>
        implements SseEventSink, Flushable, JerseyFlowSubscriber<Object> {

    private static final Logger LOGGER = Logger.getLogger(JerseyEventSink.class.getName());
    private static final byte[] SSE_EVENT_DELIMITER = "\n".getBytes(StandardCharsets.UTF_8);
    private Flow.Subscription subscription = null;
    private final AtomicBoolean subscribed = new AtomicBoolean(false);
    private volatile MediaType implicitMediaType = null;

    JerseyEventSink(Provider<AsyncContext> asyncContextProvider) {
        super(SSE_EVENT_DELIMITER, asyncContextProvider);
    }

    @Override
    public void onSubscribe(final Flow.Subscription subscription) {
        if (subscription == null) {
            throw new NullPointerException(LocalizationMessages.PARAM_NULL("subscription"));
        }
        if (subscribed.getAndSet(true)) {
            subscription.cancel();
            return;
        }

        this.subscription = subscription;
        if (isClosed()) {
            subscription.cancel();
        } else {
            subscription.request(Long.MAX_VALUE);
        }
    }


    @Override
    public void onNext(final Object item) {
        if (item == null) {
            throw new NullPointerException(LocalizationMessages.PARAM_NULL("outboundSseEvent"));
        }
        try {
            checkClosed();
            MediaType implicitType = resolveMediaType(item);
            if (MediaType.SERVER_SENT_EVENTS_TYPE.equals(implicitType)) {
                // already wrapped
                write((OutboundSseEvent) item);
            } else {
                // implicit wrapping
                // TODO: Jersey annotation for explicit media type
                write(new OutboundEvent.Builder()
                        .mediaType(implicitType)
                        .data(item)
                        .build());
            }
        } catch (final Throwable e) {
            // spec allows only NPE to be thrown from onNext
            LOGGER.log(Level.SEVERE, LocalizationMessages.EVENT_SINK_NEXT_FAILED(), e);
            cancelSubscription();
        }
    }

    @Override
    public void onError(final Throwable throwable) {
        if (throwable == null) {
            throw new NullPointerException(LocalizationMessages.PARAM_NULL("throwable"));
        }
        try {
            LOGGER.log(Level.SEVERE, LocalizationMessages.EVENT_SOURCE_DEFAULT_ONERROR(), throwable);
            super.close();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, LocalizationMessages.EVENT_SINK_CLOSE_FAILED(), e);
        }
    }

    public void onComplete() {
        try {
            super.close();
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, LocalizationMessages.EVENT_SINK_CLOSE_FAILED(), e);
        }
    }

    @Override
    public void close() {
        try {
            cancelSubscription();
            super.close();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, LocalizationMessages.EVENT_SINK_CLOSE_FAILED(), e);
        }
    }

    @Override
    public CompletionStage<?> send(OutboundSseEvent event) {
        checkClosed();
        try {
            this.write(event);
            return CompletableFuture.completedFuture(null);
        } catch (IOException e) {
            CompletableFuture<Void> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
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

    @Override
    protected void onClose(Exception e) {
        cancelSubscription();
    }

    private void cancelSubscription() {
        if (subscription != null) {
            subscription.cancel();
        }
    }

    private void checkClosed() {
        if (isClosed()) {
            cancelSubscription();
            throw new IllegalStateException(LocalizationMessages.EVENT_SOURCE_ALREADY_CLOSED());
        }
    }

    private MediaType resolveMediaType(Object item) {
        // resolve lazily as all stream items are presumed to be of a same type
        if (implicitMediaType == null) {
            Class<?> clazz = item.getClass();
            if (String.class.equals(clazz)
                    || Number.class.isAssignableFrom(clazz)
                    || Character.class.equals(clazz)
                    || Boolean.class.equals(clazz)) {
                implicitMediaType = MediaType.TEXT_PLAIN_TYPE;
                return implicitMediaType;
            }
            // unknown unwrapped objects are treated as json media type
            implicitMediaType = MediaType.APPLICATION_JSON_TYPE;
        }
        return implicitMediaType;
    }
}
