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

import java.math.BigDecimal;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import org.junit.Test;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * Base class for JAX-RS resource tests.
 *
 * @author Marko Asplund (marko.asplund at yahoo.com)
 */
public abstract class AccountResourceTestBase extends ResourceTestBase {

    // test singleton scoped Spring bean injection using @Inject + @Autowired
    @Test
    public void testSingletonScopedSpringService() {
        final BigDecimal newBalance = new BigDecimal(Math.random());
        final WebTarget t = target(getResourceFullPath());

        t.path("/singleton/xyz123").request().put(Entity.entity(newBalance.toString(), MediaType.TEXT_PLAIN_TYPE));
        final BigDecimal balance = t.path("/singleton/autowired/xyz123").request().get(BigDecimal.class);
        assertEquals(newBalance, balance);
    }

    @Test
    public void testRequestScopedSpringService() {
        final BigDecimal newBalance = new BigDecimal(Math.random());
        final WebTarget t = target(getResourceFullPath());
        final BigDecimal balance = t.path("request/abc456").request().put(Entity.text(newBalance), BigDecimal.class);
        assertEquals(newBalance, balance);
    }

    @Test
    public void testPrototypeScopedSpringService() {
        final BigDecimal newBalance = new BigDecimal(Math.random());
        final WebTarget t = target(getResourceFullPath());
        final BigDecimal balance = t.path("prototype/abc456").request().put(Entity.text(newBalance), BigDecimal.class);
        assertEquals(new BigDecimal("987.65"), balance);
    }

    @Test
    public void testServletInjection() {
        final WebTarget t = target(getResourceFullPath());

        String server = t.path("server").request().get(String.class);
        assertThat(server, startsWith("PASSED: "));

        server = t.path("singleton/server").request().get(String.class);
        assertThat(server, startsWith("PASSED: "));

        server = t.path("singleton/autowired/server").request().get(String.class);
        assertThat(server, startsWith("PASSED: "));

        server = t.path("request/server").request().get(String.class);
        assertThat(server, startsWith("PASSED: "));

        server = t.path("prototype/server").request().get(String.class);
        assertThat(server, startsWith("PASSED: "));
    }
}
