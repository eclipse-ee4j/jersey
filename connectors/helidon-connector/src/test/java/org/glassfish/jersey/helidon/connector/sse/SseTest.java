/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.helidon.connector.sse;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.helidon.connector.HelidonConnectorProvider;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Assert;
import org.junit.Test;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.sse.Sse;
import javax.ws.rs.sse.SseBroadcaster;
import javax.ws.rs.sse.SseEventSink;
import javax.ws.rs.sse.SseEventSource;
import java.io.Closeable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class SseTest extends JerseyTest {

    private static String PALINDROME = "neveroddoreven";
    private static int WAIT_TIME = 5000;

    @Path("simple")
    public static class SimpleSseResource {
        @GET
        @Produces(MediaType.SERVER_SENT_EVENTS)
        public void send(@Context SseEventSink sink, @Context Sse sse) {
            try (SseEventSink s = sink) {
                for (int i = 0; i != 10; i++) {
                    s.send(sse.newEvent("A"));
                }
            }
        }
    }

    @Path("broadcast")
    @Singleton
    public static class BroadcasterResource {
        private static final String WELCOME = "Welcome";

        @Context
        private Sse sse;

        private static SseBroadcaster sseBroadcaster;

        @PostConstruct
        public void init() {
            System.out.println("INIT");
            sseBroadcaster = sse.newBroadcaster();
        }

        @GET
        @Path("register")
        @Produces(MediaType.SERVER_SENT_EVENTS)
        public void register(@Context SseEventSink sink) {
            sseBroadcaster.register(sink);
            sink.send(sse.newEvent(WELCOME));
        }

        @POST
        @Path("broadcast")
        @Consumes(MediaType.TEXT_PLAIN)
        public void broadcast(String event) {
            sseBroadcaster.broadcast(sse.newEvent(event));
        }
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(SimpleSseResource.class, BroadcasterResource.class);
    }

    @Override
    protected void configureClient(ClientConfig config) {
        config.connectorProvider(new HelidonConnectorProvider());
        //config.property("jersey.config.helidon.client.entity.type", "OUTPUT_STREAM_PUBLISHER");
    }

    @Test
    public void testSend() throws InterruptedException {
        final StringBuilder sb = new StringBuilder();
        final CountDownLatch latch = new CountDownLatch(10);
        try (SseEventSource source = SseEventSource.target(target().path("simple")).build()) {
            source.register((event) -> sb.append(event.readData()));
            source.register((event) -> latch.countDown());
            source.open();

            latch.await(WAIT_TIME, TimeUnit.MILLISECONDS);
        }

        Assert.assertEquals("AAAAAAAAAA", sb.toString());
        Assert.assertEquals(0, latch.getCount());
    }

    @Test
    public void testBroadcast() throws InterruptedException {
        final BroadcasterClient clientOne = new BroadcasterClient(target());
        final BroadcasterClient clientTwo = new BroadcasterClient(target());

        clientOne.register();
        clientTwo.register();

        clientOne.broadcast();
        clientTwo.broadcast();

        clientOne.messageLatch.await(WAIT_TIME, TimeUnit.MILLISECONDS);
        clientTwo.messageLatch.await(WAIT_TIME, TimeUnit.MILLISECONDS);

        Assert.assertEquals(0, clientOne.messageLatch.getCount());
        Assert.assertEquals(0, clientTwo.messageLatch.getCount());

        Assert.assertEquals(BroadcasterResource.WELCOME + PALINDROME + PALINDROME, clientOne.message.toString());
        Assert.assertEquals(BroadcasterResource.WELCOME + PALINDROME + PALINDROME, clientTwo.message.toString());

        clientOne.close();
        clientTwo.close();
    }

    private static class BroadcasterClient implements Closeable {
        private final WebTarget target;
        private final CountDownLatch messageLatch = new CountDownLatch(3);
        private final SseEventSource source;
        private final StringBuilder message = new StringBuilder();

        private BroadcasterClient(WebTarget target) {
            this.target = target;
            source = SseEventSource.target(target.path("broadcast/register")).build();
        }

        private void register() throws InterruptedException {
            final CountDownLatch latch = new CountDownLatch(1);
            source.register((event) -> message.append(event.readData()));
            source.register((event) -> latch.countDown());
            source.register((event) -> messageLatch.countDown());
            source.open();

            latch.await(WAIT_TIME, TimeUnit.MILLISECONDS);
            Assert.assertEquals(0, latch.getCount());
        }

        private void broadcast() {
            try (Response r = target.path("broadcast/broadcast")
                    .request().buildPost(Entity.entity(PALINDROME, MediaType.TEXT_PLAIN)).invoke()) {
                Assert.assertEquals(204, r.getStatus());
            }
        }

        @Override
        public void close() {
            source.close();
        }
    }
}
