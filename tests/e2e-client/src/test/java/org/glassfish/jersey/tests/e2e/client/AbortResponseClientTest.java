/*
 * Copyright (c) 2012, 2019 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.e2e.client;

import java.io.IOException;
import java.net.URI;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonString;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * Tests aborting the request on the client side.
 *
 * @author Miroslav Fuksa
 */
public class AbortResponseClientTest extends JerseyTest {

    @Override
    protected Application configure() {
        return new ResourceConfig(TestResource.class);
    }

    @Test
    public void testRequestAbort() {

        final Date date = getDate();

        ClientRequestFilter outFilter = new ClientRequestFilter() {

            @Override
            public void filter(ClientRequestContext context) throws IOException {
                NewCookie cookie1 = new NewCookie("cookie1", "cookie1");
                NewCookie cookie2 = new NewCookie("cookie2", "cookie2");
                final Response response = Response.ok().cookie(cookie1).cookie(cookie2)
                        .header("head1", "head1").header(HttpHeaders.DATE, date).header(HttpHeaders.ETAG,
                                "\"123465\"").header(HttpHeaders.CONTENT_LANGUAGE, "language").header(HttpHeaders.LAST_MODIFIED,
                                date).header(HttpHeaders.CONTENT_LENGTH, 99).type(MediaType.TEXT_HTML_TYPE)
                        .location(URI.create("www.oracle.com")).build();

                // abort the request
                context.abortWith(response);
            }
        };
        ClientResponseFilter inFilter = new ClientResponseFilter() {
            @Override
            public void filter(ClientRequestContext requestContext,
                               ClientResponseContext responseContext) throws IOException {
                Map<String, NewCookie> map = responseContext.getCookies();
                assertEquals("cookie1", map.get("cookie1").getValue());
                assertEquals("cookie2", map.get("cookie2").getValue());
                final MultivaluedMap<String, String> headers = responseContext.getHeaders();
                assertEquals("head1", headers.get("head1").get(0));
                assertEquals(date.getTime(), responseContext.getDate().getTime());
            }
        };

        WebTarget target = target().path("test");
        target.register(outFilter).register(inFilter);
        Invocation i = target.request().buildGet();
        Response r = i.invoke();

        assertEquals("head1", r.getHeaderString("head1"));
        assertEquals("cookie1", r.getCookies().get("cookie1").getValue());
        assertEquals("cookie2", r.getCookies().get("cookie2").getValue());
        assertEquals(date.getTime(), r.getDate().getTime());
        assertEquals("123465", r.getEntityTag().getValue());
        assertEquals("language", r.getLanguage().toString());
        assertEquals(date.getTime(), r.getLastModified().getTime());
        // Assert.assertEquals("uri", r.getLink("link")); TODO: not supported yet
        assertEquals("www.oracle.com", r.getLocation().toString());
        assertEquals(MediaType.TEXT_HTML_TYPE, r.getMediaType());
        assertEquals(99, r.getLength());

        assertEquals(200, r.getStatus());
        r.close();
    }

    @Test
    public void testAbortWithJson() {
        final JsonString jsonString = Json.createValue("{\"key\":\"value\"}");
        ClientRequestFilter filter = new ClientRequestFilter() {
            @Override
            public void filter(ClientRequestContext requestContext) throws IOException {
                requestContext.abortWith(Response.ok(jsonString).build());
            }
        };
        WebTarget target = target().path("test").register(filter);
        try (Response response = target.request().get()){
            assertThat(response.getMediaType(), is(MediaType.APPLICATION_JSON_TYPE));
            assertThat(response.readEntity(JsonString.class), is(jsonString));
        };
    }

    private Date getDate() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, 2012);
        cal.set(Calendar.MONTH, 7);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR, 10);
        cal.set(Calendar.MINUTE, 5);
        cal.set(Calendar.SECOND, 1);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    @Path("test")
    public static class TestResource {

        @GET
        public String get() {
            return "this will never be called.";
        }

    }
}
