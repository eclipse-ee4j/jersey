/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.integration.servlet_40_mvc_1;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import javax.ws.rs.core.Response;

import org.glassfish.jersey.message.GZipEncoder;
import org.junit.Test;

public class GzipITCase extends TestSupport {

    @Test
    public void testString() throws Exception {
        Response response = target("/client/string").register(GZipEncoder.class)
                .request("text/html").acceptEncoding("gzip").get();
        String resp = response.readEntity(String.class);
        assertResponseContains(resp, "string string string string string string");
        assertEquals("gzip", response.getHeaderString("Content-Encoding"));
    }

    @Test
    public void testJsp() throws Exception {
        Response response = target("/client/html").register(GZipEncoder.class)
                .request("text/html", "application/xhtml+xml", "application/xml;q=0.9", "*/*;q=0.8").acceptEncoding("gzip")
                .get();
        String resp = response.readEntity(String.class);
        assertHtmlResponse(resp);
        assertResponseContains(resp, "find this string");
        assertEquals("gzip", response.getHeaderString("Content-Encoding"));
    }

    @Test
    public void testJspNotDecoded() throws Exception {
        Response response = target("/client/html")
                .request("text/html", "application/xhtml+xml", "application/xml;q=0.9", "*/*;q=0.8").acceptEncoding("gzip")
                .get();
        String resp = response.readEntity(String.class);
        assertFalse(resp.contains("find this string"));
        assertEquals("gzip", response.getHeaderString("Content-Encoding"));
    }

}
