/*
 * Copyright (c) 2025 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.netty.connector;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientRequest;
import org.glassfish.jersey.client.ClientResponse;
import org.glassfish.jersey.http.HttpHeaders;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

public class CustomRedirectControllerTest extends JerseyTest {
    private static final String REDIRECTED = "redirected";

    @Path("/")
    public static class CustomRedirectControllerTestResource {
        @Context
        UriInfo uriInfo;

        @GET
        @Path(REDIRECTED)
        public String redirected() {
            return REDIRECTED;
        }

        @POST
        @Path("doRedirect")
        public Response doRedirect(int status) {
            return Response.status(status)
                    .header(HttpHeaders.LOCATION, uriInfo.getBaseUri().toString() + "redirected")
                    .build();
        }
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(CustomRedirectControllerTestResource.class);
    }

    @Override
    protected void configureClient(ClientConfig config) {
        config.connectorProvider(new NettyConnectorProvider());
    }

    @Test
    public void testRedirectToGET() {
        try (Response r = target("doRedirect")
                .property(NettyClientProperties.PRESERVE_METHOD_ON_REDIRECT, false)
                .request().post(Entity.entity(301, MediaType.TEXT_PLAIN_TYPE))) {
            MatcherAssert.assertThat(r.getStatus(), Matchers.is(200));
            MatcherAssert.assertThat(r.readEntity(String.class), Matchers.is(REDIRECTED));
        }
    }

    @Test
    public void testNotRedirected() {
        try (Response response = target("doRedirect")
                .property(NettyClientProperties.HTTP_REDIRECT_CONTROLLER, new NettyHttpRedirectController() {
                    @Override
                    public boolean prepareRedirect(ClientRequest request, ClientResponse response) {
                        return false;
                    }
                }).request().post(Entity.entity(301, MediaType.TEXT_PLAIN_TYPE))) {
            MatcherAssert.assertThat(response.getStatus(), Matchers.is(301));
        }
    }
}
