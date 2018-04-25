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

import org.glassfish.jersey.server.ApplicationHandler;
import org.glassfish.jersey.server.ContainerResponse;
import org.glassfish.jersey.server.RequestContextBuilder;
import org.glassfish.jersey.server.ResourceConfig;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Taken from Jersey-1: jersey-tests:com.sun.jersey.impl.subresources.SubResourceDynamicTest
 *
 * @author Paul Sandoz
 */
public class SubResourceDynamicTest {

    ApplicationHandler app;

    private ApplicationHandler createApplication(Class<?>... classes) {
        return new ApplicationHandler(new ResourceConfig(classes));
    }

    @Path("/parent")
    public static class Parent {

        @GET
        public String getMe() {
            return "parent";
        }

        @Path("child")
        public Child getChild() {
            return new Child();
        }
    }

    public static class Child {

        @GET
        public String getMe() {
            return "child";
        }
    }

    @Test
    public void testSubResourceDynamic() throws Exception {
        app = createApplication(Parent.class);

        ContainerResponse response;

        response = app.apply(RequestContextBuilder.from("/parent", "GET").accept("text/plain").build()).get();
        assertEquals("parent", response.getEntity());

        response = app.apply(RequestContextBuilder.from("/parent/child", "GET").accept("text/plain").build()).get();
        assertEquals("child", response.getEntity());
    }

    @Path("/{p}")
    public static class ParentWithTemplates {

        @GET
        public String getMe(@PathParam("p") String p) {
            return p;
        }

        @Path("child/{c}")
        public ChildWithTemplates getChildWithTemplates() {
            return new ChildWithTemplates();
        }
    }

    public static class ChildWithTemplates {

        @GET
        public String getMe(@PathParam("c") String c) {
            return c;
        }
    }

    @Test
    public void testSubResourceDynamicWithTemplates() throws Exception {
        app = createApplication(ParentWithTemplates.class);

        ContainerResponse response;

        response = app.apply(RequestContextBuilder.from("/parent", "GET").accept("text/plain").build()).get();
        assertEquals("parent", response.getEntity());
        response = app.apply(RequestContextBuilder.from("/parent/child/first", "GET").accept("text/plain").build()).get();
        assertEquals("first", response.getEntity());
    }

    @Path("/")
    public static class SubResourceExplicitRegexCapturingGroups {

        @Path("{a: (\\d)(\\d*)}-{b: (\\d)(\\d*)}-{c: (\\d)(\\d*)}")
        public SubResourceExplicitRegexCapturingGroupsSub getMultiple() {
            return new SubResourceExplicitRegexCapturingGroupsSub();
        }
    }

    public static class SubResourceExplicitRegexCapturingGroupsSub {

        @GET
        @Path("{d}")
        public String getMe(@PathParam("d") String d) {
            return d;
        }
    }

    @Test
    public void testSubResourceCapturingGroups() throws Exception {
        app = createApplication(SubResourceExplicitRegexCapturingGroups.class);

        ContainerResponse response;

        response = app.apply(RequestContextBuilder.from("/123-456-789/d", "GET").accept("text/plain").build()).get();
        assertEquals("d", response.getEntity());
    }
}
