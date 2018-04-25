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

package org.glassfish.jersey.tests.e2e.server.wadl;

import java.io.IOException;
import java.io.StringReader;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import static org.junit.Assert.assertEquals;

/**
 * Tests, that Jersey returns wildcard mediaType in case no response representation was specified.
 *
 * @author Adam Lindenthal (adam.lindenthal at oracle.com)
 */
public class WadlEmptyMediaTypeTest extends JerseyTest {

    @Path("test")
    public static class WadlEmptyMediaTypeTestResource {
        @Path("getEmpty")
        @GET
        public String getEmpty() {
            return "No @Produces annotation";
        }

        @Path("getText")
        @Produces("text/plain")
        @GET
        public String getText() {
            return "Produces text/plain";
        }
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(WadlEmptyMediaTypeTestResource.class);
    }

    @Test
    public void testOverride() throws ParserConfigurationException, IOException, SAXException, XPathExpressionException {
        WebTarget target = target("/application.wadl");
        String wadl = target.request().get(String.class);

        InputSource is = new InputSource(new StringReader(wadl));
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);

        XPath xpath = XPathFactory.newInstance().newXPath();
        String val = xpath.evaluate("//method[@id='getEmpty']/response/representation/@mediaType", document);
        assertEquals("*/*", val);

        val = xpath.evaluate("//method[@id='getText']/response/representation/@mediaType", document);
        assertEquals("text/plain", val);
    }
}
