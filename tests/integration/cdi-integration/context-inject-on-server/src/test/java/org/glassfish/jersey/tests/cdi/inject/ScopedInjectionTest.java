/*
 * Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.cdi.inject;

import org.glassfish.jersey.inject.hk2.Hk2InjectionManagerFactory;
import org.glassfish.jersey.servlet.ServletProperties;
import org.glassfish.jersey.test.DeploymentContext;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.ServletDeploymentContext;
import org.glassfish.jersey.test.grizzly.GrizzlyWebTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.glassfish.jersey.test.spi.TestContainerFactory;
import org.jboss.weld.environment.se.Weld;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

public class ScopedInjectionTest extends JerseyTest {
    private Weld weld;

    @Before
    public void setup() {
        Assume.assumeTrue(Hk2InjectionManagerFactory.isImmediateStrategy());
    }

    @Override
    public void setUp() throws Exception {
        if (Hk2InjectionManagerFactory.isImmediateStrategy()) {
            weld = new Weld();
            weld.initialize();
            super.setUp();
        }
    }

    @Override
    public void tearDown() throws Exception {
        if (Hk2InjectionManagerFactory.isImmediateStrategy()) {
            weld.shutdown();
            super.tearDown();
        }
    }

    @Override
    protected Application configure() {
        return new ScopedApplication();
    }

    @Override
    protected TestContainerFactory getTestContainerFactory() throws TestContainerException {
        return new GrizzlyWebTestContainerFactory();
    }

    @Override
    protected DeploymentContext configureDeployment() {
        return ServletDeploymentContext.builder(configure())
                .initParam(ServletProperties.JAXRS_APPLICATION_CLASS, ScopedApplication.class.getName())
                .build();
    }

    @Test
    public void testIsInjectedOnResource() {
        try (Response r = target(InjectionChecker.ROOT).path("scope").path("injected").request()
                .header(InjectionChecker.HEADER, InjectionChecker.HEADER).get()) {
            System.out.println(r.readEntity(String.class));
            Assert.assertEquals(Response.Status.OK.getStatusCode(), r.getStatus());
        }
    }

    @Test
    public void testIsContextedOnResource() {
        try (Response r = target(InjectionChecker.ROOT).path("scope").path("contexted").request()
                .header(InjectionChecker.HEADER, InjectionChecker.HEADER).get()) {
            System.out.println(r.readEntity(String.class));
            Assert.assertEquals(Response.Status.OK.getStatusCode(), r.getStatus());
        }
    }

    @Test
    public void testNegativeContext() {
        try (Response r = target(InjectionChecker.ROOT).path("nonexisting").path("contexted").request()
                .header(InjectionChecker.HEADER, InjectionChecker.HEADER).get()) {
            System.out.println(r.readEntity(String.class));
            Assert.assertEquals(Response.Status.EXPECTATION_FAILED.getStatusCode(), r.getStatus());
        }
    }

    @Test
    public void testNegativeInject() {
        try (Response r = target(InjectionChecker.ROOT).path("nonexisting").path("injected").request()
                .header(InjectionChecker.HEADER, InjectionChecker.HEADER).get()) {
            System.out.println(r.readEntity(String.class));
            Assert.assertEquals(Response.Status.EXPECTATION_FAILED.getStatusCode(), r.getStatus());
        }
    }

    @Test
    public void testIsInjectedMapper() {
        try (Response r = target(InjectionChecker.ROOT).path("scope").path("iae").path("injected").request()
                .header(InjectionChecker.HEADER, InjectionChecker.HEADER).get()) {
            System.out.println(r.readEntity(String.class));
            Assert.assertEquals(Response.Status.OK.getStatusCode(), r.getStatus());
        }
    }

    @Test
    public void testIsContextedMapper() {
        try (Response r = target(InjectionChecker.ROOT).path("scope").path("iae").path("contexted").request()
                .header(InjectionChecker.HEADER, InjectionChecker.HEADER).get()) {
            System.out.println(r.readEntity(String.class));
            Assert.assertEquals(Response.Status.OK.getStatusCode(), r.getStatus());
        }
    }
}
