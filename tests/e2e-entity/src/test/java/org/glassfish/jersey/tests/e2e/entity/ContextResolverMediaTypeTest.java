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

package org.glassfish.jersey.tests.e2e.entity;

import java.io.IOException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.Providers;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import static org.junit.Assert.assertEquals;

/**
 * @author Paul Sandoz
 * @author Martin Matula
 */
@RunWith(Enclosed.class)
public class ContextResolverMediaTypeTest {

    @Produces("text/plain")
    @Provider
    @Ignore("This class is not a test class & must be ignored by the Enclosed test runner.")
    public static class TextPlainContextResolver implements ContextResolver<String> {

        public String getContext(Class<?> objectType) {
            return "text/plain";
        }
    }

    @Produces("text/*")
    @Provider
    @Ignore("This class is not a test class & must be ignored by the Enclosed test runner.")
    public static class TextContextResolver implements ContextResolver<String> {

        public String getContext(Class<?> objectType) {
            return "text/*";
        }
    }

    @Produces("*/*")
    @Provider
    @Ignore("This class is not a test class & must be ignored by the Enclosed test runner.")
    public static class WildcardContextResolver implements ContextResolver<String> {

        public String getContext(Class<?> objectType) {
            return "*/*";
        }
    }

    @Produces({"text/plain", "text/html"})
    @Provider
    @Ignore("This class is not a test class & must be ignored by the Enclosed test runner.")
    public static class TextPlainHtmlContextResolver implements ContextResolver<String> {

        public String getContext(Class<?> objectType) {
            return "text/plain/html";
        }

    }

    @Produces("text/html")
    @Provider
    @Ignore("This class is not a test class & must be ignored by the Enclosed test runner.")
    public static class TextHtmlContextResolver implements ContextResolver<String> {

        public String getContext(Class<?> objectType) {
            return "text/html";
        }

    }

    @Path("/")
    @Ignore("This class is not a test class & must be ignored by the Enclosed test runner.")
    public static class ContextResource {

        @Context
        Providers p;

        @Context
        ContextResolver<String> cr;

        @GET
        @Path("{id: .+}")
        public String get(@PathParam("id") MediaType m) {
            ContextResolver<String> cr = p.getContextResolver(String.class, m);

            // Verify cache is working
            ContextResolver<String> cachedCr = p.getContextResolver(String.class, m);
            assertEquals(cr, cachedCr);

            if (cr == null) {
                return "NULL";
            } else {
                return cr.getContext(null);
            }
        }
    }

    public static class ProduceTest extends JerseyTest {

        @Override
        protected Application configure() {
            return new ResourceConfig(ContextResource.class,
                    TextPlainContextResolver.class,
                    TextContextResolver.class,
                    WildcardContextResolver.class);
        }

        @Test
        public void testProduce() throws IOException {

            WebTarget target = target();

            assertEquals("text/plain", target.path("text/plain").request().get(String.class));
            assertEquals("text/*", target.path("text/*").request().get(String.class));
            assertEquals("*/*", target.path("*/*").request().get(String.class));

            assertEquals("text/*", target.path("text/html").request().get(String.class));

            assertEquals("*/*", target.path("application/xml").request().get(String.class));
            assertEquals("*/*", target.path("application/*").request().get(String.class));
        }
    }

    public static class ProducesTest extends JerseyTest {

        @Override
        protected Application configure() {
            return new ResourceConfig(ContextResource.class,
                    TextPlainHtmlContextResolver.class,
                    TextContextResolver.class,
                    WildcardContextResolver.class);
        }

        @Test
        public void testProduces() throws IOException {
            WebTarget target = target();

            assertEquals("text/plain/html", target.path("text/plain").request().get(String.class));
            assertEquals("text/plain/html", target.path("text/html").request().get(String.class));
            assertEquals("text/*", target.path("text/*").request().get(String.class));
            assertEquals("*/*", target.path("*/*").request().get(String.class));

            assertEquals("text/*", target.path("text/csv").request().get(String.class));

            assertEquals("*/*", target.path("application/xml").request().get(String.class));
            assertEquals("*/*", target.path("application/*").request().get(String.class));
        }
    }

    public static class ProducesSeparateTest extends JerseyTest {

        @Override
        protected Application configure() {
            return new ResourceConfig(ContextResource.class,
                    TextPlainContextResolver.class,
                    TextHtmlContextResolver.class,
                    TextContextResolver.class,
                    WildcardContextResolver.class);
        }

        @Test
        public void testProducesSeparate() throws IOException {
            WebTarget target = target();

            assertEquals("text/plain", target.path("text/plain").request().get(String.class));
            assertEquals("text/html", target.path("text/html").request().get(String.class));
            assertEquals("text/*", target.path("text/*").request().get(String.class));
            assertEquals("*/*", target.path("*/*").request().get(String.class));

            assertEquals("text/*", target.path("text/csv").request().get(String.class));

            assertEquals("*/*", target.path("application/xml").request().get(String.class));
            assertEquals("*/*", target.path("application/*").request().get(String.class));
        }
    }

    public static class ProducesXXXTest extends JerseyTest {

        @Override
        protected Application configure() {
            return new ResourceConfig(ContextResource.class,
                    TextPlainContextResolver.class,
                    TextHtmlContextResolver.class);
        }

        @Test
        public void testProducesXXX() throws IOException {
            WebTarget target = target();

            assertEquals("text/plain", target.path("text/plain").request().get(String.class));
            assertEquals("text/html", target.path("text/html").request().get(String.class));
            assertEquals("NULL", target.path("text/*").request().get(String.class));
            assertEquals("NULL", target.path("*/*").request().get(String.class));

            assertEquals("NULL", target.path("text/csv").request().get(String.class));

            assertEquals("NULL", target.path("application/xml").request().get(String.class));
            assertEquals("NULL", target.path("application/*").request().get(String.class));
        }
    }
}
