/*
 * Copyright (c) 2014, 2018 Oracle and/or its affiliates. All rights reserved.
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

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import org.glassfish.jersey.client.ClientConfig;

import org.apache.http.client.CookieStore;
import org.junit.Test;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

/**
 * Test of access to the underlying CookieStore instance used by the connector.
 *
 * @author Maksim Mukosey (mmukosey at gmail.com)
 */
public class UnderlyingCookieStoreAccessTest {

    @Test
    public void testCookieStoreInstanceAccess() {
        final Client client = ClientBuilder.newClient(new ClientConfig().connectorProvider(new ApacheConnectorProvider()));
        final CookieStore csOnClient = ApacheConnectorProvider.getCookieStore(client);
        // important: the web target instance in this test must be only created AFTER the client has been pre-initialized
        // (see org.glassfish.jersey.client.Initializable.preInitialize method). This is here achieved by calling the
        // connector provider's static getCookieStore method above.
        final WebTarget target = client.target("http://localhost/");
        final CookieStore csOnTarget = ApacheConnectorProvider.getCookieStore(target);

        assertNotNull("CookieStore instance set on JerseyClient should not be null.", csOnClient);
        assertNotNull("CookieStore instance set on JerseyWebTarget should not be null.", csOnTarget);
        assertSame("CookieStore instance set on JerseyClient should be the same instance as the one set on JerseyWebTarget"
                + "(provided the target instance has not been further configured).", csOnClient, csOnTarget);
    }
}
