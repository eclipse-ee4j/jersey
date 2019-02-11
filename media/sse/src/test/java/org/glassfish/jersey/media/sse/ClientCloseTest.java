/*
 * Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;

import javax.inject.Singleton;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.TestCase.assertTrue;

/**
 * @author Petr Janouch (petr.janouch at oracle.com)
 */
public class ClientCloseTest extends JerseyTest {

    private static final int LATCH_WAIT_TIMEOUT = 16;

    /**
     * The test test that SSE connection is really closed when EventSource.close() is called.
     * <p/>
     * This test is very HttpURLConnection and Grizzly server specific, so it will probably fail, if other client and server
     * transport are used.
     */
    @Test
    public void testClose() throws InterruptedException {
        WebTarget sseTarget = target("sse");

        CountDownLatch eventLatch = new CountDownLatch(3);
        CountDownLatch eventLatch2 = new CountDownLatch(4);
        EventSource eventSource = new EventSource(sseTarget) {
            @Override
            public void onEvent(final InboundEvent inboundEvent) {
                eventLatch.countDown();
                eventLatch2.countDown();
            }
        };

        // Server sends 3 events we are interested in.
        IntStream.range(0, 3).forEach((i) -> assertEquals("OK",
                target("sse/send").request().get().readEntity(String.class)));
        assertTrue(eventLatch.await(LATCH_WAIT_TIMEOUT, TimeUnit.SECONDS));

        // After receiving the 3 events, we try to close.
        eventSource.close();

        // Unfortunately the HTTPURLConnection is blocked in read() method, so it will close only after receiving the next event.
        assertEquals("OK", target("sse/send").request().get().readEntity(String.class));
        // Wait for the event that will unblock the HTTPURLConnection and result in sending FIN.
        assertTrue(eventLatch2.await(LATCH_WAIT_TIMEOUT, TimeUnit.SECONDS));
        // Now it is interesting. Client has sent FIN, but Grizzly does not listen for client input (selector READ key is
        // disabled), while streaming the response. For some reason we need to send 2 more events for Grizzly to notice
        // that the client is gone.
        assertEquals("OK", target("sse/send").request().get().readEntity(String.class));
        assertEquals("OK", target("sse/send").request().get().readEntity(String.class));
        for (int i = 0; i < 10; i++) {
            System.out.println(i + ": " + target("sse/send").request().get().readEntity(String.class));
        }
        // Now the grizzly should notice that the SSE connection is finally dead and sending events from the server will fail.
        assertEquals("NOK", target("sse/send").request().get().readEntity(String.class));
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(SseEndpoint.class);
    }

    @Override
    protected void configureClient(ClientConfig config) {
        config.register(SseFeature.class);
    }

    @Singleton
    @Path("sse")
    public static class SseEndpoint {

        private final EventOutput eventOutput = new EventOutput();

        @GET
        @Path("send")
        public String sendEvent() throws InterruptedException {
            OutboundEvent event = new OutboundEvent.Builder().data("An event").build();
            try {
                if (eventOutput.isClosed()) {
                    return "NOK";
                }

                eventOutput.write(event);
            } catch (IOException e) {
                return "NOK";
            }

            return "OK";
        }

        @GET
        @Produces(SseFeature.SERVER_SENT_EVENTS)
        public EventOutput get() {
            return eventOutput;
        }
    }
}
