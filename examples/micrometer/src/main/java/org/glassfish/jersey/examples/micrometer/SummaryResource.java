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

@Path("summary")
public class SummaryResource {

    private final MetricsStore store;

    public SummaryResource(MetricsStore store) {
        this.store = store;
    }

    @GET
    @Produces("text/html")
    public String getExtendedMeters() {
        final StringBuffer result = new StringBuffer();
        try {
            result.append("<html><body>"
                    + "Listing available meters<br/>Many occurrences of the same name means that there are more meters"
                    + " which could be used with different tags,"
                    + " but this is actually a challenge to handle all available metrics :<br/> ");
            for (final Meter meter : store.getRegistry().getMeters()) {
                result.append(meter.getId().getName());
                result.append(";<br/>\n\r ");
            }
        } catch (Exception ex) {
            result.append("Looks like there are no proper metrics.<br/>");
            result.append("\n\r");
            result.append("Please visit /measure/timed first <br/>");
            result.append(ex);
        }
        if (store.getRegistry().getMeters().size() > 0) {
            try {
                final Timer timer = store.getRegistry().get("http.shared.metrics")
                        .tags("method", "GET", "status", "200", "exception", "None", "outcome", "SUCCESS", "uri", "/micro/init")
                        .timer();

                result.append(String.format("Counts to the init page: %d, time spent on requests to the init "
                                + "page (millis): %f <br/>\n\r",
                        timer.count(), timer.totalTime(TimeUnit.MILLISECONDS)));

                final Timer annotatedTimer = store.getRegistry().timer(MeasuredTimedResource.TIMER_NAME,
                        "method", "GET", "status", "200", "exception", "None",
                        "outcome", "SUCCESS", "uri", "/micro/measure/timed");

                result.append(String.format("Requests to 'measure/timed' counts: %d, total time (millis): %f <br/>\n\r",
                        annotatedTimer.count(), annotatedTimer.totalTime(TimeUnit.MILLISECONDS)));

            } catch (Exception ex) {
                result.append("Exception occurred, more info is in console...<br/>");
                result.append(ex);
            }
        }
        return result.append("</body></html>").toString();
    }
}
