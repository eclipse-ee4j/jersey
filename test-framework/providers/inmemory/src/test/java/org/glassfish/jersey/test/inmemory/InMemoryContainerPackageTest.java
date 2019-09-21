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

package org.glassfish.jersey.test.inmemory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Package scanning test for {@link org.glassfish.jersey.test.inmemory.InMemoryConnector}.
 *
 * @author Paul Sandoz
 */
public class InMemoryContainerPackageTest extends JerseyTest {

    /**
     * Creates new instance.
     */
    public InMemoryContainerPackageTest() {
        super(new InMemoryTestContainerFactory());
    }

    @Override
    protected ResourceConfig configure() {
        ResourceConfig rc = new ResourceConfig();
        rc.packages(this.getClass().getPackage().getName());
        return rc;
    }

    /**
     * Test resource class.
     */
    @Path("packagetest")
    public static class TestResource {

        /**
         * Test resource method.
         *
         * @return Test simple string response.
         */
        @GET
        public String getSomething() {
            return "get";
        }

        /**
         * Test (sub)resource method.
         *
         * @return Simple string test response.
         */
        @GET
        @Path("sub")
        public String getSubResource() {
            return "sub";
        }
    }

    @Test
    public void testInMemoryConnectorGet() {
        final Response response = target("packagetest").request().get();

        assertTrue(response.getStatus() == 200);
    }

    @Test
    public void testGetSub() {
        final String response = target("packagetest").path("sub").request().get(String.class);
        assertEquals("sub", response);
    }
}
