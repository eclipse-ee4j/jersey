/*
 * Copyright (c) 2020, 2024 Oracle and/or its affiliates. All rights reserved.
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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Singleton;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.sse.Sse;
import jakarta.ws.rs.sse.SseBroadcaster;
import jakarta.ws.rs.sse.SseEventSink;
import jakarta.ws.rs.sse.SseEventSource;
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
        public void send(@Context SseEventSink sink, @Context Sse sse) throws Exception {
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
            source.register((event) -> {
                sb.append(event.readData());
                latch.countDown();
            });
            source.open();

            latch.await(WAIT_TIME, TimeUnit.MILLISECONDS);
        }

        Assertions.assertEquals("AAAAAAAAAA", sb.toString());
        Assertions.assertEquals(0, latch.getCount());
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

        Assertions.assertEquals(0, clientOne.messageLatch.getCount());
        Assertions.assertEquals(0, clientTwo.messageLatch.getCount());

        Assertions.assertEquals(BroadcasterResource.WELCOME + PALINDROME + PALINDROME, clientOne.message.toString());
        Assertions.assertEquals(BroadcasterResource.WELCOME + PALINDROME + PALINDROME, clientTwo.message.toString());

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
            source.register((event) -> {
                message.append(event.readData());
                latch.countDown();
                messageLatch.countDown();
            });
            source.open();

            latch.await(WAIT_TIME, TimeUnit.MILLISECONDS);
            Assertions.assertEquals(0, latch.getCount());
        }

        private void broadcast() {
            try (Response r = target.path("broadcast/broadcast")
                    .request().buildPost(Entity.entity(PALINDROME, MediaType.TEXT_PLAIN)).invoke()) {
                Assertions.assertEquals(204, r.getStatus());
            }
        }

        @Override
        public void close() {
            source.close();
        }
    }
}
