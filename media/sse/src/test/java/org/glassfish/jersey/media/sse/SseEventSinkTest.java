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

package org.glassfish.jersey.media.sse;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.sse.Sse;
import javax.ws.rs.sse.SseEventSink;
import javax.ws.rs.sse.SseEventSource;

import javax.inject.Singleton;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Test;
import static org.junit.Assert.assertTrue;

/**
 * @author Pavel Bucek (pavel.bucek at oracle.com)
 */
public class SseEventSinkTest extends JerseyTest {

    private static final CountDownLatch CLIENT_RECEIVED_A_MESSAGE_LATCH = new CountDownLatch(1);
    private static final CountDownLatch RESOURCE_METHOD_END_LATCH = new CountDownLatch(1);

    private static volatile SseEventSink output = null;

    @Override
    protected Application configure() {
        return new ResourceConfig(SseEndpoint.class);
    }

    @Singleton
    @Path("sse")
    public static class SseEndpoint {

        @GET
        @Produces(SseFeature.SERVER_SENT_EVENTS)
        public void get(@Context SseEventSink output, @Context Sse sse) throws InterruptedException {
            SseEventSinkTest.output = output;

            System.out.println("### Server is about to send a message.");

            output.send(sse.newEvent("How will this end?"));

            System.out.println("### Server waiting for client to receive a message.");

            CLIENT_RECEIVED_A_MESSAGE_LATCH.await();

            System.out.println("### Server resource method invocation end.");

            RESOURCE_METHOD_END_LATCH.countDown();
        }
    }

    /**
     * The test test that SSE connection is really closed when SseEventSource.close() is called.
     * <p/>
     * This test is very HttpURLConnection and Grizzly server specific, so it will probably fail, if other client and server
     * transport are used.
     */
    @Test
    public void testBlockingResourceMethod() throws InterruptedException {
        WebTarget sseTarget = target("sse");

        final CountDownLatch eventLatch = new CountDownLatch(3);
        SseEventSource eventSource = SseEventSource.target(sseTarget).build();
        eventSource.register((event) -> {
            System.out.println("### Client received: " + event);
            CLIENT_RECEIVED_A_MESSAGE_LATCH.countDown();
        });
        eventSource.open();

        // client waiting for confirmation that resource method ended.
        assertTrue(RESOURCE_METHOD_END_LATCH.await(10000, TimeUnit.MILLISECONDS));
    }
}
