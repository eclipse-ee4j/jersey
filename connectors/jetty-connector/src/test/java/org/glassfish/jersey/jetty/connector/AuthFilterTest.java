/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.util.logging.Logger;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * @author Paul Sandoz
 * @author Arul Dhesiaseelan (aruld at acm.org)
 */
public class AuthFilterTest extends JerseyTest {

    private static final Logger LOGGER = Logger.getLogger(AuthFilterTest.class.getName());

    @Override
    protected Application configure() {
        ResourceConfig config = new ResourceConfig(AuthTest.AuthResource.class);
        config.register(new LoggingFeature(LOGGER, LoggingFeature.Verbosity.PAYLOAD_ANY));
        return config;
    }


    @Override
    protected void configureClient(ClientConfig config) {
        config.connectorProvider(new JettyConnectorProvider());
    }

    @Test
    public void testAuthGetWithClientFilter() {
        client().register(HttpAuthenticationFeature.basic("name", "password"));
        Response response = target("test/filter").request().get();
        assertEquals("GET", response.readEntity(String.class));
    }

    @Test
    public void testAuthPostWithClientFilter() {
        client().register(HttpAuthenticationFeature.basic("name", "password"));
        Response response = target("test/filter").request().post(Entity.text("POST"));
        assertEquals("POST", response.readEntity(String.class));
    }


    @Test
    public void testAuthDeleteWithClientFilter() {
        client().register(HttpAuthenticationFeature.basic("name", "password"));
        Response response = target("test/filter").request().delete();
        assertEquals(204, response.getStatus());
    }

}
