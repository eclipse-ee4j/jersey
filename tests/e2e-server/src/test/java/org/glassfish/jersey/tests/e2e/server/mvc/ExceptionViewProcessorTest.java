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
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.mvc.MvcFeature;
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
public class ExceptionViewProcessorTest extends JerseyTest {

    @Override
    protected Application configure() {
        enable(TestProperties.DUMP_ENTITY);
        enable(TestProperties.LOG_TRAFFIC);

        return new ResourceConfig(ExplicitTemplate.class)
                .register(MvcFeature.class)
                .register(TestViewProcessor.class)
                .register(WebAppExceptionMapper.class);
    }

    @Provider
    public static class WebAppExceptionMapper implements ExceptionMapper<WebApplicationException> {

        public Response toResponse(WebApplicationException exception) {
            // Absolute.
            if (exception.getResponse().getStatus() == 404) {
                return Response
                        .status(404)
                        .entity(new Viewable("/org/glassfish/jersey/tests/e2e/server/mvc/ExceptionViewProcessorTest/404", "404"))
                        .build();
            }

            // Relative.
            if (exception.getResponse().getStatus() == 406) {
                return Response.status(406).entity(
                        new Viewable(
                                "/org/glassfish/jersey/tests/e2e/server/mvc/ExceptionViewProcessorTest/WebAppExceptionMapper/406",
                                "406")).build();
            }

            return exception.getResponse();
        }
    }

    @Path("/")
    public static class ExplicitTemplate {

        @GET
        @Produces("application/foo")
        public Viewable get() {
            return new Viewable("show", "get");
        }
    }

    @Test
    public void testAbsoluteExplicitTemplate() throws IOException {
        final Invocation.Builder request = target("/does-not-exist").request();

        Response cr = request.get(Response.class);
        assertEquals(404, cr.getStatus());

        Properties p = new Properties();
        p.load(cr.readEntity(InputStream.class));
        assertEquals("/org/glassfish/jersey/tests/e2e/server/mvc/ExceptionViewProcessorTest/404.testp", p.getProperty("path"));
        assertEquals("404", p.getProperty("model"));
    }

    @Test
    public void testResolvingClassExplicitTemplate() throws IOException {
        final Invocation.Builder request = target("/").request("application/wrong-media-type");

        Response cr = request.get(Response.class);
        assertEquals(406, cr.getStatus());

        Properties p = new Properties();
        p.load(cr.readEntity(InputStream.class));
        assertEquals("/org/glassfish/jersey/tests/e2e/server/mvc/ExceptionViewProcessorTest/WebAppExceptionMapper/406.testp",
                p.getProperty("path"));
        assertEquals("406", p.getProperty("model"));
    }
}
