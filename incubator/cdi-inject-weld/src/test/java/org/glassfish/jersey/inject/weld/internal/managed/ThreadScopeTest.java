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

package org.glassfish.jersey.inject.weld.internal.managed;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Vetoed;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import org.glassfish.jersey.internal.inject.PerThread;
import org.glassfish.jersey.process.internal.RequestScope;

import org.hamcrest.core.StringStartsWith;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Testing thread scope integration.
 *
 * @author Petr Bouda
 */
public class ThreadScopeTest extends TestParent {

    private static AtomicBoolean runOnlyOnceGuard = new AtomicBoolean(false);

    @BeforeEach
    public void initOnce() {
        if (!runOnlyOnceGuard.getAndSet(true)) {
            BindingTestHelper.bind(injectionManager, binder -> {
                binder.bindFactory(new SupplierGreeting())
                        .to(Greeting.class)
                        .in(PerThread.class);
            });
        }
    }

    @Test
    public void testThreadScopedInDifferentThread() throws InterruptedException {
//        InjectionManager injectionManager = BindingTestHelper.createInjectionManager();
//        BindingTestHelper.bind(injectionManager, binder -> {
//            binder.bindAsContract(SingletonObject.class)
//                    .in(Singleton.class);
//
//            binder.bindFactory(new SupplierGreeting())
//                    .to(Greeting.class)
//                    .in(PerThread.class);
//        });

        SingletonObject instance1 = injectionManager.getInstance(SingletonObject.class);
        Greeting greeting1 = instance1.getGreeting();
        String greetingString1 = greeting1.getGreeting();
        assertThat(greetingString1, StringStartsWith.startsWith(CzechGreeting.GREETING));

        CountDownLatch latch = new CountDownLatch(1);
        new Thread(() -> {
            // Precisely the same object
            SingletonObject instance2 = injectionManager.getInstance(SingletonObject.class);
            Greeting greeting2 = instance2.getGreeting();
            String greetingString2 = greeting2.getGreeting();
            assertThat(greetingString2, StringStartsWith.startsWith(CzechGreeting.GREETING));

            assertNotEquals(greetingString1, greetingString2);
            latch.countDown();
        }).start();

        latch.await();

        SingletonObject instance3 = injectionManager.getInstance(SingletonObject.class);
        assertEquals(instance3.getGreeting().getGreeting(), greetingString1);
    }

    @Test
    public void testThreadScopedInRequestScope() {
//        InjectionManager injectionManager = BindingTestHelper.createInjectionManager();
//        BindingTestHelper.bind(injectionManager, binder -> {
//            binder.bindAsContract(RequestScopedInterface.class)
//                    .in(RequestScoped.class);
//
//            binder.bindFactory(new SupplierGreeting())
//                    .to(Greeting.class)
//                    .in(PerThread.class);
//        });

        RequestScope request = injectionManager.getInstance(RequestScope.class);
        request.runInScope(() -> {
            RequestScopedInterface instance1 = injectionManager.getInstance(RequestScopedInterface.class);
            Greeting greeting1 = instance1.getGreeting();
            assertNotNull(greeting1);

            // Precisely the same object
            RequestScopedInterface instance2 = injectionManager.getInstance(RequestScopedInterface.class);
            Greeting greeting2 = instance2.getGreeting();
            assertNotNull(greeting2);

            assertEquals(greeting1, greeting2);
        });
    }

    @Test
    public void testThreadScopedInRequestScopeImplementation() {
//        InjectionManager injectionManager = BindingTestHelper.createInjectionManager();
//        BindingTestHelper.bind(injectionManager, binder -> {
//            binder.bindAsContract(RequestScopedCzech.class)
//                    .in(RequestScoped.class);
//
//            binder.bindFactory(new SupplierGreeting())
//                    .to(CzechGreeting.class)
//                    .in(PerThread.class);
//        });

        RequestScope request = injectionManager.getInstance(RequestScope.class);
        request.runInScope(() -> {
            RequestScopedCzech instance1 = injectionManager.getInstance(RequestScopedCzech.class);
            CzechGreeting greeting1 = instance1.getGreeting();
            assertNotNull(greeting1);

            // Precisely the same object
            RequestScopedCzech instance2 = injectionManager.getInstance(RequestScopedCzech.class);
            CzechGreeting greeting2 = instance2.getGreeting();
            assertNotNull(greeting2);

            assertEquals(greeting1, greeting2);
        });
    }

    @Test
    public void testThreadScopedInRequestTwoTypes() {
//        InjectionManager injectionManager = BindingTestHelper.createInjectionManager();
//        BindingTestHelper.bind(injectionManager, binder -> {
//            binder.bindAsContract(RequestScopedCzech.class)
//                    .in(RequestScoped.class);
//
//            binder.bindAsContract(RequestScopedEnglish.class)
//                    .in(RequestScoped.class);
//
//            binder.bindFactory(new SupplierGreeting(CzechGreeting.GREETING))
//                    .to(CzechGreeting.class)
//                    .in(PerThread.class);
//
//            binder.bindFactory(new SupplierGreeting(EnglishGreeting.GREETING))
//                    .to(EnglishGreeting.class)
//                    .in(PerThread.class);
//        });

        RequestScope request = injectionManager.getInstance(RequestScope.class);
        request.runInScope(() -> {
            RequestScopedCzech2 instance1 = injectionManager.getInstance(RequestScopedCzech2.class);
            CzechGreeting2 greeting1 = instance1.getGreeting();
            assertNotNull(greeting1);

            // Precisely the same object
            RequestScopedEnglish2 instance2 = injectionManager.getInstance(RequestScopedEnglish2.class);
            EnglishGreeting2 greeting2 = instance2.getGreeting();
            assertNotNull(greeting2);

            assertNotSame(greeting1, greeting2);
        });
    }

    @Test
    public void testThreadScopedInSingletonScope() throws InterruptedException {
//        InjectionManager injectionManager = BindingTestHelper.createInjectionManager();
//        BindingTestHelper.bind(injectionManager, binder -> {
//            binder.bindAsContract(SingletonObject.class)
//                    .in(Singleton.class);
//
//            binder.bindFactory(new SupplierGreeting())
//                    .to(Greeting.class)
//                    .in(PerThread.class);
//        });

        SingletonObject instance1 = injectionManager.getInstance(SingletonObject.class);
        Greeting greeting1 = instance1.getGreeting();
        assertNotNull(greeting1);

        // Precisely the same object
        SingletonObject instance2 = injectionManager.getInstance(SingletonObject.class);
        Greeting greeting2 = instance2.getGreeting();
        assertNotNull(greeting2);

        assertEquals(greeting1, greeting2);

        final AtomicReference<String> greetingAtomicReference = new AtomicReference<>();
        Runnable runnable = () ->
                greetingAtomicReference.set(injectionManager.getInstance(SingletonObject.class).getGreeting().getGreeting());

        Thread newThread = new Thread(runnable);
        newThread.start();
        newThread.join();

        assertEquals(greeting1.getGreeting(), greeting2.getGreeting());
        assertNotEquals(greeting1.getGreeting(), greetingAtomicReference.get());
    }

    @Test
    public void testSupplierClassBindingThreadScopedInSingletonScope() throws InterruptedException {
//        InjectionManager injectionManager = BindingTestHelper.createInjectionManager();
//        BindingTestHelper.bind(injectionManager, binder -> {
//            binder.bindAsContract(SingletonObject.class)
//                    .in(Singleton.class);
//
//            binder.bindFactory(SupplierGreeting.class)
//                    .to(Greeting.class)
//                    .in(PerThread.class);
//        });

        SingletonObject3 instance1 = injectionManager.getInstance(SingletonObject3.class);
        Greeting3 greeting1 = instance1.getGreeting();
        assertNotNull(greeting1);

        // Precisely the same object
        SingletonObject3 instance2 = injectionManager.getInstance(SingletonObject3.class);
        Greeting3 greeting2 = instance2.getGreeting();
        assertNotNull(greeting2);

        assertEquals(greeting1, greeting2);

        final AtomicReference<String> greetingAtomicReference = new AtomicReference<>();
        Runnable runnable = () ->
                greetingAtomicReference.set(injectionManager.getInstance(SingletonObject3.class).getGreeting().getGreeting());

        Thread newThread = new Thread(runnable);
        newThread.start();
        newThread.join();

        assertEquals(greeting1.getGreeting(), greeting2.getGreeting());
        assertNotEquals(greeting1.getGreeting(), greetingAtomicReference.get());
    }

    @RequestScoped
    public static class RequestScopedInterface {

        @Inject
        Greeting greeting;

        public Greeting getGreeting() {
            return greeting;
        }
    }

    @RequestScoped
    public static class RequestScopedCzech {

        @Inject
        CzechGreeting greeting;

        public CzechGreeting getGreeting() {
            return greeting;
        }
    }

    @Singleton
    public static class SingletonObject3 {

        @Inject
        Greeting3 greeting;

        public Greeting3 getGreeting() {
            return greeting;
        }
    }

    @Singleton
    public static class SingletonObject {

        @Inject
        Greeting greeting;

        public Greeting getGreeting() {
            return greeting;
        }
    }

    @RequestScoped
    public static class RequestScopedCzech2 {

        @Inject
        CzechGreeting2 greeting;

        public CzechGreeting2 getGreeting() {
            return greeting;
        }
    }

    @RequestScoped
    public static class RequestScopedEnglish2 {

        @Inject
        EnglishGreeting2 greeting;

        public EnglishGreeting2 getGreeting() {
            return greeting;
        }
    }

    @Vetoed
    static class SupplierGreeting3 implements Supplier<Greeting3> {
        @Override
        public Greeting3 get() {
            return new CzechGreeting3();
        }
    }

    @Vetoed
    static class SupplierGreeting2 implements Supplier<Greeting2> {

        private final String greetingType;

        /**
         * Default constructor.
         */
        public SupplierGreeting2() {
            this(CzechGreeting.GREETING);
        }

        /**
         * Supplier's constructor.
         *
         * @param greetingType greetingType in a specific language.
         */
        public SupplierGreeting2(String greetingType) {
            this.greetingType = greetingType;
        }

        @Override
        public Greeting2 get() {
            if (CzechGreeting2.GREETING.equals(greetingType)) {
                return new CzechGreeting2();
            } else {
                return new EnglishGreeting2();
            }
        }
    }

    @Vetoed
    static class CzechGreeting3 implements Greeting3 {

        static final String GREETING = "Ahoj";

        private String greeting = GREETING + "#" + Thread.currentThread().getName();

        @Override
        public String getGreeting() {
            return greeting;
        }

        @Override
        public String toString() {
            return "CzechGreeting";
        }
    }

    @Vetoed
    static class CzechGreeting2 implements Greeting2 {

        static final String GREETING = "Ahoj";

        private String greeting = GREETING + "#" + Thread.currentThread().getName();

        @Override
        public String getGreeting() {
            return greeting;
        }

        @Override
        public String toString() {
            return "CzechGreeting";
        }
    }

    @Vetoed
    static class EnglishGreeting2 implements Greeting2 {

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

    @Vetoed
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

    @Vetoed
    static class CzechGreeting implements Greeting {

        static final String GREETING = "Ahoj";

        private String greeting = GREETING + "#" + Thread.currentThread().getName();

        @Override
        public String getGreeting() {
            return greeting;
        }

        @Override
        public String toString() {
            return "CzechGreeting";
        }
    }

    @FunctionalInterface
    static interface Greeting3 {
        String getGreeting();
    }

    @FunctionalInterface
    static interface Greeting2 {
        String getGreeting();
    }

    @FunctionalInterface
    static interface Greeting {
        String getGreeting();
    }
}
