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

package org.glassfish.jersey.inject.hk2;

import jakarta.ws.rs.core.GenericType;
import org.glassfish.jersey.internal.inject.Binder;
import org.glassfish.jersey.internal.inject.Binding;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.internal.inject.InjectionResolver;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.function.Supplier;

/**
 * Utility class to create a new injection binding descriptions for arbitrary Java beans.
 *
 * @author Petr Bouda
 */
public final class Bindings {

    private Bindings() {
        throw new AssertionError("Utility class instantiation forbidden.");
    }

    public static Collection<Binding> getBindings(InjectionManager injectionManager, Binder binder) {
        return org.glassfish.jersey.innate.inject.Bindings.getBindings(injectionManager, binder);
    }

    /**
     * Start building a new class-based service binding.
     * <p>
     * Does NOT service the service type itself as a contract type.
     *
     * @param <T>         service type.
     * @param serviceType service class.
     * @return initialized binding builder.
     */
    public static <T> Binding service(Class<T> serviceType) {
        return org.glassfish.jersey.innate.inject.Bindings.service(serviceType);
    }

    /**
     * Start building a new class-based service binding.
     * <p>
     * Binds the service type itself as a contract type.
     *
     * @param <T>         service type.
     * @param serviceType service class.
     * @return initialized binding builder.
     */
    public static <T> Binding serviceAsContract(Class<T> serviceType) {
        return org.glassfish.jersey.innate.inject.Bindings.serviceAsContract(serviceType);
    }

    /**
     * Start building a new generic type-based service binding.
     * <p>
     * Binds the generic service type itself as a contract type.
     *
     * @param <T>         service type.
     * @param serviceType generic service type information.
     * @return initialized binding builder.
     */
    @SuppressWarnings("unchecked")
    public static <T> Binding service(GenericType<T> serviceType) {
        return org.glassfish.jersey.innate.inject.Bindings.service(serviceType);
    }

    /**
     * Start building a new generic type-based service binding.
     * <p>
     * Binds the generic service type itself as a contract type.
     *
     * @param <T>         service type.
     * @param serviceType generic service type information.
     * @return initialized binding builder.
     */
    @SuppressWarnings("unchecked")
    public static <T> Binding serviceAsContract(GenericType<T> serviceType) {
        return org.glassfish.jersey.innate.inject.Bindings.serviceAsContract(serviceType);
    }

    /**
     * Start building a new generic type-based service binding.
     * <p>
     * Binds the generic service type itself as a contract type.
     *
     * @param <T>         service type.
     * @param serviceType generic service type information.
     * @return initialized binding builder.
     */
    @SuppressWarnings("unchecked")
    public static <T> Binding serviceAsContract(Type serviceType) {
        return org.glassfish.jersey.innate.inject.Bindings.serviceAsContract(serviceType);
    }

    /**
     * Start building a new instance-based service binding. The binding is naturally
     * considered to be a {@link jakarta.inject.Singleton singleton-scoped}.
     * <p>
     * Does NOT service the service type itself as a contract type.
     *
     * @param <T>     service type.
     * @param service service instance.
     * @return initialized binding builder.
     */
    public static <T> Binding service(T service) {
        return org.glassfish.jersey.innate.inject.Bindings.service(service);
    }

    /**
     * Start building a new instance-based service binding. The binding is naturally
     * considered to be a {@link jakarta.inject.Singleton singleton-scoped}.
     * <p>
     * Binds the generic service type itself as a contract type.
     *
     * @param <T>     service type.
     * @param service service instance.
     * @return initialized binding builder.
     */
    public static <T> Binding serviceAsContract(T service) {
        return org.glassfish.jersey.innate.inject.Bindings.serviceAsContract(service);
    }

    /**
     * Start building a new supplier class-based service binding.
     *
     * @param <T>           service type.
     * @param supplierType  service supplier class.
     * @param supplierScope factory scope.
     * @return initialized binding builder.
     */
    public static <T> Binding supplier(
            Class<? extends Supplier<T>> supplierType, Class<? extends Annotation> supplierScope) {
        return org.glassfish.jersey.innate.inject.Bindings.supplier(supplierType, supplierScope);
    }

    /**
     * Start building a new supplier class-based service binding.
     * <p>
     * The supplier itself is bound in a per-lookup scope.
     *
     * @param <T>          service type.
     * @param supplierType service supplier class.
     * @return initialized binding builder.
     */
    public static <T> Binding supplier(Class<? extends Supplier<T>> supplierType) {
        return org.glassfish.jersey.innate.inject.Bindings.supplier(supplierType);
    }

    /**
     * Start building a new supplier instance-based service binding.
     *
     * @param <T>      service type.
     * @param supplier service instance.
     * @return initialized binding builder.
     */
    public static <T> Binding supplier(Supplier<T> supplier) {
        return org.glassfish.jersey.innate.inject.Bindings.supplier(supplier);
    }

    /**
     * Start building a new injection resolver binding. The injection resolver is naturally
     * considered to be a {@link jakarta.inject.Singleton singleton-scoped}.
     * <p>
     * There is no need to provide any additional information. Other method on {@link Binding}
     * will be ignored.
     *
     * @param <T>        type of the injection resolver.
     * @param resolver   injection resolver instance.
     * @return initialized binding builder.
     */
    public static <T extends InjectionResolver> Binding injectionResolver(T resolver) {
        return org.glassfish.jersey.innate.inject.Bindings.injectionResolver(resolver);
    }
}
