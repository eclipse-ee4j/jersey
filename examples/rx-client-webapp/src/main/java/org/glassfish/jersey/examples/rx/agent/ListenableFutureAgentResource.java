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

import java.util.Arrays;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.GenericType;

import org.glassfish.jersey.client.rx.guava.RxListenableFutureInvoker;
import org.glassfish.jersey.client.rx.guava.RxListenableFutureInvokerProvider;
import org.glassfish.jersey.examples.rx.domain.AgentResponse;
import org.glassfish.jersey.examples.rx.domain.Calculation;
import org.glassfish.jersey.examples.rx.domain.Destination;
import org.glassfish.jersey.examples.rx.domain.Forecast;
import org.glassfish.jersey.examples.rx.domain.Recommendation;
import org.glassfish.jersey.server.ManagedAsync;
import org.glassfish.jersey.server.Uri;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

/**
 * Obtain information about visited (destination) and recommended (destination, forecast, price) places for "Guava" user. Uses
 * Guava ListenableFuture and Jersey Client to obtain the data.
 *
 * @author Michal Gajdos
 * @author Pavel Bucek (pavel.bucek at oracle.com)
 */
@Path("agent/listenable")
@Produces("application/json")
public class ListenableFutureAgentResource {

    @Uri("remote/destination")
    private WebTarget destination;

    @Uri("remote/calculation/from/{from}/to/{to}")
    private WebTarget calculation;

    @Uri("remote/forecast/{destination}")
    private WebTarget forecast;

    @GET
    @ManagedAsync
    public void listenable(@Suspended final AsyncResponse async) {
        final long time = System.nanoTime();
        final AgentResponse response = new AgentResponse();

        // Obtain and set visited and recommended places to create a response ...
        final ListenableFuture<List<AgentResponse>> successful = Futures.successfulAsList(
                Arrays.asList(visited(response), recommended(response)));

        // ... and when we have them, return response to the client.
        Futures.addCallback(successful, new FutureCallback<List<AgentResponse>>() {

            @Override
            public void onSuccess(final List<AgentResponse> result) {
                response.setProcessingTime((System.nanoTime() - time) / 1000000);
                async.resume(response);
            }

            @Override
            public void onFailure(final Throwable t) {
                async.resume(t);
            }
        });
    }

    private ListenableFuture<AgentResponse> visited(final AgentResponse response) {
        destination.register(RxListenableFutureInvokerProvider.class);

        // Get a list of visited destinations ...
        final ListenableFuture<List<Destination>> visited = destination.path("visited").request()
                                                                       // Identify the user.
                                                                       .header("Rx-User", "Guava")
                                                                       // Reactive invoker.
                                                                       .rx(RxListenableFutureInvoker.class)
                                                                       // Return a list of destinations.
                                                                       .get(new GenericType<List<Destination>>() {
                                                                       });

        // ... and set them to the final response.
        return Futures.transform(visited, (AsyncFunction<List<Destination>, AgentResponse>) destinations -> {
            response.setVisited(destinations);

            return Futures.immediateFuture(response);
        });
    }

    private ListenableFuture<AgentResponse> recommended(final AgentResponse response) {
        destination.register(RxListenableFutureInvokerProvider.class);

        // Get a list of recommended destinations ...
        final ListenableFuture<List<Destination>> destinations = destination.path("recommended")
                                                                            .request()
                                                                            // Identify the user.
                                                                            .header("Rx-User", "Guava")
                                                                            // Reactive invoker.
                                                                            .rx(RxListenableFutureInvoker.class)
                                                                            // Return a list of destinations.
                                                                            .get(new GenericType<List<Destination>>() {
                                                                            });

        // ... transform them to Recommendation instances ...
        final ListenableFuture<List<Recommendation>> recommendations = Futures.transform(
                destinations,
                (AsyncFunction<List<Destination>, List<Recommendation>>) destinationList -> {
                    // Create new array list to avoid multiple remote calls.
                    final List<Recommendation> recommendationList = Lists.newArrayList(Lists.transform(
                            destinationList,
                            destination -> new Recommendation(destination.getDestination(), null, 0)));

                    return Futures.immediateFuture(recommendationList);
                });

        // ... add forecasts and calculations ...
        final ListenableFuture<List<List<Recommendation>>> filledRecommendations = Futures
                .successfulAsList(Arrays.asList(
                        // Add Forecasts to Recommendations.
                        forecasts(recommendations),
                        // Add Forecasts to Recommendations.
                        calculations(recommendations)));

        // ... and transform the list into agent response with filled recommendations.
        return Futures
                .transform(filledRecommendations, (AsyncFunction<List<List<Recommendation>>, AgentResponse>) input -> {
                    response.setRecommended(input.get(0));

                    return Futures.immediateFuture(response);
                });
    }

    private ListenableFuture<List<Recommendation>> forecasts(final ListenableFuture<List<Recommendation>> recommendations) {
        forecast.register(RxListenableFutureInvokerProvider.class);

        // Fill the list with weather forecast.
        return Futures.transform(recommendations, (AsyncFunction<List<Recommendation>, List<Recommendation>>) list ->
                // For each recommendation ...
                Futures.successfulAsList(Lists.transform(list, recommendation -> Futures.transform(
                        // ... get the weather forecast ...
                        forecast.resolveTemplate("destination", recommendation.getDestination()).request()
                                .rx(RxListenableFutureInvoker.class)
                                .get(Forecast.class),
                        // ... and set it to the recommendation.
                        (AsyncFunction<Forecast, Recommendation>) forecast -> {
                            recommendation.setForecast(forecast.getForecast());
                            return Futures.immediateFuture(recommendation);
                        }))));
    }

    private ListenableFuture<List<Recommendation>> calculations(final ListenableFuture<List<Recommendation>> recommendations) {
        calculation.register(RxListenableFutureInvokerProvider.class);

        // Fill the list with price calculations.
        return Futures.transform(recommendations, (AsyncFunction<List<Recommendation>, List<Recommendation>>) list ->
                // For each recommendation ...
                Futures.successfulAsList(Lists.transform(list, recommendation -> Futures.transform(
                        // ... get the price calculation ...
                        calculation.resolveTemplate("from", "Moon")
                                   .resolveTemplate("to", recommendation.getDestination())
                                   .request().rx(RxListenableFutureInvoker.class).get(Calculation.class),
                        // ... and set it to the recommendation.
                        (AsyncFunction<Calculation, Recommendation>) calculation -> {
                            recommendation.setPrice(calculation.getPrice());
                            return Futures.immediateFuture(recommendation);
                        })))
        );
    }
}
