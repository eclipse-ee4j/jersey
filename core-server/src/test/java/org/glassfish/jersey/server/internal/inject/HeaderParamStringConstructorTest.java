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
import java.math.BigInteger;
import java.net.URI;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;

import org.glassfish.jersey.server.ContainerResponse;
import org.glassfish.jersey.server.RequestContextBuilder;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 *
 * @author Paul Sandoz
 * @author Pavel Bucek (pavel.bucek at oracle.com)
 */
public class HeaderParamStringConstructorTest extends AbstractTest {

    @Path("/")
    public static class ResourceString {

        @GET
        public String doGet(
                @HeaderParam("arg1") BigDecimal arg1,
                @HeaderParam("arg2") BigInteger arg2,
                @HeaderParam("arg3") URI arg3) {
            assertEquals("3.145", arg1.toString());
            assertEquals("3145", arg2.toString());
            assertEquals("http://test", arg3.toString());
            return "content";
        }
    }

    @Path("/")
    public static class ResourceStringList {

        @GET
        public String doGetString(@HeaderParam("args") List<BigDecimal> args) {
            assertEquals("3.145", args.get(0).toString());
            assertEquals("2.718", args.get(1).toString());
            assertEquals("1.618", args.get(2).toString());
            return "content";
        }
    }

    @Path("/")
    public static class ResourceStringListEmpty {

        @GET
        public String doGetString(@HeaderParam("args") List<BigDecimal> args) {
            assertEquals(3, args.size());
            assertEquals(null, args.get(0));
            assertEquals(null, args.get(1));
            assertEquals(null, args.get(2));
            return "content";
        }
    }

    @Path("/")
    public static class ResourceStringNullDefault {

        @GET
        public String doGet(
                @HeaderParam("arg1") BigDecimal arg1) {
            assertEquals(null, arg1);
            return "content";
        }
    }

    @Path("/")
    public static class ResourceStringDefault {

        @GET
        public String doGet(
                @HeaderParam("arg1") @DefaultValue("3.145") BigDecimal arg1) {
            assertEquals("3.145", arg1.toString());
            return "content";
        }
    }

    @Path("/")
    public static class ResourceStringDefaultOverride {

        @GET
        public String doGet(
                @HeaderParam("arg1") @DefaultValue("3.145") BigDecimal arg1) {
            assertEquals("2.718", arg1.toString());
            return "content";
        }
    }

    @Path("/")
    public static class ResourceStringListEmptyDefault {

        @GET
        public String doGetString(@HeaderParam("args") List<BigDecimal> args) {
            assertEquals(0, args.size());
            return "content";
        }
    }

    @Path("/")
    public static class ResourceStringListDefault {

        @GET
        public String doGetString(
                @HeaderParam("args") @DefaultValue("3.145") List<BigDecimal> args) {
            assertEquals("3.145", args.get(0).toString());
            return "content";
        }
    }

    @Path("/")
    public static class ResourceStringListDefaultOverride {

        @GET
        public String doGetString(
                @HeaderParam("args") @DefaultValue("3.145") List<BigDecimal> args) {
            assertEquals("2.718", args.get(0).toString());
            return "content";
        }
    }

    @Test
    public void testStringConstructorGet() throws ExecutionException, InterruptedException {
        initiateWebApplication(ResourceString.class);

        assertEquals("content", apply(
                RequestContextBuilder.from("/", "GET")
                        .header("arg1", "3.145")
                        .header("arg2", "3145")
                        .header("arg3", "http://test")
                        .build()
        ).getEntity());
    }

    @Test
    public void testStringConstructorListGet() throws ExecutionException, InterruptedException {
        initiateWebApplication(ResourceStringList.class);

        assertEquals("content", apply(
                RequestContextBuilder.from("/", "GET")
                        .accept("application/stringlist")
                        .header("args", "3.145")
                        .header("args", "2.718")
                        .header("args", "1.618")
                        .build()
        ).getEntity());
    }

    @Test
    public void testStringConstructorListEmptyGet() throws ExecutionException, InterruptedException {
        initiateWebApplication(ResourceStringListEmpty.class);

        assertEquals("content", apply(
                RequestContextBuilder.from("/", "GET")
                        .accept("application/stringlist")
                        .header("args", "")
                        .header("args", "")
                        .header("args", "")
                        .build()
        ).getEntity());
    }

    @Test
    public void testStringConstructorEmptyDefault() throws ExecutionException, InterruptedException {
        initiateWebApplication(ResourceStringNullDefault.class);

        _test("/");
    }

    @Test
    public void testStringConstructorDefault() throws ExecutionException, InterruptedException {
        initiateWebApplication(ResourceStringDefault.class);

        _test("/");
    }

    @Test
    public void testStringConstructorDefaultOverride() throws ExecutionException, InterruptedException {
        initiateWebApplication(ResourceStringDefault.class);

        assertEquals("content", apply(
                RequestContextBuilder.from("/", "GET")
                        .header("args", "2.718")
                        .build()
        ).getEntity());
    }

    @Test
    public void testStringConstructorListNullDefault() throws ExecutionException, InterruptedException {
        initiateWebApplication(ResourceStringListEmptyDefault.class);

        _test("/");
    }

    @Test
    public void testStringConstructorListDefault() throws ExecutionException, InterruptedException {
        initiateWebApplication(ResourceStringListDefault.class);

        _test("/");
    }

    @Test
    public void testStringConstructorListDefaultOverride() throws ExecutionException, InterruptedException {
        initiateWebApplication(ResourceStringListDefaultOverride.class);

        assertEquals("content", apply(
                RequestContextBuilder.from("/", "GET")
                        .header("args", "2.718")
                        .build()
        ).getEntity());
    }

    @Test
    public void testBadStringConstructorValue() throws ExecutionException, InterruptedException {
        initiateWebApplication(ResourceString.class);

        final ContainerResponse responseContext = apply(
                RequestContextBuilder.from("/", "GET")
                        .header("arg1", "ABCDEF")
                        .header("arg2", "3145")
                        .header("arg3", "http://test")
                        .build()
        );

        assertEquals(400, responseContext.getStatus());
    }
}
