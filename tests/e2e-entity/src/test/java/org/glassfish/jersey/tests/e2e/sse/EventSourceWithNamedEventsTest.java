/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.Produces;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;

import javax.inject.Singleton;

import org.glassfish.jersey.media.sse.EventListener;
import org.glassfish.jersey.media.sse.EventOutput;
import org.glassfish.jersey.media.sse.EventSource;
import org.glassfish.jersey.media.sse.InboundEvent;
import org.glassfish.jersey.media.sse.OutboundEvent;
import org.glassfish.jersey.media.sse.SseFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests handling of SSEs with name defined in {@link EventSource}.
 *
 * @author Miroslav Fuksa
 *
 */
public class EventSourceWithNamedEventsTest extends JerseyTest {

    public static final String SSE_NAME = "message-to-client";

    @Override
    protected Application configure() {
        final ResourceConfig resourceConfig = new ResourceConfig(SseResource.class, SseFeature.class);
        return resourceConfig;
    }

    public static final int MSG_COUNT = 10;
    private static final CountDownLatch latch = new CountDownLatch(MSG_COUNT);

    @Path("events")
    @Singleton
    public static class SseResource {

        @GET
        @Produces(SseFeature.SERVER_SENT_EVENTS)
        public EventOutput getServerSentEvents() {
            final EventOutput eventOutput = new EventOutput();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        int i = 0;
                        while (latch.getCount() > 0) {

                            // send message with name "message-to-client" -> should be read by the client
                            eventOutput.write(new OutboundEvent.Builder()
                                    .name("message-to-client")
                                    .mediaType(MediaType.TEXT_PLAIN_TYPE)
                                    .data(Integer.class, i)
                                    .build());

                            // send another event with name "foo" -> should be ignored by the client
                            eventOutput.write(new OutboundEvent.Builder()
                                    .name("foo")
                                    .mediaType(MediaType.TEXT_PLAIN_TYPE)
                                    .data(String.class, "bar")
                                    .build());

                            // send another un-mamed event -> should be ignored by the client
                            eventOutput.write(new OutboundEvent.Builder()
                                    .mediaType(MediaType.TEXT_PLAIN_TYPE)
                                    .data(String.class, "baz")
                                    .build());
                            latch.countDown();
                            i++;
                        }

                    } catch (IOException e) {
                        throw new RuntimeException("Error when writing the event.", e);
                    } finally {
                        try {
                            eventOutput.close();
                        } catch (IOException ioClose) {
                            throw new RuntimeException("Error when closing the event output.", ioClose);
                        }
                    }
                }
            }).start();
            return eventOutput;
        }
    }


    @Test
    public void testWithEventSource() throws IOException, NoSuchAlgorithmException, InterruptedException {
        final WebTarget endpoint = target().register(SseFeature.class).path("events");
        EventSource eventSource = EventSource.target(endpoint).build();
        final CountDownLatch count = new CountDownLatch(MSG_COUNT);

        final EventListener listener = new EventListener() {
            @Override
            public void onEvent(InboundEvent inboundEvent) {
                try {
                    final Integer data = inboundEvent.readData(Integer.class);
                    System.out.println(inboundEvent.getName() + "; " + data);
                    Assert.assertEquals(SSE_NAME, inboundEvent.getName());
                    Assert.assertEquals(MSG_COUNT - count.getCount(), data.intValue());
                    count.countDown();
                } catch (ProcessingException ex) {
                    throw new RuntimeException("Error when deserializing of data.", ex);
                }
            }
        };
        eventSource.register(listener, "message-to-client");
        eventSource.open();
        final boolean sent = latch.await(5 * getAsyncTimeoutMultiplier(), TimeUnit.SECONDS);
        Assert.assertTrue("Awaiting for SSE message has timeout. Not all message were sent.", sent);
        final boolean handled = count.await(5 * getAsyncTimeoutMultiplier(), TimeUnit.SECONDS);
        Assert.assertTrue("Awaiting for SSE message has timeout. Not all message were handled by the listener.", handled);
    }
}
