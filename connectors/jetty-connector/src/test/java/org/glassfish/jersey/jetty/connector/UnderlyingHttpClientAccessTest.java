/*
 * Copyright (c) 2014, 2019 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.jetty.connector;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import org.glassfish.jersey.client.ClientConfig;

import org.eclipse.jetty.client.HttpClient;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;

/**
 * Test of access to the underlying HTTP client instance used by the connector.
 *
 * @author Marek Potociar
 */
public class UnderlyingHttpClientAccessTest {

    /**
     * Verifier of JERSEY-2424 fix.
     */
    @Test
    public void testHttpClientInstanceAccess() {
        final Client client = ClientBuilder.newClient(new ClientConfig().connectorProvider(new JettyConnectorProvider()));
        final HttpClient hcOnClient = JettyConnectorProvider.getHttpClient(client);
        // important: the web target instance in this test must be only created AFTER the client has been pre-initialized
        // (see org.glassfish.jersey.client.Initializable.preInitialize method). This is here achieved by calling the
        // connector provider's static getHttpClient method above.
        final WebTarget target = client.target("http://localhost/");
        final HttpClient hcOnTarget = JettyConnectorProvider.getHttpClient(target);

        assertNotNull("HTTP client instance set on JerseyClient should not be null.", hcOnClient);
        assertNotNull("HTTP client instance set on JerseyWebTarget should not be null.", hcOnTarget);
        assertSame("HTTP client instance set on JerseyClient should be the same instance as the one set on JerseyWebTarget"
                        + "(provided the target instance has not been further configured).",
                hcOnClient, hcOnTarget
        );
    }

    @Test
    public void testGetProvidedClientInstance() {
        final HttpClient httpClient = new HttpClient();
        final ClientConfig clientConfig = new ClientConfig()
                .connectorProvider(new JettyConnectorProvider())
                .register(new JettyHttpClientSupplier(httpClient));
        final Client client = ClientBuilder.newClient(clientConfig);
        final WebTarget target = client.target("http://localhost/");
        final HttpClient hcOnTarget = JettyConnectorProvider.getHttpClient(target);

        assertThat("Instance provided to a ClientConfig differs from instance provided by JettyProvider",
                httpClient, is(hcOnTarget));
    }
}
