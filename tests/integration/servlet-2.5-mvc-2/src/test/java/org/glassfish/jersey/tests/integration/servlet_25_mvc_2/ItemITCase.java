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

package org.glassfish.jersey.tests.integration.servlet_25_mvc_2;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.tests.integration.servlet_25_mvc_2.resource.Book;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ItemITCase extends TestSupport {

    @Test
    public void testResourceAsXml() throws Exception {
        final String text = item1resource().request("application/xml").get(String.class);
        System.out.println("Item XML is: " + text);

        final Book response = item1resource().request("application/xml").get(Book.class);
        assertItemXmlResponse(response);
    }

    @Test
    public void testResourceAsHtmlUsingWebKitAcceptHeaders() throws Exception {
        final Response response = item1resource().request(
                "text/html",
                "application/xhtml+xml",
                "application/xml;q=0.9",
                "*/*;q=0.8").get(Response.class);

        assertEquals(404, response.getStatus());
    }

    private void assertItemXmlResponse(final Book response) {
        assertNotNull("Should have returned an item!", response);
        assertEquals("item title", "Svejk", response.getTitle());
    }

    protected WebTarget item1resource() {
        return target().path("/items/1");
    }
}
