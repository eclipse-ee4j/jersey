/*
 * Copyright (c) 2021, 2024 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.grizzly.connector.test;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.grizzly.connector.GrizzlyConnectorProvider;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.Test;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Response;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the Http methods.
 */
public class HttpMethodTest extends JerseyTest {

    @Override
    protected Application configure() {
        return new ResourceConfig(HttpMethodResource.class);
    }

    protected Client createClient() {
        ClientConfig cc = new ClientConfig();
        cc.connectorProvider(new GrizzlyConnectorProvider());
        return ClientBuilder.newClient(cc);
    }

    private WebTarget getWebTarget(final Client client) {
        return client.target(getBaseUri()).path("test");
    }

    private WebTarget getWebTarget() {
        return getWebTarget(createClient());
    }

    @Path("/test")
    public static class HttpMethodResource {
        @GET
        public String get() {
            return "GET";
        }
    }

    @Test
    public void testOptionsWithEntity() {
        WebTarget r = getWebTarget();
        Response response = r.request().build("OPTIONS", Entity.text("OPTIONS")).invoke();
        assertEquals(200, response.getStatus());
        response.close();
    }

    @Test
    public void testGet() {
        WebTarget r = getWebTarget();
        assertEquals("GET", r.request().get(String.class));

        Response cr = r.request().get();
        assertTrue(cr.hasEntity());
        cr.close();
    }
}
