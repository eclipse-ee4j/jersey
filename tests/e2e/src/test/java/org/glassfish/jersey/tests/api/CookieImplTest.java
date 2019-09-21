/*
 * Copyright (c) 2014, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.api;

import java.util.Map;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.NewCookie;

import org.glassfish.jersey.message.internal.HttpHeaderReader;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Marc Hadley
 */
public class CookieImplTest {

    @Test
    public void testCookieToString() {
        Cookie cookie = new Cookie("fred", "flintstone");
        String expResult = "$Version=1;fred=flintstone";
        assertEquals(expResult, cookie.toString());

        cookie = new Cookie("fred", "flintstone", "/path", null);
        expResult = "$Version=1;fred=flintstone;$Path=/path";
        assertEquals(expResult, cookie.toString());

        cookie = new Cookie("fred", "flintstone", "/path", ".sun.com");
        expResult = "$Version=1;fred=flintstone;$Domain=.sun.com;$Path=/path";
        assertEquals(expResult, cookie.toString());

        cookie = new Cookie("fred", "flintstone", "/path", ".sun.com", 2);
        expResult = "$Version=2;fred=flintstone;$Domain=.sun.com;$Path=/path";
        assertEquals(expResult, cookie.toString());
    }

    @Test
    public void testCookieValueOf() {
        Cookie cookie = Cookie.valueOf("$Version=2;fred=flintstone");
        assertEquals("fred", cookie.getName());
        assertEquals("flintstone", cookie.getValue());
        assertEquals(2, cookie.getVersion());

        cookie = Cookie.valueOf("$Version=1;fred=flintstone;$Path=/path");
        assertEquals("fred", cookie.getName());
        assertEquals("flintstone", cookie.getValue());
        assertEquals(1, cookie.getVersion());
        assertEquals("/path", cookie.getPath());

        cookie = Cookie.valueOf("$Version=1;fred=flintstone;$Domain=.sun.com;$Path=/path");
        assertEquals("fred", cookie.getName());
        assertEquals("flintstone", cookie.getValue());
        assertEquals(1, cookie.getVersion());
        assertEquals(".sun.com", cookie.getDomain());
        assertEquals("/path", cookie.getPath());
    }

    @Test
    public void testCreateCookies() {
        String cookieHeader = "fred=flintstone";
        Map<String, Cookie> cookies = HttpHeaderReader.readCookies(cookieHeader);
        assertEquals(cookies.size(), 1);
        Cookie c = cookies.get("fred");
        assertEquals(c.getVersion(), 0);
        assertTrue("fred".equals(c.getName()));
        assertTrue("flintstone".equals(c.getValue()));

        cookieHeader = "fred=flintstone,barney=rubble";
        cookies = HttpHeaderReader.readCookies(cookieHeader);
        assertEquals(cookies.size(), 2);
        c = cookies.get("fred");
        assertEquals(c.getVersion(), 0);
        assertTrue("fred".equals(c.getName()));
        assertTrue("flintstone".equals(c.getValue()));
        c = cookies.get("barney");
        assertEquals(c.getVersion(), 0);
        assertTrue("barney".equals(c.getName()));
        assertTrue("rubble".equals(c.getValue()));

        cookieHeader = "fred=flintstone;barney=rubble";
        cookies = HttpHeaderReader.readCookies(cookieHeader);
        assertEquals(cookies.size(), 2);
        c = cookies.get("fred");
        assertEquals(c.getVersion(), 0);
        assertTrue("fred".equals(c.getName()));
        assertTrue("flintstone".equals(c.getValue()));
        c = cookies.get("barney");
        assertEquals(c.getVersion(), 0);
        assertTrue("barney".equals(c.getName()));
        assertTrue("rubble".equals(c.getValue()));

        cookieHeader = "$Version=1;fred=flintstone;$Path=/path;barney=rubble";
        cookies = HttpHeaderReader.readCookies(cookieHeader);
        assertEquals(cookies.size(), 2);
        c = cookies.get("fred");
        assertEquals(c.getVersion(), 1);
        assertTrue("fred".equals(c.getName()));
        assertTrue("flintstone".equals(c.getValue()));
        assertTrue("/path".equals(c.getPath()));
        c = cookies.get("barney");
        assertEquals(c.getVersion(), 1);
        assertTrue("barney".equals(c.getName()));
        assertTrue("rubble".equals(c.getValue()));

        cookieHeader = "$Version=1;fred=flintstone;$Path=/path,barney=rubble;$Domain=.sun.com";
        cookies = HttpHeaderReader.readCookies(cookieHeader);
        assertEquals(cookies.size(), 2);
        c = cookies.get("fred");
        assertEquals(c.getVersion(), 1);
        assertTrue("fred".equals(c.getName()));
        assertTrue("flintstone".equals(c.getValue()));
        assertTrue("/path".equals(c.getPath()));
        c = cookies.get("barney");
        assertEquals(c.getVersion(), 1);
        assertTrue("barney".equals(c.getName()));
        assertTrue("rubble".equals(c.getValue()));
        assertTrue(".sun.com".equals(c.getDomain()));

        cookieHeader = "$Version=1; fred = flintstone ; $Path=/path, barney=rubble ;$Domain=.sun.com";
        cookies = HttpHeaderReader.readCookies(cookieHeader);
        assertEquals(cookies.size(), 2);
        c = cookies.get("fred");
        assertEquals(c.getVersion(), 1);
        assertTrue("fred".equals(c.getName()));
        assertTrue("flintstone".equals(c.getValue()));
        assertTrue("/path".equals(c.getPath()));
        c = cookies.get("barney");
        assertEquals(c.getVersion(), 1);
        assertTrue("barney".equals(c.getName()));
        assertTrue("rubble".equals(c.getValue()));
        assertTrue(".sun.com".equals(c.getDomain()));
    }

    @Test
    public void testNewCookieToString() {
        NewCookie cookie = new NewCookie("fred", "flintstone");
        String expResult = "fred=flintstone;Version=1";
        assertEquals(expResult, cookie.toString());

        cookie = new NewCookie("fred", "flintstone", null, null, null, 60, false);
        expResult = "fred=flintstone;Version=1;Max-Age=60";
        assertEquals(expResult, cookie.toString());

        cookie = new NewCookie("fred", "flintstone", null, null, "a modern stonage family", 60, false);
        expResult = "fred=flintstone;Version=1;Comment=\"a modern stonage family\";Max-Age=60";
        assertEquals(expResult, cookie.toString());
    }

    @Test
    public void testNewCookieValueOf() {
        NewCookie cookie = NewCookie.valueOf("fred=flintstone;Version=2");
        assertEquals("fred", cookie.getName());
        assertEquals("flintstone", cookie.getValue());
        assertEquals(2, cookie.getVersion());

        cookie = NewCookie.valueOf("fred=flintstone;Version=1;Max-Age=60");
        assertEquals("fred", cookie.getName());
        assertEquals("flintstone", cookie.getValue());
        assertEquals(1, cookie.getVersion());
        assertEquals(60, cookie.getMaxAge());

        cookie = NewCookie.valueOf("fred=flintstone;Version=1;Comment=\"a modern stonage family\";Max-Age=60;Secure");
        assertEquals("fred", cookie.getName());
        assertEquals("flintstone", cookie.getValue());
        assertEquals("a modern stonage family", cookie.getComment());
        assertEquals(1, cookie.getVersion());
        assertEquals(60, cookie.getMaxAge());
        assertTrue(cookie.isSecure());
    }

}
