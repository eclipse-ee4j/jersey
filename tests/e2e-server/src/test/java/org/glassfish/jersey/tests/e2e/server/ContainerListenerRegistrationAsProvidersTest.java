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

package org.glassfish.jersey.tests.e2e.server;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.spi.AbstractContainerLifecycleListener;
import org.glassfish.jersey.server.spi.Container;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
public class ContainerListenerRegistrationAsProvidersTest extends JerseyTest {

    MyListener listener;

    @Override
    public ResourceConfig configure() {
        listener = new MyListener();
        final ResourceConfig result = new ResourceConfig(One.class, YetAnotherListener.class).registerInstances(listener);
        return result;
    }

    static class YetAnotherListener extends AbstractContainerLifecycleListener {

        static boolean started;

        @Override
        public void onStartup(Container container) {
            started = true;
        }
    }

    static class MyListener extends AbstractContainerLifecycleListener {

        boolean startupInvoked;

        @Override
        public void onStartup(Container container) {
            startupInvoked = true;
        }
    }

    @Test
    public void testListener() {
        assertEquals("whatever", target().path("doesNotMatter").request().get().readEntity(String.class));
        assertTrue(listener.startupInvoked);
        assertTrue(YetAnotherListener.started);
    }

    @Path("doesNotMatter")
    public static class One {
        @GET
        public String get() {
            return "whatever";
        }
    }
}
