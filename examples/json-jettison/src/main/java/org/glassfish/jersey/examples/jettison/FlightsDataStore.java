/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.jettison;

/**
 * TODO javadoc.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public class FlightsDataStore {

    private static volatile Flights flightsData = initFlightsData();

    public static void init() {
        init(initFlightsData());
    }

    public static void init(final Flights flights) {
        flightsData = flights;
    }

    public static Flights getFlights() {
        return flightsData;
    }

    private static Flights initFlightsData() {
        Flights flights = new Flights();
        FlightType flight123 = new FlightType();
        flight123.setCompany("Czech Airlines");
        flight123.setNumber(123);
        flight123.setFlightId("OK123");
        flight123.setAircraft("B737");
        FlightType flight124 = new FlightType();
        flight124.setCompany("Czech Airlines");
        flight124.setNumber(124);
        flight124.setFlightId("OK124");
        flight124.setAircraft("AB115");
        flights.getFlight().add(flight123);
        flights.getFlight().add(flight124);

        return flights;
    }
}
