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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.InvocationCallback;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;

import org.glassfish.jersey.examples.rx.domain.AgentResponse;
import org.glassfish.jersey.examples.rx.domain.Calculation;
import org.glassfish.jersey.examples.rx.domain.Destination;
import org.glassfish.jersey.examples.rx.domain.Forecast;
import org.glassfish.jersey.examples.rx.domain.Recommendation;
import org.glassfish.jersey.internal.guava.ThreadFactoryBuilder;
import org.glassfish.jersey.process.JerseyProcessingUncaughtExceptionHandler;
import org.glassfish.jersey.server.ManagedAsync;
import org.glassfish.jersey.server.Uri;

/**
 * Obtain information about visited (destination) and recommended (destination, forecast, price) places for "Async" user. Uses
 * standard JAX-RS async approach to obtain the data.
 *
 * @author Michal Gajdos
 */
@Path("agent/async")
@Produces("application/json")
public class AsyncAgentResource {

    @Uri("remote/destination")
    private WebTarget destination;

    @Uri("remote/calculation/from/{from}/to/{to}")
    private WebTarget calculation;

    @Uri("remote/forecast/{destination}")
    private WebTarget forecast;

    private final ExecutorService executor;

    public AsyncAgentResource() {
        executor = new ScheduledThreadPoolExecutor(20, new ThreadFactoryBuilder()
                .setNameFormat("jersey-rx-client-async-%d")
                .setUncaughtExceptionHandler(new JerseyProcessingUncaughtExceptionHandler())
                .build());
    }

    @GET
    @ManagedAsync
    public void async(@Suspended final AsyncResponse async) {
        final long time = System.nanoTime();

        final AgentResponse response = new AgentResponse();

        final CountDownLatch outerLatch = new CountDownLatch(2);
        final Queue<String> errors = new ConcurrentLinkedQueue<>();

        // Obtain visited destinations.
        destination.path("visited").request()
                // Identify the user.
                .header("Rx-User", "Async")
                // Async invoker.
                .async()
                // Return a list of destinations
                .get(new InvocationCallback<List<Destination>>() {
                    @Override
                    public void completed(final List<Destination> destinations) {
                        response.setVisited(destinations);
                        outerLatch.countDown();
                    }

                    @Override
                    public void failed(final Throwable throwable) {
                        errors.offer("Visited: " + throwable.getMessage());
                        outerLatch.countDown();
                    }
                });


        // Obtain recommended destinations. (does not depend on visited ones)
        destination.path("recommended").request()
                // Identify the user.
                .header("Rx-User", "Async")
                // Async invoker.
                .async()
                // Return a list of destinations.
                .get(new InvocationCallback<List<Destination>>() {
                    @Override
                    public void completed(final List<Destination> recommended) {
                        final CountDownLatch innerLatch = new CountDownLatch(recommended.size() * 2);

                        // Forecasts. (depend on recommended destinations)
                        final Map<String, Forecast> forecasts = Collections.synchronizedMap(new HashMap<>());
                        for (final Destination dest : recommended) {
                            forecast.resolveTemplate("destination", dest.getDestination()).request()
                                    .async()
                                    .get(new InvocationCallback<Forecast>() {
                                        @Override
                                        public void completed(final Forecast forecast) {
                                            forecasts.put(dest.getDestination(), forecast);
                                            innerLatch.countDown();
                                        }

                                        @Override
                                        public void failed(final Throwable throwable) {
                                            errors.offer("Forecast: " + throwable.getMessage());
                                            innerLatch.countDown();
                                        }
                                    });
                        }

                        // Calculations. (depend on recommended destinations)
                        final List<Future<Calculation>> futures = recommended.stream()
                                .map(dest -> calculation.resolveTemplate("from", "Moon").resolveTemplate("to",
                                        dest.getDestination()).request().async().get(Calculation.class))
                                .collect(Collectors.toList());

                        final Map<String, Calculation> calculations = new HashMap<>();
                        while (!futures.isEmpty()) {
                            final Iterator<Future<Calculation>> iterator = futures.iterator();

                            while (iterator.hasNext()) {
                                final Future<Calculation> f = iterator.next();
                                if (f.isDone()) {
                                    try {
                                        final Calculation calculation = f.get();
                                        calculations.put(calculation.getTo(), calculation);

                                        innerLatch.countDown();
                                    } catch (final Throwable t) {
                                        errors.offer("Calculation: " + t.getMessage());
                                        innerLatch.countDown();
                                    } finally {
                                        iterator.remove();
                                    }
                                }
                            }
                        }

                        // Have to wait here for dependent requests ...
                        try {
                            if (!innerLatch.await(10, TimeUnit.SECONDS)) {
                                errors.offer("Inner: Waiting for requests to complete has timed out.");
                            }
                        } catch (final InterruptedException e) {
                            errors.offer("Inner: Waiting for requests to complete has been interrupted.");
                        }

                        // Recommendations.
                        final List<Recommendation> recommendations = new ArrayList<>(recommended.size());
                        for (final Destination dest : recommended) {
                            final Forecast fore = forecasts.get(dest.getDestination());
                            final Calculation calc = calculations.get(dest.getDestination());

                            recommendations.add(new Recommendation(dest.getDestination(),
                                    fore != null ? fore.getForecast() : "N/A", calc != null ? calc.getPrice() : -1));
                        }
                        response.setRecommended(recommendations);

                        outerLatch.countDown();
                    }

                    @Override
                    public void failed(final Throwable throwable) {
                        errors.offer("Recommended: " + throwable.getMessage());
                        outerLatch.countDown();
                    }
                });

        // ... and have to wait also here for independent requests.
        try {
            if (!outerLatch.await(10, TimeUnit.SECONDS)) {
                errors.offer("Outer: Waiting for requests to complete has timed out.");
            }
        } catch (final InterruptedException e) {
            errors.offer("Outer: Waiting for requests to complete has been interrupted.");
        }

        // Do something with errors.
        // ...

        response.setProcessingTime((System.nanoTime() - time) / 1000000);
        async.resume(response);
    }
}
