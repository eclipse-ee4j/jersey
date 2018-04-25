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

package org.glassfish.jersey.inject.cdi.se;

import java.util.function.Supplier;

import javax.ws.rs.core.GenericType;

import javax.enterprise.inject.Vetoed;

import org.glassfish.jersey.internal.inject.InjectionManager;

import org.hamcrest.core.StringStartsWith;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;

/**
 * Tests that {@link Supplier} can be registered as a instance-factory.
 *
 * @author Petr Bouda
 */
@Vetoed
public class SupplierInstanceBindingTest {

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
    public void testInstanceFactorySupplerOnly() {
        MyVetoedLongSupplier supplier = new MyVetoedLongSupplier();
        supplier.get();
        supplier.get();

        BindingTestHelper.bind(injectionManager, binder ->
                binder.bindFactory(supplier).to(Long.class));

        Supplier<Long> instance = injectionManager.getInstance(new GenericType<Supplier<Long>>() {}.getType());
        assertEquals((Long) 3L, instance.get());
        assertEquals((Long) 4L, instance.get());
    }

    @Test
    public void testInstanceFactoryValuesOnly() {
        MyVetoedLongSupplier supplier = new MyVetoedLongSupplier();
        supplier.get();
        supplier.get();

        BindingTestHelper.bind(injectionManager, binder ->
                binder.bindFactory(supplier).to(Long.class));

        Long instance3 = injectionManager.getInstance(Long.class);
        Long instance4 = injectionManager.getInstance(Long.class);

        assertEquals((Long) 3L, instance3);
        assertEquals((Long) 4L, instance4);
    }

    @Test
    public void testMessages() {
        BindingTestHelper.bind(injectionManager, binder -> {
            binder.bindFactory(new SupplierGreeting()).to(Greeting.class);
            binder.bindAsContract(Conversation.class);
        });

        Conversation conversation = injectionManager.getInstance(Conversation.class);
        assertThat(conversation.greeting.getGreeting(), StringStartsWith.startsWith(CzechGreeting.GREETING));
    }

    @Test
    public void testSupplierSingletonInstancePerLookup() {
        BindingTestHelper.bind(injectionManager, binder -> {
            binder.bindFactory(new SupplierGreeting()).to(Greeting.class);
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
}
