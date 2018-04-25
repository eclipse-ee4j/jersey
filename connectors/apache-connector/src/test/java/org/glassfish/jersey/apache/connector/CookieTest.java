/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.apache.connector;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

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
        cc.connectorProvider(new ApacheConnectorProvider());
        Client client = ClientBuilder.newClient(cc);
        WebTarget r = client.target(getBaseUri());

        assertEquals("NO-COOKIE", r.request().get(String.class));
        assertEquals("value", r.request().get(String.class));
    }

    @Test
    public void testDisabledCookies() {
        ClientConfig cc = new ClientConfig();
        cc.property(ApacheClientProperties.DISABLE_COOKIES, true);
        cc.connectorProvider(new ApacheConnectorProvider());
        JerseyClient client = JerseyClientBuilder.createClient(cc);
        WebTarget r = client.target(getBaseUri());

        assertEquals("NO-COOKIE", r.request().get(String.class));
        assertEquals("NO-COOKIE", r.request().get(String.class));

        final ApacheConnector connector = (ApacheConnector) client.getConfiguration().getConnector();
        if (connector.getCookieStore() != null) {
            assertTrue(connector.getCookieStore().getCookies().isEmpty());
        } else {
            assertNull(connector.getCookieStore());
        }
    }

    @Test
    public void testCookies() {
        ClientConfig cc = new ClientConfig();
        cc.connectorProvider(new ApacheConnectorProvider());
        JerseyClient client = JerseyClientBuilder.createClient(cc);
        WebTarget r = client.target(getBaseUri());

        assertEquals("NO-COOKIE", r.request().get(String.class));
        assertEquals("value", r.request().get(String.class));

        final ApacheConnector connector = (ApacheConnector) client.getConfiguration().getConnector();
        assertNotNull(connector.getCookieStore().getCookies());
        assertEquals(1, connector.getCookieStore().getCookies().size());
        assertEquals("value", connector.getCookieStore().getCookies().get(0).getValue());
    }
}
