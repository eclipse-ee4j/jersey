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

package org.glassfish.jersey.server.internal;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.container.ResourceContext;
import javax.ws.rs.core.Context;

import org.glassfish.jersey.server.ApplicationHandler;
import org.glassfish.jersey.server.RequestContextBuilder;
import org.glassfish.jersey.server.ResourceConfig;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Test basic application behavior.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public class ResourceContextTest {

    ApplicationHandler application;

    private ApplicationHandler createApplication(Class<?>... classes) {
        final ResourceConfig resourceConfig = new ResourceConfig(classes);

        return new ApplicationHandler(resourceConfig);
    }

    @Path("a")
    public static class ResourceA {

        @Path("b/{id}")
        public ResourceB resourceB(@Context ResourceContext rc) {
            return rc.getResource(ResourceB.class);
        }

        @Path("{name}")
        public SubResource subResource(@Context ResourceContext rc) {
            return rc.getResource(SubResource.class);
        }

        @GET
        @Path("is-null")
        public String isNull(@Context ResourceContext rc) {
            return (rc.getResource(NotInstantiable.class) == null) ? "null" : "not null";
        }


        @Path("non-instantiable")
        public NotInstantiable notInstantiable(@Context ResourceContext rc) {
            return rc.getResource(NotInstantiable.class);
        }
    }

    @Path("b/{id}")
    public static class ResourceB {

        @PathParam("id")
        private String id;

        @GET
        public String doGet() {
            return "B: " + id;
        }
    }

    public static class SubResource {

        @PathParam("name")
        private String name;

        @GET
        public String doGet() {
            return "SR: " + name;
        }
    }

    public class NotInstantiable {

    }

    @Test
    public void testGetResource() throws Exception {
        ApplicationHandler app = createApplication(ResourceA.class, ResourceB.class);

        assertEquals("B: c",
                app.apply(RequestContextBuilder.from("/a/b/c", "GET").build())
                        .get().getEntity());
        assertEquals("SR: foo",
                app.apply(RequestContextBuilder.from("/a/foo", "GET").build())
                        .get().getEntity());
        assertEquals("null",
                app.apply(RequestContextBuilder.from("/a/is-null", "GET").build())
                        .get().getEntity());
        assertEquals(404,
                app.apply(RequestContextBuilder.from("/a/non-instantiable", "GET").build())
                        .get().getStatus());
    }
}
