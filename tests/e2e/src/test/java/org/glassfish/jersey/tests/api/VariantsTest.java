/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Variant;

import org.glassfish.jersey.message.internal.MediaTypes;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Server-side variant selection & handling test.
 *
 * @author Paul Sandoz
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public class VariantsTest extends JerseyTest {

    @Override
    protected ResourceConfig configure() {
        return new ResourceConfig(
                LanguageVariantResource.class,
                ComplexVariantResource.class,
                MediaTypeQualitySourceResource.class);
    }

    @Path("/lvr")
    public static class LanguageVariantResource {

        @GET
        public Response doGet(@Context Request r) {
            List<Variant> vs = Variant.VariantListBuilder.newInstance()
                    .languages(new Locale("zh"))
                    .languages(new Locale("fr"))
                    .languages(new Locale("en"))
                    .add()
                    .build();

            Variant v = r.selectVariant(vs);
            if (v == null) {
                return Response.notAcceptable(vs).build();
            } else {
                return Response.ok(v.getLanguage().toString(), v).build();
            }
        }
    }

    @Test
    public void testGetLanguageEn() throws IOException {
        WebTarget rp = target("/lvr");

        Response r = rp.request()
                .header("Accept-Language", "en")
                .get();
        assertEquals("en", r.readEntity(String.class));
        assertEquals("en", r.getLanguage().toString());
        String vary = r.getHeaderString("Vary");
        assertNotNull(vary);
        assertTrue(!contains(vary, "Accept"));
        assertTrue(contains(vary, "Accept-Language"));
    }

    @Test
    public void testGetLanguageZh() throws IOException {
        WebTarget rp = target("/lvr");

        Response r = rp.request()
                .header("Accept-Language", "zh")
                .get();
        assertEquals("zh", r.readEntity(String.class));
        assertEquals("zh", r.getLanguage().toString());
        System.out.println(r.getMetadata().getFirst("Vary"));
        String vary = r.getHeaderString("Vary");
        assertNotNull(vary);
        assertTrue(!contains(vary, "Accept"));
        assertTrue(contains(vary, "Accept-Language"));
    }

    @Test
    public void testGetLanguageMultiple() throws IOException {
        WebTarget rp = target("/lvr");

        Response r = rp
                .request()
                .header("Accept-Language", "en;q=0.3, zh;q=0.4, fr")
                .get();
        assertEquals("fr", r.readEntity(String.class));
        assertEquals("fr", r.getLanguage().toString());
        String vary = r.getHeaderString("Vary");
        assertNotNull(vary);
        assertTrue(!contains(vary, "Accept"));
        assertTrue(contains(vary, "Accept-Language"));
    }

    @Path("/cvr")
    public static class ComplexVariantResource {

        @GET
        public Response doGet(@Context Request r) {
            List<Variant> vs = Variant.VariantListBuilder.newInstance()
                    .mediaTypes(MediaType.valueOf("image/jpeg"))
                    .add()
                    .mediaTypes(MediaType.valueOf("application/xml"))
                    .languages(new Locale("en", "us"))
                    .add()
                    .mediaTypes(MediaType.valueOf("text/xml"))
                    .languages(new Locale("en"))
                    .add()
                    .mediaTypes(MediaType.valueOf("text/xml"))
                    .languages(new Locale("en", "us"))
                    .add()
                    .build();

            Variant v = r.selectVariant(vs);
            if (v == null) {
                return Response.notAcceptable(vs).build();
            } else {
                return Response.ok("GET", v).build();
            }
        }
    }

    @Test
    public void testGetComplex1() throws IOException {
        WebTarget rp = target("/cvr");

        Response r = rp.request("text/xml",
                "application/xml",
                "application/xhtml+xml",
                "image/png",
                "text/html;q=0.9",
                "text/plain;q=0.8",
                "*/*;q=0.5")
                .header("Accept-Language", "en-us,en;q=0.5")
                .get();
        assertEquals("GET", r.readEntity(String.class));
        assertEquals(MediaType.valueOf("text/xml"), r.getMediaType());
        assertEquals("en", r.getLanguage().getLanguage());
        assertEquals("US", r.getLanguage().getCountry());
        String vary = r.getHeaderString("Vary");
        assertNotNull(vary);
        assertTrue(contains(vary, "Accept"));
        assertTrue(contains(vary, "Accept-Language"));
    }

    @Test
    public void testGetComplex2() throws IOException {
        WebTarget rp = target("/cvr");

        Response r = rp.request("text/xml",
                "application/xml",
                "application/xhtml+xml",
                "image/png",
                "text/html;q=0.9",
                "text/plain;q=0.8",
                "*/*;q=0.5")
                .header("Accept-Language", "en,en-us")
                .get();
        assertEquals("GET", r.readEntity(String.class));
        assertEquals(MediaType.valueOf("text/xml"), r.getMediaType());
        assertEquals("en", r.getLanguage().toString());
        String vary = r.getHeaderString("Vary");
        assertNotNull(vary);
        assertTrue(contains(vary, "Accept"));
        assertTrue(contains(vary, "Accept-Language"));
    }

    @Test
    public void testGetComplex3() throws IOException {
        WebTarget rp = target("/cvr");

        Response r = rp.request("application/xml",
                "text/xml",
                "application/xhtml+xml",
                "image/png",
                "text/html;q=0.9",
                "text/plain;q=0.8",
                "*/*;q=0.5")
                .header("Accept-Language", "en-us,en;q=0.5")
                .get();
        assertEquals("GET", r.readEntity(String.class));
        assertEquals(MediaType.valueOf("application/xml"), r.getMediaType());
        assertEquals("en", r.getLanguage().getLanguage());
        assertEquals("US", r.getLanguage().getCountry());
        String vary = r.getHeaderString("Vary");
        assertNotNull(vary);
        assertTrue(contains(vary, "Accept"));
        assertTrue(contains(vary, "Accept-Language"));
    }

    @Test
    public void testGetComplexNotAcceptable() throws IOException {
        WebTarget rp = target("/cvr");

        Response r = rp.request("application/atom+xml")
                .header("Accept-Language", "en-us,en")
                .get();
        String vary = r.getHeaderString("Vary");
        assertNotNull(vary);
        assertTrue(contains(vary, "Accept"));
        assertTrue(contains(vary, "Accept-Language"));
        assertEquals(406, r.getStatus());

        r = rp.request("application/xml")
                .header("Accept-Language", "fr")
                .get();
        vary = r.getHeaderString("Vary");
        assertNotNull(vary);
        assertTrue(contains(vary, "Accept"));
        assertTrue(contains(vary, "Accept-Language"));
        assertEquals(406, r.getStatus());
    }

    @Path("/mtqsr")
    public static class MediaTypeQualitySourceResource {

        @GET
        public Response doGet(@Context Request r) {
            List<Variant> vs = Variant.VariantListBuilder.newInstance()
                    .mediaTypes(MediaType.valueOf("application/xml;qs=0.8"))
                    .mediaTypes(MediaType.valueOf("text/html;qs=1.0"))
                    .add()
                    .build();

            Variant v = r.selectVariant(vs);
            if (v == null) {
                return Response.notAcceptable(vs).build();
            } else {
                return Response.ok("GET", v).build();
            }
        }
    }

    @Test
    public void testMediaTypeQualitySource() throws IOException {
        WebTarget rp = target("/mtqsr");

        Response r = rp.request(
                "application/xml",
                "text/html;q=0.9")
                .get();
        assertTrue(MediaTypes.typeEqual(MediaType.valueOf("text/html"), r.getMediaType()));
        String vary = r.getHeaderString("Vary");
        assertNotNull(vary);
        assertTrue(contains(vary, "Accept"));

        r = rp.request(
                "text/html",
                "application/xml")
                .get();
        assertTrue(MediaTypes.typeEqual(MediaType.valueOf("text/html"), r.getMediaType()));
        vary = r.getHeaderString("Vary");
        assertNotNull(vary);
        assertTrue(contains(vary, "Accept"));

        r = rp.request(
                "application/xml",
                "text/html")
                .get();
        assertTrue(MediaTypes.typeEqual(MediaType.valueOf("text/html"), r.getMediaType()));
        vary = r.getHeaderString("Vary");
        assertNotNull(vary);
        assertTrue(contains(vary, "Accept"));

        r = rp.request(
                "application/xml")
                .get();
        assertTrue(MediaTypes.typeEqual(MediaType.valueOf("application/xml"), r.getMediaType()));
        vary = r.getHeaderString("Vary");
        assertNotNull(vary);
        assertTrue(contains(vary, "Accept"));
    }

    private boolean contains(String l, String v) {
        String[] vs = l.split(",");
        for (String s : vs) {
            s = s.trim();
            if (s.equalsIgnoreCase(v)) {
                return true;
            }
        }

        return false;
    }
}
