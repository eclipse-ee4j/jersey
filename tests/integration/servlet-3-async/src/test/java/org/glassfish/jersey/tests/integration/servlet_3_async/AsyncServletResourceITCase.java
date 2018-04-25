/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.integration.servlet_3_async;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.internal.guava.ThreadFactoryBuilder;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.process.JerseyProcessingUncaughtExceptionHandler;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.external.ExternalTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.glassfish.jersey.test.spi.TestContainerFactory;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Asynchronous servlet-deployed resource test.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public class AsyncServletResourceITCase extends JerseyTest {
    private static final Logger LOGGER = Logger.getLogger(AsyncServletResourceITCase.class.getName());

    private static class ResponseRecord {
        final int status;
        final String message;

        private ResponseRecord(int status, String message) {
            this.status = status;
            this.message = message;
        }

        @Override
        public String toString() {
            return status + " : \"" + message + '\"';
        }
    }

    @Override
    protected Application configure() {
        return new Application();
    }

    @Override
    protected TestContainerFactory getTestContainerFactory() throws TestContainerException {
        return new ExternalTestContainerFactory();
    }

    /**
     * Test asynchronous servlet-deployed resource.
     *
     * @throws InterruptedException in case the waiting for all requests to complete was interrupted.
     */
    @Test
    public void testAsyncServlet() throws InterruptedException {
        final WebTarget resourceTarget = target("async");
        resourceTarget.register(LoggingFeature.class);
        final String expectedResponse = AsyncServletResource.HELLO_ASYNC_WORLD;

        final int MAX_MESSAGES = 50;
        final int LATCH_WAIT_TIMEOUT = 10 * getAsyncTimeoutMultiplier();
        final boolean debugMode = false;
        final boolean sequentialGet = false;
        final Object sequentialGetLock = new Object();

        final ExecutorService executor = Executors.newCachedThreadPool(new ThreadFactoryBuilder()
                .setNameFormat("async-resource-test-%d")
                .setUncaughtExceptionHandler(new JerseyProcessingUncaughtExceptionHandler())
                .build());

        final Map<Integer, ResponseRecord> getResponses = new ConcurrentHashMap<Integer, ResponseRecord>();

        final CountDownLatch getRequestLatch = new CountDownLatch(MAX_MESSAGES);

        try {
            for (int i = 0; i < MAX_MESSAGES; i++) {
                final int requestId = i;
                executor.submit(new Runnable() {

                    @Override
                    public void run() {
                        //noinspection PointlessBooleanExpression,ConstantConditions
                        if (debugMode || sequentialGet) {
                            synchronized (sequentialGetLock) {
                                get();
                            }
                        } else {
                            get();
                        }
                    }

                    private void get() {
                        try {
                            final Response response = resourceTarget.request().get();
                            getResponses.put(
                                    requestId,
                                    new ResponseRecord(response.getStatus(), response.readEntity(String.class)));
                        } catch (Throwable t) {
                            t.printStackTrace();
                        } finally {
                            getRequestLatch.countDown();
                        }
                    }
                });
            }

            //noinspection ConstantConditions
            if (debugMode) {
                getRequestLatch.await();
            } else {
                assertTrue("Waiting for all GET requests to complete has timed out.", getRequestLatch.await(LATCH_WAIT_TIMEOUT,
                        TimeUnit.SECONDS));
            }
        } finally {
            executor.shutdownNow();
        }

        StringBuilder messageBuilder = new StringBuilder();
        for (Map.Entry<Integer, ResponseRecord> getResponseEntry : getResponses.entrySet()) {
            messageBuilder.append("GET response for message ")
                    .append(getResponseEntry.getKey()).append(": ")
                    .append(getResponseEntry.getValue().toString()).append('\n');
        }
        LOGGER.info(messageBuilder.toString());

        assertEquals(MAX_MESSAGES, getResponses.size());
        for (Map.Entry<Integer, ResponseRecord> entry : getResponses.entrySet()) {
            assertEquals(
                    "Unexpected GET response status for request " + entry.getKey(),
                    200, entry.getValue().status);
            assertEquals(
                    "Unexpected GET response message for request " + entry.getKey(),
                    expectedResponse, entry.getValue().message);
        }
    }

    /**
     * Test canceling of an async request to a servlet-deployed resource.
     *
     * @throws InterruptedException in case the waiting for all requests to complete was interrupted.
     */
    @Test
    public void testAsyncRequestCanceling() throws InterruptedException {
        final WebTarget resourceTarget = target("async/canceled");
        resourceTarget.register(LoggingFeature.class);

        final int MAX_MESSAGES = 10;
        final int LATCH_WAIT_TIMEOUT = 10 * getAsyncTimeoutMultiplier();
        final boolean debugMode = false;
        final boolean sequentialGet = false;
        final boolean sequentialPost = false;
        final Object sequentialGetLock = new Object();
        final Object sequentialPostLock = new Object();

        final ExecutorService executor = Executors.newCachedThreadPool(new ThreadFactoryBuilder()
                .setNameFormat("async-canceled-resource-test-%d")
                .setUncaughtExceptionHandler(new JerseyProcessingUncaughtExceptionHandler())
                .build());

        final Map<Integer, String> postResponses = new ConcurrentHashMap<Integer, String>();
        final Map<Integer, String> getResponses = new ConcurrentHashMap<Integer, String>();

        final CountDownLatch postRequestLatch = new CountDownLatch(MAX_MESSAGES);
        final CountDownLatch getRequestLatch = new CountDownLatch(MAX_MESSAGES);

        try {
            for (int i = 0; i < MAX_MESSAGES; i++) {
                final int requestId = i;
                executor.submit(new Runnable() {

                    @Override
                    public void run() {
                        //noinspection PointlessBooleanExpression,ConstantConditions
                        if (debugMode || sequentialGet) {
                            synchronized (sequentialGetLock) {
                                get();
                            }
                        } else {
                            get();
                        }
                    }

                    private void get() {
                        try {
                            final String response = resourceTarget.queryParam("id", requestId).request().get(String.class);
                            getResponses.put(requestId, response);
                        } catch (WebApplicationException ex) {
                            final Response response = ex.getResponse();
                            getResponses.put(requestId, response.getStatus() + ": " + response.readEntity(String.class));
                        } finally {
                            getRequestLatch.countDown();
                        }
                    }
                });
                executor.submit(new Runnable() {

                    @Override
                    public void run() {
                        //noinspection PointlessBooleanExpression,ConstantConditions
                        if (debugMode || sequentialPost) {
                            synchronized (sequentialPostLock) {
                                post();
                            }
                        } else {
                            post();
                        }
                    }

                    private void post() throws ProcessingException {
                        try {
                            final String response = resourceTarget.request().post(Entity.text("" + requestId), String.class);
                            postResponses.put(requestId, response);
                        } finally {
                            postRequestLatch.countDown();
                        }
                    }
                });
            }

            //noinspection ConstantConditions
            if (debugMode) {
                postRequestLatch.await();
                getRequestLatch.await();
            } else {
                assertTrue("Waiting for all POST requests to complete has timed out.",
                        postRequestLatch.await(LATCH_WAIT_TIMEOUT, TimeUnit.SECONDS));
                assertTrue("Waiting for all GET requests to complete has timed out.", getRequestLatch.await(LATCH_WAIT_TIMEOUT,
                        TimeUnit.SECONDS));
            }
        } finally {
            executor.shutdownNow();
        }

        StringBuilder messageBuilder = new StringBuilder();
        for (Map.Entry<Integer, String> postResponseEntry : postResponses.entrySet()) {
            messageBuilder.append("POST response for message ")
                    .append(postResponseEntry.getKey()).append(": ")
                    .append(postResponseEntry.getValue()).append('\n');
        }
        messageBuilder.append('\n');
        for (Map.Entry<Integer, String> getResponseEntry : getResponses.entrySet()) {
            messageBuilder.append("GET response for message ")
                    .append(getResponseEntry.getKey()).append(": ")
                    .append(getResponseEntry.getValue()).append('\n');
        }
        LOGGER.info(messageBuilder.toString());

        assertEquals(MAX_MESSAGES, postResponses.size());
        for (Map.Entry<Integer, String> postResponseEntry : postResponses.entrySet()) {
            assertTrue("Unexpected POST notification response for message " + postResponseEntry.getKey(),
                    postResponseEntry.getValue().startsWith(AsyncServletResource.CANCELED));
        }

        assertEquals(MAX_MESSAGES, getResponses.size());
        final Collection<Integer> getResponseKeys = getResponses.keySet();
        for (int i = 0; i < MAX_MESSAGES; i++) {
            assertTrue("Detected a GET message response loss: " + i, getResponseKeys.contains(i));
            final String getResponseEntry = getResponses.get(i);
            assertTrue("Unexpected canceled GET response status for request " + i,
                    getResponseEntry.startsWith("503: "));
        }
    }
}
