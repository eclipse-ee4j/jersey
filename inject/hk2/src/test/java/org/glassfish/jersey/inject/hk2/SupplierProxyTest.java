/*
 * Copyright (c) 2017, 2022 Oracle and/or its affiliates. All rights reserved.
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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests that {@link java.util.function.Supplier} can contain proxies.
 *
 * @author Petr Bouda
 */
public class SupplierProxyTest {

    private InjectionManager injectionManager;

    @BeforeEach
    public void setup() {
        injectionManager = BindingTestHelper.createInjectionManager();
    }

    @AfterEach
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
        assertTrue(conversation.greeting.getClass().getName().contains(".proxy"));
        assertFalse(conversation.greetingSupplier.getClass().getName().contains(".proxy"));
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

        assertTrue(conversation.greeting.getClass().getName().contains(".proxy"));
        assertFalse(conversation.greetingSupplier.getClass().getName().contains(".proxy"));
        injectionManager.shutdown();
    }
}
