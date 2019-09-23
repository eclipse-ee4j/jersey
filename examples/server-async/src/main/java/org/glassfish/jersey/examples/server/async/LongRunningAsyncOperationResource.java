/*
 * Copyright (c) 2011, 2019 Oracle and/or its affiliates. All rights reserved.
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
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;

import org.glassfish.jersey.internal.guava.ThreadFactoryBuilder;
import org.glassfish.jersey.process.JerseyProcessingUncaughtExceptionHandler;

/**
 * Example resource for long running async operations.
 *
 * @author Marek Potociar
 */
//TODO move the test to integration tests.
//@Path(App.LONG_RUNNING_ASYNC_OP_PATH)
@Produces("text/plain")
public class LongRunningAsyncOperationResource {

    public static final String NOTIFICATION_RESPONSE = "Hello async world!";
    //
    private static final Logger LOGGER = Logger.getLogger(LongRunningAsyncOperationResource.class.getName());
    private static final int SLEEP_TIME_IN_MILLIS = 1000;
    private static final ExecutorService TASK_EXECUTOR = Executors.newCachedThreadPool(new ThreadFactoryBuilder()
            .setNameFormat("long-running-resource-executor-%d")
            .setUncaughtExceptionHandler(new JerseyProcessingUncaughtExceptionHandler())
            .build());

    @GET
    @Path("basicSyncExample")
    public String basicSyncExample() {
        try {
            Thread.sleep(SLEEP_TIME_IN_MILLIS);
        } catch (InterruptedException ex) {
            LOGGER.log(Level.SEVERE, "Response processing interrupted", ex);
        }
        return NOTIFICATION_RESPONSE;
    }

    @GET
    @Path("async")
    public void suspendViaContextExample(@Suspended final AsyncResponse ar, @QueryParam("query") final String query) {
        TASK_EXECUTOR.submit(new Runnable() {

            @Override
            public void run() {
                try {
                    Thread.sleep(SLEEP_TIME_IN_MILLIS);
                } catch (InterruptedException ex) {
                    LOGGER.log(Level.SEVERE, "Response processing interrupted", ex);
                }
                ar.resume("Complex result for " + query);
            }
        });
    }
}
