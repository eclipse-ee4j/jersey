/*
 * Copyright (c) 2014, 2020 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.rx.remote;

import java.util.Random;

import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;

import org.glassfish.jersey.examples.rx.Helper;
import org.glassfish.jersey.examples.rx.domain.Calculation;
import org.glassfish.jersey.server.ManagedAsync;

/**
 * Obtain a calculation for a trip from one destination to another.
 *
 * @author Michal Gajdos
 */
@Path("remote/calculation")
@Produces("application/json")
public class CalculationResource {

    @GET
    @ManagedAsync
    @Path("/from/{from}/to/{to}")
    public Calculation calculation(@PathParam("from") @DefaultValue("Moon") final String from,
                                   @PathParam("to") final String to) {
        // Simulate long-running operation.
        Helper.sleep(350);

        return new Calculation(from, to, new Random().nextInt(10000));
    }
}
