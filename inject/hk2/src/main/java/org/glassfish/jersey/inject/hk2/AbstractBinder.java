/*
 * Copyright (c) 2024, 2025 Oracle and/or its affiliates. All rights reserved.
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

import jakarta.inject.Provider;
import jakarta.ws.rs.core.GenericType;
import org.glassfish.jersey.innate.inject.BlindBinder;
import org.glassfish.jersey.internal.inject.Binder;
import org.glassfish.jersey.internal.inject.Binding;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.internal.inject.InjectionResolver;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.function.Supplier;

/**
 * Implementation of {@link Binder} interface dedicated to keep some level of code compatibility between previous HK2
 * implementation and new DI SPI.
 * <p>
 * Currently, there are supported only bind method and more complicated method where HK2 interfaces are required were omitted.
 */
public abstract class AbstractBinder implements Binder {
    private class XBinder extends BlindBinder {
        protected <T> Provider<T> createManagedInstanceProvider(Class<T> clazz) {
            return super.createManagedInstanceProvider(clazz);
        }
        protected void configure() {
            AbstractBinder.this.configure();
        }
    }
    private XBinder binder = new XBinder();

    /**
     * Implement to provide binding definitions using the exposed binding methods.
     */
    protected abstract void configure();

    /**
     * Creates a new instance of {@link Provider} which is able to retrieve a managed instance registered in
     * {@link InjectionManager}. If {@code InjectionManager} is {@code null} at the time of calling {@link Provider#get()} then
     * {@link IllegalStateException} is thrown.
     *
     * @param clazz class of managed instance.
     * @param <T>   type of the managed instance returned using provider.
     * @return provider with instance of managed instance.
     */
    protected final <T> Provider<T> createManagedInstanceProvider(Class<T> clazz) {
        return binder.createManagedInstanceProvider(clazz);
    }

    /**
     * Start building a new class-based service binding.
     * <p>
     * Does NOT bind the service type itself as a contract type.
     *
     * @param <T>         service type.
     * @param serviceType service class.
     * @return initialized binding builder.
     */
    public <T> Binding<T, ?> bind(Class<T> serviceType) {
        return binder.bind(serviceType);
    }

    /**
     * Binds the provided binding and return the same instance.
     *
     * @param binding binding.
     * @return the same provided binding.
     */
    public Binding bind(Binding binding) {
        return binder.bind(binding);
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
    public <T> Binding<T, ?> bindAsContract(Class<T> serviceType) {
        return binder.bindAsContract(serviceType);
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
    public <T> Binding<T, ?> bindAsContract(GenericType<T> serviceType) {
        return binder.bindAsContract(serviceType);
    }

    /**
     * Start building a new generic type-based service binding.
     * <p>
     * Binds the generic service type itself as a contract type.
     *
     * @param serviceType generic service type information.
     * @return initialized binding builder.
     */
    public Binding<Object, ?> bindAsContract(Type serviceType) {
        return binder.bindAsContract(serviceType);
    }

    /**
     * Start building a new instance-based service binding. The binding is naturally
     * considered to be a {@link jakarta.inject.Singleton singleton-scoped}.
     * <p>
     * Does NOT bind the service type itself as a contract type.
     *
     * @param <T>     service type.
     * @param service service instance.
     * @return initialized binding builder.
     */
    public <T> Binding<T, ?> bind(T service) {
        return binder.bind(service);
    }

    /**
     * Start building a new supplier class-based service binding.
     *
     * @param <T>           service type.
     * @param supplierType  service supplier class.
     * @param supplierScope factory scope.
     * @return initialized binding builder.
     */
    public <T> Binding<Supplier<T>, ?> bindFactory(
            Class<? extends Supplier<T>> supplierType, Class<? extends Annotation> supplierScope) {
        return binder.bindFactory(supplierType, supplierScope);
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
    public <T> Binding<Supplier<T>, ?> bindFactory(Class<? extends Supplier<T>> supplierType) {
        return binder.bindFactory(supplierType);
    }

    /**
     * Start building a new supplier instance-based service binding.
     *
     * @param <T>     service type.
     * @param factory service instance.
     * @return initialized binding builder.
     */
    public <T> Binding<Supplier<T>, ?> bindFactory(Supplier<T> factory) {
        return binder.bindFactory(factory);
    }

    /**
     * Start building a new injection resolver binding. The injection resolver is naturally
     * considered to be a {@link jakarta.inject.Singleton singleton-scoped}.
     * <p>
     * There is no need to provide any additional information. Other method on {@link Binding}
     * will be ignored.
     *
     * @param <T>      type of the injection resolver.
     * @param resolver injection resolver instance.
     * @return initialized binding builder.
     */
    public <T extends InjectionResolver> Binding<T, ?> bind(T resolver) {
        return binder.bind(resolver);
    }

    /**
     * Adds all binding definitions from the binders to the binding configuration.
     *
     * @param binders binders whose binding definitions should be configured.
     */
    public final void install(Binder... binders) {
        binder.install(binders);
    }

    @Override
    public Collection<Binding> getBindings() {
        return binder.getBindings();
    }

}
