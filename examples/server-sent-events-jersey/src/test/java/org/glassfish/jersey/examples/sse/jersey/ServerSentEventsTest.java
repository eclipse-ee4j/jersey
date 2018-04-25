/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.sse.jersey;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.media.sse.EventInput;
import org.glassfish.jersey.media.sse.EventListener;
import org.glassfish.jersey.media.sse.EventSource;
import org.glassfish.jersey.media.sse.InboundEvent;
import org.glassfish.jersey.media.sse.SseFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * SSE example resources test.
 *
 * @author Pavel Bucek (pavel.bucek at oracle.com)
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public class ServerSentEventsTest extends JerseyTest {
    // TODO - due to JdkConnector migration this was radically reduced. It deadlocks with 25 clients, find out why!
    private static final int MAX_CLIENTS = 10;

    @Override
    protected Application configure() {
        // enable(TestProperties.LOG_TRAFFIC);
        return new ResourceConfig(ServerSentEventsResource.class, DomainResource.class, SseFeature.class);
    }

    @Override
    protected void configureClient(ClientConfig config) {
        config.property(ClientProperties.ASYNC_THREADPOOL_SIZE, MAX_CLIENTS + 2);
    }

    /**
     * Test consuming a single SSE event via event source.
     *
     * @throws Exception in case of a failure during the test execution.
     */
    @Test
    public void testEventSource() throws Exception {

        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<String> message = new AtomicReference<String>();
        final EventSource eventSource = new EventSource(target().path(App.ROOT_PATH)) {
            @Override
            public void onEvent(InboundEvent inboundEvent) {
                try {
                    final String value = inboundEvent.readData();
                    message.set(value);
                    latch.countDown();
                } catch (ProcessingException e) {
                    e.printStackTrace();
                }
            }
        };

        target().path(App.ROOT_PATH).request().post(Entity.text("message"));

        try {
            assertTrue("Waiting for message to be delivered has timed out.",
                    latch.await(5 * getAsyncTimeoutMultiplier(), TimeUnit.SECONDS));
        } finally {
            eventSource.close();
        }
        assertThat("Unexpected SSE event data value.", message.get(), equalTo("message"));
    }

    /**
     * Test consuming multiple SSE events sequentially using event input.
     *
     * @throws Exception in case of a failure during the test execution.
     */
    @Test
    public void testInboundEventReader() throws Exception {
        final int MAX_MESSAGES = 5;
        final CountDownLatch startLatch = new CountDownLatch(1);

        final ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            final Future<List<String>> futureMessages =
                    executor.submit(new Callable<List<String>>() {

                        @Override
                        public List<String> call() throws Exception {
                            final EventInput eventInput = target(App.ROOT_PATH).register(SseFeature.class)
                                    .request().get(EventInput.class);

                            startLatch.countDown();

                            final List<String> messages = new ArrayList<String>(MAX_MESSAGES);
                            try {
                                for (int i = 0; i < MAX_MESSAGES; i++) {
                                    InboundEvent event = eventInput.read();
                                    messages.add(event.readData());
                                }
                            } finally {
                                if (eventInput != null) {
                                    eventInput.close();
                                }
                            }

                            return messages;
                        }
                    });

            assertTrue("Waiting for receiver thread to start has timed out.",
                    startLatch.await(5, TimeUnit.SECONDS));

            for (int i = 0; i < MAX_MESSAGES; i++) {
                target(App.ROOT_PATH).request().post(Entity.text("message " + i));
            }

            int i = 0;
            for (String message : futureMessages.get(5000, TimeUnit.SECONDS)) {
                assertThat("Unexpected SSE event data value.", message, equalTo("message " + i++));
            }
        } finally {
            executor.shutdownNow();
        }
    }

    /**
     * Test receiving all streamed messages in parallel by multiple event sources.
     *
     * @throws Exception in case of a failure during the test execution.
     */
    @Test
    public void testCreateDomain() throws Exception {
        final int MESSAGE_COUNT = 6;

        final Response response = target().path("domain/start")
                .queryParam("testSources", MAX_CLIENTS)
                .request().post(Entity.text("data"), Response.class);

        assertThat("Unexpected start domain response status code.",
                response.getStatus(), equalTo(Response.Status.CREATED.getStatusCode()));

        final Map<Integer, Integer> messageCounts = new ConcurrentHashMap<Integer, Integer>(MAX_CLIENTS);
        final CountDownLatch doneLatch = new CountDownLatch(MAX_CLIENTS);
        final EventSource[] sources = new EventSource[MAX_CLIENTS];

        final String processUriString = target().getUri().relativize(response.getLocation()).toString();

        final WebTarget sseTarget = target().path(processUriString).queryParam("testSource", "true");
        for (int i = 0; i < MAX_CLIENTS; i++) {
            final int id = i;
            sources[id] = EventSource.target(sseTarget).build();
            sources[id].register(new EventListener() {

                private final AtomicInteger messageCount = new AtomicInteger(0);

                @Override
                public void onEvent(InboundEvent inboundEvent) {
                    messageCount.incrementAndGet();
                    final String message = inboundEvent.readData(String.class);
                    if ("done".equals(message)) {
                        messageCounts.put(id, messageCount.get());
                        doneLatch.countDown();
                    }
                }
            });
            sources[i].open();
        }

        doneLatch.await(5 * getAsyncTimeoutMultiplier(), TimeUnit.SECONDS);

        for (EventSource source : sources) {
            source.close();
        }

        for (int i = 0; i < MAX_CLIENTS; i++) {
            final Integer count = messageCounts.get(i);
            assertThat("Final message not received by event source " + i, count, notNullValue());
            assertThat("Unexpected number of messages received by event source " + i,
                    count, equalTo(MESSAGE_COUNT));
        }
    }
}
