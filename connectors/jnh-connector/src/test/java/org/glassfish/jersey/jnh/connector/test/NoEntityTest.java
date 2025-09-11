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

import jakarta.ws.rs.core.GenericType;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jnh.connector.JavaNetHttpConnectorProvider;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import java.util.logging.Logger;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

public class NoEntityTest extends JerseyTest {
    private static final Logger LOGGER = Logger.getLogger(NoEntityTest.class.getName());

    @Path("/test")
    public static class HttpMethodResource {
        @GET
        public Response get() {
            return Response.status(Response.Status.CONFLICT).build();
        }

        @POST
        public void post(String entity) {
        }

        @GET
        @Path("/success")
        public Response getSuccessfully() {
            return Response.status(Response.Status.NO_CONTENT).build();
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
        config.connectorProvider(new JavaNetHttpConnectorProvider());
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
    public void testGetVoidWithClose() {
        WebTarget r = target("test");
        for (int i = 0; i < 5; i++) {
            Response cr = r.request().get();
            cr.close();
        }
    }

    @Test
    public void testGetVoid() {
        WebTarget r = target("test/success");
        for (int i = 0; i < 5; i++) {
            r.request().get(void.class);
        }
    }

    @Test
    public void testGetGenericVoid() {
        WebTarget r = target("test/success");
        for (int i = 0; i < 5; i++) {
            r.request().get(new GenericType<Void>() {
            });
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
