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

import org.glassfish.jersey.server.ContainerResponse;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author Paul Sandoz
 * @author Pavel Bucek (pavel.bucek at oracle.com)
 */
public class QueryParamAsSetPrimitiveTest extends AbstractTest {

    public QueryParamAsSetPrimitiveTest() {
        initiateWebApplication(
                ResourceQueryPrimitiveSet.class,
                ResourceQueryPrimitiveSetDefaultEmpty.class,
                ResourceQueryPrimitiveSetDefault.class,
                ResourceQueryPrimitiveSetDefaultOverride.class
        );
    }

    @Path("/Set")
    public static class ResourceQueryPrimitiveSet {

        @GET
        @Produces("application/boolean")
        public String doGetBoolean(@QueryParam("boolean") Set<Boolean> v) {
            assertTrue(v.contains(true));
            return "content";
        }

        @GET
        @Produces("application/byte")
        public String doGetByte(@QueryParam("byte") Set<Byte> v) {
            assertTrue(v.contains((byte) 127));
            return "content";
        }

        @GET
        @Produces("application/short")
        public String doGetShort(@QueryParam("short") Set<Short> v) {
            assertTrue(v.contains((short) 32767));
            return "content";
        }

        @GET
        @Produces("application/int")
        public String doGetInteger(@QueryParam("int") Set<Integer> v) {
            assertTrue(v.contains(2147483647));
            return "content";
        }

        @GET
        @Produces("application/long")
        public String doGetLong(@QueryParam("long") Set<Long> v) {
            assertTrue(v.contains(9223372036854775807L));
            return "content";
        }

        @GET
        @Produces("application/float")
        public String doGetFloat(@QueryParam("float") Set<Float> v) {
            assertTrue(v.contains(3.14159265f));
            return "content";
        }

        @GET
        @Produces("application/double")
        public String doGetDouble(@QueryParam("double") Set<Double> v) {
            assertTrue(v.contains(3.14159265358979d));
            return "content";
        }
    }

    @Path("/Set/default/null")
    public static class ResourceQueryPrimitiveSetDefaultEmpty {

        @GET
        @Produces("application/boolean")
        public String doGetBoolean(@QueryParam("boolean") Set<Boolean> v) {
            assertEquals(0, v.size());
            return "content";
        }

        @GET
        @Produces("application/byte")
        public String doGetByte(@QueryParam("byte") Set<Byte> v) {
            assertEquals(0, v.size());
            return "content";
        }

        @GET
        @Produces("application/short")
        public String doGetShort(@QueryParam("short") Set<Short> v) {
            assertEquals(0, v.size());
            return "content";
        }

        @GET
        @Produces("application/int")
        public String doGetInteger(@QueryParam("int") Set<Integer> v) {
            assertEquals(0, v.size());
            return "content";
        }

        @GET
        @Produces("application/long")
        public String doGetLong(@QueryParam("long") Set<Long> v) {
            assertEquals(0, v.size());
            return "content";
        }

        @GET
        @Produces("application/float")
        public String doGetFloat(@QueryParam("float") Set<Float> v) {
            assertEquals(0, v.size());
            return "content";
        }

        @GET
        @Produces("application/double")
        public String doGetDouble(@QueryParam("double") Set<Double> v) {
            assertEquals(0, v.size());
            return "content";
        }
    }

    @Path("/Set/default")
    public static class ResourceQueryPrimitiveSetDefault {

        @GET
        @Produces("application/boolean")
        public String doGetBoolean(@QueryParam("boolean") @DefaultValue("true") Set<Boolean> v) {
            assertTrue(v.contains(true));
            return "content";
        }

        @GET
        @Produces("application/byte")
        public String doGetByte(@QueryParam("byte") @DefaultValue("127") Set<Byte> v) {
            assertTrue(v.contains((byte) 127));
            return "content";
        }

        @GET
        @Produces("application/short")
        public String doGetShort(@QueryParam("short") @DefaultValue("32767") Set<Short> v) {
            assertTrue(v.contains((short) 32767));
            return "content";
        }

        @GET
        @Produces("application/int")
        public String doGetInteger(@QueryParam("int") @DefaultValue("2147483647") Set<Integer> v) {
            assertTrue(v.contains(2147483647));
            return "content";
        }

        @GET
        @Produces("application/long")
        public String doGetLong(@QueryParam("long") @DefaultValue("9223372036854775807") Set<Long> v) {
            assertTrue(v.contains(9223372036854775807L));
            return "content";
        }

        @GET
        @Produces("application/float")
        public String doGetFloat(@QueryParam("float") @DefaultValue("3.14159265") Set<Float> v) {
            assertTrue(v.contains(3.14159265f));
            return "content";
        }

        @GET
        @Produces("application/double")
        public String doGetDouble(@QueryParam("double") @DefaultValue("3.14159265358979") Set<Double> v) {
            assertTrue(v.contains(3.14159265358979d));
            return "content";
        }
    }

    @Path("/Set/default/override")
    public static class ResourceQueryPrimitiveSetDefaultOverride {

        @GET
        @Produces("application/boolean")
        public String doGetBoolean(@QueryParam("boolean") @DefaultValue("false") Set<Boolean> v) {
            assertTrue(v.contains(true));
            return "content";
        }

        @GET
        @Produces("application/byte")
        public String doGetByte(@QueryParam("byte") @DefaultValue("0") Set<Byte> v) {
            assertTrue(v.contains((byte) 127));
            return "content";
        }

        @GET
        @Produces("application/short")
        public String doGetShort(@QueryParam("short") @DefaultValue("0") Set<Short> v) {
            assertTrue(v.contains((short) 32767));
            return "content";
        }

        @GET
        @Produces("application/int")
        public String doGetInteger(@QueryParam("int") @DefaultValue("0") Set<Integer> v) {
            assertTrue(v.contains(2147483647));
            return "content";
        }

        @GET
        @Produces("application/long")
        public String doGetLong(@QueryParam("long") @DefaultValue("0") Set<Long> v) {
            assertTrue(v.contains(9223372036854775807L));
            return "content";
        }

        @GET
        @Produces("application/float")
        public String doGetFloat(@QueryParam("float") @DefaultValue("0.0") Set<Float> v) {
            assertTrue(v.contains(3.14159265f));
            return "content";
        }

        @GET
        @Produces("application/double")
        public String doGetDouble(@QueryParam("double") @DefaultValue("0.0") Set<Double> v) {
            assertTrue(v.contains(3.14159265358979d));
            return "content";
        }
    }

    void _test(String type, String value) throws ExecutionException, InterruptedException {
        String param = type + "=" + value;

        super.getResponseContext("/Set?" + param + "&" + param + "&" + param, "application/" + type);
    }

    void _testDefault(String base, String type, String value) throws ExecutionException, InterruptedException {
        super.getResponseContext(base + "default/null", "application/" + type);

        super.getResponseContext(base + "default", "application/" + type);

        String param = type + "=" + value;
        super.getResponseContext(base + "default/override?" + param, "application/" + type);
    }

    void _testSetDefault(String type, String value) throws ExecutionException, InterruptedException {
        _testDefault("/Set/", type, value);
    }

    @Test
    public void testGetBoolean() throws ExecutionException, InterruptedException {
        _test("boolean", "true");
    }

    @Test
    public void testGetBooleanPrimitiveSetDefault() throws ExecutionException, InterruptedException {
        _testSetDefault("boolean", "true");
    }

    @Test
    public void testGetByte() throws ExecutionException, InterruptedException {
        _test("byte", "127");
    }

    @Test
    public void testGetBytePrimitiveSetDefault() throws ExecutionException, InterruptedException {
        _testSetDefault("byte", "127");
    }

    @Test
    public void testGetShort() throws ExecutionException, InterruptedException {
        _test("short", "32767");
    }

    @Test
    public void testGetShortPrimtiveSetDefault() throws ExecutionException, InterruptedException {
        _testSetDefault("short", "32767");
    }

    @Test
    public void testGetInt() throws ExecutionException, InterruptedException {
        _test("int", "2147483647");
    }

    @Test
    public void testGetIntPrimitiveSetDefault() throws ExecutionException, InterruptedException {
        _testSetDefault("int", "2147483647");
    }

    @Test
    public void testGetLong() throws ExecutionException, InterruptedException {
        _test("long", "9223372036854775807");
    }

    @Test
    public void testGetLongPrimitiveSetDefault() throws ExecutionException, InterruptedException {
        _testSetDefault("long", "9223372036854775807");
    }

    @Test
    public void testGetFloat() throws ExecutionException, InterruptedException {
        _test("float", "3.14159265");
    }

    @Test
    public void testGetFloatPrimitiveSetDefault() throws ExecutionException, InterruptedException {
        _testSetDefault("float", "3.14159265");
    }

    @Test
    public void testGetDouble() throws ExecutionException, InterruptedException {
        _test("double", "3.14159265358979");
    }

    @Test
    public void testGetDoublePrimitiveSetDefault() throws ExecutionException, InterruptedException {
        _testSetDefault("double", "3.14159265358979");
    }

    @Test
    public void testBadPrimitiveSetValue() throws ExecutionException, InterruptedException {
        final ContainerResponse response = super.getResponseContext("/Set?int=abcdef&int=abcdef", "application/int");

        assertEquals(404, response.getStatus());
    }
}
