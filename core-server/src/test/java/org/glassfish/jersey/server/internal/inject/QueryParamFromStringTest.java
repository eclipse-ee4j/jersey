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
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 *
 * @author Paul Sandoz
 * @author Pavel Bucek (pavel.bucek at oracle.com)
 */
public class QueryParamFromStringTest extends AbstractTest {

    public static class Parameter {
        public String s;

        private Parameter(CharSequence cs) {
            this.s = cs.toString();
        }

        public static Parameter fromString(String s) {
            return new Parameter(s + "fromString");
        }

        @Override
        public String toString() {
            return s;
        }
    }

    @Path("/")
    public static class ResourceString {
        @GET
        public String doGet(
                @QueryParam("arg1") Parameter p) {
            assertEquals("3.145fromString", p.s);
            return "content";
        }

        @GET
        @Path("list")
        public String doGet(@QueryParam("arg") List<Parameter> p) {
            System.err.println(p.toString());
            assertEquals("[afromString, fromString, bfromString]", p.toString());
            return "content";
        }

        @GET
        @Path("empty")
        public String doGetEmpty(
                @QueryParam("arg1") Parameter p) {
            assertEquals("fromString", p.s);
            return "content";
        }
    }

    @Test
    public void testFromStringGet() throws ExecutionException, InterruptedException {
        initiateWebApplication(ResourceString.class);

        _test("/?arg1=3.145");
    }

    @Test
    public void testListFromStringGet() throws ExecutionException, InterruptedException {
        initiateWebApplication(ResourceString.class);

        _test("/list/?arg=a&arg=&arg=b");
    }

    @Test
    public void testFromEmptyStringGet() throws ExecutionException, InterruptedException {
        initiateWebApplication(ResourceString.class);

        _test("/empty/?arg1=");
    }
}
