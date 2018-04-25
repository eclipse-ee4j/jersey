/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package org.glassfish.jersey.jetty.connector;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import javax.xml.bind.annotation.XmlRootElement;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Tests the Http content negotiation.
 *
 * @author Arul Dhesiaseelan (aruld at acm.org)
 */
public class EntityTest extends JerseyTest {

    private static final Logger LOGGER = Logger.getLogger(EntityTest.class.getName());

    private static final String PATH = "test";

    @Path("/test")
    public static class EntityResource {

        @GET
        public Person get() {
            return new Person("John", "Doe");
        }

        @POST
        public Person post(Person entity) {
            return entity;
        }

    }

    @XmlRootElement
    public static class Person {

        private String firstName;
        private String lastName;

        public Person() {
        }

        public Person(String firstName, String lastName) {
            this.firstName = firstName;
            this.lastName = lastName;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        @Override
        public String toString() {
            return firstName + " " + lastName;
        }
    }

    @Override
    protected Application configure() {
        ResourceConfig config = new ResourceConfig(EntityResource.class, JacksonFeature.class);
        config.register(new LoggingFeature(LOGGER, LoggingFeature.Verbosity.PAYLOAD_ANY));
        return config;
    }

    @Override
    protected void configureClient(ClientConfig config) {
        config.connectorProvider(new JettyConnectorProvider())
                .register(JacksonFeature.class);
    }

    @Test
    public void testGet() {
        Response response = target(PATH).request(MediaType.APPLICATION_XML_TYPE).get();
        Person person = response.readEntity(Person.class);
        assertEquals("John Doe", person.toString());
        response = target(PATH).request(MediaType.APPLICATION_JSON_TYPE).get();
        person = response.readEntity(Person.class);
        assertEquals("John Doe", person.toString());
    }

    @Test
    public void testGetAsync() throws ExecutionException, InterruptedException {
        Response response = target(PATH).request(MediaType.APPLICATION_XML_TYPE).async().get().get();
        Person person = response.readEntity(Person.class);
        assertEquals("John Doe", person.toString());
        response = target(PATH).request(MediaType.APPLICATION_JSON_TYPE).async().get().get();
        person = response.readEntity(Person.class);
        assertEquals("John Doe", person.toString());
    }

    @Test
    public void testPost() {
        Response response = target(PATH).request(MediaType.APPLICATION_XML_TYPE).post(Entity.xml(new Person("John", "Doe")));
        Person person = response.readEntity(Person.class);
        assertEquals("John Doe", person.toString());
        response = target(PATH).request(MediaType.APPLICATION_JSON_TYPE).post(Entity.xml(new Person("John", "Doe")));
        person = response.readEntity(Person.class);
        assertEquals("John Doe", person.toString());
    }

    @Test
    public void testPostAsync() throws ExecutionException, InterruptedException, TimeoutException {
        Response response = target(PATH).request(MediaType.APPLICATION_XML_TYPE).async()
                .post(Entity.xml(new Person("John", "Doe"))).get();
        Person person = response.readEntity(Person.class);
        assertEquals("John Doe", person.toString());
        response = target(PATH).request(MediaType.APPLICATION_JSON_TYPE).async().post(Entity.xml(new Person("John", "Doe")))
                .get();
        person = response.readEntity(Person.class);
        assertEquals("John Doe", person.toString());
    }
}
