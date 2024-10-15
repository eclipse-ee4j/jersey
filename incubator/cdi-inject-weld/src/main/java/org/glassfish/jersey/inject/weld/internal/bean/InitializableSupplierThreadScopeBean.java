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
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.PassivationCapable;
import jakarta.ws.rs.RuntimeType;

import org.glassfish.jersey.inject.weld.internal.inject.InitializableSupplierInstanceBinding;
import org.glassfish.jersey.inject.weld.internal.injector.JerseyInjectionTarget;
import org.glassfish.jersey.internal.inject.SupplierInstanceBinding;
import org.jboss.weld.bean.StringBeanIdentifier;
import org.jboss.weld.bean.proxy.BeanInstance;
import org.jboss.weld.bean.proxy.ContextBeanInstance;
import org.jboss.weld.bean.proxy.ProxyFactory;
import org.jboss.weld.manager.BeanManagerImpl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.WeakHashMap;
import java.util.function.Supplier;

/**
 * Creates an implementation of {@link Bean} interface using Jersey's {@link SupplierInstanceBinding}.
 * Binding provides the information about the bean also called {@link jakarta.enterprise.inject.spi.BeanAttributes} information.
 * The {@code Bean} does not use {@link JerseyInjectionTarget} because serves already
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
 *         bindFactory(new MyFactoryInjectionProvider())
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
public class InitializableSupplierThreadScopeBean extends JerseyBean<Object> {

    private final ThreadScopeBeanInstance<Object> beanInstance;
    private final InitializableSupplierInstanceBinding binding;
    private final Object proxy;

    /**
     * Creates a new Jersey-specific {@link Bean} instance.
     *
     * @param binding {@link jakarta.enterprise.inject.spi.BeanAttributes} part of the bean.
     */
    @SuppressWarnings("unchecked")
    InitializableSupplierThreadScopeBean(RuntimeType runtimeType,
                                         InitializableSupplierInstanceBinding binding,
                                         BeanManagerImpl manager) {
        super(runtimeType, binding);
        this.binding = binding;
        this.beanInstance = new ThreadScopeBeanInstance<>(binding.getSupplier(), this, manager.getContextId());
        this.proxy = createClientProxy(beanInstance, manager.getContextId());
    }

    @Override
    public Class<? extends Annotation> getScope() {
        return Dependent.class;
    }

    @Override
    public Object create(CreationalContext<Object> ctx) {
        return proxy;
    }

    @Override
    public void destroy(Object instance, CreationalContext<Object> creationalContext) {
        this.beanInstance.dispose();
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
