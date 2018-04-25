/*
 * Copyright (c) 2014, 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.client.InvocationCallback;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Tests the behaviour of the Async client when the {@link java.util.concurrent.Future} is cancelled.
 *
 * <p>
 * Tests, that if the async request future is cancelled by the client,
 * the {@link javax.ws.rs.client.InvocationCallback#completed(Object)} callback is not invoked and that
 * {@link java.util.concurrent.CancellationException} is correctly returned (according to spec.) to
 * {@link javax.ws.rs.client.InvocationCallback#failed(Throwable)} callback method.
 * </p>
 *
 * @author Adam Lindenthal (adam.lindenthal at oracle.com)
 */
public class CancelFutureClientTest extends JerseyTest {

    public static final long MAX_WAITING_SECONDS = 2L;
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    @Override
    protected Application configure() {
        return new ResourceConfig(TestResource.class);
    }

    @Test
    public void testCancelFuture() throws InterruptedException, TimeoutException {
        Future<Response> future = target().path("test").request().async().get(
                new InvocationCallback<Response>() {
                    public void completed(final Response response) {
                        fail("[completed()] callback was invoked, although the Future should have been cancelled.");
                    }

                    public void failed(final Throwable throwable) {
                        assertEquals(CancellationException.class, throwable.getClass());
                        countDownLatch.countDown();
                    }
                }
        );
        if (!future.cancel(true)) {
            fail("The Future could not be canceled.");
        }

        // prevent the test container to stop the method execution before the callbacks can be reached.
        if (!countDownLatch.await(MAX_WAITING_SECONDS * getAsyncTimeoutMultiplier(), TimeUnit.SECONDS)) {
            throw new TimeoutException("Callback was not triggered within the time limit." + countDownLatch.getCount());
        }
    }

    @Path("test")
    public static class TestResource {
        @GET
        public Response get() {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return Response.noContent().build();
        }
    }
}
