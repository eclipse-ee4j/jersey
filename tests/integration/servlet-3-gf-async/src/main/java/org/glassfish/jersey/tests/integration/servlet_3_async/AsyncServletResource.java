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

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;

/**
 * Asynchronous servlet-deployed resource.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
@Path("async")
public class AsyncServletResource {
    /**
     * Hello world message.
     */
    public static final String HELLO_ASYNC_WORLD = "Hello Async World!";
    public static final String CANCELED = "Canceled";

    private static BlockingQueue<CanceledRequest> cancelingQueue = new ArrayBlockingQueue<CanceledRequest>(5);

    private static class CanceledRequest {
        private final String id;
        private final AsyncResponse asyncResponse;

        private CanceledRequest(final String id, final AsyncResponse asyncResponse) {
            this.id = id;
            this.asyncResponse = asyncResponse;
        }
    }

    /**
     * Get the async "Hello World" message.
     */
    @GET
    @Produces("text/plain")
    public void get(@Suspended final AsyncResponse ar) {
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(100);
                    ar.resume(HELLO_ASYNC_WORLD);
                } catch (final InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Get a canceled request.
     *
     * @param id request id.
     * @throws InterruptedException in case of not being able to put the request
     *                              to an internal queue for canceling.
     */
    @GET
    @Path("canceled")
    public void getCanceled(@Suspended final AsyncResponse ar, @QueryParam("id") final String id) throws InterruptedException {
        cancelingQueue.put(new CanceledRequest(id, ar));
    }

    /**
     * Cancel a request that is on top of the canceling queue.
     *
     * @return notification message about successful request canceling.
     * @throws InterruptedException in case of not being able to take a cancelled request
     *                              from an internal canceling queue.
     */
    @POST
    @Produces("text/plain")
    @Path("canceled")
    public String cancel(final String requestId) throws InterruptedException {
        final CanceledRequest canceledRequest = cancelingQueue.take();
        canceledRequest.asyncResponse.cancel();

        return CANCELED + " " + canceledRequest.id + " by POST " + requestId;
    }

}
