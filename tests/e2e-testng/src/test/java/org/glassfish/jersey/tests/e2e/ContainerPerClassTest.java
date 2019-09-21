/*
 * Copyright (c) 2014, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.e2e;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import javax.inject.Singleton;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTestNg;

import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;

/**
 * Tests that container is created only once per class and each test method sends request to the same container.
 *
 * @author Michal Gajdos
 */
public class ContainerPerClassTest extends JerseyTestNg.ContainerPerClassTest {

    @Path("/")
    @Singleton
    @Produces("text/plain")
    public static class Resource {

        private int i = 1;

        @GET
        public int get() {
            return i++;
        }
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(Resource.class);
    }

    @Test(priority = 1)
    public void first() throws Exception {
        test(1);
    }

    @Test(priority = 2)
    public void second() throws Exception {
        test(2);
    }

    @Test(priority = 3)
    public void third() throws Exception {
        test(3);
    }

    private void test(final Integer expected) {
        final Response response = target().request().get();

        assertEquals(response.getStatus(), 200);
        assertEquals(response.readEntity(Integer.class), expected);
    }
}
