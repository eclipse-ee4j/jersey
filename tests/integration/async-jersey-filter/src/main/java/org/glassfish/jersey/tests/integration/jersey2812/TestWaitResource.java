/*
 * Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.integration.jersey2812;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.NotAcceptableException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;

import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;

/**
 * This resource provides a way to reproduce JERSEY-2818.
 *
 * @author Stepan Vavra (stepan.vavra at oracle.com)
 */
@Path("/async")
@Produces("text/plain")
@Singleton
public class TestWaitResource {

    private static final Logger LOGGER = Logger.getLogger(TestWaitResource.class.getName());

    /**
     * Test context identified by UUID chosen by client.
     */
    private final ConcurrentMap<String, TestContext> testContextMap = new ConcurrentHashMap<>();

    private TestContext testContextForUUID(String uuid) {
        testContextMap.putIfAbsent(uuid, new TestContext());
        return testContextMap.get(uuid);
    }

    @GET
    @Path("wait/{uuid}")
    public void waitForEvent(@Suspended AsyncResponse asyncResponse,
                             @Context HttpServletRequest request,
                             @PathParam("uuid") String uuid) {

        LOGGER.finer("Adding response: " + asyncResponse);

        final TestContext testContext = testContextForUUID(uuid);
        final CountDownLatch finishedCdl = (CountDownLatch) request.getAttribute(TestFilter.CDL_FINISHED);

        if (finishedCdl == null) {
            throw new IllegalStateException("The " + TestFilter.class + " was not properly processed before this request!");
        }

        testContext.asyncResponse = asyncResponse;
        testContext.finishedCdl = finishedCdl;
        testContext.startedCdl.countDown();

        LOGGER.finer("Decreasing started cdl: " + testContext.startedCdl);
    }

    @POST
    @Path("release/{uuid}")
    public String releaseLastSuspendedAsyncRequest(@PathParam("uuid") String uuid) {

        LOGGER.finer("Releasing async response");

        if (!testContextMap.containsKey(uuid)) {
            throw new NotAcceptableException("UUID not found!" + uuid);
        }

        // clean it up
        final TestContext releasedTestContext = testContextMap.remove(uuid);
        releasedTestContext.finishedCdl.countDown();
        releasedTestContext.startedCdl.countDown();
        releasedTestContext.asyncResponse.resume("async-OK-" + uuid);

        return "RELEASED";
    }

    @GET
    @Path("await/{uuid}/started")
    public boolean awaitForTheAsyncRequestThreadToStart(@PathParam("uuid") String uuid, @QueryParam("millis") Long millis) {
        final CountDownLatch startedCdl = testContextForUUID(uuid).startedCdl;
        try {
            LOGGER.finer("Checking started cdl: " + startedCdl);
            return startedCdl.await(millis, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            throw new IllegalStateException("Interrupted while waiting for the thread to finish!", e);
        }
    }

    @GET
    @Path("await/{uuid}/finished")
    public boolean awaitForTheAsyncRequestThreadToFinish(@PathParam("uuid") String uuid, @QueryParam("millis") Long millis) {
        if (!testContextMap.containsKey(uuid)) {
            throw new NotAcceptableException("UUID not found!" + uuid);
        }
        try {
            LOGGER.finer("Decreasing finished cdl: " + testContextMap.get(uuid).finishedCdl);
            return testContextMap.get(uuid).finishedCdl.await(millis, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            throw new IllegalStateException("Interrupted while waiting for the thread to finish!", e);
        }
    }

    /**
     * Test context holder class.
     * <p/>
     * Holds the information for one test identified by UUID chosen by client.
     *
     * @see #testContextMap
     */
    private static class TestContext {

        /**
         * This CDL ensures the server-side thread processing the request to /async/wait/{uuid} has started handling the request.
         */
        final CountDownLatch startedCdl = new CountDownLatch(1);

        /**
         * This CDL ensures the server-side thread processing the request to /async/wait/{uuid} was returned to the thread-pool.
         */
        volatile CountDownLatch finishedCdl;

        /**
         * The async response that does get resumed outside of the request to /async/wait/{uuid}. This reproduces the JERSEY-2812
         * bug.
         */
        volatile AsyncResponse asyncResponse;
    }

}
