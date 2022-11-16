/*
 * Copyright (c) 2021, 2022 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.inject.weld.internal.managed;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.se.SeContainerInitializer;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.Vetoed;
import javax.inject.Inject;
import javax.ws.rs.core.GenericType;

import org.glassfish.jersey.internal.inject.AbstractBinder;

import org.hamcrest.core.StringStartsWith;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Tests that {@link Supplier} can be registered as a instance-factory.
 *
 * @author Petr Bouda
 */
@Vetoed
public class SupplierInstanceBindingTest extends TestParent {

    private static long supplierHit = 2;
    private static AtomicBoolean runOnlyOnceGuard = new AtomicBoolean(false);

    @BeforeAll
    public static void setup() {
        SeContainerInitializer containerInitializer = SeContainerInitializer.newInstance();
        containerInitializer.addExtensions(new SupplierInstanceBindingTestExtension());
        container = containerInitializer.initialize();
    }

    @BeforeEach
    public void initBinding() {
        if (!runOnlyOnceGuard.getAndSet(true)) {
            MyVetoedLongSupplier supplier = new MyVetoedLongSupplier();
            supplier.get();
            supplier.get();

            BindingTestHelper.bind(injectionManager, binder ->
                    binder.bindFactory(supplier).to(Long.class));

            BindingTestHelper.bind(injectionManager, binder -> {
                binder.bindFactory(new SupplierGreeting()).to(Greeting.class);
            });
        }
    }

    @Test
    public void testInstanceFactorySupplierOnly() {
//        MyVetoedLongSupplier supplier = new MyVetoedLongSupplier();
//        supplier.get();
//        supplier.get();
//
//        BindingTestHelper.bind(injectionManager, binder ->
//                binder.bindFactory(supplier).to(Long.class));

        Supplier<Long> instance = injectionManager.getInstance(new GenericType<Supplier<Long>>() {
        }.getType());
        assertEquals((Long) (supplierHit++ + 1), instance.get());
        assertEquals((Long) (supplierHit++ + 1), instance.get());
    }

    @Test
    public void testInstanceFactoryValuesOnly() {
//        MyVetoedLongSupplier supplier = new MyVetoedLongSupplier();
//        supplier.get();
//        supplier.get();
//
//        BindingTestHelper.bind(injectionManager, binder ->
//                binder.bindFactory(supplier).to(Long.class));

        Long instance3 = injectionManager.getInstance(Long.class);
        Long instance4 = injectionManager.getInstance(Long.class);

        assertEquals((Long) (supplierHit++ + 1), instance3);
        assertEquals((Long) (supplierHit++ + 1), instance4);
    }

    @Test
    public void testMessages() {
//        BindingTestHelper.bind(injectionManager, binder -> {
//            binder.bindFactory(new SupplierGreeting()).to(Greeting.class);
//            binder.bindAsContract(Conversation.class);
//        });

        Conversation conversation = injectionManager.getInstance(Conversation.class);
        assertThat(conversation.greeting.getGreeting(), StringStartsWith.startsWith(CzechGreeting.GREETING));
    }

    @Test
    public void testSupplierSingletonInstancePerLookup() {
//        BindingTestHelper.bind(injectionManager, binder -> {
//            binder.bindFactory(new SupplierGreeting()).to(Greeting.class);
//            binder.bindAsContract(Conversation.class);
//        });

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

    @Vetoed
    static class Conversation {
        @Inject
        Greeting greeting;

        @Inject
        Supplier<Greeting> greetingSupplier;
    }

    @Vetoed
    static class SupplierGreeting implements Supplier<Greeting> {

        private final String greetingType;

        /**
         * Default constructor.
         */
        public SupplierGreeting() {
            this(CzechGreeting.GREETING);
        }

        /**
         * Supplier's constructor.
         *
         * @param greetingType greetingType in a specific language.
         */
        public SupplierGreeting(String greetingType) {
            this.greetingType = greetingType;
        }

        @Override
        public Greeting get() {
            if (CzechGreeting.GREETING.equals(greetingType)) {
                return new CzechGreeting();
            } else {
                return new EnglishGreeting();
            }
        }
    }

    static class EnglishGreeting implements Greeting {

        static final String GREETING = "Hello";

        @Override
        public String getGreeting() {
            return GREETING + "#" + Thread.currentThread().getName();
        }

        @Override
        public String toString() {
            return "EnglishGreeting";
        }
    }

    static class CzechGreeting implements Greeting {

        static final String GREETING = "Ahoj";

        @Override
        public String getGreeting() {
            return GREETING + "#" + Thread.currentThread().getName();
        }

        @Override
        public String toString() {
            return "CzechGreeting";
        }
    }

    @FunctionalInterface
    static interface Greeting {
        /**
         * Returns greeting in a specific language.
         *
         * @return type of the greeting.
         */
        String getGreeting();
    }

    private static class SupplierInstanceBindingTestExtension implements Extension {
        void registerBindings(@Observes BeforeBeanDiscovery beforeBeanDiscovery, BeanManager beanManager) {
            AbstractBinder testBinder = new AbstractBinder() {
                @Override
                protected void configure() {
                    MyVetoedLongSupplier supplier = new MyVetoedLongSupplier();
                    supplier.get();
                    supplier.get();
                    bindFactory(supplier).to(Long.class);

                    bindFactory(new SupplierGreeting()).to(Greeting.class);
                    bindAsContract(Conversation.class);
                }
            };

            beanManager.getExtension(BinderRegisterExtension.class).register(beforeBeanDiscovery, testBinder.getBindings());
        }
    }
}
