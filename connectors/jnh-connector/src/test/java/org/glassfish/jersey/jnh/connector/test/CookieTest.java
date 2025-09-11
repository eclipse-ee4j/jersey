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
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.glassfish.jersey.jnh.connector.JavaNetHttpClientProperties;
import org.glassfish.jersey.jnh.connector.JavaNetHttpConnector;
import org.glassfish.jersey.jnh.connector.JavaNetHttpConnectorProvider;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.CookieManager;
import java.util.Collections;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CookieTest extends JerseyTest {

private static final Logger LOGGER = Logger.getLogger(CookieTest.class.getName());

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
        ResourceConfig config = new ResourceConfig(CookieResource.class);
        config.register(new LoggingFeature(LOGGER, LoggingFeature.Verbosity.PAYLOAD_ANY));
        return config;
    }

    @Test
    public void testCookieResource() {
        ClientConfig config = new ClientConfig();
        config.property(JavaNetHttpClientProperties.COOKIE_HANDLER, new CookieManager());
        config.connectorProvider(new JavaNetHttpConnectorProvider());
        Client client = ClientBuilder.newClient(config);
        WebTarget r = client.target(getBaseUri());


        assertEquals("NO-COOKIE", r.request().get(String.class));
        assertEquals("value", r.request().get(String.class));
        client.close();
    }

    @Test
    public void testDisabledCookies() throws IOException {
        ClientConfig cc = new ClientConfig();
        cc.property(JavaNetHttpClientProperties.DISABLE_COOKIES, true);
        cc.property(JavaNetHttpClientProperties.COOKIE_HANDLER, new CookieManager());
        cc.connectorProvider(new JavaNetHttpConnectorProvider());
        JerseyClient client = JerseyClientBuilder.createClient(cc);
        WebTarget r = client.target(getBaseUri());

        assertEquals("NO-COOKIE", r.request().get(String.class));
        assertEquals("NO-COOKIE", r.request().get(String.class));

        CookieManager manager = new CookieManager();
        manager.getCookieStore().getCookies();

        final JavaNetHttpConnector connector = (JavaNetHttpConnector) client.getConfiguration().getConnector();

        if (connector.getCookieHandler() != null) {
            assertTrue(connector.getCookieHandler().get(getBaseUri(), Collections.emptyMap()).get("Cookie").isEmpty());
        } else {
            assertNull(connector.getCookieHandler());
        }
        client.close();
    }

    @Test
    public void testCookies() throws IOException {
        ClientConfig cc = new ClientConfig();
        final CookieManager manager = new CookieManager();
        cc.property(JavaNetHttpClientProperties.COOKIE_HANDLER, manager);
        cc.connectorProvider(new JavaNetHttpConnectorProvider());
        JerseyClient client = JerseyClientBuilder.createClient(cc);
        WebTarget r = client.target(getBaseUri());

        assertEquals("NO-COOKIE", r.request().get(String.class));
        assertEquals("value", r.request().get(String.class));

        final JavaNetHttpConnector connector = (JavaNetHttpConnector) client.getConfiguration().getConnector();
        assertNotNull(connector.getCookieHandler());
        assertEquals(1, connector.getCookieHandler().get(getBaseUri(), Collections.emptyMap()).size());
        assertEquals("value", manager.getCookieStore().getCookies().get(0).getValue());
        client.close();
    }
}