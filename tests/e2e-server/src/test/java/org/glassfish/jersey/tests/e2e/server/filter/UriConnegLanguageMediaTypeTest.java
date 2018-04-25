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

package org.glassfish.jersey.tests.e2e.server.filter;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Variant;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 *
 * @author Paul Sandoz
 * @author Martin Matula
 */
public class UriConnegLanguageMediaTypeTest extends JerseyTest {

    @Path("/abc")
    public static class LanguageVariantResource {

        @GET
        public Response doGet(@Context Request r) {
            final List<Variant> variants = Variant.VariantListBuilder.newInstance()
                    .mediaTypes(MediaType.valueOf("application/foo"))
                    .languages(new Locale("en")).languages(new Locale("fr")).add()
                    .mediaTypes(MediaType.valueOf("application/bar"))
                    .languages(new Locale("en")).languages(new Locale("fr")).add()
                    .build();

            final Variant variant = r.selectVariant(variants);
            if (variant == null) {
                return Response.notAcceptable(variants).build();
            } else {
                return Response.ok(variant.getMediaType().toString() + ", " + variant.getLanguage(), variant).build();
            }
        }
    }

    @Override
    protected Application configure() {
        Map<String, MediaType> mediaTypes = new HashMap<>();
        mediaTypes.put("foo", MediaType.valueOf("application/foo"));
        mediaTypes.put("bar", MediaType.valueOf("application/bar"));

        Map<String, String> languages = new HashMap<>();
        languages.put("english", "en");
        languages.put("french", "fr");

        ResourceConfig rc = new ResourceConfig(LanguageVariantResource.class);
        rc.property(ServerProperties.LANGUAGE_MAPPINGS, languages);
        rc.property(ServerProperties.MEDIA_TYPE_MAPPINGS, mediaTypes);
        return rc;
    }

    @Test
    public void testMediaTypesAndLanguages() {
        _test("english", "foo", "en", "application/foo");
        _test("french", "foo", "fr", "application/foo");

        _test("english", "bar", "en", "application/bar");
        _test("french", "bar", "fr", "application/bar");
    }

    private void _test(String ul, String um, String l, String m) {
        Response r = target().path("abc." + ul + "." + um).request().get();
        assertEquals(m + ", " + l, r.readEntity(String.class));
        assertEquals(l, r.getLanguage().toString());
        assertEquals(m, r.getMediaType().toString());

        r = target().path("abc." + um + "." + ul).request().get();
        assertEquals(m + ", " + l, r.readEntity(String.class));
        assertEquals(l, r.getLanguage().toString());
        assertEquals(m, r.getMediaType().toString());

        r = target().path("abc").request(m).header(HttpHeaders.ACCEPT_LANGUAGE, l).get();
        assertEquals(m + ", " + l, r.readEntity(String.class));
        assertEquals(l, r.getLanguage().toString());
        assertEquals(m, r.getMediaType().toString());
    }
}
