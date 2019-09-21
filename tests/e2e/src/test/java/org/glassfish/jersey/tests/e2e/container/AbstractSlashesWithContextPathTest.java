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
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.test.JerseyTest;

/**
 * Abstract Jersey test with prepared resources for URL match
 * testing on different containers.
 *
 * @author Petr Bouda
 */
public class AbstractSlashesWithContextPathTest extends JerseyTest {

    public static final String CONTAINER_RESPONSE = "Container-Response";
    public static final String CONTEXT_PATH = "base";

    @Override
    protected Application configure() {
        ResourceConfig resourceConfig = new ResourceConfig(SimpleResource.class,
                PathParamResource.class,
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

    @Path("pathparam")
    public static class PathParamResource {

        @GET
        @Path("{bar:.*}/{baz:.*}/test")
        public String hello(@PathParam("bar") final String bar, @PathParam("baz") final String baz) {
            return bar + "-" + baz;
        }
    }

    @Path("/")
    public static class EmptyPathParamResource {

        @GET
        @Path("{bar:.*}/{baz:.*}/test")
        public String hello(@PathParam("bar") final String bar, @PathParam("baz") final String baz) {
            return bar + "-" + baz;
        }

        @GET
        @Path("{bar:.*}/{baz:.*}/testParams")
        public String helloWithQueryParams(@PathParam("bar") final String bar,
                                           @PathParam("baz") final String baz,
                                           @QueryParam("bar") final String queryBar,
                                           @QueryParam("baz") final String queryBaz) {
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
        public String hello() {
            return CONTAINER_RESPONSE;
        }
    }

    protected Response call(String path) {
        URI hostPort = UriBuilder.fromUri("http://localhost").port(getPort()).build();
        return client().target(hostPort).path(path).request().get();
    }

    /**
     * Context path configuration
     */
    @Override
    protected URI getBaseUri() {
        URI baseUri = super.getBaseUri();
        return UriBuilder.fromUri(baseUri).path(CONTEXT_PATH).build();
    }
}
