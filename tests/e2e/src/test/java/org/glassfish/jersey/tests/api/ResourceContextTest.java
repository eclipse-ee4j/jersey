/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.container.ResourceContext;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;

import javax.inject.Singleton;

import org.glassfish.jersey.inject.hk2.Hk2InjectionManagerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Test {@link ResourceContext}: resource context must provide access to
 * sub-resources that can be provided by a custom component provider.
 *
 * @author Martin Grotzke
 * @author Paul Sandoz
 * @author Marek Potociar (marek.potociar at oracle.com)
 * @author Miroslav Fuksa
 */
public class ResourceContextTest extends JerseyTest {

    @Before
    public void setup() {
        Assume.assumeTrue(Hk2InjectionManagerFactory.isImmediateStrategy());
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(MyRootResource.class);
    }

    @Path("/")
    public static class MyRootResource {

        @Context
        ResourceContext resourceContext;

        @Path("singleton")
        public SingletonResource getSingletonResource() {
            return resourceContext.getResource(SingletonResource.class);
        }

        @Path("perrequest")
        public PerRequestResource getPerRequestSubResource() {
            return resourceContext.getResource(PerRequestResource.class);
        }

        @Path("inject/{path}")
        public InjectResource getInjectResource() {
            final InjectResource resource = resourceContext.getResource(InjectResource.class);
            resource.setPath("something");
            return resourceContext.initResource(resource);
        }

        @Path("injectFromNewResource/{path}")
        public InjectResource getInjectResourceFromNew() {
            final InjectResource resource = new InjectResource();
            resource.setPath("something");
            return resourceContext.initResource(resource);
        }

    }

    public static class InjectResource {
        @PathParam("path")
        private String path;

        @GET
        public String get() {
            return path;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }
    }


    @Singleton
    public static class SingletonResource {
        int i;

        @GET
        public String get() {
            i++;
            return Integer.toString(i);
        }
    }

    public static class PerRequestResource {
        int i;

        @GET
        public String get() {
            i++;
            return Integer.toString(i);
        }
    }

    @Test
    public void testGetResourceFromResourceContext() {
        assertEquals("1", target("/singleton").request().get(String.class));
        assertEquals("2", target("/singleton").request().get(String.class));

        assertEquals("1", target("/perrequest").request().get(String.class));
        assertEquals("1", target("/perrequest").request().get(String.class));
    }


    @Test
    public void testInitializeResourceFromResourceContext() {
        assertEquals("aaa", target("/inject/aaa").request().get(String.class));
        assertEquals("bbb", target("/inject/bbb").request().get(String.class));
    }

    @Test
    public void testInitializeResourceFromNewResource() {
        assertEquals("aaa", target("/injectFromNewResource/aaa").request().get(String.class));
        assertEquals("bbb", target("/injectFromNewResource/bbb").request().get(String.class));
    }

}
