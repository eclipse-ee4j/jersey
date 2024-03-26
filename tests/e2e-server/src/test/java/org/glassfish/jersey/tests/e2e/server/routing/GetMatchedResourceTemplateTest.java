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

package org.glassfish.jersey.tests.e2e.server.routing;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.glassfish.jersey.server.internal.routing.UriRoutingContext;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Set;

public class GetMatchedResourceTemplateTest extends JerseyTest {

    @Path("/resource")
    public static class UriInfoTestResource {
        public static final String ONE_POST = "{name:[a-zA-Z][a-zA-Z_0-9]*}";
        public static final String TWO_GET = "{Prefix}{p:/?}{id: ((\\d+)?)}/abc{p2:/?}{number: (([A-Za-z0-9]*)?)}";
        public static final String TWO_POST = "{Prefix}{p:/?}{id: ((\\d+)?)}/abc/{yeah}";
        public static final String THREE_SUB = "{x:[a-z]}";

        public static class SubGet {
            @PUT
            public String get(@Context UriInfo uriInfo) {
                return ((UriRoutingContext) uriInfo).getMatchedResourceTemplate();
            }
        }

        @POST
        @Path("one/" + ONE_POST)
        public Response post(@Context UriInfo info) {
            return Response.ok(((UriRoutingContext) info).getMatchedResourceTemplate()).build();
        }

        @GET
        @Path("two/" + TWO_GET)
        public Response get(@Context UriInfo info) {
            return Response.ok(((UriRoutingContext) info).getMatchedResourceTemplate()).build();
        }

        @POST
        @Path("two/" + TWO_POST)
        public Response postTwo(@Context UriInfo info) {
            return Response.ok(((UriRoutingContext) info).getMatchedResourceTemplate()).build();
        }

        @Path("three/" + THREE_SUB)
        public SubGet doAnything4() {
            return new SubGet();
        }

        @Path("four/" + THREE_SUB)
        public UriInfoTestResource postOne() {
            return new UriInfoTestResource();
        }
    }

    @ApplicationPath("/app")
    private static class App extends Application {
        @Override
        public Set<Class<?>> getClasses() {
            return Collections.singleton(UriInfoTestResource.class);
        }
    }

    @Override
    protected Application configure() {
        return new App();
    }

    @Test
    public void testPOSTone() {
        try (Response r = target("app/resource/one/azazaz00").request().post(null)) {
            Assertions.assertEquals(200, r.getStatus());
            Assertions.assertEquals("/app/resource/one/" + UriInfoTestResource.ONE_POST, r.readEntity(String.class));
        }
    }

    @Test
    public void testTWOget() {
        try (Response r = target("app/resource/two/P/abc/MyNumber").request().get()) {
            Assertions.assertEquals(200, r.getStatus());
            Assertions.assertEquals("/app/resource/two/" + UriInfoTestResource.TWO_GET, r.readEntity(String.class));
        }
    }

    @Test
    public void testTWOpost() {
        try (Response r = target("app/resource/two/P/abc/MyNumber").request().post(null)) {
            Assertions.assertEquals(200, r.getStatus());
            Assertions.assertEquals("/app/resource/two/" + UriInfoTestResource.TWO_POST, r.readEntity(String.class));
        }
    }

    @Test
    public void testPUTthree2() {
        try (Response r = target("app/resource/three/a").request().put(Entity.entity("", MediaType.WILDCARD_TYPE))) {
            Assertions.assertEquals(200, r.getStatus());
            Assertions.assertEquals("/app/resource/three/" + UriInfoTestResource.THREE_SUB, r.readEntity(String.class));
        }
    }
}
