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

package org.glassfish.jersey.tests.cdi.singleton;

import org.glassfish.jersey.inject.hk2.Hk2InjectionManagerFactory;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.external.ExternalTestContainerFactory;
import org.jboss.weld.environment.se.Weld;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

public class SingletonTest extends JerseyTest {
    private Weld weld;

    @Before
    public void setup() {
        Assume.assumeTrue(Hk2InjectionManagerFactory.isImmediateStrategy());
    }

    @Override
    public void setUp() throws Exception {
        if (Hk2InjectionManagerFactory.isImmediateStrategy()) {
            if (!ExternalTestContainerFactory.class.isAssignableFrom(getTestContainerFactory().getClass())) {
                weld = new Weld();
                weld.initialize();
            }
            super.setUp();
        }
    }

    @Override
    public void tearDown() throws Exception {
        weld.shutdown();
        super.tearDown();
    }

    @Override
    protected Application configure() {
        return new SingletonTestApp();
    }

    @Test
    public void testSingleton() {
        try (Response response = target().request().get()) {
            String entity = response.readEntity(String.class);
            Assert.assertEquals(SingletonResource.class.getSimpleName(), entity);

            final SingletonResource actualResource = SingletonTestApp.SINGLETON_RESOURCES[1];
            Assert.assertEquals(actualResource, SingletonTestApp.SINGLETON_RESOURCES[0]);
            Assert.assertEquals(actualResource, SingletonTestApp.SINGLETON_RESOURCES[2]);
        }
    }
}
