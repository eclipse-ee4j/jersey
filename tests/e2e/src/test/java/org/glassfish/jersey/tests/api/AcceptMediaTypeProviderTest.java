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

import java.util.List;

import javax.ws.rs.core.MediaType;
import org.glassfish.jersey.message.internal.AcceptableMediaType;
import org.glassfish.jersey.message.internal.HttpHeaderReader;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author unknown
 */
public class AcceptMediaTypeProviderTest {

    @Test
    public void testOneMediaType() throws Exception {
        String header = "application/xml";
        List<AcceptableMediaType> l = HttpHeaderReader.readAcceptMediaType(header);

        assertEquals(1, l.size());

        MediaType m = l.get(0);
        assertEquals("application", m.getType());
        assertEquals("xml", m.getSubtype());
        assertEquals(0, m.getParameters().size());
    }

    @Test
    public void testOneMediaTypeWithParameters() throws Exception {
        String header = "application/xml;charset=utf8";
        List<AcceptableMediaType> l = HttpHeaderReader.readAcceptMediaType(header);

        assertEquals(1, l.size());

        MediaType m = l.get(0);
        assertEquals("application", m.getType());
        assertEquals("xml", m.getSubtype());
        assertEquals(1, m.getParameters().size());
        assertTrue(m.getParameters().containsKey("charset"));
        assertEquals("utf8", m.getParameters().get("charset"));
    }

    @Test
    public void testMultipleMediaType() throws Exception {
        String header = "application/xml, text/xml, text/html";
        List<AcceptableMediaType> l = HttpHeaderReader.readAcceptMediaType(header);

        assertEquals(3, l.size());

        MediaType m;
        m = l.get(0);
        assertEquals("application", m.getType());
        assertEquals("xml", m.getSubtype());
        assertEquals(0, m.getParameters().size());
        m = l.get(1);
        assertEquals("text", m.getType());
        assertEquals("xml", m.getSubtype());
        assertEquals(0, m.getParameters().size());
        m = l.get(2);
        assertEquals("text", m.getType());
        assertEquals("html", m.getSubtype());
        assertEquals(0, m.getParameters().size());
    }

    @Test
    public void testMultipleMediaTypeWithQuality() throws Exception {
        String header = "application/xml;q=0.1, text/xml;q=0.2, text/html;q=0.3";
        List<AcceptableMediaType> l = HttpHeaderReader.readAcceptMediaType(header);

        assertEquals(3, l.size());

        MediaType m;
        m = l.get(0);
        assertEquals("text", m.getType());
        assertEquals("html", m.getSubtype());
        assertEquals(1, m.getParameters().size());
        m = l.get(1);
        assertEquals("text", m.getType());
        assertEquals("xml", m.getSubtype());
        assertEquals(1, m.getParameters().size());
        m = l.get(2);
        assertEquals("application", m.getType());
        assertEquals("xml", m.getSubtype());
        assertEquals(1, m.getParameters().size());
    }

    @Test
    public void testHttpURLConnectionAcceptHeader() throws Exception {
        String header = "text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2";
        List<AcceptableMediaType> l = HttpHeaderReader.readAcceptMediaType(header);

        assertEquals(5, l.size());

        MediaType m;
        m = l.get(0);
        assertEquals("text", m.getType());
        assertEquals("html", m.getSubtype());
        assertEquals(0, m.getParameters().size());
        m = l.get(1);
        assertEquals("image", m.getType());
        assertEquals("gif", m.getSubtype());
        assertEquals(0, m.getParameters().size());
        m = l.get(2);
        assertEquals("image", m.getType());
        assertEquals("jpeg", m.getSubtype());
        assertEquals(0, m.getParameters().size());
        m = l.get(3);
        assertEquals("*", m.getType());
        assertEquals("*", m.getSubtype());
        assertEquals(1, m.getParameters().size());
        m = l.get(4);
        assertEquals("*", m.getType());
        assertEquals("*", m.getSubtype());
        assertEquals(1, m.getParameters().size());
    }

    @Test
    public void testFirefoxAcceptHeader() throws Exception {
        String header = "text/xml,application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5";
        List<AcceptableMediaType> l = HttpHeaderReader.readAcceptMediaType(header);

        assertEquals(7, l.size());

        MediaType m;
        m = l.get(0);
        assertEquals("text", m.getType());
        assertEquals("xml", m.getSubtype());
        assertEquals(0, m.getParameters().size());
        m = l.get(1);
        assertEquals("application", m.getType());
        assertEquals("xml", m.getSubtype());
        assertEquals(0, m.getParameters().size());
        m = l.get(2);
        assertEquals("application", m.getType());
        assertEquals("xhtml+xml", m.getSubtype());
        assertEquals(0, m.getParameters().size());
        m = l.get(3);
        assertEquals("image", m.getType());
        assertEquals("png", m.getSubtype());
        assertEquals(0, m.getParameters().size());
        m = l.get(4);
        assertEquals("text", m.getType());
        assertEquals("html", m.getSubtype());
        assertEquals(1, m.getParameters().size());
        m = l.get(5);
        assertEquals("text", m.getType());
        assertEquals("plain", m.getSubtype());
        assertEquals(1, m.getParameters().size());
        m = l.get(6);
        assertEquals("*", m.getType());
        assertEquals("*", m.getSubtype());
        assertEquals(1, m.getParameters().size());
    }

    @Test
    public void testWithStarAcceptHeader() throws Exception {
        String header = "application/xml;q=0.1, text/xml;q=0.2, *;q=0.3";
        List<AcceptableMediaType> l = HttpHeaderReader.readAcceptMediaType(header);

        assertEquals(3, l.size());

        MediaType m;
        m = l.get(0);
        assertEquals("*", m.getType());
        assertEquals("*", m.getSubtype());
        assertEquals(1, m.getParameters().size());
        m = l.get(1);
        assertEquals("text", m.getType());
        assertEquals("xml", m.getSubtype());
        assertEquals(1, m.getParameters().size());
        m = l.get(2);
        assertEquals("application", m.getType());
        assertEquals("xml", m.getSubtype());
        assertEquals(1, m.getParameters().size());
    }

    @Test
    public void testMediaTypeSpecifity() throws Exception {
        String header = "*/*, text/*, text/plain";
        List<AcceptableMediaType> l = HttpHeaderReader.readAcceptMediaType(header);

        assertEquals(3, l.size());

        MediaType m;
        m = l.get(0);
        assertEquals("text", m.getType());
        assertEquals("plain", m.getSubtype());
        assertEquals(0, m.getParameters().size());
        m = l.get(1);
        assertEquals("text", m.getType());
        assertEquals("*", m.getSubtype());
        assertEquals(0, m.getParameters().size());
        m = l.get(2);
        assertEquals("*", m.getType());
        assertEquals("*", m.getSubtype());
        assertEquals(0, m.getParameters().size());
    }

    @Test
    public void testMediaTypeSpecifityWithQuality() throws Exception {
        String header = "*/*, */*;q=0.5, text/*, text/*;q=0.5, text/plain, text/plain;q=0.5";
        List<AcceptableMediaType> l = HttpHeaderReader.readAcceptMediaType(header);

        assertEquals(6, l.size());

        MediaType m;
        m = l.get(0);
        assertEquals("text", m.getType());
        assertEquals("plain", m.getSubtype());
        assertEquals(0, m.getParameters().size());
        m = l.get(1);
        assertEquals("text", m.getType());
        assertEquals("*", m.getSubtype());
        assertEquals(0, m.getParameters().size());
        m = l.get(2);
        assertEquals("*", m.getType());
        assertEquals("*", m.getSubtype());
        assertEquals(0, m.getParameters().size());
        m = l.get(3);
        assertEquals("text", m.getType());
        assertEquals("plain", m.getSubtype());
        assertEquals(1, m.getParameters().size());
        m = l.get(4);
        assertEquals("text", m.getType());
        assertEquals("*", m.getSubtype());
        assertEquals(1, m.getParameters().size());
        m = l.get(5);
        assertEquals("*", m.getType());
        assertEquals("*", m.getSubtype());
        assertEquals(1, m.getParameters().size());
    }

    @Test
    public void testMediaTypeSpecifityHTTPExample1() throws Exception {
        String header = "text/*, text/html, text/html;level=1, */*";
        List<AcceptableMediaType> l = HttpHeaderReader.readAcceptMediaType(header);

        MediaType m;
        m = l.get(0);
        assertEquals("text", m.getType());
        assertEquals("html", m.getSubtype());
        assertEquals(0, m.getParameters().size());
        m = l.get(1);
        assertEquals("text", m.getType());
        assertEquals("html", m.getSubtype());
        assertEquals(1, m.getParameters().size());
        m = l.get(2);
        assertEquals("text", m.getType());
        assertEquals("*", m.getSubtype());
        assertEquals(0, m.getParameters().size());
        m = l.get(3);
        assertEquals("*", m.getType());
        assertEquals("*", m.getSubtype());
        assertEquals(0, m.getParameters().size());
    }

    @Test
    public void testMediaTypeSpecifityHTTPExample2() throws Exception {
        String header = "text/*, text/html;level=1, text/html, */*";
        List<AcceptableMediaType> l = HttpHeaderReader.readAcceptMediaType(header);

        MediaType m;
        m = l.get(0);
        assertEquals("text", m.getType());
        assertEquals("html", m.getSubtype());
        assertEquals(1, m.getParameters().size());
        m = l.get(1);
        assertEquals("text", m.getType());
        assertEquals("html", m.getSubtype());
        assertEquals(0, m.getParameters().size());
        m = l.get(2);
        assertEquals("text", m.getType());
        assertEquals("*", m.getSubtype());
        assertEquals(0, m.getParameters().size());
        m = l.get(3);
        assertEquals("*", m.getType());
        assertEquals("*", m.getSubtype());
        assertEquals(0, m.getParameters().size());
    }

    @Test
    public void testHttpURLConnectionAcceptHeaderWithPrority() throws Exception {
        String header = "text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2";

        List<AcceptableMediaType> l = HttpHeaderReader.readAcceptMediaType(header,
                HttpHeaderReader.readQualitySourceMediaType(MediaType.TEXT_HTML));

        assertEquals(5, l.size());

        MediaType m;
        m = l.get(0);
        assertEquals("text", m.getType());
        assertEquals("html", m.getSubtype());
        assertEquals(0, m.getParameters().size());
        m = l.get(1);
        assertEquals("image", m.getType());
        assertEquals("gif", m.getSubtype());
        assertEquals(0, m.getParameters().size());
        m = l.get(2);
        assertEquals("image", m.getType());
        assertEquals("jpeg", m.getSubtype());
        assertEquals(0, m.getParameters().size());
        m = l.get(3);
        assertEquals("*", m.getType());
        assertEquals("*", m.getSubtype());
        assertEquals(1, m.getParameters().size());
        m = l.get(4);
        assertEquals("*", m.getType());
        assertEquals("*", m.getSubtype());
        assertEquals(1, m.getParameters().size());
    }

    @Test
    public void testFirefoxAcceptHeaderWithPrority() throws Exception {
        String header = "text/xml,application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5";
        List<AcceptableMediaType> l = HttpHeaderReader.readAcceptMediaType(
                header, HttpHeaderReader.readQualitySourceMediaType("text/html;qs=1"));

        assertEquals(7, l.size());

        MediaType m;
        m = l.get(0);
        assertEquals("text", m.getType());
        assertEquals("html", m.getSubtype());
        assertEquals(1, m.getParameters().size());
        m = l.get(1);
        assertEquals("text", m.getType());
        assertEquals("xml", m.getSubtype());
        assertEquals(0, m.getParameters().size());
        m = l.get(2);
        assertEquals("application", m.getType());
        assertEquals("xml", m.getSubtype());
        assertEquals(0, m.getParameters().size());
        m = l.get(3);
        assertEquals("application", m.getType());
        assertEquals("xhtml+xml", m.getSubtype());
        assertEquals(0, m.getParameters().size());
        m = l.get(4);
        assertEquals("image", m.getType());
        assertEquals("png", m.getSubtype());
        assertEquals(0, m.getParameters().size());
        m = l.get(5);
        assertEquals("text", m.getType());
        assertEquals("plain", m.getSubtype());
        assertEquals(1, m.getParameters().size());
        m = l.get(6);
        assertEquals("*", m.getType());
        assertEquals("*", m.getSubtype());
        assertEquals(1, m.getParameters().size());
    }

    @Test
    public void testFirefoxAcceptHeaderWithPrority2() throws Exception {
        String header = "text/xml,application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5";
        List<AcceptableMediaType> l = HttpHeaderReader.readAcceptMediaType(
                header,
                HttpHeaderReader.readQualitySourceMediaType(new String[]{"text/html;qs=0.8", "application/xml;qs=0.1"}));

        assertEquals(7, l.size());

        MediaType m;
        m = l.get(0);
        assertEquals("text", m.getType());
        assertEquals("html", m.getSubtype());
        assertEquals(1, m.getParameters().size());
        m = l.get(1);
        assertEquals("application", m.getType());
        assertEquals("xml", m.getSubtype());
        assertEquals(0, m.getParameters().size());
        m = l.get(2);
        assertEquals("text", m.getType());
        assertEquals("xml", m.getSubtype());
        assertEquals(0, m.getParameters().size());
        m = l.get(3);
        assertEquals("application", m.getType());
        assertEquals("xhtml+xml", m.getSubtype());
        assertEquals(0, m.getParameters().size());
        m = l.get(4);
        assertEquals("image", m.getType());
        assertEquals("png", m.getSubtype());
        assertEquals(0, m.getParameters().size());
        m = l.get(5);
        assertEquals("text", m.getType());
        assertEquals("plain", m.getSubtype());
        assertEquals(1, m.getParameters().size());
        m = l.get(6);
        assertEquals("*", m.getType());
        assertEquals("*", m.getSubtype());
        assertEquals(1, m.getParameters().size());
    }
}
