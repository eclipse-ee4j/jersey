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
public class UriConnegLanguageTest extends JerseyTest {

    @Path("/abc")
    public static class LanguageVariantResource {

        @GET
        public Response doGet(@Context Request request, @Context HttpHeaders headers) {

            assertEquals(1, headers.getAcceptableLanguages().size());

            List<Variant> vs = Variant.VariantListBuilder.newInstance()
                    .languages(new Locale("zh"))
                    .languages(new Locale("fr"))
                    .languages(new Locale("en")).add()
                    .build();

            Variant v = request.selectVariant(vs);
            if (v == null) {
                return Response.notAcceptable(vs).build();
            } else {
                return Response.ok(v.getLanguage().toString(), v).build();
            }
        }
    }

    @Override
    protected Application configure() {
        Map<String, String> languages = new HashMap<>();
        languages.put("english", "en");
        languages.put("french", "fr");

        ResourceConfig rc = new ResourceConfig(LanguageVariantResource.class);
        rc.property(ServerProperties.LANGUAGE_MAPPINGS, languages);
        return rc;
    }

    @Test
    public void testLanguages() {
        Response response = target().path("abc.english").request().get();
        assertEquals("en", response.readEntity(String.class));
        assertEquals("en", response.getLanguage().toString());

        response = target().path("abc.french").request().get();
        assertEquals("fr", response.readEntity(String.class));
        assertEquals("fr", response.getLanguage().toString());

        response = target().path("abc.french").request().header(HttpHeaders.ACCEPT_LANGUAGE, "en").get();
        assertEquals("fr", response.readEntity(String.class));
        assertEquals("fr", response.getLanguage().toString());

        response = target().path("abc").request().header(HttpHeaders.ACCEPT_LANGUAGE, "en").get();
        assertEquals("en", response.readEntity(String.class));
        assertEquals("en", response.getLanguage().toString());

        response = target().path("abc").request().header(HttpHeaders.ACCEPT_LANGUAGE, "fr").get();
        assertEquals("fr", response.readEntity(String.class));
        assertEquals("fr", response.getLanguage().toString());
    }
}
