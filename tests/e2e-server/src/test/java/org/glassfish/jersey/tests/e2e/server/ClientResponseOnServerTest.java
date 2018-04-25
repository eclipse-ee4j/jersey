/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.Uri;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Test for support of client-side response in the server-side resource implementation.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public class ClientResponseOnServerTest extends JerseyTest {

    @Path("root")
    public static class RootResource {
        @GET
        @Produces(MediaType.TEXT_PLAIN)
        @Path("response")
        public Response getResponse(@Uri("internal/response") WebTarget target) {
            // returns client-side response instance
            return target.request(MediaType.TEXT_PLAIN).get();
        }

        @GET
        @Produces(MediaType.TEXT_PLAIN)
        @Path("error")
        public String getError(@Uri("internal/error") WebTarget target) {
            // throws WebApplicationException with an error response
            return target.request(MediaType.TEXT_PLAIN).get(String.class);
        }
    }

    @Path("internal")
    public static class InternalResource {
        @GET
        @Produces(MediaType.TEXT_PLAIN)
        @Path("response")
        public String getResponse() {
            return "response";
        }

        @GET
        @Produces(MediaType.TEXT_PLAIN)
        @Path("error")
        public Response getError() {
            // Testing for a cross-stack support of a completely custom status code.
            return Response.status(699).type(MediaType.TEXT_PLAIN).entity("error").build();
        }
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(
                RootResource.class,
                InternalResource.class
        );
    }

    @Test
    public void testClientResponseUsageOnServer() {
        final WebTarget target = target("root/{type}");

        Response response;

        response = target.resolveTemplate("type", "response").request(MediaType.TEXT_PLAIN).get();
        assertEquals(200, response.getStatus());
        assertEquals("response", response.readEntity(String.class));

        response = target.resolveTemplate("type", "error").request(MediaType.TEXT_PLAIN).get();
        assertEquals(699, response.getStatus());
        assertEquals("error", response.readEntity(String.class));
    }
}
