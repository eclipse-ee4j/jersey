/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.net.URI;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests that configuration of {@link ClientProperties#FOLLOW_REDIRECTS} works when HEAD method is used.
 *
 * @author Paul Sandoz
 * @author Miroslav Fuksa
 */
public class FollowRedirectHeadTest extends JerseyTest {

    @Path("resource")
    public static class Resource {

        @Path("redirect")
        @GET
        public Response redirect() {
            return Response.status(303).location(URI.create("resource/final")).build();
        }

        @Path("final")
        @GET
        public Response afterRedirection() {
            return Response.ok("final-entity").build();
        }
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(Resource.class, LoggingFeature.class);
    }

    private WebTarget getTarget(boolean followRedirect) {
        Client client = ClientBuilder.newClient(new ClientConfig().property(ClientProperties.FOLLOW_REDIRECTS,
                followRedirect));
        return client.target(getBaseUri()).path("resource/redirect");
    }

    @Test
    public void testDontFollowRedirectHead() throws Exception {
        Response response = getTarget(false).request().head();
        Assert.assertEquals(303, response.getStatus());
        Assert.assertTrue(response.getLocation().toString().endsWith("/final"));
    }

    @Test
    public void testDontFollowRedirectGet() throws Exception {
        Response response = getTarget(false).request().get();
        Assert.assertEquals(303, response.getStatus());
        Assert.assertTrue(response.getLocation().toString().endsWith("/final"));
    }

    @Test
    public void testFollowRedirectHead() throws Exception {
        Response response = getTarget(true).request().head();
        Assert.assertEquals(200, response.getStatus());
        Assert.assertFalse(response.hasEntity());
    }

    @Test
    public void testFollowRedirectGet() throws Exception {
        Response response = getTarget(true).request().get();
        Assert.assertEquals(200, response.getStatus());
        Assert.assertEquals("final-entity", response.readEntity(String.class));
    }
}
