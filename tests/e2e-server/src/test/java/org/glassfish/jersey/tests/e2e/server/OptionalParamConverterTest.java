/*
 * Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved.
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

import java.util.List;
import java.util.Optional;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class OptionalParamConverterTest extends JerseyTest {

    private static final String PARAM_NAME = "paramName";

    @Path("/OptionalResource")
    public static class OptionalResource {

        @GET
        @Path("/fromString")
        public Response fromString(@QueryParam(PARAM_NAME) Optional<String> data) {
            return Response.ok(data.orElse("")).build();
        }

        @GET
        @Path("/fromInteger")
        public Response fromInteger(@QueryParam(PARAM_NAME) Optional<Integer> data) {
            return Response.ok(data.orElse(0)).build();
        }

        @GET
        @Path("/fromList")
        public Response fromList(@QueryParam(PARAM_NAME) List<Optional<Integer>> data) {
            StringBuilder builder = new StringBuilder("");
            for (Optional<Integer> val : data) {
                builder.append(val.orElse(0));
            }
            return Response.ok(builder.toString()).build();
        }
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(OptionalResource.class);
    }

    @Test
    public void fromOptionalStr() {
        Response empty = target("/OptionalResource/fromString").request().get();
        Response notEmpty = target("/OptionalResource/fromString").queryParam(PARAM_NAME, "anyValue").request().get();
        assertEquals(200, empty.getStatus());
        assertEquals("", empty.readEntity(String.class));
        assertEquals(200, notEmpty.getStatus());
        assertEquals("anyValue", notEmpty.readEntity(String.class));
    }

    @Test
    public void fromOptionalInt() {
        Response empty = target("/OptionalResource/fromInteger").request().get();
        Response notEmpty = target("/OptionalResource/fromInteger").queryParam(PARAM_NAME, 1).request().get();
        assertEquals(200, empty.getStatus());
        assertEquals(Integer.valueOf(0), empty.readEntity(Integer.class));
        assertEquals(200, notEmpty.getStatus());
        assertEquals(Integer.valueOf(1), notEmpty.readEntity(Integer.class));
    }

    @Test
    public void fromOptionalList() {
        Response empty = target("/OptionalResource/fromList").request().get();
        Response notEmpty = target("/OptionalResource/fromList").queryParam(PARAM_NAME, 1)
                .queryParam(PARAM_NAME, 2).request().get();
        assertEquals(200, empty.getStatus());
        assertEquals("", empty.readEntity(String.class));
        assertEquals(200, notEmpty.getStatus());
        assertEquals("12", notEmpty.readEntity(String.class));
    }
}
