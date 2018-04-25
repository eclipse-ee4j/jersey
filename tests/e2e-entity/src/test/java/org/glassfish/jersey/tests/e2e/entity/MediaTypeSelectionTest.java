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

package org.glassfish.jersey.tests.e2e.entity;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * @author Martin Matula
 */
public class MediaTypeSelectionTest extends AbstractTypeTester {
    @Path("form")
    public static class FormResource {
        @POST
        public Response post(MultivaluedMap<String, String> data) {
            return Response.ok(data, MediaType.APPLICATION_FORM_URLENCODED_TYPE).build();
        }
    }

    @Path("foo")
    public static class FooResource {
        @POST
        @Consumes("foo/*")
        @Produces("foo/*")
        public String foo(String foo) {
            return foo;
        }
    }

    @Path("text")
    public static class TextResource {
        @GET
        @Produces("text/*")
        public String getText() {
            return "text";
        }

        @GET
        @Produces("application/*")
        @Path("any")
        public String getAny() {
            return "text";
        }

        @POST
        @Produces("text/*")
        public Response post(String entity) {
            return Response.ok().entity("entity").build();
        }
    }

    @Path("wildcard")
    public static class WildCardResource {
        @POST
        public String wildCard(String wc) {
            return wc;
        }
    }

    @Path("jira/1518")
    public static class Issue1518Resource {
        @POST
        @Consumes("text/plain;qs=0.7")
        public String never() {
            throw new WebApplicationException(Response.Status.CONFLICT);
        }

        @POST
        @Consumes("text/*")
        public String text() {
            return "1518";
        }
    }

    // JERSEY-1518 reproducer test
    @Test
    public void testQsInConsumes() {
        Response r = target("jira/1518").request(MediaType.TEXT_PLAIN_TYPE).post(Entity.text("request"));
        assertEquals(200, r.getStatus());
        assertEquals(MediaType.TEXT_PLAIN_TYPE, r.getMediaType());
        assertEquals("1518", r.readEntity(String.class));
    }

    // JERSEY-1187 regression test
    @Test
    public void testExplicitMediaType() {
        Response r = target("form").request().post(Entity.form(new Form().param("a", "b")));
        assertEquals(MediaType.APPLICATION_FORM_URLENCODED_TYPE, r.getMediaType());
        assertEquals("b", r.readEntity(Form.class).asMap().getFirst("a"));
    }

    @Test
    public void testAmbiguousWildcard() {
        Response r = target("foo").request().post(Entity.entity("test", "foo/plain"));
        assertEquals(406, r.getStatus());
    }

    @Test
    public void testWildcardInSubType() {
        Response r = target("text").request("text/*").get();
        assertEquals(406, r.getStatus());
    }

    @Test
    public void testWildcardInSubTypePost() {
        Response r = target("text").request("text/*").post(Entity.entity("test", MediaType.TEXT_PLAIN_TYPE));
        assertEquals(406, r.getStatus());
    }

    @Test
    public void testWildcardInSubType2() {
        Response r = target("text").request("*/*").get();
        assertEquals(406, r.getStatus());
    }

    @Test
    public void testWildcardsInTypeAndSubType() {
        Response r = target("text/any").request("*/*").get();
        assertEquals(200, r.getStatus());
        assertEquals(MediaType.APPLICATION_OCTET_STREAM_TYPE, r.getMediaType());
    }

    @Test
    public void testNoAcceptHeader() {
        // This test is testing the situation when the client sends no Accept header to the server and it expects
        // APPLICATION_OCTET_STREAM_TYPE to be returned. But when no Accept header is defined by the client api the
        // HttpURLConnection (in HttpUrlConnector)  always put there some default Accept header (like */*, text/plain, ...).
        // To overwrite this behaviour we set Accept to empty String. This works fine as the server code handles empty
        // Accept header like no Accept header.
        final MultivaluedHashMap headers = new MultivaluedHashMap();
        headers.add("Accept", "");

        Response r = target("text/any").request().headers(headers).get();
        assertEquals(200, r.getStatus());
        assertEquals(MediaType.APPLICATION_OCTET_STREAM_TYPE, r.getMediaType());
    }

    @Test
    public void testSpecific() {
        Response r = target("foo").request("foo/plain").post(Entity.entity("test", "foo/plain"));
        assertEquals(MediaType.valueOf("foo/plain"), r.getMediaType());
        assertEquals("test", r.readEntity(String.class));
    }

    @Test
    @Ignore("JSONB breaks this test.")
    public void testApplicationWildCard() {
        Response r = target("wildcard").request("application/*").post(Entity.text("test"));
        assertEquals(MediaType.APPLICATION_OCTET_STREAM_TYPE, r.getMediaType());
        assertEquals("test", r.readEntity(String.class));
    }
}
