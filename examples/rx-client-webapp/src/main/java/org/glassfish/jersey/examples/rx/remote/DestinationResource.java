/*
 * Copyright (c) 2014, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.rx.remote;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import javax.inject.Singleton;

import org.glassfish.jersey.examples.rx.Helper;
import org.glassfish.jersey.examples.rx.domain.Destination;
import org.glassfish.jersey.internal.util.collection.Views;
import org.glassfish.jersey.server.ManagedAsync;

/**
 * Obtain a list of visited / recommended places for a given user.
 *
 * @author Michal Gajdos
 */
@Singleton
@Path("remote/destination")
@Produces("application/json")
public class DestinationResource {

    private static final Map<String, List<String>> VISITED = new HashMap<>();

    static {
        VISITED.put("Sync", Helper.getCountries(5));
        VISITED.put("Async", Helper.getCountries(5));
        VISITED.put("Guava", Helper.getCountries(5));
        VISITED.put("RxJava", Helper.getCountries(5));
        VISITED.put("RxJava2", Helper.getCountries(5));
        VISITED.put("CompletionStage", Helper.getCountries(5));
    }

    @GET
    @ManagedAsync
    @Path("visited")
    public List<Destination> visited(@HeaderParam("Rx-User") @DefaultValue("KO") final String user) {
        // Simulate long-running operation.
        Helper.sleep();

        if (!VISITED.containsKey(user)) {
            VISITED.put(user, Helper.getCountries(5));
        }

        return Views.listView(VISITED.get(user), Destination::new);
    }

    @GET
    @ManagedAsync
    @Path("recommended")
    public List<Destination> recommended(@HeaderParam("Rx-User") @DefaultValue("KO") final String user,
                                         @QueryParam("limit") @DefaultValue("5") final int limit) {
        // Simulate long-running operation.
        Helper.sleep();

        if (!VISITED.containsKey(user)) {
            VISITED.put(user, Helper.getCountries(5));
        }

        return Views.listView(Helper.getCountries(limit, VISITED.get(user)), Destination::new);
    }
}
