/*
 * Copyright (c) 2013, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.e2e.client;

import java.util.concurrent.ExecutionException;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.client.AsyncInvoker;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.SyncInvoker;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test {@link GenericType} with {@link Response}.
 *
 * @author Miroslav Fuksa
 */
public class GenericResponseTest extends JerseyTest {

    @Override
    protected Application configure() {
        return new ResourceConfig(TestResource.class);
    }

    @Path("resource")
    public static class TestResource {

        @GET
        public String get() {
            return "get";
        }

        @POST
        public String post(String post) {
            return post;
        }
    }

    @Test
    public void testPost() {
        GenericType<Response> generic = new GenericType<Response>(Response.class);
        Entity entity = Entity.entity("entity", MediaType.WILDCARD_TYPE);

        WebTarget target = target("resource");
        SyncInvoker sync = target.request();

        Response response = sync.post(entity, generic);
        Assert.assertEquals(200, response.getStatus());
        Assert.assertEquals("entity", response.readEntity(String.class));
    }

    @Test
    public void testAsyncPost() throws ExecutionException, InterruptedException {
        GenericType<Response> generic = new GenericType<Response>(Response.class);
        Entity entity = Entity.entity("entity", MediaType.WILDCARD_TYPE);

        WebTarget target = target("resource");
        final AsyncInvoker async = target.request().async();

        Response response = async.post(entity, generic).get();
        Assert.assertEquals(200, response.getStatus());
        Assert.assertEquals("entity", response.readEntity(String.class));
    }

    @Test
    public void testGet() {
        GenericType<Response> generic = new GenericType<Response>(Response.class);

        WebTarget target = target("resource");
        SyncInvoker sync = target.request();
        Response response = sync.get(generic);
        Assert.assertEquals(200, response.getStatus());
        Assert.assertEquals("get", response.readEntity(String.class));
    }

    @Test
    public void testAsyncGet() throws ExecutionException, InterruptedException {
        GenericType<Response> generic = new GenericType<Response>(Response.class);

        WebTarget target = target("resource");
        final AsyncInvoker async = target.request().async();
        Response response = async.get(generic).get();
        Assert.assertEquals(200, response.getStatus());
        Assert.assertEquals("get", response.readEntity(String.class));
    }

    @Test
    public void testGetGenericString() {
        GenericType<String> generic = new GenericType<String>(String.class);

        WebTarget target = target("resource");
        SyncInvoker sync = target.request();
        final String entity = sync.get(generic);
        Assert.assertEquals("get", entity);
    }
}
