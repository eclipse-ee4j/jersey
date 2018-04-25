/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.bookstore.webapp.resource;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author James Strachan
 * @author Naresh
 * @author Michal Gajdos
 */
public class BookstoreTest extends TestSupport {

    @Test
    public void testResourceAsHtml() throws Exception {
        assertBookstoreHtmlResponse(target("bookstore-webapp").request().get(String.class));
    }

    @Test
    public void testHappyResourceAsHtml() throws Exception {
        final String response = target("bookstore-webapp/happy").request().get(String.class);

        assertHtmlResponse(response);
        assertResponseContains(response, "Happy");
    }

    @Test
    public void testResourceAsXml() throws Exception {
        final Bookstore response = target("bookstore-webapp").request("application/xml").get(Bookstore.class);

        assertNotNull("Should have returned a bookstore!", response);
        assertEquals("bookstore name", "Czech Bookstore", response.getName());
    }

    @Test
    public void testResourceAsHtmlUsingWebKitAcceptHeaders() throws Exception {
        final String response = target("bookstore-webapp").request(
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
