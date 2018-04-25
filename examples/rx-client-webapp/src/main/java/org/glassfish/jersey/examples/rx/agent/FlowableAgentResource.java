/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.GenericType;

import javax.inject.Singleton;

import org.glassfish.jersey.client.rx.rxjava2.RxFlowableInvoker;
import org.glassfish.jersey.client.rx.rxjava2.RxFlowableInvokerProvider;
import org.glassfish.jersey.examples.rx.domain.AgentResponse;
import org.glassfish.jersey.examples.rx.domain.Calculation;
import org.glassfish.jersey.examples.rx.domain.Destination;
import org.glassfish.jersey.examples.rx.domain.Forecast;
import org.glassfish.jersey.examples.rx.domain.Recommendation;
import org.glassfish.jersey.server.Uri;

import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;

/**
 * Obtain information about visited (destination) and recommended (destination, forecast, price) places for "RxJava2"
 * user. Uses RxJava2 Flowable and Jersey Client to obtain the data.
 *
 * @author Michal Gajdos
 * @author Pavel Bucek (pavel.bucek at oracle.com)
 */
@Singleton
@Path("agent/flowable")
@Produces("application/json")
public class FlowableAgentResource {

    @Uri("remote/destination")
    private WebTarget destination;

    @Uri("remote/calculation/from/{from}/to/{to}")
    private WebTarget calculation;

    @Uri("remote/forecast/{destination}")
    private WebTarget forecast;

    @GET
    public void flowable(@Suspended final AsyncResponse async) {
        final long time = System.nanoTime();
        final Queue<String> errors = new ConcurrentLinkedQueue<>();

        Flowable.just(new AgentResponse())
                // Obtain visited destinations.
                .zipWith(visited(errors), (agentResponse, visited) -> {
                    agentResponse.setVisited(visited);
                    return agentResponse;
                })
                // Obtain recommended destinations. (does not depend on visited ones)
                .zipWith(
                        recommended(errors),
                        (agentResponse, recommendations) -> {
                            agentResponse.setRecommended(recommendations);
                            return agentResponse;
                        })
                // Observe on another thread than the one processing visited or recommended destinations.
                .observeOn(Schedulers.io())
                // Subscribe.
                .subscribe(agentResponse -> {
                    agentResponse.setProcessingTime((System.nanoTime() - time) / 1000000);
                    async.resume(agentResponse);

                }, async::resume);
    }

    private Flowable<List<Destination>> visited(final Queue<String> errors) {
        destination.register(RxFlowableInvokerProvider.class);

        return destination.path("visited").request()
                          // Identify the user.
                          .header("Rx-User", "RxJava2")
                          // Reactive invoker.
                          .rx(RxFlowableInvoker.class)
                          // Return a list of destinations.
                          .get(new GenericType<List<Destination>>() {
                          })
                          // Handle Errors.
                          .onErrorReturn(throwable -> {
                              errors.offer("Visited: " + throwable.getMessage());
                              return Collections.emptyList();
                          });
    }

    private Flowable<List<Recommendation>> recommended(final Queue<String> errors) {
        destination.register(RxFlowableInvokerProvider.class);

        // Recommended places.
        final Flowable<Destination> recommended = destination.path("recommended").request()
                                                             // Identify the user.
                                                             .header("Rx-User", "RxJava2")
                                                             // Reactive invoker.
                                                             .rx(RxFlowableInvoker.class)
                                                             // Return a list of destinations.
                                                             .get(new GenericType<List<Destination>>() {
                                                             })
                                                             // Handle Errors.
                                                             .onErrorReturn(throwable -> {
                                                                 errors.offer("Recommended: " + throwable
                                                                         .getMessage());
                                                                 return Collections.emptyList();
                                                             })
                                                             // Emit destinations one-by-one.
                                                             .flatMap(Flowable::fromIterable)
                                                             // Remember emitted items for dependant requests.
                                                             .cache();

        forecast.register(RxFlowableInvokerProvider.class);

        // Forecasts. (depend on recommended destinations)
        final Flowable<Forecast> forecasts = recommended.flatMap(destination ->
                forecast
                        .resolveTemplate("destination", destination.getDestination())
                        .request().rx(RxFlowableInvoker.class).get(Forecast.class)
                        .onErrorReturn(throwable -> {
                            errors.offer("Forecast: " + throwable.getMessage());
                            return new Forecast(destination.getDestination(), "N/A");
                        }));

        calculation.register(RxFlowableInvokerProvider.class);

        // Calculations. (depend on recommended destinations)
        final Flowable<Calculation> calculations = recommended.flatMap(destination -> {
            return calculation.resolveTemplate("from", "Moon").resolveTemplate("to", destination.getDestination())
                              .request().rx(RxFlowableInvoker.class).get(Calculation.class)
                              .onErrorReturn(throwable -> {
                                  errors.offer("Calculation: " + throwable.getMessage());
                                  return new Calculation("Moon", destination.getDestination(), -1);
                              });
        });

        return Flowable.zip(recommended, forecasts, calculations, Recommendation::new).buffer(Integer.MAX_VALUE);
    }
}
