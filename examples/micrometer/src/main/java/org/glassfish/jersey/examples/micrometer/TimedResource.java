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

import static org.glassfish.jersey.examples.micrometer.App.WEB_PATH;

@Path("timed")
public class TimedResource {

    public static final String MESSAGE = "<html><body>Gaining measures in the annotated way. "
            + "<br/>Take a look at <a href=\"" + WEB_PATH + "metrics\">the standard way of measurements</a><br/> "
            + "Or just go to <a href=\"" + WEB_PATH + "summary\">summary</a> to check what you've got</body></html>";
    public static final String TIMER_NAME = "http.timers";
    public static final String TIMER_DESCRIPTION = "resource measurement timer";

    @GET
    @Produces("text/html")
    @Timed(value = TIMER_NAME, description = TIMER_DESCRIPTION, histogram = true)
    public String getTimedMessage() {
        return MESSAGE;
    }
}
