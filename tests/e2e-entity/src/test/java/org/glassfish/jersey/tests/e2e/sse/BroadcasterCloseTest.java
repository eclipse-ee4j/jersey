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

package org.glassfish.jersey.tests.e2e.sse;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.sse.OutboundSseEvent;
import javax.ws.rs.sse.Sse;
import javax.ws.rs.sse.SseBroadcaster;
import javax.ws.rs.sse.SseEventSink;

import javax.inject.Singleton;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test broadcaster behaviour when closing.
 *
 * Proves, that broadcaster attempts to send the messages remaining in the buffer after it receives the close signal.
 *
 * @author Adam Lindenthal (adam.lindenthal at oracle.com)
 */
public class BroadcasterCloseTest extends JerseyTest {

    private static final int SLOW_SUBSCRIBER_LATENCY = 200;
    private static final int MSG_COUNT = 8;
    private static final CountDownLatch onCompleteLatch = new CountDownLatch(1);

    @Override
    protected Application configure() {
        return new ResourceConfig(SseResource.class);
    }

    @Path("events")
    @Singleton
    public static class SseResource {
        private final Sse sse;
        private final SseBroadcaster broadcaster;
        private final List<String> data = new ArrayList<>();

        public SseResource(@Context final Sse sse) {
            this.sse = sse;
            this.broadcaster = sse.newBroadcaster();
            this.broadcaster.register(new SseEventSink() {

                volatile boolean closed = false;

                @Override
                public boolean isClosed() {
                    return closed;
                }

                @Override
                public CompletionStage<?> send(OutboundSseEvent event) {
                    try {
                        Thread.sleep(SLOW_SUBSCRIBER_LATENCY);
                    } catch (InterruptedException e) {
                        System.out.println("Slow subscriber's sleep was interrupted.");
                    }
                    data.add("" + event.getData());

                    return CompletableFuture.completedFuture(null);
                }

                @Override
                public void close() {
                    System.out.println("Slow subscriber completed");
                    onCompleteLatch.countDown();
                    this.closed = true;
                }
            });
        }

        @GET
        @Produces(MediaType.SERVER_SENT_EVENTS)
        public void getServerSentEvents(@Context final SseEventSink eventSink) {
            broadcaster.register(eventSink);
        }

        @GET
        @Path("push/{msg}")
        public String addMessage(@PathParam("msg") String message) throws InterruptedException {
            broadcaster.broadcast(sse.newEvent(message));
            return "Message added.";
        }

        @GET
        @Path("close")
        public String closeMe() {
            broadcaster.close();
            return "Closed";
        }

        @GET
        @Path("result")
        public String getResult() {
            return data.stream().collect(Collectors.joining(","));
        }
    }

    @Test
    public void testBroadcasterKeepsSendingAfterCLose() throws InterruptedException {
        // push some events to the broadcaster
        IntStream.range(0, MSG_COUNT).forEach((i) -> {
            final Response response = target()
                    .path("events/push/{msg}")
                    .resolveTemplate("msg", "msg" + i)
                    .request()
                    .get();
            Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        });

        // instruct broadcaster to close
        final Response response = target().path("events/close").request().get();
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        // send one more message (should be rejected -> request will fail)
        final Response badResponse = target()
                                        .path("events/push/{msg}")
                                        .resolveTemplate("msg", "too-late")
                                        .request()
                                        .get();
        Assert.assertNotEquals(Response.Status.OK.getStatusCode(), badResponse.getStatus());

        // wait up to latency * msgcount (+1 as reserve) before the server shuts down
        Assert.assertTrue(onCompleteLatch.await(SLOW_SUBSCRIBER_LATENCY * (MSG_COUNT + 1), TimeUnit.MILLISECONDS));

        // get data gathered by the slow subsciber
        String result = target().path("events/result").request().get(String.class);
        final String[] resultArray = result.split(",");

        // check, that broadcaster sent all the buffered events to the subscriber before completely closing
        Assert.assertEquals(MSG_COUNT, resultArray.length);
        for (int i = 0; i < MSG_COUNT; i++) {
            Assert.assertEquals("msg" + i, resultArray[i]);
        }
    }
}
