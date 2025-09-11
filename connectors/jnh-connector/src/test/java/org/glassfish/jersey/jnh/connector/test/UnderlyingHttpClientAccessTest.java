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

import org.glassfish.jersey.client.ClientConfig;

import java.net.http.HttpClient;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import org.glassfish.jersey.jnh.connector.JavaNetHttpConnectorProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

public class UnderlyingHttpClientAccessTest {

    @Test
    public void testHttpClientInstanceAccess() {
        final Client client = ClientBuilder.newClient(new ClientConfig().connectorProvider(new JavaNetHttpConnectorProvider()));
        final HttpClient hcOnClient = JavaNetHttpConnectorProvider.getHttpClient(client);
        // important: the web target instance in this test must be only created AFTER the client has been pre-initialized
        // (see org.glassfish.jersey.client.Initializable.preInitialize method). This is here achieved by calling the
        // connector provider's static getHttpClient method above.
        final WebTarget target = client.target("http://localhost/");
        final HttpClient hcOnTarget = JavaNetHttpConnectorProvider.getHttpClient(target);

        assertNotNull(hcOnClient, "HTTP client instance set on JerseyClient should not be null.");
        assertNotNull(hcOnTarget, "HTTP client instance set on JerseyWebTarget should not be null.");
        assertSame(hcOnClient, hcOnTarget,
                "HTTP client instance set on JerseyClient should be the same instance as the one set on JerseyWebTarget"
                   + "(provided the target instance has not been further configured)."
        );
    }

}