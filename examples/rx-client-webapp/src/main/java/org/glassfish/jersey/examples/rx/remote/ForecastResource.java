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

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.glassfish.jersey.examples.rx.Helper;
import org.glassfish.jersey.examples.rx.domain.Forecast;
import org.glassfish.jersey.server.ManagedAsync;

/**
 * Obtain current weather conditions in a destination.
 *
 * @author Michal Gajdos
 */
@Path("remote/forecast")
@Produces("application/json")
public class ForecastResource {

    @GET
    @ManagedAsync
    @Path("/{destination}")
    public Forecast forecast(@PathParam("destination") final String destination) {
        // Simulate long-running operation.
        Helper.sleep(350);

        return new Forecast(destination, Helper.getForecast());
    }
}
