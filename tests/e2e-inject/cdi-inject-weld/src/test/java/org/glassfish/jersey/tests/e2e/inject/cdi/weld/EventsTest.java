/*
 * Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.e2e.inject.cdi.weld;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.inject.hk2.Hk2InjectionManagerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.jboss.weld.environment.se.Weld;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Tests that the resource can fire an event.
 *
 * @author Petr Bouda
 */
public class EventsTest extends JerseyTest {

    private Weld weld;

    @Before
    public void setup() {
        Assume.assumeTrue(Hk2InjectionManagerFactory.isImmediateStrategy());
    }

    @Override
    public void setUp() throws Exception {
        if (Hk2InjectionManagerFactory.isImmediateStrategy()) {
            weld = new Weld();
            weld.initialize();
            super.setUp();
        }
    }

    @Override
    public void tearDown() throws Exception {
        if (Hk2InjectionManagerFactory.isImmediateStrategy()) {
            weld.shutdown();
            super.tearDown();
        }
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(AccountResource.class);
    }

    @Test
    public void testFiredEvents() {
        Response credit = target("account").queryParam("amount", 50).request().post(Entity.json(""));
        assertEquals(204, credit.getStatus());

        Response debit = target("account").queryParam("amount", 25).request().delete();
        assertEquals(204, debit.getStatus());

        Long current = target("account").queryParam("amount", 25).request().get(Long.class);
        assertEquals(25, current.longValue());
    }
}
