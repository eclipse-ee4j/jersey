/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.jaxrs.inject;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.external.ExternalTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.glassfish.jersey.test.spi.TestContainerFactory;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Tests {@link javax.ws.rs.core.Context} injection into JAX-RS components Provider, Resource, Application.
 *
 * @author Petr Bouda
 */
public class ApplicationInjectITCase extends JerseyTest {

    @Override
    protected Application configure() {
        return new Application();
    }

    @Override
    protected TestContainerFactory getTestContainerFactory() throws TestContainerException {
        return new ExternalTestContainerFactory();
    }

    @Test
    public void testInstance() throws Exception {
        WebTarget t = target();
        t.register(LoggingFeature.class);
        Response r = t.path("/resource/instance").request().get();
        assertEquals(200, r.getStatus());
        assertEquals("111111111", r.readEntity(String.class));
    }

    @Test
    public void testMethod() throws Exception {
        WebTarget t = target();
        t.register(LoggingFeature.class);
        Response r = t.path("/resource/method").request().get();
        assertEquals(200, r.getStatus());
        assertEquals("111111111", r.readEntity(String.class));
    }
    @Test
    public void testApplication() throws Exception {
        WebTarget t = target();
        t.register(LoggingFeature.class);
        Response r = t.path("/resource/application").request().get();
        assertEquals(200, r.getStatus());
        assertEquals("111111111", r.readEntity(String.class));
    }
}
