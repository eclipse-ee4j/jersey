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

package org.glassfish.jersey.jdkhttp;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.spi.AbstractContainerLifecycleListener;
import org.glassfish.jersey.server.spi.Container;

import org.junit.Test;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Reload and ContainerLifecycleListener support test.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public class LifecycleListenerTest extends AbstractJdkHttpServerTester {

    @Path("/one")
    public static class One {
        @GET
        public String get() {
            return "one";
        }

        @GET
        @Path("sub")
        public String getSub() {
            return "one-sub";
        }
    }

    @Path("/two")
    public static class Two {
        @GET
        public String get() {
            return "two";
        }
    }

    public static class Reloader extends AbstractContainerLifecycleListener {
        Container container;

        public void reload(ResourceConfig newConfig) {
            container.reload(newConfig);
        }

        public void reload() {
            container.reload();
        }

        @Override
        public void onStartup(Container container) {
            this.container = container;
        }
    }

    @Test
    public void testReload() {
        final ResourceConfig rc = new ResourceConfig(One.class);

        Reloader reloader = new Reloader();
        rc.registerInstances(reloader);

        startServer(rc);

        WebTarget r = ClientBuilder.newClient().target(getUri().path("/").build());

        assertEquals("one", r.path("one").request().get(String.class));
        assertEquals("one-sub", r.path("one/sub").request().get(String.class));
        assertEquals(404, r.path("two").request().get(Response.class).getStatus());

        // add Two resource
        reloader.reload(new ResourceConfig(One.class, Two.class));

        assertEquals("one", r.path("one").request().get(String.class));
        assertEquals("one-sub", r.path("one/sub").request().get(String.class));
        assertEquals("two", r.path("two").request().get(String.class));
    }

    static class StartStopListener extends AbstractContainerLifecycleListener {
        volatile boolean started;
        volatile boolean stopped;

        @Override
        public void onStartup(Container container) {
            started = true;
        }

        @Override
        public void onShutdown(Container container) {
            stopped = true;
        }
    }

    @Test
    public void testStartupShutdownHooks() {
        final StartStopListener listener = new StartStopListener();

        startServer(new ResourceConfig(One.class).register(listener));

        WebTarget r = ClientBuilder.newClient().target(getUri().path("/").build());

        assertThat(r.path("one").request().get(String.class), equalTo("one"));
        assertThat(r.path("two").request().get(Response.class).getStatus(), equalTo(404));

        stopServer();

        assertTrue("ContainerLifecycleListener.onStartup has not been called.", listener.started);
        assertTrue("ContainerLifecycleListener.onShutdown has not been called.", listener.stopped);
    }
}
