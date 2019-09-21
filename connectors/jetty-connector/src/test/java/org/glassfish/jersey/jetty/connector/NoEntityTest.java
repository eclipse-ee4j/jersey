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

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Test;

/**
 * @author Paul Sandoz
 * @author Arul Dhesiaseelan (aruld at acm.org)
 */
public class NoEntityTest extends JerseyTest {
    private static final Logger LOGGER = Logger.getLogger(NoEntityTest.class.getName());

    @Path("/test")
    public static class HttpMethodResource {
        @GET
        public Response get() {
            return Response.status(Status.CONFLICT).build();
        }

        @POST
        public void post(String entity) {
        }
    }

    @Override
    protected Application configure() {
        ResourceConfig config = new ResourceConfig(HttpMethodResource.class);
        config.register(new LoggingFeature(LOGGER, LoggingFeature.Verbosity.PAYLOAD_ANY));
        return config;
    }

    @Override
    protected void configureClient(ClientConfig config) {
        config.connectorProvider(new JettyConnectorProvider());
    }

    @Test
    public void testGet() {
        WebTarget r = target("test");

        for (int i = 0; i < 5; i++) {
            Response cr = r.request().get();
            cr.close();
        }
    }

    @Test
    public void testGetWithClose() {
        WebTarget r = target("test");
        for (int i = 0; i < 5; i++) {
            Response cr = r.request().get();
            cr.close();
        }
    }

    @Test
    public void testPost() {
        WebTarget r = target("test");
        for (int i = 0; i < 5; i++) {
            Response cr = r.request().post(null);
        }
    }

    @Test
    public void testPostWithClose() {
        WebTarget r = target("test");
        for (int i = 0; i < 5; i++) {
            Response cr = r.request().post(null);
            cr.close();
        }
    }
}
