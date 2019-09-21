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

package org.glassfish.jersey.server.internal.inject;

import java.util.concurrent.ExecutionException;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.glassfish.jersey.server.ContainerResponse;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * @author Miroslav Fuksa
 *
 */
public class PathParamDefaultValueTest extends AbstractTest {


    @Test
    public void testStandardPathParamValueFoo() throws ExecutionException, InterruptedException {
        initiateWebApplication(FooResource.class);

        ContainerResponse response = getResponseContext("/foo/bar/test");
        assertEquals("test", response.getEntity());
    }

    @Test
    public void testDefaultPathParamValueFoo() throws ExecutionException, InterruptedException {
        initiateWebApplication(FooResource.class);

        ContainerResponse response = getResponseContext("/foo");
        assertEquals("default-id", response.getEntity());
    }

    @Test
    public void testDefaultPathParamValueOnResource1() throws ExecutionException, InterruptedException {
        initiateWebApplication(Resource.class);

        ContainerResponse response = getResponseContext("/foo");
        assertEquals("default-id", response.getEntity());
    }


    @Test
    public void testDefaultPathParamValueOnResource2() throws ExecutionException, InterruptedException {
        initiateWebApplication(Resource.class);

        ContainerResponse response = getResponseContext("/foo/bar/aaa");
        assertEquals("aaa", response.getEntity());
    }

    @Test
    public void testCallWithMissingPathParam404() throws ExecutionException, InterruptedException {
        initiateWebApplication(Resource.class);

        ContainerResponse response = getResponseContext("/foo/bar");
        assertEquals(404, response.getStatus());
    }

    @Test
    public void testDefaultPathParamInSubResource() throws ExecutionException, InterruptedException {
        initiateWebApplication(FooResource.class);

        ContainerResponse response = getResponseContext("/foo/baz/sub");
        assertEquals(200, response.getStatus());
        assertEquals("default-id", response.getEntity());
    }


    @Test
    public void testParamInSubResource() throws ExecutionException, InterruptedException {
        initiateWebApplication(FooResource.class);

        ContainerResponse response = getResponseContext("/foo/baz/iddd");
        assertEquals(200, response.getStatus());
        assertEquals("iddd", response.getEntity());
    }


    @Test
    public void testDefaultPathParamValueOnAnotherResource1() throws ExecutionException, InterruptedException {
        initiateWebApplication(AnotherResource.class);

        ContainerResponse response = getResponseContext("/foo/test/bar/barrr");
        assertEquals("test:barrr", response.getEntity());
    }


    @Test
    public void testDefaultPathParamValueOnAnotherResource2() throws ExecutionException, InterruptedException {
        initiateWebApplication(AnotherResource.class);

        ContainerResponse response = getResponseContext("/foo");
        assertEquals("default-id:default-bar", response.getEntity());
    }


    @Test
    public void testDefaultPathParamValueOnAnotherResource3() throws ExecutionException, InterruptedException {
        initiateWebApplication(AnotherResource.class);

        ContainerResponse response = getResponseContext("/foo/test");
        assertEquals("test:default-bar", response.getEntity());
    }

    @Path("foo")
    public static class FooResource {
        @PathParam("id")
        @DefaultValue("default-id")
        String id;

        @GET
        public String getFoo() {
            return id;
        }

        @GET
        @Path("bar/{id}")
        public String getBar() {
            return id;
        }

        @Path("baz/{id}")
        public Resource getResource() {
            return new Resource();
        }

        @Path("baz/sub")
        public Resource getResource2() {
            return new Resource();
        }

    }

    @Path("foo")
    public static class Resource {
        @GET
        public String getFoo(@PathParam("id") @DefaultValue("default-id") String id) {
            return id;
        }

        @GET
        @Path("bar/{id}")
        public String getBar(@PathParam("id") @DefaultValue("default-id") String id) {
            return id;
        }
    }

    @Path("foo")
    public static class AnotherResource {
        @PathParam("bar")
        @DefaultValue("default-bar")
        String bar;

        @GET
        public String getFoo(@PathParam("id") @DefaultValue("default-id") String id) {
            return id + ":" + bar;
        }

        @GET
        @Path("{id}")
        public String getBar(@PathParam("id") @DefaultValue("default-id") String id) {
            return id + ":" + bar;
        }

        @GET
        @Path("{id}/bar/{bar}")
        public String getBarBar(@PathParam("id") @DefaultValue("default-id") String id) {
            return id + ":" + bar;
        }
    }
}
