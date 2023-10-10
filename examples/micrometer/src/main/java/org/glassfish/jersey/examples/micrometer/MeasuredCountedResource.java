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

import io.micrometer.core.annotation.Counted;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("measure")
public class MeasuredCountedResource {

    public static final String CLICHED_MESSAGE = "Requests to this method are counted. Use /metrics to see more";
    public static final String COUNTER_NAME = "http.counters";
    public static final String COUNTER_DESCRIPTION = "resource measurement counter";

    @GET
    //unfortunately the @counted annotation is not supported for the Jersey Integration.
    @Counted(value = COUNTER_NAME, description = COUNTER_DESCRIPTION)
    @Produces(MediaType.TEXT_PLAIN)
    @Path("counted")
    public String getCounterMessage() {
        return CLICHED_MESSAGE;
    }

}
