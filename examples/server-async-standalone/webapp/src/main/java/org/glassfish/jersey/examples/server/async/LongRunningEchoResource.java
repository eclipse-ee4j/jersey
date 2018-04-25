/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.server.async;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.ServiceUnavailableException;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;

/**
 * Example of a simple resource with a long-running operation executed in a
 * custom application thread.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
@Path("long-running")
@Produces("text/plain")
public class LongRunningEchoResource {

    private static final int SLEEP_TIME_IN_MILLIS = 1000;
    private static final ExecutorService TASK_EXECUTOR = Executors.newCachedThreadPool();

    /**
     * Synchronously echo the last path segment after sleeping for a long time.
     *
     * @param echo message to echo.
     * @return echoed message.
     */
    @GET
    @Path("sync/{echo}")
    public String syncEcho(@PathParam("echo") final String echo) {
        try {
            Thread.sleep(SLEEP_TIME_IN_MILLIS);
        } catch (final InterruptedException ex) {
            throw new ServiceUnavailableException();
        }
        return echo;
    }

    /**
     * Asynchronously echo the last path segment after sleeping for a long time.
     *
     * @param echo message to echo.
     * @param ar AsynchronousResponse, will contain echoed message.
     */
    @GET
    @Path("async/{echo}")
    public void asyncEcho(@PathParam("echo") final String echo, @Suspended final AsyncResponse ar) {
        TASK_EXECUTOR.submit(new Runnable() {

            @Override
            public void run() {
                try {
                    Thread.sleep(SLEEP_TIME_IN_MILLIS);
                } catch (final InterruptedException ex) {
                    ar.cancel();
                }
                ar.resume(echo);
            }
        });
    }
}
