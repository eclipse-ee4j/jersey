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

import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jnh.connector.JavaNetHttpConnectorProvider;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ErrorTest extends JerseyTest {

    private static final Logger LOGGER = Logger.getLogger(ErrorTest.class.getName());

    @Override
    protected Application configure() {
        ResourceConfig config = new ResourceConfig(ErrorResource.class);
        config.register(new LoggingFeature(LOGGER, LoggingFeature.Verbosity.PAYLOAD_ANY));
        return config;
    }


    @Override
    protected void configureClient(ClientConfig config) {
        config.connectorProvider(new JavaNetHttpConnectorProvider());
    }


    @Path("/test")
    public static class ErrorResource {
        @POST
        public Response post(String entity) {
            return Response.serverError().build();
        }

        @Path("entity")
        @POST
        public Response postWithEntity(String entity) {
            return Response.serverError().entity("error").build();
        }
    }

    @Test
    public void testPostError() {
        final WebTarget target = target("test");

        for (int i = 0; i < 100; i++) {
            final Response resp = target.request().post(Entity.text("POST"));
            assertEquals(500, resp.getStatus());
        }
    }

    @Test
    public void testPostErrorWithEntity() {
        WebTarget r = target("test");

        for (int i = 0; i < 100; i++) {
            try {
                r.request().post(Entity.text("POST"));
            } catch (ClientErrorException ex) {
                String s = ex.getResponse().readEntity(String.class);
                assertEquals("error", s);
            }
        }
    }

    @Test
    public void testPostErrorAsync() throws ExecutionException, InterruptedException {
        final WebTarget target = target("test");

        final List<Future<Response>> responses = new ArrayList<>(100);

        for (int i = 0; i < 100; i++) {
                responses.add(target.request().async().post(Entity.text("POST")));
        }
        for (int i = responses.size() - 1; i >= 0;) {
            if (responses.get(i).isDone()) {
                assertEquals(500, responses.remove(i).get().getStatus());
                i--;
            }
        }
    }

    @Test
    public void testPostErrorWithEntityAsync() {
        WebTarget r = target("test");

        for (int i = 0; i < 100; i++) {
            try {
                r.request().async().post(Entity.text("POST"));
            } catch (ClientErrorException ex) {
                String s = ex.getResponse().readEntity(String.class);
                assertEquals("error", s);
            }
        }
    }
}
