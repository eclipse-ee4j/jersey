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

package org.glassfish.jersey.test.inmemory.internal;

import java.io.IOException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.ClientResponse;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * In-memory connector follow redirect tests.
 *
 * @author Martin Matula
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public class FollowRedirectsTest extends JerseyTest {
    @Path("/followTest")
    public static class RedirectResource {
        @GET
        public String get() {
            return "GET";
        }

        @GET
        @Path("redirect1")
        public Response redirect1() {
            return Response.status(302).location(
                    UriBuilder.fromResource(RedirectResource.class).path(RedirectResource.class, "redirect2").build())
                    .build();
        }

        @GET
        @Path("redirect2")
        public Response redirect2() {
            return Response.status(302).location(UriBuilder.fromResource(RedirectResource.class).build()).build();
        }
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(RedirectResource.class);
    }


    private static class RedirectTestFilter implements ClientResponseFilter {
        public static final String RESOLVED_URI_HEADER = "resolved-uri";

        @Override
        public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) throws IOException {
            if (responseContext instanceof ClientResponse) {
                ClientResponse clientResponse = (ClientResponse) responseContext;
                responseContext.getHeaders().putSingle(RESOLVED_URI_HEADER, clientResponse.getResolvedRequestUri().toString());
            }
        }
    }

    @Test
    public void testDoFollow() {
        Response r = target("followTest/redirect1").register(RedirectTestFilter.class).request().get();
        assertEquals(200, r.getStatus());
        assertEquals("GET", r.readEntity(String.class));
        assertEquals(
                UriBuilder.fromUri(getBaseUri()).path(RedirectResource.class).build().toString(),
                r.getHeaderString(RedirectTestFilter.RESOLVED_URI_HEADER));
    }

    @Test
    public void testDontFollow() {
        WebTarget t = target("followTest/redirect1");
        t.property(ClientProperties.FOLLOW_REDIRECTS, false);
        assertEquals(302, t.request().get().getStatus());
    }
}
