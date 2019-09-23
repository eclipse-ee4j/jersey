/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import org.glassfish.jersey.server.ApplicationHandler;
import org.glassfish.jersey.server.RequestContextBuilder;
import org.glassfish.jersey.server.ResourceConfig;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Taken from Jersey-1: jersey-tests:com.sun.jersey.impl.subresources.SubResourceDynamicWithParametersTest
 *
 * @author Paul Sandoz
 */
public class SubResourceDynamicWithParametersTest {

    ApplicationHandler app;

    private ApplicationHandler createApplication(Class<?>... classes) {
        return new ApplicationHandler(new ResourceConfig(classes));
    }

    @Path("/{p}")
    public static class ParentWithTemplates {

        @GET
        public String getMe(@PathParam("p") String p) {
            return p;
        }

        @Path("child/{c}")
        public ChildWithTemplates getChildWithTemplates(
                @PathParam("p") String p, @PathParam("c") String c,
                @QueryParam("a") int a, @QueryParam("b") int b) {
            assertEquals("parent", p);
            assertEquals("first", c);
            assertEquals(1, a);
            assertEquals(2, b);
            return new ChildWithTemplates();
        }

        @Path(value = "unmatchedPath/{path: .*}")
        public UnmatchedPathResource getUnmatchedPath(
                @PathParam("p") String p,
                @PathParam("path") String path) {
            assertEquals("parent", p);
            return new UnmatchedPathResource(path);
        }
    }

    public static class ChildWithTemplates {

        @GET
        public String getMe(@PathParam("c") String c) {
            return c;
        }
    }

    public static class UnmatchedPathResource {

        String path;

        UnmatchedPathResource(String path) {
            this.path = path;
        }

        @GET
        public String getMe() {
            if (path == null) {
                path = "";
            }
            return path;
        }
    }

    @Test
    public void testSubResourceDynamicWithTemplates() throws Exception {
        app = createApplication(ParentWithTemplates.class);

        assertEquals("parent", app.apply(RequestContextBuilder.from("/parent", "GET").build()).get().getEntity());
        assertEquals("first",
                app.apply(RequestContextBuilder.from("/parent/child/first?a=1&b=2", "GET").build()).get().getEntity());
    }

    @Test
    public void testSubResourceDynamicWithUnmatchedPath() throws Exception {
        app = createApplication(ParentWithTemplates.class);

        assertEquals("", app.apply(RequestContextBuilder.from("/parent/unmatchedPath/", "GET").build()).get().getEntity());
        assertEquals("a/b/c/d",
                app.apply(RequestContextBuilder.from("/parent/unmatchedPath/a/b/c/d", "GET").build()).get().getEntity());
    }
}
