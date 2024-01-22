/*
 * Copyright (c) 2021, 2024 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.inject.weld;

import jakarta.enterprise.inject.se.SeContainer;
import jakarta.enterprise.inject.se.SeContainerInitializer;

import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.internal.inject.Injections;
import org.glassfish.jersey.internal.inject.ServiceHolder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;

import java.util.List;

public class TestParent {
    protected static SeContainer container;
    protected InjectionManager injectionManager;

    @BeforeAll
    public static void setup() {
        SeContainerInitializer containerInitializer = SeContainerInitializer.newInstance();
        container = containerInitializer.initialize();
    }

    @BeforeEach
    public void init() {
        injectionManager = Injections.createInjectionManager();
    }

    @AfterAll
    public static void tearDown() throws Exception {
        container.close();
    }

    protected <T> void assertOneInstance(Class<T> type, String implementationName) {
        Assertions.assertEquals(1, injectionManager.getAllInstances(type).size());
        Assertions.assertEquals(1, injectionManager.getAllServiceHolders(type).size());
        T provider = injectionManager.getInstance(type);
        Assertions.assertNotNull(provider);
        Assertions.assertTrue(provider.getClass().getName().contains(implementationName),
                "The provider name was " + provider.getClass().getName() + " but expected was " + implementationName);
    }

    protected <T> void assertMultiple(Class<T> type, int minCount, String... implementationNames) {
        assertMultiple(injectionManager, type, minCount, implementationNames);
    }

    public static <T> void assertMultiple(
            InjectionManager injectionManager, Class<T> type, int minCount, String... implementationNames) {
        List<T> instances = injectionManager.getAllInstances(type);
        Assertions.assertTrue(instances.size() >= minCount, type.getSimpleName() + " instances " + instances.size());
        for (String implName : implementationNames) {
            Assertions.assertTrue(instances.stream().anyMatch(i -> i.getClass().getSimpleName().equals(implName)),
                    implName + " was not found among the instances");
        }

        List<ServiceHolder<T>> holders = injectionManager.getAllServiceHolders(type);
        Assertions.assertTrue(holders.size() >= minCount, type.getSimpleName() + " service holders " + instances.size());
        for (String implName : implementationNames) {
            Assertions.assertTrue(holders.stream().anyMatch(h -> h.getInstance().getClass().getSimpleName().equals(implName)),
                    implName + " was not found among the service holders");
        }
    }

}
