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
import java.util.Set;
import java.util.SortedSet;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ArrayParamConverterTest extends JerseyTest {

    private static final String PARAM_NAME = "paramName";

    @Path("/test")
    public static class ArraysResource {

        @Path("/queryStrArray")
        @GET
        public Response queryStrArray(@QueryParam(PARAM_NAME) String[] data) {
            return Response.ok(String.join("", data)).build();
        }

        @Path("/queryStrList")
        @GET
        public Response queryStrList(@QueryParam(PARAM_NAME) List<String> data) {
            return Response.ok(String.join("", data)).build();
        }

        @Path("/queryIntArray")
        @GET
        public Response queryIntArray(@QueryParam(PARAM_NAME) @DefaultValue("3") Integer[] data) {
            StringBuilder sb = new StringBuilder(data.length);
            for (int d : data) {
              sb.append(d);
            }
            return Response.ok(sb.toString()).build();
        }

        @Path("/queryIntList")
        @GET
        public Response queryIntList(@QueryParam(PARAM_NAME) List<Integer> data) {
            StringBuilder sb = new StringBuilder(data.size());
            for (int d : data) {
              sb.append(d);
            }
            return Response.ok(sb.toString()).build();
        }

        @Path("/queryStrSortedSet")
        @GET
        public Response queryStrSortedSet(@QueryParam(PARAM_NAME) SortedSet<String> data) {
            return Response.ok(String.join("", data)).build();
        }

        @Path("/queryStrSet")
        @GET
        public Response queryStrSet(@QueryParam(PARAM_NAME) Set<String> data) {
            return Response.ok(String.join("", data)).build();
        }

        @Path("/queryArrayTestBeanWithConstructor")
        @GET
        public Response queryArrayTestBeanWithConstructor(@QueryParam(PARAM_NAME) ArrayTestBeanWithConstructor[] data) {
            StringBuilder sb = new StringBuilder(data.length);
            for (ArrayTestBeanWithConstructor d : data) {
              sb.append(d.value);
            }
            return Response.ok(sb.toString()).build();
        }

        @Path("/queryArrayTestBeanWithFromStringArray")
        @GET
        public Response queryArrayTestBeanWithFromStringArray(@QueryParam(PARAM_NAME) ArrayTestBeanWithFromString[] data) {
            StringBuilder sb = new StringBuilder(data.length);
            for (ArrayTestBeanWithFromString d : data) {
              sb.append(d.value);
            }
            return Response.ok(sb.toString()).build();
        }

        @Path("/queryArrayTestBeanWithFromStringList")
        @GET
        public Response queryArrayTestBeanWithFromStringList(@QueryParam(PARAM_NAME) List<ArrayTestBeanWithFromString> data) {
            StringBuilder sb = new StringBuilder(data.size());
            for (ArrayTestBeanWithFromString d : data) {
                sb.append(d.value);
            }
            return Response.ok(sb.toString()).build();
        }

        @Path("/queryArrayTestBeanWithValueOf")
        @GET
        public Response queryArrayTestBeanWithValueOf(@QueryParam(PARAM_NAME) ArrayTestBeanWithValueOf[] data) {
            StringBuilder sb = new StringBuilder(data.length);
            for (ArrayTestBeanWithValueOf d : data) {
              sb.append(d.value);
            }
            return Response.ok(sb.toString()).build();
        }

        @Path("/querySingleInt")
        @GET
        public Response querySingleInt(@QueryParam(PARAM_NAME) int data) {
            return Response.ok(String.valueOf(data)).build();
        }

        @Path("/queryArrayInt")
        @GET
        public Response queryArrayInt(@QueryParam(PARAM_NAME) int[] data) {
            StringBuilder sb = new StringBuilder(data.length);
            for (int d : data) {
              sb.append(d);
            }
            return Response.ok(sb.toString()).build();
        }
    }

    public static class ArrayTestBeanWithConstructor {
        private final String value;
        public ArrayTestBeanWithConstructor(String value) {
            this.value = value;
        }
    }

    public static class ArrayTestBeanWithFromString {
        private final String value;
        private ArrayTestBeanWithFromString(String value) {
            this.value = value;
        }
        public static ArrayTestBeanWithFromString fromString(String value) {
            return new ArrayTestBeanWithFromString(value);
        }
    }

    public static class ArrayTestBeanWithValueOf {
        private final String value;
        private ArrayTestBeanWithValueOf(String value) {
            this.value = value;
        }
        public static ArrayTestBeanWithValueOf valueOf(String value) {
            return new ArrayTestBeanWithValueOf(value);
        }
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(ArraysResource.class);
    }

    @Test
    public void queryStr() {
        Response expected = target("/test/queryStrList").queryParam(PARAM_NAME, "1", "2").request().get();
        Response arrayResponse = target("/test/queryIntArray").queryParam(PARAM_NAME, "1", "2").request().get();
        verifyStr(expected, arrayResponse);
    }

    @Test
    public void queryInt() {
        Response expected = target("/test/queryIntList").queryParam(PARAM_NAME, 1, 2).request().get();
        Response arrayResponse = target("/test/queryIntArray").queryParam(PARAM_NAME, 1, 2).request().get();
        verifyStr(expected, arrayResponse);
    }

    @Test
    public void queryStrSortedSet() {
        Response response = target("/test/queryStrSortedSet").queryParam(PARAM_NAME, 1, 2).request().get();
        assertEquals(200, response.getStatus());
        assertEquals("12", response.readEntity(String.class));
    }

    @Test
    public void queryStrSet() {
        Response response = target("/test/queryStrSet").queryParam(PARAM_NAME, 1, 2).request().get();
        assertEquals(200, response.getStatus());
        assertEquals("12", response.readEntity(String.class));
    }

    @Test
    public void queryArrayTestBeanWithConstructor() {
        Response response = target("/test/queryArrayTestBeanWithConstructor").queryParam(PARAM_NAME, 1, 2).request().get();
        assertEquals(200, response.getStatus());
        assertEquals("12", response.readEntity(String.class));
    }

    @Test
    public void queryArrayTestBeanWithFromString() {
        Response expected = target("/test/queryArrayTestBeanWithFromStringList").queryParam(PARAM_NAME, "1", "2")
                .request().get();
        Response arrayResponse = target("/test/queryArrayTestBeanWithFromStringArray")
                .queryParam(PARAM_NAME, "1", "2").request().get();
        verifyStr(expected, arrayResponse);
    }

    @Test
    public void queryArrayTestBeanWithValueOf() {
        Response response = target("/test/queryArrayTestBeanWithValueOf").queryParam(PARAM_NAME, 1, 2).request().get();
        assertEquals(200, response.getStatus());
        assertEquals("12", response.readEntity(String.class));
    }

    @Test
    public void querySingleInt() {
        Response response = target("/test/querySingleInt").queryParam(PARAM_NAME, 1).request().get();
        assertEquals(200, response.getStatus());
        assertEquals("1", response.readEntity(String.class));
    }

    @Test
    public void queryArrayInt() {
        Response response = target("/test/queryArrayInt").queryParam(PARAM_NAME, 1, 2).request().get();
        assertEquals(200, response.getStatus());
        assertEquals("12", response.readEntity(String.class));
    }

    @Test
    public void queryEmptyArray() {
        Response response = target("/test/queryArrayInt").queryParam(PARAM_NAME).request().get();
        assertEquals(200, response.getStatus());
        assertEquals("", response.readEntity(String.class));
        response = target("/test/queryArrayTestBeanWithValueOf").queryParam(PARAM_NAME).request().get();
        assertEquals(200, response.getStatus());
        assertEquals("", response.readEntity(String.class));
    }

    @Test
    public void queryDefault() {
        Response response = target("/test/queryIntArray").queryParam(PARAM_NAME).request().get();
        assertEquals(200, response.getStatus());
        assertEquals("3", response.readEntity(String.class));
    }

    private void verifyStr(Response expected, Response arrayResponse) {
        assertEquals(200, expected.getStatus());
        assertEquals(200, arrayResponse.getStatus());
        String expectedStr = expected.readEntity(String.class);
        String arrayStr = arrayResponse.readEntity(String.class);
        // Check the result is the same
        assertEquals(expectedStr, arrayStr);
        assertEquals("12", arrayStr);
    }
}
