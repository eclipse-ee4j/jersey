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

package org.glassfish.jersey.server.internal.routing;

import java.util.concurrent.ExecutionException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.glassfish.jersey.server.ApplicationHandler;
import org.glassfish.jersey.server.ContainerResponse;
import org.glassfish.jersey.server.RequestContextBuilder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;

import org.junit.Assert;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * This tests disabling of a sub resource locator validation.
 *
 * @author Miroslav Fuksa
 *
 */
public class SubResourceValidationTest {

    @Path("root")
    public static class RootResource {
        @Path("sub")
        public InvalidSubResource getSubResource() {
            return new InvalidSubResource();
        }
    }


    public static class InvalidSubResource {
        // invalid: multiple get methods on the same path

        @GET
        public String get() {
            return "get";
        }

        @GET
        @Path("aaa")
        public String aget() {
            return "aaa-get";
        }

        @GET
        @Path("aaa")
        public String aget2() {
            return "aaa-get2";
        }

    }

    @Test
    public void testEnable() throws ExecutionException, InterruptedException {
        ResourceConfig resourceConfig = new ResourceConfig(RootResource.class);
        resourceConfig.property(ServerProperties.RESOURCE_VALIDATION_DISABLE, "false");

        ApplicationHandler applicationHandler = new ApplicationHandler(resourceConfig);

        try {
            final ContainerResponse response = applicationHandler.apply(
                    RequestContextBuilder.from("/root/sub", "GET").build()).get();
            // should throw an exception or return 500
            Assert.assertEquals(500, response.getStatus());
        } catch (Exception e) {
            // ok
        }
    }

    @Test
    public void testDisable() throws ExecutionException, InterruptedException {
        ResourceConfig resourceConfig = new ResourceConfig(RootResource.class);
        resourceConfig.property(ServerProperties.RESOURCE_VALIDATION_DISABLE, "true");

        ApplicationHandler applicationHandler = new ApplicationHandler(resourceConfig);

        final ContainerResponse response = applicationHandler.apply(
                RequestContextBuilder.from("/root/sub", "GET").build()).get();
        assertEquals(200, response.getStatus());
        assertEquals("get", response.getEntity());
    }
}
