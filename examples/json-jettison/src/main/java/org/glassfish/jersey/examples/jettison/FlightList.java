/*
 * Copyright (c) 2010, 2020 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.jettison;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

/**
 * TODO javadoc.
 *
 * @author Jakub Podlesak
 * @author Marek Potociar
 */
@Path(value = "/flights")
public class FlightList {

    @GET
    @Produces({"application/json", "application/xml"})
    public Flights getFlightList() {
        return FlightsDataStore.getFlights();
    }

    @PUT
    @Consumes({"application/json", "application/xml"})
    public void putFlightList(Flights flights) {
        FlightsDataStore.init(flights);
    }

    @POST
    @Path("init")
    @Produces({"application/json", "application/xml"})
    public void initData() {
        FlightsDataStore.init();
    }

}
