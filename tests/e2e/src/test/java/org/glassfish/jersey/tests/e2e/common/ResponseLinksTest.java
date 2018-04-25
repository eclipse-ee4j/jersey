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

package org.glassfish.jersey.tests.e2e.common;

import java.net.URI;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Libor Kamolis (libor.kramolis at oracle.com)
 */
public class ResponseLinksTest extends JerseyTest {

    @Path("/test")
    public static class MyResource {

        @Context
        private UriInfo uriInfo;

        /**
         * Reproducer for JERSEY-2168
         */
        @Path("1")
        @GET
        @Produces({MediaType.APPLICATION_JSON})
        public Response getLink() {
            URI link = uriInfo.getAbsolutePathBuilder().queryParam("limit", 50).build();
            return Response.status(Response.Status.OK).link(link, "prev").build();
        }

        /**
         * Reproducer for JERSEY-2168
         */
        @Path("2")
        @GET
        @Produces({MediaType.APPLICATION_JSON})
        public Response getLinks() {
            Link link1 = Link.fromUri(uriInfo.getAbsolutePathBuilder().queryParam("limit", 50).build())
                    .rel("prev").build();
            Link link2 = Link.fromUri(
                    uriInfo.getAbsolutePathBuilder().queryParam("limit", 50).queryParam("action", "next").build()).rel("next")
                    .title("next page").build();
            return Response.status(Response.Status.OK).links(link1, link2).build();
        }
    }

    @Override
    protected Application configure() {
        ResourceConfig resourceConfig = new ResourceConfig(MyResource.class);
        resourceConfig.register(LoggingFeature.class);
        return resourceConfig;
    }

    @Override
    protected void configureClient(ClientConfig config) {
        config.register(LoggingFeature.class);
        super.configureClient(config);
    }

    /**
     * Reproducer for JERSEY-2168
     */
    @Test
    public void testGetLink() {
        WebTarget target = target("test");
        Response response = target.path("1").request(MediaType.APPLICATION_JSON).get();
        Set<Link> links = response.getLinks();
        Assert.assertEquals(1, links.size());
        Assert.assertNotNull(response.getLink("prev"));
        Assert.assertTrue(response.getLink("prev").getUri().toString().endsWith("1?limit=50"));
    }

    /**
     * Reproducer for JERSEY-2168
     */
    @Test
    public void testGetLinks() {
        WebTarget target = target("test");
        Response response = target.path("2").request(MediaType.APPLICATION_JSON).get();
        Set<Link> links = response.getLinks();
        Assert.assertEquals(2, links.size());
        Assert.assertNotNull(response.getLink("prev"));
        Assert.assertTrue(response.getLink("prev").getUri().toString().endsWith("2?limit=50"));
        Assert.assertNotNull(response.getLink("next"));
        Assert.assertEquals("next page", response.getLink("next").getTitle());
        Assert.assertTrue(response.getLink("next").getUri().toString().endsWith("2?limit=50&action=next"));
    }

}
