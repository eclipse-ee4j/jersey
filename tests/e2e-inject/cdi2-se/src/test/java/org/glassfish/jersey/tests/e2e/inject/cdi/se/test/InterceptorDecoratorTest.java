/*
 * Copyright (c) 2017, 2025 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.e2e.inject.cdi.se.test;

import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Response;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.glassfish.jersey.tests.e2e.inject.cdi.se.HelloResource;
import org.glassfish.jersey.tests.e2e.inject.cdi.se.NameService;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests that the resource can be intercepted and decorated.
 *
 * @author Petr Bouda
 */
public class InterceptorDecoratorTest extends JerseyTest {

    @Override
    protected Application configure() {
        return new ResourceConfig(HelloResource.class);
    }

    @Test
    public void testInterceptedGet() {
        String intercepted = target("intercepted").queryParam("user", NameService.NAME).request().get(String.class);
        assertEquals("***Hello James***", intercepted);
    }

    @Test
    public void testForbiddenGet() {
        Response result = target("intercepted").queryParam("user", "unknown").request().get();
        assertEquals(403, result.getStatus());
    }
}
