/*
 * Copyright (c) 2023 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.server.model;

import org.glassfish.jersey.server.ApplicationHandler;
import org.glassfish.jersey.server.ContainerResponse;
import org.glassfish.jersey.server.RequestContextBuilder;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.util.concurrent.ExecutionException;

public class ResourceMethodDispatcherInheritanceTest {
    public interface ResourceIfc1 {
        @GET
        public void get();
    }

    @Path("/")
    static class ResourceClass1 implements ResourceIfc1 {
        public void get() {

        }
    }

    interface ResourceIfc2 {
        @GET
        public void get();
    }

    @Path("/")
    public static class ResourceClass2 implements ResourceIfc2 {
        public void get() {

        }
    }

    @Test
    public void testInheritedMethodPublicClass() throws ExecutionException, InterruptedException {
        ApplicationHandler app = new ApplicationHandler(new ResourceConfig(ResourceClass2.class));
        ContainerResponse response;
        response = app.apply(RequestContextBuilder.from("/", "GET").accept("text/plain").build()).get();
        Assertions.assertEquals(204, response.getStatus());
    }

    @Test
    public void testInheritedMethodPublicIface() throws ExecutionException, InterruptedException {
        ApplicationHandler app = new ApplicationHandler(new ResourceConfig(ResourceClass1.class));
        ContainerResponse response;
        response = app.apply(RequestContextBuilder.from("/", "GET").accept("text/plain").build()).get();
        Assertions.assertEquals(204, response.getStatus());
    }
}
