/*
 * Copyright (c) 2013, 2022 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.integration.servlet_25_mvc_3;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.tests.integration.servlet_25_mvc_3.resource.Book;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ItemITCase extends TestSupport {

    @Test
    public void testResourceAsHtml() throws Exception {
        // NOTE: HttpUrlConnector sends several accepted types by default when not explicitly set by the caller.
        // In such case, the .accept("text/html") call is not necessary. However, other connectors act in a different way and
        // this leads in different behaviour when selecting the MessageBodyWriter. Leaving the definition explicit for broader
        // compatibility.
        final String response = item1resource().request(MediaType.TEXT_HTML).get(String.class);
        assertItemHtmlResponse(response);
    }

    @Test
    public void testResourceAsHtmlUtf8() throws Exception {
        final Response response = item1resource().path("utf").request().get();
        final String html = response.readEntity(String.class);

        assertItemHtmlResponse(html);
        assertResponseContains(html, "Ha\u0161ek");
    }

    @Test
    @Disabled("Jetty 9 is ignoring the charset")
    public void testResourceAsHtmlIso88592() throws Exception {
        final Response response = item1resource().path("iso").request().get();
        response.bufferEntity();

        final String htmlUtf8 = response.readEntity(String.class);

        assertItemHtmlResponse(htmlUtf8);
        assertFalse(htmlUtf8.contains("Ha\u0161ek"), "Response shouldn't contain Ha\u0161ek but was: " + htmlUtf8);

        final byte[] bytes = response.readEntity(byte[].class);
        final String htmlIso = new String(bytes, "ISO-8859-2");

        assertItemHtmlResponse(htmlIso);
        assertFalse(htmlIso.contains("Ha\u0161ek"), "Response shouldn't contain Ha\u0161ek but was: " + htmlIso);
        assertResponseContains(htmlIso, new String("Ha\u0161ek".getBytes(Charset.forName("UTF-8")), "ISO-8859-2"));
    }

    @Test
    public void testResourceAsXml() throws Exception {
        final String text = item1resource().request("application/xml").get(String.class);
        System.out.println("Item XML is: " + text);

        final Book response = item1resource().request("application/xml").get(Book.class);
        assertNotNull(response, "Should have returned an item!");
        assertEquals("Svejk", response.getTitle(), "item title");
    }

    @Test
    public void testResourceAsHtmlUsingWebKitAcceptHeaders() throws Exception {
        final String response = item1resource().request(
                "text/html",
                "application/xhtml+xml",
                "application/xml;q=0.9",
                "*/*;q=0.8").get(String.class);

        assertItemHtmlResponse(response);
    }

    protected void assertItemHtmlResponse(final String response) {
        assertHtmlResponse(response);
        assertResponseContains(response, "<title>Book</title>");
        assertResponseContains(response, "<h1>Svejk</h1>");
    }

    protected WebTarget item1resource() {
        return target().path("/items/1");
    }
}
