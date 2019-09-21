/*
 * Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.helloworld;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;

import org.junit.Test;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * {@link org.glassfish.jersey.examples.helloworld.HelloWorldResource} tests.
 *
 * @author Michal Gajdos
 */
public class HelloWorldTest extends JerseyTest {

    @Override
    protected ResourceConfig configure() {
        enable(TestProperties.LOG_TRAFFIC);

        return new ResourceConfig(HelloWorldResource.class);
    }

    @Test
    public void testGet() {
        final Response response = target().path("helloworld").request("text/plain").get();

        assertThat("Wrong HTTP response code returned.", response.getStatus(), is(200));
        assertThat("Unexpected response entity.", response.readEntity(String.class), is(HelloWorldResource.CLICHED_MESSAGE));
    }

    @Test
    public void testGetLocator() {
        final Response response = target().path("helloworld").path("locator").request("text/plain").get();

        assertThat("Wrong HTTP response code returned.", response.getStatus(), is(200));
        assertThat("Unexpected response entity.", response.readEntity(String.class), is(HelloWorldResource.CLICHED_MESSAGE));
    }

    @Test
    public void testPost() {
        final Response response = target().path("helloworld")
                .request("text/plain")
                .post(Entity.text(HelloWorldResource.CLICHED_MESSAGE));

        assertThat("Wrong HTTP response code returned.", response.getStatus(), is(200));
        assertThat("Unexpected response entity.", response.readEntity(String.class), is(HelloWorldResource.CLICHED_MESSAGE));
    }

    @Test
    public void testPostLocator() {
        final Response response = target().path("helloworld").path("locator")
                .request("text/plain")
                .post(Entity.text(HelloWorldResource.CLICHED_MESSAGE));

        assertThat("Wrong HTTP response code returned.", response.getStatus(), is(200));
        assertThat("Unexpected response entity.", response.readEntity(String.class), is(HelloWorldResource.CLICHED_MESSAGE));
    }

    @Test
    public void testPut() {
        final Response response = target().path("helloworld")
                .request("text/plain")
                .put(Entity.text(HelloWorldResource.CLICHED_MESSAGE));

        assertThat("Wrong HTTP response code returned.", response.getStatus(), is(204));
    }

    @Test
    public void testPutLocator() {
        final Response response = target().path("helloworld").path("locator")
                .request("text/plain")
                .put(Entity.text(HelloWorldResource.CLICHED_MESSAGE));

        assertThat("Wrong HTTP response code returned.", response.getStatus(), is(204));
    }
}
