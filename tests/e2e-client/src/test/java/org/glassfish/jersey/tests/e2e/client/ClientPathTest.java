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

package org.glassfish.jersey.tests.e2e.client;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Test definition of path in client invocation.
 *
 * @author Miroslav Fuksa
 *
 */
public class ClientPathTest extends JerseyTest {

    @Override
    protected Application configure() {
        return new ResourceConfig(TestResource.class, TestResourceWithPathParams.class);
    }

    /**
     * Test that {@link PathParam path parameters} can be passed to {@link Client#target(String)} method.
     */
    @Test
    public void pathParamInTargetTest() {

        Response response = client().target("http://localhost:" + getPort() + "/test/{beginBy}")
                .resolveTemplate("beginBy", "abc")
                .request(MediaType.TEXT_PLAIN_TYPE).get();
        assertEquals(200, response.getStatus());
        assertEquals("test-get,abc", response.readEntity(String.class));
    }

    /**
     * Tests path concatenation. (regression test for JERSEY-1114)
     */
    @Test
    public void pathConcatenationTest1() {
        Response response = client().target("http://localhost:" + getPort()).path("path").request(MediaType.TEXT_PLAIN_TYPE)
                .get();
        assertEquals(200, response.getStatus());
        assertEquals("test-path", response.readEntity(String.class));
    }

    /**
     * Tests path concatenation. (regression test for JERSEY-1114)
     */
    @Test
    public void pathConcatenationTest2() {
        Response response = client().target("http://localhost:" + getPort()).path("/path").request(MediaType.TEXT_PLAIN_TYPE)
                .get();
        assertEquals(200, response.getStatus());
        assertEquals("test-path", response.readEntity(String.class));
    }

    /**
     * Tests path concatenation. (regression test for JERSEY-1114)
     */
    @Test
    public void pathConcatenationTest3() {
        Response response = client().target("http://localhost:" + getPort()).path("/path/").path("/another/")
                .request(MediaType.TEXT_PLAIN_TYPE).get();
        assertEquals(200, response.getStatus());
        assertEquals("test-another-path", response.readEntity(String.class));
    }

    /**
     * Tests path concatenation. (regression test for JERSEY-1114)
     */
    @Test
    public void pathConcatenationTest4() {
        Response response = client().target("http://localhost:" + getPort()).path("/path").path("another/")
                .request(MediaType.TEXT_PLAIN_TYPE).get();
        assertEquals(200, response.getStatus());
        assertEquals("test-another-path", response.readEntity(String.class));
    }

    /**
     * Tests path concatenation. (regression test for JERSEY-1114)
     */
    @Test
    public void pathConcatenationTest6() {
        Response response = client().target("http://localhost:" + getPort() + "/").path("/path/another")
                .request(MediaType.TEXT_PLAIN_TYPE).get();
        assertEquals(200, response.getStatus());
        assertEquals("test-another-path", response.readEntity(String.class));
    }

    /**
     * Test resource class with {@link PathParam path parameters).
     *
     */
    @Path("test/{beginBy}")
    public static class TestResourceWithPathParams {

        @GET
        @Produces(MediaType.TEXT_PLAIN)
        @Consumes(MediaType.TEXT_PLAIN)
        public String get(@PathParam(value = "beginBy") String param) {
            return "test-get," + param;
        }

    }

    /**
     * Test resource class.
     *
     */
    @Path("path")
    public static class TestResource {

        @GET
        @Produces(MediaType.TEXT_PLAIN)
        @Consumes(MediaType.TEXT_PLAIN)
        public String get() {
            return "test-path";
        }

        @GET
        @Produces(MediaType.TEXT_PLAIN)
        @Consumes(MediaType.TEXT_PLAIN)
        @Path("another")
        public String getAnother() {
            return "test-another-path";
        }

    }

}
