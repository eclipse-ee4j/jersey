/*
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.server.filter;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.ApplicationHandler;
import org.glassfish.jersey.server.ContainerResponse;
import org.glassfish.jersey.server.RequestContextBuilder;
import org.glassfish.jersey.server.ResourceConfig;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 *
 * @author Martin Matula
 */
public class CsrfProtectionFilterTest {

    private ApplicationHandler handler;

    @Path("/resource")
    public static class Resource {

        @GET
        public String get() {
            return "GET";
        }

        @PUT
        public String put() {
            return "PUT";
        }
    }

    @Before
    public void setUp() {
        ResourceConfig rc = new ResourceConfig(Resource.class, CsrfProtectionFilter.class);
        handler = new ApplicationHandler(rc);
    }

    @Test
    public void testGetNoHeader() throws Exception {
        ContainerResponse response = handler.apply(RequestContextBuilder.from("", "/resource", "GET").build()).get();
        assertEquals("GET", response.getEntity());
    }

    @Test
    public void testGetWithHeader() throws Exception {
        ContainerResponse response = handler
                .apply(RequestContextBuilder.from("", "/resource", "GET").header(CsrfProtectionFilter.HEADER_NAME, "").build())
                .get();
        assertEquals("GET", response.getEntity());
    }

    @Test
    public void testPutNoHeader() throws Exception {
        ContainerResponse response = handler.apply(RequestContextBuilder.from("", "/resource", "PUT").build()).get();
        assertEquals(Response.Status.BAD_REQUEST, response.getStatusInfo());
    }

    @Test
    public void testPutWithHeader() throws Exception {
        ContainerResponse response = handler
                .apply(RequestContextBuilder.from("", "/resource", "PUT").header(CsrfProtectionFilter.HEADER_NAME, "").build())
                .get();
        assertEquals("PUT", response.getEntity());
    }
}
