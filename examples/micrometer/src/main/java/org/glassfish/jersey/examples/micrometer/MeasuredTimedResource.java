/*
 * Copyright (c) 2023 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.micrometer;

import io.micrometer.core.annotation.Timed;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path("measure")
public class MeasuredTimedResource {

    public static final String CLICHED_MESSAGE = "Requests to this method are measured. Use /metrics to see more";
    public static final String TIMER_NAME = "http.timers";
    public static final String TIMER_DESCRIPTION = "resource measurement timer";

    @GET
    @Produces("text/plain")
    @Timed(value = TIMER_NAME, description = TIMER_DESCRIPTION, histogram = true)
    @Path("timed")
    public String getTimedMessage() {
        return CLICHED_MESSAGE;
    }
}
