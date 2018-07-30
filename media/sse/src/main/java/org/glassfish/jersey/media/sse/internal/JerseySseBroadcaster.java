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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.ws.rs.sse.OutboundSseEvent;
import javax.ws.rs.sse.Sse;
import javax.ws.rs.sse.SseBroadcaster;
import javax.ws.rs.sse.SseEventSink;

import org.glassfish.jersey.internal.jsr166.Flow;
import org.glassfish.jersey.internal.util.JerseyPublisher;
import org.glassfish.jersey.media.sse.LocalizationMessages;

/**
 * Used for broadcasting SSE to multiple {@link javax.ws.rs.sse.SseEventSink} instances.
 * <p>
 * JAX-RS 2.1 {@link SseBroadcaster} implementation.
 *
 * @author Adam Lindenthal (adam.lindenthal at oracle.com)
 */
class JerseySseBroadcaster extends JerseyPublisher<OutboundSseEvent> implements SseBroadcaster {

    /**
     * Callbacks notified when {@code SseBroadcaster} is being closed.
     */
    private final CopyOnWriteArrayList<Consumer<SseEventSink>> onCloseListeners;

    /**
     * Callbacks notified when error occurs.
     */
    private final CopyOnWriteArrayList<BiConsumer<SseEventSink, Throwable>> onExceptionListeners;

    /**
     * Package-private constructor.
     * <p>
     * The broadcaster instance should be obtained by calling {@link Sse#newBroadcaster()}, not directly.
     */
    JerseySseBroadcaster() {
        onExceptionListeners = new CopyOnWriteArrayList<>();
        onCloseListeners = new CopyOnWriteArrayList<>();
    }

    /**
     * Package-private constructor.
     * <p>
     * The broadcaster instance should be obtained by calling {@link Sse#newBroadcaster()}, not directly.
     *
     * @param executorService {@code ExecutorService} the executor to use for async delivery,
     *                        supporting creation of at least one independent thread
     */
    JerseySseBroadcaster(final ExecutorService executorService) {
        super(executorService);
        onExceptionListeners = new CopyOnWriteArrayList<>();
        onCloseListeners = new CopyOnWriteArrayList<>();
    }

    @Override
    public void register(SseEventSink sseEventSink) {
        super.subscribe(new SseEventSinkWrapper(sseEventSink));
    }

    @Override
    public void onError(BiConsumer<SseEventSink, Throwable> onError) {
        if (onError == null) {
            throw new IllegalArgumentException(LocalizationMessages.PARAM_NULL("onError"));
        }

        onExceptionListeners.add(onError);
    }

    @Override
    public void onClose(Consumer<SseEventSink> onClose) {
        if (onClose == null) {
            throw new IllegalArgumentException(LocalizationMessages.PARAM_NULL("onClose"));
        }

        onCloseListeners.add(onClose);
    }

    @Override
    public CompletionStage<?> broadcast(final OutboundSseEvent event) {
        if (event == null) {
            throw new IllegalArgumentException(LocalizationMessages.PARAM_NULL("event"));
        }

        return CompletableFuture.completedFuture(publish(event));
    }

    private void notifyOnCompleteHandlers(Flow.Subscriber<? super OutboundSseEvent> subscriber) {
        if (subscriber instanceof SseEventSinkWrapper) {
            onCloseListeners.forEach((listener) -> listener.accept(((SseEventSinkWrapper) subscriber).sseEventSink));
        }
    }

    private void notifyOnErrorCallbacks(final Flow.Subscriber<? super OutboundSseEvent> subscriber, final Throwable throwable) {
        if (subscriber instanceof SseEventSinkWrapper) {
            onExceptionListeners.forEach(
                    (listener) -> listener.accept(((SseEventSinkWrapper) subscriber).sseEventSink, throwable));
        }
    }

    private class SseEventSinkWrapper implements Flow.Subscriber<OutboundSseEvent> {

        private final SseEventSink sseEventSink;

        SseEventSinkWrapper(SseEventSink sseEventSink) {
            this.sseEventSink = sseEventSink;
        }

        @Override
        public void onSubscribe(final Flow.Subscription subscription) {
            // TODO JAX-RS 2.1
            subscription.request(Long.MAX_VALUE);
        }

        @Override
        public void onNext(final OutboundSseEvent item) {
            sseEventSink.send(item);
        }

        @Override
        public void onError(final Throwable throwable) {
            // TODO JAX-RS 2.1
            sseEventSink.close();
            notifyOnErrorCallbacks(this, throwable);
        }

        @Override
        public void onComplete() {
            // TODO JAX-RS 2.1
            sseEventSink.close();
            notifyOnCompleteHandlers(this);

        }
    }
}
