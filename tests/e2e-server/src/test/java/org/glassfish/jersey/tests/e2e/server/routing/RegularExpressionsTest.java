/*
 * Copyright (c) 2013, 2019 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.e2e.server.routing;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class RegularExpressionsTest extends JerseyTest {
    private static final String GET_VALUE = "get value";
    private static final String POST_VALUE = "post value";
    private static final String PUT_VALUE = "put value";


    @Path("one")
    public static class ResourceOne {
        @POST
        public String post(String entity) {
            return entity;
        }

        @GET
        @Path("x")
        public Response get() {
            return Response.ok(GET_VALUE).build();

        }

        @POST
        @Path("{name:[a-zA-Z][a-zA-Z_0-9]*}")
        public Response post() {
            return Response.ok(POST_VALUE).build();

        }

        @Path("{x:[a-z]}")
        public SubGet doAnything4() {
            return new SubGet();
        }
    }

    @Path("two")
    public static class ResourceTwo {
        @GET
        @Path("{Prefix}{p:/?}{id: ((\\d+)?)}/abc{p2:/?}{number: (([A-Za-z0-9]*)?)}")
        public Response get() {
            return Response.ok(GET_VALUE).build();

        }

        @POST
        @Path("{Prefix}{p:/?}{id: ((\\d+)?)}/abc/{yeah}")
        public Response post() {
            return Response.ok(POST_VALUE).build();

        }
    }

    public static class SubGet {
        @PUT
        public String get() {
            return PUT_VALUE;
        }
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(ResourceOne.class, ResourceTwo.class);
    }

    @Test
    public void testPostOne() {
        String entity = target("one").path("x").request()
                .buildPost(Entity.entity("AA", MediaType.TEXT_PLAIN_TYPE)).invoke().readEntity(String.class);
        assertThat(entity, is(POST_VALUE));
    }

    @Test
    public void testGetOne() {
        String entity = target("one").path("x").request().buildGet().invoke().readEntity(String.class);
        assertThat(entity, is(GET_VALUE));
    }

    @Test
    public void testPostTwo() {
        String entity = target("two").path("P/abc/MyNumber").request()
                .buildPost(Entity.entity("AA", MediaType.TEXT_PLAIN_TYPE)).invoke().readEntity(String.class);
        assertThat(entity, is(POST_VALUE));
    }

    @Test
    public void testGetTwo() {
        String entity = target("two").path("P/abc/MyNumber").request().buildGet().invoke().readEntity(String.class);
        assertThat(entity, is(GET_VALUE));
    }

    @Test
    /**
     * By the Spec, sub-resource locator should not be found in this case
     */
    public void testPutOne() {
        try (Response response = target("one").path("x").request()
                .buildPut(Entity.entity("AA", MediaType.TEXT_PLAIN_TYPE)).invoke()) {
            assertThat(response.getStatus(), is(405));
        }
    }
}
