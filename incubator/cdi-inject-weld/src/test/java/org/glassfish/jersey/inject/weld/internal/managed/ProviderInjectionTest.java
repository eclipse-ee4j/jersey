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

import javax.enterprise.event.Observes;
import javax.enterprise.inject.Vetoed;
import javax.enterprise.inject.se.SeContainerInitializer;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.core.Context;

import org.glassfish.jersey.internal.inject.AbstractBinder;

import org.hamcrest.core.StringStartsWith;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.function.Supplier;

import static org.hamcrest.MatcherAssert.assertThat;

public class ProviderInjectionTest extends TestParent {

    @BeforeAll
    public static void setup() {
        SeContainerInitializer containerInitializer = SeContainerInitializer.newInstance();
        containerInitializer.addExtensions(new ProviderInjectionTestExtension());
        container = containerInitializer.initialize();
    }


    @Test
    public void testProviderInject() {
//        BindingTestHelper.bind(injectionManager, binder -> {
//            binder.bind(CzechGreeting.class).to(Greeting.class);
//            binder.bindAsContract(ProviderInject.class);
//        });

        Greeting greeting = injectionManager.getInstance(Greeting.class);
        Assertions.assertNotNull(greeting);

        ProviderInject instance = injectionManager.getInstance(ProviderInject.class);
        assertThat(instance.greeting.get().getGreeting(), StringStartsWith.startsWith(CzechGreeting.GREETING));
    }

    @Test
    public void testProviderContext() {
//        BindingTestHelper.bind(injectionManager, binder -> {
//            binder.bind(CzechGreeting.class).to(Greeting.class);
//            binder.bindAsContract(ProviderContext.class);
//        });

        ProviderContext instance = injectionManager.getInstance(ProviderContext.class);
        assertThat(instance.greeting.get().getGreeting(), StringStartsWith.startsWith(CzechGreeting.GREETING));
    }

    @Test
    public void testProviderFactoryInject() {
//        BindingTestHelper.bind(injectionManager, binder -> {
//            binder.bindFactory(SupplierGreeting.class).to(Greeting2.class);
//            binder.bindAsContract(ProviderInject2.class);
//        });

        ProviderInject2 conversation = injectionManager.getInstance(ProviderInject2.class);
        assertThat(conversation.greeting.get().getGreeting(), StringStartsWith.startsWith(CzechGreeting.GREETING));
    }


    @Test
    public void testProviderFactoryContext() {
//        BindingTestHelper.bind(injectionManager, binder -> {
//            binder.bindFactory(SupplierGreeting.class).to(Greeting2.class);
//            binder.bindAsContract(ProviderContext2.class);
//        });

        ProviderContext2 conversation = injectionManager.getInstance(ProviderContext2.class);
        assertThat(conversation.greeting.get().getGreeting(), StringStartsWith.startsWith(CzechGreeting.GREETING));
    }

    public static class ProviderInject2 {
        @Inject
        Provider<Greeting2> greeting;
    }

    public static class ProviderContext2 {
        @Context
        Provider<Greeting2> greeting;
    }

    public static class ProviderInject {
        @Inject
        Provider<Greeting> greeting;
    }

    public static class ProviderContext {
        @Context
        Provider<Greeting> greeting;
    }

    @Vetoed
    static class SupplierGreeting implements Supplier<Greeting2> {

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
        public Greeting2 get() {
            if (CzechGreeting.GREETING.equals(greetingType)) {
                return new CzechGreeting2();
            } else {
                return new EnglishGreeting2();
            }
        }
    }

    @Vetoed
    static class EnglishGreeting2 extends EnglishGreeting implements Greeting2 {
    }

    @Vetoed
    static class CzechGreeting2 extends CzechGreeting implements Greeting2 {
    }

    @Vetoed
    static class EnglishGreeting implements Greeting, Printable {

        static final String GREETING = "Hello";

        @Override
        public String getGreeting() {
            return GREETING + "#" + Thread.currentThread().getName();
        }

        @Override
        public void print() {
            System.out.println(GREETING);
        }

        @Override
        public String toString() {
            return "EnglishGreeting";
        }
    }

    @Vetoed
    static class CzechGreeting implements Greeting, Printable {

        static final String GREETING = "Ahoj";

        @Override
        public String getGreeting() {
            return GREETING + "#" + Thread.currentThread().getName();
        }

        @Override
        public void print() {
            System.out.println(GREETING);
        }

        @Override
        public String toString() {
            return "CzechGreeting";
        }
    }

    @FunctionalInterface
    interface Greeting2 {
        String getGreeting();
    }

    @FunctionalInterface
    interface Greeting {
        String getGreeting();
    }

    @FunctionalInterface
    public interface Printable {
        void print();
    }

    private static class ProviderInjectionTestExtension implements Extension {
        void registerBindings(@Observes BeforeBeanDiscovery beforeBeanDiscovery, BeanManager beanManager) {
            AbstractBinder testBinder = new AbstractBinder() {
                @Override
                protected void configure() {
                    bind(CzechGreeting.class).to(Greeting.class);
                    bindAsContract(ProviderInject.class);
                    bindAsContract(ProviderContext.class);

                    bindFactory(SupplierGreeting.class).to(Greeting2.class);
                    bindAsContract(ProviderInject2.class);
                    bindAsContract(ProviderContext2.class);
                }
            };

            beanManager.getExtension(BinderRegisterExtension.class).register(beforeBeanDiscovery, testBinder.getBindings());
        }
    }
}
