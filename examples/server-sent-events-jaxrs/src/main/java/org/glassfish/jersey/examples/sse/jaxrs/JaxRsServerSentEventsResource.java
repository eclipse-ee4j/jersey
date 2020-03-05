/*
 * Copyright (c) 2017, 2020 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.sse.jaxrs;

import java.io.IOException;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.sse.Sse;
import jakarta.ws.rs.sse.SseEventSink;

/**
 * @author Pavel Bucek
 * @author Adam Lindenthal
 */
@Path("server-sent-events")
public class JaxRsServerSentEventsResource {

    private static volatile SseEventSink eventSink = null;

    @GET
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public void getMessageQueue(@Context SseEventSink sink) {
        eventSink = sink;
    }

    @POST
    public void addMessage(final String message, @Context Sse sse) throws IOException {
        final SseEventSink localSink = eventSink;
        if (localSink != null) {
            localSink.send(sse.newEventBuilder().name("custom-message").data(String.class, message).build());
        }
    }

    @DELETE
    public void close() throws IOException {
        final SseEventSink localSink = eventSink;
        if (localSink != null) {
            eventSink.close();
        }
        eventSink = null;
    }

    @POST
    @Path("domains/{id}")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public void startDomain(@PathParam("id") final String id, @Context SseEventSink domainSink, @Context Sse sse) {
        new Thread(() -> {
            try {
                domainSink.send(sse.newEventBuilder()
                                    .name("domain-progress")
                                    .data(String.class, "starting domain " + id + " ...")
                                    .build());
                Thread.sleep(200);
                domainSink.send(sse.newEventBuilder().name("domain-progress").data(String.class, "50%").build());
                Thread.sleep(200);
                domainSink.send(sse.newEventBuilder().name("domain-progress").data(String.class, "60%").build());
                Thread.sleep(200);
                domainSink.send(sse.newEventBuilder().name("domain-progress").data(String.class, "70%").build());
                Thread.sleep(200);
                domainSink.send(sse.newEventBuilder().name("domain-progress").data(String.class, "99%").build());
                Thread.sleep(200);
                domainSink.send(sse.newEventBuilder().name("domain-progress").data(String.class, "done").build());
                domainSink.close();

            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
