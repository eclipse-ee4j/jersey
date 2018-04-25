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
public class UriInfoMatchedUrisTest {

    private ApplicationHandler createApplication(Class<?>... rc) {
        final ResourceConfig resourceConfig = new ResourceConfig(rc);
        return new ApplicationHandler(resourceConfig);
    }

    @Path("/")
    public static class RootPathResource {

        @GET
        public String getRoot(@Context UriInfo uriInfo) {
            assertMatchedUris(uriInfo, "");
            return "root";
        }

        @GET
        @Path("bar")
        public String getRootBar(@Context UriInfo uriInfo) {
            assertMatchedUris(uriInfo, "bar", "");
            return "rootbar";
        }

        @Path("baz")
        public RootSubResource getRootBaz(@Context UriInfo uriInfo) {
            assertMatchedUris(uriInfo, "baz", "");
            return new RootSubResource();
        }
    }

    public static class RootSubResource {

        @GET
        public String getRootBaz(@Context UriInfo uriInfo) {
            assertMatchedUris(uriInfo, "baz", "");
            return "rootbaz";
        }

        @GET
        @Path("bar")
        public String getRootBazBar(@Context UriInfo uriInfo) {
            assertMatchedUris(uriInfo, "baz/bar", "baz", "");
            return "rootbazbar";
        }
    }

    @Path("foo")
    public static class Resource {

        @GET
        public String getFoo(@Context UriInfo uriInfo) {
            assertMatchedUris(uriInfo, "foo");
            return "foo";
        }

        @GET
        @Path("bar")
        public String getFooBar(@Context UriInfo uriInfo) {
            assertMatchedUris(uriInfo, "foo/bar", "foo");
            return "foobar";
        }

        @Path("baz")
        public SubResource getFooBaz(@Context UriInfo uriInfo) {
            assertMatchedUris(uriInfo, "foo/baz", "foo");
            return new SubResource();
        }
    }

    public static class SubResource {

        @GET
        public String getFooBaz(@Context UriInfo uriInfo) {
            assertMatchedUris(uriInfo, "foo/baz", "foo");
            return "foobaz";
        }

        @GET
        @Path("bar")
        public String getFooBazBar(@Context UriInfo uriInfo) {
            assertMatchedUris(uriInfo, "foo/baz/bar", "foo/baz", "foo");
            return "foobazbar";
        }
    }

    @Test
    public void testMatchedUris() throws Exception {
        ApplicationHandler app = createApplication(Resource.class, RootPathResource.class);

        ContainerResponse responseContext;

        responseContext = app.apply(RequestContextBuilder.from("/", "GET").build()).get();
        assertEquals(200, responseContext.getStatus());
        assertEquals("root", responseContext.getEntity());

        responseContext = app.apply(RequestContextBuilder.from("/bar", "GET").build()).get();
        assertEquals(200, responseContext.getStatus());
        assertEquals("rootbar", responseContext.getEntity());

        responseContext = app.apply(RequestContextBuilder.from("/baz", "GET").build()).get();
        assertEquals(200, responseContext.getStatus());
        assertEquals("rootbaz", responseContext.getEntity());

        responseContext = app.apply(RequestContextBuilder.from("/baz/bar", "GET").build()).get();
        assertEquals(200, responseContext.getStatus());
        assertEquals("rootbazbar", responseContext.getEntity());

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

    /**
     * Reproducer test for JERSEY-2071.
     *
     * @throws Exception in case of test error.
     */
    @Test
    public void testAbsoluteMatchedUris() throws Exception {
        ApplicationHandler app = createApplication(Resource.class, RootPathResource.class);

        ContainerResponse responseContext;
        responseContext = app.apply(RequestContextBuilder.from("http://localhost:8080/", "http://localhost:8080/", "GET").build())
                .get();
        assertEquals(200, responseContext.getStatus());
        assertEquals("root", responseContext.getEntity());

        responseContext = app
                .apply(RequestContextBuilder.from("http://localhost:8080/", "http://localhost:8080/bar", "GET").build()).get();
        assertEquals(200, responseContext.getStatus());
        assertEquals("rootbar", responseContext.getEntity());

        responseContext = app
                .apply(RequestContextBuilder.from("http://localhost:8080/", "http://localhost:8080/baz", "GET").build()).get();
        assertEquals(200, responseContext.getStatus());
        assertEquals("rootbaz", responseContext.getEntity());

        responseContext = app
                .apply(RequestContextBuilder.from("http://localhost:8080/", "http://localhost:8080/baz/bar", "GET").build())
                .get();
        assertEquals(200, responseContext.getStatus());
        assertEquals("rootbazbar", responseContext.getEntity());

        responseContext = app
                .apply(RequestContextBuilder.from("http://localhost:8080/", "http://localhost:8080/foo", "GET").build()).get();
        assertEquals(200, responseContext.getStatus());
        assertEquals("foo", responseContext.getEntity());

        responseContext = app
                .apply(RequestContextBuilder.from("http://localhost:8080/", "http://localhost:8080/foo/bar", "GET").build())
                .get();
        assertEquals(200, responseContext.getStatus());
        assertEquals("foobar", responseContext.getEntity());

        responseContext = app
                .apply(RequestContextBuilder.from("http://localhost:8080/", "http://localhost:8080/foo/baz", "GET").build())
                .get();
        assertEquals(200, responseContext.getStatus());
        assertEquals("foobaz", responseContext.getEntity());

        responseContext = app
                .apply(RequestContextBuilder.from("http://localhost:8080/", "http://localhost:8080/foo/baz/bar", "GET").build())
                .get();
        assertEquals(200, responseContext.getStatus());
        assertEquals("foobazbar", responseContext.getEntity());
    }

    private static void assertMatchedUris(UriInfo uriInfo, String... expectedMatchedUris) {
        final List<String> uris = uriInfo.getMatchedURIs();

        assertEquals(expectedMatchedUris.length, uris.size());
        int i = 0;
        for (String uri : expectedMatchedUris) {
            assertEquals(uri, uris.get(i++));
        }
    }
}
