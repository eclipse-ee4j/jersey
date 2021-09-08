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

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;

public class Issue4790Test extends JerseyTest {

    @Override
    protected Application configure() {
        return new ResourceConfig(TestResource.class);
    }

    @Test
    public void testString() {
        String paramMissing = target("/string").request().get(String.class);
        assertEquals("null", paramMissing);

        String paramProvided = target("/string").queryParam("s", "42").request().get(String.class);
        assertEquals("s was 42", paramProvided);

        String paramEmpty = target("/string").queryParam("s", "").request().get(String.class);
        assertEquals("s was ", paramEmpty);
    }

    @Test
    public void testOptionalInteger() {
        String paramMissing = target("/optionalInteger").request().get(String.class);
        assertEquals("default", paramMissing);

        String paramProvided = target("/optionalInteger").queryParam("i", 42).request().get(String.class);
        assertEquals("i was 42", paramProvided);

        Response paramInvalidResponse = target("/optionalInteger").queryParam("i", "not-a-number").request().get();
        assertEquals(404, paramInvalidResponse.getStatus());

        Response paramEmptyResponse = target("/optionalInteger").queryParam("i", "").request().get();
        assertEquals(404, paramEmptyResponse.getStatus());
    }

    @Test
    public void testInteger() {
        String paramMissing = target("/integer").request().get(String.class);
        assertEquals("null", paramMissing);

        String paramProvided = target("/integer").queryParam("i", 42).request().get(String.class);
        assertEquals("i was 42", paramProvided);

        Response paramInvalidResponse = target("/integer").queryParam("i", "not-a-number").request().get();
        assertEquals(404, paramInvalidResponse.getStatus());

        Response paramEmptyResponse = target("/integer").queryParam("i", "").request().get();
        assertEquals(404, paramEmptyResponse.getStatus());
    }

    @Test
    public void testPrimitiveInt() {
        String paramMissing = target("/int").request().get(String.class);
        assertEquals("i was 0", paramMissing);

        String paramProvided = target("/int").queryParam("i", 42).request().get(String.class);
        assertEquals("i was 42", paramProvided);

        Response paramInvalidResponse = target("/int").queryParam("i", "not-a-number").request().get();
        assertEquals(404, paramInvalidResponse.getStatus());

        String paramEmpty = target("/int").queryParam("i", "").request().get(String.class);
        assertEquals("i was 0", paramEmpty);
    }

    @Test
    public void emptyStringQueryParamReturnsListWithOneNullElement() {
        Response response = target("/uuid")
            .queryParam("list", "")
            .request()
            .get();
        assertEquals(404, response.getStatus());
    }

    @Test
    public void missingQueryParamReturnsEmptyList() {
        Response response = target("/uuid")
            .request()
            .get();
        assertEquals(200, response.getStatus());
        assertEquals("0: []", response.readEntity(String.class));
    }

    @Test
    public void filledQueryParamReturnsListWithOneElement() {
        Response response = target("/uuid")
            .queryParam("list", "ec0cf621-d744-4a1c-b1d8-4b8a44b3dad7")
            .request()
            .get();
        assertEquals(200, response.getStatus());
        assertEquals("1: [ec0cf621-d744-4a1c-b1d8-4b8a44b3dad7]", response.readEntity(String.class));
    }

    @Produces(MediaType.TEXT_PLAIN)
    @Path("/")
    public static class TestResource {
        @GET
        @Path("/optionalInteger")
        public String optionalInt(@QueryParam("i") Optional<Integer> i) {
            if (i == null) {
                return "null";
            }
            return i.map(param -> "i was " + param).orElse("default");
        }

        @GET
        @Path("/integer")
        public String integer(@QueryParam("i") Integer i) {
            if (i == null) {
                return "null";
            }
            return "i was " + i;
        }

        @GET
        @Path("/int")
        public String primitiveInt(@QueryParam("i") int i) {
            return "i was " + i;
        }

        @GET
        @Path("/uuid")
        public String uuid(@QueryParam("list") List<UUID> list) {
            return list.size() + ": " + list.toString();
        }

        @GET
        @Path("/string")
        public String string(@QueryParam("s") String s) {
            if (s == null) {
                return "null";
            }
            return "s was " + s;
        }

    }
}
