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

package org.glassfish.jersey.tests.e2e.client.httpurlconnector;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.client.AsyncInvoker;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.container.TimeoutHandler;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.HttpUrlConnectorProvider;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.hamcrest.Matchers;
import org.junit.Ignore;
import org.junit.Test;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Asynchronous connector test.
 *
 * @author Arul Dhesiaseelan (aruld at acm.org)
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public class AsyncTest extends JerseyTest {

    private static final Logger LOGGER = Logger.getLogger(AsyncTest.class.getName());
    private static final String PATH = "async";

    /**
     * Asynchronous test resource.
     */
    @Path(PATH)
    public static class AsyncResource {
        /**
         * Typical long-running operation duration.
         */
        public static final long OPERATION_DURATION = 1000;

        /**
         * Long-running asynchronous post.
         *
         * @param asyncResponse async response.
         * @param id            post request id (received as request payload).
         */
        @POST
        public void asyncPost(@Suspended final AsyncResponse asyncResponse, final String id) {
            LOGGER.info("Long running post operation called with id " + id + " on thread " + Thread.currentThread().getName());
            new Thread(new Runnable() {

                @Override
                public void run() {
                    final String result = veryExpensiveOperation();
                    asyncResponse.resume(result);
                }

                private String veryExpensiveOperation() {
                    // ... very expensive operation that typically finishes within 1 seconds, simulated using sleep()
                    try {
                        Thread.sleep(OPERATION_DURATION);
                        return "DONE-" + id;
                    } catch (final InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return "INTERRUPTED-" + id;
                    } finally {
                        LOGGER.info("Long running post operation finished on thread " + Thread.currentThread().getName());
                    }
                }
            }, "async-post-runner-" + id).start();
        }

        /**
         * Long-running async get request that times out.
         *
         * @param asyncResponse async response.
         */
        @GET
        @Path("timeout")
        public void asyncGetWithTimeout(@Suspended final AsyncResponse asyncResponse) {
            LOGGER.info("Async long-running get with timeout called on thread " + Thread.currentThread().getName());
            asyncResponse.setTimeoutHandler(new TimeoutHandler() {

                @Override
                public void handleTimeout(final AsyncResponse asyncResponse) {
                    asyncResponse.resume(Response.status(Response.Status.SERVICE_UNAVAILABLE)
                            .entity("Operation time out.").build());
                }
            });
            asyncResponse.setTimeout(1, TimeUnit.SECONDS);

            new Thread(new Runnable() {

                @Override
                public void run() {
                    final String result = veryExpensiveOperation();
                    asyncResponse.resume(result);
                }

                private String veryExpensiveOperation() {
                    // very expensive operation that typically finishes within 1 second but can take up to 5 seconds,
                    // simulated using sleep()
                    try {
                        Thread.sleep(5 * OPERATION_DURATION);
                        return "DONE";
                    } catch (final InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return "INTERRUPTED";
                    } finally {
                        LOGGER.info("Async long-running get with timeout finished on thread " + Thread.currentThread().getName());
                    }
                }
            }).start();
        }

    }

    @Override
    protected Application configure() {
        return new ResourceConfig(AsyncResource.class, ThreadPoolResource.class)
                .register(new LoggingFeature(LOGGER, LoggingFeature.Verbosity.PAYLOAD_ANY));
    }

    @Override
    protected void configureClient(final ClientConfig config) {
        config.register(new LoggingFeature(LOGGER, LoggingFeature.Verbosity.PAYLOAD_ANY));
        config.connectorProvider(new HttpUrlConnectorProvider());
    }

    /**
     * Test asynchronous POST.
     *
     * Send 3 async POST requests and wait to receive the responses. Check the response content and
     * assert that the operation did not take more than twice as long as a single long operation duration
     * (this ensures async request execution).
     *
     * @throws Exception in case of a test error.
     */
    @Test
    public void testAsyncPost() throws Exception {
        final long tic = System.currentTimeMillis();

        // Submit requests asynchronously.
        final Future<Response> rf1 = target(PATH).request().async().post(Entity.text("1"));
        final Future<Response> rf2 = target(PATH).request().async().post(Entity.text("2"));
        final Future<Response> rf3 = target(PATH).request().async().post(Entity.text("3"));
        // get() waits for the response
        final String r1 = rf1.get().readEntity(String.class);
        final String r2 = rf2.get().readEntity(String.class);
        final String r3 = rf3.get().readEntity(String.class);

        final long toc = System.currentTimeMillis();

        assertEquals("DONE-1", r1);
        assertEquals("DONE-2", r2);
        assertEquals("DONE-3", r3);

        assertThat("Async processing took too long.", toc - tic, Matchers.lessThan(3 * AsyncResource.OPERATION_DURATION));
    }

    /**
     * Test accessing an operation that times out on the server.
     *
     * @throws Exception in case of a test error.
     */
    @Test
    public void testAsyncGetWithTimeout() throws Exception {
        final Future<Response> responseFuture = target(PATH).path("timeout").request().async().get();
        // Request is being processed asynchronously.
        final Response response = responseFuture.get();

        // get() waits for the response
        assertEquals(503, response.getStatus());
        assertEquals("Operation time out.", response.readEntity(String.class));
    }

    @Path("/threadpool")
    public static class ThreadPoolResource {

        @GET
        public String get() {
            sleep();
            return "GET";
        }

        private void sleep() {
            try {
                Thread.sleep(100);
            } catch (final InterruptedException ex) {
                // NOOP.
            }
        }
    }

    @Test
    @Ignore("Unstable test.")
    public void testClientThreadPool() throws Exception {
        final AsyncInvoker invoker = ClientBuilder
                .newClient(new ClientConfig().property(ClientProperties.ASYNC_THREADPOOL_SIZE, 9))
                .target(getBaseUri())
                .path("threadpool")
                .request()
                .async();

        final CountDownLatch latch = new CountDownLatch(100);
        final int threadCount = Thread.activeCount();

        final List<Thread> threads = new ArrayList<Thread>(20);
        for (int i = 0; i < 20; i++) {
            threads.add(new Thread(new Runnable() {
                @Override
                public void run() throws RuntimeException {
                    for (int i = 0; i < 5; i++) {
                        try {
                            assertThat(invoker.get().get().readEntity(String.class), equalTo("GET"));
                            assertThat(Thread.activeCount() - threadCount - 20, lessThanOrEqualTo(10));
                            latch.countDown();
                        } catch (final InterruptedException e) {
                            fail();
                        } catch (final ExecutionException e) {
                            fail();
                        }
                    }
                }
            }));
        }
        for (final Thread thread : threads) {
            thread.start();
        }

        assertTrue(latch.await(10 * getAsyncTimeoutMultiplier(), TimeUnit.SECONDS));
    }
}
