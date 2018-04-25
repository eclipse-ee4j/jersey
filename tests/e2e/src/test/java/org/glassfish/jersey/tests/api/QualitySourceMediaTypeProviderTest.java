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

import org.glassfish.jersey.message.internal.HttpHeaderReader;
import org.glassfish.jersey.message.internal.QualitySourceMediaType;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author unknown
 */
public class QualitySourceMediaTypeProviderTest {

    @Test
    public void testOneMediaType() throws Exception {
        final String header = "application/xml";
        final List<QualitySourceMediaType> l = HttpHeaderReader.readQualitySourceMediaType(header);
        assertEquals(1, l.size());
        final MediaType m = l.get(0);
        assertEquals("application", m.getType());
        assertEquals("xml", m.getSubtype());
        assertEquals(0, m.getParameters().size());
    }

    @Test
    public void testOneMediaTypeWithParameters() throws Exception {
        final String header = "application/xml;charset=utf8";
        final List<QualitySourceMediaType> l = HttpHeaderReader.readQualitySourceMediaType(header);

        assertEquals(1, l.size());

        final MediaType m = l.get(0);
        assertEquals("application", m.getType());
        assertEquals("xml", m.getSubtype());
        assertEquals(1, m.getParameters().size());
        assertTrue(m.getParameters().containsKey("charset"));
        assertEquals("utf8", m.getParameters().get("charset"));
    }

    @Test
    public void testMultipleMediaType() throws Exception {
        final String header = "application/xml, text/xml, text/html";
        final List<QualitySourceMediaType> l = HttpHeaderReader.readQualitySourceMediaType(header);

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
        final String header = "application/xml;qs=0.1, text/xml;qs=0.2, text/html;qs=0.3";
        final List<QualitySourceMediaType> l = HttpHeaderReader.readQualitySourceMediaType(header);

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
    public void testMultipleMediaTypeWithQuality2() throws Exception {
        final String header = "application/xml;qs=0.1, text/xml;qs=0.2, text/html;qs=0.93";
        final List<QualitySourceMediaType> l = HttpHeaderReader.readQualitySourceMediaType(header);

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
        final String header = "text/html, image/gif, image/jpeg, */*; qs=.2";
        final List<QualitySourceMediaType> l = HttpHeaderReader.readQualitySourceMediaType(header);

        assertEquals(4, l.size());

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
    }

    @Test
    public void testFirefoxAcceptHeader() throws Exception {
        final String header = "text/xml,application/xml,application/xhtml+xml,text/html;qs=0.9,text/plain;qs=0.8,image/png,*/*;"
                + "qs=0.5";
        final List<QualitySourceMediaType> l = HttpHeaderReader.readQualitySourceMediaType(header);

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
    public void testMediaTypeSpecifity() throws Exception {
        final String header = "*/*, text/*, text/plain";
        final List<QualitySourceMediaType> l = HttpHeaderReader.readQualitySourceMediaType(header);

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
        final String header = "*/*, */*;qs=0.5, text/*, text/*;qs=0.5, text/plain, text/plain;qs=0.5";
        final List<QualitySourceMediaType> l = HttpHeaderReader.readQualitySourceMediaType(header);

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
        final String header = "text/*, text/html, text/html;level=1, */*";
        final List<QualitySourceMediaType> l = HttpHeaderReader.readQualitySourceMediaType(header);

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
        final String header = "text/*, text/html;level=1, text/html, */*";
        final List<QualitySourceMediaType> l = HttpHeaderReader.readQualitySourceMediaType(header);

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
}
