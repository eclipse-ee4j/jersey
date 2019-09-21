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
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.Path;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.mvc.ErrorTemplate;
import org.glassfish.jersey.server.mvc.MvcFeature;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.glassfish.jersey.tests.e2e.server.mvc.provider.TestViewProcessor;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * @author Michal Gajdos
 */
public class ErrorTemplateTest extends JerseyTest {

    @Override
    protected Application configure() {
        enable(TestProperties.DUMP_ENTITY);
        enable(TestProperties.LOG_TRAFFIC);

        return new ResourceConfig(ErrorTemplateResource.class)
                .register(MvcFeature.class)
                .register(TestViewProcessor.class);
    }

    @Path("/")
    public static class ErrorTemplateResource {

        @GET
        @ErrorTemplate
        public String method() {
            throw new RuntimeException("ErrorTemplate");
        }

        @GET
        @Path("methodRelativePath")
        @ErrorTemplate(name = "relative")
        public String methodRelativePath() {
            throw new RuntimeException("ErrorTemplate");
        }

        @GET
        @Path("methodAbsolutePath")
        @ErrorTemplate(name = "/org/glassfish/jersey/tests/e2e/server/mvc/ErrorTemplateTest/ErrorTemplateResource/absolute")
        public String methodAbsolutePath() {
            throw new RuntimeException("ErrorTemplate");
        }

        @Path("subResource")
        public ErrorTemplateResource subResource() {
            return new ErrorTemplateResource();
        }

        @Path("subResourceTemplate")
        @ErrorTemplate
        public ErrorTemplateSubResource subResourceTemplate() {
            return new ErrorTemplateSubResource();
        }
    }

    public static class ErrorTemplateSubResource {

        @GET
        public String get() {
            throw new RuntimeException("ErrorTemplate");
        }
    }

    @Test(expected = InternalServerErrorException.class)
    public void testErrorMethodTemplateSubResource() throws Exception {
        target("subResourceTemplate").request().get(String.class);
    }

    @Test
    public void testErrorTemplate() throws Exception {
        testErrorTemplate(target());
    }

    @Test
    public void testErrorTemplateSubResource() throws Exception {
        testErrorTemplate(target("subResource"));
    }

    private void testErrorTemplate(final WebTarget target) throws Exception {
        Properties props = new Properties();
        props.load(target.request().get(InputStream.class));
        assertEquals("/org/glassfish/jersey/tests/e2e/server/mvc/ErrorTemplateTest/ErrorTemplateResource/index.testp",
                props.getProperty("path"));
        assertEquals("java.lang.RuntimeException: ErrorTemplate", props.getProperty("model"));

        props = new Properties();
        props.load(target.path("methodRelativePath").request().get(InputStream.class));
        assertEquals("/org/glassfish/jersey/tests/e2e/server/mvc/ErrorTemplateTest/ErrorTemplateResource/relative.testp",
                props.getProperty("path"));
        assertEquals("java.lang.RuntimeException: ErrorTemplate", props.getProperty("model"));

        props = new Properties();
        props.load(target.path("methodAbsolutePath").request().get(InputStream.class));
        assertEquals("/org/glassfish/jersey/tests/e2e/server/mvc/ErrorTemplateTest/ErrorTemplateResource/absolute.testp",
                props.getProperty("path"));
        assertEquals("java.lang.RuntimeException: ErrorTemplate", props.getProperty("model"));
    }
}
