/*
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.test.jetty;

import java.net.URI;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.inject.hk2.DelayedHk2InjectionManager;
import org.glassfish.jersey.inject.hk2.ImmediateHk2InjectionManager;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.jetty.JettyHttpContainer;
import org.glassfish.jersey.jetty.JettyHttpContainerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.glassfish.hk2.api.ServiceLocator;

import org.jvnet.hk2.internal.ServiceLocatorImpl;

import org.eclipse.jetty.server.Server;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test class for {@link JettyHttpContainer}.
 *
 * @author Arul Dhesiaseelan (aruld at acm org)
 * @author Miroslav Fuksa
 */
public class JettyContainerTest extends JerseyTest {

    /**
     * Creates new instance.
     */
    public JettyContainerTest() {
        super(new JettyTestContainerFactory());
    }

    @Override
    protected ResourceConfig configure() {
        return new ResourceConfig(Resource.class);
    }

    /**
     * Test resource class.
     */
    @Path("one")
    public static class Resource {

        /**
         * Test resource method.
         *
         * @return Test simple string response.
         */
        @GET
        public String getSomething() {
            return "get";
        }
    }

    @Test
    /**
     * Test {@link Server Jetty Server} container.
     */
    public void testJettyContainerTarget() {
        final Response response = target().path("one").request().get();

        assertEquals("Response status unexpected.", 200, response.getStatus());
        assertEquals("Response entity unexpected.", "get", response.readEntity(String.class));
    }

    /**
     * Test that defined ServiceLocator becomes a parent of the newly created service locator.
     */
    @Test
    public void testParentServiceLocator() {
        final ServiceLocator locator = new ServiceLocatorImpl("MyServiceLocator", null);
        final Server server = JettyHttpContainerFactory.createServer(URI.create("http://localhost:9876"),
                new ResourceConfig(Resource.class), false, locator);
        JettyHttpContainer container = (JettyHttpContainer) server.getHandler();
        InjectionManager injectionManager = container.getApplicationHandler().getInjectionManager();

        ServiceLocator serviceLocator;
        if (injectionManager instanceof ImmediateHk2InjectionManager) {
            serviceLocator = ((ImmediateHk2InjectionManager) injectionManager).getServiceLocator();
        } else if (injectionManager instanceof DelayedHk2InjectionManager) {
            serviceLocator = ((DelayedHk2InjectionManager) injectionManager).getServiceLocator();
        } else {
            throw new RuntimeException("Invalid Hk2 InjectionManager");
        }
        assertTrue("Application injection manager was expected to have defined parent locator",
                   serviceLocator.getParent() == locator);
    }
}
