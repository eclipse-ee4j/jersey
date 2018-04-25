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

import java.util.HashMap;

import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.message.internal.MediaTypeProvider;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Media type provider tests.
 *
 * @author Mark Hadley
 * @author Adam Lindenthal (adam.lindenthal at oracle.com)
 */
public class MediaTypeProviderTest {
    @Test
    public void testToString() {
        final MediaType header = new MediaType("application", "xml");
        final MediaTypeProvider instance = new MediaTypeProvider();

        final String expResult = "application/xml";
        final String result = instance.toString(header);
        assertEquals(expResult, result);
    }

    @Test
    public void testToStringWithParams() {
        final HashMap<String, String> params = new HashMap<>();
        params.put("charset", "utf8");
        final MediaType header = new MediaType("application", "xml", params);
        final MediaTypeProvider instance = new MediaTypeProvider();

        final String expResult = "application/xml;charset=utf8";
        final String result = instance.toString(header);
        assertEquals(expResult, result);
    }

    @Test
    public void testFromString() throws Exception {
        final MediaTypeProvider instance = new MediaTypeProvider();

        final String header = "application/xml";
        final MediaType result = instance.fromString(header);
        assertEquals(result.getType(), "application");
        assertEquals(result.getSubtype(), "xml");
        assertEquals(result.getParameters().size(), 0);
    }

    @Test
    public void testFromStringWithParams() throws Exception {
        final String header = "application/xml;charset=utf8";
        final MediaTypeProvider instance = new MediaTypeProvider();

        final MediaType result = instance.fromString(header);
        assertEquals(result.getType(), "application");
        assertEquals(result.getSubtype(), "xml");
        assertEquals(result.getParameters().size(), 1);
        assertTrue(result.getParameters().containsKey("charset"));
        assertEquals(result.getParameters().get("charset"), "utf8");
    }

    @Test
    public void testWithQuotedParam() {
        final HashMap<String, String> params = new HashMap<String, String>();
        params.put("foo", "\"bar\"");
        final MediaType header = new MediaType("application", "xml", params);
        final MediaTypeProvider instance = new MediaTypeProvider();

        final String result = instance.toString(header);
        final String expResult = "application/xml;foo=\"\\\"bar\\\"\"";
        assertEquals(expResult, result);

        final MediaType m = instance.fromString(result);
        assertEquals("\"bar\"", m.getParameters().get("foo"));
    }
}
