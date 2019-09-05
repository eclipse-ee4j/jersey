/*
 * Copyright (c) 2013, 2019 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.integration.servlet_3_params;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.external.ExternalTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.glassfish.jersey.test.spi.TestContainerFactory;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * @author Pavel Bucek
 */
public class ParamsTestITCase extends JerseyTest {
    @Override
    protected Application configure() {
        return new Application();
    }

    @Override
    protected TestContainerFactory getTestContainerFactory() throws TestContainerException {
        return new ExternalTestContainerFactory();
    }

    // see web.xml
    @Test
    public void testInitParam() throws Exception {
        Response r = target().path("params").queryParam("param-name", "myInitParam").request().get();
        assertEquals(200, r.getStatus());
        assertEquals("myValue", r.readEntity(String.class));
    }

    // see CustomContextListener.java
    @Test
    public void testContextParam() throws Exception {
        Response r = target().path("params").queryParam("param-name", "myContextParam").request().get();
        assertEquals(200, r.getStatus());
        assertEquals("myValue", r.readEntity(String.class));
    }
}
