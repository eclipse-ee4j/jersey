/*
 * Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2021 Payara Foundation and/or its affiliates. All rights reserved.
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
package org.glassfish.jersey.microprofile.restclient;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.sse.InboundSseEvent;
import org.glassfish.jersey.client.ChunkedInput;
import org.glassfish.jersey.internal.PropertiesDelegate;
import org.glassfish.jersey.internal.util.JerseyPublisher;
import org.glassfish.jersey.media.sse.EventInput;
import org.glassfish.jersey.media.sse.InboundEvent;
import org.glassfish.jersey.message.MessageBodyWorkers;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

public class SseEventPublisher extends EventInput implements Publisher<InboundEvent> {

    private final Executor executor;
    private final Type genericType;
    private final JerseyPublisher<Object> publisher;

    /**
     * Package-private constructor used by the
     * {@link org.glassfish.jersey.microprofile.restclient.SseMessageBodyReader}.
     *
     * @param inputStream response input stream.
     * @param annotations annotations associated with response entity.
     * @param mediaType response entity media type.
     * @param headers response headers.
     * @param messageBodyWorkers message body workers.
     * @param propertiesDelegate properties delegate for this request/response.
     */
    SseEventPublisher(InputStream inputStream,
            Type genericType,
            Annotation[] annotations,
            MediaType mediaType,
            MultivaluedMap<String, String> headers,
            MessageBodyWorkers messageBodyWorkers,
            PropertiesDelegate propertiesDelegate,
            ExecutorService executor) {
        super(inputStream, annotations, mediaType, headers, messageBodyWorkers, propertiesDelegate);

        this.executor = executor;
        this.genericType = genericType;
        this.publisher = new JerseyPublisher<>(executor::submit, JerseyPublisher.PublisherStrategy.BEST_EFFORT);
    }

    private static final Logger LOG = Logger.getLogger(SseEventPublisher.class.getName());

    /**
     * Request {@link SseEventPublisher} to start streaming data.
     *
     * Each {@link SseEventSubscription} will work for only a single
     * {@link Subscriber}. If the {@link SseEventPublisher} rejects the
     * subscription attempt or otherwise fails it will signal the error via
     * {@link Subscriber#onError(Throwable)}.
     *
     * @param subscriber the {@link Subscriber} that will consume signals from
     * the {@link SseEventPublisher}
     */
    @Override
    public void subscribe(Subscriber subscriber) {
        if (subscriber == null) {
            throw new NullPointerException("The subscriber is `null`");
        }
        this.publisher.subscribe(new SseEventSuscriber(subscriber));

        Runnable readEventTask = () -> {
            Type typeArgument;
            if (genericType instanceof ParameterizedType) {
                typeArgument = ((ParameterizedType) genericType).getActualTypeArguments()[0];
                ChunkedInput<InboundEvent> input = SseEventPublisher.this;
                try {
                    InboundSseEvent event;
                    //  org.reactivestreams.Publisher<javax.ws.rs.sse.InboundSseEvent>
                    if (typeArgument.equals(InboundSseEvent.class)) {
                        while ((event = input.read()) != null) {
                         this.publisher.publish(event);
                        }
                    } else {
                        // Read event data as a given Java type e.g org.reactivestreams.Publisher<CustomEvent>
                        while ((event = input.read()) != null) {
                            this.publisher.publish(event.readData((Class) typeArgument));
                        }
                    }
                } catch (Throwable t) {
                    subscriber.onError(t);
                    return;
                }
                this.publisher.close();
            }
        };
        try {
            executor.execute(readEventTask);
        } catch (RejectedExecutionException ex) {
            LOG.log(Level.WARNING, "Executor {0} rejected emit event task", executor);
        }
    }

}
