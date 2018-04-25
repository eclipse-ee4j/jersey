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

import java.lang.reflect.Type;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import javax.ws.rs.core.GenericType;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.glassfish.jersey.internal.inject.DisposableSupplier;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.process.internal.RequestScope;
import org.glassfish.jersey.process.internal.RequestScoped;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

/**
 * Tests that {@link org.glassfish.jersey.internal.inject.DisposableSupplier} is properly processed by {@link Hk2Helper}.
 *
 * @author Petr Bouda
 */
public class DisposableSupplierTest {

    private static final Type DISPOSABLE_SUPPLIER_TYPE = new GenericType<DisposableSupplier<String>>() {}.getType();
    private static final Type SUPPLIER_TYPE = new GenericType<Supplier<String>>() {}.getType();

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
    public void testBindSingletonClassDisposableSupplier() {
        BindingTestHelper.bind(
                injectionManager, binder ->  binder.bindFactory(DisposableSupplierImpl.class, Singleton.class).to(String.class));

        Object supplier = injectionManager.getInstance(SUPPLIER_TYPE);
        Object disposableSupplier = injectionManager.getInstance(DISPOSABLE_SUPPLIER_TYPE);
        assertNotNull(supplier);
        assertNotNull(disposableSupplier);
        assertSame(supplier, disposableSupplier);
    }

    @Test
    public void testBindPerLookupClassDisposableSupplier() {
        BindingTestHelper.bind(injectionManager, binder -> binder.bindFactory(DisposableSupplierImpl.class).to(String.class));

        Object supplier = injectionManager.getInstance(SUPPLIER_TYPE);
        Object disposableSupplier = injectionManager.getInstance(DISPOSABLE_SUPPLIER_TYPE);
        assertNotNull(supplier);
        assertNotNull(disposableSupplier);
        assertNotSame(supplier, disposableSupplier);
    }

    @Test
    public void testBindInstanceDisposableSupplier() {
        BindingTestHelper.bind(injectionManager, binder -> binder.bindFactory(new DisposableSupplierImpl()).to(String.class));

        Object supplier = injectionManager.getInstance(SUPPLIER_TYPE);
        Object disposableSupplier = injectionManager.getInstance(DISPOSABLE_SUPPLIER_TYPE);
        assertNotNull(supplier);
        assertNotNull(disposableSupplier);
        assertSame(supplier, disposableSupplier);
    }

    @Test
    public void testNotBindClassDisposableSupplier() {
        BindingTestHelper.bind(injectionManager, binder -> binder.bindFactory(SupplierGreeting.class).to(String.class));
        assertNull(injectionManager.getInstance(DISPOSABLE_SUPPLIER_TYPE));
    }

    @Test
    public void testNotBindInstanceDisposableSupplier() {
        BindingTestHelper.bind(injectionManager, binder -> binder.bindFactory(new SupplierGreeting()).to(String.class));
        assertNull(injectionManager.getInstance(DISPOSABLE_SUPPLIER_TYPE));
    }

    @Test
    public void testOnlyIncrementSingletonSupplier() {
        BindingTestHelper.bind(injectionManager, binder -> binder.bindFactory(DisposableSupplierImpl.class, Singleton.class)
                        .to(String.class));

        Object instance1 = injectionManager.getInstance(DISPOSABLE_SUPPLIER_TYPE);
        assertEquals("1", ((DisposableSupplier<?>) instance1).get());
        Object instance2 = injectionManager.getInstance(DISPOSABLE_SUPPLIER_TYPE);
        assertEquals("2", ((DisposableSupplier<?>) instance2).get());
        Object instance3 = injectionManager.getInstance(DISPOSABLE_SUPPLIER_TYPE);
        assertEquals("3", ((DisposableSupplier<?>) instance3).get());
    }

    @Test
    public void testOnlyIncrementInstanceSupplier() {
        BindingTestHelper.bind(injectionManager, binder -> binder.bindFactory(new DisposableSupplierImpl())
                        .to(String.class));

        Object instance1 = injectionManager.getInstance(DISPOSABLE_SUPPLIER_TYPE);
        assertEquals("1", ((DisposableSupplier<?>) instance1).get());
        Object instance2 = injectionManager.getInstance(DISPOSABLE_SUPPLIER_TYPE);
        assertEquals("2", ((DisposableSupplier<?>) instance2).get());
        Object instance3 = injectionManager.getInstance(DISPOSABLE_SUPPLIER_TYPE);
        assertEquals("3", ((DisposableSupplier<?>) instance3).get());
    }

    @Test
    public void testOnlyIncrementPerLookupSupplier() {
        BindingTestHelper.bind(injectionManager, binder -> binder.bindFactory(DisposableSupplierImpl.class)
                        .to(String.class));

        Object instance1 = injectionManager.getInstance(DISPOSABLE_SUPPLIER_TYPE);
        assertEquals("1", ((DisposableSupplier<?>) instance1).get());
        Object instance2 = injectionManager.getInstance(DISPOSABLE_SUPPLIER_TYPE);
        assertEquals("1", ((DisposableSupplier<?>) instance2).get());
        Object instance3 = injectionManager.getInstance(DISPOSABLE_SUPPLIER_TYPE);
        assertEquals("1", ((DisposableSupplier<?>) instance3).get());
    }

    @Test
    public void testOnlyIncrementSingletonInstances() {
        BindingTestHelper.bind(injectionManager, binder -> binder.bindFactory(DisposableSupplierImpl.class, Singleton.class)
                        .to(String.class));

        Object instance1 = injectionManager.getInstance(String.class);
        assertEquals("1", instance1);
        Object instance2 = injectionManager.getInstance(String.class);
        assertEquals("2", instance2);
        Object instance3 = injectionManager.getInstance(String.class);
        assertEquals("3", instance3);
    }

    @Test
    public void testOnlyIncrementInstanceInstance() {
        BindingTestHelper.bind(injectionManager, binder -> binder.bindFactory(new DisposableSupplierImpl())
                        .to(String.class));

        Object instance1 = injectionManager.getInstance(String.class);
        assertEquals("1", instance1);
        Object instance2 = injectionManager.getInstance(String.class);
        assertEquals("2", instance2);
        Object instance3 = injectionManager.getInstance(String.class);
        assertEquals("3", instance3);
    }

    @Test
    public void testOnlyIncrementPerLookupInstance() {
        BindingTestHelper
                .bind(injectionManager, binder -> binder.bindFactory(DisposableSupplierImpl.class)
                        .to(String.class));

        Object instance1 = injectionManager.getInstance(String.class);
        assertEquals("1", instance1);
        Object instance2 = injectionManager.getInstance(String.class);
        assertEquals("1", instance2);
        Object instance3 = injectionManager.getInstance(String.class);
        assertEquals("1", instance3);
    }

    @Test
    public void testDisposeSingletonSupplier() {
        BindingTestHelper.bind(injectionManager, binder -> binder.bindFactory(DisposableSupplierImpl.class, Singleton.class)
                        .to(String.class));

        // 1-1
        DisposableSupplier<String> supplier1 =
                injectionManager.getInstance(DISPOSABLE_SUPPLIER_TYPE);
        String instance1 = supplier1.get();
        // 2-2
        DisposableSupplier<String> supplier2 =
                injectionManager.getInstance(DISPOSABLE_SUPPLIER_TYPE);
        String instance2 = supplier2.get();
        // 3-3
        DisposableSupplier<String> supplier3 =
                injectionManager.getInstance(DISPOSABLE_SUPPLIER_TYPE);
        supplier3.get();
        // 2-2
        supplier1.dispose(instance1);
        // 1-1
        supplier2.dispose(instance2);
        // 2-2
        Supplier<String> supplier4 = injectionManager.getInstance(DISPOSABLE_SUPPLIER_TYPE);
        String result = supplier4.get();
        assertEquals("2", result);
    }

    @Test
    public void testDisposePerLookupSupplier() {
        BindingTestHelper.bind(injectionManager, binder -> binder.bindFactory(DisposableSupplierImpl.class)
                .to(String.class));

        // 1
        DisposableSupplier<String> supplier1 =
                injectionManager.getInstance(DISPOSABLE_SUPPLIER_TYPE);
        String instance1 = supplier1.get();
        // 1
        DisposableSupplier<String> supplier2 =
                injectionManager.getInstance(DISPOSABLE_SUPPLIER_TYPE);
        String instance2 = supplier2.get();
        // 1
        DisposableSupplier<String> supplier3 =
                injectionManager.getInstance(DISPOSABLE_SUPPLIER_TYPE);
        supplier3.get();
        // 0
        supplier1.dispose(instance1);
        // 0
        supplier2.dispose(instance2);
        // 1
        Supplier<String> supplier4 = injectionManager.getInstance(DISPOSABLE_SUPPLIER_TYPE);
        String result = supplier4.get();
        assertEquals("1", result);
    }

    @Test
    public void testDisposeSingletonSupplierRequestScopedInstance() {
        BindingTestHelper.bind(injectionManager, binder -> {
                    binder.bindFactory(DisposableSupplierImpl.class, Singleton.class)
                            .to(String.class)
                            .in(RequestScoped.class);

                    binder.bind(Hk2RequestScope.class)
                            .to(RequestScope.class);
                });

        RequestScope request = injectionManager.getInstance(RequestScope.class);
        AtomicReference<Supplier<String>> atomicSupplier = new AtomicReference<>();
        request.runInScope(() -> {
            // Save Singleton Supplier for later check that the instance was disposed.
            Supplier<String> supplier = injectionManager.getInstance(DISPOSABLE_SUPPLIER_TYPE);
            atomicSupplier.set(supplier);

            // All instances should be the same because they are request scoped.
            Object instance1 = injectionManager.getInstance(String.class);
            assertEquals("1", instance1);
            Object instance2 = injectionManager.getInstance(String.class);
            assertEquals("1", instance2);
        });

        Supplier<String> cleanedSupplier = atomicSupplier.get();
        // Next should be 1-1
        assertEquals("1", cleanedSupplier.get());
    }

    /**
     * Tests that object created in request scope is disposing at the time of ending the scope.
     */
    @Test
    public void testDisposePerLookupSupplierRequestScopedInstance() {
        BindingTestHelper.bind(injectionManager, binder -> {
                    binder.bindFactory(DisposableSupplierImpl.class)
                            .to(String.class)
                            .in(RequestScoped.class);

                    binder.bind(Hk2RequestScope.class)
                            .to(RequestScope.class);
                });

        RequestScope request = injectionManager.getInstance(RequestScope.class);
        AtomicReference<Supplier<String>> atomicSupplier = new AtomicReference<>();
        request.runInScope(() -> {
            // Save Singleton Supplier for later check that the instance was disposed.
            Supplier<String> supplier = injectionManager.getInstance(DISPOSABLE_SUPPLIER_TYPE);
            atomicSupplier.set(supplier);

            // All instances should be the same because they are request scoped.
            Object instance1 = injectionManager.getInstance(String.class);
            assertEquals("1", instance1);
            Object instance2 = injectionManager.getInstance(String.class);
            assertEquals("1", instance2);
        });

        Supplier<String> cleanedSupplier = atomicSupplier.get();
        // Next should be 1
        assertEquals("1", cleanedSupplier.get());
    }

    /**
     * Tests that inherited request scoped is also cleaned by disposing the objects.
     */
    @Test
    public void testDisposeSingletonSupplierMultiRequestScoped() {
        BindingTestHelper.bind(injectionManager, binder -> {
                    binder.bindFactory(DisposableSupplierImpl.class)
                            .to(String.class)
                            .in(RequestScoped.class);

                    binder.bind(Hk2RequestScope.class)
                            .to(RequestScope.class);
                });

        RequestScope request = injectionManager.getInstance(RequestScope.class);
        AtomicReference<Supplier<String>> firstSupplier = new AtomicReference<>();
        AtomicReference<Supplier<String>> secondSupplier = new AtomicReference<>();
        request.runInScope(() -> {
            Supplier<String> supplier1 = injectionManager.getInstance(DISPOSABLE_SUPPLIER_TYPE);
            firstSupplier.set(supplier1);

            Object instance1 = injectionManager.getInstance(String.class);
            assertEquals("1", instance1);

            request.runInScope(() -> {
                // Save Singleton Supplier for later check that the instance was disposed.
                Supplier<String> supplier2 =
                        injectionManager.getInstance(DISPOSABLE_SUPPLIER_TYPE);
                secondSupplier.set(supplier2);

                Object instance2 = injectionManager.getInstance(String.class);
                // 1-2 because the same static class is used in inherited runInScope
                assertEquals("1", instance2);
            });
        });

        Supplier<String> cleanedSupplier1 = firstSupplier.get();
        Supplier<String> cleanedSupplier2 = secondSupplier.get();
        // Next should be 1-1
        assertEquals("1", cleanedSupplier1.get());
        // 1-2 because the same static class is used but the instance is cleaned.
        assertEquals("1", cleanedSupplier2.get());
    }

    /**
     * PerLookup fields are not disposed therefore they should never be used as a DisposedSupplier because the field stay in
     * {@link SupplierFactoryBridge} forever.
     */
    @Test
    public void testDisposeComposedObjectWithPerLookupFields() {
        BindingTestHelper.bind(injectionManager, binder -> {
                    binder.bindFactory(DisposableSupplierImpl.class, Singleton.class)
                            .to(String.class);

                    binder.bindAsContract(ComposedObject.class)
                            .in(RequestScoped.class);

                    binder.bind(Hk2RequestScope.class)
                            .to(RequestScope.class);
                });

        RequestScope request = injectionManager.getInstance(RequestScope.class);
        AtomicReference<Supplier<String>> atomicSupplier = new AtomicReference<>();
        request.runInScope(() -> {
            // Save Singleton Supplier for later check that the instance was disposed.
            Supplier<String> supplier = injectionManager.getInstance(DISPOSABLE_SUPPLIER_TYPE);
            atomicSupplier.set(supplier);

            // All instances should be the same because they are request scoped.
            ComposedObject instance = injectionManager.getInstance(ComposedObject.class);
            assertEquals("1", instance.first);
            assertEquals("2", instance.second);
            assertEquals("3", instance.third);
        });

        Supplier<String> cleanedSupplier = atomicSupplier.get();
        // Next should be 4
        assertEquals("4", cleanedSupplier.get());
    }

    private static class ComposedObject {

        @Inject
        String first;

        @Inject
        String second;

        @Inject
        String third;
    }

    private static class DisposableSupplierImpl implements DisposableSupplier<String> {
        private final AtomicInteger counter = new AtomicInteger();

        @Override
        public String get() {
            // Create a new string - don't share the instances in the string pool.
            return new String(counter.incrementAndGet() + "");
        }

        @Override
        public void dispose(final String instance) {
            counter.decrementAndGet();
        }
    }
}
