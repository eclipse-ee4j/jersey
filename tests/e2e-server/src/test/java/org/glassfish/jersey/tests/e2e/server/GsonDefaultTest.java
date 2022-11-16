/*
 * Copyright (c) 2022 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.e2e.server;

import static org.junit.jupiter.api.Assertions.assertEquals;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.gson.JsonGsonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.Test;

public class GsonDefaultTest extends JerseyTest {

    @Path("/test")
    public static class Resource {

        @POST
        @Consumes("application/json")
        @Produces("application/json")
        public Obj post(Obj entity) {
            entity.setValue("bar");
            return entity;
        }

        @GET
        @Consumes("application/json")
        public Obj get() {
            Obj entity = new Obj();
            entity.setValue("get");
            return entity;
        }
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(Resource.class)
                .register(JsonGsonFeature.class);
    }

    @Override
    protected void configureClient(ClientConfig config) {
        config.register(JsonGsonFeature.class);
    }

    @Test
    public void get() {
        Response response = target("/test").request().get();
        assertEquals(200, response.getStatus());
        Obj obj = response.readEntity(Obj.class);
        assertEquals("get", obj.getValue());
    }

    @Test
    public void post() {
        Obj obj = new Obj();
        obj.setValue("foo");
        Response response = target("/test").request().post(Entity.json(obj));
        assertEquals(200, response.getStatus());
        obj = response.readEntity(Obj.class);
        assertEquals("bar", obj.getValue());
    }

    public static class Obj {
        private String value;

        public String getValue() {
            return value;
        }
        public void setValue(String value) {
            this.value = value;
        }
    }
}
