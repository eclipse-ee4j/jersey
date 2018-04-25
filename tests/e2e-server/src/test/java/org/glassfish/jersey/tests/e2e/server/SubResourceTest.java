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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Assert;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Sub-resource access/processing E2E tests.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 * @author Miroslav Fuksa
 */
public class SubResourceTest extends JerseyTest {

    @Path("root/sub")
    public static class Resource {
        @Path("/")
        public SubResource getSubResourceLocator() {
            return new SubResource();
        }

        @Path("sub2")
        public SubResource getSubResourceLocator2() {
            return new SubResource();
        }

        static final String GET = "get";

        @Path("some/path")
        @GET
        public String get() {
            return GET;
        }

        @Path("empty-locator")
        public EmptySubResourceClass getEmptyLocator() {
            return new EmptySubResourceClass();
        }
    }

    public static class SubResource {
        public static final String MESSAGE = "Got it!";

        @GET
        public String getIt() {
            return MESSAGE;
        }


        @POST
        public String post(String str) {
            return str;
        }
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(Resource.class, LocatorAndMethodResource.class, EmptyRootResource.class);
    }

    /**
     * Test concurrent sub-resource access. (See JERSEY-1421).
     *
     * @throws Exception in case of test failure.
     */
    @Test
    public void testConcurrentSubResourceAccess() throws Exception {
        final WebTarget subResource = target("root/sub/sub2");

        final int MAX = 25;

        final List<Future<String>> results = new ArrayList<Future<String>>(MAX);
        for (int i = 0; i < MAX; i++) {
            results.add(subResource.request().async().get(String.class));
        }

        for (Future<String> resultFuture : results) {
            assertEquals(SubResource.MESSAGE, resultFuture.get());
        }
    }

    @Test
    public void subResourceTest() throws Exception {
        Response response = target("root/sub/sub2").request().get();
        Assert.assertEquals(200, response.getStatus());
        Assert.assertEquals(SubResource.MESSAGE, response.readEntity(String.class));

        response = target("root/sub/sub2").request().get();
        Assert.assertEquals(200, response.getStatus());
        Assert.assertEquals(SubResource.MESSAGE, response.readEntity(String.class));
    }

    @Test
    public void subResourceWithoutPathTest() throws Exception {
        Response response = target("root/sub").request().get();
        Assert.assertEquals(200, response.getStatus());
        Assert.assertEquals(SubResource.MESSAGE, response.readEntity(String.class));
    }

    @Test
    public void testGet() throws Exception {
        Response response = target("root/sub/some/path").request().get();
        Assert.assertEquals(200, response.getStatus());
        Assert.assertEquals(Resource.GET, response.readEntity(String.class));
    }

    @Test
    public void testPost() throws Exception {
        Response response = target("root/sub/sub2").request().post(Entity.entity("post", MediaType.TEXT_PLAIN_TYPE));
        Assert.assertEquals(200, response.getStatus());
        Assert.assertEquals("post", response.readEntity(String.class));
    }

    // this resource class will report warning during validation, but should be loaded
    @Path("locator-and-method")
    public static class LocatorAndMethodResource {
        @GET
        @Path("sub")
        public String getSub() {
            return "get";
        }

        @Path("sub")
        public PostSubResource getSubResourceSub() {
            return new PostSubResource();
        }

        @GET
        public String get() {
            return "get";
        }

        @Path("/")
        public PostSubResource getSubResource() {
            return new PostSubResource();
        }
    }

    public static class PostSubResource {
        @GET
        public String get() {
            return "fail: locator get should never be called !!!";
        }

        @POST
        public String post(String post) {
            return "fail: post should never be called !!!";
        }

        @GET
        @Path("inner")
        public String getInner() {
            return "inner";
        }
    }

    @Test
    public void testGetIsCalled() throws Exception {
        Response response = target("locator-and-method").request().get();
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        Assert.assertEquals("get", response.readEntity(String.class));
    }

    @Test
    public void testGetIsCalledInSub() throws Exception {
        Response response = target("locator-and-method/sub").request().get();
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        Assert.assertEquals("get", response.readEntity(String.class));
    }

    @Test
    public void testGetIsCalledInInner() throws Exception {
        Response response = target("locator-and-method/inner").request().get();
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        Assert.assertEquals("inner", response.readEntity(String.class));
    }

    @Test
    public void testGetIsCalledInSubInner() throws Exception {
        Response response = target("locator-and-method/sub/inner").request().get();
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        Assert.assertEquals("inner", response.readEntity(String.class));
    }

    @Test
    public void testPostShouldNeverBeCalled() throws Exception {
        Response response = target("locator-and-method").request().post(Entity.entity("post", MediaType.TEXT_PLAIN_TYPE));
        Assert.assertEquals(Response.Status.METHOD_NOT_ALLOWED.getStatusCode(), response.getStatus());
    }

    @Test
    public void testPostShouldNeverBeCalledInSub() throws Exception {
        Response response = target("locator-and-method/sub").request().post(Entity.entity("post", MediaType.TEXT_PLAIN_TYPE));
        Assert.assertEquals(Response.Status.METHOD_NOT_ALLOWED.getStatusCode(), response.getStatus());
    }

    @Path("empty-root")
    public static class EmptyRootResource {

    }

    @Test
    public void testCallEmptyResource() throws Exception {
        Response response = target("empty-root").request().get();
        Assert.assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }

    public static class EmptySubResourceClass {
        // empty
    }

    @Test
    public void testCallEmptySubResource() throws Exception {
        Response response = target("empty-locator").request().get();
        Assert.assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }
}
