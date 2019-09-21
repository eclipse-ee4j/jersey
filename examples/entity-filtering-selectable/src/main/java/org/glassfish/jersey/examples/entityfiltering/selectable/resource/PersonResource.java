/*
 * Copyright (c) 2014, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.entityfiltering.selectable.resource;

import java.util.ArrayList;
import java.util.HashMap;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.glassfish.jersey.examples.entityfiltering.selectable.domain.Address;
import org.glassfish.jersey.examples.entityfiltering.selectable.domain.Person;
import org.glassfish.jersey.examples.entityfiltering.selectable.domain.PhoneNumber;

/**
 * Resource to support query parameter driven entity filtering.
 *
 * @author Andy Pemberton (pembertona at gmail.com)
 */
@Path("people")
@Produces("application/json")
public class PersonResource {

    @GET
    @Path("{id}")
    public Person getPerson() {
        final Person person = new Person();
        person.setGivenName("Andrew");
        person.setFamilyName("Dowd");
        person.setHonorificPrefix("Mr.");
        person.setHonorificSuffix("PhD");
        person.setRegion("1st Level Region");

        final ArrayList<Address> addresses = new ArrayList<>();
        person.setAddresses(addresses);

        final Address address = new Address();
        addresses.add(address);
        address.setRegion("2nd Level Region");
        address.setStreetAddress("1234 fake st.");
        address.setPhoneNumber(new PhoneNumber());
        address.getPhoneNumber().setNumber("867-5309");
        address.getPhoneNumber().setAreaCode("540");

        person.setPhoneNumbers(new HashMap<String, PhoneNumber>());
        final PhoneNumber number = new PhoneNumber();
        number.setAreaCode("804");
        number.setNumber("867-5309");
        person.getPhoneNumbers().put("HOME", number);

        return person;
    }

    @GET
    @Path("{id}/addresses")
    public Address getAddress() {
        return this.getPerson().getAddresses().get(0);
    }
}
