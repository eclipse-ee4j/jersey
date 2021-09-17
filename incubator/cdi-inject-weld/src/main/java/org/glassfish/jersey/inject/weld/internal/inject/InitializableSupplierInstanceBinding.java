/*
 * Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved.
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

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

/**
 * Supplier instance binding to be created in the pre-initialization phase and initialized in runtime.
 * @param <T> Type of the supplied service described by this injection binding.
 */
public class InitializableSupplierInstanceBinding<T>
        extends MatchableBinding<Supplier<T>, InitializableSupplierInstanceBinding<T>>
        implements Cloneable {

    private final InitializableSupplier<T> supplier;

    /**
     * Creates a supplier as an instance.
     *
     * @param supplier service's instance.
     */
    public InitializableSupplierInstanceBinding(Supplier<T> supplier) {
        this.supplier = DisposableSupplier.class.isInstance(supplier)
                ? new InitializableDisposableSupplier<T>((DisposableSupplier<T>) supplier)
                : new InitializableSupplier<T>(supplier);
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

    public static <T> InitializableSupplierInstanceBinding<T> from(SupplierInstanceBinding<T> binding) {
        return new InitializableSupplierWrappingInstanceBinding(binding);
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

    private static class InitializableDisposableSupplier<T> extends InitializableSupplier<T> implements DisposableSupplier<T> {
        private InitializableDisposableSupplier(DisposableSupplier<T> originalSupplier) {
            super(originalSupplier);
        }

        @Override
        public void dispose(T instance) {
            ((DisposableSupplier) supplier).dispose(instance);
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
        public InitializableSupplierWrappingInstanceBinding(SupplierInstanceBinding binding) {
            super(binding.getSupplier());
            wrapped = binding;
        }

        private InitializableSupplierWrappingInstanceBinding(InitializableSupplierWrappingInstanceBinding binding) {
            super(binding.getOriginalSupplier());
            wrapped = binding.wrapped;
        }

        @Override
        public Class getImplementationType() {
            return super.getImplementationType();
        }

        @Override
        public Supplier getSupplier() {
            return super.getSupplier();
        }

        @Override
        public Class<? extends Annotation> getScope() {
            return wrapped.getScope();
        }

        @Override
        public Set<Type> getContracts() {
            return wrapped.getContracts();
        }

        @Override
        public Integer getRank() {
            return wrapped.getRank();
        }

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
        public String getName() {
            return wrapped.getName();
        }

        @Override
        public InitializableSupplierWrappingInstanceBinding clone() {
            return new InitializableSupplierWrappingInstanceBinding(this);
        }

        @Override
        public String toString() {
            return "InitializableSupplierWrappingInstanceBinding(" +  wrapped.getSupplier() + ")";
        }
    }

}
