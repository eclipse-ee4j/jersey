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

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Timer;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.util.concurrent.TimeUnit;

import static org.glassfish.jersey.examples.micrometer.App.WEB_PATH;

@Path("init")
public class MetricsResource {

    @GET
    @Produces("text/html")
    public String getMeters() {
       return "<html><body>Static meters are initialized, try <a href=\""
               + WEB_PATH + "summary\">summary</a>. If you wan more measurements just refresh this page several times."
               + "</body></html>";
    }
}