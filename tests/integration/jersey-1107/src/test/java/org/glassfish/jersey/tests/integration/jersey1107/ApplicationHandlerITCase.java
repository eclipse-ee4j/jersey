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

package org.glassfish.jersey.tests.integration.jersey1107;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.external.ExternalTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.glassfish.jersey.test.spi.TestContainerFactory;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests for JERSEY-1107: Thread gets stuck if no MessageBodyWriter is found in ApplicationHandler#writeResponse.
 * <p/>
 * If an exception (e.g. NPE caused by non-existent MessageBodyWriter) is thrown in ApplicationHandler#writeResponse before
 * headers and response status are written by ContainerResponseWriter#writeResponseStatusAndHeaders then the
 * ContainerResponseWriter#commit in the finally clause will stuck the thread.
 * <p/>
 * The purpose of the tests below is to show that a response is returned from the server and none of the threads gets stuck.
 *
 * @author Michal Gajdos
 */
public class ApplicationHandlerITCase extends JerseyTest {

    @Override
    protected ResourceConfig configure() {
        return new ResourceConfig().registerInstances(new Jersey1107());
    }

    @Override
    protected TestContainerFactory getTestContainerFactory() throws TestContainerException {
        return new ExternalTestContainerFactory();
    }

    /**
     * Checks if a thread gets stuck when an {@code IOException} is thrown from the {@code
     * MessageBodyWriter#writeTo}.
     */
    @Test
    public void testIOExceptionInWriteResponseMethod() throws Exception {
        _testExceptionInWriteResponseMethod("ioe", "exception/ioexception", Response.Status.INTERNAL_SERVER_ERROR);
    }

    /**
     * Checks if a thread gets stuck when an {@code WebApplicationException} is thrown from the {@code
     * MessageBodyWriter#writeTo}.
     */
    @Test
    public void testWebApplicationExceptionInWriteResponseMethod() throws Exception {
        _testExceptionInWriteResponseMethod("wae", "exception/webapplicationexception", Response.Status.INTERNAL_SERVER_ERROR);
    }

    /**
     * Checks if a thread gets stuck when no {@code MessageBodyWriter} is found and therefore an {@code NPE} is thrown
     * when trying to invoke {@code MessageBodyWriter#writeTo} on an empty object.
     */
    @Test
    public void testNullPointerExceptionInWriteResponseMethod() throws Exception {
        _testExceptionInWriteResponseMethod("npe", "exception/nullpointerexception", Response.Status.INTERNAL_SERVER_ERROR);
    }

    /**
     * Creates a request to the server (with the whole process time set to the maximum of 5 seconds) for the given {@code path}
     * and {@code mediaType} that should result in the {@code expectedResponse}.
     */
    private void _testExceptionInWriteResponseMethod(final String path, final String mediaType,
                                                     final Response.Status expectedResponse) throws Exception {
        // Executor.
        final ExecutorService executor = Executors.newSingleThreadExecutor();

        final Future<Response> responseFuture = executor.submit(new Callable<Response>() {

            @Override
            public Response call() throws Exception {
                return target().path(path).request(mediaType).get();
            }

        });

        executor.shutdown();
        final boolean inTime = executor.awaitTermination(5000, TimeUnit.MILLISECONDS);

        // Asserts.
        assertTrue(inTime);

        // Response.
        final Response response = responseFuture.get();
        assertEquals(expectedResponse.getStatusCode(), response.getStatusInfo().getStatusCode());
    }

}
