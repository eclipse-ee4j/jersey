/*
 * Copyright (c) 2016, 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Test;
import static org.junit.Assert.assertTrue;

/**
 * Tests handling of empty SSE events.
 */
public class EmptyEventsTest extends JerseyTest {

    @Override
    protected Application configure() {
        return new ResourceConfig(SseTestResource.class, SseFeature.class);
    }

    @Override
    protected void configureClient(ClientConfig config) {
        config.register(SseFeature.class);
    }

    /**
     * Tests a situation when 2 non-empty SSE events are separated with an empty one.
     */
    @Test
    public void test1EmptyEvent() throws InterruptedException {
        doTest("sse/1");
    }

    /**
     * Tests a situation when 2 non-empty SSE events are separated with 2 empty ones.
     */
    @Test
    public void test2EmptyEvents() throws InterruptedException {
        doTest("sse/2");
    }

    /**
     * Tests a situation when 2 non-empty SSE events are separated with 3 empty ones.
     */
    @Test
    public void test3EmptyEvents() throws InterruptedException {
        doTest("sse/3");
    }

    private void doTest(String target) throws InterruptedException {
        List<String> receivedNames = new ArrayList<>();
        List<String> receivedData = new LinkedList<>();

        WebTarget sseTarget = target(target);
        CountDownLatch latch = new CountDownLatch(2);
        new EventSource(sseTarget) {
            @Override
            public void onEvent(InboundEvent inboundEvent) {
                receivedNames.add(inboundEvent.getName());
                try {
                    receivedData.add(new String(inboundEvent.getRawData(), "ASCII"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                latch.countDown();
            }
        };

        assertTrue(latch.await(5, TimeUnit.SECONDS));
        assertTrue(receivedNames.contains("e1"));
        assertTrue(receivedNames.contains("e2"));
        assertTrue(receivedData.contains("d1"));
        assertTrue(receivedData.contains("d2"));
    }

    @Path("/sse")
    public static class SseTestResource {

        @GET
        @Path("/1")
        @Produces("text/event-stream;charset=utf-8")
        public String send1EmptyEvent() {
            return "event: e1\r\n"
                    + "data: d1\r\n"
                    + "\r\n"
                    // end of e1
                    + "\r\n"
                    // end of an empty event
                    + "event: e2\r\n"
                    + "data: d2\r\n"
                    + "\r\n";
        }

        @GET
        @Path("/2")
        @Produces("text/event-stream;charset=utf-8")
        public String send2EmptyEvents() {
            return "event: e1\r\n"
                    + "data: d1\r\n"
                    + "\r\n"
                    // end of e1
                    + "\r\n"
                    // end of an empty event
                    + "\r\n"
                    // end of an empty event
                    + "event: e2\r\n"
                    + "data: d2\r\n"
                    + "\r\n";
        }

        @GET
        @Path("/3")
        @Produces("text/event-stream;charset=utf-8")
        public String send3EmptyEvents() {
            return "event: e1\r\n"
                    + "data: d1\r\n"
                    + "\r\n"
                    // end of e1
                    + "\r\n"
                    // end of an empty event
                    + "\r\n"
                    // end of an empty event
                    + "\r\n"
                    // end of an empty event
                    + "event: e2\r\n"
                    + "data: d2\r\n"
                    + "\r\n";
        }
    }
}
