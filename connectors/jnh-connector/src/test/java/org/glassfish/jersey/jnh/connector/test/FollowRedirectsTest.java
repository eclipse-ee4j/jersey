/*
 * Copyright (c) 2022, 2025 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.jnh.connector.test;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientResponseContext;
import jakarta.ws.rs.client.ClientResponseFilter;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.ClientResponse;
import org.glassfish.jersey.jnh.connector.JavaNetHttpConnectorProvider;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class FollowRedirectsTest extends JerseyTest {

    private static final Logger LOGGER = Logger.getLogger(FollowRedirectsTest.class.getName());

    @Path("/test")
    public static class RedirectResource {
        @GET
        public String get() {
            return "GET";
        }

        @GET
        @Path("redirect")
        public Response redirect() {
            return Response.seeOther(UriBuilder.fromResource(RedirectResource.class).build()).build();
        }
    }

    @Override
    protected Application configure() {
        ResourceConfig config = new ResourceConfig(RedirectResource.class);
        config.register(new LoggingFeature(LOGGER, LoggingFeature.Verbosity.PAYLOAD_ANY));
        return config;
    }

    @Override
    protected void configureClient(ClientConfig config) {
        config.property(ClientProperties.FOLLOW_REDIRECTS, false);
        config.connectorProvider(new JavaNetHttpConnectorProvider());
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
        final URI u = target().getUri();
        ClientConfig config = new ClientConfig().property(ClientProperties.FOLLOW_REDIRECTS, true);
        config.connectorProvider(new JavaNetHttpConnectorProvider());
        Client c = ClientBuilder.newClient(config);
        WebTarget t = c.target(u);
        Response r = t.path("test/redirect")
                .register(RedirectTestFilter.class)
                .request().get();
        assertEquals(200, r.getStatus());
        assertEquals("GET", r.readEntity(String.class));
// TODO uncomment as part of JERSEY-2388 fix.
//        assertEquals(
//                UriBuilder.fromUri(getBaseUri()).path(RedirectResource.class).build().toString(),
//                r.getHeaderString(RedirectTestFilter.RESOLVED_URI_HEADER));

        c.close();
    }

    @Test
    public void testDoFollowPerRequestOverride() {
        WebTarget t = target("test/redirect");
        t.property(ClientProperties.FOLLOW_REDIRECTS, true);
        Response r = t.request().get();
        assertEquals(200, r.getStatus());
        assertEquals("GET", r.readEntity(String.class));
    }

    @Test
    public void testDontFollow() {
        WebTarget t = target("test/redirect");
        assertEquals(303, t.request().get().getStatus());
    }

    @Test
    public void testDontFollowPerRequestOverride() {
        final URI u = target().getUri();
        ClientConfig config = new ClientConfig().property(ClientProperties.FOLLOW_REDIRECTS, true);
        config.connectorProvider(new JavaNetHttpConnectorProvider());
        Client client = ClientBuilder.newClient(config);
        WebTarget t = client.target(u);
        t.property(ClientProperties.FOLLOW_REDIRECTS, false);
        Response r = t.path("test/redirect").request().get();
        assertEquals(303, r.getStatus());
        client.close();
    }
}
