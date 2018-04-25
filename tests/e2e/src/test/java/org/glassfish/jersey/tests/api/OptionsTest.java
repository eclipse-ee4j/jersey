/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Pavel Bucek (pavel.bucek at oracle.com)
 */
public class OptionsTest extends JerseyTest {

    @Override
    protected ResourceConfig configure() {
        return new ResourceConfig(HttpOptionsTest.class);
    }

    @Path("/OptionsTest")
    public static class HttpOptionsTest {

        static String html_content =
                "<html><head><title>get text/html</title></head>"
                        + "<body>get text/html</body></html>";

        @GET
        public Response getPlain() {
            return Response.ok("CTS-get text/plain").header("TEST-HEAD", "text-plain")
                    .build();
        }

        @GET
        @Produces("text/html")
        public Response getHtml() {
            return Response.ok(html_content).header("TEST-HEAD", "text-html")
                    .build();
        }

        @GET
        @Path("/sub")
        public Response getSub() {
            return Response.ok("TEST-get text/plain").header("TEST-HEAD",
                    "sub-text-plain")
                    .build();
        }

        @GET
        @Path("/sub")
        @Produces(value = "text/html")
        public Response headSub() {
            return Response.ok(html_content).header("TEST-HEAD", "sub-text-html")
                    .build();
        }
    }

    /*
     * Client invokes OPTIONS on a sub resource at /OptionsTest/sub;
     * which no request method designated for OPTIONS.
     * Verify that an automatic response is generated.
     */
    @Test
    public void OptionSubTest() {
        final Response response = target().path("/OptionsTest/sub").request(MediaType.TEXT_HTML_TYPE).options();

        assertTrue(response.getAllowedMethods().contains("GET"));
        assertTrue(response.getAllowedMethods().contains("HEAD"));
        assertTrue(response.getAllowedMethods().contains("OPTIONS"));

        assertEquals(response.getMediaType(), MediaType.TEXT_HTML_TYPE);
    }
}
