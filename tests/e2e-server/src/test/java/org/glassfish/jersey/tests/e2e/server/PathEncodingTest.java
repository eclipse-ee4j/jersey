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
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Testing encoding of {@Path path annotations}.
 *
 * @author Miroslav Fuksa
 */
public class PathEncodingTest extends JerseyTest {
    @Override
    protected Application configure() {
        return new ResourceConfig(PercentEncodedTest.class, AsteriskResource.class);
    }

    @Test
    public void test1() {
        assertEquals("ok", target().path("test").path("[]").request().get(String.class));
    }

    @Test
    public void test2() {
        assertEquals("ok", target().path("test").path("%5b%5d").request().get(String.class));
    }

    @Test
    public void test3() {
        assertEquals("ok", target().path("test").path("%5b%5D").request().get(String.class));
    }

    @Test
    public void testComplex1() {
        assertEquals("a-ok", target().path("test").path("test/a/[]").request().get(String.class));
    }

    @Test
    public void testComplex2() {
        assertEquals("a-ok", target().path("test").path("test/a/%5b%5D").request().get(String.class));
    }

    @Test
    public void testComplex3() {
        final Response res = target().path("test").path("test/a/path/%5b%5d").request().get();
        assertEquals(200, res.getStatus());
        assertEquals("a-ok", res.readEntity(String.class));
    }

    @Test
    public void testNotFound() {
        final Response res = target().path("test").path("test/a/path/%5ab").request().get();
        assertEquals(404, res.getStatus());
    }

    @Test
    public void testComplex4() {
        assertEquals("a-ok", target().path("test").path("test/a/path/[]").request().get(String.class));
    }

    @Test
    public void testSlash() {
        assertEquals("ok", target().path("test/slash/").request().get(String.class));
    }

    @Test
    public void testWithoutSlash() {
        assertEquals("ok", target().path("test/slash").request().get(String.class));
    }

    @Test
    public void testAsteriskInPath() {
        Response response = target().path("*").request().get();
        assertEquals(200, response.getStatus());
        assertEquals("ok", response.readEntity(String.class));
    }

    @Path("*")
    public static class AsteriskResource {
        @GET
        public String get() {
            return "ok";
        }
    }

    @Path("test")
    public static class PercentEncodedTest {

        @GET
        @Path("[]")
        public String simple() {
            return "ok";
        }

        @GET
        @Path("slash/")
        public String slash(@Context UriInfo uri) {
            return "ok";
        }

        @GET
        @Path("test/{a : .* }/[]")
        public String complex(@PathParam("a") String a) {
            return a + "-ok";
        }

        @GET
        @Path("test/{a : .* }/path/%5b%5D")
        public String complex2(@PathParam("a") String a) {
            return a + "-ok";
        }


    }
}
