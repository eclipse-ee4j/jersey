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
import static org.junit.Assert.assertNull;


/**
 * Tests empty entity in filter.
 * @author Miroslav Fuksa
 *
 */
public class EntityTypeFilterTest {
    @Test
    public void test() throws ExecutionException, InterruptedException {
        ApplicationHandler handler = new ApplicationHandler(new ResourceConfig(Resource.class, ResponseFilter.class));
        final ContainerResponse response = handler.apply(RequestContextBuilder.from("/resource/getentitytype",
                "GET").build()).get();
        assertEquals(200, response.getStatus());
    }


    @Provider
    @Priority(500)
    public static class ResponseFilter implements ContainerResponseFilter {
        @Override
        public void filter(ContainerRequestContext requestContext,
                           ContainerResponseContext responseContext) throws IOException {
            assertNull(responseContext.getEntityType());
            assertNull(responseContext.getEntityClass());
            assertNull(responseContext.getEntity());
        }
    }

    @Path("resource")
    public static class Resource {
        @GET
        @Path("getentitytype")
        public Response getEntityType() {
            Response response = Response.ok().build();
            return response;
        }
    }
}
