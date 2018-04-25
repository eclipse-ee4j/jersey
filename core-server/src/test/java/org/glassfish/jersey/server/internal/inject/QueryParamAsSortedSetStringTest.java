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

import java.util.SortedSet;
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
public class QueryParamAsSortedSetStringTest extends AbstractTest {

    @Path("/")
    public static class ResourceStringSortedSet {
        @GET
        @Produces("application/stringSortedSet")
        public String doGetString(@QueryParam("args") SortedSet<String> args) {
            assertTrue(args.contains("a"));
            assertTrue(args.contains("b"));
            assertTrue(args.contains("c"));
            return "content";
        }

        @GET
        @Produces("application/SortedSet")
        public String doGet(@QueryParam("args") SortedSet args) {
            assertTrue(args.contains("a"));
            assertTrue(args.contains("b"));
            assertTrue(args.contains("c"));
            return "content";
        }
    }

    @Path("/")
    public static class ResourceStringSortedSetEmpty {
        @GET
        @Produces("application/stringSortedSet")
        public String doGetString(@QueryParam("args") SortedSet<String> args) {
            assertEquals(1, args.size());
            assertTrue(args.contains(""));
            return "content";
        }
    }

    @Path("/")
    public static class ResourceStringSortedSetEmptyDefault {
        @GET
        @Produces("application/stringSortedSet")
        public String doGetString(
                @QueryParam("args") SortedSet<String> args) {
            assertEquals(0, args.size());
            return "content";
        }

        @GET
        @Produces("application/SortedSet")
        public String doGet(
                @QueryParam("args") SortedSet args) {
            assertEquals(0, args.size());
            return "content";
        }
    }

    @Path("/")
    public static class ResourceStringSortedSetDefault {
        @GET
        @Produces("application/stringSortedSet")
        public String doGetString(
                @QueryParam("args") @DefaultValue("a") SortedSet<String> args) {
            assertTrue(args.contains("a"));
            return "content";
        }

        @GET
        @Produces("application/SortedSet")
        public String doGet(
                @QueryParam("args") @DefaultValue("a") SortedSet args) {
            assertTrue(args.contains("a"));
            return "content";
        }
    }

    @Path("/")
    public static class ResourceStringSortedSetDefaultOverride {
        @GET
        @Produces("application/stringSortedSet")
        public String doGetString(
                @QueryParam("args") @DefaultValue("a") SortedSet<String> args) {
            assertTrue(args.contains("b"));
            return "content";
        }

        @GET
        @Produces("application/SortedSet")
        public String doGet(
                @QueryParam("args") @DefaultValue("a") SortedSet args) {
            assertTrue(args.contains("b"));
            return "content";
        }
    }

    @Test
    public void testStringSortedSetGet() throws ExecutionException, InterruptedException {
        initiateWebApplication(ResourceStringSortedSet.class);

        _test("/?args=a&args=b&args=c", "application/stringSortedSet");
    }

    @Test
    public void testStringSortedSetEmptyGet() throws ExecutionException, InterruptedException {
        initiateWebApplication(ResourceStringSortedSetEmpty.class);

        _test("/?args&args&args", "application/stringSortedSet");
    }

    @Test
    public void testSortedSetGet() throws ExecutionException, InterruptedException {
        initiateWebApplication(ResourceStringSortedSet.class);

        _test("/?args=a&args=b&args=c", "application/SortedSet");
    }

    @Test
    public void testStringSortedSetEmptyDefault() throws ExecutionException, InterruptedException {
        initiateWebApplication(ResourceStringSortedSetEmptyDefault.class);

        _test("/", "application/stringSortedSet");
    }

    @Test
    public void testSortedSetEmptyDefault() throws ExecutionException, InterruptedException {
        initiateWebApplication(ResourceStringSortedSetEmptyDefault.class);

        _test("/", "application/SortedSet");
    }

    @Test
    public void testStringSortedSetDefault() throws ExecutionException, InterruptedException {
        initiateWebApplication(ResourceStringSortedSetDefault.class);

        _test("/", "application/stringSortedSet");
    }

    @Test
    public void testSortedSetDefault() throws ExecutionException, InterruptedException {
        initiateWebApplication(ResourceStringSortedSetDefault.class);

        _test("/", "application/SortedSet");
    }

    @Test
    public void testSortedSetDefaultOverride() throws ExecutionException, InterruptedException {
        initiateWebApplication(ResourceStringSortedSetDefaultOverride.class);

        _test("/?args=b", "application/SortedSet");
    }
}
