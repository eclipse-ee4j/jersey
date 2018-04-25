/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.bookstore.webapp.resource;

import javax.ws.rs.client.WebTarget;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author James Strachan
 * @author Naresh
 * @author Michal Gajdos
 */
public class ItemTest extends TestSupport {

    @Test
    public void testResourceAsHtml() throws Exception {
        final String response = item1resource().request().get(String.class);
        assertItemHtmlResponse(response);
    }

    @Test
    public void testResourceAsXml() throws Exception {
        final String text = item1resource().request("application/xml").get(String.class);
        System.out.println("Item XML is: " + text);

        final Book response = item1resource().request("application/xml").get(Book.class);
        assertNotNull("Should have returned an item!", response);
        assertEquals("item title", "Svejk", response.getTitle());
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

    protected void assertItemHtmlResponse(String response) {
        assertHtmlResponse(response);
        assertResponseContains(response, "<title>Book</title>");
        assertResponseContains(response, "<h1>Svejk</h1>");
    }

    protected WebTarget item1resource() {
        return target("bookstore-webapp").path("/items/1");
    }

}
