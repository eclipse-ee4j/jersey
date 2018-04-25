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

import java.util.concurrent.CompletionStage;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

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

import javax.inject.Singleton;

import org.glassfish.jersey.client.ClientAsyncExecutor;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.server.ManagedAsyncExecutor;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.spi.ThreadPoolExecutorProvider;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Assert;
import org.junit.Test;

/**
 * Managed executor service injection and propagation into broadcaster test.
 *
 * @author Adam Lindenthal (adam.lindenthal at oracle.com)
 */
public class BroadcasterExecutorTest extends JerseyTest {

    private static final String THREAD_PREFIX = "custom-executor-thread";
    private static Logger LOGGER = Logger.getLogger(BroadcasterExecutorTest.class.getName());

    private static CountDownLatch closeLatch = new CountDownLatch(1);
    private static CountDownLatch txLatch = new CountDownLatch(2);

    private static boolean sendThreadOk = false;
    private static boolean onCompleteThreadOk = false;

    @Path("sse")
    @Singleton
    public static class SseResource {
        private final Sse sse;
        private SseBroadcaster broadcaster;

        public SseResource(@Context final Sse sse) {
            this.sse = sse;
            broadcaster = sse.newBroadcaster();
            System.out.println("Broadcaster created: " + broadcaster);
        }

        @GET
        @Produces(MediaType.SERVER_SENT_EVENTS)
        @Path("events")
        public void getServerSentEvents(@Context final SseEventSink eventSink, @Context final Sse sse) {

            // TODO JAX-RS 2.1
            broadcaster.register(new SseEventSink() {
                @Override
                public boolean isClosed() {
                    return eventSink.isClosed();
                }

                @Override
                public CompletionStage<?> send(OutboundSseEvent event) {
                    final String name = Thread.currentThread().getName();
                    LOGGER.info("onNext called with [" + event + "] from " + name);
                    sendThreadOk = name.startsWith(THREAD_PREFIX);
                    txLatch.countDown();
                    return eventSink.send(event);
                }

                @Override
                public void close() {
                    final String name = Thread.currentThread().getName();
                    LOGGER.info("onComplete called from " + name);
                    onCompleteThreadOk = name.startsWith(THREAD_PREFIX);
                    closeLatch.countDown();
                    eventSink.close();
                }
            });
        }

        @Path("push/{msg}")
        @GET
        public String pushMessage(@PathParam("msg") final String msg) {
            broadcaster.broadcast(sse.newEventBuilder().data(msg).build());
            return "Broadcasting message: " + msg;
        }

        @Path("close")
        @GET
        public String close() {
            broadcaster.close();
            return "Closed.";
        }
    }

    @Override
    protected Application configure() {
        final ResourceConfig rc = new ResourceConfig(SseResource.class);
        rc.property(ServerProperties.WADL_FEATURE_DISABLE, true);
        rc.register(new CustomManagedAsyncExecutorProvider());
        return rc;
    }

    @Override
    protected void configureClient(ClientConfig config) {
        config.register(new CustomClientAsyncExecutor());
    }

    @ManagedAsyncExecutor
    private static class CustomManagedAsyncExecutorProvider extends ThreadPoolExecutorProvider {
        CustomManagedAsyncExecutorProvider() {
            super("custom-executor-thread");
        }
    }

    @ClientAsyncExecutor
    private static class CustomClientAsyncExecutor extends ThreadPoolExecutorProvider {
        CustomClientAsyncExecutor() {
            super("custom-client-executor");
        }
    }

    @Test
    public void test() throws InterruptedException {
        final String[] onEventThreadName = {""};
        SseEventSource eventSource = SseEventSource
                .target(target().path("sse/events"))
                .build();

        eventSource.register((event) -> {
                    LOGGER.info("Event: " + event + " from: " + Thread.currentThread().getName());
                    onEventThreadName[0] = Thread.currentThread().getName();
                }
        );

        eventSource.open();

        target().path("sse/push/firstBroadcast").request().get(String.class);
        target().path("sse/push/secondBroadcast").request().get(String.class);
        Assert.assertTrue("txLatch time-outed.", txLatch.await(2000, TimeUnit.MILLISECONDS));

        target().path("sse/close").request().get();
        Assert.assertTrue("closeLatch time-outed.", closeLatch.await(2000, TimeUnit.MILLISECONDS));

        Assert.assertTrue("send either not invoked at all or from wrong thread", sendThreadOk);
        Assert.assertTrue("onComplete either not invoked at all or from wrong thread", onCompleteThreadOk);

        Assert.assertTrue("Client event called from wrong thread ( " + onEventThreadName[0] + ")",
               onEventThreadName[0].startsWith("custom-client-executor"));
    }
}
