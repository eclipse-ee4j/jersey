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

package org.glassfish.jersey.tests.e2e.server;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.NameBinding;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import javax.annotation.Priority;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * @author Martin Matula
 */
public class AppNameBindingTest extends JerseyTest {
    @NameBinding
    @Retention(RetentionPolicy.RUNTIME)
    private static @interface Global {}

    @NameBinding
    @Retention(RetentionPolicy.RUNTIME)
    private static @interface NameBoundRequest {}

    @Global
    public static class GlobalNameBoundFilter implements ContainerRequestFilter {
        @Override
        public void filter(ContainerRequestContext requestContext) throws IOException {
            requestContext.abortWith(Response.ok("global", MediaType.TEXT_PLAIN_TYPE).build());
        }
    }

    @NameBoundRequest
    @Priority(1)
    public static class NameBoundRequestFilter implements ContainerRequestFilter {
        @Override
        public void filter(ContainerRequestContext requestContext) throws IOException {
            requestContext.abortWith(Response.ok("nameBoundRequest", MediaType.TEXT_PLAIN_TYPE).build());
        }
    }

    @Global
    public static class MyResourceConfig extends ResourceConfig {
        public MyResourceConfig() {
            super(
                    MyResource.class,
                    GlobalNameBoundFilter.class,
                    NameBoundRequestFilter.class
            );
        }
    }

    @Path("/")
    public static class MyResource {
        @Path("nameBoundRequest")
        @GET
        @NameBoundRequest
        public String getNameBoundRequest() {
            return "";
        }

        @Path("global")
        @GET
        public String getPostMatching() {
            return "";
        }
    }

    @Override
    protected Application configure() {
        return new MyResourceConfig();
    }

    @Test
    public void testNameBoundRequest() {
        test("nameBoundRequest");
    }

    @Test
    public void testGlobal() {
        test("global");
    }

    private void test(String name) {
        Response r = target(name).request().get();
        assertEquals(200, r.getStatus());
        assertEquals(name, r.readEntity(String.class));
    }
}
