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

package org.glassfish.jersey.test.simple;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.simple.SimpleContainer;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Test class for {@link SimpleContainer}.
 *
 * @author Arul Dhesiaseelan (aruld@acm.org)
 * @author Miroslav Fuksa
 */
public class SimpleContainerTest extends JerseyTest {

    /**
     * Creates new instance.
     */
    public SimpleContainerTest() {
        super(new SimpleTestContainerFactory());
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
     * Test {@link Simple HttpServer} container.
     */
    public void testSimpleContainerTarget() {
        final Response response = target().path("one").request().get();

        assertEquals("Response status unexpected.", 200, response.getStatus());
        assertEquals("Response entity unexpected.", "get", response.readEntity(String.class));
    }
}
