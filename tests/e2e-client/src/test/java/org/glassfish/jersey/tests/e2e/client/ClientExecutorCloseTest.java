/*
 * Copyright (c) 2015, 2022 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.e2e.client;

import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import javax.ws.rs.sse.SseEventSource;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Petr Janouch
 */
public class ClientExecutorCloseTest extends JerseyTest {
    private static CountDownLatch cdl = new CountDownLatch(2);
    private static boolean schedulerFound = false;

    /**
     * Tests that closing a client shuts down a corresponding client async executor service.
     */
    @Test
    @Disabled("Jersey uses ForkJoin common pool by default, which shouldn't be closed when client closes.")
    public void testCloseAsyncExecutor() throws InterruptedException {
        assertFalse(clientExecutorThreadPresent());
        target("resource").request().async().get();
        final SseEventSource eventSource = SseEventSource
                .target(target("resource/fail"))
                .reconnectingEvery(11, TimeUnit.MILLISECONDS)
                .build();
        eventSource.register(System.out::println);
        eventSource.open();
        assertTrue(cdl.await(5000, TimeUnit.MILLISECONDS), "Waiting for eventSource to open time-outed");
        assertTrue(clientExecutorThreadPresent(), "Client async executor thread not found.");
        assertTrue(schedulerFound, "Scheduler thread not found.");
        client().close();
        assertFalse(clientExecutorThreadPresent(),
                "Client async executor thread should have been already removed.");
        assertFalse(clientSchedulerThreadPresent(),
                "Client background scheduler thread should have been already removed.");
    }

    private boolean clientExecutorThreadPresent() {
        Set<Thread> threads = Thread.getAllStackTraces().keySet();
        return threads.stream().map(Thread::getName).anyMatch(name -> name.contains("jersey-client-async-executor"));
    }

    private static boolean clientSchedulerThreadPresent() {
        Set<Thread> threads = Thread.getAllStackTraces().keySet();
        for (Thread thread : threads) {
            if (thread.getName().contains("jersey-client-background-scheduler")) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(Resource.class);
    }

    @Path("resource")
    public static class Resource {

        @GET
        public String getHello() {
            return "Hello";
        }

        @GET
        @Path("fail")
        public Response fail() {
            // should return false on first (regular) connect and true on reconnect
            schedulerFound = clientSchedulerThreadPresent();
            cdl.countDown();
            // simulate unsuccessful connect attempt -> force reconnect (eventSource will submit a task into scheduler)
            return Response.status(503).build();
        }
    }
}
