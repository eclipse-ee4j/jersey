/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.aggregator;

import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.media.sse.EventOutput;
import org.glassfish.jersey.media.sse.OutboundEvent;
import org.glassfish.jersey.media.sse.SseBroadcaster;
import org.glassfish.jersey.media.sse.SseFeature;
import org.glassfish.jersey.server.ChunkedOutput;

/**
 * Resource that aggregates incoming messages and broadcasts them
 * to the registered Server-Sent Even (SSE) client streams.
 * <p>
 * Uses the Jersey-specific SSE API.
 *
 * @see MessageStreamResourceJaxRs
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
@Path("message/stream/jersey")
public final class MessageStreamResourceJersey {
    private static final Logger LOGGER = Logger.getLogger(MessageStreamResourceJersey.class.getName());

    private static SseBroadcaster broadcaster = new SseBroadcaster() {
        @Override
        public void onException(final ChunkedOutput<OutboundEvent> chunkedOutput, final Exception exception) {
            LOGGER.log(Level.SEVERE, "Error broadcasting message.", exception);
        }
    };
    private static AtomicLong nextMessageId = new AtomicLong(0);

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

        final OutboundEvent event = new OutboundEvent.Builder()
                .id(String.valueOf(nextMessageId.getAndIncrement()))
                .mediaType(MediaType.APPLICATION_JSON_TYPE)
                .data(Message.class, message)
                .build();

        broadcaster.broadcast(event);
    }

    /**
     * Get the new SSE message stream channel.
     *
     * @return new SSE message stream channel.
     */
    @GET
    @Produces(SseFeature.SERVER_SENT_EVENTS)
    public EventOutput getMessageStream() {
        LOGGER.info("--> SSE connection received.");
        final EventOutput eventOutput = new EventOutput();
        broadcaster.add(eventOutput);
        return eventOutput;
    }

}
