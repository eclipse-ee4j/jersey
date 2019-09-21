/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.integration.servlet_25_mvc_1;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.tests.integration.servlet_25_mvc_1.resource.Bookstore;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class BookstoreITCase extends TestSupport {

    @Test
    public void testResourceAsHtml() throws Exception {
        // NOTE: HttpUrlConnector sends several accepted types by default when not explicitly set by the caller.
        // In such case, the .accept("text/html") call is not necessary. However, other connectors act in a different way and
        // this leads in different behaviour when selecting the MessageBodyWriter. Leaving the definition explicit for broader
        // compatibility.
        assertBookstoreHtmlResponse(target().request(MediaType.TEXT_HTML_TYPE).get(String.class));
    }

    @Test
    public void testResourceAsXml() throws Exception {
        final Bookstore response = target().request("application/xml").get(Bookstore.class);

        assertNotNull("Should have returned a bookstore!", response);
        assertEquals("bookstore name", "Czech Bookstore", response.getName());
    }

    @Test
    public void testSingleContentTypeAndContentLengthValueInXmlResponse() throws Exception {
        assertStatusContentTypeAndLength(target().request("application/xml").get());
        assertStatusContentTypeAndLength(target()
                .request("text/html", "application/xhtml+xml", "application/xml;q=0.9", "*/*;q=0.8").get());
    }

    private void assertStatusContentTypeAndLength(Response response) {
        assertEquals("Should have returned a 200 response!", 200, response.getStatus());
        assertTrue("Should contain a Content-Type header!", response.getHeaders().containsKey(HttpHeaders.CONTENT_TYPE));
        assertEquals("Should have a single Content-Type header!", 1, response.getHeaders().get(HttpHeaders.CONTENT_TYPE).size());
        assertTrue("Should contain a Content-Length header!", response.getHeaders().containsKey(HttpHeaders.CONTENT_LENGTH));
        assertEquals("Should have a single Content-Length header!",
                1, response.getHeaders().get(HttpHeaders.CONTENT_LENGTH).size());
    }

    @Test
    public void testResourceAsHtmlUsingWebKitAcceptHeaders() throws Exception {
        final String response = target().request(
                "text/html",
                "application/xhtml+xml",
                "application/xml;q=0.9",
                "*/*;q=0.8").get(String.class);

        assertBookstoreHtmlResponse(response);
    }

    protected void assertBookstoreHtmlResponse(String response) {
        assertHtmlResponse(response);
        assertResponseContains(response, "Bookstore");
        assertResponseContains(response, "Item List");
    }
}
