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

package org.glassfish.jersey.tests.e2e.inject.cdi.se.scopes;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * Tests CDI resources.
 */
public class ScopesTest extends JerseyTest {

    @Override
    protected ResourceConfig configure() {
        return new ResourceConfig(RequestScopedResource.class, SingletonScopedResource.class);
    }

    @Test
    public void testCheckRequest() throws InterruptedException {
        String[] response1 = target().path("request").path("James").request().get(String.class).split(" ");
        String[] response2 = target().path("request").path("Marcus").request().get(String.class).split(" ");
        assertResponses("request", response1, response2);
        assertNotEquals(response1[3], response2[3]);
    }

    @Test
    public void testCheckSingleton() throws InterruptedException {
        String[] response1 = target().path("singleton").path("James").request().get(String.class).split(" ");
        String[] response2 = target().path("singleton").path("Marcus").request().get(String.class).split(" ");
        assertResponses("singleton", response1, response2);
        assertEquals(response1[3], response2[3]);
    }

    private void assertResponses(String type, String[] response1, String[] response2) {
        assertEquals("Hello_James", response1[0]);
        assertEquals("[1]", response1[1]);
        assertEquals("[" + type + "/James]", response1[2]);

        assertEquals("Hello_Marcus", response2[0]);
        assertEquals("[2]", response2[1]);
        assertEquals("[" + type + "/Marcus]", response2[2]);
    }
}
