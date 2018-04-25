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
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Set;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.Path;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;

import org.glassfish.jersey.inject.cdi.se.injector.JerseyInjectionTarget;
import org.glassfish.jersey.internal.inject.ClassBinding;

/**
 * Creates an implementation of {@link javax.enterprise.inject.spi.Bean} interface using Jersey's {@link ClassBinding}. Binding
 * provides the information about the bean also called {@link javax.enterprise.inject.spi.BeanAttributes} information and
 * {@link JerseyInjectionTarget} provides the contextual part of the bean because implements
 * {@link javax.enterprise.context.spi.Contextual} with Jersey injection extension (is able to inject into JAX-RS/Jersey specified
 * annotation).
 * <p>
 * Inject example:
 * <pre>
 * AbstractBinder {
 *     &#64;Override
 *     protected void configure() {
 *         bind(MyBean.class)
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
class ClassBean<T> extends JerseyBean<T> {

    private final ClassBinding<T> binding;
    private InjectionTarget<T> injectionTarget;

    /**
     * Creates a new Jersey-specific {@link javax.enterprise.inject.spi.Bean} instance.
     *
     * @param binding {@link javax.enterprise.inject.spi.BeanAttributes} part of the bean.
     */
    ClassBean(ClassBinding<T> binding) {
        super(binding);
        this.binding = binding;
    }

    @Override
    public Class<? extends Annotation> getScope() {
        /*
         * Resource class without the Scope annotation should registered as a RequestScoped.
         */
        if (isResourceClass(binding.getService()) && binding.getScope() == null) {
            return RequestScoped.class;
        }

        return binding.getScope() == null ? Dependent.class : transformScope(binding.getScope());
    }

    @Override
    @SuppressWarnings("unchecked")
    public T create(CreationalContext<T> context) {
        T instance = injectionTarget.produce(context);
        injectionTarget.inject(instance, context);
        injectionTarget.postConstruct(instance);
        return instance;
    }

    @Override
    public void destroy(T instance, CreationalContext<T> context) {
        injectionTarget.preDestroy(instance);
        injectionTarget.dispose(instance);
        context.release();
    }

    @Override
    public Set<Type> getTypes() {
        Set<Type> contracts = super.getTypes();
        contracts.addAll(Arrays.asList(binding.getService().getInterfaces()));
        return contracts;
    }

    @Override
    public Class<?> getBeanClass() {
        return binding.getService();
    }

    @Override
    public Set<InjectionPoint> getInjectionPoints() {
        return injectionTarget.getInjectionPoints();
    }

    /**
     * Lazy set of an injection target because to create fully functional injection target needs already created bean.
     *
     * @param injectionTarget {@link javax.enterprise.context.spi.Contextual} information belonging to this bean.
     */
    void setInjectionTarget(InjectionTarget<T> injectionTarget) {
        this.injectionTarget = injectionTarget;
    }

    private static boolean isResourceClass(Class<?> clazz) {
        if (isJaxrsResource(clazz)) {
            return true;
        }

        for (Class<?> iface : clazz.getInterfaces()) {
            if (isJaxrsResource(iface)) {
                return true;
            }
        }

        return false;
    }

    private static boolean isJaxrsResource(Class<?> clazz) {
        if (clazz.isAnnotationPresent(Path.class)) {
            return true;
        }

        for (Method method : clazz.getMethods()) {
            if (method.isAnnotationPresent(Path.class)) {
                return true;
            }

            for (Annotation annotation : method.getAnnotations()) {
                if (annotation.annotationType().isAnnotationPresent(HttpMethod.class)) {
                    return true;
                }
            }
        }

        return false;
    }

}
