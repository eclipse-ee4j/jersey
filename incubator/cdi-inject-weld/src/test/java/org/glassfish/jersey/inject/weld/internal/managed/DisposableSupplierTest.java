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

import java.lang.reflect.Type;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import javax.enterprise.inject.Vetoed;
import javax.inject.Inject;
import javax.ws.rs.core.GenericType;

import org.glassfish.jersey.inject.weld.internal.bean.BeanHelper;
import org.glassfish.jersey.internal.inject.DisposableSupplier;
import org.glassfish.jersey.process.internal.RequestScope;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Tests that {@link DisposableSupplier} is properly processed by {@link BeanHelper}.
 *
 * @author Petr Bouda
 */
@Vetoed
public class DisposableSupplierTest extends TestParent {

    private static final Type DISPOSABLE_SUPPLIER_CLASS_TYPE =
            new GenericType<DisposableSupplier<StringForSupplierClass>>() {}.getType();
    private static final Type DISPOSABLE_SUPPLIER_SINGLETON_CLASS_TYPE =
            new GenericType<DisposableSupplier<StringForSupplierSingletonClass>>() {}.getType();
    private static final Type DISPOSABLE_SUPPLIER_INSTANCE_TYPE =
            new GenericType<DisposableSupplier<StringForSupplierInstance>>() {}.getType();
    private static final Type PROXIABLE_DISPOSABLE_SUPPLIER_CLASS_TYPE =
            new GenericType<DisposableSupplier<ProxiableHolderClass>>() {}.getType();

    private static AtomicBoolean onlyOnceGuard = new AtomicBoolean(false);

    @BeforeEach
    public void bindInit() {
        if (!onlyOnceGuard.getAndSet(true)) {
            BindingTestHelper.bind(injectionManager, binder -> binder.bindFactory(new DisposableSupplierImpl())
                    .to(StringForSupplierInstance.class));
        }
    }

    @Test
    public void testBindSingletonClassDisposableSupplier() {
//        BindingTestHelper.bind(injectionManager, binder ->  binder.bindFactory(DisposableSupplierImpl.class, Singleton.class)
//                .to(StringForSupplierSingletonClass.class));

        Object supplier = injectionManager.getInstance(new GenericType<Supplier<StringForSupplierSingletonClass>>() {}.getType());
        Object disposableSupplier = injectionManager.getInstance(DISPOSABLE_SUPPLIER_SINGLETON_CLASS_TYPE);
        assertNotNull(supplier);
        assertNotNull(disposableSupplier);
        assertSame(supplier, disposableSupplier);
    }

    @Test
    public void testBindPerLookupClassDisposableSupplier() {
//        BindingTestHelper.bind(injectionManager, binder -> binder.bindFactory(DisposableSupplierImpl.class)
//                .to(StringForSupplierClass.class));

        Object supplier = injectionManager.getInstance(new GenericType<Supplier<StringForSupplierClass>>() {}.getType());
        Object disposableSupplier = injectionManager.getInstance(DISPOSABLE_SUPPLIER_CLASS_TYPE);
        assertNotNull(supplier);
        assertNotNull(disposableSupplier);
        assertNotSame(supplier, disposableSupplier);
    }

    @Test
    public void testBindInstanceDisposableSupplier() {
//        BindingTestHelper.bind(injectionManager, binder -> binder.bindFactory(new DisposableSupplierImpl())
//                .to(StringForSupplierInstance.class));

        Object supplier = injectionManager.getInstance(new GenericType<Supplier<StringForSupplierInstance>>() {}.getType());
        Object disposableSupplier = injectionManager.getInstance(DISPOSABLE_SUPPLIER_INSTANCE_TYPE);
        assertNotNull(supplier);
        assertNotNull(disposableSupplier);
        assertSame(supplier, disposableSupplier);
    }

    @Test
    public void testNotBindClassDisposableSupplier() {
//        BindingTestHelper.bind(injectionManager, binder -> binder.bindFactory(SupplierGreeting.class).to(GreetingsClass.class));
        assertNull(injectionManager.getInstance(new GenericType<DisposableSupplier<GreetingsClass>>() {}.getType()));
    }

    @Test
    public void testNotBindInstanceDisposableSupplier() {
//        BindingTestHelper.bind(injectionManager,
//                binder -> binder.bindFactory(new SupplierGreeting()).to(GreetingsInstance.class));
        assertNull(injectionManager.getInstance(new GenericType<DisposableSupplier<GreetingsInstance>>() {}.getType()));
    }

    @Test
    public void testOnlyIncrementSingletonSupplier() {
//        BindingTestHelper.bind(injectionManager, binder -> binder.bindFactory(DisposableSupplierImpl.class, Singleton.class)
//                        .to(StringForSupplierSingletonClass.class));

        Object instance1 = injectionManager.getInstance(DISPOSABLE_SUPPLIER_SINGLETON_CLASS_TYPE);
        assertEquals("1", ((DisposableSupplier<?>) instance1).get());
        Object instance2 = injectionManager.getInstance(DISPOSABLE_SUPPLIER_SINGLETON_CLASS_TYPE);
        assertEquals("2", ((DisposableSupplier<?>) instance2).get());
        Object instance3 = injectionManager.getInstance(DISPOSABLE_SUPPLIER_SINGLETON_CLASS_TYPE);
        assertEquals("3", ((DisposableSupplier<?>) instance3).get());
        dispose(instance1, instance2, instance3);
    }

    @Test
    public void testOnlyIncrementInstanceSupplier() {
//        BindingTestHelper.bind(injectionManager, binder -> binder.bindFactory(new DisposableSupplierImpl())
//                        .to(StringForSupplierInstance.class));

        Object instance1 = injectionManager.getInstance(DISPOSABLE_SUPPLIER_INSTANCE_TYPE);
        assertEquals("1", ((DisposableSupplier<?>) instance1).get());
        Object instance2 = injectionManager.getInstance(DISPOSABLE_SUPPLIER_INSTANCE_TYPE);
        assertEquals("2", ((DisposableSupplier<?>) instance2).get());
        Object instance3 = injectionManager.getInstance(DISPOSABLE_SUPPLIER_INSTANCE_TYPE);
        assertEquals("3", ((DisposableSupplier<?>) instance3).get());
        dispose(instance1, instance2, instance3);
    }

    @Test
    public void testOnlyIncrementPerLookupSupplier() {
//        BindingTestHelper.bind(injectionManager, binder -> binder.bindFactory(DisposableSupplierImpl.class)
//                        .to(StringForSupplierClass.class));

        Object instance1 = injectionManager.getInstance(DISPOSABLE_SUPPLIER_CLASS_TYPE);
        assertEquals("1", ((DisposableSupplier<?>) instance1).get());
        Object instance2 = injectionManager.getInstance(DISPOSABLE_SUPPLIER_CLASS_TYPE);
        assertEquals("1", ((DisposableSupplier<?>) instance2).get());
        Object instance3 = injectionManager.getInstance(DISPOSABLE_SUPPLIER_CLASS_TYPE);
        assertEquals("1", ((DisposableSupplier<?>) instance3).get());
        dispose(instance1, instance2, instance3);
    }

    @Test
    public void testOnlyIncrementSingletonInstances() {
//        BindingTestHelper.bind(injectionManager, binder -> binder.bindFactory(DisposableSupplierImpl.class, Singleton.class)
//                        .to(StringForSupplierSingletonClass.class));

        Object instance1 = injectionManager.getInstance(StringForSupplierSingletonClass.class);
        assertEquals("1", instance1);
        Object instance2 = injectionManager.getInstance(StringForSupplierSingletonClass.class);
        assertEquals("2", instance2);
        Object instance3 = injectionManager.getInstance(StringForSupplierSingletonClass.class);
        assertEquals("3", instance3);
        Object o = injectionManager.getInstance(
                new GenericType<DisposableSupplier<StringForSupplierSingletonClass>>() {}.getType());
        dispose(o, o, o);
    }

    @Test
    public void testOnlyIncrementInstanceInstance() {
//        BindingTestHelper.bind(injectionManager, binder -> binder.bindFactory(new DisposableSupplierImpl())
//                        .to(StringForSupplierInstance.class));

        Object instance1 = injectionManager.getInstance(StringForSupplierInstance.class);
        assertEquals("1", instance1);
        Object instance2 = injectionManager.getInstance(StringForSupplierInstance.class);
        assertEquals("2", instance2);
        Object instance3 = injectionManager.getInstance(StringForSupplierInstance.class);
        assertEquals("3", instance3);
        Object o = injectionManager.getInstance(new GenericType<DisposableSupplier<StringForSupplierInstance>>() {}.getType());
        dispose(o, o, o);
    }

    @Test
    public void testDisposeSingletonSupplier() {
//        BindingTestHelper.bind(injectionManager, binder -> binder.bindFactory(DisposableSupplierImpl.class, Singleton.class)
//                        .to(StringForSupplierSingletonClass.class));

        // 1-1
        DisposableSupplier<StringForSupplierSingletonClass> supplier1 =
                injectionManager.getInstance(DISPOSABLE_SUPPLIER_SINGLETON_CLASS_TYPE);
        CharSequence instance1 = supplier1.get();
        // 2-2
        DisposableSupplier<StringForSupplierSingletonClass> supplier2 =
                injectionManager.getInstance(DISPOSABLE_SUPPLIER_SINGLETON_CLASS_TYPE);
        CharSequence instance2 = supplier2.get();
        // 3-3
        DisposableSupplier<StringForSupplierSingletonClass> supplier3 =
                injectionManager.getInstance(DISPOSABLE_SUPPLIER_SINGLETON_CLASS_TYPE);
        supplier3.get();
        // 2-2
        supplier1.dispose(null);
        // 1-1
        supplier2.dispose(null);
        // 2-2
        Supplier<StringForSupplierSingletonClass> supplier4 = injectionManager.getInstance(
                DISPOSABLE_SUPPLIER_SINGLETON_CLASS_TYPE);
        CharSequence result = supplier4.get();
        assertEquals("2", result);
        dispose(supplier3, supplier4);
    }

    @Test
    public void testDisposePerLookupSupplier() {
//        BindingTestHelper.bind(injectionManager, binder -> binder.bindFactory(DisposableSupplierImpl.class)
//                .to(StringForSupplierClass.class));

        // 1
        DisposableSupplier<StringForSupplierClass> supplier1 =
                injectionManager.getInstance(DISPOSABLE_SUPPLIER_CLASS_TYPE);
        CharSequence instance1 = supplier1.get();
        // 1
        DisposableSupplier<StringForSupplierClass> supplier2 =
                injectionManager.getInstance(DISPOSABLE_SUPPLIER_CLASS_TYPE);
        CharSequence instance2 = supplier2.get();
        // 1
        DisposableSupplier<StringForSupplierClass> supplier3 =
                injectionManager.getInstance(DISPOSABLE_SUPPLIER_CLASS_TYPE);
        supplier3.get();
        // 0
        supplier1.dispose(null);
        // 0
        supplier2.dispose(null);
        // 1
        Supplier<StringForSupplierClass> supplier4 = injectionManager.getInstance(DISPOSABLE_SUPPLIER_CLASS_TYPE);
        CharSequence result = supplier4.get();
        assertEquals("1", result);
        dispose(supplier3, supplier4);
    }

    @Test
    public void testDisposeSingletonSupplierRequestScopedInstance() {
//        BindingTestHelper.bind(injectionManager, binder -> {
//                    binder.bindFactory(ProxiableDisposableSingletonSupplierImpl.class, Singleton.class)
//                            .to(ProxiableHolderSingletonClass.class)
//                            .in(RequestScoped.class);
//                });

        RequestScope request = injectionManager.getInstance(RequestScope.class);
        AtomicReference<Supplier<ProxiableHolderSingletonClass>> atomicSupplier = new AtomicReference<>();
        request.runInScope(() -> {
            // Save Singleton Supplier for later check that the instance was disposed.
            Supplier<ProxiableHolderSingletonClass> supplier = injectionManager.getInstance(
                    new GenericType<DisposableSupplier<ProxiableHolderSingletonClass>>() {}.getType());
            atomicSupplier.set(supplier);

            // All instances should be the same because they are request scoped.
            ProxiableHolderSingletonClass instance1 = injectionManager.getInstance(ProxiableHolderSingletonClass.class);
            assertEquals("1", instance1.getValue());
            ProxiableHolderSingletonClass instance2 = injectionManager.getInstance(ProxiableHolderSingletonClass.class);
            assertEquals("1", instance2.getValue());
        });

        Supplier<ProxiableHolderSingletonClass> cleanedSupplier = atomicSupplier.get();
        // Next should be 1-1
        assertEquals("1", cleanedSupplier.get().getValue());
    }

    /**
     * Tests that object created in request scope is disposing at the time of ending the scope.
     */
    @Test
    public void testDisposePerLookupSupplierRequestScopedInstance() {
//        BindingTestHelper.bind(injectionManager, binder -> {
//                    binder.bindFactory(ProxiableDisposableSupplierImpl.class)
//                            .to(ProxiableHolderClass.class)
//                            .in(RequestScoped.class);
//                });

        RequestScope request = injectionManager.getInstance(RequestScope.class);
        AtomicReference<Supplier<ProxiableHolderClass>> atomicSupplier = new AtomicReference<>();
        request.runInScope(() -> {
            // Save Singleton Supplier for later check that the instance was disposed.
            Supplier<ProxiableHolderClass> supplier = injectionManager.getInstance(PROXIABLE_DISPOSABLE_SUPPLIER_CLASS_TYPE);
            atomicSupplier.set(supplier);

            // All instances should be the same because they are request scoped.
            ProxiableHolderClass instance1 = injectionManager.getInstance(ProxiableHolderClass.class);
            assertEquals("1", instance1.getValue());
            ProxiableHolderClass instance2 = injectionManager.getInstance(ProxiableHolderClass.class);
            assertEquals("1", instance2.getValue());
        });

        Supplier<ProxiableHolderClass> cleanedSupplier = atomicSupplier.get();
        // Next should be 1
        assertEquals("1", cleanedSupplier.get().getValue());
    }

    /**
     * Tests that inherited request scoped is also cleaned by disposing the objects.
     */
    @Test
    public void testDisposeSingletonSupplierMultiRequestScoped() {
//        BindingTestHelper.bind(injectionManager, binder -> {
//                    binder.bindFactory(ProxiableDisposableSupplierImpl.class)
//                            .to(ProxiableHolderClass.class)
//                            .in(RequestScoped.class);
//                });

        RequestScope request = injectionManager.getInstance(RequestScope.class);
        AtomicReference<Supplier<ProxiableHolderClass>> firstSupplier = new AtomicReference<>();
        AtomicReference<Supplier<ProxiableHolderClass>> secondSupplier = new AtomicReference<>();

        request.runInScope(() -> {
            Supplier<ProxiableHolderClass> supplier1 = injectionManager.getInstance(PROXIABLE_DISPOSABLE_SUPPLIER_CLASS_TYPE);
            firstSupplier.set(supplier1);

            ProxiableHolderClass instance1 = injectionManager.getInstance(ProxiableHolderClass.class);
            assertEquals("1", instance1.getValue());

            request.runInScope(() -> {
                // Save Singleton Supplier for later check that the instance was disposed.
                Supplier<ProxiableHolderClass> supplier2 = injectionManager.getInstance(PROXIABLE_DISPOSABLE_SUPPLIER_CLASS_TYPE);
                secondSupplier.set(supplier2);

                ProxiableHolderClass instance2 = injectionManager.getInstance(ProxiableHolderClass.class);
                // 1-2 because the same static class is used in inherited runInScope
                assertEquals("1", instance2.getValue());
            });
        });

        Supplier<ProxiableHolderClass> cleanedSupplier1 = firstSupplier.get();
        Supplier<ProxiableHolderClass> cleanedSupplier2 = secondSupplier.get();
        // Next should be 1-1
        assertEquals("1", cleanedSupplier1.get().getValue());
        // 1-2 because the same static class is used but the instance is cleaned.
        assertEquals("1", cleanedSupplier2.get().getValue());
    }

    /**
     * PerLookup fields are not disposed therefore they should never be used as a DisposedSupplier because the field stay in
     * {@link org.glassfish.jersey.inject.weld.bean.SupplierClassBean} forever.
     */
    @Test
    public void testDisposeComposedObjectWithPerLookupFields() {
//        BindingTestHelper.bind(injectionManager, binder -> {
//                    binder.bindFactory(DisposableSupplierForComposedImpl.class, Singleton.class)
//                            .to(StringForComposed.class);
//
//                    binder.bindAsContract(ComposedObject.class)
//                            .in(RequestScoped.class);
//                });

        RequestScope request = injectionManager.getInstance(RequestScope.class);
        AtomicReference<Supplier<StringForComposed>> atomicSupplier = new AtomicReference<>();
        request.runInScope(() -> {
            // Save Singleton Supplier for later check that the instance was disposed.
            Supplier<StringForComposed> supplier = injectionManager.getInstance(
                    new GenericType<DisposableSupplier<StringForComposed>>() {}.getType());
            atomicSupplier.set(supplier);

            // All instances should be the same because they are request scoped.
            ComposedObject instance = injectionManager.getInstance(ComposedObject.class);
            assertEquals("1", instance.getFirst().toString());
            assertEquals("2", instance.getSecond().toString());
            assertEquals("3", instance.getThird().toString());
        });

        Supplier<StringForComposed> cleanedSupplier = atomicSupplier.get();
        // Next should be 1 - all instances are disposed and decremented back
        assertEquals("1", cleanedSupplier.get().toString());
    }

    private void dispose(Object... objects) {
        for (Object object : objects) {
            if (DisposableSupplier.class.isInstance(object)) {
                ((DisposableSupplier) object).dispose(null);
            }
        }
    }

    @Vetoed
    static class ComposedObject {

        @Inject
        StringForComposed first;

        @Inject
        StringForComposed second;

        @Inject
        StringForComposed third;

        public StringForComposed getFirst() {
            return first;
        }

        public StringForComposed getSecond() {
            return second;
        }

        public StringForComposed getThird() {
            return third;
        }
    }

    @Vetoed
    static class DisposableSupplierForComposedImpl implements DisposableSupplier<StringForComposed> {
        private final AtomicInteger counter = new AtomicInteger();

        @Override
        public StringForComposed get() {
            // Create a new string - don't share the instances in the string pool.
            return new StringForComposed(counter.incrementAndGet() + "");
        }

        @Override
        public void dispose(final StringForComposed instance) {
            counter.decrementAndGet();
        }
    }

    @Vetoed
    static class DisposableSupplierImpl implements DisposableSupplier<String> {
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

    @Vetoed
    static class ProxiableDisposableSupplierImpl implements DisposableSupplier<AbstractProxiableHolder> {
        private final AtomicInteger counter = new AtomicInteger();

        @Override
        public AbstractProxiableHolder get() {
            // Create a new string - don't share the instances in the string pool.
            return new ProxiableHolderClass(counter.incrementAndGet() + "");
        }

        @Override
        public void dispose(AbstractProxiableHolder instance) {
            counter.decrementAndGet();
        }
    }

    @Vetoed
    static class ProxiableDisposableSingletonSupplierImpl implements DisposableSupplier<AbstractProxiableHolder> {
        private final AtomicInteger counter = new AtomicInteger();

        @Override
        public AbstractProxiableHolder get() {
            // Create a new string - don't share the instances in the string pool.
            return new ProxiableHolderSingletonClass(counter.incrementAndGet() + "");
        }

        @Override
        public void dispose(AbstractProxiableHolder instance) {
            counter.decrementAndGet();
        }
    }

    @Vetoed
    static class ProxiableHolderSingletonClass extends AbstractProxiableHolder {
        public ProxiableHolderSingletonClass(String value) {
            super(value);
        }
    }

    @Vetoed
    static class ProxiableHolderClass extends AbstractProxiableHolder {
        public ProxiableHolderClass(String value) {
            super(value);
        }
    }

    @Vetoed
    abstract static class AbstractProxiableHolder {
        private String value;

        public AbstractProxiableHolder(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }


    @Vetoed
    static final class GreetingsInstance extends ExtendableString {
        public GreetingsInstance(CharSequence inner) {
            super(inner);
        }
    }

    @Vetoed
    static final class GreetingsClass extends ExtendableString {
        public GreetingsClass(CharSequence inner) {
            super(inner);
        }
    }

    @Vetoed
    static final class StringForComposed extends ExtendableString {
        public StringForComposed(CharSequence inner) {
            super(inner);
        }
    }

    @Vetoed
    static final class StringForSupplierSingletonClass extends ExtendableString {
        public StringForSupplierSingletonClass(CharSequence inner) {
            super(inner);
        }
    }

    @Vetoed
    static final class StringForSupplierClass extends ExtendableString {
        public StringForSupplierClass(CharSequence inner) {
            super(inner);
        }
    }

    @Vetoed
    static final class StringForSupplierInstance extends ExtendableString {
        public StringForSupplierInstance(CharSequence inner) {
            super(inner);
        }
    }

    static class ExtendableString implements CharSequence, Comparable<ExtendableString> {

        private final CharSequence inner;

        protected ExtendableString(CharSequence inner) {
            this.inner = inner;
        }

        @Override
        public int length() {
            return inner.length();
        }

        @Override
        public char charAt(int index) {
            return inner.charAt(index);
        }

        @Override
        public CharSequence subSequence(int start, int end) {
            return inner.subSequence(start, end);
        }

        @Override
        public String toString() {
            return inner.toString();
        }

        @Override
        public int compareTo(ExtendableString o) {
            if (this == o) return 0;
            if (o == null) return -1;
            return Objects.compare(inner.toString(), o.toString(), String.CASE_INSENSITIVE_ORDER);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null) return false;
            return Objects.equals(inner.toString(), inner.toString());
        }

        @Override
        public int hashCode() {
            return Objects.hash(inner);
        }
    }
}
