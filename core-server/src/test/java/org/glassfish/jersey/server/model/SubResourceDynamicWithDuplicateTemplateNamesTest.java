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

import java.util.List;
import java.util.Map.Entry;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.glassfish.jersey.server.ApplicationHandler;
import org.glassfish.jersey.server.RequestContextBuilder;
import org.glassfish.jersey.server.ResourceConfig;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Taken from Jersey-1: jersey-tests:com.sun.jersey.impl.subresources.SubResourceDynamicWithDuplicateTemplateNamesTest
 *
 * @author Paul Sandoz
 */
public class SubResourceDynamicWithDuplicateTemplateNamesTest {

    ApplicationHandler app;

    private ApplicationHandler createApplication(Class<?>... classes) {
        return new ApplicationHandler(new ResourceConfig(classes));
    }

    @Path("/{v}")
    public static class Parent {

        @Path("child/")
        public Child getChild(@PathParam("v") String v) {
            return new Child(v);
        }
    }

    public static class Child {

        private StringBuilder buffer;

        public Child(String v) {
            this.buffer = new StringBuilder(v).append(" -> ");
        }

        @Override
        public String toString() {
            return this.buffer.toString();
        }

        @GET
        public String getMe(@PathParam("v") String v) {
            return this.buffer.append("me() : ").append(v).toString();
        }

        @GET
        @Path("next/{v}")
        public String getMeAndNext(@PathParam("v") String next) {
            return this.buffer.append("next() : ").append(next).toString();
        }

        @GET
        @Path("all")
        public String getAllParams(@Context UriInfo uriInfo) {
            final MultivaluedMap<String, String> params = uriInfo.getPathParameters();

            StringBuilder sb = new StringBuilder();
            for (Entry<String, List<String>> e : params.entrySet()) {
                sb.append("Param '").append(e.getKey()).append("' values:");
                for (String value : e.getValue()) {
                    sb.append(' ').append(value);
                }
            }

            return sb.toString();
        }

        @Path("{v}")
        public Child getChild(@PathParam("v") String v) {
            this.buffer.append(v).append(" -> ");

            return this;
        }
    }

    @Test
    public void testSubResourceDynamicWithTemplates() throws Exception {
        app = createApplication(Parent.class);

        // Parent.getChild(...) -> Child.getMe(...)
        assertEquals("parent -> me() : parent",
                app.apply(RequestContextBuilder.from("/parent/child", "GET").build()).get().getEntity());

        // Parent.getChild(...) -> Child.getChild(...) -> Child.getMe(...)
        assertEquals("parent -> first -> me() : first",
                app.apply(RequestContextBuilder.from("/parent/child/first", "GET").build()).get().getEntity());

        // Parent.getChild(...) -> Child.getChild(...) -> Child.getChild(...) -> Child.getMe(...)
        assertEquals("parent -> first -> second -> me() : second",
                app.apply(RequestContextBuilder.from("/parent/child/first/second", "GET").build()).get().getEntity());

        // Parent.getChild(...) -> Child.getChild(...) -> Child.getChild(...) -> Child.getChild(...) -> Child.getMe(...)
        assertEquals("parent -> first -> second -> third -> me() : third",
                app.apply(RequestContextBuilder.from("/parent/child/first/second/third", "GET").build()).get().getEntity());

        // Parent.getChild(...) -> Child.getChild(...) -> Child.getChild(...) -> Child.getChild(...) -> Child.getMeAndNext(...)
        assertEquals("parent -> first -> second -> third -> next() : fourth",
                app.apply(RequestContextBuilder.from("/parent/child/first/second/third/next/fourth", "GET").build()).get()
                        .getEntity());

        // Parent.getChild(...) -> Child.getChild(...) -> Child.getChild(...) -> Child.getChild(...) -> Child.getChild(...) ->
        // Child.getAllParams(...)
        assertEquals("Param 'v' values: fourth third second first parent",
                app.apply(RequestContextBuilder.from("/parent/child/first/second/third/fourth/all", "GET").build()).get()
                        .getEntity());
    }
}
