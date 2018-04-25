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

package org.glassfish.jersey.tests.cdi.resources;

import javax.ws.rs.client.WebTarget;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Test CDI timers injected into EJB beans.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
public class CdiIntoEjbTest extends TestBase {

    @Test
    public void testStateless() {
        _testRequestScoped("stateless");
        _testAppScoped("stateless");
    }

    @Test
    public void testStateful() {
        _testRequestScoped("stateful");
        _testAppScoped("stateful");
    }

    @Test
    public void testSingleton() {
        _testRequestScoped("singleton");
        _testAppScoped("singleton");
    }

    private void _testRequestScoped(final String ejbType) {

        final WebTarget target = target().path(ejbType).path("request-scoped-timer");
        long firstMillis = _getMillis(target);
        sleep(2);
        long secondMillis = _getMillis(target);

        assertTrue("Second request should have greater millis!", secondMillis > firstMillis);
    }

    private void _testAppScoped(final String ejbType) {

        final WebTarget target = target().path(ejbType).path("app-scoped-timer");
        long firstMillis = _getMillis(target);
        sleep(2);
        long secondMillis = _getMillis(target);

        assertTrue("Second request should have the same millis!", secondMillis == firstMillis);
    }
}
