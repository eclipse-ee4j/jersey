/*
 * Copyright (c) 2024 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.server.internal.routing;

import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.glassfish.jersey.server.ApplicationHandler;
import org.glassfish.jersey.server.ExtendedUriInfo;
import org.glassfish.jersey.server.RequestContextBuilder;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;

public class RegularExpressionTest {

    public static final String ONE_POST = "{name:[a-zA-Z][a-zA-Z_0-9]*}";
    public static final String TWO_GET = "{Prefix}{p:/?}{id: ((\\d+)?)}/abc{p2:/?}{number: (([A-Za-z0-9]*)?)}";
    public static final String TWO_POST = "{Prefix}{p:/?}{id: ((\\d+)?)}/abc/{yeah}";
    public static final String THREE_SUB = "{x:[a-z]}";

    public static class SubGet {
        @PUT
        public String get(@Context UriInfo uriInfo) {
            return ((ExtendedUriInfo) uriInfo).getResourceTemplate();
        }
    }

    @Path("one")
    public static class ResourceOne {
        @POST
        @Path(ONE_POST)
        public Response post(@Context UriInfo info) {
            return Response.ok(((ExtendedUriInfo) info).getResourceTemplate()).build();
        }
    }

    @Path("two")
    public static class ResourceTwo {
        @GET
        @Path(TWO_GET)
        public Response get(@Context UriInfo info) {
            return Response.ok(((ExtendedUriInfo) info).getResourceTemplate()).build();
        }

        @POST
        @Path(TWO_POST)
        public Response post(@Context UriInfo info) {
            return Response.ok(((ExtendedUriInfo) info).getResourceTemplate()).build();
        }
    }

    @Path("three")
    public static class ResourceThree {
        @Path(THREE_SUB)
        public SubGet doAnything4() {
            return new SubGet();
        }
    }

    @Path("four")
    public static class ResourceFour {
        @Path(THREE_SUB)
        public ResourceOne postOne() {
            return new ResourceOne();
        }
    }

    @Test
    public void testPOSTone() throws ExecutionException, InterruptedException {
        ApplicationHandler applicationHandler = new ApplicationHandler(new ResourceConfig(ResourceOne.class));
        Object entity = applicationHandler.apply(RequestContextBuilder.from("/one/azazaz00", HttpMethod.POST)
                .build()).get().getEntity();
        Assertions.assertEquals("/one/" + ONE_POST, entity);
    }

    @Test
    public void testTWOget() throws ExecutionException, InterruptedException {
        ApplicationHandler applicationHandler = new ApplicationHandler(new ResourceConfig(ResourceTwo.class));
        Object entity = applicationHandler.apply(RequestContextBuilder.from("/two/P/abc/MyNumber", HttpMethod.GET)
                .build()).get().getEntity();
        Assertions.assertEquals("/two/" + TWO_GET, entity);
    }

    @Test
    public void testTWOpost() throws ExecutionException, InterruptedException {
        ApplicationHandler applicationHandler = new ApplicationHandler(new ResourceConfig(ResourceTwo.class));
        Object entity = applicationHandler.apply(RequestContextBuilder.from("/two/P/abc/MyNumber", HttpMethod.POST)
                .build()).get().getEntity();
        Assertions.assertEquals("/two/" + TWO_POST, entity);
    }

    @Test
    public void testPUTthree() throws ExecutionException, InterruptedException {
        ApplicationHandler applicationHandler = new ApplicationHandler(new ResourceConfig(ResourceThree.class));
        Object entity = applicationHandler.apply(RequestContextBuilder.from("/three/a", HttpMethod.PUT)
                .build()).get().getEntity();
        Assertions.assertEquals("/three/" + THREE_SUB, entity);
    }

    @Test
    public void testPOSTfour() throws ExecutionException, InterruptedException {
        ApplicationHandler applicationHandler = new ApplicationHandler(new ResourceConfig(ResourceFour.class));
        Object entity = applicationHandler.apply(RequestContextBuilder.from("/four/a/azazaz00", HttpMethod.POST)
                .build()).get().getEntity();
        Assertions.assertEquals("/four/" + THREE_SUB + "/" + ONE_POST, entity);
    }
}
