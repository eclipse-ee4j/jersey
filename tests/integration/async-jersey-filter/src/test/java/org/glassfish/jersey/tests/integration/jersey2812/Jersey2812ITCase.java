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

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.core.Response;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.glassfish.jersey.internal.guava.ThreadFactoryBuilder;
import org.glassfish.jersey.tests.integration.async.AbstractAsyncJerseyTest;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * JERSEY-2812 reproducer.
 * <p/>
 * This test must not run in parallel.
 *
 * @author Stepan Vavra (stepan.vavra at oracle.com)
 */
public class Jersey2812ITCase extends AbstractAsyncJerseyTest {

    private static final Logger LOGGER = Logger.getLogger(Jersey2812ITCase.class.getName());
    private static long WAIT_TIMEOUT = 5000;

    private AtomicReference<String> asyncResult = new AtomicReference<>();
    private String uuid = UUID.randomUUID().toString();
    private ExecutorService executorService = Executors
            .newSingleThreadExecutor(new ThreadFactoryBuilder().setDaemon(true).build());

    @Before
    public void triggerTheWaitRequestInSeparateThread() throws Exception {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                LOGGER.finer("Running a request to /async/wait in a separate thread.");
                asyncResult.set(target("/asyncTest/async/wait").path(uuid).request().get(String.class));
            }
        });
    }

    /**
     * Tests whether the server-side thread that is processing a http request to the servlet-filter-based Jersey setup ends up
     * stuck or returned back to the pool of available threads.
     * <p/>
     * This test prevents a regression reported in JERSEY-2812.
     * <p/>
     * When the {@link javax.ws.rs.container.AsyncResponse} was left intact in the RESTful resource (as implemented in {@link
     * TestWaitResource#waitForEvent(AsyncResponse, HttpServletRequest, String)}), the server-side Jersey thread ended up in
     * {@link org.glassfish.jersey.servlet.internal.ResponseWriter#getResponseContext()} blocked because of the resolution of http
     * response status from {@link org.glassfish.jersey.servlet.ServletContainer#doFilter(HttpServletRequest, HttpServletResponse,
     * FilterChain, String, String, String)}
     * <p/>
     * This test uses a separate thread to call {@code /async/wait/{uuid}} resource which blocks until the {@code
     * /async/release/{uuid}} is called. In the meantime the JUnit thread calls {@code /async/await/{uuid}} to discover whether
     * the server-side thread processing the request to {@code /async/await/{uuid}/started} did start processing of the request.
     * Consecutively, the JUnit thread calls {@code /async/await/{uuid}/finished} with a timeout {@code #WAIT_TIMEOUT} to discover
     * whether the server-side thread got stuck (which is what JERSEY-2812 reported) or not.
     *
     * @throws Exception
     */
    @Test
    public void asyncSuspendedResourceDoesNotGetStuck() throws Exception {
        // [1] wait for the /async/wait request to be processed
        final Response startResponse = target("/asyncTest/async/await").path(uuid).path("started")
                .queryParam("millis", WAIT_TIMEOUT).request().get();
        assertTrue("The server-side thread handling the request to /async/wait didn't start in timely fashion. "
                        + "This error indicates this test is not executed / designed properly rather than a regression in "
                        + "JERSEY-2812 fix.",
                startResponse.readEntity(Boolean.class));

        // [2] wait for the /async/wait request to finish
        final Response finishResponse = target("/asyncTest/async/await").path(uuid).path("finished")
                .queryParam("millis", WAIT_TIMEOUT).request().get();
        assertTrue("The thread processing the /async/wait request did not respond in timely fashion. "
                        + "Memory leak / thread got stuck detected!",
                finishResponse.readEntity(Boolean.class));

        // [3] release the blocked http call to /async/wait
        final String releaseResponse = target("/asyncTest/async/release").path(uuid).request().post(null, String.class);
        assertEquals("RELEASED", releaseResponse);

        // [4] test whether everything ended as expected
        executorService.shutdown();
        assertTrue("The test thread did not finish in timely fashion!",
                executorService.awaitTermination(WAIT_TIMEOUT, TimeUnit.MILLISECONDS));
        assertEquals("async-OK-" + uuid, asyncResult.get());
    }

    @After
    public void releaseResources() {
        // release the server-side thread regardless of whether left un-attended
        target("/asyncTest/async/release").path(uuid).request().post(null);
    }

    @After
    public void terminateThread() {
        // forcibly terminate the test client thread
        executorService.shutdownNow();
    }

}
