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

import org.glassfish.jersey.server.ContainerResponse;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author Paul Sandoz
 * @author Pavel Bucek (pavel.bucek at oracle.com)
 */
public class QueryParamAsSortedSetPrimitiveTest extends AbstractTest {

    public QueryParamAsSortedSetPrimitiveTest() {
        initiateWebApplication(
                ResourceQueryPrimitiveSortedSet.class,
                ResourceQueryPrimitiveSortedSetDefaultEmpty.class,
                ResourceQueryPrimitiveSortedSetDefault.class,
                ResourceQueryPrimitiveSortedSetDefaultOverride.class
        );
    }

    @Path("/SortedSet")
    public static class ResourceQueryPrimitiveSortedSet {

        @GET
        @Produces("application/boolean")
        public String doGetBoolean(@QueryParam("boolean") SortedSet<Boolean> v) {
            assertTrue(v.contains(true));
            return "content";
        }

        @GET
        @Produces("application/byte")
        public String doGetByte(@QueryParam("byte") SortedSet<Byte> v) {
            assertTrue(v.contains((byte) 127));
            return "content";
        }

        @GET
        @Produces("application/short")
        public String doGetShort(@QueryParam("short") SortedSet<Short> v) {
            assertTrue(v.contains((short) 32767));
            return "content";
        }

        @GET
        @Produces("application/int")
        public String doGetInteger(@QueryParam("int") SortedSet<Integer> v) {
            assertTrue(v.contains(2147483647));
            return "content";
        }

        @GET
        @Produces("application/long")
        public String doGetLong(@QueryParam("long") SortedSet<Long> v) {
            assertTrue(v.contains(9223372036854775807L));
            return "content";
        }

        @GET
        @Produces("application/float")
        public String doGetFloat(@QueryParam("float") SortedSet<Float> v) {
            assertTrue(v.contains(3.14159265f));
            return "content";
        }

        @GET
        @Produces("application/double")
        public String doGetDouble(@QueryParam("double") SortedSet<Double> v) {
            assertTrue(v.contains(3.14159265358979d));
            return "content";
        }
    }

    @Path("/SortedSet/default/null")
    public static class ResourceQueryPrimitiveSortedSetDefaultEmpty {

        @GET
        @Produces("application/boolean")
        public String doGetBoolean(@QueryParam("boolean") SortedSet<Boolean> v) {
            assertEquals(0, v.size());
            return "content";
        }

        @GET
        @Produces("application/byte")
        public String doGetByte(@QueryParam("byte") SortedSet<Byte> v) {
            assertEquals(0, v.size());
            return "content";
        }

        @GET
        @Produces("application/short")
        public String doGetShort(@QueryParam("short") SortedSet<Short> v) {
            assertEquals(0, v.size());
            return "content";
        }

        @GET
        @Produces("application/int")
        public String doGetInteger(@QueryParam("int") SortedSet<Integer> v) {
            assertEquals(0, v.size());
            return "content";
        }

        @GET
        @Produces("application/long")
        public String doGetLong(@QueryParam("long") SortedSet<Long> v) {
            assertEquals(0, v.size());
            return "content";
        }

        @GET
        @Produces("application/float")
        public String doGetFloat(@QueryParam("float") SortedSet<Float> v) {
            assertEquals(0, v.size());
            return "content";
        }

        @GET
        @Produces("application/double")
        public String doGetDouble(@QueryParam("double") SortedSet<Double> v) {
            assertEquals(0, v.size());
            return "content";
        }
    }

    @Path("/SortedSet/default")
    public static class ResourceQueryPrimitiveSortedSetDefault {

        @GET
        @Produces("application/boolean")
        public String doGetBoolean(@QueryParam("boolean") @DefaultValue("true") SortedSet<Boolean> v) {
            assertTrue(v.contains(true));
            return "content";
        }

        @GET
        @Produces("application/byte")
        public String doGetByte(@QueryParam("byte") @DefaultValue("127") SortedSet<Byte> v) {
            assertTrue(v.contains((byte) 127));
            return "content";
        }

        @GET
        @Produces("application/short")
        public String doGetShort(@QueryParam("short") @DefaultValue("32767") SortedSet<Short> v) {
            assertTrue(v.contains((short) 32767));
            return "content";
        }

        @GET
        @Produces("application/int")
        public String doGetInteger(@QueryParam("int") @DefaultValue("2147483647") SortedSet<Integer> v) {
            assertTrue(v.contains(2147483647));
            return "content";
        }

        @GET
        @Produces("application/long")
        public String doGetLong(@QueryParam("long") @DefaultValue("9223372036854775807") SortedSet<Long> v) {
            assertTrue(v.contains(9223372036854775807L));
            return "content";
        }

        @GET
        @Produces("application/float")
        public String doGetFloat(@QueryParam("float") @DefaultValue("3.14159265") SortedSet<Float> v) {
            assertTrue(v.contains(3.14159265f));
            return "content";
        }

        @GET
        @Produces("application/double")
        public String doGetDouble(@QueryParam("double") @DefaultValue("3.14159265358979") SortedSet<Double> v) {
            assertTrue(v.contains(3.14159265358979d));
            return "content";
        }
    }

    @Path("/SortedSet/default/override")
    public static class ResourceQueryPrimitiveSortedSetDefaultOverride {

        @GET
        @Produces("application/boolean")
        public String doGetBoolean(@QueryParam("boolean") @DefaultValue("false") SortedSet<Boolean> v) {
            assertTrue(v.contains(true));
            return "content";
        }

        @GET
        @Produces("application/byte")
        public String doGetByte(@QueryParam("byte") @DefaultValue("0") SortedSet<Byte> v) {
            assertTrue(v.contains((byte) 127));
            return "content";
        }

        @GET
        @Produces("application/short")
        public String doGetShort(@QueryParam("short") @DefaultValue("0") SortedSet<Short> v) {
            assertTrue(v.contains((short) 32767));
            return "content";
        }

        @GET
        @Produces("application/int")
        public String doGetInteger(@QueryParam("int") @DefaultValue("0") SortedSet<Integer> v) {
            assertTrue(v.contains(2147483647));
            return "content";
        }

        @GET
        @Produces("application/long")
        public String doGetLong(@QueryParam("long") @DefaultValue("0") SortedSet<Long> v) {
            assertTrue(v.contains(9223372036854775807L));
            return "content";
        }

        @GET
        @Produces("application/float")
        public String doGetFloat(@QueryParam("float") @DefaultValue("0.0") SortedSet<Float> v) {
            assertTrue(v.contains(3.14159265f));
            return "content";
        }

        @GET
        @Produces("application/double")
        public String doGetDouble(@QueryParam("double") @DefaultValue("0.0") SortedSet<Double> v) {
            assertTrue(v.contains(3.14159265358979d));
            return "content";
        }
    }

    void _test(String type, String value) throws ExecutionException, InterruptedException {
        String param = type + "=" + value;

        super.getResponseContext("/SortedSet?" + param + "&" + param + "&" + param, "application/" + type);
    }

    void _testDefault(String base, String type, String value) throws ExecutionException, InterruptedException {
        super.getResponseContext(base + "default/null", "application/" + type);

        super.getResponseContext(base + "default", "application/" + type);

        String param = type + "=" + value;
        super.getResponseContext(base + "default/override?" + param, "application/" + type);
    }

    void _testSortedSetDefault(String type, String value) throws ExecutionException, InterruptedException {
        _testDefault("/SortedSet/", type, value);
    }

    @Test
    public void testGetBoolean() throws ExecutionException, InterruptedException {
        _test("boolean", "true");
    }

    @Test
    public void testGetBooleanPrimitiveSortedSetDefault() throws ExecutionException, InterruptedException {
        _testSortedSetDefault("boolean", "true");
    }

    @Test
    public void testGetByte() throws ExecutionException, InterruptedException {
        _test("byte", "127");
    }

    @Test
    public void testGetBytePrimitiveSortedSetDefault() throws ExecutionException, InterruptedException {
        _testSortedSetDefault("byte", "127");
    }

    @Test
    public void testGetShort() throws ExecutionException, InterruptedException {
        _test("short", "32767");
    }

    @Test
    public void testGetShortPrimtiveSortedSetDefault() throws ExecutionException, InterruptedException {
        _testSortedSetDefault("short", "32767");
    }

    @Test
    public void testGetInt() throws ExecutionException, InterruptedException {
        _test("int", "2147483647");
    }

    @Test
    public void testGetIntPrimitiveSortedSetDefault() throws ExecutionException, InterruptedException {
        _testSortedSetDefault("int", "2147483647");
    }

    @Test
    public void testGetLong() throws ExecutionException, InterruptedException {
        _test("long", "9223372036854775807");
    }

    @Test
    public void testGetLongPrimitiveSortedSetDefault() throws ExecutionException, InterruptedException {
        _testSortedSetDefault("long", "9223372036854775807");
    }

    @Test
    public void testGetFloat() throws ExecutionException, InterruptedException {
        _test("float", "3.14159265");
    }

    @Test
    public void testGetFloatPrimitiveSortedSetDefault() throws ExecutionException, InterruptedException {
        _testSortedSetDefault("float", "3.14159265");
    }

    @Test
    public void testGetDouble() throws ExecutionException, InterruptedException {
        _test("double", "3.14159265358979");
    }

    @Test
    public void testGetDoublePrimitiveSortedSetDefault() throws ExecutionException, InterruptedException {
        _testSortedSetDefault("double", "3.14159265358979");
    }

    @Test
    public void testBadPrimitiveSortedSetValue() throws ExecutionException, InterruptedException {
        final ContainerResponse response = super.getResponseContext("/SortedSet?int=abcdef&int=abcdef", "application/int");

        assertEquals(404, response.getStatus());
    }
}
