/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.jettison;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

/**
 * TODO javadoc.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 * @author Marek Potociar (marek.potociar at oracle.com)
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
