/*
 * Copyright (c) 2014, 2018 Oracle and/or its affiliates. All rights reserved.
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
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.glassfish.jersey.server.ApplicationHandler;
import org.glassfish.jersey.server.ContainerResponse;
import org.glassfish.jersey.server.RequestContextBuilder;
import org.glassfish.jersey.server.ResourceConfig;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Tests capability of URI modification during pre-matching filtering.
 *
 * @author Paul Sandoz
 * @author Libor Kramolis (libor.kramolis at oracle.com)
 */
public class UriModificationFilterTest {

    @Path("/a/b")
    public static class Resource {
        @GET
        public String get(@Context UriInfo ui, @QueryParam("filter") String f) {
            assertEquals("c", f);
            return ui.getRequestUri().toASCIIString();
        }
    }

    @PreMatching
    public static class UriModifyFilter implements ContainerRequestFilter {
        public void filter(ContainerRequestContext requestContext) throws IOException {
            UriBuilder ub = requestContext.getUriInfo().getBaseUriBuilder();

            List<PathSegment> pss = requestContext.getUriInfo().getPathSegments();

            for (int i = 0; i < pss.size() - 1; i++) {
                ub.segment(pss.get(i).getPath());
            }
            ub.queryParam("filter", pss.get(pss.size() - 1).getPath());

            requestContext.setRequestUri(requestContext.getUriInfo().getBaseUri(), ub.build());
        }
    }

    @Test
    public void testWithInstance() throws ExecutionException, InterruptedException {
        final ResourceConfig resourceConfig = new ResourceConfig(Resource.class)
                .register(UriModifyFilter.class);
        final ApplicationHandler application = new ApplicationHandler(resourceConfig);
        final ContainerResponse response = application.apply(RequestContextBuilder.from("/a/b/c", "GET").build()).get();

        assertEquals(200, response.getStatus());
        assertEquals("/a/b?filter=c", response.getEntity());
    }
}
