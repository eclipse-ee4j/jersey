/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.e2e.server.filter;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.filter.HttpMethodOverrideFilter;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 *
 * @author Martin Matula
 */
public class PostToPutDeleteTest extends JerseyTest {

    @Path("/")
    public static class Resource {

        @GET
        public String get(@QueryParam("a") String a) {
            return "GET: " + a;
        }

        @PUT
        public String put() {
            return "PUT";
        }

        @DELETE
        public String delete() {
            return "DELETE";
        }

        @POST
        public String post() {
            return "POST";
        }
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(HttpMethodOverrideFilter.class, Resource.class);
    }

    @Test
    public void testPut() {
        assertResponseEquals("PUT,PUT,PUT,", _test("PUT"));
    }

    @Test
    public void testDelete() {
        assertResponseEquals("DELETE,DELETE,DELETE,", _test("DELETE"));
    }

    @Test
    public void testGet() {
        assertResponseEquals("GET: test,GET: test,GET: test,", _test("GET"));
    }

    @Test
    public void testConflictingMethods() {
        Response cr = target("/").queryParam("_method", "PUT").request()
                .header("X-HTTP-Method-Override", "DELETE").post(Entity.text(""));
        assertEquals(400, cr.getStatus());
    }

    @Test
    public void testUnsupportedMethod() {
        assertResponseEquals("405,405,405,", _test("PATCH"));
    }

    @Test
    public void testGetWithQueryParam() {
        String result = target().queryParam("_method", "GET").queryParam("a", "test").request().post(null, String.class);
        assertEquals("GET: test", result);
    }

    @Test
    public void testGetWithOtherEntity() {
        String result = target().queryParam("_method", "GET").request().post(Entity.text("a=test"), String.class);
        assertEquals("GET: null", result);
    }

    @Test
    public void testPlainPost() {
        String result = target().request().post(null, String.class);
        assertEquals("POST", result);
    }

    public Response[] _test(String method) {
        Response[] result = new Response[3];
        WebTarget target = target();

        result[0] = target.request().header("X-HTTP-Method-Override", method)
                .post(Entity.form(new Form().param("a", "test")));
        result[1] = target.queryParam("_method", method).request()
                .post(Entity.form(new Form().param("a", "test")));
        result[2] = target.queryParam("_method", method).request().header("X-HTTP-Method-Override", method)
                .post(Entity.form(new Form().param("a", "test")));
        return result;
    }

    public void assertResponseEquals(String expected, Response[] responses) {
        StringBuilder result = new StringBuilder();

        for (Response r : responses) {
            if (r.getStatus() == Response.Status.OK.getStatusCode()) {
                result.append(r.readEntity(String.class));
            } else {
                result.append(r.getStatus());
            }
            result.append(",");
        }

        assertEquals(expected, result.toString());
    }
}
