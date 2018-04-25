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
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import org.glassfish.jersey.server.ApplicationHandler;
import org.glassfish.jersey.server.RequestContextBuilder;
import org.glassfish.jersey.server.ResourceConfig;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Taken from Jersey-1: jersey-tests:com.sun.jersey.impl.subresources.SubResourceHttpMethodsTest
 *
 * @author Paul Sandoz
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
public class SubResourceHttpMethodsTest {

    ApplicationHandler app;

    private ApplicationHandler createApplication(Class<?>... classes) {
        return new ApplicationHandler(new ResourceConfig(classes));
    }

    @Path("/")
    public static class SubResourceMethods {

        @GET
        public String getMe() {
            return "/";
        }

        @Path("sub")
        @GET
        public String getMeSub() {
            return "/sub";
        }

        @Path("sub/sub")
        @GET
        public String getMeSubSub() {
            return "/sub/sub";
        }
    }

    @Test
    public void testSubResourceMethods() throws Exception {
        app = createApplication(SubResourceMethods.class);

        assertEquals("/", app.apply(RequestContextBuilder.from("/", "GET").build()).get().getEntity());
        assertEquals("/sub", app.apply(RequestContextBuilder.from("/sub", "GET").build()).get().getEntity());
        assertEquals("/sub/sub", app.apply(RequestContextBuilder.from("/sub/sub", "GET").build()).get().getEntity());
    }

    @Path("/")
    public static class SubResourceMethodsWithTemplates {

        @GET
        public String getMe() {
            return "/";
        }

        @Path("sub{t}")
        @GET
        public String getMeSub(@PathParam("t") String t) {
            return t;
        }

        @Path("sub/{t}")
        @GET
        public String getMeSubSub(@PathParam("t") String t) {
            return t;
        }

        @Path("subunlimited{t: .*}")
        @GET
        public String getMeSubUnlimited(@PathParam("t") String t) {
            return t;
        }

        @Path("subunlimited/{t: .*}")
        @GET
        public String getMeSubSubUnlimited(@PathParam("t") String t) {
            return t;
        }
    }

    @Test
    public void testSubResourceMethodsWithTemplates() throws Exception {
        app = createApplication(SubResourceMethodsWithTemplates.class);

        assertEquals("/", app.apply(RequestContextBuilder.from("/", "GET").build()).get().getEntity());

        assertEquals("value", app.apply(RequestContextBuilder.from("/subvalue", "GET").build()).get().getEntity());
        assertEquals("a", app.apply(RequestContextBuilder.from("/sub/a", "GET").build()).get().getEntity());

        assertEquals("value/a", app.apply(RequestContextBuilder.from("/subunlimitedvalue/a", "GET").build()).get().getEntity());
        assertEquals("a/b/c/d", app.apply(RequestContextBuilder.from("/subunlimited/a/b/c/d", "GET").build()).get().getEntity());
    }

    @Path("/")
    public static class SubResourceMethodsWithDifferentTemplates {

        @Path("{foo}")
        @GET
        public String getFoo(@PathParam("foo") String foo) {
            return foo;
        }

        // TODO: was bar in the @Path and @PathParam annotations below, shall it work?
        //        @Path("{bar}")
        @Path("{foo}")
        @POST
        public String postBar(@PathParam("foo") String bar) {
            return bar;
        }
    }

    @Test
    public void testSubResourceMethodsWithDifferentTemplates() throws Exception {
        app = createApplication(SubResourceMethodsWithDifferentTemplates.class);

        assertEquals("foo", app.apply(RequestContextBuilder.from("/foo", "GET").build()).get().getEntity());
        assertEquals("bar", app.apply(RequestContextBuilder.from("/bar", "POST").build()).get().getEntity());
    }

    @Path("/{p}/")
    public static class SubResourceMethodWithLimitedTemplate {

        @GET
        public String getMe(@PathParam("p") String p, @QueryParam("id") String id) {
            return p + id;
        }

        @GET
        @Path("{id: .*}")
        public String getUnmatchedPath(
                @PathParam("p") String p,
                @PathParam("id") String path) {
            return path;
        }
    }

    @Test
    public void testSubResourceMethodWithLimitedTemplate() throws Exception {
        app = createApplication(SubResourceMethodWithLimitedTemplate.class);

        assertEquals("topone", app.apply(RequestContextBuilder.from("/top/?id=one", "GET").build()).get().getEntity());
        assertEquals("a/b/c/d", app.apply(RequestContextBuilder.from("/top/a/b/c/d", "GET").build()).get().getEntity());
    }

    @Path("/{p}")
    public static class SubResourceNoSlashMethodWithLimitedTemplate {

        @GET
        public String getMe(@PathParam("p") String p, @QueryParam("id") String id) {
            System.out.println(id);
            return p + id;
        }

        @GET
        @Path(value = "{id: .*}")
        public String getUnmatchedPath(
                @PathParam("p") String p,
                @PathParam("id") String path) {
            return path;
        }
    }

    @Test
    public void testSubResourceNoSlashMethodWithLimitedTemplate() throws Exception {
        app = createApplication(SubResourceNoSlashMethodWithLimitedTemplate.class);

        assertEquals("topone", app.apply(RequestContextBuilder.from("/top?id=one", "GET").build()).get().getEntity());
        assertEquals("a/b/c/d", app.apply(RequestContextBuilder.from("/top/a/b/c/d", "GET").build()).get().getEntity());
    }

    @Path("/")
    public static class SubResourceWithSameTemplate {

        public static class SubResource {

            @GET
            @Path("bar")
            public String get() {
                return "BAR";
            }
        }

        @GET
        @Path("foo")
        public String get() {
            return "FOO";
        }

        @Path("foo")
        public SubResource getUnmatchedPath() {
            return new SubResource();
        }
    }

    @Test
    public void testSubResourceMethodWithSameTemplate() throws Exception {
        app = createApplication(SubResourceWithSameTemplate.class);

        assertEquals("FOO", app.apply(RequestContextBuilder.from("/foo", "GET").build()).get().getEntity());
        assertEquals("BAR", app.apply(RequestContextBuilder.from("/foo/bar", "GET").build()).get().getEntity());
    }

    @Path("/")
    public static class SubResourceExplicitRegex {

        @GET
        @Path("{id}")
        public String getSegment(@PathParam("id") String id) {
            return "segment: " + id;
        }

        @GET
        @Path("{id: .+}")
        public String getSegments(@PathParam("id") String id) {
            return "segments: " + id;
        }

        @GET
        @Path("digit/{id: \\d+}")
        public String getDigit(@PathParam("id") int id) {
            return "digit: " + id;
        }

        @GET
        @Path("digit/{id}")
        public String getDigitAnything(@PathParam("id") String id) {
            return "anything: " + id;
        }
    }

    @Test
    public void testSubResource() throws Exception {
        app = createApplication(SubResourceExplicitRegex.class);

        assertEquals("segments: foo", app.apply(RequestContextBuilder.from("/foo", "GET").build()).get().getEntity());
        assertEquals("segments: foo/bar", app.apply(RequestContextBuilder.from("/foo/bar", "GET").build()).get().getEntity());

        assertEquals("digit: 123", app.apply(RequestContextBuilder.from("/digit/123", "GET").build()).get().getEntity());
        assertEquals("anything: foo", app.apply(RequestContextBuilder.from("/digit/foo", "GET").build()).get().getEntity());
    }

    @Path("/")
    public static class SubResourceExplicitRegexCapturingGroups {

        @GET
        @Path("{a: (\\d)(\\d*)}")
        public String getSingle(@PathParam("a") int a) {
            return "" + a;
        }

        @GET
        @Path("{a: (\\d)(\\d*)}-{b: (\\d)(\\d*)}-{c: (\\d)(\\d*)}")
        public String getMultiple(
                @PathParam("a") int a,
                @PathParam("b") int b,
                @PathParam("c") int c) {
            return "" + a + "-" + b + "-" + c;
        }
    }

    @Test
    public void testSubResourceCapturingGroups() throws Exception {
        app = createApplication(SubResourceExplicitRegexCapturingGroups.class);

        assertEquals("123", app.apply(RequestContextBuilder.from("/123", "GET").build()).get().getEntity());
        assertEquals("123-456-789", app.apply(RequestContextBuilder.from("/123-456-789", "GET").build()).get().getEntity());
    }

    @Path("/")
    public static class SubResourceXXX {

        @GET
        @Path("{id}/literal")
        public String getSegment(@PathParam("id") String id) {
            return id;
        }

        @GET
        @Path("{id1}/{id2}/{id3}")
        public String getSegments(
                @PathParam("id1") String id1,
                @PathParam("id2") String id2,
                @PathParam("id3") String id3) {
            return id1 + id2 + id3;
        }
    }

    @Test
    public void testSubResourceXXX() throws Exception {
        app = createApplication(SubResourceXXX.class);

        assertEquals("123", app.apply(RequestContextBuilder.from("/123/literal", "GET").build()).get().getEntity());
        assertEquals("123literal789", app.apply(RequestContextBuilder.from("/123/literal/789", "GET").build()).get().getEntity());
    }
}
