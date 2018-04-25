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

import java.util.Set;
import java.util.concurrent.ExecutionException;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author Paul Sandoz
 * @author Pavel Bucek (pavel.bucek at oracle.com)
 */
@SuppressWarnings("unchecked")
public class QueryParamAsSetStringTest extends AbstractTest {

    @Path("/")
    public static class ResourceStringSet {
        @GET
        @Produces("application/stringSet")
        public String doGetString(@QueryParam("args") Set<String> args) {
            assertTrue(args.contains("a"));
            assertTrue(args.contains("b"));
            assertTrue(args.contains("c"));
            return "content";
        }

        @GET
        @Produces("application/Set")
        public String doGet(@QueryParam("args") Set args) {
            assertTrue(args.contains("a"));
            assertTrue(args.contains("b"));
            assertTrue(args.contains("c"));
            return "content";
        }
    }

    @Path("/")
    public static class ResourceStringSetEmpty {
        @GET
        @Produces("application/stringSet")
        public String doGetString(@QueryParam("args") Set<String> args) {
            assertEquals(1, args.size());
            assertTrue(args.contains(""));
            return "content";
        }
    }

    @Path("/")
    public static class ResourceStringSetEmptyDefault {
        @GET
        @Produces("application/stringSet")
        public String doGetString(
                @QueryParam("args") Set<String> args) {
            assertEquals(0, args.size());
            return "content";
        }

        @GET
        @Produces("application/Set")
        public String doGet(
                @QueryParam("args") Set args) {
            assertEquals(0, args.size());
            return "content";
        }
    }

    @Path("/")
    public static class ResourceStringSetDefault {
        @GET
        @Produces("application/stringSet")
        public String doGetString(
                @QueryParam("args") @DefaultValue("a") Set<String> args) {
            assertTrue(args.contains("a"));
            return "content";
        }

        @GET
        @Produces("application/Set")
        public String doGet(
                @QueryParam("args") @DefaultValue("a") Set args) {
            assertTrue(args.contains("a"));
            return "content";
        }
    }

    @Path("/")
    public static class ResourceStringSetDefaultOverride {
        @GET
        @Produces("application/stringSet")
        public String doGetString(
                @QueryParam("args") @DefaultValue("a") Set<String> args) {
            assertTrue(args.contains("b"));
            return "content";
        }

        @GET
        @Produces("application/Set")
        public String doGet(
                @QueryParam("args") @DefaultValue("a") Set args) {
            assertTrue(args.contains("b"));
            return "content";
        }
    }


    @Test
    public void testStringSetGet() throws ExecutionException, InterruptedException {
        initiateWebApplication(ResourceStringSet.class);

        _test("/?args=a&args=b&args=c", "application/stringSet");
    }

    @Test
    public void testStringSetEmptyGet() throws ExecutionException, InterruptedException {
        initiateWebApplication(ResourceStringSetEmpty.class);

        _test("/?args&args&args", "application/stringSet");
    }

    @Test
    public void testSetGet() throws ExecutionException, InterruptedException {
        initiateWebApplication(ResourceStringSet.class);

        _test("/?args=a&args=b&args=c", "application/Set");
    }

    @Test
    public void testStringSetEmptyDefault() throws ExecutionException, InterruptedException {
        initiateWebApplication(ResourceStringSetEmptyDefault.class);

        _test("/", "application/stringSet");
    }

    @Test
    public void testSetEmptyDefault() throws ExecutionException, InterruptedException {
        initiateWebApplication(ResourceStringSetEmptyDefault.class);

        _test("/", "application/Set");
    }

    @Test
    public void testStringSetDefault() throws ExecutionException, InterruptedException {
        initiateWebApplication(ResourceStringSetDefault.class);

        _test("/", "application/stringSet");
    }

    @Test
    public void testSetDefault() throws ExecutionException, InterruptedException {
        initiateWebApplication(ResourceStringSetDefault.class);

        _test("/", "application/Set");
    }

    @Test
    public void testSetDefaultOverride() throws ExecutionException, InterruptedException {
        initiateWebApplication(ResourceStringSetDefaultOverride.class);

        _test("/?args=b", "application/Set");
    }
}
