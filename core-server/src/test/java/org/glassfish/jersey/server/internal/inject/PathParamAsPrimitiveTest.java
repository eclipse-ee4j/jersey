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
 * Taken from Jersey-1: jersey-tests: com.sun.jersey.impl.methodparams.PathParamAsPrimitiveTest
 *
 * @author Paul Sandoz
 */
public class PathParamAsPrimitiveTest {

    ApplicationHandler app;

    private ApplicationHandler createApplication(Class<?>... classes) {
        return new ApplicationHandler(new ResourceConfig(classes));
    }

    public PathParamAsPrimitiveTest() {
        app = createApplication(
                ResourceUriBoolean.class,
                ResourceUriByte.class,
                ResourceUriCharacter.class,
                ResourceUriShort.class,
                ResourceUriInt.class,
                ResourceUriLong.class,
                ResourceUriFloat.class,
                ResourceUriDouble.class,
                ResourceUriBooleanWrapper.class,
                ResourceUriByteWrapper.class,
                ResourceUriCharacterWrapper.class,
                ResourceUriShortWrapper.class,
                ResourceUriIntWrapper.class,
                ResourceUriLongWrapper.class,
                ResourceUriFloatWrapper.class,
                ResourceUriDoubleWrapper.class);
    }

    @Path("/boolean/{arg}")
    public static class ResourceUriBoolean {

        @GET
        public String doGet(@PathParam("arg") boolean v) {
            assertEquals(true, v);
            return "content";
        }
    }

    @Path("/byte/{arg}")
    public static class ResourceUriByte {

        @GET
        public String doGet(@PathParam("arg") byte v) {
            assertEquals(127, v);
            return "content";
        }
    }
    @Path("/char/{arg}")
    public static class ResourceUriCharacter {

        @GET
        public String doGet(@PathParam("arg") char v) {
            assertEquals('c', v);
            return "content";
        }
    }

    @Path("/short/{arg}")
    public static class ResourceUriShort {

        @GET
        public String doGet(@PathParam("arg") short v) {
            assertEquals(32767, v);
            return "content";
        }
    }

    @Path("/int/{arg}")
    public static class ResourceUriInt {

        @GET
        public String doGet(@PathParam("arg") int v) {
            assertEquals(2147483647, v);
            return "content";
        }
    }

    @Path("/long/{arg}")
    public static class ResourceUriLong {

        @GET
        public String doGet(@PathParam("arg") long v) {
            assertEquals(9223372036854775807L, v);
            return "content";
        }
    }

    @Path("/float/{arg}")
    public static class ResourceUriFloat {

        @GET
        public String doGet(@PathParam("arg") float v) {
            assertEquals(3.14159265f, v, 0f);
            return "content";
        }
    }

    @Path("/double/{arg}")
    public static class ResourceUriDouble {

        @GET
        public String doGet(@PathParam("arg") double v) {
            assertEquals(3.14159265358979d, v, 0d);
            return "content";
        }
    }

    @Path("/boolean/wrapper/{arg}")
    public static class ResourceUriBooleanWrapper {

        @GET
        public String doGet(@PathParam("arg") Boolean v) {
            assertEquals(true, v);
            return "content";
        }
    }

    @Path("/byte/wrapper/{arg}")
    public static class ResourceUriByteWrapper {

        @GET
        public String doGet(@PathParam("arg") Byte v) {
            assertEquals(127, v.byteValue());
            return "content";
        }
    }

    @Path("/char/wrapper/{arg}")
    public static class ResourceUriCharacterWrapper {

        @GET
        public String doGet(@PathParam("arg") Character v) {
            assertEquals('c', v.charValue());
            return "content";
        }
    }

    @Path("/short/wrapper/{arg}")
    public static class ResourceUriShortWrapper {

        @GET
        public String doGet(@PathParam("arg") Short v) {
            assertEquals(32767, v.shortValue());
            return "content";
        }
    }

    @Path("/int/wrapper/{arg}")
    public static class ResourceUriIntWrapper {

        @GET
        public String doGet(@PathParam("arg") Integer v) {
            assertEquals(2147483647, v.intValue());
            return "content";
        }
    }

    @Path("/long/wrapper/{arg}")
    public static class ResourceUriLongWrapper {

        @GET
        public String doGet(@PathParam("arg") Long v) {
            assertEquals(9223372036854775807L, v.longValue());
            return "content";
        }
    }

    @Path("/float/wrapper/{arg}")
    public static class ResourceUriFloatWrapper {

        @GET
        public String doGet(@PathParam("arg") Float v) {
            assertEquals(3.14159265f, v, 0f);
            return "content";
        }
    }

    @Path("/double/wrapper/{arg}")
    public static class ResourceUriDoubleWrapper {

        @GET
        public String doGet(@PathParam("arg") Double v) {
            assertEquals(3.14159265358979d, v, 0d);
            return "content";
        }
    }

    void _test(String type, String value) throws Exception {
        app.apply(RequestContextBuilder.from("/" + type + "/" + value, "GET").build()).get().getEntity();
        app.apply(RequestContextBuilder.from("/" + type + "/wrapper/" + value, "GET").build()).get().getEntity();
    }

    @Test
    public void testGetBoolean() throws Exception {
        _test("boolean", "true");
    }

    @Test
    public void testGetByte() throws Exception {
        _test("byte", "127");
    }

    @Test
    public void testGetCharacter() throws Exception {
        _test("char", "c");
    }

    @Test
    public void testGetShort() throws Exception {
        _test("short", "32767");
    }

    @Test
    public void testGetInt() throws Exception {
        _test("int", "2147483647");
    }

    @Test
    public void testGetLong() throws Exception {
        _test("long", "9223372036854775807");
    }

    @Test
    public void testGetFloat() throws Exception {
        _test("float", "3.14159265");
    }

    @Test
    public void testGetDouble() throws Exception {
        _test("double", "3.14159265358979");
    }

    @Test
    public void testBadPrimitiveValue() throws Exception {
        ContainerResponse responseContext = app.apply(RequestContextBuilder.from("/int/abcdef", "GET").build()).get();
        assertEquals(404, responseContext.getStatus());
    }

    @Test
    public void testBadPrimitiveWrapperValue() throws Exception {
        ContainerResponse responseContext = app.apply(RequestContextBuilder.from("/int/wrapper/abcdef", "GET").build()).get();
        assertEquals(404, responseContext.getStatus());
    }
}
