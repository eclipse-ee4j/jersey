/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.io.IOException;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.internal.inject.PerLookup;
import org.glassfish.jersey.process.Inflector;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.model.Resource;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

/**
 * Test resources which also acts as providers.
 *
 * @author Miroslav Fuksa
 */
public class SingletonProvidersResourcesTest extends JerseyTest {

    @Override
    protected Application configure() {
        ResourceConfig resourceConfig = new ResourceConfig(ResourceSingleton.class, ResourceNotSingleton.class);

        final Resource.Builder resourceBuilder = Resource.builder();
        resourceBuilder.name("programmatic").path("programmatic").addMethod("GET")
                .handledBy(ResourceProgrammaticNotSingleton.class);
        resourceConfig.registerResources(resourceBuilder.build());

        return resourceConfig;
    }

    @Test
    public void testResourceAsFilter() {
        String str = target().path("singleton").request().header("singleton", "singleton").get(String.class);
        assertTrue(str, str.startsWith("true/"));
        String str2 = target().path("singleton").request().header("singleton", "singleton").get(String.class);
        assertTrue(str2, str2.startsWith("true/"));
        assertEquals(str, str2);
    }

    @Test
    public void testResourceAsFilterAnnotatedPerLookup() {
        String str = target().path("perlookup").request().header("not-singleton", "not-singleton").get(String.class);
        assertTrue(str.startsWith("false/"));
        String str2 = target().path("perlookup").request().header("not-singleton", "not-singleton").get(String.class);
        assertTrue(str2.startsWith("false/"));
        assertNotSame(str, str2);
    }

    @Test
    public void testResourceProgrammatic() {
        String str = target().path("programmatic").request().header("programmatic", "programmatic").get(String.class);
        assertTrue(str.startsWith("false/"));
        String str2 = target().path("programmatic").request().header("programmatic", "programmatic").get(String.class);
        assertTrue(str2.startsWith("false/"));
        assertNotSame(str, str2);
    }

    // this should be singleton, it means the same instance for the usage as a filter and as an resource
    @Path("singleton")
    public static class ResourceSingleton implements ContainerRequestFilter {

        @Override
        public void filter(ContainerRequestContext requestContext) throws IOException {
            if (requestContext.getHeaders().containsKey("singleton")) {
                requestContext.getHeaders().add("filter-class", this.toString());
            }
        }

        @GET
        public String get(@HeaderParam("filter-class") String filterClass) {
            return String.valueOf(String.valueOf(this.toString().equals(filterClass)) + "/" + this.toString() + ":"
                    + filterClass);
        }
    }

    // this should NOT be singleton, because it is annotated as per lookup
    @Path("perlookup")
    @PerLookup
    public static class ResourceNotSingleton implements ContainerRequestFilter {

        @Override
        public void filter(ContainerRequestContext requestContext) throws IOException {
            if (requestContext.getHeaders().containsKey("not-singleton")) {
                requestContext.getHeaders().add("filter-class", this.toString());
            }
        }

        @GET
        public String get(@HeaderParam("filter-class") String filterClass) {
            return String.valueOf(String.valueOf(this.toString().equals(filterClass)) + "/" + this.toString() + ":"
                    + filterClass);
        }
    }

    // should not be a singleton as this is only programmatic resource and is not registered as provider
    public static class ResourceProgrammaticNotSingleton implements ContainerRequestFilter,
                                                                    Inflector<Request, Response> {

        @Override
        public void filter(ContainerRequestContext requestContext) throws IOException {
            if (requestContext.getHeaders().containsKey("programmatic")) {
                requestContext.getHeaders().add("filter-class", this.toString());
            }
        }

        @Override                            //JerseyContainerRequestContext
        public Response apply(Request request) {
            String filterClass = ((ContainerRequestContext) request).getHeaders().getFirst("filter-class");
            return Response.ok(String.valueOf(String.valueOf(this.toString().equals(filterClass)) + "/" + this.toString() + ":"
                    + filterClass)).build();
        }
    }

}
