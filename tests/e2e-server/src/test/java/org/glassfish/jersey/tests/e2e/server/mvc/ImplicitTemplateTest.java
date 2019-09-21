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

package org.glassfish.jersey.tests.e2e.server.mvc;

import java.io.InputStream;
import java.util.Properties;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;

import javax.inject.Singleton;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.mvc.MvcFeature;
import org.glassfish.jersey.server.mvc.Template;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.glassfish.jersey.tests.e2e.server.mvc.provider.TestViewProcessor;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * @author Michal Gajdos
 */
public class ImplicitTemplateTest extends JerseyTest {

    @Override
    protected Application configure() {
        enable(TestProperties.DUMP_ENTITY);
        enable(TestProperties.LOG_TRAFFIC);

        return new ResourceConfig(
                ImplicitResource.class, AnotherImplicitResource.class,
                ImplicitSingletonResource.class, ImplicitRootResource.class,
                ImplicitGetResource.class, AnotherImplicitGetResource.class, AnotherAnotherImplicitGetResource.class)
                .register(MvcFeature.class)
                .register(TestViewProcessor.class);
    }

    @Template
    @Path("/implicit")
    public static class ImplicitResource {

        public String toString() {
            return "ImplicitTemplate";
        }
    }

    @Path("/implicit")
    public static class AnotherImplicitResource {

        public String toString() {
            return "ImplicitAnotherTemplate";
        }
    }

    @Test
    public void testImplicitTemplateResources() throws Exception {
        for (final String path : new String[] {"", "index", "get"}) {
            WebTarget target = target("implicit");
            String templateName = "index";

            if (!"".equals(path)) {
                templateName = path;
                target = target.path(path);
            }

            Properties p = new Properties();
            p.load(target.request().get(InputStream.class));
            assertEquals(
                    "/org/glassfish/jersey/tests/e2e/server/mvc/ImplicitTemplateTest/ImplicitResource/" + templateName + ".testp",
                    p.getProperty("path"));
            assertEquals("ImplicitTemplate", p.getProperty("model"));
        }
    }

    @Test
    public void testImplicitTemplateResourcesNegative() throws Exception {
        assertEquals(404, target("implicit").path("do-not-exist").request().get().getStatus());
    }

    @Path("/implicit-get")
    @Produces("text/html")
    public static class ImplicitGetResource {

        @GET
        public String get() {
            return toString();
        }

        public String toString() {
            return "ImplicitGetTemplate";
        }
    }

    @Path("/implicit-get")
    @Template
    @Produces("text/plain")
    public static class AnotherImplicitGetResource {

        @GET
        @Path("sub")
        public String get() {
            return toString();
        }

        public String toString() {
            return "AnotherImplicitGetTemplate";
        }
    }

    @Path("/implicit-get/another")
    public static class AnotherAnotherImplicitGetResource {

        @GET
        public String get() {
            return toString();
        }

        public String toString() {
            return "AnotherAnotherImplicitGetTemplate";
        }
    }

    @Test
    public void testImplicitGetTemplateResources() throws Exception {
        for (final String path : new String[] {"", "index", "get"}) {
            WebTarget target = target("implicit-get");
            String templateName = "index";

            if (!"".equals(path)) {
                templateName = path;
                target = target.path(path);
            }

            Properties p = new Properties();
            p.load(target.request("text/plain").get(InputStream.class));
            assertEquals(
                    "/org/glassfish/jersey/tests/e2e/server/mvc/ImplicitTemplateTest/AnotherImplicitGetResource/" + templateName
                            + ".testp",
                    p.getProperty("path"));
            assertEquals("AnotherImplicitGetTemplate", p.getProperty("model"));
        }
    }

    @Template
    @Singleton
    @Path("/implicit-singleton")
    public static class ImplicitSingletonResource {

        private int counter = 0;

        public String toString() {
            return "ImplicitSingletonTemplate" + counter++;
        }
    }

    @Test
    public void testImplicitTemplateSingletonResources() throws Exception {
        for (int i = 0; i < 10; i++) {
            final WebTarget target = target("implicit-singleton");

            Properties p = new Properties();
            p.load(target.request().get(InputStream.class));
            assertEquals("/org/glassfish/jersey/tests/e2e/server/mvc/ImplicitTemplateTest/ImplicitSingletonResource/index.testp",
                    p.getProperty("path"));
            assertEquals("ImplicitSingletonTemplate" + i, p.getProperty("model"));
        }
    }

    @Path("/implicit-sub-resource")
    public static class ImplicitRootResource {

        @Path("sub")
        public ImplicitSubResource getSubResource() {
            return new ImplicitSubResource("ImplicitRootResource");
        }
    }

    public static class ImplicitSubResource {

        private final String string;

        public ImplicitSubResource(final String string) {
            this.string = string;
        }

        @Path("sub")
        public ImplicitSubSubResource getSubResource() {
            return new ImplicitSubSubResource(string + "ImplicitSubResource");
        }
    }

    @Template
    public static class ImplicitSubSubResource {

        private final String string;

        public ImplicitSubSubResource(final String string) {
            this.string = string;
        }

        @Override
        public String toString() {
            return string + "ImplicitSubSubResource";
        }
    }

    @Test
    public void testImplicitTemplateSubResources() throws Exception {
        final WebTarget target = target("implicit-sub-resource").path("sub").path("sub");

        Properties p = new Properties();
        p.load(target.request().get(InputStream.class));
        assertEquals("/org/glassfish/jersey/tests/e2e/server/mvc/ImplicitTemplateTest/ImplicitSubSubResource/index.testp",
                p.getProperty("path"));
        assertEquals("ImplicitRootResourceImplicitSubResourceImplicitSubSubResource", p.getProperty("model"));
    }
}
