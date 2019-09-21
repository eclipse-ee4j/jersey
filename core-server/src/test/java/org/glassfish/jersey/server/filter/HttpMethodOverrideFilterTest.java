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

import javax.ws.rs.DELETE;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.ApplicationHandler;
import org.glassfish.jersey.server.ContainerResponse;
import org.glassfish.jersey.server.RequestContextBuilder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Martin Matula
 */
public class HttpMethodOverrideFilterTest {

    @Test
    public void testEnableFor() {
        ResourceConfig rc = new ResourceConfig();
        HttpMethodOverrideFilter.enableFor(rc, HttpMethodOverrideFilter.Source.HEADER);
        assertTrue(rc.getClasses().contains(HttpMethodOverrideFilter.class));
        assertEquals(1, ((HttpMethodOverrideFilter.Source[]) rc.getProperty(ServerProperties.HTTP_METHOD_OVERRIDE)).length);
        assertEquals(HttpMethodOverrideFilter.Source.HEADER,
                ((HttpMethodOverrideFilter.Source[]) rc.getProperty(ServerProperties.HTTP_METHOD_OVERRIDE))[0]);
    }

    @Test
    public void testDefaultConfig() {
        HttpMethodOverrideFilter f = new HttpMethodOverrideFilter(new ResourceConfig());
        assertTrue(HttpMethodOverrideFilter.Source.HEADER.isPresentIn(f.config)
                && HttpMethodOverrideFilter.Source.QUERY.isPresentIn(f.config));
    }

    @Test
    public void testHeaderOnlyConfig() {
        HttpMethodOverrideFilter f = new HttpMethodOverrideFilter(new ResourceConfig().property(
                ServerProperties.HTTP_METHOD_OVERRIDE, "HEADER"
        ));
        assertTrue(HttpMethodOverrideFilter.Source.HEADER.isPresentIn(f.config)
                && !HttpMethodOverrideFilter.Source.QUERY.isPresentIn(f.config));
    }

    @Test
    public void testQueryOnlyConfig() {
        HttpMethodOverrideFilter f = new HttpMethodOverrideFilter(new ResourceConfig().property(
                ServerProperties.HTTP_METHOD_OVERRIDE, "QUERY"
        ));
        assertTrue(!HttpMethodOverrideFilter.Source.HEADER.isPresentIn(f.config)
                && HttpMethodOverrideFilter.Source.QUERY.isPresentIn(f.config));
    }

    @Test
    public void testHeaderAndQueryConfig() {
        HttpMethodOverrideFilter f = new HttpMethodOverrideFilter(new ResourceConfig().property(
                ServerProperties.HTTP_METHOD_OVERRIDE, "HEADER, QUERY"
        ));
        assertTrue(HttpMethodOverrideFilter.Source.HEADER.isPresentIn(f.config)
                && HttpMethodOverrideFilter.Source.QUERY.isPresentIn(f.config));
    }

    @Test
    public void testInvalidAndQueryConfig() {
        HttpMethodOverrideFilter f = new HttpMethodOverrideFilter(new ResourceConfig().property(
                ServerProperties.HTTP_METHOD_OVERRIDE, "foo, QUERY"
        ));
        assertTrue(!HttpMethodOverrideFilter.Source.HEADER.isPresentIn(f.config)
                && HttpMethodOverrideFilter.Source.QUERY.isPresentIn(f.config));
    }

    @Test
    public void testInitWithStringArrayConfig() {
        HttpMethodOverrideFilter f = new HttpMethodOverrideFilter(new ResourceConfig().property(
                ServerProperties.HTTP_METHOD_OVERRIDE, new String[] {"HEADER"}
        ));
        assertTrue(HttpMethodOverrideFilter.Source.HEADER.isPresentIn(f.config)
                && !HttpMethodOverrideFilter.Source.QUERY.isPresentIn(f.config));
    }

    @Path("/")
    public static class Resource {

        @PUT
        public String put() {
            return "PUT";
        }

        @DELETE
        public String delete() {
            return "DELETE";
        }
    }

    @Test
    public void testDefault() {
        assertEquals("400", test());
    }

    @Test
    public void testHeader() {
        assertEquals("PUT", test(HttpMethodOverrideFilter.Source.HEADER));
    }

    @Test
    public void testQuery() {
        assertEquals("DELETE", test(HttpMethodOverrideFilter.Source.QUERY));
    }

    @Test
    public void testHeaderAndQuery() {
        assertEquals("400", test(HttpMethodOverrideFilter.Source.HEADER, HttpMethodOverrideFilter.Source.QUERY));
    }

    public String test(HttpMethodOverrideFilter.Source... sources) {
        ResourceConfig rc = new ResourceConfig(Resource.class);
        HttpMethodOverrideFilter.enableFor(rc, sources);
        ApplicationHandler handler = new ApplicationHandler(rc);
        try {
            ContainerResponse response = handler.apply(RequestContextBuilder.from("", "/?_method=DELETE", "POST")
                    .header("X-HTTP-Method-Override", "PUT").build()).get();
            if (Response.Status.OK.equals(response.getStatusInfo())) {
                return (String) response.getEntity();
            } else {
                return "" + response.getStatus();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }
}
