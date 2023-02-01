/*
 * Copyright (c) 2022, 2023 Oracle and/or its affiliates. All rights reserved.
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.net.URI;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.netty.connector.internal.RedirectException;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FollowRedirectsTest extends JerseyTest {

    private static final Logger LOGGER = Logger.getLogger(FollowRedirectsTest.class.getName());
    private static final String TEST_URL = "http://localhost:%d/test";
    private static final AtomicReference<String> TEST_URL_REF = new AtomicReference<>();

    @BeforeEach
    public void before() {
        final String url = String.format(TEST_URL, getPort());
        TEST_URL_REF.set(url);
    }

    @Path("/test")
    public static class RedirectResource {
        @GET
        public String get() {
            return "GET";
        }

        @GET
        @Path("redirect")
        public Response redirect() {
            return Response.seeOther(URI.create(TEST_URL_REF.get())).build();
        }

        @GET
        @Path("loop")
        public Response loop() {
            return Response.seeOther(URI.create(TEST_URL_REF.get() + "/loop")).build();
        }

        @GET
        @Path("redirect2")
        public Response redirect2() {
            return Response.seeOther(URI.create(TEST_URL_REF.get() + "/redirect")).build();
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
        config.connectorProvider(new NettyConnectorProvider());
    }

    @Test
    public void testDoFollow() {
        final URI u = target().getUri();
        ClientConfig config = new ClientConfig().property(ClientProperties.FOLLOW_REDIRECTS, true);
        config.connectorProvider(new NettyConnectorProvider());
        Client c = ClientBuilder.newClient(config);
        WebTarget t = c.target(u);
        Response r = t.path("test/redirect")
                .request().get();
        assertEquals(200, r.getStatus());
        assertEquals("GET", r.readEntity(String.class));
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
        config.connectorProvider(new NettyConnectorProvider());
        Client client = ClientBuilder.newClient(config);
        WebTarget t = client.target(u);
        t.property(ClientProperties.FOLLOW_REDIRECTS, false);
        Response r = t.path("test/redirect").request().get();
        assertEquals(303, r.getStatus());
        client.close();
    }

    @Test
    public void testInfiniteLoop() {
        WebTarget t = target("test/loop");
        t.property(ClientProperties.FOLLOW_REDIRECTS, true);
        try {
            t.request().get();
            fail("Expected exception");
        } catch (ProcessingException e) {
            assertEquals(RedirectException.class, e.getCause().getClass());
            assertEquals(LocalizationMessages.REDIRECT_INFINITE_LOOP(), e.getCause().getMessage());
        }
    }

    @Test
    public void testRedirectLimitReached() {
        WebTarget t = target("test/redirect2");
        t.property(ClientProperties.FOLLOW_REDIRECTS, true);
        t.property(NettyClientProperties.MAX_REDIRECTS, 1);
        try {
            t.request().get();
            fail("Expected exception");
        } catch (ProcessingException e) {
            assertEquals(RedirectException.class, e.getCause().getClass());
            assertEquals(LocalizationMessages.REDIRECT_LIMIT_REACHED(1), e.getCause().getMessage());
        }
    }

    @Test
    public void testRedirectNoLimitReached() {
        WebTarget t = target("test/redirect2");
        t.property(ClientProperties.FOLLOW_REDIRECTS, true);
        Response r = t.request().get();
        assertEquals(200, r.getStatus());
        assertEquals("GET", r.readEntity(String.class));
    }
}
