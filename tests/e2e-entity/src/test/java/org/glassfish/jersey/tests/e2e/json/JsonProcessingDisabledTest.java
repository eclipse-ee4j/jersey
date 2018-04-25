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

package org.glassfish.jersey.tests.e2e.json;

import java.io.StringReader;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import javax.json.Json;
import javax.json.JsonObject;

import org.glassfish.jersey.internal.InternalProperties;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * @author Michal Gajdos
 */
public class JsonProcessingDisabledTest extends JerseyTest {

    private static final String JSON_OBJECT_STR = "{\"foo\":\"bar\"}";
    private static final JsonObject JSON_OBJECT = Json.createReader(new StringReader(JSON_OBJECT_STR)).readObject();

    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public static class Resource {

        @POST
        @Path("jsonObject")
        public JsonObject postJsonObject(final JsonObject jsonObject) {
            return jsonObject;
        }
    }

    @Override
    protected Application configure() {
        enable(TestProperties.DUMP_ENTITY);
        enable(TestProperties.LOG_TRAFFIC);

        return new ResourceConfig(Resource.class)
                .property(ServerProperties.JSON_PROCESSING_FEATURE_DISABLE, true)
                // Make sure other JSON providers are disabled as well.
                .property(InternalProperties.JSON_FEATURE_SERVER, "JsonProcessing");
    }

    @Test
    public void testJsonObject() throws Exception {
        final Response response = target("jsonObject").request(MediaType.APPLICATION_JSON).post(Entity.json(JSON_OBJECT));

        assertEquals(415, response.getStatus());
    }
}
