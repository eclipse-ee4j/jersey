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

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

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
public class PathParamAsStringTest extends AbstractTest {

    @Path("/{arg1}/{arg2}/{arg3}")
    public static class Resource {

        @GET
        public String doGet(@PathParam("arg1") String arg1,
                            @PathParam("arg2") String arg2, @PathParam("arg3") String arg3) {
            assertEquals("a", arg1);
            assertEquals("b", arg2);
            assertEquals("c", arg3);
            return "content";
        }

        @POST
        public String doPost(@PathParam("arg1") String arg1,
                             @PathParam("arg2") String arg2, @PathParam("arg3") String arg3,
                             String r) {
            assertEquals("a", arg1);
            assertEquals("b", arg2);
            assertEquals("c", arg3);
            assertEquals("content", r);
            return "content";
        }
    }

    @Test
    public void testStringArgsGet() throws ExecutionException, InterruptedException {
        initiateWebApplication(Resource.class);

        _test("/a/b/c");
    }

    @Test
    public void testStringArgsPost() throws ExecutionException, InterruptedException {
        initiateWebApplication(Resource.class);

        final ContainerResponse response = apply(
                RequestContextBuilder.from("/a/b/c", "POST")
                        .entity("content")
                        .build()
        );

        assertEquals("content", response.getEntity());
    }

    @Path("/{id}")
    public static class Duplicate {

        @GET
        public String get(@PathParam("id") String id) {
            return id;
        }

        @GET
        @Path("/{id}")
        public String getSub(@PathParam("id") String id) {
            return id;
        }
    }

    @Test
    public void testDuplicate() throws ExecutionException, InterruptedException {
        initiateWebApplication(Duplicate.class);

        assertEquals("foo", getResponseContext("/foo").getEntity());
        assertEquals("bar", getResponseContext("/foo/bar").getEntity());
    }

    @Path("/{id}")
    public static class DuplicateList {

        @GET
        public String get(@PathParam("id") String id) {
            return id;
        }

        @GET
        @Path("/{id}")
        public String getSub(@PathParam("id") List<String> id) {
            assertEquals(2, id.size());
            return id.get(0) + id.get(1);
        }
    }

    @Test
    public void testDuplicateList() throws ExecutionException, InterruptedException {
        initiateWebApplication(DuplicateList.class);

        assertEquals("foo", getResponseContext("/foo").getEntity());
        assertEquals("barfoo", getResponseContext("/foo/bar").getEntity());
    }
}
