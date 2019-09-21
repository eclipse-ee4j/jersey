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

import javax.inject.Singleton;

import org.glassfish.jersey.server.ApplicationHandler;
import org.glassfish.jersey.server.RequestContextBuilder;
import org.glassfish.jersey.server.ResourceConfig;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Taken from Jersey-1: jersey-tests:com.sun.jersey.impl.subresources.SubResourceClassDynamicTest
 *
 * @author Paul Sandoz
 */
public class SubResourceClassDynamicTest {

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
        public Class<Child> getChild() {
            return Child.class;
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

        assertEquals("parent", app.apply(RequestContextBuilder.from("/parent", "GET").build()).get().getEntity());
        assertEquals("child", app.apply(RequestContextBuilder.from("/parent/child", "GET").build()).get().getEntity());
    }

    @Path("/{p}")
    public static class ParentWithTemplates {

        @GET
        public String getMe(@PathParam("p") String p) {
            return p;
        }

        @Path("child/{c}")
        public Class<ChildWithTemplates> getChildWithTemplates() {
            return ChildWithTemplates.class;
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

        assertEquals("parent", app.apply(RequestContextBuilder.from("/parent", "GET").build()).get().getEntity());
        assertEquals("first", app.apply(RequestContextBuilder.from("/parent/child/first", "GET").build()).get().getEntity());
    }

    @Path("/{p}")
    public static class ParentWithTemplatesLifecycle {

        @GET
        public String getMe(@PathParam("p") String p) {
            return p;
        }

        @Path("child/{c}")
        public Class<ChildWithTemplatesPerRequest> getChildWithTemplates() {
            return ChildWithTemplatesPerRequest.class;
        }

        @Path("child/singleton/{c}")
        public Class<ChildWithTemplatesSingleton> getChildWithTemplatesSingleton() {
            return ChildWithTemplatesSingleton.class;
        }
    }

    public static class ChildWithTemplatesPerRequest {

        private int i = 0;
        private String c;

        public ChildWithTemplatesPerRequest(@PathParam("c") String c) {
            this.c = c;
        }

        @GET
        public String getMe() {
            i++;
            return c + i;
        }
    }

    @Singleton
    public static class ChildWithTemplatesSingleton {

        private int i = 0;

        @GET
        public String getMe(@PathParam("c") String c) {
            i++;
            return c + i;
        }
    }

    @Test
    public void testSubResourceDynamicWithTemplatesLifecycle() throws Exception {
        app = createApplication(ParentWithTemplatesLifecycle.class);

        assertEquals("parent", app.apply(RequestContextBuilder.from("/parent", "GET").build()).get().getEntity());
        assertEquals("x1", app.apply(RequestContextBuilder.from("/parent/child/x", "GET").build()).get().getEntity());
        assertEquals("x1", app.apply(RequestContextBuilder.from("/parent/child/x", "GET").build()).get().getEntity());
        assertEquals("x1", app.apply(RequestContextBuilder.from("/parent/child/singleton/x", "GET").build()).get().getEntity());
        assertEquals("x2", app.apply(RequestContextBuilder.from("/parent/child/singleton/x", "GET").build()).get().getEntity());
    }
}
