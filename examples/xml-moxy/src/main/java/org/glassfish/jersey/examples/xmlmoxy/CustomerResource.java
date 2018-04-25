/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.xmlmoxy;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.examples.xmlmoxy.beans.Address;
import org.glassfish.jersey.examples.xmlmoxy.beans.Customer;
import org.glassfish.jersey.examples.xmlmoxy.beans.PhoneNumber;

@Path("/customer")
public class CustomerResource {

    private static Customer customer = createInitialCustomer();

    @GET
    @Produces(MediaType.APPLICATION_XML)
    public Customer getCustomer() {
        return customer;
    }

    @PUT
    @Consumes(MediaType.APPLICATION_XML)
    public void setCustomer(final Customer c) {
        setCustomerToStatic(c);
    }

    private static Customer createInitialCustomer() {
        final Customer result = new Customer();

        result.setName("Jane Doe");
        result.setAddress(new Address("123 Any Street", "My Town"));
        result.getPhoneNumbers().add(new PhoneNumber("work", "613-555-1111"));
        result.getPhoneNumbers().add(new PhoneNumber("cell", "613-555-2222"));

        return result;
    }

    private static void setCustomerToStatic(final Customer customer) {
        CustomerResource.customer = customer;
    }
}
