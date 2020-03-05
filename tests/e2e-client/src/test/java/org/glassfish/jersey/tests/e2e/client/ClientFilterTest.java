/*
 * Copyright (c) 2012, 2020 Oracle and/or its affiliates. All rights reserved.
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

import java.io.IOException;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.client.ClientResponseContext;
import jakarta.ws.rs.client.ClientResponseFilter;
import jakarta.ws.rs.client.ResponseProcessingException;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.CacheControl;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Pavel Bucek
 */
public class ClientFilterTest extends JerseyTest {

    @Path("/test")
    public static class FooResource {

        @GET
        public String get(@Context HttpHeaders headers) {
            return "GET "
                    + headers.getHeaderString(HttpHeaders.COOKIE)
                    + headers.getHeaderString(HttpHeaders.CACHE_CONTROL);
        }
    }

    public static class CustomRequestFilter implements ClientRequestFilter {

        @Override
        public void filter(ClientRequestContext clientRequestContext) throws IOException {
            clientRequestContext.abortWith(Response.ok("OK!", MediaType.TEXT_PLAIN_TYPE).build());
        }
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(FooResource.class);
    }

    @Test
    public void filterAbortTest() {
        final WebTarget target = target();
        target.register(CustomRequestFilter.class);
        final String entity = target.request().get(String.class);

        assertEquals("OK!", entity);
    }

    public static class HeaderProvidersRequestFilter implements ClientRequestFilter {

        @Override
        public void filter(ClientRequestContext clientRequestContext) throws IOException {
            final CacheControl cacheControl = new CacheControl();
            cacheControl.setPrivate(true);
            clientRequestContext.getHeaders().putSingle(HttpHeaders.CACHE_CONTROL, cacheControl);
            clientRequestContext.getHeaders().putSingle(HttpHeaders.COOKIE, new Cookie("cookie", "cookie-value"));
        }
    }

    @Test
    public void filterHeaderProvidersTest() {
        final WebTarget target = target();
        target.register(HeaderProvidersRequestFilter.class).register(LoggingFeature.class);
        final Response response = target.path("test").request().get(Response.class);
        final String entity = response.readEntity(String.class);

        assertTrue(entity.contains("GET"));
        assertTrue(entity.contains("private"));
        assertTrue(entity.contains("cookie-value"));
    }

    public static class IOExceptionResponseFilter implements ClientResponseFilter {

        @Override
        public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) throws
                IOException {
            throw new IOException(IOExceptionResponseFilter.class.getName());

        }
    }

    @Test
    public void ioExceptionResponseFilterTest() {
        final WebTarget target = target();
        target.register(IOExceptionResponseFilter.class).register(LoggingFeature.class);

        boolean caught = false;

        try {
            target.path("test").request().get(Response.class);
        } catch (ResponseProcessingException e) {
            caught = true;
            assertNotNull(e.getCause());
            assertEquals(IOException.class, e.getCause().getClass());
            assertEquals(IOExceptionResponseFilter.class.getName(), e.getCause().getMessage());
        }

        assertTrue(caught);
    }
}
