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

import java.util.Date;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import org.glassfish.jersey.gson.JsonGsonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GsonCustomTest extends JerseyTest {

    private static final Date date = new Date(0);

    @Path("/test")
    public static class Resource {

        @GET
        @Consumes("application/json")
        public Date get() {
            return date;
        }
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(Resource.class).register(JsonGsonFeature.class).register(GsonContextResolver.class);
    }

    @Test
    public void get() {
        Response response = target("/test").request().get();
        assertEquals(200, response.getStatus());
        String obj = response.readEntity(String.class);
        assertEquals("\"1970\"", obj);
    }

    @Provider
    public static class GsonContextResolver implements ContextResolver<Gson> {
        @Override
        public Gson getContext(Class<?> type) {
            GsonBuilder builder = new GsonBuilder();
            builder.setDateFormat("yyyy");
            return builder.create();
        }
    }
}
