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

package org.glassfish.jersey.inject.hk2;

import org.glassfish.jersey.internal.inject.Bindings;
import org.glassfish.jersey.internal.inject.ClassBinding;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.internal.inject.Injections;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.Binder;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

import org.junit.Test;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Petr Bouda
 */
public class InjectionManagerTest {

    @Test
    public void testServiceLocatorParent() {
        AbstractBinder binder = new AbstractBinder() {
            @Override
            protected void configure() {
                bindAsContract(EnglishGreeting.class);
            }
        };
        ServiceLocator parentLocator = ServiceLocatorUtilities.bind(binder);

        InjectionManager injectionManager = Injections.createInjectionManager(parentLocator);
        injectionManager.completeRegistration();
        assertNotNull(injectionManager.getInstance(EnglishGreeting.class));
    }

    @Test
    public void testInjectionManagerParent() {
        ClassBinding<EnglishGreeting> greetingBinding = Bindings.serviceAsContract(EnglishGreeting.class);
        InjectionManager parentInjectionManager = Injections.createInjectionManager();
        parentInjectionManager.register(greetingBinding);
        parentInjectionManager.completeRegistration();

        InjectionManager injectionManager = Injections.createInjectionManager(parentInjectionManager);
        injectionManager.completeRegistration();
        assertNotNull(injectionManager.getInstance(EnglishGreeting.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnknownParent() {
        Injections.createInjectionManager(new Object());
    }

    @Test
    public void testIsRegistrable() {
        InjectionManager injectionManager = Injections.createInjectionManager();
        assertTrue(injectionManager.isRegistrable(Binder.class));
        assertTrue(injectionManager.isRegistrable(AbstractBinder.class));
        assertFalse(injectionManager.isRegistrable(org.glassfish.jersey.internal.inject.AbstractBinder.class));
        assertFalse(injectionManager.isRegistrable(String.class));
    }

    @Test
    public void testRegisterBinder() {
        AbstractBinder binder = new AbstractBinder() {
            @Override
            protected void configure() {
                bindAsContract(EnglishGreeting.class);
            }
        };

        InjectionManager injectionManager = Injections.createInjectionManager();
        injectionManager.register(binder);
        injectionManager.completeRegistration();
        assertNotNull(injectionManager.getInstance(EnglishGreeting.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRegisterUnknownProvider() {
        InjectionManager injectionManager = Injections.createInjectionManager();
        injectionManager.register(new Object());
    }
}
