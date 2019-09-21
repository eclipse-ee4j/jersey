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

import org.glassfish.jersey.internal.inject.InjectionManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests that {@link java.util.function.Supplier} can contain proxies.
 *
 * @author Petr Bouda
 */
public class SupplierProxyTest {

    private InjectionManager injectionManager;

    @Before
    public void setup() {
        injectionManager = BindingTestHelper.createInjectionManager();
    }

    @After
    public void teardown() {
        injectionManager.shutdown();
    }

    @Test
    public void testClassSupplierProxy() {
        BindingTestHelper.bind(injectionManager, binder -> {
            binder.bindFactory(SupplierGreeting.class)
                    .to(Greeting.class)
                    .proxy(true)
                    .in(ProxiableSingleton.class);

            binder.bind(new ProxiableSingletonContext())
                    .to(ProxiableSingletonContext.class);

            binder.bindAsContract(Conversation.class);
        });

        Conversation conversation = injectionManager.getInstance(Conversation.class);
        assertTrue(conversation.greeting.getClass().getName().startsWith("com.sun.proxy"));
        assertFalse(conversation.greetingSupplier.getClass().getName().startsWith("com.sun.proxy"));
        injectionManager.shutdown();
    }

    @Test
    public void testInstanceSupplierProxy() {
        BindingTestHelper.bind(injectionManager, binder -> {
            binder.bindFactory(new SupplierGreeting())
                    .to(Greeting.class)
                    .proxy(true)
                    .in(ProxiableSingleton.class);

            binder.bind(new ProxiableSingletonContext())
                    .to(ProxiableSingletonContext.class);

            binder.bindAsContract(Conversation.class);
        });

        Conversation conversation = injectionManager.getInstance(Conversation.class);
        assertTrue(conversation.greeting.getClass().getName().startsWith("com.sun.proxy"));
        assertFalse(conversation.greetingSupplier.getClass().getName().startsWith("com.sun.proxy"));
        injectionManager.shutdown();
    }
}
