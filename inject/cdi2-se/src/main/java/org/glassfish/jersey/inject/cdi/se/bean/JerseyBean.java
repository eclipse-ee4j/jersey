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
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Priority;
import javax.enterprise.context.Dependent;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.PassivationCapable;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Singleton;

import org.glassfish.jersey.internal.inject.Binding;
import org.glassfish.jersey.internal.inject.PerLookup;
import org.glassfish.jersey.internal.inject.PerThread;

import org.jboss.weld.environment.se.contexts.ThreadScoped;

/**
 * Jersey-specific abstract class which implements {@link Bean} interface. Class particularly contains default implementations
 * of {@link Bean} interface.
 *
 * @author Petr Bouda
 */
public abstract class JerseyBean<T> implements Bean<T>, PassivationCapable {

    static final Set<Annotation> DEFAULT_QUALIFIERS;

    static {
        DEFAULT_QUALIFIERS = new HashSet<>();
        DEFAULT_QUALIFIERS.add(new AnnotationLiteral<Default>() {});
        DEFAULT_QUALIFIERS.add(new AnnotationLiteral<Any>() {});
    }

    private final Binding<T, ?> binding;

    /**
     * JerseyBean constructor with {@link Binding} which represents {@link javax.enterprise.context.spi.Contextual} part of the
     * bean.
     *
     * @param binding information about the bean.
     */
    JerseyBean(Binding<T, ?> binding) {
        this.binding = binding;
    }

    /**
     * Transforms Jersey scopes/annotations to HK2 equivalents.
     *
     * @param scope Jersey scope/annotation.
     * @return HK2 equivalent scope/annotation.
     */
    protected static Class<? extends Annotation> transformScope(Class<? extends Annotation> scope) {
        if (scope == PerLookup.class) {
            return Dependent.class;
        } else if (scope == PerThread.class) {
            return ThreadScoped.class;
        } else if (scope == org.glassfish.jersey.process.internal.RequestScoped.class) {
            return RequestScoped.class;
        }
        return scope;
    }

    @Override
    public Set<Type> getTypes() {
        Set<Type> contracts = new HashSet<>();
        contracts.addAll(binding.getContracts());

        // Merge aliases with the main bean
        if (!binding.getAliases().isEmpty()) {
            binding.getAliases().forEach(alias -> contracts.add(alias.getContract()));
        }
        contracts.add(Object.class);
        return contracts;
    }

    @Override
    public Set<Annotation> getQualifiers() {
        Set<Annotation> qualifiers = new HashSet<>();
        qualifiers.addAll(DEFAULT_QUALIFIERS);
        if (binding.getQualifiers() != null) {
            qualifiers.addAll(binding.getQualifiers());
        }

        // Merge aliases with the main bean
        if (!binding.getAliases().isEmpty()) {
            binding.getAliases().forEach(alias -> qualifiers.addAll(alias.getQualifiers()));
        }
        return qualifiers;
    }

    @Override
    public String getName() {
        return binding.getName();
    }

    @Override
    public Class<? extends Annotation> getScope() {
        return Singleton.class;
    }

    @Override
    public Set<Class<? extends Annotation>> getStereotypes() {
        return Collections.emptySet();
    }

    @Override
    public boolean isAlternative() {
        return false;
    }

    @Override
    public boolean isNullable() {
        return false;
    }

    @Override
    public void destroy(T instance, CreationalContext<T> creationalContext) {
    }

    @Override
    public Set<InjectionPoint> getInjectionPoints() {
        return Collections.emptySet();
    }

    public int getRank() {
        if (binding.getRank() != null) {
            return binding.getRank();
        }

        Class<T> type = binding.getImplementationType();
        if (type != null) {
            Priority priority = type.getAnnotation(Priority.class);
            if (priority != null) {
                return priority.value();
            }
        }

        return 1;
    }

    @Override
    public Class<?> getBeanClass() {
        return Object.class;
    }

    @Override
    public String getId() {
        return getBeanClass().getTypeName() + "#jersey";
    }
}
