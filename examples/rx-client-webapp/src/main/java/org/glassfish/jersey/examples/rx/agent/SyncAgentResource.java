/*
 * Copyright (c) 2014, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.rx.agent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;

import org.glassfish.jersey.examples.rx.domain.AgentResponse;
import org.glassfish.jersey.examples.rx.domain.Calculation;
import org.glassfish.jersey.examples.rx.domain.Destination;
import org.glassfish.jersey.examples.rx.domain.Forecast;
import org.glassfish.jersey.examples.rx.domain.Recommendation;
import org.glassfish.jersey.server.Uri;

/**
 * Obtain information about visited (destination) and recommended (destination, forecast, price) places for "Sync" user. Uses
 * standard JAX-RS sync approach to obtain the data.
 *
 * @author Michal Gajdos
 */
@Path("agent/sync")
@Produces("application/json")
public class SyncAgentResource {

    @Uri("remote/destination")
    private WebTarget destination;

    @Uri("remote/calculation/from/{from}/to/{to}")
    private WebTarget calculation;

    @Uri("remote/forecast/{destination}")
    private WebTarget forecast;

    @GET
    public AgentResponse sync() {
        final long time = System.nanoTime();

        final AgentResponse response = new AgentResponse();
        final Queue<String> errors = new ConcurrentLinkedQueue<>();

        // Obtain visited destinations.
        try {
            response.setVisited(destination.path("visited").request()
                    // Identify the user.
                    .header("Rx-User", "Sync")
                    // Return a list of destinations
                    .get(new GenericType<List<Destination>>() {}));
        } catch (final Throwable throwable) {
            errors.offer("Visited: " + throwable.getMessage());
        }

        // Obtain recommended destinations. (does not depend on visited ones)
        List<Destination> recommended = Collections.emptyList();
        try {
            recommended = destination.path("recommended").request()
                    // Identify the user.
                    .header("Rx-User", "Sync")
                    // Return a list of destinations.
                    .get(new GenericType<List<Destination>>() {});
        } catch (final Throwable throwable) {
            errors.offer("Recommended: " + throwable.getMessage());
        }

        // Forecasts. (depend on recommended destinations)
        final Map<String, Forecast> forecasts = new HashMap<>();
        for (final Destination dest : recommended) {
            try {
                forecasts.put(dest.getDestination(),
                        forecast.resolveTemplate("destination", dest.getDestination()).request().get(Forecast.class));
            } catch (final Throwable throwable) {
                errors.offer("Forecast: " + throwable.getMessage());
            }
        }

        // Calculations. (depend on recommended destinations)
        final Map<String, Calculation> calculations = new HashMap<>();
        recommended.stream().forEach(destination -> {
            try {
                calculations.put(destination.getDestination(), calculation.resolveTemplate("from", "Moon")
                        .resolveTemplate("to", destination.getDestination())
                        .request().get(Calculation.class));
            } catch (final Throwable throwable) {
                errors.offer("Calculation: " + throwable.getMessage());
            }
        });

        // Recommendations.
        final List<Recommendation> recommendations = new ArrayList<>(recommended.size());
        for (final Destination dest : recommended) {
            final Forecast fore = forecasts.get(dest.getDestination());
            final Calculation calc = calculations.get(dest.getDestination());

            recommendations.add(new Recommendation(dest.getDestination(),
                    fore != null ? fore.getForecast() : "N/A", calc != null ? calc.getPrice() : -1));
        }

        // Do something with errors.
        // ...

        response.setRecommended(recommendations);
        response.setProcessingTime((System.nanoTime() - time) / 1000000);
        return response;
    }
}
