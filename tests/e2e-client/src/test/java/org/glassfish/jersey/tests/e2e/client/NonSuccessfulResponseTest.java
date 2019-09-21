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

package org.glassfish.jersey.tests.e2e.client;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.SyncInvoker;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test no successful (3XX, 4XX, 5XX) responses with no empty body.
 *
 * @author Ballesi Ezequiel (ezequielballesi at gmail.com)
 */
public class NonSuccessfulResponseTest extends JerseyTest {

    @Override
    protected Application configure() {
        return new ResourceConfig(TestResource.class);
    }

    @Override
    protected void configureClient(ClientConfig config) {
        // NOTE: This is not necessary when using connector, that does not follow redirects by default, such as HttpUrlConnector.
        // However, the testcase would fail when FOLLOW_REDIRECTS is set to true. Therefor, we configure the behaviour on
        // redirects explicitly.
        config.property(ClientProperties.FOLLOW_REDIRECTS, false);
    }

    @Path("resource")
    public static class TestResource {

        @GET
        @Path("/{status}")
        public Response getXXX(@PathParam("status") int status) {
            return Response.status(status).entity("get").build();
        }

        @POST
        @Path("/{status}")
        public Response postXXX(@PathParam("status") int status, String post) {
            return Response.status(status).entity(post).build();
        }

    }

    @Test
    public void testGet3XX() {
        generalTestGet(302);
    }

    @Test
    public void testPost3XX() {
        generalTestPost(302);
    }

    @Test
    public void testGet4XX() {
        generalTestGet(401);
    }

    @Test
    public void testPost4XX() {
        generalTestPost(401);
    }

    @Test
    public void testGet5XX() {
        generalTestGet(500);
    }

    @Test
    public void testPost5XX() {
        generalTestPost(500);
    }

    private void generalTestGet(int status) {
        WebTarget target = target("resource").path(Integer.toString(status));
        SyncInvoker sync = target.request();
        Response response = sync.get(Response.class);
        Assert.assertEquals(status, response.getStatus());
        Assert.assertEquals("get", response.readEntity(String.class));
    }

    private void generalTestPost(int status) {
        Entity<String> entity = Entity.entity("entity", MediaType.WILDCARD_TYPE);
        WebTarget target = target("resource").path(Integer.toString(status));
        SyncInvoker sync = target.request();
        Response response = sync.post(entity, Response.class);
        Assert.assertEquals(status, response.getStatus());
        Assert.assertEquals("entity", response.readEntity(String.class));
    }

}
