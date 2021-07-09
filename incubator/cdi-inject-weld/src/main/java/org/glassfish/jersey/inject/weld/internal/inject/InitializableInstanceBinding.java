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
import org.glassfish.jersey.internal.inject.Bindings;
import org.glassfish.jersey.internal.inject.InstanceBinding;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Injection binding description of a bean bound directly as a specific instance to be created in a pre-initialization phase
 * and initialized in runtime.
 *
 * @param <T> Type of the service described by this injection binding.
 */
public class InitializableInstanceBinding<T> extends MatchableBinding<T, InitializableInstanceBinding<T>> implements Cloneable {

    protected T service;
    private AtomicBoolean isInit = new AtomicBoolean(false);
    private Class<T> implementationType;

    /**
     * Creates a service as an instance.
     *
     * @param service service's instance.
     */
    protected InitializableInstanceBinding(T service) {
        this(service, null);
    }

    /**
     * Creates a service as an instance.
     *
     * @param service      service's instance.
     * @param contractType service's contractType.
     */
    private InitializableInstanceBinding(T service, Type contractType) {
        this.service = service;
        this.implementationType = service == null ? null : (Class<T>) service.getClass();
        if (contractType != null) {
            this.to(contractType);
        }
    }

    /**
     * Gets service' class.
     *
     * @return service's class.
     */
    public T getService() {
        if (!isInit.get()) {
            String types = Arrays.toString(getContracts().toArray());
            throw new IllegalStateException("Not initialized " + service + "(" + types + ")");
        }
        return service;
    }

    /**
     * Gets service's type.
     *
     * @return service's type.
     */
    public Class<T> getImplementationType() {
        return implementationType;
    }

    public void init(T service) {
        if (!isInit.getAndSet(true)) {
            this.service = service;
            implementationType = (Class<T>) service.getClass();
        } else if (this.service != service) {
            throw new IllegalStateException("Multiple initialized for " + service.getClass());
        }
    }

    public boolean isInit() {
        return isInit.get();
    }

    public Matching<InitializableInstanceBinding<T>> matches(Binding other) {
        return super.matches(other);
    }

    @Override
    public InitializableInstanceBinding<T> clone() {
        throw new RuntimeException(new CloneNotSupportedException());
    }

    public static <T> InitializableInstanceBinding<T> from(InstanceBinding<T> instanceBinding) {
        return new InitializableWrappingInstanceBinding(instanceBinding);
    }

    @Override
    protected MatchLevel bestMatchLevel() {
        return MatchLevel.IMPLEMENTATION;
    }

    private static class InitializableWrappingInstanceBinding<T> extends InitializableInstanceBinding<T> {
        private final InstanceBinding<T> wrapped;
        public InitializableWrappingInstanceBinding(InstanceBinding<T> binding) {
            super(binding.getService());
            wrapped = binding;
        }

        private InitializableWrappingInstanceBinding(InitializableWrappingInstanceBinding<T> binding) {
            super(binding.service);
            wrapped = binding.wrapped;
        }

        @Override
        public Class<T> getImplementationType() {
            return super.getImplementationType();
        }

        @Override
        public T getService() {
            return super.getService();
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
        public String toString() {
            return "InitializableWrappingInstanceBinding(" + wrapped.getService().getClass() + ")";
        }

        @Override
        public InitializableWrappingInstanceBinding clone() {
            return new InitializableWrappingInstanceBinding(this);
        }
    }
}