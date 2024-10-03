/*
 * Copyright (c) 2017, 2024 Oracle and/or its affiliates. All rights reserved.
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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;
import javax.xml.parsers.SAXParserFactory;

import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.internal.inject.PerThread;
import org.glassfish.jersey.process.internal.RequestScope;

import org.hamcrest.core.StringStartsWith;
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
public class ThreadScopeTest {

    @Test
    public void testThreadScopedInDifferentThread() throws InterruptedException {
        InjectionManager injectionManager = BindingTestHelper.createInjectionManager();
        BindingTestHelper.bind(injectionManager, binder -> {
            binder.bindAsContract(SingletonObject.class)
                    .in(Singleton.class);

            binder.bindFactory(new SupplierGreeting())
                    .to(Greeting.class)
                    .in(PerThread.class);
        });

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
        InjectionManager injectionManager = BindingTestHelper.createInjectionManager();
        BindingTestHelper.bind(injectionManager, binder -> {
            binder.bindAsContract(RequestScopedInterface.class)
                    .in(RequestScoped.class);

            binder.bindFactory(new SupplierGreeting())
                    .to(Greeting.class)
                    .in(PerThread.class);
        });

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
        InjectionManager injectionManager = BindingTestHelper.createInjectionManager();
        BindingTestHelper.bind(injectionManager, binder -> {
            binder.bindAsContract(RequestScopedCzech.class)
                    .in(RequestScoped.class);

            binder.bindFactory(new SupplierGreeting())
                    .to(CzechGreeting.class)
                    .in(PerThread.class);
        });

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
        InjectionManager injectionManager = BindingTestHelper.createInjectionManager();
        BindingTestHelper.bind(injectionManager, binder -> {
            binder.bindAsContract(RequestScopedCzech.class)
                    .in(RequestScoped.class);

            binder.bindAsContract(RequestScopedEnglish.class)
                    .in(RequestScoped.class);

            binder.bindFactory(new SupplierGreeting(CzechGreeting.GREETING))
                    .to(CzechGreeting.class)
                    .in(PerThread.class);

            binder.bindFactory(new SupplierGreeting(EnglishGreeting.GREETING))
                    .to(EnglishGreeting.class)
                    .in(PerThread.class);
        });

        RequestScope request = injectionManager.getInstance(RequestScope.class);
        request.runInScope(() -> {
            RequestScopedCzech instance1 = injectionManager.getInstance(RequestScopedCzech.class);
            CzechGreeting greeting1 = instance1.getGreeting();
            assertNotNull(greeting1);

            // Precisely the same object
            RequestScopedEnglish instance2 = injectionManager.getInstance(RequestScopedEnglish.class);
            EnglishGreeting greeting2 = instance2.getGreeting();
            assertNotNull(greeting2);

            assertNotSame(greeting1, greeting2);
        });
    }

    @Test
    public void testThreadScopedInSingletonScope() throws InterruptedException {
        InjectionManager injectionManager = BindingTestHelper.createInjectionManager();
        BindingTestHelper.bind(injectionManager, binder -> {
            binder.bindAsContract(SingletonObject.class)
                    .in(Singleton.class);

            binder.bindFactory(new SupplierGreeting())
                    .to(Greeting.class)
                    .in(PerThread.class);
        });

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
        InjectionManager injectionManager = BindingTestHelper.createInjectionManager();
        BindingTestHelper.bind(injectionManager, binder -> {
            binder.bindAsContract(SingletonObject.class)
                    .in(Singleton.class);

            binder.bindFactory(SupplierGreeting.class)
                    .to(Greeting.class)
                    .in(PerThread.class);
        });

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

    @RequestScoped
    public static class RequestScopedProvider {

        @Inject
        Provider<SAXParserFactory> greeting;

        public Provider<SAXParserFactory> provider() {
            return greeting;
        }
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

    @RequestScoped
    public static class RequestScopedEnglish {

        @Inject
        EnglishGreeting greeting;

        public EnglishGreeting getGreeting() {
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
}
