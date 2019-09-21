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

package org.glassfish.jersey.inject.cdi.se.bean;

import java.lang.annotation.Annotation;
import java.util.function.Supplier;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;

import org.glassfish.jersey.internal.inject.DisposableSupplier;
import org.glassfish.jersey.internal.inject.SupplierInstanceBinding;

/**
 * Creates an implementation of {@link javax.enterprise.inject.spi.Bean} interface using Jersey's {@link SupplierInstanceBinding}.
 * Binding provides the information about the bean also called {@link javax.enterprise.inject.spi.BeanAttributes} information.
 * The {@code Bean} does not use {@link org.glassfish.jersey.inject.cdi.se.injector.JerseyInjectionTarget} because serves already
 * created instances, therefore the create operation just return provided instance without any other contextual operation
 * (produce, inject, destroy). Client has to manage the instance alone.
 * <p>
 * This implementation works as bridge between {@link Supplier} and its provided value. This solves the case when the concrete
 * type of supplier value is fetched from {@link org.glassfish.jersey.internal.inject.InjectionManager} then this
 * {@link javax.enterprise.inject.spi.Bean} implementation just invokes {@link Supplier#get} method on underlying/registered
 * supplier.
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
 *    private MyBean myBean&#59;
 *  }
 * </pre>
 *
 * @author Petr Bouda
 */
class SupplierInstanceBeanBridge<T> extends JerseyBean<Object> {

    private final Supplier<T> supplier;
    private final Class<? extends Annotation> scope;

    /**
     * Creates a new Jersey-specific {@link javax.enterprise.inject.spi.Bean} instance.
     *
     * @param binding {@link javax.enterprise.inject.spi.BeanAttributes} part of the bean.
     */
    @SuppressWarnings("unchecked")
    SupplierInstanceBeanBridge(SupplierInstanceBinding binding) {
        super(binding);

        SupplierInstanceBinding<T> casted = (SupplierInstanceBinding<T>) binding;
        this.supplier = casted.getSupplier();
        this.scope = casted.getScope();
    }

    @Override
    public Object create(CreationalContext creationalContext) {
        return supplier.get();
    }

    @Override
    public void destroy(Object instance, CreationalContext<Object> context) {
        if (DisposableSupplier.class.isAssignableFrom(supplier.getClass())) {
            ((DisposableSupplier<Object>) supplier).dispose(instance);
        }
    }

    /**
     * {@link SupplierInstanceBeanBridge} needs have the same scope as a keeping value.
     *
     * @return scope of the supplier bean.
     */
    @Override
    public Class<? extends Annotation> getScope() {
        return scope == null ? Dependent.class : transformScope(scope);
    }
}
