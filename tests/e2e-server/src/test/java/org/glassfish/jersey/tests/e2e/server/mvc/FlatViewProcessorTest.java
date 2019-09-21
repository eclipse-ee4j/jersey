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
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;

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
public class FlatViewProcessorTest extends JerseyTest {

    @Override
    protected Application configure() {
        enable(TestProperties.DUMP_ENTITY);
        enable(TestProperties.LOG_TRAFFIC);

        return new ResourceConfig(ExplicitTemplate.class, ImplicitTemplate.class, ImplicitExplicitTemplate.class,
                ImplicitWithGetTemplate.class, ImplicitWithSubResourceGetTemplate.class)
                .register(MvcFeature.class)
                .register(TestViewProcessor.class);
    }

    @Path("/explicit")
    public static class ExplicitTemplate {

        @GET
        public Viewable get() {
            return new Viewable("show", "get");
        }

        @POST
        public Viewable post() {
            return new Viewable("show", "post");
        }
    }

    @Test
    public void testExplicitTemplate() throws IOException {
        final Invocation.Builder request = target("explicit").request();

        Properties p = new Properties();
        p.load(request.get(InputStream.class));
        assertEquals("/org/glassfish/jersey/tests/e2e/server/mvc/FlatViewProcessorTest.ExplicitTemplate.show.testp",
                p.getProperty("path"));
        assertEquals("get", p.getProperty("model"));

        p = new Properties();
        p.load(request.post(Entity.entity("", MediaType.TEXT_PLAIN_TYPE), InputStream.class));
        assertEquals("/org/glassfish/jersey/tests/e2e/server/mvc/FlatViewProcessorTest.ExplicitTemplate.show.testp",
                p.getProperty("path"));
        assertEquals("post", p.getProperty("model"));
    }

    @Path("/implicit")
    @Template
    public static class ImplicitTemplate {

        public String toString() {
            return "ImplicitTemplate";
        }
    }

    @Test
    public void testImplicitTemplate() throws IOException {
        final Invocation.Builder request = target("implicit").request();

        Properties p = new Properties();
        p.load(request.get(InputStream.class));
        assertEquals("/org/glassfish/jersey/tests/e2e/server/mvc/FlatViewProcessorTest.ImplicitTemplate.index.testp",
                p.getProperty("path"));
        assertEquals("ImplicitTemplate", p.getProperty("model"));
    }

    @Path("/implicit-explicit")
    @Template
    public static class ImplicitExplicitTemplate {

        public String toString() {
            return "ImplicitExplicitTemplate";
        }

        @POST
        public Viewable post() {
            return new Viewable("show", "post");
        }

        @Path("sub")
        @GET
        public Viewable get() {
            return new Viewable("show", "get");
        }
    }

    @Test
    public void testImplicitExplicitTemplate() throws IOException {
        final Invocation.Builder request = target("implicit-explicit").request();

        Properties p = new Properties();
        p.load(request.get(InputStream.class));
        assertEquals("/org/glassfish/jersey/tests/e2e/server/mvc/FlatViewProcessorTest.ImplicitExplicitTemplate.index.testp",
                p.getProperty("path"));
        assertEquals("ImplicitExplicitTemplate", p.getProperty("model"));

        p = new Properties();
        p.load(request.post(Entity.entity("", MediaType.TEXT_PLAIN_TYPE), InputStream.class));
        assertEquals("/org/glassfish/jersey/tests/e2e/server/mvc/FlatViewProcessorTest.ImplicitExplicitTemplate.show.testp",
                p.getProperty("path"));
        assertEquals("post", p.getProperty("model"));

        p = new Properties();
        p.load(target("implicit-explicit").path("sub").request().get(InputStream.class));
        assertEquals("/org/glassfish/jersey/tests/e2e/server/mvc/FlatViewProcessorTest.ImplicitExplicitTemplate.show.testp",
                p.getProperty("path"));
        assertEquals("get", p.getProperty("model"));
    }

    @Path("/implicit-get")
    @Template
    public static class ImplicitWithGetTemplate {

        @GET
        @Produces("application/foo")
        public String toString() {
            return "ImplicitWithGetTemplate";
        }
    }

    @Test
    public void testImplicitWithGetTemplate() throws IOException {
        final WebTarget target = target("implicit-get");

        Properties p = new Properties();
        p.load(target.request("text/plain").get(InputStream.class));
        assertEquals("/org/glassfish/jersey/tests/e2e/server/mvc/FlatViewProcessorTest.ImplicitWithGetTemplate.index.testp",
                p.getProperty("path"));
        assertEquals("ImplicitWithGetTemplate", p.getProperty("model"));

        assertEquals("ImplicitWithGetTemplate", target.request("application/foo").get(String.class));
    }

    @Path("/implicit-get-subresource")
    @Template
    public static class ImplicitWithSubResourceGetTemplate {

        @Path("sub")
        @GET
        @Produces("application/foo")
        public String toString() {
            return "ImplicitWithSubResourceGetTemplate";
        }
    }

    @Test
    public void testImplicitWithSubResourceGetTemplate() throws IOException {
        final WebTarget target = target("implicit-get-subresource").path("sub");

        Properties p = new Properties();
        p.load(target.request("text/plain").get(InputStream.class));
        assertEquals(
                "/org/glassfish/jersey/tests/e2e/server/mvc/FlatViewProcessorTest.ImplicitWithSubResourceGetTemplate.sub.testp",
                p.getProperty("path"));
        assertEquals("ImplicitWithSubResourceGetTemplate", p.getProperty("model"));

        assertEquals("ImplicitWithSubResourceGetTemplate", target.request("application/foo").get(String.class));
    }

}
