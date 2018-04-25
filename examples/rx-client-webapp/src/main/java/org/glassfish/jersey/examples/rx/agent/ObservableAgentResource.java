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
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.GenericType;

import javax.inject.Singleton;

import org.glassfish.jersey.client.rx.rxjava.RxObservableInvoker;
import org.glassfish.jersey.client.rx.rxjava.RxObservableInvokerProvider;
import org.glassfish.jersey.examples.rx.domain.AgentResponse;
import org.glassfish.jersey.examples.rx.domain.Calculation;
import org.glassfish.jersey.examples.rx.domain.Destination;
import org.glassfish.jersey.examples.rx.domain.Forecast;
import org.glassfish.jersey.examples.rx.domain.Recommendation;
import org.glassfish.jersey.server.Uri;

import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * Obtain information about visited (destination) and recommended (destination, forecast, price) places for "RxJava" user. Uses
 * RxJava Observable and Jersey Client to obtain the data.
 *
 * @author Michal Gajdos
 * @author Pavel Bucek (pavel.bucek at oracle.com)
 */
@Singleton
@Path("agent/observable")
@Produces("application/json")
public class ObservableAgentResource {

    @Uri("remote/destination")
    private WebTarget destination;

    @Uri("remote/calculation/from/{from}/to/{to}")
    private WebTarget calculation;

    @Uri("remote/forecast/{destination}")
    private WebTarget forecast;

    @GET
    public void observable(@Suspended final AsyncResponse async) {
        final long time = System.nanoTime();
        final Queue<String> errors = new ConcurrentLinkedQueue<>();

        Observable.just(new AgentResponse())
                  // Obtain visited destinations.
                  .zipWith(visited(errors), (response, visited) -> {
                      response.setVisited(visited);
                      return response;
                  })
                  // Obtain recommended destinations. (does not depend on visited ones)
                  .zipWith(recommended(errors), (response, recommendations) -> {
                      response.setRecommended(recommendations);
                      return response;
                  })
                  // Observe on another thread than the one processing visited or recommended destinations.
                  .observeOn(Schedulers.io())
                  // Subscribe.
                  .subscribe(response -> {
                      // Do something with errors.

                      response.setProcessingTime((System.nanoTime() - time) / 1000000);
                      async.resume(response);
                  }, async::resume);
    }

    private Observable<List<Destination>> visited(final Queue<String> errors) {
        destination.register(RxObservableInvokerProvider.class);

        return destination.path("visited").request()
                          // Identify the user.
                          .header("Rx-User", "RxJava")
                          // Reactive invoker.
                          .rx(RxObservableInvoker.class)
                          // Return a list of destinations.
                          .get(new GenericType<List<Destination>>() {
                          })
                          // Handle Errors.
                          .onErrorReturn(throwable -> {
                              errors.offer("Visited: " + throwable.getMessage());
                              return Collections.emptyList();
                          });
    }

    private Observable<List<Recommendation>> recommended(final Queue<String> errors) {
        destination.register(RxObservableInvokerProvider.class);

        // Recommended places.
        final Observable<Destination> recommended = destination.path("recommended").request()
                                                               // Identify the user.
                                                               .header("Rx-User", "RxJava")
                                                               // Reactive invoker.
                                                               .rx(RxObservableInvoker.class)
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
                                                               .flatMap(Observable::from)
                                                               // Remember emitted items for dependant requests.
                                                               .cache();

        forecast.register(RxObservableInvokerProvider.class);

        // Forecasts. (depend on recommended destinations)
        final Observable<Forecast> forecasts = recommended.flatMap(destination ->
                forecast
                        .resolveTemplate("destination", destination.getDestination())
                        .request().rx(RxObservableInvoker.class).get(Forecast.class)
                        .onErrorReturn(throwable -> {
                            errors.offer("Forecast: " + throwable.getMessage());
                            return new Forecast(destination.getDestination(), "N/A");
                        }));

        calculation.register(RxObservableInvokerProvider.class);

        // Calculations. (depend on recommended destinations)
        final Observable<Calculation> calculations = recommended.flatMap(destination ->
                calculation.resolveTemplate("from", "Moon").resolveTemplate("to", destination.getDestination())
                           .request().rx(RxObservableInvoker.class).get(Calculation.class)
                           .onErrorReturn(throwable -> {
                               errors.offer("Calculation: " + throwable.getMessage());
                               return new Calculation("Moon", destination.getDestination(), -1);
                           }));

        return Observable.zip(recommended, forecasts, calculations, Recommendation::new).toList();
    }
}
