/*
 * Copyright (c) 2026 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.media.sse;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.sse.InboundSseEvent;
import jakarta.ws.rs.sse.Sse;
import jakarta.ws.rs.sse.SseEventSink;
import jakarta.ws.rs.sse.SseEventSource;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Tests that an HTTP 204 (No Content) response terminates the {@link SseEventSource} gracefully -
 * the completion callback is invoked, no error is reported and no further reconnect attempts are
 * made - in alignment with the WHATWG EventSource processing model.
 */
public class SseEventSourceNoContentTerminationTest extends JerseyTest {

    private static final AtomicInteger CONNECT_COUNT = new AtomicInteger();

    @Path("sse")
    public static class NoContentTerminationTestSseEndpoint {

        @Path("204")
        @GET
        @Produces(SseFeature.SERVER_SENT_EVENTS)
        public void immediateNoContent(@Context SseEventSink output, @Context Sse sse) {
            CONNECT_COUNT.incrementAndGet();
            throw new WebApplicationException(Response.noContent().build());
        }

        @Path("204-reconnect")
        @GET
        @Produces(SseFeature.SERVER_SENT_EVENTS)
        public void noContentOnReconnect(@HeaderParam(HttpHeaders.LAST_EVENT_ID_HEADER) String lastEventId,
                                         @Context SseEventSink output,
                                         @Context Sse sse) throws IOException {
            CONNECT_COUNT.incrementAndGet();
            if (lastEventId != null) {
                // the client reconnected after the stream completed - signal that no further events
                // will ever be sent by responding with HTTP 204
                throw new WebApplicationException(Response.noContent().build());
            }
            try (SseEventSink sink = output) {
                for (int i = 0; i < 3; i++) {
                    sink.send(sse.newEventBuilder().id(String.valueOf(i)).data(String.valueOf(i)).build());
                }
            }
        }
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(NoContentTerminationTestSseEndpoint.class);
    }

    @Test
    public void testImmediateNoContentTerminatesWithoutError() throws InterruptedException {
        CONNECT_COUNT.set(0);
        WebTarget sseTarget = target("sse/204");
        AtomicReference<Throwable> throwable = new AtomicReference<>();
        CountDownLatch completeLatch = new CountDownLatch(1);

        SseEventSource eventSource = SseEventSource.target(sseTarget).build();
        eventSource.register(event -> { }, throwable::set, completeLatch::countDown);
        eventSource.open();

        MatcherAssert.assertThat(completeLatch.await(10_000, TimeUnit.MILLISECONDS), Matchers.is(true));
        MatcherAssert.assertThat(throwable.get(), Matchers.nullValue());
        MatcherAssert.assertThat(eventSource.isOpen(), Matchers.is(false));
        MatcherAssert.assertThat(CONNECT_COUNT.get(), Matchers.is(1));
    }

    @Test
    public void testNoContentOnReconnectTerminatesWithoutError() throws InterruptedException {
        CONNECT_COUNT.set(0);
        WebTarget sseTarget = target("sse/204-reconnect");
        AtomicReference<Throwable> throwable = new AtomicReference<>();
        List<String> events = new CopyOnWriteArrayList<>();
        CountDownLatch completeLatch = new CountDownLatch(1);

        SseEventSource eventSource = SseEventSource.target(sseTarget)
                .reconnectingEvery(100, TimeUnit.MILLISECONDS)
                .build();
        eventSource.register(event -> events.add(event.readData()), throwable::set, completeLatch::countDown);
        eventSource.open();

        MatcherAssert.assertThat(completeLatch.await(10_000, TimeUnit.MILLISECONDS), Matchers.is(true));
        MatcherAssert.assertThat(throwable.get(), Matchers.nullValue());
        MatcherAssert.assertThat(events, Matchers.contains("0", "1", "2"));
        MatcherAssert.assertThat(eventSource.isOpen(), Matchers.is(false));

        // the event source must not attempt any further reconnects after the 204 response
        Thread.sleep(500);
        MatcherAssert.assertThat(CONNECT_COUNT.get(), Matchers.is(2));
    }
}
