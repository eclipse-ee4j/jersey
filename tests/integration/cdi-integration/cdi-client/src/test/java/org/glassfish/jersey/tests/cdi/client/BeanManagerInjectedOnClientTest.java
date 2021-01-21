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

package org.glassfish.jersey.tests.cdi.client;

import org.glassfish.jersey.inject.hk2.Hk2InjectionManagerFactory;
import org.glassfish.jersey.internal.util.ReflectionHelper;
import org.jboss.weld.environment.se.Weld;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.ws.rs.Priorities;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;
import java.security.AccessController;

public class BeanManagerInjectedOnClientTest {
    private static Weld weld;

    @BeforeAll
    public static void setup() {
        Assumptions.assumeTrue(Hk2InjectionManagerFactory.isImmediateStrategy());
        weld = new Weld();
        weld.initialize();
    }

    @AfterAll
    public static void tearDown() throws Exception {
        if (Hk2InjectionManagerFactory.isImmediateStrategy()) {
            weld.shutdown();
        }
    }

    @Test
    public void testBeanManagerIsInjected() {
        // test that there is no server is available

        final String serverComponentProvider = "org.glassfish.jersey.server.spi.ComponentProvider";
        final Class<?> aClass = AccessController.doPrivileged(ReflectionHelper.classForNamePA(serverComponentProvider));
        Assertions.assertNull(aClass);

        // test CDI injection
        try (Response r = ClientBuilder.newClient()
                .register(CdiClientFilter.class, Priorities.USER - 500)
                .register(CdiLowerPriorityClientFilter.class, Priorities.USER)
                .target("http://localhost:8080/abort").request().get()) {
            Assertions.assertEquals(Response.Status.OK.getStatusCode(), r.getStatus());
        }
    }
}
