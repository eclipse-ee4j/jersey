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

package org.glassfish.jersey.server.internal.inject;

import javax.ws.rs.CookieParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import org.glassfish.jersey.server.ApplicationHandler;
import org.glassfish.jersey.server.ContainerResponse;
import org.glassfish.jersey.server.RequestContextBuilder;
import org.glassfish.jersey.server.ResourceConfig;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * @author Pavel Bucek (pavel.bucek at oracle.com)
 */
public class InvalidParamsTest {

    private ApplicationHandler createApplication(Class<?>... classes) {
        return new ApplicationHandler(new ResourceConfig(classes));
    }

    public static class ParamEntity {

        public static ParamEntity fromString(String arg) {
            return new ParamEntity();
        }
    }

    @Path("invalid/path/param")
    public static class ResourceInvalidParams {

        @PathParam("arg1")
        String f1;
        @QueryParam("arg2")
        String f2;
        @CookieParam("arg3")
        String f3;
        @QueryParam("arg4")
        ParamEntity f4;

        @GET
        public String doGet(@PathParam("arg1") String s1,
                            @QueryParam("arg2") String s2,
                            @CookieParam("arg3") String s3,
                            @QueryParam("arg4") ParamEntity s4) {
            assertEquals(s1, null);
            assertEquals(s2, null);
            assertEquals(s3, null);
            assertEquals(s4, null);
            assertEquals(f1, null);
            assertEquals(f2, null);
            assertEquals(f3, null);
            assertEquals(f4, null);

            return s1;
        }
    }

    @Test
    public void testInvalidPathParam() throws Exception {
        ContainerResponse responseContext = createApplication(ResourceInvalidParams.class)
                .apply(RequestContextBuilder.from("/invalid/path/param", "GET").build()).get();
        // returned param is null -> 204 NO CONTENT
        assertEquals(204, responseContext.getStatus());
    }

    public static class FaultyParamEntityWAE {

        public static FaultyParamEntityWAE fromString(String arg) {
            throw new WebApplicationException(500);
        }
    }

    public static class WebApplicationExceptionMapper implements ExceptionMapper<WebApplicationException> {

        @Override
        public Response toResponse(WebApplicationException exception) {
            return Response.status(500).entity("caught").build();
        }
    }

    @Path("invalid/path/param")
    public static class ResourceWithFaultyParamEntityParamWAE {

        @GET
        public String doGet(@QueryParam("arg4") FaultyParamEntityWAE s4) {
            assertEquals(s4, null);

            return "foo";
        }
    }

    @Test
    public void testInvalidQueryParamParamWAE() throws Exception {
        ContainerResponse responseContext = createApplication(ResourceWithFaultyParamEntityParamWAE.class,
                WebApplicationExceptionMapper.class)
                .apply(RequestContextBuilder.from("/invalid/path/param?arg4=test", "GET").build()).get();

        assertEquals(500, responseContext.getStatus());
        assertEquals("caught", responseContext.getEntity());
    }

    @Path("invalid/path/param")
    public static class ResourceWithFaultyParamEntityFieldWAE {

        @QueryParam("arg4")
        FaultyParamEntityWAE f4;

        @GET
        public String doGet() {
            assertEquals(f4, null);

            return "foo";
        }
    }

    @Test
    public void testInvalidQueryParamFieldWAE() throws Exception {
        ContainerResponse responseContext = createApplication(ResourceWithFaultyParamEntityFieldWAE.class,
                WebApplicationExceptionMapper.class)
                .apply(RequestContextBuilder.from("/invalid/path/param?arg4=test", "GET").build()).get();

        assertEquals(500, responseContext.getStatus());
        assertEquals("caught", responseContext.getEntity());
    }

    public static class FaultyParamEntityISE {

        public static FaultyParamEntityISE fromString(String arg) {
            throw new IllegalStateException("error");
        }
    }

    @Path("invalid/path/param")
    public static class ResourceWithFaultyParamEntityFieldISE {

        @QueryParam("arg4")
        FaultyParamEntityISE f4;

        @GET
        public String doGet() {
            assertEquals(f4, null);

            return "foo";
        }
    }

    @Test
    public void testInvalidQueryParamFieldISE() throws Exception {
        ContainerResponse responseContext = createApplication(ResourceWithFaultyParamEntityFieldISE.class)
                .apply(RequestContextBuilder.from("/invalid/path/param?arg4=test", "GET").build()).get();

        assertEquals(404, responseContext.getStatus());
    }

    @Path("invalid/path/param")
    public static class ResourceWithFaultyParamEntityParamISE {

        @GET
        public String doGet(@QueryParam("arg4") FaultyParamEntityISE s4) {
            assertEquals(s4, null);

            return "foo";
        }
    }

    @Test
    public void testInvalidQueryParamParamISE() throws Exception {
        ContainerResponse responseContext = createApplication(ResourceWithFaultyParamEntityParamISE.class)
                .apply(RequestContextBuilder.from("/invalid/path/param?arg4=test", "GET").build()).get();

        assertEquals(404, responseContext.getStatus());
    }
}
