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

package org.glassfish.jersey.test.grizzly.web;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

import javax.inject.Singleton;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.test.DeploymentContext;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.ServletDeploymentContext;
import org.glassfish.jersey.test.grizzly.GrizzlyWebTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerFactory;

import org.junit.Test;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Test injection support in the {@link org.glassfish.jersey.test.grizzly.GrizzlyWebTestContainerFactory}.
 *
 * @author Pavel Bucek (pavel.bucek at oracle.com)
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public class GrizzlyWebInjectionTest extends JerseyTest {

    @Path("fields")
    public static class FieldsResource {

        @Context
        private HttpServletRequest request;
        @Context
        private HttpServletResponse response;

        @Context
        private ServletConfig config;
        @Context
        private ServletContext context;

        @GET
        public String get() {
            return testInjections(request, response, config, context);
        }
    }

    @Path("singleton/fields")
    @Singleton
    public static class SingletonFieldsResource {

        @Context
        private HttpServletRequest request;
        @Context
        private HttpServletResponse response;

        @Context
        private ServletConfig config;
        @Context
        private ServletContext context;

        @GET
        public String get() {
            return testInjections(request, response, config, context);
        }
    }

    @Path("/constructor")
    public static class ConstructorResource {

        private HttpServletRequest request;
        private HttpServletResponse response;

        private ServletConfig config;
        private ServletContext context;

        public ConstructorResource(
                @Context HttpServletRequest req,
                @Context HttpServletResponse res,
                @Context ServletConfig sconf,
                @Context ServletContext scont) {
            this.request = req;
            this.response = res;
            this.config = sconf;
            this.context = scont;
        }

        @GET
        public String get() {
            return testInjections(request, response, config, context);
        }
    }

    private static String testInjections(final HttpServletRequest request, final HttpServletResponse response,
                                  final ServletConfig config, final ServletContext context) {
        if (config != null && context != null
                && request != null && response != null
                && config.getInitParameter(ServerProperties.PROVIDER_PACKAGES)
                         .equals(GrizzlyWebInjectionTest.class.getPackage().getName())) {
            return "SUCCESS";
        } else {
            return "FAIL";
        }
    }

    @Override
    protected DeploymentContext configureDeployment() {
        return ServletDeploymentContext.builder(new ResourceConfig())
                .initParam(ServerProperties.PROVIDER_PACKAGES, this.getClass().getPackage().getName())
                .build();
    }

    @Override
    protected TestContainerFactory getTestContainerFactory() {
        return new GrizzlyWebTestContainerFactory();
    }

    @Test
    public void testFields() {
        _test("fields");
    }

    @Test
    public void testSingletonFields() {
        _test("singleton/fields");
    }

    @Test
    public void testConstructor() {
        _test("constructor");
    }


    private void _test(final String path) {
        assertThat(target(path).request().get().readEntity(String.class), is("SUCCESS"));
    }
}
