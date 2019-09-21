/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.server.spring;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import javax.inject.Singleton;

import org.glassfish.jersey.internal.inject.Injectee;
import org.glassfish.jersey.internal.inject.InjectionResolver;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.context.ApplicationContext;
import org.springframework.core.MethodParameter;

/**
 * HK2 injection resolver for Spring framework {@link Autowired} annotation injection.
 *
 * @author Marko Asplund (marko.asplund at yahoo.com)
 * @author Vetle Leinonen-Roeim (vetle at roeim.net)
 */
@Singleton
public class AutowiredInjectResolver implements InjectionResolver<Autowired> {

    private static final Logger LOGGER = Logger.getLogger(AutowiredInjectResolver.class.getName());

    private volatile ApplicationContext ctx;

    /**
     * Create a new instance.
     *
     * @param ctx Spring application context.
     */
    public AutowiredInjectResolver(ApplicationContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public Object resolve(Injectee injectee) {
        AnnotatedElement parent = injectee.getParent();
        String beanName = null;
        if (parent != null) {
            Qualifier an = parent.getAnnotation(Qualifier.class);
            if (an != null) {
                beanName = an.value();
            }
        }
        boolean required = parent != null ? parent.getAnnotation(Autowired.class).required() : false;
        return getBeanFromSpringContext(beanName, injectee, required);
    }

    private Object getBeanFromSpringContext(String beanName, Injectee injectee, final boolean required) {
        try {
            DependencyDescriptor dependencyDescriptor = createSpringDependencyDescriptor(injectee);
            Set<String> autowiredBeanNames = new HashSet<>(1);
            autowiredBeanNames.add(beanName);
            return ctx.getAutowireCapableBeanFactory().resolveDependency(dependencyDescriptor, null,
                    autowiredBeanNames, null);
        } catch (NoSuchBeanDefinitionException e) {
            if (required) {
                LOGGER.warning(e.getMessage());
                throw e;
            }
            return null;
        }
    }

    private DependencyDescriptor createSpringDependencyDescriptor(final Injectee injectee) {
        AnnotatedElement annotatedElement = injectee.getParent();

        if (annotatedElement.getClass().isAssignableFrom(Field.class)) {
            return new DependencyDescriptor((Field) annotatedElement, !injectee.isOptional());
        } else if (annotatedElement.getClass().isAssignableFrom(Method.class)) {
            return new DependencyDescriptor(
                    new MethodParameter((Method) annotatedElement, injectee.getPosition()), !injectee.isOptional());
        } else {
            return new DependencyDescriptor(
                    new MethodParameter((Constructor) annotatedElement, injectee.getPosition()), !injectee.isOptional());
        }
    }

    @Override
    public boolean isConstructorParameterIndicator() {
        return false;
    }

    @Override
    public boolean isMethodParameterIndicator() {
        return false;
    }

    @Override
    public Class<Autowired> getAnnotation() {
        return Autowired.class;
    }
}
