/*
 * Copyright (c) 2025 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.helidon;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.container.AsyncResponse;
import jakarta.ws.rs.container.Suspended;
import jakarta.ws.rs.container.TimeoutHandler;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class AsyncTest extends AbstractHelidonServerTester {

    @Path("/async")
    @SuppressWarnings("VoidMethodAnnotatedWithGET")
    public static class AsyncResource {

        public static AtomicInteger INVOCATION_COUNT = new AtomicInteger(0);

        @GET
        public void asyncGet(@Suspended final AsyncResponse asyncResponse) {
            new Thread(new Runnable() {

                @Override
                public void run() {
                    final String result = veryExpensiveOperation();
                    asyncResponse.resume(result);
                }

                private String veryExpensiveOperation() {
                    // ... very expensive operation that typically finishes within 5 seconds, simulated using sleep()
                    try {
                        Thread.sleep(5000);
                    } catch (final InterruptedException e) {
                        // ignore
                    }
                    return "DONE";
                }
            }).start();
        }

        @GET
        @Path("timeout")
        public void asyncGetWithTimeout(@Suspended final AsyncResponse asyncResponse) {
            asyncResponse.setTimeoutHandler(new TimeoutHandler() {

                @Override
                public void handleTimeout(final AsyncResponse asyncResponse) {
                    asyncResponse.resume(Response.status(Response.Status.SERVICE_UNAVAILABLE).entity("Operation time out.")
                            .build());
                }
            });
            asyncResponse.setTimeout(3, TimeUnit.SECONDS);

            new Thread(new Runnable() {

                @Override
                public void run() {
                    final String result = veryExpensiveOperation();
                    asyncResponse.resume(result);
                }

                private String veryExpensiveOperation() {
                    // ... very expensive operation that typically finishes within 10 seconds, simulated using sleep()
                    try {
                        Thread.sleep(7000);
                    } catch (final InterruptedException e) {
                        // ignore
                    }
                    return "DONE";
                }
            }).start();
        }

        @GET
        @Path("multiple-invocations")
        public void asyncMultipleInvocations(@Suspended final AsyncResponse asyncResponse) {
            INVOCATION_COUNT.incrementAndGet();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    asyncResponse.resume("OK");
                }
            }).start();
        }
    }

    private Client client;

    @BeforeEach
    public void setUp() throws Exception {
        startServer(AsyncResource.class);
        client = ClientBuilder.newClient();
    }

    @Override
    @AfterEach
    public void tearDown() {
        super.tearDown();
        client = null;
    }

    @Test
    public void testAsyncGet() throws ExecutionException, InterruptedException {
        final Future<Response> responseFuture = client.target(getUri().path("/async")).request().async().get();
        // Request is being processed asynchronously.
        final Response response = responseFuture.get();
        // get() waits for the response
        assertEquals("DONE", response.readEntity(String.class));
    }

    @Test
    @Disabled
    public void testAsyncGetWithTimeout() throws ExecutionException, InterruptedException, TimeoutException {
        final Future<Response> responseFuture = client.target(getUri().path("/async/timeout")).request().async().get();
        // Request is being processed asynchronously.
        final Response response = responseFuture.get();

        // get() waits for the response
        assertEquals(503, response.getStatus());
        assertEquals("Operation time out.", response.readEntity(String.class));
    }

    /**
     * JERSEY-2616 reproducer. Make sure resource method is only invoked once per one request.
     */
    @Test
    public void testAsyncMultipleInvocations() throws Exception {
        final Response response = client.target(getUri().path("/async/multiple-invocations")).request().get();

        assertThat(AsyncResource.INVOCATION_COUNT.get(), is(1));

        assertThat(response.getStatus(), is(200));
        assertThat(response.readEntity(String.class), is("OK"));
    }
}
