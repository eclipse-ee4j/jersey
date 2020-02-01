/*
 * Copyright (c) 2017, 2020 Oracle and/or its affiliates. All rights reserved.
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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.sse.OutboundSseEvent;
import jakarta.ws.rs.sse.Sse;
import jakarta.ws.rs.sse.SseEventSink;
import jakarta.ws.rs.sse.SseEventSource;

import javax.inject.Singleton;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test, that {@code SseEventSink} and the connection is closed eventually after closing {@code SseEventSource} on client side.
 *
 * @author Adam Lindenthal
 */
public class SseEventSinkCloseTest extends JerseyTest {

    private static Logger LOGGER = Logger.getLogger(SseEventSinkCloseTest.class.getName());
    private static volatile SseEventSink output = null;
    private static CountDownLatch openLatch = new CountDownLatch(1);

    @Singleton
    @Path("sse")
    public static class SseEndpoint {
        @GET
        @Path("send")
        public String sendEvent(@Context Sse sse) throws InterruptedException {
            OutboundSseEvent event = sse.newEventBuilder().data("An event").build();
            if (!output.isClosed()) {
                output.send(event);
                return "OK";
            }
            return "Closed";
        }

        @GET
        @Produces(SseFeature.SERVER_SENT_EVENTS)
        public void get(@Context SseEventSink output, @Context Sse sse) {
            SseEventSinkCloseTest.output = output;
            openLatch.countDown();
        }
    }

    /**
     * The test test that SSE connection is really closed when SseEventSource.close() is called.
     * <p/>
     * This test is very HttpURLConnection and Grizzly server specific, so it will probably fail, if other client and server
     * transport are used.
     */
    @Test
    public void testClose() throws InterruptedException {
        WebTarget sseTarget = target("sse");

        final CountDownLatch eventLatch = new CountDownLatch(3);
        SseEventSource eventSource = SseEventSource.target(sseTarget).build();
        eventSource.register((event) -> eventLatch.countDown());
        eventSource.open();
        openLatch.await();

        // Tell server to send us 3 events
        for (int i = 0; i < 3; i++) {
            final String response = target("sse/send").request().get().readEntity(String.class);
            assertEquals("OK", response);
        }

        // ... and wait for the events to be processed by the client side, then close the eventSource
        assertTrue("EventLatch timed out.", eventLatch.await(5, TimeUnit.SECONDS));
        eventSource.close();
        assertEquals("SseEventSource should have been already closed", false, eventSource.isOpen());

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

        CountDownLatch closeLatch = new CountDownLatch(100);
        executor.scheduleAtFixedRate(() -> {
            if (output.isClosed()) {
                // countdown to zero
                while (closeLatch.getCount() > 0) {
                    closeLatch.countDown();
                    return;
                }
            }
            final Response response = target("sse/send").request().get();
            LOGGER.info(200 == response.getStatus() ? "Still alive" : "Error received");
            closeLatch.countDown();
        }, 0, 100, TimeUnit.MILLISECONDS);

        assertTrue(closeLatch.await(10000, TimeUnit.MILLISECONDS));
        executor.shutdown();
        assertTrue("SseEventOutput should have been already closed.", output.isClosed());
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(SseEndpoint.class);
    }
}
