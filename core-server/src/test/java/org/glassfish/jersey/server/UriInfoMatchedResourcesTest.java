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

package org.glassfish.jersey.server;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Test UriInfo content.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public class UriInfoMatchedResourcesTest {

    ApplicationHandler application;

    private ApplicationHandler createApplication(Class<?>... rc) {
        final ResourceConfig resourceConfig = new ResourceConfig(rc);
        return new ApplicationHandler(resourceConfig);
    }

    @Path("foo")
    public static class Resource {

        @GET
        public String getFoo(@Context UriInfo uriInfo) {
            assertMatchedResources(uriInfo, Resource.class);
            return "foo";
        }

        @GET
        @Path("bar")
        public String getFooBar(@Context UriInfo uriInfo) {
            assertMatchedResources(uriInfo, Resource.class);
            return "foobar";
        }

        @Path("baz")
        public SubResource getFooBaz(@Context UriInfo uriInfo) {
            assertMatchedResources(uriInfo, Resource.class);
            return new SubResource();
        }
    }

    public static class SubResource {

        @GET
        public String getFooBaz(@Context UriInfo uriInfo) {
            assertMatchedResources(uriInfo, SubResource.class, Resource.class);
            return "foobaz";
        }

        @GET
        @Path("bar")
        public String getFooBazBar(@Context UriInfo uriInfo) {
            assertMatchedResources(uriInfo, SubResource.class, Resource.class);
            return "foobazbar";
        }
    }

    @Test
    public void testMatchedResources() throws Exception {
        ApplicationHandler app = createApplication(Resource.class);

        ContainerResponse responseContext;
        responseContext = app.apply(RequestContextBuilder.from("/foo", "GET").build()).get();
        assertEquals(200, responseContext.getStatus());
        assertEquals("foo", responseContext.getEntity());

        responseContext = app.apply(RequestContextBuilder.from("/foo/bar", "GET").build()).get();
        assertEquals(200, responseContext.getStatus());
        assertEquals("foobar", responseContext.getEntity());

        responseContext = app.apply(RequestContextBuilder.from("/foo/baz", "GET").build()).get();
        assertEquals(200, responseContext.getStatus());
        assertEquals("foobaz", responseContext.getEntity());

        responseContext = app.apply(RequestContextBuilder.from("/foo/baz/bar", "GET").build()).get();
        assertEquals(200, responseContext.getStatus());
        assertEquals("foobazbar", responseContext.getEntity());
    }

    private static void assertMatchedResources(UriInfo uriInfo, Class<?>... expectedMatchedResourceClasses) {
        final List<Object> resources = uriInfo.getMatchedResources();

        assertEquals(expectedMatchedResourceClasses.length, resources.size());
        int i = 0;
        for (Class<?> rc : expectedMatchedResourceClasses) {
            assertEquals(rc, resources.get(i++).getClass());
        }
    }
}
