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

import java.util.function.Supplier;

import javax.inject.Singleton;

import org.glassfish.jersey.internal.inject.InjectionManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

/**
 * Tests that {@link java.util.function.Supplier} can be registered as a class-factory.
 *
 * @author Petr Bouda
 */
public class SupplierClassBindingTest {

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
    public void testMessages() {
        BindingTestHelper.bind(injectionManager, binder -> {
            binder.bindFactory(SupplierGreeting.class).to(Greeting.class);
            binder.bindAsContract(Conversation.class);
        });

        Conversation conversation = injectionManager.getInstance(Conversation.class);
        assertEquals(CzechGreeting.GREETING, conversation.greeting.getGreeting());
        assertEquals(CzechGreeting.GREETING, conversation.greetingSupplier.get().getGreeting());
    }

    @Test
    public void testSupplierPerLookupInstancePerLookup() {
        BindingTestHelper.bind(injectionManager, binder -> {
            binder.bindFactory(SupplierGreeting.class).to(Greeting.class);
            binder.bindAsContract(Conversation.class);
        });

        Greeting greeting1 = injectionManager.getInstance(Conversation.class).greeting;
        Greeting greeting2 = injectionManager.getInstance(Conversation.class).greeting;
        Greeting greeting3 = injectionManager.getInstance(Conversation.class).greeting;

        assertNotSame(greeting1, greeting2);
        assertNotSame(greeting2, greeting3);

        Supplier<Greeting> supplier1 = injectionManager.getInstance(Conversation.class).greetingSupplier;
        Supplier<Greeting> supplier2 = injectionManager.getInstance(Conversation.class).greetingSupplier;
        Supplier<Greeting> supplier3 = injectionManager.getInstance(Conversation.class).greetingSupplier;

        assertNotSame(supplier1, supplier2);
        assertNotSame(supplier2, supplier3);
    }

    @Test
    public void testSupplierSingletonInstancePerLookup() {
        BindingTestHelper.bind(injectionManager, binder -> {
            binder.bindFactory(SupplierGreeting.class, Singleton.class).to(Greeting.class);
            binder.bindAsContract(Conversation.class);
        });

        Greeting greeting1 = injectionManager.getInstance(Conversation.class).greeting;
        Greeting greeting2 = injectionManager.getInstance(Conversation.class).greeting;
        Greeting greeting3 = injectionManager.getInstance(Conversation.class).greeting;

        assertNotSame(greeting1, greeting2);
        assertNotSame(greeting2, greeting3);

        Supplier<Greeting> supplier1 = injectionManager.getInstance(Conversation.class).greetingSupplier;
        Supplier<Greeting> supplier2 = injectionManager.getInstance(Conversation.class).greetingSupplier;
        Supplier<Greeting> supplier3 = injectionManager.getInstance(Conversation.class).greetingSupplier;

        assertSame(supplier1, supplier2);
        assertSame(supplier2, supplier3);
    }

    @Test
    public void testSupplierPerLookupInstanceSingleton() {
        BindingTestHelper.bind(injectionManager, binder -> {
            binder.bindFactory(SupplierGreeting.class).to(Greeting.class).in(Singleton.class);
            binder.bindAsContract(Conversation.class);
        });

        Greeting greeting1 = injectionManager.getInstance(Conversation.class).greeting;
        Greeting greeting2 = injectionManager.getInstance(Conversation.class).greeting;
        Greeting greeting3 = injectionManager.getInstance(Conversation.class).greeting;

        assertSame(greeting1, greeting2);
        assertSame(greeting2, greeting3);

        Supplier<Greeting> supplier1 = injectionManager.getInstance(Conversation.class).greetingSupplier;
        Supplier<Greeting> supplier2 = injectionManager.getInstance(Conversation.class).greetingSupplier;
        Supplier<Greeting> supplier3 = injectionManager.getInstance(Conversation.class).greetingSupplier;

        assertNotSame(supplier1, supplier2);
        assertNotSame(supplier2, supplier3);
    }

    @Test
    public void testSupplierSingletonInstanceSingleton() {
        BindingTestHelper.bind(injectionManager, binder -> {
            binder.bindFactory(SupplierGreeting.class, Singleton.class).to(Greeting.class).in(Singleton.class);
            binder.bindAsContract(Conversation.class);
        });

        Greeting greeting1 = injectionManager.getInstance(Conversation.class).greeting;
        Greeting greeting2 = injectionManager.getInstance(Conversation.class).greeting;
        Greeting greeting3 = injectionManager.getInstance(Conversation.class).greeting;

        assertSame(greeting1, greeting2);
        assertSame(greeting2, greeting3);

        Supplier<Greeting> supplier1 = injectionManager.getInstance(Conversation.class).greetingSupplier;
        Supplier<Greeting> supplier2 = injectionManager.getInstance(Conversation.class).greetingSupplier;
        Supplier<Greeting> supplier3 = injectionManager.getInstance(Conversation.class).greetingSupplier;

        assertSame(supplier1, supplier2);
        assertSame(supplier2, supplier3);
    }

    @Test
    public void testSupplierBeanNamed() {
        BindingTestHelper.bind(injectionManager, binder -> {
            binder.bindFactory(TestSuppliers.TestSupplier.class).named(TestSuppliers.TEST).to(String.class);
            binder.bindFactory(TestSuppliers.OtherTestSupplier.class).named(TestSuppliers.OTHER_TEST).to(String.class);
            binder.bindAsContract(TestSuppliers.TargetSupplierBean.class);
        });

        TestSuppliers.TargetSupplierBean instance = injectionManager.getInstance(TestSuppliers.TargetSupplierBean.class);
        assertEquals(TestSuppliers.OTHER_TEST, instance.obj);
    }

    @Test
    public void testSupplierNamed() {
        BindingTestHelper.bind(injectionManager, binder -> {
            binder.bindFactory(TestSuppliers.TestSupplier.class).named(TestSuppliers.TEST).to(String.class);
            binder.bindFactory(TestSuppliers.OtherTestSupplier.class).named(TestSuppliers.OTHER_TEST).to(String.class);
            binder.bindAsContract(TestSuppliers.TargetSupplier.class);
        });

        TestSuppliers.TargetSupplier instance = injectionManager.getInstance(TestSuppliers.TargetSupplier.class);
        assertEquals(TestSuppliers.OTHER_TEST, instance.supplier.get());
    }
}
