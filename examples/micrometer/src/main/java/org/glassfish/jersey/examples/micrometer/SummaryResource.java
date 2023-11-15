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

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import java.util.concurrent.TimeUnit;

import static org.glassfish.jersey.examples.micrometer.App.WEB_PATH;
import static org.glassfish.jersey.examples.micrometer.MetricsStore.REGISTRY_NAME;

@Path("summary")
public class SummaryResource {

    @Context
    private MetricsStore store;

    @GET
    @Produces("text/html")
    public String getExtendedMeters() {
        final StringBuffer result = new StringBuffer();
        try {
            result.append("<html><body>"
                    + "Listing available meters<br/><br/>Many occurrences of the same name means that there are more meters"
                    + " which could be used with different tags,"
                    + " but this is actually a challenge to handle all available metrics :<br/><br/> ");
            for (final Meter meter : store.getRegistry().getMeters()) {
                result.append(meter.getId().getName());
                result.append(";<br/>\n\r ");
            }
        } catch (Exception ex) {
            result.append("Try clicking links below to gain more metrics.<br/>");
        }
        result.append("<br/>\n\r ");
        result.append("<br/>\n\r ");
        try {
            final Timer timer = store.getRegistry().get(REGISTRY_NAME)
                    .tags("method", "GET", "status", "200", "exception", "None",
                            "outcome", "SUCCESS", "uri", "/micro/metrics")
                    .timer();

            result.append(
                    String.format("Counts to the page with standard measurements: %d, time spent on requests to the init "
                            + "page (millis): %f <br/>\n\r",
                    timer.count(), timer.totalTime(TimeUnit.MILLISECONDS)));

            final Timer annotatedTimer = store.getRegistry().timer(TimedResource.TIMER_NAME,
                    "method", "GET", "status", "200", "exception", "None",
                    "outcome", "SUCCESS", "uri", "/micro/timed");

            result.append(
                    String.format("Counts to the page with annotated measurements: %d, total time (millis): %f <br/>\n\r",
                    annotatedTimer.count(), annotatedTimer.totalTime(TimeUnit.MILLISECONDS)));

        } catch (Exception ex) {
            result.append(String.format("Counts to the init page: %d, total time (millis): %d <br/>\n\r",
                    0, 0));
            result.append("Try clicking links below to gain more metrics.<br/>");
        }
        result.append("<br/><br/>Available pages for measurements: <a href=\""
                + WEB_PATH + "metrics\">measure requests in the standard way</a> &nbsp;, <a href=\""
                + WEB_PATH + "timed\">measure requests in the annotated way</a>");
        return result.append("</body></html>").toString();
    }
}
