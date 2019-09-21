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
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;

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
public class ExplicitTemplateTest extends JerseyTest {

    @Override
    protected Application configure() {
        enable(TestProperties.DUMP_ENTITY);
        enable(TestProperties.LOG_TRAFFIC);

        return new ResourceConfig(ExplicitTemplate.class)
                .register(MvcFeature.class)
                .register(TestViewProcessor.class);
    }

    public static class CustomResolvingClass {
    }

    @Path("/")
    public static class ExplicitTemplate {

        @GET
        @Template
        public String method() {
            return "method";
        }

        @GET
        @Path("methodRelativePath")
        @Template(name = "relative")
        public String methodRelativePath() {
            return "methodRelativePath";
        }

        @GET
        @Path("methodAbsolutePath")
        @Template(name = "/org/glassfish/jersey/tests/e2e/server/mvc/ExplicitTemplateTest/ExplicitTemplate/absolute")
        public String methodAbsolutePath() {
            return "methodAbsolutePath";
        }

        @Path("subResource")
        public ExplicitTemplate subResource() {
            return new ExplicitTemplate();
        }

        @Path("subResourceTemplate")
        @Template
        public ExplicitTemplateSubResource subResourceTemplate() {
            return new ExplicitTemplateSubResource();
        }
    }

    public static class ExplicitTemplateSubResource {

        @GET
        public String get() {
            return "get";
        }
    }

    @Test
    public void testExplicitMethodTemplateSubResource() throws Exception {
        assertEquals("get", target("subResourceTemplate").request().get(String.class));
    }

    @Test
    public void testExplicitTemplate() throws Exception {
        _testExplicitTemplate(target());
    }

    @Test
    public void testExplicitTemplateSubResource() throws Exception {
        _testExplicitTemplate(target("subResource"));
    }

    void _testExplicitTemplate(final WebTarget target) throws Exception {
        Properties props = new Properties();
        props.load(target.request().get(InputStream.class));
        assertEquals("/org/glassfish/jersey/tests/e2e/server/mvc/ExplicitTemplateTest/ExplicitTemplate/index.testp",
                props.getProperty("path"));
        assertEquals("method", props.getProperty("model"));

        props = new Properties();
        props.load(target.path("methodRelativePath").request().get(InputStream.class));
        assertEquals("/org/glassfish/jersey/tests/e2e/server/mvc/ExplicitTemplateTest/ExplicitTemplate/relative.testp",
                props.getProperty("path"));
        assertEquals("methodRelativePath", props.getProperty("model"));

        props = new Properties();
        props.load(target.path("methodAbsolutePath").request().get(InputStream.class));
        assertEquals("/org/glassfish/jersey/tests/e2e/server/mvc/ExplicitTemplateTest/ExplicitTemplate/absolute.testp",
                props.getProperty("path"));
        assertEquals("methodAbsolutePath", props.getProperty("model"));
    }
}
