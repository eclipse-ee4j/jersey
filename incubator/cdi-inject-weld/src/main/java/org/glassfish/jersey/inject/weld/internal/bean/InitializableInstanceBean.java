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
package org.glassfish.jersey.inject.weld.internal.bean;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.ws.rs.RuntimeType;

import org.glassfish.jersey.inject.weld.internal.inject.InitializableInstanceBinding;
import org.glassfish.jersey.inject.weld.internal.injector.JerseyClientCreationalContext;

import java.lang.annotation.Annotation;

/**
 * Instance bean to be created in the pre-initialization phase and initialized after Jersey is bootstrap.
 * @param <T> the class of the bean instance.
 */
public class InitializableInstanceBean<T> extends JerseyBean<T> {

    private InjectionTarget<T> injectionTarget;

    /**
     * Creates a new Jersey-specific {@link javax.enterprise.inject.spi.Bean} instance.
     *
     * @param binding {@link javax.enterprise.inject.spi.BeanAttributes} part of the bean.
     */
    InitializableInstanceBean(RuntimeType runtimeType, InitializableInstanceBinding<T> binding) {
        super(runtimeType, binding);
    }

    @Override
    public Class<? extends Annotation> getScope() {
        return getBinding().getScope() == null ? Dependent.class : transformScope(getBinding().getScope());
    }

    @Override
    public T create(CreationalContext<T> context) {
        InitializableInstanceBinding<T> realBinding = (InitializableInstanceBinding<T>) getBinding();
        if (JerseyClientCreationalContext.class.isInstance(context)) {
            realBinding = ((JerseyClientCreationalContext) context).getInjectionManager().getInjectionManagerBinding(realBinding);
        }
        T service = realBinding.getService();
        this.injectionTarget.inject(service, context);
        return service;
    }

    @Override
    public Class<?> getBeanClass() {
        final InitializableInstanceBinding<T> binding = (InitializableInstanceBinding<T>) getBinding();
        return binding.isInit() ? binding.getImplementationType() : Object.class;
    }

    /**
     * Lazy set of an injection target because to create fully functional injection target needs already created bean.
     *
     * @param injectionTarget {@link javax.enterprise.context.spi.Contextual} information belonging to this bean.
     */
    void setInjectionTarget(InjectionTarget<T> injectionTarget) {
        this.injectionTarget = injectionTarget;
    }

    @Override
    public String toString() {
        return "InitializableInstanceBean{" + getBeanClass() + "}";
    }
}
