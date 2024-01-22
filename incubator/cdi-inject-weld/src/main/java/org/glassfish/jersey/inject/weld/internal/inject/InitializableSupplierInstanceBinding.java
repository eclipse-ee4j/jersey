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

package org.glassfish.jersey.inject.weld.internal.inject;

import org.glassfish.jersey.internal.inject.AliasBinding;
import org.glassfish.jersey.internal.inject.Binding;
import org.glassfish.jersey.internal.inject.DisposableSupplier;
import org.glassfish.jersey.internal.inject.SupplierInstanceBinding;

import jakarta.ws.rs.RuntimeType;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Supplier instance binding to be created in the pre-initialization phase and initialized in runtime.
 * @param <T> Type of the supplied service described by this injection binding.
 */
public class InitializableSupplierInstanceBinding<T>
        extends InitializableBinding<Supplier<T>, InitializableSupplierInstanceBinding<T>>
        implements Cloneable {

    private final InitializableSupplier<T> supplier;
    private final RuntimeType runtimeType;

    /**
     * Creates a supplier as an instance.
     *
     * @param supplier    service's instance.
     * @param runtimeType
     */
    public InitializableSupplierInstanceBinding(Supplier<T> supplier, RuntimeType runtimeType) {
        this.supplier = DisposableSupplier.class.isInstance(supplier)
                ? new InitializableDisposableSupplier<T>((DisposableSupplier<T>) supplier)
                : new InitializableSupplier<T>(supplier);
        this.runtimeType = runtimeType;
        if ("EmptyReferenceFactory".equals(supplier.getClass().getSimpleName())) {
            this.supplier.init(supplier);
            this.supplier.isReferencingFactory = true;
        }
        if ("InitializedReferenceFactory".equals(supplier.getClass().getSimpleName())) {
            this.supplier.init(supplier);
            this.supplier.isReferencingFactory = true;
        }
        T t = supplier.get();
    }

    public void init(Supplier<T> supplier) {
        this.supplier.init(supplier);
    }

    public boolean isInit() {
        return supplier.init.get();
    }

    @Override
    public RuntimeType getRuntimeType() {
        return runtimeType;
    }

    /**
     * Gets supplier's instance.
     *
     * @return supplier's instance.
     */
    public Supplier<T> getSupplier() {
        return supplier;
    }

    public Supplier<T> getOriginalSupplier() {
        if (supplier.originalSupplier == null) {
            throw new IllegalStateException("Supplier must not be null");
        }
        return supplier.originalSupplier;
    }

    public static <T> InitializableSupplierInstanceBinding<T> from(SupplierInstanceBinding<T> binding, RuntimeType runtimeType) {
        return new InitializableSupplierWrappingInstanceBinding(binding, runtimeType);
    }

    @Override
    public InitializableSupplierInstanceBinding clone() {
        throw new RuntimeException(new CloneNotSupportedException());
    }

    public Matching matches(SupplierInstanceBinding<T> other) {
        return matches(this.getOriginalSupplier().getClass(), other.getSupplier().getClass(), other);
    }

    public Matching matches(InitializableSupplierInstanceBinding<T> other) {
        return matches(this.getOriginalSupplier().getClass(), other.getSupplier().getClass(), other);
    }

    private Matching<InitializableSupplierInstanceBinding<T>> matches(
            Class<?> originalSupplierClass, Class<?> otherSupplierClass, Binding other) {
        if (getId() != 0) {
            return matchesById(other);
        }
        final boolean matchesService = originalSupplierClass.equals(otherSupplierClass);
        final Matching matching = matchesContracts(other);
        if (matching.matchLevel == MatchLevel.FULL_CONTRACT && matchesService) {
            matching.matchLevel = MatchLevel.SUPPLIER;
        }
        return matching;
    }

    @Override
    protected MatchLevel bestMatchLevel() {
        return MatchLevel.SUPPLIER;
    }

    @Override
    public Matching<MatchableBinding> matching(Binding other) {
        return visitor.matches((InitializableSupplierInstanceBinding) this, other);
    }

    private static class InitializableDisposableSupplier<T> extends InitializableSupplier<T> implements DisposableSupplier<T> {
        private InitializableDisposableSupplier(DisposableSupplier<T> originalSupplier) {
            super(originalSupplier);
        }

        @Override
        public void dispose(T instance) {
            if (isInit()) {
                ((DisposableSupplier) supplier).dispose(instance);
            }
        }
    }

    private static class InitializableSupplier<T> implements Supplier<T> {

        private AtomicBoolean init = new AtomicBoolean(false);
        protected Supplier<T> supplier;
        protected final Supplier<T> originalSupplier;
        private boolean isReferencingFactory = false;

        private InitializableSupplier(Supplier<T> originalSupplier) {
            this.originalSupplier = originalSupplier;
        }

        private void init(Supplier<T> supply) {
            if (!init.getAndSet(true)) {
                this.supplier = supply;
            } else if (!isReferencingFactory && supplier != supply) {
                throw new IllegalStateException("Multiple initialized for " + originalSupplier.getClass());
            }
        }

        @Override
        public T get() {
            if (!init.get()) {
                throw new IllegalStateException("Not initialized" + originalSupplier.getClass());
            }
            return supplier.get();
        }

        public boolean isInit() {
            return init.get();
        }
    }

    private static class InitializableSupplierWrappingInstanceBinding<T> extends InitializableSupplierInstanceBinding<T> {
        private final SupplierInstanceBinding wrapped;
        InitializableSupplierWrappingInstanceBinding(SupplierInstanceBinding binding, RuntimeType runtimeType) {
            super(binding.getSupplier(), runtimeType);
            wrapped = binding;
            postCreate();
        }

        private InitializableSupplierWrappingInstanceBinding(InitializableSupplierWrappingInstanceBinding binding,
                                                             RuntimeType runtimeType) {
            super(binding.getOriginalSupplier(), runtimeType);
            wrapped = binding.wrapped;
            postCreate();
        }

        private void postCreate() {
            wrapped.getContracts().forEach(c -> this.to((Type) c));
            if (wrapped.getRank() != null) {
                this.ranked(wrapped.getRank());
            }
            this.named(wrapped.getName());
            this.id(wrapped.getId());
            this.in(wrapped.getScope());
            this.forClient(wrapped.isForClient());
        }

//        @Override
//        public Class getImplementationType() {
//            return super.getImplementationType();
//        }
//
//        @Override
//        public Supplier getSupplier() {
//            return super.getSupplier();
//        }
//
//        @Override
//        public RuntimeType getRuntime() {
//            return super.getRuntime();
//        }

//        @Override
//        public Class<? extends Annotation> getScope() {
//            return wrapped.getScope();
//        }
//
//        @Override
//        public Set<Type> getContracts() {
//            return wrapped.getContracts();
//        }
//
//        @Override
//        public Integer getRank() {
//            return wrapped.getRank();
//        }

        @Override
        public Set<AliasBinding> getAliases() {
            return wrapped.getAliases();
        }

        @Override
        public Set<Annotation> getQualifiers() {
            return wrapped.getQualifiers();
        }

        @Override
        public String getAnalyzer() {
            return wrapped.getAnalyzer();
        }

        @Override
        public String toString() {
            return wrapped.toString();
        }


//        @Override
//        public String getName() {
//            return wrapped.getName();
//        }

//        @Override
//        public long getId() {
//            return wrapped.getId();
//        }
//
//        @Override
//        public boolean isForClient() {
//            return wrapped.isForClient();
//        }

        @Override
        public InitializableSupplierWrappingInstanceBinding clone() {
            return new InitializableSupplierWrappingInstanceBinding(this, getRuntimeType());
        }

//        @Override
//        public String toString() {
//            return "InitializableSupplierWrappingInstanceBinding(" +  wrapped.getSupplier() + ")";
//        }
    }

}
