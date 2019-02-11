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

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Assert;
import org.junit.Test;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.sse.OutboundSseEvent;
import javax.ws.rs.sse.Sse;
import javax.ws.rs.sse.SseBroadcaster;
import javax.ws.rs.sse.SseEventSink;
import javax.ws.rs.sse.SseEventSource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * JAX-RS {@link javax.ws.rs.sse.SseBroadcaster} test.
 *
 * @author Adam Lindenthal (adam.lindenthal at oracle.com)
 */
public class BroadcasterTest extends JerseyTest {

    static final CountDownLatch closeLatch = new CountDownLatch(4);
    static final CountDownLatch txLatch = new CountDownLatch(4);
    private static boolean isSingleton = false;

    private static int ASYNC_WAIT_TIMEOUT = 1000; //timeout for asynchronous events to complete activities

    @Path("sse")
    @Singleton
    public static class SseResource {
        private final Sse sse;
        private SseBroadcaster broadcaster;
        private OutboundSseEvent.Builder builder;

        public SseResource(@Context final Sse sse) {
            this.sse = sse;
            broadcaster = sse.newBroadcaster();
        }

        @GET
        @Produces(MediaType.SERVER_SENT_EVENTS)
        @Path("events")
        public void getServerSentEvents(@Context final SseEventSink eventSink, @Context final Sse sse) {
            isSingleton = this.sse == sse;
            builder = sse.newEventBuilder();
            eventSink.send(builder.data("Event1").build());
            eventSink.send(builder.data("Event2").build());
            eventSink.send(builder.data("Event3").build());
            broadcaster.register(eventSink);
            broadcaster.onClose((subscriber) -> {
                if (subscriber == eventSink) {
                    closeLatch.countDown();
                }
            });
            txLatch.countDown();
        }

        @Path("push/{msg}")
        @Produces(MediaType.SERVER_SENT_EVENTS)
        @GET
        public String pushMessage(@PathParam("msg") final String msg) {
            broadcaster.broadcast(builder.data(msg).build());
            txLatch.countDown();
            return "Broadcasting message: " + msg;
        }

        @Path("close")
        @GET
        public String close() {
            broadcaster.close();
            return "Closed.";
        }
    }

    /**
     * Wrapper to hold results coming from events (including broadcast)
     *
     * @param <T> type of expected results
     */
    public static class EventListWrapper<T> {
        private final List<T> data; //event results
        private final CountDownLatch eventCountDown; //count down delay for expected results
        private final CountDownLatch broadcastLag = new CountDownLatch(1); //broadcast lag
        // which shall be hold until thread is ready to process events from broadcast
        private static final int LAG_INTERVAL = 1000; //broadcast lag timeout - in milliseconds (1s)
        private static final int EXPECTED_REGULAR_EVENTS_COUNT = 3; //expected regular outbound events

        public EventListWrapper(List<T> data, CountDownLatch eventCountDown) {
            this.data = data;
            this.eventCountDown = eventCountDown;
        }

        public void add(T msg) {
            data.add(msg);
            eventCountDown.countDown();
            if (eventCountDown.getCount() == EXPECTED_REGULAR_EVENTS_COUNT) { //all regular events are received,
                                                                              //ready for broadcast
                broadcastLag.countDown();
            }
        }

        public CountDownLatch getEventCountDown() {
            return eventCountDown;
        }

        public T get(int pos) {
            return data.get(pos);
        }

        public int size() {
            return data.size();
        }

        /**
         * makes current thread to wait for predefined interval until broadcast is ready
         *
         * @throws InterruptedException in case of something went wrong
         */
        public boolean waitBroadcast() throws InterruptedException {
            return broadcastLag.await(LAG_INTERVAL, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    protected Application configure() {
        final ResourceConfig rc = new ResourceConfig(SseResource.class);
        rc.property(ServerProperties.WADL_FEATURE_DISABLE, true);
        return rc;
    }

    @Test
    public void test() throws InterruptedException {
        final SseEventSource eventSourceA = SseEventSource.target(target().path("sse/events")).build();
        final EventListWrapper<String> resultsA1 = new EventListWrapper(new ArrayList(), new CountDownLatch(5));
        final EventListWrapper<String> resultsA2 = new EventListWrapper(new ArrayList(), new CountDownLatch(5));

        eventSourceA.register(event -> resultsA1.add(event.readData()));
        eventSourceA.register(event -> resultsA2.add(event.readData()));
        eventSourceA.open();

        Assert.assertTrue(resultsA1.waitBroadcast()); //some delay is required to process consumer and producer
        Assert.assertTrue(resultsA2.waitBroadcast()); //some delay is required to process consumer and producer

        target().path("sse/push/firstBroadcast").request().get(String.class);


        final SseEventSource eventSourceB = SseEventSource.target(target().path("sse/events")).build();
        final EventListWrapper<String> resultsB1 = new EventListWrapper(new ArrayList(), new CountDownLatch(4));
        final EventListWrapper<String> resultsB2 = new EventListWrapper(new ArrayList(), new CountDownLatch(4));

        eventSourceB.register(event -> resultsB1.add(event.readData()));
        eventSourceB.register(event -> resultsB2.add(event.readData()));
        eventSourceB.open();

        Assert.assertTrue(resultsB1.waitBroadcast()); //some delay is required to process consumer and producer
        Assert.assertTrue(resultsB2.waitBroadcast()); //some delay is required to process consumer and producer

        target().path("sse/push/secondBroadcast").request().get(String.class);

        Assert.assertTrue("Waiting for resultsA1 to be complete failed.",
                resultsA1.getEventCountDown().await(ASYNC_WAIT_TIMEOUT, TimeUnit.MILLISECONDS));
        Assert.assertTrue("Waiting for resultsA2 to be complete failed.",
                resultsA2.getEventCountDown().await(ASYNC_WAIT_TIMEOUT, TimeUnit.MILLISECONDS));

        Assert.assertTrue("Waiting for resultsB1 to be complete failed.",
                resultsB1.getEventCountDown().await(ASYNC_WAIT_TIMEOUT, TimeUnit.MILLISECONDS));
        Assert.assertTrue("Waiting for resultsB2 to be complete failed.",
                resultsB2.getEventCountDown().await(ASYNC_WAIT_TIMEOUT, TimeUnit.MILLISECONDS));

        Assert.assertTrue(txLatch.await(5000, TimeUnit.MILLISECONDS));

        // Event1, Event2, Event3, firstBroadcast, secondBroadcast
        Assert.assertEquals("resultsA1 does not contain 5 elements.", 5, resultsA1.size());
        Assert.assertEquals("resultsA2 does not contain 5 elements.", 5, resultsA2.size());
        Assert.assertTrue("resultsA1 does not contain expected data",
                resultsA1.get(0).equals("Event1")
                        && resultsA1.get(1).equals("Event2")
                        && resultsA1.get(2).equals("Event3")
                        && resultsA1.get(3).equals("firstBroadcast")
                        && resultsA1.get(4).equals("secondBroadcast"));

        Assert.assertTrue("resultsA2 does not contain expected data",
                resultsA2.get(0).equals("Event1")
                        && resultsA2.get(1).equals("Event2")
                        && resultsA2.get(2).equals("Event3")
                        && resultsA2.get(3).equals("firstBroadcast")
                        && resultsA2.get(4).equals("secondBroadcast"));

        Assert.assertEquals("resultsB1 does not contain 4 elements.", 4, resultsB1.size());
        Assert.assertEquals("resultsB2 does not contain 4 elements.", 4, resultsB2.size());
        Assert.assertTrue("resultsB1 does not contain expected data",
                resultsB1.get(0).equals("Event1")
                        && resultsB1.get(1).equals("Event2")
                        && resultsB1.get(2).equals("Event3")
                        && resultsB1.get(3).equals("secondBroadcast"));

        Assert.assertTrue("resultsB2 does not contain expected data",
                resultsB2.get(0).equals("Event1")
                        && resultsB2.get(1).equals("Event2")
                        && resultsB2.get(2).equals("Event3")
                        && resultsB2.get(3).equals("secondBroadcast"));
        target().path("sse/close").request().get();
        closeLatch.await();
        Assert.assertTrue("Sse instances injected into resource and constructor differ. Sse should have been injected"
                + "as a singleton", isSingleton);
    }
}
