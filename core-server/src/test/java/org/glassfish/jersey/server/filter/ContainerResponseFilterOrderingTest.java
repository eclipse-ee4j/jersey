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

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import javax.annotation.Priority;

import org.glassfish.jersey.server.ApplicationHandler;
import org.glassfish.jersey.server.ContainerResponse;
import org.glassfish.jersey.server.RequestContextBuilder;
import org.glassfish.jersey.server.ResourceConfig;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Pavel Bucek (pavel.bucek at oracle.com)
 */
public class ContainerResponseFilterOrderingTest {

    @Test
    public void testResponseFilter() throws ExecutionException, InterruptedException {
        ApplicationHandler handler = new ApplicationHandler(
                new ResourceConfig(Resource.class, ResponseFilter1.class, ResponseFilter2.class, ResponseFilter3.class));
        ContainerResponse res = handler.apply(RequestContextBuilder.from("", "/resource/", "GET").build()).get();
        assertEquals(200, res.getStatus());
    }

    @Provider
    @Priority(1)
    public static class ResponseFilter1 implements ContainerResponseFilter {

        public static Boolean called = false;

        @Override
        public void filter(ContainerRequestContext requestContext,
                           ContainerResponseContext responseContext) throws IOException {
            called = true;

            assertTrue(ResponseFilter3.called);
            assertTrue(ResponseFilter2.called);
        }
    }

    @Provider
    @Priority(2)
    public static class ResponseFilter2 implements ContainerResponseFilter {

        public static Boolean called = false;

        @Override
        public void filter(ContainerRequestContext requestContext,
                           ContainerResponseContext responseContext) throws IOException {
            called = true;

            assertTrue(ResponseFilter3.called);
            assertFalse(ResponseFilter1.called);
        }
    }

    @Provider
    @Priority(3)
    public static class ResponseFilter3 implements ContainerResponseFilter {

        public static Boolean called = false;

        @Override
        public void filter(ContainerRequestContext requestContext,
                           ContainerResponseContext responseContext) throws IOException {
            called = true;

            assertFalse(ResponseFilter1.called);
            assertFalse(ResponseFilter2.called);
        }
    }

    @Path("resource")
    public static class Resource {

        @GET
        public Response get() {
            return Response.ok().build();
        }
    }
}
