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

package org.glassfish.jersey.tests.e2e.client.connector.ssl;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import javax.net.ssl.SSLContext;

import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.logging.LoggingFeature;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * SSL connector tests.
 *
 * @author Pavel Bucek (pavel.bucek at oracle.com)
 * @author Arul Dhesiaseelan (aruld at acm.org)
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
@RunWith(Parameterized.class)
public class SslConnectorConfigurationTest extends AbstractConnectorServerTest {

    /**
     * Test to see that the correct Http status is returned.
     *
     * @throws Exception in case of a test failure.
     */
    @Test
    public void testSSLWithAuth() throws Exception {
        final SSLContext sslContext = getSslContext();

        final ClientConfig cc = new ClientConfig().connectorProvider(connectorProvider);
        final Client client = ClientBuilder.newBuilder()
                .withConfig(cc)
                .sslContext(sslContext)
                .build();

        // client basic auth demonstration
        client.register(HttpAuthenticationFeature.basic("user", "password"));
        final WebTarget target = client.target(Server.BASE_URI).register(LoggingFeature.class);

        final Response response = target.path("/").request().get(Response.class);

        assertEquals(200, response.getStatus());
    }

    /**
     * Test to see that HTTP 401 is returned when client tries to GET without
     * proper credentials.
     *
     * @throws Exception in case of a test failure.
     */
    @Test
    public void testHTTPBasicAuth1() throws Exception {
        final SSLContext sslContext = getSslContext();

        final ClientConfig cc = new ClientConfig().connectorProvider(new ApacheConnectorProvider());
        final Client client = ClientBuilder.newBuilder()
                .withConfig(cc)
                .sslContext(sslContext)
                .build();

        final WebTarget target = client.target(Server.BASE_URI).register(LoggingFeature.class);

        final Response response = target.path("/").request().get(Response.class);

        assertEquals(401, response.getStatus());
    }

    /**
     * Test to see that SSLHandshakeException is thrown when client don't have
     * trusted key.
     *
     * @throws Exception in case of a test failure.
     */
    @Test
    public void testSSLAuth1() throws Exception {
        final SSLContext sslContext = getSslContext();

        final ClientConfig cc = new ClientConfig().connectorProvider(new ApacheConnectorProvider());
        final Client client = ClientBuilder.newBuilder()
                .withConfig(cc)
                .sslContext(sslContext)
                .build();

        WebTarget target = client.target(Server.BASE_URI).register(LoggingFeature.class);

        boolean caught = false;
        try {
            target.path("/").request().get(String.class);
        } catch (Exception e) {
            caught = true;
        }

        assertTrue(caught);
    }

    /**
     * Test that a response to an authentication challenge has the same SSL configuration as the original request.
     */
    @Test
    public void testSSLWithNonPreemptiveAuth() throws Exception {
        final SSLContext sslContext = getSslContext();

        final ClientConfig cc = new ClientConfig().connectorProvider(connectorProvider);
        final Client client = ClientBuilder.newBuilder()
                .withConfig(cc)
                .sslContext(sslContext)
                .build();

        // client basic auth demonstration
        HttpAuthenticationFeature authFeature = HttpAuthenticationFeature.basicBuilder()
                .nonPreemptive()
                .credentials("user", "password")
                .build();

        client.register(authFeature);
        final WebTarget target = client.target(Server.BASE_URI).register(LoggingFeature.class);

        final Response response = target.path("/").request().get(Response.class);

        assertEquals(200, response.getStatus());
    }
}
