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

package org.glassfish.jersey.server.internal.inject;

import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.ws.rs.CookieParam;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Cookie;

import org.glassfish.jersey.server.ContainerResponse;
import org.glassfish.jersey.server.RequestContextBuilder;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 *
 * @author Paul Sandoz
 * @author Pavel Bucek (pavel.bucek at oracle.com)
 */
@SuppressWarnings("unchecked")
public class CookieParamAsStringTest extends AbstractTest {

    @Path("/")
    public static class ResourceString {

        @GET
        public String doGet(@CookieParam("arg1") String arg1,
                            @CookieParam("arg2") String arg2, @CookieParam("arg3") String arg3) {
            assertEquals("a", arg1);
            assertEquals("b", arg2);
            assertEquals("c", arg3);
            return "content";
        }

        @POST
        public String doPost(@CookieParam("arg1") String arg1,
                             @CookieParam("arg2") String arg2, @CookieParam("arg3") String arg3,
                             String r) {
            assertEquals("a", arg1);
            assertEquals("b", arg2);
            assertEquals("c", arg3);
            assertEquals("content", r);
            return "content";
        }
    }

    @Path("/")
    public static class ResourceStringEmpty {

        @GET
        public String doGet(@CookieParam("arg1") String arg1) {
            assertEquals("", arg1);
            return "content";
        }
    }

    @Path("/")
    public static class ResourceStringAbsent {

        @GET
        public String doGet(@CookieParam("arg1") String arg1) {
            assertEquals(null, arg1);
            return "content";
        }
    }

    @Path("/")
    public static class ResourceStringList {

        @GET
        @Produces("application/stringlist")
        public String doGetString(@CookieParam("args") List<String> args) {
            assertEquals("a", args.get(0));
            return "content";
        }

        @GET
        @Produces("application/list")
        public String doGet(@CookieParam("args") List args) {
            assertEquals(String.class, args.get(0).getClass());
            assertEquals("a", args.get(0));
            return "content";
        }
    }

    @Path("/")
    public static class ResourceStringListEmpty {

        @GET
        @Produces("application/stringlist")
        public String doGetString(@CookieParam("args") List<String> args) {
            assertEquals(1, args.size());
            assertEquals("", args.get(0));
            return "content";
        }
    }

    @Path("/")
    public static class ResourceStringNullDefault {

        @GET
        public String doGet(@CookieParam("arg1") String arg1,
                            @CookieParam("arg2") String arg2, @CookieParam("arg3") String arg3) {
            assertEquals(null, arg1);
            assertEquals(null, arg2);
            assertEquals(null, arg3);
            return "content";
        }
    }

    @Path("/")
    public static class ResourceStringDefault {

        @GET
        public String doGet(
                @CookieParam("arg1") @DefaultValue("a") String arg1,
                @CookieParam("arg2") @DefaultValue("b") String arg2,
                @CookieParam("arg3") @DefaultValue("c") String arg3) {
            assertEquals("a", arg1);
            assertEquals("b", arg2);
            assertEquals("c", arg3);
            return "content";
        }
    }

    @Path("/")
    public static class ResourceStringDefaultOverride {

        @GET
        public String doGet(
                @CookieParam("arg1") @DefaultValue("a") String arg1,
                @CookieParam("arg2") @DefaultValue("b") String arg2,
                @CookieParam("arg3") @DefaultValue("c") String arg3) {
            assertEquals("d", arg1);
            assertEquals("e", arg2);
            assertEquals("f", arg3);
            return "content";
        }
    }

    @Path("/")
    public static class ResourceStringListEmptyDefault {

        @GET
        @Produces("application/stringlist")
        public String doGetString(
                @CookieParam("args") List<String> args) {
            assertEquals(0, args.size());
            return "content";
        }

        @GET
        @Produces("application/list")
        public String doGet(
                @CookieParam("args") List args) {
            assertEquals(0, args.size());
            return "content";
        }
    }

    @Path("/")
    public static class ResourceStringListDefault {

        @GET
        @Produces("application/stringlist")
        public String doGetString(
                @CookieParam("args") @DefaultValue("a") List<String> args) {
            assertEquals("a", args.get(0));
            return "content";
        }

        @GET
        @Produces("application/list")
        public String doGet(
                @CookieParam("args") @DefaultValue("a") List args) {
            assertEquals(String.class, args.get(0).getClass());
            assertEquals("a", args.get(0));
            return "content";
        }
    }

    @Path("/")
    public static class ResourceStringListDefaultOverride {

        @GET
        @Produces("application/stringlist")
        public String doGetString(
                @CookieParam("args") @DefaultValue("a") List<String> args) {
            assertEquals("b", args.get(0));
            return "content";
        }

        @GET
        @Produces("application/list")
        public String doGet(
                @CookieParam("args") @DefaultValue("a") List args) {
            assertEquals(String.class, args.get(0).getClass());
            assertEquals("b", args.get(0));
            return "content";
        }
    }

    @Test
    public void testStringGet() throws ExecutionException, InterruptedException {
        initiateWebApplication(ResourceString.class);

        _test("/", new Cookie("arg1", "a"), new Cookie("arg2", "b"), new Cookie("arg3", "c"));
    }

    @Test
    public void testStringEmptyGet() throws ExecutionException, InterruptedException {
        initiateWebApplication(ResourceStringEmpty.class);

        _test("/", new Cookie("arg1", ""));
    }

    @Test
    public void testStringAbsentGet() throws ExecutionException, InterruptedException {
        initiateWebApplication(ResourceStringAbsent.class);

        _test("/");
    }

    @Test
    public void testStringPost() throws ExecutionException, InterruptedException {
        initiateWebApplication(ResourceString.class);

        final ContainerResponse responseContext = apply(
                RequestContextBuilder
                        .from("/", "POST")
                        .cookie(new Cookie("arg1", "a"))
                        .cookie(new Cookie("arg2", "b"))
                        .cookie(new Cookie("arg3", "c"))
                        .entity("content").build()
        );

        assertEquals("content", responseContext.getEntity());
    }

    @Test
    public void testStringListGet() throws ExecutionException, InterruptedException {
        initiateWebApplication(ResourceStringList.class);

        _test("/", "application/stringlist", new Cookie("args", "a"));
    }

    @Test
    public void testStringListEmptyGet() throws ExecutionException, InterruptedException {
        initiateWebApplication(ResourceStringListEmpty.class);

        _test("/", "application/stringlist", new Cookie("args", ""));
    }

    @Test
    public void testListGet() throws ExecutionException, InterruptedException {
        initiateWebApplication(ResourceStringList.class);

        _test("/", "application/list", new Cookie("args", "a"));
    }

    @Test
    public void testStringNullDefault() throws ExecutionException, InterruptedException {
        initiateWebApplication(ResourceStringNullDefault.class);

        _test("/");
    }

    @Test
    public void testStringDefault() throws ExecutionException, InterruptedException {
        initiateWebApplication(ResourceStringDefault.class);

        _test("/");
    }

    @Test
    public void testStringDefaultOverride() throws ExecutionException, InterruptedException {
        initiateWebApplication(ResourceStringDefaultOverride.class);

        _test("/", new Cookie("arg1", "d"), new Cookie("arg2", "e"), new Cookie("arg3", "f"));
    }

    @Test
    public void testStringListEmptyDefault() throws ExecutionException, InterruptedException {
        initiateWebApplication(ResourceStringListEmptyDefault.class);

        _test("/", "application/stringlist");
    }

    @Test
    public void testListEmptyDefault() throws ExecutionException, InterruptedException {
        initiateWebApplication(ResourceStringListEmptyDefault.class);

        _test("/", "application/list");
    }

    @Test
    public void testStringListDefault() throws ExecutionException, InterruptedException {
        initiateWebApplication(ResourceStringListDefault.class);

        _test("/", "application/stringlist");
    }

    @Test
    public void testListDefault() throws ExecutionException, InterruptedException {
        initiateWebApplication(ResourceStringListDefault.class);

        _test("/", "application/list");
    }

    @Test
    public void testListDefaultOverride() throws ExecutionException, InterruptedException {
        initiateWebApplication(ResourceStringListDefaultOverride.class);

        _test("/", "application/list", new Cookie("args", "b"));
    }
}
