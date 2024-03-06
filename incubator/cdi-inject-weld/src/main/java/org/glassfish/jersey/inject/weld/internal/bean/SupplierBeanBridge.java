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

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.ws.rs.RuntimeType;

import org.glassfish.jersey.inject.weld.internal.injector.JerseyInjectionTarget;
import org.glassfish.jersey.inject.weld.managed.CdiInjectionManagerFactory;
import org.glassfish.jersey.inject.weld.internal.type.ParameterizedTypeImpl;
import org.glassfish.jersey.innate.inject.SupplierClassBinding;
import org.glassfish.jersey.innate.inject.SupplierInstanceBinding;
import org.glassfish.jersey.internal.inject.DisposableSupplier;
import org.glassfish.jersey.internal.inject.InjectionManager;

/**
 * Creates an implementation of {@link jakarta.enterprise.inject.spi.Bean} interface using Jersey's {@link SupplierInstanceBinding}.
 * Binding provides the information about the bean also called {@link jakarta.enterprise.inject.spi.BeanAttributes} information.
 * The {@code Bean} does not use {@link JerseyInjectionTarget} because serves already
 * the instances created by underlying {@link Supplier} injected target on which the call is delegated.
 * <p>
 * This implementation works as bridge between {@link Supplier} and its provided value. This solves the case when the concrete
 * type of supplier value is fetched from {@link org.glassfish.jersey.internal.inject.InjectionManager} then this
 * {@link jakarta.enterprise.inject.spi.Bean} implementation just invokes {@link Supplier#get} method on underlying/registered
 * supplier.
 * <p>
 * Inject example:
 * <pre>
 * AbstractBinder {
 *     &#64;Override
 *     protected void configure() {
 *         bindFactory(MyBeanFactory.class)
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
class SupplierBeanBridge extends JerseyBean<Object> {

    private final BeanManager beanManager;
    private ParameterizedType type;
    private boolean disposable;
    private SupplierClassBinding binding;

    // This bridge can create multiple instances using the method 'provide' therefore must map created suppliers because of
    // 'dispose' invocation later on.
    // TODO: Key as a WeakReference - prevent objects in scope which never dispose the objects such as PerLookup.
    private final Map<Object, DisposableSupplier<Object>> disposableSuppliers = new IdentityHashMap<>();

    /**
     * Creates a new Jersey-specific {@link jakarta.enterprise.inject.spi.Bean} instance.
     *
     * @param binding {@link jakarta.enterprise.inject.spi.BeanAttributes} part of the bean.
     */
    @SuppressWarnings("unchecked")
    SupplierBeanBridge(RuntimeType runtimeType, SupplierClassBinding binding, BeanManager beanManager) {
        super(runtimeType, binding);

        // Register wrapper for factory functionality, wrapper automatically call service locator which is able to retrieve
        // the service in the proper context and scope. Bridge is registered for all contracts but is able to lookup from
        // service locator only using the first contract.
        Type contract = null;
        if (binding.getContracts().iterator().hasNext()) {
            contract = (Type) binding.getContracts().iterator().next();
        }

        this.binding = binding;
        this.beanManager = beanManager;
        this.disposable = DisposableSupplier.class.isAssignableFrom(binding.getSupplierClass());
        this.type = new ParameterizedTypeImpl(Supplier.class, contract);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object create(CreationalContext creationalContext) {
        if (type != null) {
            InjectionManager injectionManager = CdiInjectionManagerFactory.getInjectionManager(creationalContext);
            Supplier<?> supplier = injectionManager.getInstance(type);

            //Supplier<?> supplier = getSupplier(beanManager, type);
            Object instance = supplier.get();
            if (disposable) {
                disposableSuppliers.put(instance, (DisposableSupplier) supplier);
            }
            return instance;
        } else {
            return null;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void destroy(Object instance, CreationalContext context) {
        if (disposable) {
            DisposableSupplier disposableSupplier = disposableSuppliers.get(instance);
            disposableSupplier.dispose(instance);
            disposableSuppliers.remove(instance);
        }
    }

    private static Supplier<?> getSupplier(BeanManager beanManager, ParameterizedType supplierType) {
        Set<Bean<?>> beans = beanManager.getBeans(supplierType);
        if (beans.isEmpty()) {
            return null;
        }

        Bean<?> bean = beans.iterator().next();
        CreationalContext<?> ctx = beanManager.createCreationalContext(bean);
        return (Supplier<?>) beanManager.getReference(bean, supplierType, ctx);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends Annotation> getScope() {
        return binding.getScope() == null ? Dependent.class : transformScope(binding.getScope());
    }

    @Override
    public Class<?> getBeanClass() {
        return this.binding.getContracts().isEmpty()
                ? super.getBeanClass()
                : (Class<?>) this.binding.getContracts().iterator().next();
    }
}
