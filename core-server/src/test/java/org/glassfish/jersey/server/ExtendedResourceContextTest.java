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

package org.glassfish.jersey.server;

import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

import org.glassfish.jersey.server.model.Resource;
import org.glassfish.jersey.server.model.ResourceModel;
import org.glassfish.jersey.server.model.ResourceTestUtils;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Test (@link ExtendedResourceContext extended resource context}.
 *
 * @author Miroslav Fuksa
 */
public class ExtendedResourceContextTest {

    private static Resource getResource(List<Resource> resources, String path) {
        for (Resource resource : resources) {
            if (resource.getPath().equals(path)) {
                return resource;
            }
        }
        fail("Resource with path '" + path + "' is not in the list of resources " + resources + "!");
        return null;
    }

    @Path("a")
    public static class ResourceA {
        @Context
        ExtendedResourceContext resourceContext;

        @GET
        public String get() {
            return "get";
        }

        @GET
        @Path("child")
        public String childGet() {
            return "child-get";
        }

        @GET
        @Path("model")
        public String model() {
            final ResourceModel resourceModel = resourceContext.getResourceModel();
            final List<Resource> resources = resourceModel.getRootResources();
            final Resource a = getResource(resources, "a");
            ResourceTestUtils.containsMethod(a, "GET");
            ResourceTestUtils.containsMethod(a, "POST");

            final Resource b = getResource(resources, "b");
            ResourceTestUtils.containsMethod(b, "GET");

            final Resource q = getResource(resources, "b");
            ResourceTestUtils.containsMethod(q, "GET");


            assertEquals(3, resources.size());

            return "ok";
        }

    }

    @Path("a")
    public static class ResourceASecond {
        @POST
        public String post(String post) {
            return "post";
        }
    }

    @Path("b")
    public static class ResourceB {
        @GET
        public String get() {
            return "get";
        }

        @GET
        @Path("child")
        public String childGet() {
            return "child-get";
        }
    }


    @Test
    public void testExtendedResourceContext() throws ExecutionException, InterruptedException {
        ApplicationHandler applicationHandler = new ApplicationHandler(new ResourceConfig(ResourceA.class, ResourceASecond.class,
                ResourceB.class));
        final ContainerResponse response = applicationHandler.apply(RequestContextBuilder.from("/a/model", "GET").build()).get();
        assertEquals("ok", response.getEntity());

    }


}
