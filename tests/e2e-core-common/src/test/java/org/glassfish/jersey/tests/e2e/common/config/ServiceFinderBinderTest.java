/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.e2e.common.config;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.RuntimeType;

import org.glassfish.jersey.internal.ServiceFinderBinder;
import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.internal.inject.Injections;
import org.glassfish.jersey.internal.inject.Providers;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Service finder injection binder unit test.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public class ServiceFinderBinderTest {

    private static InjectionManager injectionManager;

    public ServiceFinderBinderTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        AbstractBinder binder = new AbstractBinder() {
            @Override
            protected void configure() {
                bind(TestServiceB.class).to(TestContract.class);
                bind(TestServiceD.class).to(TestContract.class);
                install(new ServiceFinderBinder<>(TestContract.class, null, RuntimeType.SERVER));
            }
        };
        injectionManager = Injections.createInjectionManager(binder);
        injectionManager.completeRegistration();
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Test
    public void testConfigure() {
        final Set<TestContract> providers = Providers.getProviders(injectionManager, TestContract.class);
        assertEquals(4, providers.size());

        final Collection<String> providerNames =
                providers.stream()
                         .map(TestContract::name)
                         .collect(Collectors.toList());

        assertTrue(providerNames.contains(TestServiceA.class.getName()));
        assertTrue(providerNames.contains(TestServiceB.class.getName()));
        assertTrue(providerNames.contains(TestServiceC.class.getName()));
        assertTrue(providerNames.contains(TestServiceD.class.getName()));
    }
}
