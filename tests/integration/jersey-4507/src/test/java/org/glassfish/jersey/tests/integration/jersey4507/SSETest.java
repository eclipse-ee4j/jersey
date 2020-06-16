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

package org.glassfish.jersey.tests.integration.jersey4507;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientLifecycleListener;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.examples.sse.jersey.App;
import org.glassfish.jersey.examples.sse.jersey.DomainResource;
import org.glassfish.jersey.examples.sse.jersey.ServerSentEventsResource;
import org.glassfish.jersey.media.sse.EventInput;
import org.glassfish.jersey.media.sse.InboundEvent;
import org.glassfish.jersey.media.sse.SseFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.CoreMatchers.equalTo;

public class SSETest extends JerseyTest {
    private static final int MAX_CLIENTS = 10;
    private static final int COUNT = 30;
    private static final AtomicInteger atomicInteger = new AtomicInteger(0);
    private static final CountDownLatch closeLatch = new CountDownLatch(COUNT);

    @Override
    protected Application configure() {
        // enable(TestProperties.LOG_TRAFFIC);
        return new ResourceConfig(ServerSentEventsResource.class, DomainResource.class, SseFeature.class);
    }

    @Override
    protected void configureClient(ClientConfig config) {
        config.property(ClientProperties.ASYNC_THREADPOOL_SIZE, MAX_CLIENTS + 2);
        config.register(new ClientRuntimeCloseVerifier());
    }

    /**
     * Test consuming multiple SSE events sequentially using event input.
     *
     * @throws Exception in case of a failure during the test execution.
     */
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

            Assert.assertTrue("Waiting for receiver thread to start has timed out.",
                    startLatch.await(15000, TimeUnit.SECONDS));

            for (int i = 0; i < MAX_MESSAGES; i++) {
                target(App.ROOT_PATH).request().post(Entity.text("message " + i));
            }

            int i = 0;
            for (String message : futureMessages.get(50, TimeUnit.SECONDS)) {
                Assert.assertThat("Unexpected SSE event data value.", message, equalTo("message " + i++));
            }
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    public void testInboundEventReaderMultiple() throws Exception {
        for (int i = 0; i != COUNT; i++) {
            testInboundEventReader();
        }

        System.gc();
        closeLatch.await(15_000, TimeUnit.MILLISECONDS);
        // One ClientConfig is on the Client
        // + COUNT of them is created by .register(SseFeature.class)
        Assert.assertEquals(COUNT + 1, atomicInteger.get());
        Assert.assertEquals(0, closeLatch.getCount());
    }



    public static class ClientRuntimeCloseVerifier implements ClientLifecycleListener {

        @Override
        public void onInit() {
            atomicInteger.incrementAndGet();
        }

        @Override
        public void onClose() {
            closeLatch.countDown();
        }
    }
}
