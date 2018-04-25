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

/**
 * Reload capability test.
 *
 * For jdk http server test container, run with:
 *
 * mvn -Dtest=ReloadTest -Djersey.config.test.container.factory=org.glassfish.jersey.test.jdkhttp.JdkHttpServerTestContainerFactory clean test
 *
 * For simple test container, run with:
 *
 * mvn -Dtest=ReloadTest -Djersey.config.test.container.factory=org.glassfish.jersey.test.simple.SimpleTestContainerFactory clean test
 *
 * @author Paul Sandoz
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
public class ReloadTest extends JerseyTest {

    @Path("one")
    public static class One {
        @GET
        public String get() {
            return "one";
        }
    }

    @Path("two")
    public static class Two {
        @GET
        public String get() {
            return "two";
        }
    }

    private static class Reloader extends AbstractContainerLifecycleListener {
        Container container;


        public void reload(ResourceConfig rc) {
            container.reload(rc);
        }

        @Override
        public void onStartup(Container container) {
            this.container = container;
        }
    }

    ResourceConfig rc;
    Reloader reloader;

    private ResourceConfig _createRC(Reloader r) {
        final ResourceConfig result = new ResourceConfig(One.class);
        result.registerInstances(r);

        return result;
    }

    @Override
    public ResourceConfig configure() {
        reloader = new Reloader();
        return rc = _createRC(reloader);
    }


    @Test
    public void testReload() {

        assertEquals("one", target().path("one").request().get().readEntity(String.class));
        assertEquals(404, target().path("two").request().get().getStatus());

        rc = _createRC(reloader).registerClasses(Two.class);
        reloader.reload(rc);

        assertEquals("one", target().path("one").request().get().readEntity(String.class));
        assertEquals("two", target().path("two").request().get().readEntity(String.class));
    }
}
