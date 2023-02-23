/*
 * Copyright (c) 2022, 2023 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.cdi.resources.test;

import org.glassfish.jersey.ext.cdi1x.internal.CdiComponentProvider;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.jboss.weld.environment.se.Weld;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.enterprise.inject.Vetoed;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Application;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class CdiComponentProviderTest extends JerseyTest {
    Weld weld;

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        weld = new Weld();
        weld.initialize();
        super.setUp();
    }

    @AfterEach
    @Override
    public void tearDown() throws Exception {
        weld.shutdown();
        super.tearDown();
    }

    @Override
    protected Application configure() {
        return new ResourceConfig();
    }

    @Test
    public void testVetoedClassIsNotBound() {
        BeanManager beanManager = CDI.current().getBeanManager();
        CdiComponentProvider provider = beanManager.getExtension(CdiComponentProvider.class);
        assertFalse(provider.bind(VetoedResourceClass.class, Collections.singleton(Path.class)));
    }

    @Test
    public void testInterfaceIsNotBound() {
        BeanManager beanManager = CDI.current().getBeanManager();
        CdiComponentProvider provider = beanManager.getExtension(CdiComponentProvider.class);
        assertFalse(provider.bind(InterfaceResource.class, Collections.singleton(Path.class)));
    }

    @Path("/vetoed")
    @Vetoed
    public static class VetoedResourceClass {
        @GET
        public String get() {
            return null;
        }
    }

    @Path("/iface")
    public static interface InterfaceResource {
        @GET
        public String get();
    }
}
