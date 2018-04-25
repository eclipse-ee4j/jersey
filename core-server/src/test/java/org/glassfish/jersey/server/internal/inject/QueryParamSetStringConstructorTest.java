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

import java.math.BigDecimal;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author Paul Sandoz
 * @author Pavel Bucek (pavel.bucek at oracle.com)
 */
public class QueryParamSetStringConstructorTest extends AbstractTest {

    @Path("/")
    public static class ResourceStringSet {
        @GET
        public String doGetString(@QueryParam("args") Set<BigDecimal> args) {
            assertTrue(args.contains(new BigDecimal("3.145")));
            assertTrue(args.contains(new BigDecimal("2.718")));
            assertTrue(args.contains(new BigDecimal("1.618")));
            return "content";
        }
    }

    @Path("/")
    public static class ResourceStringSetEmptyDefault {
        @GET
        public String doGetString(@QueryParam("args") Set<BigDecimal> args) {
            assertEquals(0, args.size());
            return "content";
        }
    }

    @Path("/")
    public static class ResourceStringSetDefault {
        @GET
        public String doGetString(
                @QueryParam("args") @DefaultValue("3.145") Set<BigDecimal> args) {
            assertTrue(args.contains(new BigDecimal("3.145")));
            return "content";
        }
    }

    @Path("/")
    public static class ResourceStringSetDefaultOverride {
        @GET
        public String doGetString(
                @QueryParam("args") @DefaultValue("3.145") Set<BigDecimal> args) {
            assertTrue(args.contains(new BigDecimal("2.718")));
            return "content";
        }
    }

    @Test
    public void testStringConstructorSetGet() throws ExecutionException, InterruptedException {
        initiateWebApplication(ResourceStringSet.class);

        _test("/?args=3.145&args=2.718&args=1.618", "application/stringSet");
    }

    @Test
    public void testStringConstructorSetNullDefault() throws ExecutionException, InterruptedException {
        initiateWebApplication(ResourceStringSetEmptyDefault.class);

        _test("/");
    }

    @Test
    public void testStringConstructorSetDefault() throws ExecutionException, InterruptedException {
        initiateWebApplication(ResourceStringSetDefault.class);

        _test("/");
    }

    @Test
    public void testStringConstructorSetDefaultOverride() throws ExecutionException, InterruptedException {
        initiateWebApplication(ResourceStringSetDefaultOverride.class);

        _test("/?args=2.718");
    }
}
