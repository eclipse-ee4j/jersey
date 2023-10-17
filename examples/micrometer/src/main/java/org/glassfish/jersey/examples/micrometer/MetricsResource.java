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

@Path("metrics")
public class MetricsResource {

    private MetricsStore store;


   public MetricsResource(MetricsStore store) {
        this.store = store;
    }

    @GET
    @Produces("text/html")
    public String getMeters() {
       return "<html><body>Static meters are initialized, try <a href=\""
               + WEB_PATH + "extendedMeters\">extendedMeters</a></body></html>";
    }


    @GET
    @Produces("text/plain")
    @Path("extendedMeters")
    public String getExtendedMeters() {
        final StringBuffer result = new StringBuffer();
        try {
            result.append("Listing available meters: ");
            for (final Meter meter : store.getRegistry().getMeters()) {
                result.append(meter.getId().getName());
                result.append(";\n\r ");
            }
        } catch (Exception ex) {
            result.append("Looks like there are no proper metrics.");
            result.append("\n\r");
            result.append("Please visit /measure/timed and /measure/counted first ");
            result.append(ex);
        }
        if (store.getRegistry().getMeters().size() > 0) {
            try {
                final Timer timer = store.getRegistry().get("http.shared.metrics")
                        .tags("method", "GET", "status", "200", "exception", "None", "outcome", "SUCCESS")
                        .timer();

                result.append(String.format("Overall requests counts: %d, total time (millis): %f \n\r",
                        timer.count(), timer.totalTime(TimeUnit.MILLISECONDS)));

                final Timer annotatedTimer = store.getRegistry().timer(MeasuredTimedResource.TIMER_NAME,
                        "method", "GET", "status", "200", "exception", "None",
                        "outcome", "SUCCESS", "uri", "/micro/measure/timed");

                result.append(String.format("Requests to 'measure/timed' counts: %d, total time (millis): %f \n\r",
                        annotatedTimer.count(), annotatedTimer.totalTime(TimeUnit.MILLISECONDS)));

            } catch (Exception ex) {
                result.append("Exception occurred, see log for details...");
                result.append(ex);
            }
        }
        return result.toString();
    }
}