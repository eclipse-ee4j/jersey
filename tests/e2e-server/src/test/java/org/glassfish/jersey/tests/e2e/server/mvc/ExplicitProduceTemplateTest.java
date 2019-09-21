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

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.mvc.MvcFeature;
import org.glassfish.jersey.server.mvc.Template;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.glassfish.jersey.tests.e2e.server.mvc.provider.AbcViewProcessor;
import org.glassfish.jersey.tests.e2e.server.mvc.provider.DefViewProcessor;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Michal Gajdos
 */
public class ExplicitProduceTemplateTest extends JerseyTest {

    @Override
    protected Application configure() {
        enable(TestProperties.DUMP_ENTITY);
        enable(TestProperties.LOG_TRAFFIC);

        return new ResourceConfig(ExplicitTwoGetProducesTemplate.class,
                ExplicitNoProducesTemplate.class, ExplicitWildcardProducesTemplate.class, ExplicitTemplateProducesClass.class)
                .register(MvcFeature.class)
                .register(AbcViewProcessor.class)
                .register(DefViewProcessor.class);
    }

    @Path("/explicit-no-produces")
    public static class ExplicitNoProducesTemplate {

        @GET
        @Template
        public String def() {
            return "def";
        }
    }

    @Path("/explicit-wildcard-produces")
    public static class ExplicitWildcardProducesTemplate {

        @GET
        @Template
        @Produces("*/*")
        public String def() {
            return "def";
        }
    }

    @Path("/explicit-two-get-produces")
    public static class ExplicitTwoGetProducesTemplate {

        @GET
        @Template
        @Produces("application/abc")
        public String abc() {
            return "abc";
        }

        @GET
        @Template
        @Produces("*/*")
        public String def() {
            return "def";
        }
    }

    @Path("explicitTemplateProducesClass")
    @Produces("application/abc")
    public static class ExplicitTemplateProducesClass extends ExplicitTemplateTest.ExplicitTemplate {
    }

    @Test
    public void testProducesWildcard() throws Exception {
        for (final String path : new String[] {"explicit-no-produces", "explicit-wildcard-produces",
                "explicit-two-get-produces"}) {
            final WebTarget target = target(path);

            for (final String mediaType : new String[] {"application/def", "text/plain"}) {
                final Properties p = new Properties();
                p.load(target.request(mediaType).get(InputStream.class));

                assertTrue(p.getProperty("path")
                        .matches("/org/glassfish/jersey/tests/e2e/server/mvc/ExplicitProduceTemplateTest/Explicit([a-zA-Z]+)"
                                + "Template/index.def"));
                assertEquals("def", p.getProperty("model"));
                assertEquals("DefViewProcessor", p.getProperty("name"));
            }
        }
    }

    @Test
    public void testProducesSpecific() throws Exception {
        final WebTarget target = target("explicit-two-get-produces");

        final Properties p = new Properties();
        p.load(target.request("application/abc").get(InputStream.class));

        assertEquals(
                "/org/glassfish/jersey/tests/e2e/server/mvc/ExplicitProduceTemplateTest/ExplicitTwoGetProducesTemplate/index.abc",
                p.getProperty("path"));
        assertEquals("abc", p.getProperty("model"));
        assertEquals("AbcViewProcessor", p.getProperty("name"));
    }

    @Test
    public void testExplicitTemplateProducesClass() throws Exception {
        final WebTarget target = target("explicitTemplateProducesClass");

        Properties props = new Properties();
        props.load(target.request().get(InputStream.class));
        assertEquals(
                "/org/glassfish/jersey/tests/e2e/server/mvc/ExplicitProduceTemplateTest/ExplicitTemplateProducesClass/index.abc",
                props.getProperty("path"));
        assertEquals("method", props.getProperty("model"));

        props = new Properties();
        props.load(target.path("methodRelativePath").request().get(InputStream.class));
        assertEquals(
                "/org/glassfish/jersey/tests/e2e/server/mvc/ExplicitProduceTemplateTest/ExplicitTemplateProducesClass/relative"
                        + ".abc",
                props.getProperty("path"));
        assertEquals("methodRelativePath", props.getProperty("model"));

        props = new Properties();
        props.load(target.path("methodAbsolutePath").request().get(InputStream.class));
        assertEquals("/org/glassfish/jersey/tests/e2e/server/mvc/ExplicitTemplateTest/ExplicitTemplate/absolute.abc",
                props.getProperty("path"));
        assertEquals("methodAbsolutePath", props.getProperty("model"));
    }
}
