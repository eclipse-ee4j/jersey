/*
 * Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.performance.benchmark.server;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;

import org.junit.Test;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author Michal Gajdos
 */
public class LocatorTest extends JerseyTest {

    @Override
    protected ResourceConfig configure() {
        enable(TestProperties.LOG_TRAFFIC);

        return new LocatorApplication();
    }

    @Test
    public void testResourceMethod() {
        final Response response = target().path("resource").request("text/plain").get();

        assertThat("Wrong HTTP response code returned.", response.getStatus(), is(200));
        assertThat("Unexpected response entity.", response.readEntity(String.class), is("Hello World!"));
    }

    @Test
    public void testSubResourceMethod() {
        final Response response = target().path("resource").path("locator").request("text/plain").get();

        assertThat("Wrong HTTP response code returned.", response.getStatus(), is(200));
        assertThat("Unexpected response entity.", response.readEntity(String.class), is("Hello World!"));
    }

    @Test
    public void testPost() {
        final Response response = target().path("resource")
                .request("text/plain")
                .post(Entity.text("Hello World!"));

        assertThat("Wrong HTTP response code returned.", response.getStatus(), is(200));
        assertThat("Unexpected response entity.", response.readEntity(String.class), is("Hello World!"));
    }

    @Test
    public void testPostLocator() {
        final Response response = target().path("resource").path("locator")
                .request("text/plain")
                .post(Entity.text("Hello World!"));

        assertThat("Wrong HTTP response code returned.", response.getStatus(), is(200));
        assertThat("Unexpected response entity.", response.readEntity(String.class), is("Hello World!"));
    }

    @Test
    public void testPut() {
        final Response response = target().path("resource")
                .request("text/plain")
                .put(Entity.text("Hello World!"));

        assertThat("Wrong HTTP response code returned.", response.getStatus(), is(204));
    }

    @Test
    public void testPutLocator() {
        final Response response = target().path("resource").path("locator")
                .request("text/plain")
                .put(Entity.text("Hello World!"));

        assertThat("Wrong HTTP response code returned.", response.getStatus(), is(204));
    }
}
