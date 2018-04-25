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

import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.ws.rs.CookieParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;

import org.glassfish.jersey.server.RequestContextBuilder;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 *
 * @author Paul Sandoz
 * @author Pavel Bucek (pavel.bucek at oracle.com)
 */
public class CookieParamAsCookieTest extends AbstractTest {


    @Path("/")
    public static class CookieTypeResource {
        @POST
        public String post(
                @Context HttpHeaders h,
                @CookieParam("one") Cookie one,
                @CookieParam("two") Cookie two,
                @CookieParam("three") Cookie three) {
            assertEquals("one", one.getName());
            assertEquals("value_one", one.getValue());

            assertEquals("two", two.getName());
            assertEquals("value_two", two.getValue());

            assertEquals(null, three);

            Map<String, Cookie> cs = h.getCookies();
            assertEquals(2, cs.size());
            assertEquals("value_one", cs.get("one").getValue());
            assertEquals("value_two", cs.get("two").getValue());

            return "content";
        }
    }

    @Test
    public void testCookieParam() throws ExecutionException, InterruptedException {
        initiateWebApplication(CookieTypeResource.class);

        Cookie one = new Cookie("one", "value_one");
        Cookie two = new Cookie("two", "value_two");

        assertEquals("content", apply(RequestContextBuilder.from("/", "POST").cookie(one).cookie(two).build()).getEntity());
    }
}
