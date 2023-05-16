/*
 * Copyright (c) 2012, 2023 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.e2e;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Variant;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author Marek Potociar
 */
public class ClientTest extends JerseyTest {

    @Path("helloworld")
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.TEXT_PLAIN)
    public static class HelloWorldResource {

        private static final String MESSAGE = "Hello world!";

        @GET
        public String getClichedMessage() {
            return MESSAGE;
        }
    }

    @Path("headers")
    @Produces(MediaType.TEXT_PLAIN)
    public static class HeadersTestResource {

        @POST
        @Path("content")
        public String contentHeaders(@HeaderParam("custom-header") final String customHeader,
                                     @Context final HttpHeaders headers, final String entity) {
            final StringBuilder sb = new StringBuilder(entity).append('\n');

            sb.append("custom-header:").append(customHeader).append('\n');

            for (final Map.Entry<String, List<String>> header : headers.getRequestHeaders().entrySet()) {
                sb.append(header.getKey()).append(':').append(header.getValue().toString()).append('\n');
            }

            return sb.toString();
        }

        @GET
        @Path("/tostring")
        public String headersGet(@Context HttpHeaders hs) {
            StringBuilder sb = new StringBuilder();
            List<String> myHeaders = Arrays.asList("Accept", "Content-Type");

            try {
                MultivaluedMap<String, String> rqhdrs = hs.getRequestHeaders();
                Set<String> keys = rqhdrs.keySet();
                sb.append("getRequestHeaders= ");
                for (String header : myHeaders) {
                    if (keys.contains(header)) {
                        sb.append(
                                "Found " + header + ": " + hs.getRequestHeader(header) + "; ");
                    }
                }
            } catch (Throwable ex) {
                sb.append("Unexpected exception thrown in getRequestHeaders: "
                        + ex.getMessage());
                ex.printStackTrace();
            }
            return sb.toString();
        }
    }

    @Override
    protected ResourceConfig configure() {
        return new ResourceConfig(HelloWorldResource.class, HeadersTestResource.class);
    }

    @Test
    public void testAccesingHelloworldResource() {
        final WebTarget resource = target().path("helloworld");
        final Response r = resource.request().get();
        assertEquals(200, r.getStatus());

        final String responseMessage = resource.request().get(String.class);
        assertEquals(HelloWorldResource.MESSAGE, responseMessage);
    }

    @Test
    public void testHeadersToString() {
        try (Response response = target("headers").path("tostring").request()
                .header(HttpHeaders.ACCEPT, "text/*, text/html, text/html;level=1, */*")
                .header(HttpHeaders.CONTENT_TYPE, "application/xml;charset=utf8")
                .get()) {
            String content = response.readEntity(String.class);
            int index = -1;
            Assertions.assertTrue((index = content.indexOf("getRequestHeaders=")) != -1);
            Assertions.assertTrue((index = content.indexOf("Accept:")) != -1);
            Assertions.assertTrue((index = content.indexOf("text/*")) != -1);
            Assertions.assertTrue((index = content.indexOf("text/html")) != -1);
            Assertions.assertTrue((index = content.indexOf("text/html")) != -1);
            Assertions.assertTrue((index = content.indexOf("*/*")) != -1);
            Assertions.assertTrue((index = content.indexOf("Content-Type:")) != -1);
            Assertions.assertTrue((index = content.indexOf("application/xml")) != -1);
            Assertions.assertTrue((index = content.indexOf("charset=utf8")) != -1);
        }
    }

    @Test
    public void testAccesingMissingResource() {
        final WebTarget missingResource = target().path("missing");
        final Response r = missingResource.request().get();
        assertEquals(404, r.getStatus());


        try {
            missingResource.request().get(String.class);
        } catch (final WebApplicationException ex) {
            assertEquals(404, ex.getResponse().getStatus());
            return;
        }

        fail("Expected WebApplicationException has not been thrown.");
    }

    @Test
    // Inspired by JERSEY-1502
    public void testContextHeaders() {
        final WebTarget target = target().path("headers").path("content");

        Invocation.Builder ib;
        Invocation i;
        Response r;
        String reqHeaders;

        ib = target.request("*/*");
        ib.header("custom-header", "custom-value");
        ib.header("content-encoding", "deflate");
        i = ib.build("POST", Entity.entity("aaa", MediaType.WILDCARD_TYPE));
        r = i.invoke();

        reqHeaders = r.readEntity(String.class).toLowerCase();
        for (final String expected : new String[] {"custom-header:[custom-value]", "custom-header:custom-value"}) {
            assertTrue(reqHeaders.contains(expected),
                    String.format("Request headers do not contain expected '%s' entry:\n%s", expected, reqHeaders));
        }
        final String unexpected = "content-encoding";
        assertFalse(reqHeaders.contains(unexpected),
                String.format("Request headers contains unexpected '%s' entry:\n%s", unexpected, reqHeaders));

        ib = target.request("*/*");
        i = ib.build("POST",
                Entity.entity("aaa", Variant.mediaTypes(MediaType.WILDCARD_TYPE).encodings("deflate").build().get(0)));
        r = i.invoke();

        final String expected = "content-encoding:[deflate]";
        reqHeaders = r.readEntity(String.class).toLowerCase();
        assertTrue(reqHeaders.contains(expected),
                String.format("Request headers do not contain expected '%s' entry:\n%s", expected, reqHeaders));
    }
}
