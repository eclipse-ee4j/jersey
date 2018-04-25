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

package org.glassfish.jersey.tests.integration.servlet_25_init_5;

import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.external.ExternalTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.glassfish.jersey.test.spi.TestContainerFactory;

import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Pavel Bucek (pavel.bucek at oracle.com)
 */
public class HelloWorldResourceITCase extends JerseyTest {

    @Override
    protected ResourceConfig configure() {
        return new ResourceConfig(HelloWorldResource.class);
    }

    @Override
    protected TestContainerFactory getTestContainerFactory() throws TestContainerException {
        return new ExternalTestContainerFactory();
    }

    @Test
    public void testHelloWorld() throws Exception {
        String s = target().path("filter_path/helloworld").request().get(String.class);
        assertEquals("Hello World! " + this.getClass().getPackage().getName(), s);
    }

    @Test
    public void testHelloWorldAtWrongPath() {
        Response r = target().path("application_path/filter_path/helloworld").request().get();
        assertTrue(
                "Request to application_path/helloworld should have failed, but did not. That means two applications are "
                        + "registered.",
                r.getStatus() >= 400);
    }

    @Test
    @Ignore
    public void testUnreachableResource() {
        Response r = target().path("filter_path/unreachable").request().get();
        assertTrue("Managed to reach a resource that is not registered in the application.", r.getStatus() >= 400);
    }

    @Test
    public void testUnreachableResourceAtWrongPath() {
        Response r = target().path("application_path/filter_path/unreachable").request().get();
        assertTrue("Managed to reach a resource that is not registered in the application.", r.getStatus() >= 400);
    }
}
