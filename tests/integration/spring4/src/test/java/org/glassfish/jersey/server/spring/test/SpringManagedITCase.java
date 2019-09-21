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

package org.glassfish.jersey.server.spring.test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;

import org.glassfish.jersey.server.ResourceConfig;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Tests for Spring managed JAX-RS resources.
 *
 * @author Marko Asplund (marko.asplund at yahoo.com)
 */
public class SpringManagedITCase extends AccountResourceTestBase {

    @Override
    protected ResourceConfig configure(final ResourceConfig rc) {
        return rc.register(AccountSpringResource.class);
    }

    @Override
    protected String getResourcePath() {
        return "/spring/account";
    }

    @Test
    public void testResourceScope() {
        final WebTarget t = target(getResourceFullPath());
        final String message = "hello, world";
        final String echo = t.path("message").request().put(Entity.text(message), String.class);
        assertEquals(message, echo);
        final String msg = t.path("message").request().get(String.class);
        assertEquals(message, msg);
    }

}
