/*
 * Copyright (c) 2023 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package org.glassfish.jersey.examples.micrometer;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.util.concurrent.TimeUnit;

@Path("metrics")
public class MetricsResource {

    private final MeterRegistry registry;

    public MetricsResource(MeterRegistry registry) {
        this.registry = registry;
    }

    @GET
    @Produces("text/plain")
    public String getMeters() {
        final StringBuffer result = new StringBuffer();
        try {
            result.append("Listing available meters: ");
            for (final Meter meter : registry.getMeters()) {
                result.append(meter.getId().getName());
                result.append("; ");
            }
        } catch (Exception ex) {
            System.out.println(ex);
            result.append("Exception occured, see log for details...");
            result.append(ex.toString());
        }
        return result.toString();
    }
    @GET
    @Path("metrics")
    @Produces("text/plain")
    public String getMetrics() {
        final StringBuffer result = new StringBuffer();
        try {
            final Timer timer = registry.get("http.shared.metrics")
                    .tags("method", "GET", "uri", "/micro/meter", "status", "200", "exception", "None", "outcome", "SUCCESS")
                    .timer();
            result.append(String.format("Overall requests counts: %d, total time (millis): %f",
                    timer.count(), timer.totalTime(TimeUnit.MILLISECONDS)));
        } catch (Exception ex) {
            System.out.println(ex);
            result.append("Exception occured, see log for details...");
            result.append(ex.toString());
        }
        return result.toString();
    }
}
