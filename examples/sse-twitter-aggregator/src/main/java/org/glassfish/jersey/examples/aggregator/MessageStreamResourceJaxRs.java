/*
 * Copyright (c) 2017, 2020 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.aggregator;

import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.sse.OutboundSseEvent;
import jakarta.ws.rs.sse.Sse;
import jakarta.ws.rs.sse.SseBroadcaster;
import jakarta.ws.rs.sse.SseEventSink;

import javax.inject.Singleton;

/**
 * Resource that aggregates incoming messages and broadcasts them
 * to the registered Server-Sent Even (SSE) client streams.
 * <p>
 * Uses the JAX-RS 2.1 SSE API.
 *
 * @see MessageStreamResourceJersey
 * @author Marek Potociar
 */
@Path("message/stream/jaxrs")
@Singleton
public final class MessageStreamResourceJaxRs {
    private static final Logger LOGGER = Logger.getLogger(MessageStreamResourceJaxRs.class.getName());
    private static AtomicLong nextMessageId = new AtomicLong(0);

    private final Sse sse;
    private final SseBroadcaster broadcaster;

    public MessageStreamResourceJaxRs(@Context Sse sse) {
        this.sse = sse;
        this.broadcaster = sse.newBroadcaster();
    }

    /**
     * Put a new message to the stream.
     *
     * The message will be broadcast to all registered SSE clients.
     *
     * @param message message to be broadcast.
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public void putMessage(final Message message) {
        LOGGER.info("--> Message received.");

        final OutboundSseEvent event = sse.newEventBuilder()
                .id(String.valueOf(nextMessageId.getAndIncrement()))
                .mediaType(MediaType.APPLICATION_JSON_TYPE)
                .data(Message.class, message)
                .build();

        broadcaster.broadcast(event);
    }

    /**
     * Get the new SSE message stream channel.
     */
    @GET
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public void getMessageStream(@Context SseEventSink eventSink) {
        LOGGER.info("--> SSE connection received.");
        broadcaster.register(eventSink);
    }

}
