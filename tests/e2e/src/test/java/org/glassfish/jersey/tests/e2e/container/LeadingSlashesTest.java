/*
 * Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.e2e.container;

import java.net.URI;

import javax.ws.rs.Encoded;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;

import org.junit.Test;
import static org.junit.Assert.assertNotEquals;

import static junit.framework.TestCase.assertEquals;

/**
 * Test Jersey container implementation of URL resolving.
 * In this test there is no context path that means that
 * slashes in URL are part of Resource address and couldn't
 * be deleted.
 *
 * @author Petr Bouda
 */
public class LeadingSlashesTest extends JerseyContainerTest {

    public static final String CONTAINER_RESPONSE = "Container-Response";

    @Override
    protected Application configure() {
        ResourceConfig resourceConfig = new ResourceConfig(SimpleResource.class,
                EmptyResource.class,
                EmptyPathParamResource.class);

        resourceConfig.property(ServerProperties.REDUCE_CONTEXT_PATH_SLASHES_ENABLED, true);
        return resourceConfig;
    }

    @Path("simple")
    public static class SimpleResource {

        @GET
        public String encoded() {
            return CONTAINER_RESPONSE;
        }

    }

    @Path("/")
    public static class EmptyPathParamResource {

        @GET
        @Path("{bar:.*}/{baz:.*}/test")
        public String getHello(@PathParam("bar") final String bar, @PathParam("baz") final String baz) {
            return bar + "-" + baz;
        }

        @GET
        @Path("{bar:.*}/{baz:.*}/testParams")
        public String helloWithQueryParams(@PathParam("bar") final String bar, @PathParam("baz") final String baz,
                                           @QueryParam("bar") final String queryBar, @QueryParam("baz") final String queryBaz) {
            return "PATH PARAM: " + bar + "-" + baz + ", QUERY PARAM " + queryBar + "-" + queryBaz;
        }

        @GET
        @Path("{bar:.*}/{baz:.*}/encoded")
        public String getEncoded(@Encoded @QueryParam("query") String queryParam) {
            return queryParam.equals("%25dummy23%2Ba") + ":" + queryParam;
        }
    }

    @Path("/")
    public static class EmptyResource {

        @GET
        @Path("/test")
        public String getHello() {
            return CONTAINER_RESPONSE;
        }
    }

    @Test
    public void testSimpleSlashes() {
        Response result = call("/simple");
        assertEquals(CONTAINER_RESPONSE, result.readEntity(String.class));

        result = call("//simple");
        assertNotEquals(CONTAINER_RESPONSE, result.readEntity(String.class));
    }

    @Test
    public void testSlashesWithBeginningEmpty() {
        Response result = call("/test");
        assertEquals(CONTAINER_RESPONSE, result.readEntity(String.class));
    }

    @Test
    public void testSlashesWithBeginningEmptyPathParam() {
        Response result = call("///test");
        assertEquals("-", result.readEntity(String.class));
    }

    @Test
    public void testSlashesWithBeginningEmptyPathParamWithQueryParams() {
        URI hostPort = UriBuilder.fromUri("http://localhost/").port(getPort()).build();
        WebTarget target = client().target(hostPort).path("///testParams")
                .queryParam("bar", "Container")
                .queryParam("baz", "Response");

        Response result = target.request().get();
        assertEquals("PATH PARAM: -, QUERY PARAM Container-Response", result.readEntity(String.class));
    }

    @Test
    public void testEncodedQueryParams() {
        URI hostPort = UriBuilder.fromUri("http://localhost/").port(getPort()).build();
        WebTarget target = client().target(hostPort).path("///encoded")
                .queryParam("query", "%dummy23+a");

        Response response = target.request().get();
        assertEquals(200, response.getStatus());
        assertEquals("true:%25dummy23%2Ba", response.readEntity(String.class));
    }


    private Response call(String path) {
        URI hostPort = UriBuilder.fromUri("http://localhost/").port(getPort()).build();
        return client().target(hostPort).path(path).request().get();
    }

}
