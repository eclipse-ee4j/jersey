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

package org.glassfish.jersey.jdk.connector.internal;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jdk.connector.JdkConnectorProvider;
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
        public Response queryIntArray(@QueryParam(PARAM_NAME) Integer[] data) {
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
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(ArraysResource.class);
    }

    @Override
    protected void configureClient(ClientConfig config) {
        config.connectorProvider(new JdkConnectorProvider());
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
