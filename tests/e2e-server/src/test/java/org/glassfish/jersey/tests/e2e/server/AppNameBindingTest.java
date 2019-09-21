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

package org.glassfish.jersey.tests.e2e.server;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.ws.rs.GET;
import javax.ws.rs.NameBinding;
import javax.ws.rs.Path;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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
