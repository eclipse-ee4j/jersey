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

package org.glassfish.jersey.server.model;

import java.util.Set;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

import org.glassfish.jersey.server.ApplicationHandler;
import org.glassfish.jersey.server.ContainerResponse;
import org.glassfish.jersey.server.RequestContextBuilder;
import org.glassfish.jersey.server.ResourceConfig;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The {@code Allow} header of a 405 response lists the same methods as the response to {@code OPTIONS},
 * including the {@code HEAD} method implicitly supported by {@code GET} resource methods
 * (<a href="https://github.com/eclipse-ee4j/jersey/issues/3837">#3837</a>).
 */
public class MethodNotAllowedAllowHeaderTest {

    @Path("/get")
    public static class GetResource {
        @GET
        public String get() {
            return "get";
        }
    }

    @Path("/post")
    public static class PostResource {
        @POST
        public String post(String entity) {
            return entity;
        }
    }

    @Test
    public void allowHeaderIncludesImplicitHead() throws Exception {
        ApplicationHandler app = new ApplicationHandler(new ResourceConfig(GetResource.class));

        ContainerResponse response = app.apply(RequestContextBuilder.from("/get", "DELETE").build()).get();

        assertEquals(405, response.getStatus());
        assertEquals(Set.of(HttpMethod.HEAD, HttpMethod.GET, HttpMethod.OPTIONS), response.getAllowedMethods());
        assertEquals(allowedMethodsOfOptions(app, "/get"), response.getAllowedMethods());
    }

    @Test
    public void allowHeaderHasNoHeadWithoutGet() throws Exception {
        ApplicationHandler app = new ApplicationHandler(new ResourceConfig(PostResource.class));

        ContainerResponse response = app.apply(RequestContextBuilder.from("/post", "DELETE").build()).get();

        assertEquals(405, response.getStatus());
        assertEquals(Set.of(HttpMethod.POST, HttpMethod.OPTIONS), response.getAllowedMethods());
        assertEquals(allowedMethodsOfOptions(app, "/post"), response.getAllowedMethods());
    }

    private static Set<String> allowedMethodsOfOptions(ApplicationHandler app, String path) throws Exception {
        return app.apply(RequestContextBuilder.from(path, HttpMethod.OPTIONS).build()).get().getAllowedMethods();
    }
}
