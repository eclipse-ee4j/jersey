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

import java.util.List;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jettison.JettisonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public class JsonJettisonTest extends JerseyTest {

    @Override
    protected ResourceConfig configure() {
        enable(TestProperties.LOG_TRAFFIC);

        return App.createApp();
    }

    @Override
    protected void configureClient(ClientConfig config) {
        config.register(new JettisonFeature()).register(JaxbContextResolver.class);
    }

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();

        // reset static flights list
        target().path("flights/init").request("application/json").post(null);
    }

    /**
     * Test checks that the application.wadl is reachable.
     * <p/>
     */
    @Test
    public void testApplicationWadl() {
        String applicationWadl = target().path("application.wadl").request().get(String.class);
        assertTrue("Something wrong. Returned wadl length is not > 0", applicationWadl.length() > 0);
    }

    /**
     * Test check GET on the "flights" resource in "application/json" format.
     */
    @Test
    public void testGetOnFlightsJSONFormat() {
        // get the initial representation
        Flights flights = target().path("flights").request("application/json").get(Flights.class);
        // check that there are two flight entries
        assertEquals("Expected number of initial entries not found", 2, flights.getFlight().size());
    }

    /**
     * Test checks PUT on the "flights" resource in "application/json" format.
     */
    @Test
    public void testPutOnFlightsJSONFormat() {
        // get the initial representation
        Flights flights = target().path("flights")
                .request("application/json").get(Flights.class);
        // check that there are two flight entries
        assertEquals("Expected number of initial entries not found", 2, flights.getFlight().size());

        // remove the second flight entry
        if (flights.getFlight().size() > 1) {
            flights.getFlight().remove(1);
        }

        // update the first entry
        flights.getFlight().get(0).setNumber(125);
        flights.getFlight().get(0).setFlightId("OK125");

        // and send the updated list back to the server
        target().path("flights").request().put(Entity.json(flights));

        // get the updated list out from the server:
        Flights updatedFlights = target().path("flights").request("application/json").get(Flights.class);
        //check that there is only one flight entry
        assertEquals("Remaining number of flight entries do not match the expected value", 1, updatedFlights.getFlight().size());
        // check that the flight entry in retrieved list has FlightID OK!@%
        assertEquals("Retrieved flight ID doesn't match the expected value", "OK125",
                updatedFlights.getFlight().get(0).getFlightId());
    }

    /**
     * Test checks GET on "flights" resource with mime-type "application/xml".
     */
    @Test
    public void testGetOnFlightsXMLFormat() {
        // get the initial representation
        Flights flights = target().path("flights").request("application/xml").get(Flights.class);
        // check that there are two flight entries
        assertEquals("Expected number of initial entries not found", 2, flights.getFlight().size());
    }

    /**
     * Test checks PUT on "flights" resource with mime-type "application/xml".
     */
    @Test
    public void testPutOnFlightsXMLFormat() {
        // get the initial representation
        Flights flights = target().path("flights").request("application/XML").get(Flights.class);
        // check that there are two flight entries
        assertEquals("Expected number of initial entries not found", 2, flights.getFlight().size());

        // remove the second flight entry
        if (flights.getFlight().size() > 1) {
            flights.getFlight().remove(1);
        }

        // update the first entry
        flights.getFlight().get(0).setNumber(125);
        flights.getFlight().get(0).setFlightId("OK125");

        // and send the updated list back to the server
        target().path("flights").request().put(Entity.xml(flights));

        // get the updated list out from the server:
        Flights updatedFlights = target().path("flights").request("application/XML").get(Flights.class);
        //check that there is only one flight entry
        assertEquals("Remaining number of flight entries do not match the expected value", 1, updatedFlights.getFlight().size());
        // check that the flight entry in retrieved list has FlightID OK!@%
        assertEquals("Retrieved flight ID doesn't match the expected value", "OK125",
                updatedFlights.getFlight().get(0).getFlightId());
    }

    /**
     * Test check GET on the "aircrafts" resource in "application/json" format.
     */
    @Test
    public void testGetOnAircraftsJSONFormat() {
        GenericType<List<AircraftType>> listOfAircrafts = new GenericType<List<AircraftType>>() {
        };
        // get the initial representation
        List<AircraftType> aircraftTypes = target().path("aircrafts").request("application/json").get(listOfAircrafts);
        // check that there are two aircraft type entries
        assertEquals("Expected number of initial aircraft types not found", 2, aircraftTypes.size());
    }
}
