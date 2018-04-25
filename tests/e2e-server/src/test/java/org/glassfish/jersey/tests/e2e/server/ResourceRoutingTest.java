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

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Assert;
import org.junit.Test;


/**
 * @author Miroslav Fuksa
 */
public class ResourceRoutingTest extends JerseyTest {

    @Path("a")
    public static class ResourceA {
        @Path("b/d")
        @GET
        public String get() {
            // this method cannot be chosen as the request path "a/b/..." always firstly choose the ResourceAB and routing
            // will never check this resource. This is based on the algorithm from the jax-rs spec 2
            return "a/b/d";
        }

        @Path("q")
        @GET
        public String getQ() {
            return "a/q";
        }

    }

    @Path("a/b")
    public static class ResourceAB {
        @Path("c")
        @GET
        public String get() {
            return "a/b/c";
        }
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(ResourceA.class, ResourceAB.class);
    }


    @Test
    public void subWrongPath() throws Exception {
        Response response = target("a/b/d").request().get();
        Assert.assertEquals(404, response.getStatus());
    }

    @Test
    public void correctPath() throws Exception {
        Response response = target("a/b/c").request().get();
        Assert.assertEquals(200, response.getStatus());
        Assert.assertEquals("a/b/c", response.readEntity(String.class));
    }

    @Test
    public void correctPath2() throws Exception {
        Response response = target("a/q").request().get();
        Assert.assertEquals(200, response.getStatus());
        Assert.assertEquals("a/q", response.readEntity(String.class));
    }

}




