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

package org.glassfish.jersey.inject.weld.internal.bean;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.ws.rs.RuntimeType;

import org.glassfish.jersey.inject.weld.internal.inject.InitializableSupplierInstanceBinding;
import org.glassfish.jersey.inject.weld.internal.injector.JerseyClientCreationalContext;
import org.glassfish.jersey.inject.weld.internal.injector.JerseyInjectionTarget;
import org.glassfish.jersey.inject.weld.internal.managed.CdiClientInjectionManager;
import org.glassfish.jersey.inject.weld.internal.managed.ContextSafe;
import org.glassfish.jersey.inject.weld.internal.type.ParameterizedTypeImpl;
import org.glassfish.jersey.internal.inject.DisposableSupplier;
import org.glassfish.jersey.internal.inject.SupplierInstanceBinding;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Creates an implementation of {@link jakarta.enterprise.inject.spi.Bean} interface using Jersey's {@link SupplierInstanceBinding}.
 * Binding provides the information about the bean also called {@link jakarta.enterprise.inject.spi.BeanAttributes} information.
 * The {@code Bean} does not use {@link JerseyInjectionTarget} because serves already
 * created supplier instance, therefore the create operation just return provided instance without any other contextual operation
 * (produce, inject, destroy). Client has to manage the instance alone.
 * <p>
 * Inject example:
 * <pre>
 * AbstractBinder {
 *     &#64;Override
 *     protected void configure() {
 *         bindFactory(new MyBeanFactory())
 *              .to(MyBean.class)
 *              .in(Singleton.class)&#59;
 *     }
 * }
 * </pre>
 * Register example:
 * <pre>
 *  &#64;Path("/")
 *  public class MyResource {
 *    &#64;Inject
 *    private Supplier&lt;MyBean&gt; myBean&#59;
 *  }
 * </pre>
 *
 * @author Petr Bouda
 */
class InitializableSupplierInstanceBean<T> extends JerseyBean<Supplier<T>> {

    private final Set<Type> contracts = new HashSet<>();
    private final Supplier<T> supplier;

    /**
     * Creates a new Jersey-specific {@link jakarta.enterprise.inject.spi.Bean} instance.
     *
     * @param binding {@link jakarta.enterprise.inject.spi.BeanAttributes} part of the bean.
     */
    InitializableSupplierInstanceBean(RuntimeType runtimeType, InitializableSupplierInstanceBinding<T> binding) {
        super(runtimeType, binding);
        this.supplier = binding.getSupplier();

        for (Type contract: binding.getContracts()) {
            this.contracts.add(new ParameterizedTypeImpl(Supplier.class, contract));
            if (DisposableSupplier.class.isAssignableFrom(binding.getSupplier().getClass())) {
                this.contracts.add(new ParameterizedTypeImpl(DisposableSupplier.class, contract));
            }
        }
    }

    @Override
    public Set<Type> getTypes() {
        return contracts;
    }

    @Override
    public Set<Annotation> getQualifiers() {
        return DEFAULT_QUALIFIERS;
    }

    @Override
    public Supplier<T> create(CreationalContext<Supplier<T>> context) {
        if (JerseyClientCreationalContext.class.isInstance(context)) {
            final InitializableSupplierInstanceBinding<T> binding = (InitializableSupplierInstanceBinding<T>) getBinding();
            final JerseyClientCreationalContext jerseyContext = (JerseyClientCreationalContext) context;
            return jerseyContext.getInjectionManager().getInjectionManagerBinding(binding).getSupplier();
        } else {
            CdiClientInjectionManager locked = ContextSafe.get();
            if (locked != null) {
                return locked
                        .getInjectionManagerBinding((InitializableSupplierInstanceBinding<T>) getBinding())
                        .getSupplier();
            }
            return supplier;
        }
    }

    @Override
    public Class<?> getBeanClass() {
        final InitializableSupplierInstanceBinding binding = (InitializableSupplierInstanceBinding) getBinding();
        return binding.isInit() ? binding.getOriginalSupplier().getClass() : Object.class;
    }

    @Override
    public Class<? extends Annotation> getScope() {
        return getBinding().getScope() == null ? Dependent.class : transformScope(getBinding().getScope());
    }

    @Override
    public String toString() {
        return getBinding().toString();
    }
}
