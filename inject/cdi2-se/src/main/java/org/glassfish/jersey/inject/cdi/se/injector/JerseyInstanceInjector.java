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

package org.glassfish.jersey.inject.cdi.se.injector;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import javax.enterprise.inject.spi.Bean;

import org.glassfish.jersey.internal.inject.InjectionResolver;

/**
 * Class that accepts all registered {@link InjectionResolver} and inject all possible values annotated by JAX-RS annotations
 * into provided instance in {@link #inject(Object)}.
 *
 * @author Petr Bouda
 */
class JerseyInstanceInjector<T> {

    private final Bean<T> bean;
    private final Map<Field, InjectionResolver> cachedFields;

    private final JerseyProxyResolver proxyResolver = new JerseyProxyResolver();

    /**
     * Constructor that creates a new class injector for the given class.
     *
     * @param bean      information about the injected class.
     * @param resolvers all resolvers which are registered in the application.
     */
    JerseyInstanceInjector(Bean<T> bean, Collection<InjectionResolver> resolvers) {
        this.bean = bean;
        this.cachedFields = analyzeFields(bean.getBeanClass(), resolvers);
    }

    /**
     * Takes an instance an inject the annotated field which were analyzed during the injector construction in method
     * {@link #analyzeFields(Class, Collection)}.
     *
     * @param injectMe an instance into which the values will be injected.
     */
    void inject(T injectMe) {
        InjectionUtils.justInject(injectMe, bean, cachedFields, proxyResolver);
    }

    /**
     * Takes a class and returns all fields along with {@link InjectionResolver} which will be used for injection during injection
     * process.
     *
     * @param clazz     class to be analyzed.
     * @param resolvers all registered injection resolvers.
     * @return immutable map of all fields along with injection resolvers using that can be injected.
     */
    private Map<Field, InjectionResolver> analyzeFields(Class<?> clazz, Collection<InjectionResolver> resolvers) {
        Map<? extends Class<?>, InjectionResolver> injectAnnotations = InjectionUtils.mapAnnotationToResolver(resolvers);

        Collector collector = new Collector();
        Set<Field> fields = InjectionUtils.getFields(clazz, injectAnnotations.keySet(), collector);
        collector.throwIfErrors();
        return InjectionUtils.mapElementToResolver(fields, injectAnnotations);
    }
}
