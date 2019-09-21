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

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.sse.InboundSseEvent;
import javax.ws.rs.sse.SseEventSource;

import org.glassfish.jersey.client.ClientExecutor;
import org.glassfish.jersey.client.JerseyWebTarget;
import org.glassfish.jersey.internal.jsr166.Flow;
import org.glassfish.jersey.internal.util.JerseyPublisher;
import org.glassfish.jersey.media.sse.LocalizationMessages;

/**
 * {@code SseEventSource} implementation.
 */
public class JerseySseEventSource implements SseEventSource {

    private static final long DEFAULT_RECONNECT_DELAY = 500;
    private static final Logger LOGGER = Logger.getLogger(JerseySseEventSource.class.getName());

    private static final Consumer<Flow.Subscription> DEFAULT_SUBSCRIPTION_HANDLER =
            sseSubscription -> sseSubscription.request(Long.MAX_VALUE);

    private static final Consumer<Throwable> DEFAULT_ERROR_HANDLER =
            throwable -> LOGGER.log(
                    Level.WARNING,
                    LocalizationMessages.EVENT_SOURCE_DEFAULT_ONERROR(),
                    throwable);

    private JerseyPublisher<InboundSseEvent> publisher;

    /**
     * SseEventSource internal state.
     */
    private final AtomicReference<EventProcessor.State> state = new AtomicReference<>(EventProcessor.State.READY);
    /**
     * SSE streaming resource target.
     */
    private final JerseyWebTarget endpoint;
    /**
     * Reconnect delay value.
     */
    private final long reconnectDelay;
    /**
     * Reconnect delay time unit.
     */
    private final TimeUnit reconnectTimeUnit;
    /**
     * Client provided executor facade.
     */
    private final ClientExecutor clientExecutor;

    /**
     * Private constructor.
     *
     * @param endpoint          SSE resource {@link WebTarget}
     * @param reconnectDelay    amount of time units before next reconnect attempt
     * @param reconnectTimeUnit time units to measure the reconnect attempts in
     */
    private JerseySseEventSource(final JerseyWebTarget endpoint,
                                 final long reconnectDelay,
                                 final TimeUnit reconnectTimeUnit) {

        this.endpoint = endpoint;
        this.reconnectDelay = reconnectDelay;
        this.reconnectTimeUnit = reconnectTimeUnit;
        this.clientExecutor = endpoint.getConfiguration().getClientExecutor();
        this.publisher = new JerseyPublisher<>(clientExecutor::submit, JerseyPublisher.PublisherStrategy.BLOCKING);
    }

    /**
     * On event callback, invoked whenever an event is received.
     *
     * @param inboundEvent received event.
     */
    public void onEvent(final InboundSseEvent inboundEvent) {
        publisher.publish(inboundEvent);
    }

    @Override
    public void register(final Consumer<InboundSseEvent> onEvent) {
        this.subscribe(DEFAULT_SUBSCRIPTION_HANDLER, onEvent, DEFAULT_ERROR_HANDLER, () -> {
        });
    }

    @Override
    public void register(final Consumer<InboundSseEvent> onEvent, final Consumer<Throwable> onError) {
        this.subscribe(DEFAULT_SUBSCRIPTION_HANDLER, onEvent, onError, () -> {
        });
    }

    @Override
    public void register(final Consumer<InboundSseEvent> onEvent, final Consumer<Throwable> onError, final Runnable onComplete) {
        this.subscribe(DEFAULT_SUBSCRIPTION_HANDLER, onEvent, onError, onComplete);
    }

    private void subscribe(final Consumer<Flow.Subscription> onSubscribe,
                           final Consumer<InboundSseEvent> onEvent,
                           final Consumer<Throwable> onError,
                           final Runnable onComplete) {
        if (onSubscribe == null || onEvent == null || onError == null || onComplete == null) {
            throw new IllegalArgumentException(LocalizationMessages.PARAMS_NULL());
        }

        publisher.subscribe(new Flow.Subscriber<InboundSseEvent>() {
            @Override
            public void onSubscribe(final Flow.Subscription subscription) {
                onSubscribe.accept(new Flow.Subscription() {
                    @Override
                    public void request(final long n) {
                        subscription.request(n);
                    }

                    @Override
                    public void cancel() {
                        subscription.cancel();
                    }
                });
            }

            @Override
            public void onNext(final InboundSseEvent item) {
                onEvent.accept(item);
            }

            @Override
            public void onError(final Throwable throwable) {
                onError.accept(throwable);
            }

            @Override
            public void onComplete() {
                onComplete.run();
            }
        });
    }

    @Override
    public void open() {
        if (!state.compareAndSet(EventProcessor.State.READY, EventProcessor.State.OPEN)) {
            switch (state.get()) {
                case CLOSED:
                    throw new IllegalStateException(LocalizationMessages.EVENT_SOURCE_ALREADY_CLOSED());
                case OPEN:
                    throw new IllegalStateException(LocalizationMessages.EVENT_SOURCE_ALREADY_CONNECTED());
            }
        }


        EventProcessor processor = EventProcessor
                .builder(endpoint, state, clientExecutor, this::onEvent, this::close)
                .reconnectDelay(reconnectDelay, reconnectTimeUnit)
                .build();
        clientExecutor.submit(processor);

        // return only after the first request to the SSE endpoint has been made
        processor.awaitFirstContact();
    }

    @Override
    public boolean isOpen() {
        return state.get() == EventProcessor.State.OPEN;
    }

    @Override
    public boolean close(final long timeout, final TimeUnit unit) {
        if (state.getAndSet(EventProcessor.State.CLOSED) != EventProcessor.State.CLOSED) {
            publisher.close();
        }
        return true;
    }

    /**
     * {@link SseEventSource.Builder} implementation.
     */
    public static class Builder extends javax.ws.rs.sse.SseEventSource.Builder {

        private WebTarget endpoint;
        private long reconnectDelay = DEFAULT_RECONNECT_DELAY;
        private TimeUnit reconnectTimeUnit = TimeUnit.MILLISECONDS;

        @Override
        protected Builder target(final WebTarget endpoint) {
            Objects.requireNonNull(endpoint);
            this.endpoint = endpoint;
            return this;
        }

        @Override
        public Builder reconnectingEvery(final long delay, final TimeUnit unit) {
            this.reconnectDelay = delay;
            this.reconnectTimeUnit = unit;
            return this;
        }

        @Override
        public JerseySseEventSource build() {
            if (endpoint instanceof JerseyWebTarget) {
                return new JerseySseEventSource((JerseyWebTarget) endpoint, reconnectDelay, reconnectTimeUnit);
            } else {
                throw new IllegalArgumentException(LocalizationMessages.UNSUPPORTED_WEBTARGET_TYPE(endpoint));
            }
        }
    }
}
