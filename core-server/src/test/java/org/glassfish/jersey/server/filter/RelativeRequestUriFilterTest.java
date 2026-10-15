/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation
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

import java.net.URI;
import java.util.concurrent.ExecutionException;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.UriInfo;

import org.glassfish.jersey.server.ApplicationHandler;
import org.glassfish.jersey.server.ContainerResponse;
import org.glassfish.jersey.server.RequestContextBuilder;
import org.glassfish.jersey.server.ResourceConfig;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests that a relative request URI set in a pre-matching filter is resolved against the base URI.
 */
public class RelativeRequestUriFilterTest {

    private static final String BASE_URI = "http://localhost:8080/rest/";

    @Path("a/b")
    public static class Resource {

        @GET
        public String get(@Context final UriInfo uriInfo, @QueryParam("filter") final String filter) {
            return uriInfo.getRequestUri().toASCIIString() + " " + filter;
        }
    }

    @PreMatching
    public static class RelativeUriFilter implements ContainerRequestFilter {

        @Override
        public void filter(final ContainerRequestContext requestContext) {
            requestContext.setRequestUri(URI.create("a/b?filter=c"));
        }
    }

    @PreMatching
    public static class RelativeUriWithBaseUriFilter implements ContainerRequestFilter {

        @Override
        public void filter(final ContainerRequestContext requestContext) {
            requestContext.setRequestUri(URI.create(BASE_URI), URI.create("a/b?filter=c"));
        }
    }

    @PreMatching
    public static class ServerRelativeUriFilter implements ContainerRequestFilter {

        @Override
        public void filter(final ContainerRequestContext requestContext) {
            requestContext.setRequestUri(URI.create("/rest/a/b?filter=c"));
        }
    }

    @Test
    public void testRelativeRequestUriIsResolvedAgainstBaseUri() throws ExecutionException, InterruptedException {
        final ContainerResponse response = apply(RelativeUriFilter.class);

        assertEquals(200, response.getStatus());
        assertEquals(BASE_URI + "a/b?filter=c c", response.getEntity());
    }

    @Test
    public void testRelativeRequestUriIsResolvedAgainstNewBaseUri() throws ExecutionException, InterruptedException {
        final ContainerResponse response = apply(RelativeUriWithBaseUriFilter.class);

        assertEquals(200, response.getStatus());
        assertEquals(BASE_URI + "a/b?filter=c c", response.getEntity());
    }

    @Test
    public void testServerRelativeRequestUriIsResolvedAgainstBaseUri() throws ExecutionException, InterruptedException {
        final ContainerResponse response = apply(ServerRelativeUriFilter.class);

        assertEquals(200, response.getStatus());
        assertEquals(BASE_URI + "a/b?filter=c c", response.getEntity());
    }

    private static ContainerResponse apply(final Class<? extends ContainerRequestFilter> filter)
            throws ExecutionException, InterruptedException {
        final ApplicationHandler application = new ApplicationHandler(new ResourceConfig(Resource.class).register(filter));
        return application.apply(RequestContextBuilder.from(BASE_URI, BASE_URI + "x", "GET").build()).get();
    }
}
