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

package org.glassfish.jersey.tests.e2e.server.filter;

import java.util.List;
import java.util.Locale;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Variant;

import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * No activation property ({@value org.glassfish.jersey.server.ServerProperties#LANGUAGE_MAPPINGS},
 * {@value org.glassfish.jersey.server.ServerProperties#MEDIA_TYPE_MAPPINGS}) is set into {@link ResourceConfig} which should
 * lead to point that {@link org.glassfish.jersey.server.filter.UriConnegFilter} is not registered.
 *
 * @author Michal Gajdos
 */
public class UriConnegLanguageMediaTypeNegativeTest extends JerseyTest {

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
        return new ResourceConfig(LanguageVariantResource.class).register(LoggingFeature.class);
    }

    @Test
    public void testMediaTypesAndLanguagesNegative() {
        _test("english", "foo", "en", "application/foo");
        _test("french", "foo", "fr", "application/foo");

        _test("english", "bar", "en", "application/bar");
        _test("french", "bar", "fr", "application/bar");
    }

    private void _test(String ul, String um, String l, String m) {
        Response response = target().path("abc." + ul + "." + um).request().get();
        assertEquals(404, response.getStatus());

        response = target().path("abc." + um + "." + ul).request().get();
        assertEquals(404, response.getStatus());

        response = target().path("abc").request(m).header(HttpHeaders.ACCEPT_LANGUAGE, l).get();
        assertEquals(m + ", " + l, response.readEntity(String.class));
        assertEquals(l, response.getLanguage().toString());
        assertEquals(m, response.getMediaType().toString());
    }
}
