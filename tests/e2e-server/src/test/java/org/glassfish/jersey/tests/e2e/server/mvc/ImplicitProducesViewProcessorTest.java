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
import javax.ws.rs.Produces;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.mvc.MvcFeature;
import org.glassfish.jersey.server.mvc.Template;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.glassfish.jersey.tests.e2e.server.mvc.provider.TestViewProcessor;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * @author Paul Sandoz
 * @author Michal Gajdos
 */
public class ImplicitProducesViewProcessorTest extends JerseyTest {

    @Override
    protected Application configure() {
        enable(TestProperties.DUMP_ENTITY);
        enable(TestProperties.LOG_TRAFFIC);

        return new ResourceConfig(
                ImplicitTemplate.class,
                ImplicitWithGetTemplate.class,
                ImplicitWithSubResourceGetTemplate.class)
                .register(MvcFeature.class)
                .register(TestViewProcessor.class).property("jersey.config.server.tracing", "ALL");
    }

    @Path("/implicit")
    @Template
    @Produces("text/plain;qs=.5")
    public static class ImplicitTemplate {

        public String toString() {
            return "ImplicitTemplate";
        }
    }

    @Test
    public void testImplicitTemplate() throws IOException {
        final WebTarget target = target("implicit");

        Properties p = new Properties();
        Response cr = target.request("text/plain", "application/foo").get(Response.class);
        assertEquals(MediaType.TEXT_PLAIN_TYPE, cr.getMediaType());
        p.load(cr.readEntity(InputStream.class));
        assertEquals("/org/glassfish/jersey/tests/e2e/server/mvc/ImplicitProducesViewProcessorTest/ImplicitTemplate/index.testp",
                p.getProperty("path"));
        assertEquals("ImplicitTemplate", p.getProperty("model"));

        p = new Properties();
        cr = target.request("application/foo", "text/plain").get(Response.class);
        assertEquals(MediaType.TEXT_PLAIN_TYPE, cr.getMediaType());
        p.load(cr.readEntity(InputStream.class));
        assertEquals("/org/glassfish/jersey/tests/e2e/server/mvc/ImplicitProducesViewProcessorTest/ImplicitTemplate/index.testp",
                p.getProperty("path"));
        assertEquals("ImplicitTemplate", p.getProperty("model"));

        p = new Properties();
        cr = target.request("text/plain;q=0.5", "application/foo").get(Response.class);
        assertEquals(MediaType.TEXT_PLAIN_TYPE, cr.getMediaType());
        p.load(cr.readEntity(InputStream.class));
        assertEquals("/org/glassfish/jersey/tests/e2e/server/mvc/ImplicitProducesViewProcessorTest/ImplicitTemplate/index.testp",
                p.getProperty("path"));
        assertEquals("ImplicitTemplate", p.getProperty("model"));

        p = new Properties();
        cr = target.request("application/foo", "text/plain;q=0.5").get(Response.class);
        assertEquals(MediaType.TEXT_PLAIN_TYPE, cr.getMediaType());
        p.load(cr.readEntity(InputStream.class));
        assertEquals("/org/glassfish/jersey/tests/e2e/server/mvc/ImplicitProducesViewProcessorTest/ImplicitTemplate/index.testp",
                p.getProperty("path"));
        assertEquals("ImplicitTemplate", p.getProperty("model"));
    }

    @Path("/implicit-get")
    @Template
    @Produces("text/plain;qs=0.5")
    public static class ImplicitWithGetTemplate {

        @GET
        @Produces("application/foo;qs=0.2")
        public String toString() {
            return "ImplicitWithGetTemplate";
        }
    }

    @Test
    public void testImplicitWithGetTemplate() throws IOException {
        final WebTarget target = target("implicit-get");

        Properties p = new Properties();
        Response cr = target.request("text/plain", "application/foo").get(Response.class);
        assertEquals(MediaType.TEXT_PLAIN_TYPE, cr.getMediaType());
        p.load(cr.readEntity(InputStream.class));
        assertEquals(
                "/org/glassfish/jersey/tests/e2e/server/mvc/ImplicitProducesViewProcessorTest/ImplicitWithGetTemplate/index"
                        + ".testp",
                p.getProperty("path"));
        assertEquals("ImplicitWithGetTemplate", p.getProperty("model"));

        p = new Properties();
        cr = target.request("application/foo", "text/plain").get(Response.class);
        assertEquals(MediaType.TEXT_PLAIN_TYPE, cr.getMediaType());
        p.load(cr.readEntity(InputStream.class));
        assertEquals(
                "/org/glassfish/jersey/tests/e2e/server/mvc/ImplicitProducesViewProcessorTest/ImplicitWithGetTemplate/index"
                        + ".testp",
                p.getProperty("path"));
        assertEquals("ImplicitWithGetTemplate", p.getProperty("model"));

        p = new Properties();
        cr = target.request("text/plain", "application/foo;q=0.5").get(Response.class);
        assertEquals(MediaType.TEXT_PLAIN_TYPE, cr.getMediaType());
        p.load(cr.readEntity(InputStream.class));
        assertEquals(
                "/org/glassfish/jersey/tests/e2e/server/mvc/ImplicitProducesViewProcessorTest/ImplicitWithGetTemplate/index"
                        + ".testp",
                p.getProperty("path"));
        assertEquals("ImplicitWithGetTemplate", p.getProperty("model"));

        p = new Properties();
        cr = target.request("application/foo;q=0.5", "text/plain").get(Response.class);
        assertEquals(MediaType.TEXT_PLAIN_TYPE, cr.getMediaType());
        p.load(cr.readEntity(InputStream.class));
        assertEquals(
                "/org/glassfish/jersey/tests/e2e/server/mvc/ImplicitProducesViewProcessorTest/ImplicitWithGetTemplate/index"
                        + ".testp",
                p.getProperty("path"));
        assertEquals("ImplicitWithGetTemplate", p.getProperty("model"));

        p = new Properties();
        cr = target.request("*/*").get(Response.class);
        assertEquals(MediaType.TEXT_PLAIN_TYPE, cr.getMediaType());
        p.load(cr.readEntity(InputStream.class));
        assertEquals(
                "/org/glassfish/jersey/tests/e2e/server/mvc/ImplicitProducesViewProcessorTest/ImplicitWithGetTemplate/index"
                        + ".testp",
                p.getProperty("path"));
        assertEquals("ImplicitWithGetTemplate", p.getProperty("model"));

        cr = target.request("text/plain;q=0.5", "application/foo").get(Response.class);
        assertEquals(new MediaType("application", "foo"), cr.getMediaType());
        assertEquals("ImplicitWithGetTemplate", cr.readEntity(String.class));
    }

    @Path("/implicit-get-subresource")
    @Template
    @Produces("text/plain;qs=0.5")
    public static class ImplicitWithSubResourceGetTemplate {

        @GET
        @Path("sub")
        @Produces("application/foo;qs=0.2")
        public String toString() {
            return "ImplicitWithSubResourceGetTemplate";
        }
    }

    @Test
    public void testImplicitWithSubResourceGetTemplate() throws IOException {
        final WebTarget target = target("implicit-get-subresource").path("sub");

        Properties p = new Properties();
        Response cr = target.request("text/plain", "application/foo").get(Response.class);
        assertEquals(MediaType.TEXT_PLAIN_TYPE, cr.getMediaType());
        p.load(cr.readEntity(InputStream.class));
        assertEquals(
                "/org/glassfish/jersey/tests/e2e/server/mvc/ImplicitProducesViewProcessorTest"
                        + "/ImplicitWithSubResourceGetTemplate/sub.testp",
                p.getProperty("path"));
        assertEquals("ImplicitWithSubResourceGetTemplate", p.getProperty("model"));

        p = new Properties();
        cr = target.request("application/foo", "text/plain").get(Response.class);
        assertEquals(MediaType.TEXT_PLAIN_TYPE, cr.getMediaType());
        p.load(cr.readEntity(InputStream.class));
        assertEquals(
                "/org/glassfish/jersey/tests/e2e/server/mvc/ImplicitProducesViewProcessorTest"
                        + "/ImplicitWithSubResourceGetTemplate/sub.testp",
                p.getProperty("path"));
        assertEquals("ImplicitWithSubResourceGetTemplate", p.getProperty("model"));

        p = new Properties();
        cr = target.request("text/plain", "application/foo;q=0.5").get(Response.class);
        assertEquals(MediaType.TEXT_PLAIN_TYPE, cr.getMediaType());
        p.load(cr.readEntity(InputStream.class));
        assertEquals(
                "/org/glassfish/jersey/tests/e2e/server/mvc/ImplicitProducesViewProcessorTest"
                        + "/ImplicitWithSubResourceGetTemplate/sub.testp",
                p.getProperty("path"));
        assertEquals("ImplicitWithSubResourceGetTemplate", p.getProperty("model"));

        p = new Properties();
        cr = target.request("application/foo;q=0.5", "text/plain").get(Response.class);
        assertEquals(MediaType.TEXT_PLAIN_TYPE, cr.getMediaType());
        p.load(cr.readEntity(InputStream.class));
        assertEquals(
                "/org/glassfish/jersey/tests/e2e/server/mvc/ImplicitProducesViewProcessorTest"
                        + "/ImplicitWithSubResourceGetTemplate/sub.testp",
                p.getProperty("path"));
        assertEquals("ImplicitWithSubResourceGetTemplate", p.getProperty("model"));

        assertEquals("ImplicitWithSubResourceGetTemplate", target.request("application/foo").get(String.class));
    }

}
