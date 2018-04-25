/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.server.async.managed;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;

import org.glassfish.jersey.server.ManagedAsync;

/**
 * Example of a simple resource with a long-running operation executed in a
 * custom Jersey container request processing thread.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
@Path(App.ASYNC_LONG_RUNNING_MANAGED_OP_PATH)
@Produces("text/plain")
public class SimpleJerseyExecutorManagedLongRunningResource {

    public static final String NOTIFICATION_RESPONSE = "Hello async world!";
    //
    private static final Logger LOGGER = Logger.getLogger(SimpleJerseyExecutorManagedLongRunningResource.class.getName());
    private static final int SLEEP_TIME_IN_MILLIS = 1000;

    @GET
    @ManagedAsync
    public void longGet(@Suspended final AsyncResponse ar, @QueryParam("id") int requestId) {
        try {
            Thread.sleep(SLEEP_TIME_IN_MILLIS);
        } catch (InterruptedException ex) {
            LOGGER.log(Level.SEVERE, "Response processing interrupted", ex);
        }
        ar.resume(requestId + " - " + NOTIFICATION_RESPONSE);
    }
}
