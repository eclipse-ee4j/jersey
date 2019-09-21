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

import java.lang.reflect.Type;
import java.util.Set;
import java.util.function.Supplier;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericType;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.internal.inject.Bindings;
import org.glassfish.jersey.internal.inject.ContextInjectionResolver;
import org.glassfish.jersey.internal.inject.Injectee;
import org.glassfish.jersey.internal.inject.InjecteeImpl;
import org.glassfish.jersey.internal.inject.InjectionResolver;
import org.glassfish.jersey.internal.util.ReflectionHelper;
import org.glassfish.jersey.internal.util.collection.Cache;

/**
 * Injection resolver for {@link Context @Context} injection annotation.
 *
 * @author Petr Bouda
 */
public class ContextInjectionResolverImpl implements InjectionResolver<Context>, ContextInjectionResolver {

    private Supplier<BeanManager> beanManager;

    /**
     * Creates a new {@link ContextInjectionResolver} with {@link BeanManager} to fetch Bean descriptors.
     *
     * @param beanManager current bean manager.
     */
    ContextInjectionResolverImpl(Supplier<BeanManager> beanManager) {
        this.beanManager = beanManager;
    }

    private final Cache<Type, Bean<?>> descriptorCache = new Cache<>(key -> {
        Set<Bean<?>> beans = beanManager.get().getBeans(key);
        if (beans.isEmpty()) {
            return null;
        }
        return beans.iterator().next();
    });

    @Override
    public Object resolve(Injectee injectee) {
        Injectee newInjectee = injectee;
        if (injectee.isFactory()) {
            newInjectee = getFactoryInjectee(injectee, ReflectionHelper.getTypeArgument(injectee.getRequiredType(), 0));
        }

        Bean<?> bean = descriptorCache.apply(newInjectee.getRequiredType());

        if (bean != null) {
            CreationalContext ctx = beanManager.get().createCreationalContext(bean);
            Object result = bean.create(ctx);

            if (injectee.isFactory()) {
                return (Supplier<Object>) () -> result;
            } else {
                return result;
            }
        }
        return null;
    }

    @Override
    public boolean isConstructorParameterIndicator() {
        return true;
    }

    @Override
    public boolean isMethodParameterIndicator() {
        return false;
    }

    @Override
    public Class<Context> getAnnotation() {
        return Context.class;
    }

    /**
     * Context injection resolver binder.
     */
    public static final class Binder extends AbstractBinder {

        private Supplier<BeanManager> beanManager;

        public Binder(Supplier<BeanManager> beanManager) {
            this.beanManager = beanManager;
        }

        @Override
        @SuppressWarnings("unchecked")
        protected void configure() {
            ContextInjectionResolverImpl resolver = new ContextInjectionResolverImpl(beanManager);

            /*
             * Binding for CDI, without this binding JerseyInjectionTarget wouldn't know about the ContextInjectionTarget and
             * injection into fields would be disabled.
             */
            bind(resolver)
                    .to(new GenericType<InjectionResolver<Context>>() {})
                    .to(ContextInjectionResolver.class);

            /*
             * Binding for Jersey, without this binding Jersey wouldn't put together ContextInjectionResolver and
             * DelegatedInjectionValueParamProvider and therefore injection into resource method would be disabled.
             */
            bind(Bindings.service(resolver))
                    .to(new GenericType<InjectionResolver<Context>>() {})
                    .to(ContextInjectionResolver.class);
        }
    }

    private Injectee getFactoryInjectee(Injectee injectee, Type requiredType) {
        return new RequiredTypeOverridingInjectee(injectee, requiredType);
    }

    private static class RequiredTypeOverridingInjectee extends InjecteeImpl {
        private RequiredTypeOverridingInjectee(Injectee injectee, Type requiredType) {
            setFactory(injectee.isFactory());
            setInjecteeClass(injectee.getInjecteeClass());
            setInjecteeDescriptor(injectee.getInjecteeDescriptor());
            setOptional(injectee.isOptional());
            setParent(injectee.getParent());
            setPosition(injectee.getPosition());
            setRequiredQualifiers(injectee.getRequiredQualifiers());
            setRequiredType(requiredType);
        }
    }
}
