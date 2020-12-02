/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved.
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

/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
// Portions Copyright [2018] [Payara Foundation and/or its affiliates]

package org.glassfish.jersey.server.validation.internal.hibernate;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionTarget;

/**
 * @author Hardy Ferentschik
 */
public class DestructibleBeanInstance<T> {
    private final T instance;
    private final InjectionTarget<T> injectionTarget;

    public DestructibleBeanInstance(BeanManager beanManager, Class<T> key) {
        this.injectionTarget = createInjectionTarget(beanManager, key);
        this.instance = createAndInjectBeans(beanManager, injectionTarget);
    }

    @SuppressWarnings("unchecked")
    public DestructibleBeanInstance(BeanManager beanManager, T instance) {
        this.injectionTarget = createInjectionTarget(beanManager, (Class<T>) instance.getClass());
        injectBeans(beanManager, beanManager.createCreationalContext(null), injectionTarget, instance);
        this.instance = instance;
    }

    public T getInstance() {
        return instance;
    }

    public void destroy() {
        injectionTarget.preDestroy(instance);
        injectionTarget.dispose(instance);
    }

    private InjectionTarget<T> createInjectionTarget(BeanManager beanManager, Class<T> type) {
        AnnotatedType<T> annotatedType = beanManager.createAnnotatedType(type);
        return beanManager.createInjectionTarget(annotatedType);
    }

    private static <T> T createAndInjectBeans(BeanManager beanManager, InjectionTarget<T> injectionTarget) {
        CreationalContext<T> creationalContext = beanManager.createCreationalContext(null);

        T instance = injectionTarget.produce(creationalContext);
        injectBeans(beanManager, creationalContext, injectionTarget, instance);

        return instance;
    }

    private static <T> void injectBeans(BeanManager beanManager, CreationalContext<T> creationalContext,
                                        InjectionTarget<T> injectionTarget, T instance) {
        injectionTarget.inject(instance, creationalContext);
        injectionTarget.postConstruct(instance);
    }
}
