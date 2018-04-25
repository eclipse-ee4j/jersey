/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.sse.jaxrs;

import java.io.IOException;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.sse.Sse;
import javax.ws.rs.sse.SseEventSink;

/**
 * @author Pavel Bucek (pavel.bucek at oracle.com)
 * @author Adam Lindenthal (adam.lindenthal at oracle.com)
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
