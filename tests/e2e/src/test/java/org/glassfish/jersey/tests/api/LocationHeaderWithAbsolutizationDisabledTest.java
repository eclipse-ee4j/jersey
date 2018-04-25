/*
 * Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.api;

import java.net.URI;
import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Test if the location response header is left intact in case the
 * {@link ServerProperties#LOCATION_HEADER_RELATIVE_URI_RESOLUTION_DISABLED} property is set to {@code true}.
 *
 * @author Adam Lindenthal (adam.lindenthal at oracle.com)
 */
public class LocationHeaderWithAbsolutizationDisabledTest extends JerseyTest {

    private static final Logger LOGGER = Logger.getLogger(LocationHeaderWithAbsolutizationDisabledTest.class.getName());

    @Override
    protected ResourceConfig configure() {
        enable(TestProperties.LOG_TRAFFIC);
        final ResourceConfig rc = new ResourceConfig(ResponseTest.class);
        rc.property(ServerProperties.LOCATION_HEADER_RELATIVE_URI_RESOLUTION_DISABLED, Boolean.TRUE);
        return rc;
    }

    /**
     * Test JAX-RS resource
     */
    @Path(value = "test")
    public static class ResponseTest {

        /**
         * Resource method for the basic uri test
         * @return test response with relative location uri
         */
        @GET
        @Path("location")
        public Response locationTest() {
            final URI uri = URI.create("location");
            LOGGER.info("URI Created in the resource method > " + uri);
            return Response.created(uri).build();
        }

        /**
         * Resource method for the test with null location
         * @return test response with null location uri
         */
        @GET
        @Path("locationNull")
        public Response locationTestNull() {
            return Response.created(null).build();
        }

        /**
         * Resource method for the test with location starting with single slash
         * @return test response with relative location uri starting with slash
         */
        @GET
        @Path("locationSlash")
        public Response locationTestSlash() {
            return Response.created(URI.create("/location")).build();
        }
    }

    /**
     * Test with relative location;
     * Ensures, that the location remains intact
     */
    @Test
    public void testLocation() {
        final Response response = target().path("test/location").request(MediaType.TEXT_PLAIN).get(Response.class);
        final String location = response.getHeaderString(HttpHeaders.LOCATION);
        LOGGER.info("Location resolved from response > " + location);
        assertEquals("location", location);
    }

    /**
     * Test with relative location with leading slash
     */
    @Test
    public void testLocationWithSlash() {
        final Response response = target().path("test/locationSlash").request(MediaType.TEXT_PLAIN).get(Response.class);
        final String location = response.getHeaderString(HttpHeaders.LOCATION);
        LOGGER.info("Location resolved from response > " + location);
        assertEquals("/location", location);
    }

    /**
     * Test with relative location with leading slash
     */
    @Test
    public void testNullLocation() {
        final Response response = target().path("test/locationNull").request(MediaType.TEXT_PLAIN).get(Response.class);
        final String location = response.getHeaderString(HttpHeaders.LOCATION);
        LOGGER.info("Location resolved from response > " + location);
        assertNull(location);
    }
}



