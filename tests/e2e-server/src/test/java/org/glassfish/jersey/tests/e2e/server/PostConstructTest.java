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

package org.glassfish.jersey.tests.e2e.server;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import javax.annotation.PostConstruct;

import org.glassfish.jersey.inject.hk2.Hk2InjectionManagerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.DeploymentContext;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Testing that {@link Context injection} is done before invoking method annotated with {@link PostConstruct}.
 *
 * @author Michal Gajdos
 */
public class PostConstructTest extends JerseyTest {

    @Path("/")
    public static class Resource {

        private int value;

        @Context
        private UriInfo uri;

        @Context
        private Configuration configuration;

        @PostConstruct
        public void postConstruct() {
            value = configuration != null ? 1 : 0;
        }

        @GET
        public String get() {
            return "value=" + value + "|" + configuration.getProperty("value");
        }
    }

    public static class MyApplication extends Application {

        private int value;

        @Context
        private UriInfo uriInfo;

        @PostConstruct
        public void postConstruct() {
            value = uriInfo != null ? 1 : 0;
        }

        @Override
        public Set<Class<?>> getClasses() {
            return Collections.singleton(Resource.class);
        }

        @Override
        public Map<String, Object> getProperties() {
            final HashMap<String, Object> map = new HashMap<>();
            map.put("value", value);
            return map;
        }
    }

    @Before
    public void setup() {
        Assume.assumeTrue(Hk2InjectionManagerFactory.isImmediateStrategy());
    }

    @Override
    protected DeploymentContext configureDeployment() {
        // If strategy is not IMMEDIATE then test will fail even before @Before setup method invocation.
        // It has no other reason then just run the tests in IMMEDIATE strategy.
        if (Hk2InjectionManagerFactory.isImmediateStrategy()) {
            return DeploymentContext.newInstance(MyApplication.class);
        } else {
            return DeploymentContext.newInstance(new ResourceConfig());
        }
    }

    @Test
    public void testApplicationResourcePostConstruct() throws Exception {
        assertEquals("value=1|1", target().request().get(String.class));
    }
}
