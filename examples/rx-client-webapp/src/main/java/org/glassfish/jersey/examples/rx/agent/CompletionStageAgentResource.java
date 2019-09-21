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

import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.stream.Collectors;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.GenericType;

import org.glassfish.jersey.examples.rx.domain.AgentResponse;
import org.glassfish.jersey.examples.rx.domain.Calculation;
import org.glassfish.jersey.examples.rx.domain.Destination;
import org.glassfish.jersey.examples.rx.domain.Forecast;
import org.glassfish.jersey.examples.rx.domain.Recommendation;
import org.glassfish.jersey.internal.guava.ThreadFactoryBuilder;
import org.glassfish.jersey.server.Uri;

/**
 * Obtain information about visited (destination) and recommended (destination, forecast, price) places for
 * "CompletionStage" user. Uses Java 8 CompletionStage and Jersey Client to obtain the data.
 *
 * @author Michal Gajdos
 */
@Path("agent/completion")
@Produces("application/json")
public class CompletionStageAgentResource {

    @Uri("remote/destination")
    private WebTarget destinationTarget;

    @Uri("remote/calculation/from/{from}/to/{to}")
    private WebTarget calculationTarget;

    @Uri("remote/forecast/{destination}")
    private WebTarget forecastTarget;

    private final ExecutorService executor;

    public CompletionStageAgentResource() {
        executor = new ScheduledThreadPoolExecutor(20,
                new ThreadFactoryBuilder().setNameFormat("jersey-rx-client-completion-%d").build());
    }

    @GET
    public void completion(@Suspended final AsyncResponse async) {
        final long time = System.nanoTime();

        final Queue<String> errors = new ConcurrentLinkedQueue<>();

        CompletableFuture.completedFuture(new AgentResponse())
                .thenCombine(visited(destinationTarget, executor, errors), AgentResponse::visited)
                .thenCombine(recommended(destinationTarget, executor, errors), AgentResponse::recommended)
                .whenCompleteAsync((response, throwable) -> {
                    // Do something with errors.

                    response.setProcessingTime((System.nanoTime() - time) / 1000000);
                    async.resume(throwable == null ? response : throwable);
                });
    }

    private CompletionStage<List<Destination>> visited(final WebTarget destinationTarget,
                                                       final ExecutorService executor,
                                                       final Queue<String> errors) {
        return destinationTarget.path("visited").request()
                // Identify the user.
                .header("Rx-User", "CompletionStage")
                // Reactive invoker.
                .rx()
                // Return a list of destinations.
                .get(new GenericType<List<Destination>>() {})
                .exceptionally(throwable -> {
                    errors.offer("Visited: " + throwable.getMessage());
                    return Collections.emptyList();
                });
    }

    private CompletionStage<List<Recommendation>> recommended(final WebTarget destinationTarget,
                                                              final ExecutorService executor,
                                                              final Queue<String> errors) {
        // Recommended places.
        final CompletionStage<List<Destination>> recommended = destinationTarget.path("recommended")
                .request()
                // Identify the user.
                .header("Rx-User", "CompletionStage")
                // Reactive invoker.
                .rx()
                // Return a list of destinations.
                .get(new GenericType<List<Destination>>() {})
                .exceptionally(throwable -> {
                    errors.offer("Recommended: " + throwable.getMessage());
                    return Collections.emptyList();
                });

        return recommended.thenCompose(destinations -> {
            final WebTarget finalForecast = forecastTarget;
            final WebTarget finalCalculation = calculationTarget;

            List<CompletionStage<Recommendation>> recommendations = destinations.stream().map(destination -> {
                // For each destination, obtain a weather forecast ...
                final CompletionStage<Forecast> forecast =
                        finalForecast.resolveTemplate("destination", destination.getDestination())
                                     .request().rx().get(Forecast.class)
                                     .exceptionally(throwable -> {
                                         errors.offer("Forecast: " + throwable.getMessage());
                                         return new Forecast(destination.getDestination(), "N/A");
                                     });
                // ... and a price calculation
                final CompletionStage<Calculation> calculation = finalCalculation.resolveTemplate("from", "Moon")
                        .resolveTemplate("to", destination.getDestination())
                        .request().rx().get(Calculation.class)
                        .exceptionally(throwable -> {
                            errors.offer("Calculation: " + throwable.getMessage());
                            return new Calculation("Moon", destination.getDestination(), -1);
                        });

                //noinspection unchecked
                return CompletableFuture.completedFuture(new Recommendation(destination))
                        // Set forecast for recommended destination.
                        .thenCombine(forecast, Recommendation::forecast)
                        // Set calculation for recommended destination.
                        .thenCombine(calculation, Recommendation::calculation);
            }).collect(Collectors.toList());

            // Transform List<CompletionStage<Recommendation>> to CompletionStage<List<Recommendation>>
            return sequence(recommendations);
        });
    }

    private <T> CompletionStage<List<T>> sequence(final List<CompletionStage<T>> stages) {
        //noinspection SuspiciousToArrayCall
        final CompletableFuture<Void> done = CompletableFuture.allOf(stages.toArray(new CompletableFuture[stages.size()]));

        return done.thenApply(v -> stages.stream()
                        .map(CompletionStage::toCompletableFuture)
                        .map(CompletableFuture::join)
                        .collect(Collectors.<T>toList())
        );
    }

}
