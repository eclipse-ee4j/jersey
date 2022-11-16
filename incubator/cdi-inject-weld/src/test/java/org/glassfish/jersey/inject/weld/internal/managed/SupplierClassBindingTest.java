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

import java.util.function.Supplier;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.Vetoed;
import javax.enterprise.inject.se.SeContainerInitializer;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.glassfish.jersey.internal.inject.AbstractBinder;

import org.hamcrest.core.StringStartsWith;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Tests that {@link Supplier} can be registered as a class-factory.
 */
@Vetoed
public class SupplierClassBindingTest extends TestParent {

    public static final String GREET = "Hi";

    @BeforeAll
    public static void setup() {
        SeContainerInitializer containerInitializer = SeContainerInitializer.newInstance();
        containerInitializer.addExtensions(new SupplierClassBindingTestExtension());
        container = containerInitializer.initialize();
    }

    @Test
    public void testMessages() {
//        BindingTestHelper.bind(injectionManager, binder -> {
//            binder.bindFactory(SupplierGreeting.class).to(Greeting.class);
//            binder.bindAsContract(Conversation.class);
//        });

        Conversation conversation = injectionManager.getInstance(Conversation.class);
        assertThat(conversation.greeting.getGreeting(), StringStartsWith.startsWith(GREET));
    }

    @Test
    public void testSupplierPerLookupInstancePerLookup() {
//        BindingTestHelper.bind(injectionManager, binder -> {
//            binder.bindFactory(SupplierGreeting.class).to(Greeting.class);
//            binder.bindAsContract(Conversation.class);
//        });

        Conversation conversation1 = injectionManager.getInstance(Conversation.class);
        Greeting greeting1 = conversation1.greeting;
        Conversation conversation2 = injectionManager.getInstance(Conversation.class);
        Greeting greeting2 = conversation2.greeting;
        Conversation conversation3 = injectionManager.getInstance(Conversation.class);
        Greeting greeting3 = conversation3.greeting;

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
//        BindingTestHelper.bind(injectionManager, binder -> {
//            binder.bindFactory(SupplierSingletonInstancePerLookup.class, Singleton.class)
//                    .to(GreetingSupplierSingletonInstancePerLookup.class);
//            binder.bindAsContract(ConversationSupplierSingletonInstancePerLookup.class);
//        });

        GreetingSupplierSingletonInstancePerLookup greeting1 = injectionManager
                .getInstance(ConversationSupplierSingletonInstancePerLookup.class).greeting;
        GreetingSupplierSingletonInstancePerLookup greeting2 = injectionManager
                .getInstance(ConversationSupplierSingletonInstancePerLookup.class).greeting;
        GreetingSupplierSingletonInstancePerLookup greeting3 = injectionManager
                .getInstance(ConversationSupplierSingletonInstancePerLookup.class).greeting;

        assertNotSame(greeting1, greeting2);
        assertNotSame(greeting2, greeting3);

        Supplier<GreetingSupplierSingletonInstancePerLookup> supplier1 = injectionManager
                .getInstance(ConversationSupplierSingletonInstancePerLookup.class).greetingSupplier;
        Supplier<GreetingSupplierSingletonInstancePerLookup> supplier2 = injectionManager
                .getInstance(ConversationSupplierSingletonInstancePerLookup.class).greetingSupplier;
        Supplier<GreetingSupplierSingletonInstancePerLookup> supplier3 = injectionManager
                .getInstance(ConversationSupplierSingletonInstancePerLookup.class).greetingSupplier;

        assertSame(supplier1, supplier2);
        assertSame(supplier2, supplier3);
    }

    @Test
    public void testSupplierPerLookupInstanceSingleton() {
//        BindingTestHelper.bind(injectionManager, binder -> {
//            binder.bindFactory(SupplierPerLookupInstanceSingleton.class)
//                    .to(GreetingSupplierPerLookupInstanceSingleton.class).in(Singleton.class);
//            binder.bindAsContract(ConversationSupplierPerLookupInstanceSingleton.class);
//        });

        GreetingSupplierPerLookupInstanceSingleton greeting1 = injectionManager
                .getInstance(ConversationSupplierPerLookupInstanceSingleton.class).greeting;
        GreetingSupplierPerLookupInstanceSingleton greeting2 = injectionManager
                .getInstance(ConversationSupplierPerLookupInstanceSingleton.class).greeting;
        GreetingSupplierPerLookupInstanceSingleton greeting3 = injectionManager
                .getInstance(ConversationSupplierPerLookupInstanceSingleton.class).greeting;

        assertSame(greeting1, greeting2);
        assertSame(greeting2, greeting3);

        Supplier<GreetingSupplierPerLookupInstanceSingleton> supplier1 = injectionManager
                .getInstance(ConversationSupplierPerLookupInstanceSingleton.class).greetingSupplier;
        Supplier<GreetingSupplierPerLookupInstanceSingleton> supplier2 = injectionManager
                .getInstance(ConversationSupplierPerLookupInstanceSingleton.class).greetingSupplier;
        Supplier<GreetingSupplierPerLookupInstanceSingleton> supplier3 = injectionManager
                .getInstance(ConversationSupplierPerLookupInstanceSingleton.class).greetingSupplier;

        assertNotSame(supplier1, supplier2);
        assertNotSame(supplier2, supplier3);
    }

    @Test
    public void testSupplierSingletonInstanceSingleton() {
//        BindingTestHelper.bind(injectionManager, binder -> {
//            binder.bindFactory(SupplierSingletonInstanceSingleton.class, Singleton.class)
//                    .to(GreetingSupplierSingletonInstanceSingleton.class).in(Singleton.class);
//            binder.bindAsContract(ConversationSupplierSingletonInstanceSingleton.class);
//        });

        GreetingSupplierSingletonInstanceSingleton greeting1 = injectionManager
                .getInstance(ConversationSupplierSingletonInstanceSingleton.class).greeting;
        GreetingSupplierSingletonInstanceSingleton greeting2 = injectionManager
                .getInstance(ConversationSupplierSingletonInstanceSingleton.class).greeting;
        GreetingSupplierSingletonInstanceSingleton greeting3 = injectionManager
                .getInstance(ConversationSupplierSingletonInstanceSingleton.class).greeting;

        assertSame(greeting1, greeting2);
        assertSame(greeting2, greeting3);

        Supplier<GreetingSupplierSingletonInstanceSingleton> supplier1 = injectionManager
                .getInstance(ConversationSupplierSingletonInstanceSingleton.class).greetingSupplier;
        Supplier<GreetingSupplierSingletonInstanceSingleton> supplier2 = injectionManager
                .getInstance(ConversationSupplierSingletonInstanceSingleton.class).greetingSupplier;
        Supplier<GreetingSupplierSingletonInstanceSingleton> supplier3 = injectionManager
                .getInstance(ConversationSupplierSingletonInstanceSingleton.class).greetingSupplier;

        assertSame(supplier1, supplier2);
        assertSame(supplier2, supplier3);
    }

    @Vetoed
    static class SupplierSingletonInstanceSingleton implements Supplier<GreetingSupplierSingletonInstanceSingleton> {
        @Override
        public GreetingSupplierSingletonInstanceSingleton get() {
            return new GreetingSupplierSingletonInstanceSingleton() {
                @Override
                public String getGreeting() {
                    return GREET;
                }
            };
        }
    }

    @Vetoed
    static class ConversationSupplierSingletonInstanceSingleton {
        @Inject
        GreetingSupplierSingletonInstanceSingleton greeting;

        @Inject
        Supplier<GreetingSupplierSingletonInstanceSingleton> greetingSupplier;
    }

    @FunctionalInterface
    static interface GreetingSupplierSingletonInstanceSingleton {
        String getGreeting();
    }

    // ---

    @Vetoed
    static class SupplierPerLookupInstanceSingleton implements Supplier<GreetingSupplierPerLookupInstanceSingleton> {
        @Override
        public GreetingSupplierPerLookupInstanceSingleton get() {
            return () -> GREET;
        }
    }

    @Vetoed
    static class ConversationSupplierPerLookupInstanceSingleton {
        @Inject
        GreetingSupplierPerLookupInstanceSingleton greeting;

        @Inject
        Supplier<GreetingSupplierPerLookupInstanceSingleton> greetingSupplier;
    }

    @FunctionalInterface
    static interface GreetingSupplierPerLookupInstanceSingleton {
        String getGreeting();
    }

    // ---

    @Vetoed
    static class SupplierSingletonInstancePerLookup implements Supplier<GreetingSupplierSingletonInstancePerLookup> {
        @Override
        public GreetingSupplierSingletonInstancePerLookup get() {
            return new GreetingSupplierSingletonInstancePerLookup() { // Using Lambda will provide singletons
                @Override
                public String getGreeting() {
                    return GREET;
                }
            };
        }
    }

    @Vetoed
    static class ConversationSupplierSingletonInstancePerLookup {
        @Inject
        GreetingSupplierSingletonInstancePerLookup greeting;

        @Inject
        Supplier<GreetingSupplierSingletonInstancePerLookup> greetingSupplier;
    }

    @FunctionalInterface
    static interface GreetingSupplierSingletonInstancePerLookup {
        String getGreeting();
    }

    // ---

    @Vetoed
    static class SupplierGreeting implements Supplier<Greeting> {
        @Override
        public Greeting get() {
            return new Greeting() { // Using Lambda will provide singletons
                @Override
                public String getGreeting() {
                    return GREET;
                }
            };
        }
    }

    @Vetoed
    static class Conversation {
        @Inject
        Greeting greeting;

        @Inject
        Supplier<Greeting> greetingSupplier;
    }

    @FunctionalInterface
    static interface Greeting {
        String getGreeting();
    }

    private static class SupplierClassBindingTestExtension implements Extension {
        void registerBindings(@Observes BeforeBeanDiscovery beforeBeanDiscovery, BeanManager beanManager) {
            AbstractBinder testBinder = new AbstractBinder() {
                @Override
                protected void configure() {
                    bindFactory(SupplierGreeting.class).to(Greeting.class);
                    bindAsContract(Conversation.class);

                    bindFactory(SupplierSingletonInstancePerLookup.class, Singleton.class)
                            .to(GreetingSupplierSingletonInstancePerLookup.class);
                    bindAsContract(ConversationSupplierSingletonInstancePerLookup.class);

                    bindFactory(SupplierPerLookupInstanceSingleton.class)
                            .to(GreetingSupplierPerLookupInstanceSingleton.class).in(Singleton.class);
                    bindAsContract(ConversationSupplierPerLookupInstanceSingleton.class);

                    bindFactory(SupplierSingletonInstanceSingleton.class, Singleton.class)
                            .to(GreetingSupplierSingletonInstanceSingleton.class).in(Singleton.class);
                    bindAsContract(ConversationSupplierSingletonInstanceSingleton.class);
                }
            };

            beanManager.getExtension(BinderRegisterExtension.class).register(beforeBeanDiscovery, testBinder.getBindings());
        }
    }

}
