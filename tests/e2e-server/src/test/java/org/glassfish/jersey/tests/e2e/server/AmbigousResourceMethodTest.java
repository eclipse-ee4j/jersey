/*
 * Copyright (c) 2012, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.e2e.server;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Test of validation of resources as an end-to-end test.
 *
 * @author Miroslav Fuksa
 *
 */
public class AmbigousResourceMethodTest extends JerseyTest {

    @Override
    protected Application configure() {
        return new ResourceConfig(TestResource.class);
    }

    @Test
    public void testRequestToAmbiguousResourceClass() {
        final String simpleName = TestResource.class.getSimpleName();

        Response response = target().path("test").request(MediaType.TEXT_PLAIN).get();
        assertEquals(200, response.getStatus());
        assertEquals(simpleName + simpleName, response.readEntity(String.class));

        response = target().path("test").request(MediaType.TEXT_HTML_TYPE).get();
        assertEquals(200, response.getStatus());
        assertEquals(simpleName, response.readEntity(String.class));

        response = target().path("test").request(MediaType.TEXT_HTML_TYPE).post(Entity.entity("aaaa", MediaType.TEXT_PLAIN_TYPE));
        assertEquals(200, response.getStatus());
        assertEquals(simpleName + simpleName, response.readEntity(String.class));

        response = target().path("test").request(MediaType.TEXT_HTML_TYPE).post(Entity.entity("aaaa", MediaType.TEXT_HTML_TYPE));
        assertEquals(200, response.getStatus());
        assertEquals(simpleName, response.readEntity(String.class));
    }

    /**
     * Test ambiguous resource class.
     */
    @Path("test")
    public static class TestResource {
        @POST
        public String sub() {
            return getClass().getSimpleName();
        }

        @POST
        @Consumes(MediaType.TEXT_PLAIN)
        public String subsub() {
            return sub() + sub();
        }

        @GET
        public String get() {
            return sub();
        }

        @GET
        @Produces(MediaType.TEXT_PLAIN)
        public String getget() {
            return subsub();
        }
    }
}
