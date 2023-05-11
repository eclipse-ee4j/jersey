/*
 * Copyright (c) 2023 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.restclient;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import jakarta.json.Json;
import jakarta.json.JsonBuilderFactory;
import jakarta.json.JsonObject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.MediaType;
import java.net.URI;
import java.util.Collections;

public class PathParamTest extends JerseyTest {
    @Path("/greet")
    public static class GreetResource {
        private static final JsonBuilderFactory JSON = Json.createBuilderFactory(Collections.emptyMap());

        @GET
        @Produces(MediaType.APPLICATION_JSON)
        @Path("/everything/everywhere")
        public JsonObject getDefaultMessage() {
            return createResponse("World");
        }

        private JsonObject createResponse(String who) {
            String msg = String.format("%s %s!", "Hello", who);

            return JSON.createObjectBuilder()
                    .add("message", msg)
                    .build();
        }
    }

    @Path("/greet")
    public static interface GreetResourceClient {

        @GET
        @Produces(MediaType.APPLICATION_JSON)
        @Path("{folderpath}")
        JsonObject getDefaultMessage(@PathParam("folderpath") String folderPath);
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(GreetResource.class);
    }

    @Test
    public void testRestClientRequest() {
        GreetResourceClient client = RestClientBuilder.newBuilder()
                .baseUri(URI.create("http://localhost:9998")).build(GreetResourceClient.class);
        JsonObject jsonObject = client.getDefaultMessage("everything/everywhere");
        Assertions.assertEquals("Hello World!", jsonObject.getString("message"));
    }

    @Test
    public void testStandardRequest() {
        JsonObject jsonObject = target()
                .path("greet").path("everything").path("everywhere")
                .request()
                .get(JsonObject.class);
        Assertions.assertEquals("Hello World!", jsonObject.getString("message"));
    }
}
