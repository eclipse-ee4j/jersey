/*
 * Copyright (c) 2010, 2025 Oracle and/or its affiliates. All rights reserved.
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
import java.util.Map;
import java.util.concurrent.ExecutionException;

import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.HttpHeaders;

import org.glassfish.jersey.http.JerseyCookie;
import org.glassfish.jersey.server.RequestContextBuilder;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 *
 * @author Paul Sandoz
 * @author Pavel Bucek
 */
public class CookieParamAsCookieTest extends AbstractTest {

    private static final String ONE = "one";
    private static final String TWO = "two";
    private static final String VALUE_ONE = "value_one";
    private static final String VALUE_TWO = "value_two";


    @Path("/")
    public static class CookieTypeResource {
        @POST
        public String post(
                @Context HttpHeaders h,
                @CookieParam(ONE) Cookie one,
                @CookieParam(TWO) Cookie two,
                @CookieParam("three") Cookie three) {
            assertEquals(ONE, one.getName());
            assertEquals(VALUE_ONE, one.getValue());

            assertEquals(TWO, two.getName());
            assertEquals(VALUE_TWO, two.getValue());

            assertEquals(null, three);

            Map<String, Cookie> cs = h.getCookies();
            assertEquals(2, cs.size());
            assertEquals(VALUE_ONE, cs.get(ONE).getValue());
            assertEquals(VALUE_TWO, cs.get(TWO).getValue());

            return "content";
        }

        @Path("list")
        @GET
        public String list(@CookieParam(ONE) Cookie one,
                           @CookieParam(TWO) Cookie two,
                           @HeaderParam("Cookie") List<Cookie> cookies) {
            return cookies.get(0).toString() + ',' + cookies.get(1).toString();
        }

    }

    @Test
    public void testCookieParam() throws ExecutionException, InterruptedException {
        initiateWebApplication(CookieTypeResource.class);

        Cookie one = new Cookie(ONE, VALUE_ONE);
        Cookie two = new Cookie(TWO, VALUE_TWO);

        assertEquals("content", apply(RequestContextBuilder.from("/", "POST").cookie(one).cookie(two).build()).getEntity());
    }

    @Test
    public void testCookieList() throws ExecutionException, InterruptedException {
        initiateWebApplication(CookieTypeResource.class);

        Cookie one = new Cookie.Builder(ONE).version(-1).value(VALUE_ONE).build();
        Cookie two = new JerseyCookie.Builder(TWO).value(VALUE_TWO).build();
        Object response = apply(RequestContextBuilder.from("/list", HttpMethod.GET).cookie(one).cookie(two).build()).getEntity();
        Assertions.assertEquals(ONE + '=' + VALUE_ONE + ',' + TWO + '=' + VALUE_TWO, response);
    }
}
