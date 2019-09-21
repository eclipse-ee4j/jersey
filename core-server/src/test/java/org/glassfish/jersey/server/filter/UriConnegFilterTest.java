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

package org.glassfish.jersey.server.filter;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.server.ApplicationHandler;
import org.glassfish.jersey.server.ContainerResponse;
import org.glassfish.jersey.server.RequestContextBuilder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 *
 * @author Martin Matula
 */
public class UriConnegFilterTest {

    private ApplicationHandler handler;

    @Path("/resource")
    public static class Resource {
        @GET
        @Produces("application/foo")
        public String getFoo() {
            return "foo";
        }

        @GET
        @Produces("application/bar")
        public String getBar() {
            return "bar";
        }
    }

    @Before
    public void setUp() {
        Map<String, MediaType> mediaTypes = new HashMap<>();
        mediaTypes.put("foo", MediaType.valueOf("application/foo"));
        mediaTypes.put("bar", MediaType.valueOf("application/bar"));

        ResourceConfig rc = new ResourceConfig(Resource.class);
        rc.property(ServerProperties.MEDIA_TYPE_MAPPINGS, mediaTypes);
        handler = new ApplicationHandler(rc);
    }

    @Test
    public void testGetFoo() throws Exception {
        ContainerResponse response = handler.apply(
                RequestContextBuilder.from("", "/resource.foo", "GET").build()).get();
        assertEquals("foo", response.getEntity());
    }

    @Test
    public void testGetBar() throws Exception {
        ContainerResponse response = handler.apply(
                RequestContextBuilder.from("", "/resource.bar", "GET").build()).get();
        assertEquals("bar", response.getEntity());
    }
}
