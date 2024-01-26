/*
 * Copyright (c) 2022, 2024 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.apache5.connector.test;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;

import org.apache.hc.client5.http.cookie.CookieStore;
import org.glassfish.jersey.apache5.connector.Apache5ClientProperties;
import org.glassfish.jersey.apache5.connector.Apache5ConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.glassfish.jersey.client.spi.Connector;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Paul Sandoz
 * @author Arul Dhesiaseelan (aruld at acm.org)
 */
public class CookieTest extends JerseyTest {

    @Path("/")
    public static class CookieResource {

        @GET
        public Response get(@Context HttpHeaders h) {
            Cookie c = h.getCookies().get("name");
            String e = (c == null) ? "NO-COOKIE" : c.getValue();
            return Response.ok(e)
                    .cookie(new NewCookie("name", "value")).build();
        }
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(CookieResource.class);
    }

    @Test
    public void testCookieResource() {
        ClientConfig cc = new ClientConfig();
        cc.connectorProvider(new Apache5ConnectorProvider());
        Client client = ClientBuilder.newClient(cc);
        WebTarget r = client.target(getBaseUri());

        assertEquals("NO-COOKIE", r.request().get(String.class));
        assertEquals("value", r.request().get(String.class));
    }

    @Test
    public void testDisabledCookies() {
        ClientConfig cc = new ClientConfig();
        cc.property(Apache5ClientProperties.DISABLE_COOKIES, true);
        cc.connectorProvider(new Apache5ConnectorProvider());
        JerseyClient client = JerseyClientBuilder.createClient(cc);
        WebTarget r = client.target(getBaseUri());

        assertEquals("NO-COOKIE", r.request().get(String.class));
        assertEquals("NO-COOKIE", r.request().get(String.class));

        final CookieStore cookieStore = cookieStore(client);
        if (cookieStore != null) {
            assertTrue(cookieStore.getCookies().isEmpty());
        } else {
            assertNull(cookieStore);
        }
    }

    @Test
    public void testCookies() {
        ClientConfig cc = new ClientConfig();
        cc.connectorProvider(new Apache5ConnectorProvider());
        JerseyClient client = JerseyClientBuilder.createClient(cc);
        WebTarget r = client.target(getBaseUri());

        assertEquals("NO-COOKIE", r.request().get(String.class));
        assertEquals("value", r.request().get(String.class));

        final CookieStore cookieStore = cookieStore(client);
        assertNotNull(cookieStore.getCookies());
        assertEquals(1, cookieStore.getCookies().size());
        assertEquals("value", cookieStore.getCookies().get(0).getValue());
    }

    private static CookieStore cookieStore(JerseyClient client) {
        Connector connector = client.getConfiguration().getConnector();
        Method method = null;
        try {
            method = connector.getClass().getDeclaredMethod("getCookieStore");
            method.setAccessible(true);
            return (CookieStore) method.invoke(connector);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
