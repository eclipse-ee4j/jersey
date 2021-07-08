/*
 * Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.jdk.connector.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.internal.guava.ThreadFactoryBuilder;
import org.glassfish.jersey.jdk.connector.JdkConnectorProperties;
import org.glassfish.jersey.jdk.connector.JdkConnectorProvider;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.Test;

public class StressTest extends JerseyTest {

    private static final int PARALLELISM = 10;
    private static final int IDLE_TIMEOUT = 50;
    private static final int ITERATIONS = 1000;
    private static CountDownLatch requests;
    private static CountDownLatch latch;

    @Path("/test")
    public static class TestResource {
        @GET
        @Path("/1")
        public String test1() throws InterruptedException {
            requests.countDown();
            if (latch.await(10, TimeUnit.SECONDS)) {
                return "test1";
            } else {
                throw new IllegalStateException("Timeout");
            }
        }

        @GET
        @Path("/2")
        public String test2() {
            return "test2";
        }
    }

    @Override
    protected Application configure() {
        enable(TestProperties.LOG_TRAFFIC);
        enable(TestProperties.DUMP_ENTITY);
        return new ResourceConfig(TestResource.class);
    }

    @Override
    protected void configureClient(ClientConfig config) {
        config.property(JdkConnectorProperties.MAX_CONNECTIONS_PER_DESTINATION, PARALLELISM);
        config.property(JdkConnectorProperties.CONNECTION_IDLE_TIMEOUT, IDLE_TIMEOUT);
        config.connectorProvider(new JdkConnectorProvider());
    }

    @Test
    public void hangAllRequestsStatus200() throws InterruptedException, ExecutionException {
        ExecutorService executor = Executors.newFixedThreadPool(PARALLELISM,
                new ThreadFactoryBuilder().setNameFormat("client-%d").build());
        for (int i = 0; i < ITERATIONS; i++) {
            requests = new CountDownLatch(PARALLELISM);
            latch = new CountDownLatch(1);
            List<Future<Response>> responses = new ArrayList<>();
            for (int j = 0; j < PARALLELISM; j++) {
                Future<Response> future = executor.submit(() -> target("/test/1").request().get());
                responses.add(future);
            }
            assertTrue(requests.await(20, TimeUnit.SECONDS));
            latch.countDown();
            for (Future<Response> response : responses) {
                assertEquals(200, response.get().getStatus());
            }
        }
        executor.shutdown();
        assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));
    }

    @Test
    public void randomnessStatus200() throws InterruptedException, ExecutionException {
        ExecutorService executor = Executors.newFixedThreadPool(PARALLELISM,
                new ThreadFactoryBuilder().setNameFormat("client-%d").build());
        for (int i = 0; i < ITERATIONS; i++) {
            System.out.println("Iteration " + i);
            List<Future<Response>> responses = new ArrayList<>();
            for (int j = 0; j < 100; j++) {
                Future<Response> future = executor.submit(() -> target("/test/2").request().get());
                responses.add(future);
            }
            for (Future<Response> response : responses) {
                assertEquals(200, response.get().getStatus());
            }
        }
        executor.shutdown();
        assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));
    }

    @Test
    public void syncTest() {
        Response response = target("/test/2").request().get();
        assertEquals(200, response.getStatus());
    }

    @Test
    public void asyncTest() throws InterruptedException, ExecutionException {
        Future<Response> response = target("/test/2").request().async().get();
        assertEquals(200, response.get().getStatus());
    }
}
