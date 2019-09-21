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
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.InjectionTarget;

import org.glassfish.jersey.inject.cdi.se.ParameterizedTypeImpl;
import org.glassfish.jersey.inject.cdi.se.injector.JerseyInjectionTarget;
import org.glassfish.jersey.internal.inject.DisposableSupplier;
import org.glassfish.jersey.internal.inject.SupplierClassBinding;

/**
 * Creates an implementation of {@link javax.enterprise.inject.spi.Bean} interface using Jersey's {@link SupplierClassBinding}.
 * Binding provides the information about the bean also called {@link javax.enterprise.inject.spi.BeanAttributes} information and
 * {@link JerseyInjectionTarget} provides the contextual part of the bean because implements
 * {@link javax.enterprise.context.spi.Contextual} with Jersey injection extension (is able to inject into JAX-RS/Jersey specified
 * annotation).
 * <p>
 * Bean's implementation provides possibility to register {@link Supplier} and {@link DisposableSupplier}.
 * <p>
 * Inject example:
 * <pre>
 * AbstractBinder {
 *     &#64;Override
 *     protected void configure() {
 *         bindFactory(MyBeanSupplier.class)
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
class SupplierClassBean<T> extends JerseyBean<Supplier<T>> {

    private final Set<Type> contracts = new HashSet<>();
    private final Class<? extends Supplier<T>> supplierClass;
    private final Class<? extends Annotation> supplierScope;
    private InjectionTarget<Supplier<T>> injectionTarget;

    /**
     * Creates a new Jersey-specific {@link javax.enterprise.inject.spi.Bean} instance.
     *
     * @param binding {@link javax.enterprise.inject.spi.BeanAttributes} part of the bean.
     */
    SupplierClassBean(SupplierClassBinding<T> binding) {
        super(binding);
        this.supplierClass = binding.getSupplierClass();
        this.supplierScope = binding.getSupplierScope();

        for (Type contract : binding.getContracts()) {
            this.contracts.add(new ParameterizedTypeImpl(Supplier.class, contract));
            if (DisposableSupplier.class.isAssignableFrom(supplierClass)) {
                this.contracts.add(new ParameterizedTypeImpl(DisposableSupplier.class, contract));
            }
        }
    }

    @Override
    public Class<? extends Annotation> getScope() {
        return supplierScope == null ? Dependent.class : transformScope(supplierScope);
    }

    @Override
    public Set<Type> getTypes() {
        return contracts;
    }

    @Override
    public Supplier<T> create(CreationalContext<Supplier<T>> context) {
        Supplier<T> instance = injectionTarget.produce(context);
        injectionTarget.inject(instance, context);
        injectionTarget.postConstruct(instance);
        return instance;
    }

    @Override
    public void destroy(Supplier<T> instance, CreationalContext<Supplier<T>> context) {
        injectionTarget.preDestroy(instance);
        injectionTarget.dispose(instance);
        context.release();
    }

    @Override
    public Class<?> getBeanClass() {
        return supplierClass;
    }

    /**
     * Lazy set of an injection target because to create fully functional injection target needs already created bean.
     *
     * @param injectionTarget {@link javax.enterprise.context.spi.Contextual} information belonging to this bean.
     */
    void setInjectionTarget(InjectionTarget<Supplier<T>> injectionTarget) {
        this.injectionTarget = injectionTarget;
    }
}
