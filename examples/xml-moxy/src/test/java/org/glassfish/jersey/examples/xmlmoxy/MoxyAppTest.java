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

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.examples.xmlmoxy.beans.Customer;
import org.glassfish.jersey.moxy.xml.MoxyXmlFeature;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
public class MoxyAppTest extends JerseyTest {

    @Override
    protected Application configure() {
        enable(TestProperties.LOG_TRAFFIC);
        enable(TestProperties.DUMP_ENTITY);

        return App.createApp();
    }

    @Override
    protected void configureClient(ClientConfig config) {
        config.register(new MoxyXmlFeature());
    }

    /**
     * Test that the expected response is sent back.
     * @throws java.lang.Exception
     */
    @Test
    public void testCustomer() throws Exception {
        final WebTarget webTarget = target().path("customer");

        Customer customer = webTarget.request(MediaType.APPLICATION_XML).get(Customer.class);
        customer.setName("Tom Dooley");
        webTarget.request(MediaType.APPLICATION_XML).put(Entity.entity(customer, MediaType.APPLICATION_XML));

        Customer updatedCustomer = webTarget.request(MediaType.APPLICATION_XML).get(Customer.class);
        assertEquals(customer, updatedCustomer);
    }
}
