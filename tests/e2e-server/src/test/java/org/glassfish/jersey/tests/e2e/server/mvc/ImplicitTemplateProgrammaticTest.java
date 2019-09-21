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
import java.lang.reflect.Method;
import java.util.Properties;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.Application;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.model.Resource;
import org.glassfish.jersey.server.mvc.MvcFeature;
import org.glassfish.jersey.server.mvc.Template;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.tests.e2e.server.mvc.provider.TestViewProcessor;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * @author Michal Gajdos
 */
public class ImplicitTemplateProgrammaticTest extends JerseyTest {

    @Template
    public static class Handler {

        private String constructor = "no-arg";

        @SuppressWarnings("UnusedDeclaration")
        public Handler() {
        }

        public Handler(final String constructor) {
            this.constructor = constructor;
        }

        @Override
        public String toString() {
            return "Resource_" + constructor;
        }
    }

    @Override
    protected Application configure() {
        Method toStringMethod = null;

        try {
            toStringMethod = Handler.class.getMethod("toString");
        } catch (NoSuchMethodException e) {
            // Eat that.
        }

        final Resource.Builder resourceBuilder = Resource.builder("implicit");

        resourceBuilder.addMethod(HttpMethod.POST).consumes("application/foo").handledBy(Handler.class, toStringMethod);
        resourceBuilder.addMethod(HttpMethod.POST).consumes("application/bar").handledBy(new Handler("arg"), toStringMethod);

        return new ResourceConfig()
                .registerResources(resourceBuilder.build())
                .register(MvcFeature.class)
                .register(TestViewProcessor.class);
    }

    @Test
    public void testImplicitHandlerClass() throws Exception {
        Properties p = new Properties();
        p.load(target("implicit").request().get(InputStream.class));
        assertEquals("/org/glassfish/jersey/tests/e2e/server/mvc/ImplicitTemplateProgrammaticTest/Handler/index.testp",
                p.getProperty("path"));
        assertEquals("Resource_no-arg", p.getProperty("model"));
    }
}
