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

import java.util.concurrent.atomic.AtomicInteger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import javax.inject.Singleton;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTestNg;
import org.glassfish.jersey.test.TestProperties;

import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;

/**
 * Base for TestNG parallel tests. Contains one singleton resource with GET method returning incremental sequence of integers.
 * Extensions has to implement {@link #testValue(Integer)}.
 *
 * @author Michal Gajdos
 */
@Test(threadPoolSize = 5, invocationCount = 13)
public abstract class AbstractParallelTest extends JerseyTestNg {

    @Path("/")
    @Singleton
    @Produces("text/plain")
    public static class Resource {

        private AtomicInteger ai = new AtomicInteger(0);

        @GET
        public int get() {
            return ai.getAndIncrement();
        }
    }

    @Override
    protected Application configure() {
        // Find first available port.
        forceSet(TestProperties.CONTAINER_PORT, "0");

        return new ResourceConfig(Resource.class);
    }

    public void test1() throws Exception {
        test();
    }

    public void test2() throws Exception {
        test();
    }

    public void test3() throws Exception {
        test();
    }

    private void test() {
        final Response response = target().request().get();

        assertEquals(response.getStatus(), 200);
        testValue(response.readEntity(Integer.class));
    }

    protected abstract void testValue(final Integer actual);
}
