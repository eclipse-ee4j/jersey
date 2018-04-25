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

package org.glassfish.jersey.tests.api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Pavel Bucek (pavel.bucek at oracle.com)
 */
public class ResponseIntegrationTest extends JerseyTest {

    @Override
    protected ResourceConfig configure() {
        return new ResourceConfig(ResponseIntegrationTest.ResponseTest.class);
    }

    @Override
    protected void configureClient(ClientConfig config) {
        config.property(ClientProperties.FOLLOW_REDIRECTS, false);
    }

    @Path(value = "/ResponseTest")
    public static class ResponseTest {

        @GET
        @Produces(value = "text/plain")
        public Response statusTest(@QueryParam("status") int status) {
            Response res;
            Response.ResponseBuilder resp;

            switch (status) {
                case 200:
                    resp = Response.ok();
                    break;
                case 204:
                case 201:
                case 202:
                case 303:
                case 304:
                case 307:
                case 400:
                case 401:
                case 403:
                case 404:
                case 406:
                case 409:
                case 410:
                case 415:
                case 500:
                case 503:
                case 411:
                case 412:
                    resp = Response.status(status);
                    break;
                default:
                    resp = Response.ok().entity("Unexpected parameter in request: " + status);
                    break;
            }

            res = resp.header("TESTHEADER", "status code in request = " + status).build();
            return res;
        }
    }

    private void testStatus(int status) {
        final Response response = target().path("ResponseTest").queryParam("status", status).request(MediaType.TEXT_PLAIN)
                .get(Response.class);

        assertEquals(status, response.getStatus());
    }

    private void testGenericStatus(int status) {
        final GenericType<Response> genericType = new GenericType<>(Response.class);
        final Response response = target().path("ResponseTest").queryParam("status", status).request(MediaType.TEXT_PLAIN)
                .get(genericType);

        assertEquals(status, response.getStatus());
    }

    /*
     * Client send request to a resource,
     * verify that correct status code returned
     */
    @Test
    public void testStatuses() {
        final int[] statuses = new int[] {
                200,
                201,
                202,
                204,
                303,
                304,
                307,
                401,
                403,
                404,
                406,
                409,
                410,
                411,
                412,
                415,
                500,
                503
        };

        for (Integer i : statuses) {
            System.out.println("### Testing status: " + i);
            testStatus(i);
        }
    }

    /*
     * Client send request to a resource,
     * verify that correct status code returned
     */
    @Test
    public void testGenericStatuses() {
        final int[] statuses = new int[] {
                200,
                201,
                202,
                204,
                303,
                304,
                307,
                401,
                403,
                404,
                406,
                409,
                410,
                411,
                412,
                415,
                500,
                503
        };

        for (Integer i : statuses) {
            System.out.println("### Testing status: " + i);
            testGenericStatus(i);
        }
    }

}
