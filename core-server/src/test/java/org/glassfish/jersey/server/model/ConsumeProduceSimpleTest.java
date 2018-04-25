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

package org.glassfish.jersey.server.model;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.ApplicationHandler;
import org.glassfish.jersey.server.ContainerResponse;
import org.glassfish.jersey.server.RequestContextBuilder;
import org.glassfish.jersey.server.ResourceConfig;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Taken from Jersey 1: jersey-tests:com.sun.jersey.impl.resource.ConsumeProduceSimpleTest.java
 *
 * @author Paul Sandoz
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
public class ConsumeProduceSimpleTest {

    private ApplicationHandler createApplication(Class<?>... classes) {
        return new ApplicationHandler(new ResourceConfig(classes));
    }

    @Path("/{arg1}/{arg2}")
    @Consumes("text/html")
    public static class ConsumeSimpleBean {

        @Context
        HttpHeaders httpHeaders;

        @POST
        public String doPostHtml(String data) {
            assertEquals("text/html", httpHeaders.getRequestHeader("Content-Type").get(0));
            return "HTML";
        }

        @POST
        @Consumes("text/xhtml")
        public String doPostXHtml(String data) {
            assertEquals("text/xhtml", httpHeaders.getRequestHeader("Content-Type").get(0));
            return "XHTML";
        }
    }

    @Path("/{arg1}/{arg2}")
    @Produces("text/html")
    public static class ProduceSimpleBean {

        @Context
        HttpHeaders httpHeaders;

        @GET
        public String doGetHtml() {
            assertEquals("text/html", httpHeaders.getRequestHeader("Accept").get(0));
            return "HTML";
        }

        @GET
        @Produces("text/xhtml")
        public String doGetXhtml() {
            assertEquals("text/xhtml", httpHeaders.getRequestHeader("Accept").get(0));
            return "XHTML";
        }
    }

    @Path("/{arg1}/{arg2}")
    @Consumes("text/html")
    @Produces("text/html")
    public static class ConsumeProduceSimpleBean {

        @Context
        HttpHeaders httpHeaders;

        @GET
        public String doGetHtml() {
            assertEquals("text/html", httpHeaders.getRequestHeader("Accept").get(0));
            return "HTML";
        }

        @GET
        @Produces("text/xhtml")
        public String doGetXhtml() {
            assertEquals("text/xhtml", httpHeaders.getRequestHeader("Accept").get(0));
            return "XHTML";
        }

        @POST
        @SuppressWarnings("UnusedParameters")
        public String doPostHtml(String data) {
            assertEquals("text/html", httpHeaders.getRequestHeader("Content-Type").get(0));
            assertEquals("text/html", httpHeaders.getRequestHeader("Accept").get(0));
            return "HTML";
        }

        @POST
        @Consumes("text/xhtml")
        @Produces("text/xhtml")
        @SuppressWarnings("UnusedParameters")
        public String doPostXHtml(String data) {
            assertEquals("text/xhtml", httpHeaders.getRequestHeader("Content-Type").get(0));
            assertEquals("text/xhtml", httpHeaders.getRequestHeader("Accept").get(0));
            return "XHTML";
        }
    }

    @Test
    public void testConsumeSimpleBean() throws Exception {
        ApplicationHandler app = createApplication(ConsumeSimpleBean.class);

        assertEquals("HTML",
                app.apply(RequestContextBuilder.from("/a/b", "POST").entity("").type("text/html").build()).get().getEntity());
        assertEquals("XHTML",
                app.apply(RequestContextBuilder.from("/a/b", "POST").entity("").type("text/xhtml").build()).get().getEntity());
    }

    @Test
    public void testProduceSimpleBean() throws Exception {
        ApplicationHandler app = createApplication(ProduceSimpleBean.class);

        assertEquals("HTML", app.apply(RequestContextBuilder.from("/a/b", "GET").accept("text/html").build()).get().getEntity());
        assertEquals("XHTML",
                app.apply(RequestContextBuilder.from("/a/b", "GET").accept("text/xhtml").build()).get().getEntity());
    }

    @Test
    public void testConsumeProduceSimpleBean() throws Exception {
        ApplicationHandler app = createApplication(ConsumeProduceSimpleBean.class);

        assertEquals("HTML",
                app.apply(RequestContextBuilder.from("/a/b", "POST").entity("").type("text/html").accept("text/html").build())
                        .get().getEntity());
        assertEquals("XHTML",
                app.apply(RequestContextBuilder.from("/a/b", "POST").entity("").type("text/xhtml").accept("text/xhtml").build())
                        .get().getEntity());
        assertEquals("HTML", app.apply(RequestContextBuilder.from("/a/b", "GET").accept("text/html").build()).get().getEntity());
        assertEquals("XHTML",
                app.apply(RequestContextBuilder.from("/a/b", "GET").accept("text/xhtml").build()).get().getEntity());
    }

    @Path("/")
    @Consumes("text/html")
    @Produces("text/plain")
    public static class ConsumeProduceWithParameters {

        @Context
        HttpHeaders h;

        @POST
        @SuppressWarnings("UnusedParameters")
        public String post(String in) {
            return h.getMediaType().getParameters().toString();
        }
    }

    @Test
    public void testProduceWithParameters() throws Exception {
        ApplicationHandler app = createApplication(ConsumeProduceWithParameters.class);

        assertEquals("{a=b, c=d}", app.apply(
                RequestContextBuilder.from("/", "POST").entity("<html>content</html>").type("text/html;a=b;c=d").build()).get()
                .getEntity());
    }

    @Path("/")
    public static class ImplicitProducesResource {

        @GET
        public Response getPlain() {
            return Response.ok("text/plain").header("HEAD", "text-plain").build();
        }

        @GET
        @Produces(value = "text/html")
        public Response getHtml() {
            return Response.ok("<html></html>").header("HEAD", "text-html").build();
        }
    }

    @Test
    public void testImplicitProduces() throws Exception {
        final ApplicationHandler application = createApplication(ImplicitProducesResource.class);
        final ContainerResponse response = application
                .apply(RequestContextBuilder.from("/", "GET").accept(MediaType.TEXT_PLAIN_TYPE).build()).get();

        assertEquals("text/plain", response.getEntity());
        assertEquals("text-plain", response.getHeaderString("HEAD"));
    }
}
