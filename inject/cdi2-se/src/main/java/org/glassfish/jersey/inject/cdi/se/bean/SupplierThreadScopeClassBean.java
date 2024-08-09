/*
 * Copyright (c) 2024 Oracle and/or its affiliates. All rights reserved.
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
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;

import org.glassfish.jersey.internal.inject.SupplierClassBinding;
import org.glassfish.jersey.internal.inject.SupplierInstanceBinding;
import org.glassfish.jersey.internal.util.collection.LazyValue;
import org.glassfish.jersey.internal.util.collection.Value;
import org.glassfish.jersey.internal.util.collection.Values;
import org.jboss.weld.bean.proxy.BeanInstance;
import org.jboss.weld.bean.proxy.ProxyFactory;
import org.jboss.weld.manager.BeanManagerImpl;


/**
 * Creates an implementation of {@link jakarta.enterprise.inject.spi.Bean} interface using Jersey's {@link SupplierInstanceBinding}.
 * Binding provides the information about the bean also called {@link jakarta.enterprise.inject.spi.BeanAttributes} information.
 * The {@code Bean} does not use {@link org.glassfish.jersey.inject.cdi.se.injector.JerseyInjectionTarget} because serves already
 * created proxy, therefore the create operation just return provided instance without any other contextual operation
 * (produce, inject, destroy).
 * <p>
 * This bean is special and is used only for service registered as a {@link org.glassfish.jersey.internal.inject.PerThread} and
 * works through the proxy which serves the correct instance per the given thread.
 * <p>
 * Register example:
 * <pre>
 * AbstractBinder {
 *     &#64;Override
 *     protected void configure() {
 *         bindFactory(MyFactoryInjectionSupplier.class)
 *              .to(MyBean.class)
 *              .in(PerThread.class);
 *     }
 * }
 * </pre>
 * Inject example:
 * <pre>
 * &#64;Path("/")
 * public class MyResource {
 *   &#64;Inject
 *   private MyBean myBean&#59;
 * }
 * </pre>
 */
class SupplierThreadScopeClassBean extends JerseyBean<Object> {
    private final LazyValue<ThreadScopeBeanInstance<Object>> beanInstance;
    private final SupplierClassBinding binding;
    private final LazyValue<Object> proxy;
    private final AtomicReference<CreationalContext> creationalContextAtomicReference = new AtomicReference<>();

    SupplierThreadScopeClassBean(SupplierClassBinding binding, SupplierClassBean supplierClassBean, BeanManagerImpl beanManager) {
        super(binding);
        this.binding = binding;
        this.beanInstance = Values.lazy((Value<ThreadScopeBeanInstance<Object>>) () -> {
            Supplier supplierInstance = supplierClassBean.create(creationalContextAtomicReference.get());
            ThreadScopeBeanInstance scopeBeanInstance =
                    new ThreadScopeBeanInstance(supplierInstance, this, beanManager.getContextId());
            return scopeBeanInstance;
        });
        this.proxy = Values.lazy((Value<Object>) () -> createClientProxy(beanInstance.get(), beanManager.getContextId()));
    }

    @Override
    public Class<? extends Annotation> getScope() {
        return Dependent.class;
    }

    @Override
    public Object create(CreationalContext<Object> ctx) {
        creationalContextAtomicReference.set(ctx);
        return proxy.get();
    }

    @Override
    public void destroy(Object instance, CreationalContext<Object> creationalContext) {
        if (beanInstance.isInitialized()) {
            this.beanInstance.get().dispose();
        }
    }

    @Override
    public Class<?> getBeanClass() {
        return (Class<?>) this.binding.getContracts().iterator().next();
    }

    private <T> T createClientProxy(BeanInstance beanInstance, String contextId) {
        ProxyFactory<T> factory = new ProxyFactory<>(contextId, getBeanClass(), getTypes(), this);
        return factory.create(beanInstance);
    }
}
