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

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.mvc.MvcFeature;
import org.glassfish.jersey.server.mvc.Template;
import org.glassfish.jersey.server.mvc.Viewable;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.glassfish.jersey.tests.e2e.server.mvc.provider.TestViewProcessor;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * @author Paul Sandoz
 * @author Michal Gajdos
 */
public class FlatInheritedViewProcessorTest extends JerseyTest {

    @Override
    protected Application configure() {
        enable(TestProperties.DUMP_ENTITY);
        enable(TestProperties.LOG_TRAFFIC);

        return new ResourceConfig(ExplicitTemplate.class, ImplicitTemplate.class)
                .register(MvcFeature.class)
                .register(TestViewProcessor.class);
    }

    public static class ExplicitTemplateBase {
    }

    @Path("/explicit")
    public static class ExplicitTemplate extends ExplicitTemplateBase {

        @GET
        public Viewable get() {
            return new Viewable("show", "get");
        }

        @Path("inherit")
        @GET
        public Viewable getInherited() {
            return new Viewable("inherit", "get");
        }

        @Path("override")
        @GET
        public Viewable getOverriden() {
            return new Viewable("override", "get");
        }
    }

    @Test
    public void testExplicitTemplate() throws IOException {
        final WebTarget target = target("explicit");

        Properties p = new Properties();
        p.load(target.request().get(InputStream.class));
        assertEquals("/org/glassfish/jersey/tests/e2e/server/mvc/FlatInheritedViewProcessorTest.ExplicitTemplateBase.show.testp",
                p.getProperty("path"));
        assertEquals("get", p.getProperty("model"));

        p = new Properties();
        p.load(target.path("inherit").request().get(InputStream.class));
        assertEquals(
                "/org/glassfish/jersey/tests/e2e/server/mvc/FlatInheritedViewProcessorTest.ExplicitTemplateBase.inherit.testp",
                p.getProperty("path"));
        assertEquals("get", p.getProperty("model"));

        p = new Properties();
        p.load(target.path("override").request().get(InputStream.class));
        assertEquals("/org/glassfish/jersey/tests/e2e/server/mvc/FlatInheritedViewProcessorTest.ExplicitTemplate.override.testp",
                p.getProperty("path"));
        assertEquals("get", p.getProperty("model"));
    }

    public static class ImplicitTemplateBase {
    }

    @Path("/implicit")
    @Template
    public static class ImplicitTemplate extends ImplicitTemplateBase {

        public String toString() {
            return "ImplicitTemplate";
        }
    }

    @Test
    public void testImplicitTemplate() throws IOException {
        final WebTarget target = target("implicit");

        Properties p = new Properties();
        p.load(target.request().get(InputStream.class));
        assertEquals("/org/glassfish/jersey/tests/e2e/server/mvc/FlatInheritedViewProcessorTest.ImplicitTemplateBase.index.testp",
                p.getProperty("path"));
        assertEquals("ImplicitTemplate", p.getProperty("model"));

        p = new Properties();
        p.load(target.path("inherit").request().get(InputStream.class));
        assertEquals(
                "/org/glassfish/jersey/tests/e2e/server/mvc/FlatInheritedViewProcessorTest.ImplicitTemplateBase.inherit.testp",
                p.getProperty("path"));
        assertEquals("ImplicitTemplate", p.getProperty("model"));

        p = new Properties();
        p.load(target.path("override").request().get(InputStream.class));
        assertEquals("/org/glassfish/jersey/tests/e2e/server/mvc/FlatInheritedViewProcessorTest.ImplicitTemplate.override.testp",
                p.getProperty("path"));
        assertEquals("ImplicitTemplate", p.getProperty("model"));
    }

}
