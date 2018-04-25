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

package org.glassfish.jersey.tests.e2e.json;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.message.DeflateEncoder;
import org.glassfish.jersey.message.GZipEncoder;
import org.glassfish.jersey.server.JSONP;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.filter.EncodingFilter;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests, that JSONP callback wrapping takes places before the eventual entity compression.
 *
 * See https://java.net/jira/browse/JERSEY-2524 for the original issue description.
 *
 * @author Adam Lindenthal (adam.lindenthal at oracle.com)
 */
public class JsonWithPaddingEncodingFilterTest extends JerseyTest {

    @Override
    protected ResourceConfig configure() {
        enable(TestProperties.LOG_TRAFFIC);
        return new ResourceConfig(MyResource.class)
                .register(JacksonFeature.class)
                .register(EncodingFilter.class)
                .register(GZipEncoder.class)
                .register(DeflateEncoder.class);
    }

    @Path("rest")
    public static class MyResource {
        @GET
        @Path("jsonp")
        @JSONP(queryParam = JSONP.DEFAULT_QUERY)
        @Produces("application/x-javascript")
        public Message getHelloJsonP(@Context final HttpHeaders headers) {
            final MultivaluedMap<String, String> headerParams = headers.getRequestHeaders();
            for (final String key : headerParams.keySet()) {
                System.out.println(key + ": ");
                for (final String value : headerParams.get(key)) {
                    System.out.print(value + ", ");
                }
                System.out.println("\b\b");
            }
            return new Message("Hello world JsonP!", "English");
        }
    }

    public static class Message {
        private String greeting;
        private String language;

        public Message(final String greeting, final String language) {
            this.greeting = greeting;
            this.language = language;
        }

        public String getGreeting() {
            return greeting;
        }

        public String getLanguage() {
            return language;
        }
    }

    @Test
    public void testCorrectGzipDecoding() {
        final Response response = target().path("rest/jsonp").queryParam("__callback", "dialog")
                .register(GZipEncoder.class).request("application/x-javascript")
                .header("Accept-Encoding", "gzip").get();

        final String result = response.readEntity(String.class);
        assertEquals("gzip", response.getHeaders().getFirst("Content-Encoding"));

        assertTrue(result.startsWith("dialog("));
        assertTrue(result.contains("Hello world JsonP!"));
        assertTrue(result.contains("English"));
    }

    @Test
    public void testCorrectDeflateDecoding() {
        final Response response = target().path("rest/jsonp").queryParam("__callback", "dialog")
                .register(DeflateEncoder.class).request("application/x-javascript")
                .header("Accept-Encoding", "deflate").get();

        final String result = response.readEntity(String.class);
        assertEquals("deflate", response.getHeaders().getFirst("Content-Encoding"));

        assertTrue(result.startsWith("dialog("));
        assertTrue(result.contains("Hello world JsonP!"));
        assertTrue(result.contains("English"));
    }
}
