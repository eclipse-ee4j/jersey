/*
 * Copyright (c) 2025 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.test.helidon;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.helidon.HelidonHttpContainer;
import org.glassfish.jersey.helidon.HelidonHttpContainerBuilder;
import org.glassfish.jersey.inject.hk2.DelayedHk2InjectionManager;
import org.glassfish.jersey.inject.hk2.ImmediateHk2InjectionManager;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.Test;
import org.jvnet.hk2.internal.ServiceLocatorImpl;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test class for {@link HelidonHttpContainer}.
 *
 * @author Arul Dhesiaseelan (aruld at acm org)
 * @author Miroslav Fuksa
 */
public class HelidonContainerTest extends JerseyTest {

    /**
     * Creates new instance.
     */
    public HelidonContainerTest() {
        super(new HelidonTestContainerFactory());
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
     * Test {@link Server Helidon Server} container.
     */
    public void testHelidonContainerTarget() {
        final Response response = target().path("one").request().get();

        assertEquals(200, response.getStatus(), "Response status unexpected.");
        assertEquals("get", response.readEntity(String.class), "Response entity unexpected.");
    }

    /**
     * Test that defined ServiceLocator becomes a parent of the newly created service locator.
     */
    @Test
    public void testParentServiceLocator() {
        final ServiceLocator locator = new ServiceLocatorImpl("MyServiceLocator", null);
        final HelidonHttpContainer container = HelidonHttpContainerBuilder.builder().uri(URI.create("http://localhost:9876"))
                .application(new ResourceConfig(Resource.class)).parentContext(locator).build();
        InjectionManager injectionManager = container.getApplicationHandler().getInjectionManager();

        ServiceLocator serviceLocator;
        if (injectionManager instanceof ImmediateHk2InjectionManager) {
            serviceLocator = ((ImmediateHk2InjectionManager) injectionManager).getServiceLocator();
        } else if (injectionManager instanceof DelayedHk2InjectionManager) {
            serviceLocator = ((DelayedHk2InjectionManager) injectionManager).getServiceLocator();
        } else {
            throw new RuntimeException("Invalid Hk2 InjectionManager");
        }
        assertTrue(serviceLocator.getParent() == locator,
                   "Application injection manager was expected to have defined parent locator");
    }
}
