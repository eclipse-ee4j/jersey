/*
 * Copyright (c) 2014, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.ext.cdi1x.internal;

import java.lang.annotation.Annotation;
import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.inject.spi.InjectionTargetFactory;

import org.glassfish.jersey.internal.inject.DisposableSupplier;
import org.glassfish.jersey.internal.inject.InjectionManager;

/**
 * Abstract supplier to provide CDI components obtained from CDI bean manager.
 * The factory handles CDI managed components as well as non-contextual managed beans.
 * To specify scope of provided CDI beans, an extension of this supplier
 * should implement properly annotated {@link java.util.function.Supplier#get()} method that
 * could just delegate to the existing {@link #_provide()} method.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
public abstract class AbstractCdiBeanSupplier<T> implements DisposableSupplier<T> {

    final Class<T> clazz;
    final InstanceManager<T> referenceProvider;
    final Annotation[] qualifiers;
    /**
     * Create new factory instance for given type and bean manager.
     *
     * @param rawType          type of the components to provide.
     * @param injectionManager actual injection manager instance.
     * @param beanManager      current bean manager to get references from.
     * @param cdiManaged       set to {@code true} if the component should be managed by CDI.
     */
    public AbstractCdiBeanSupplier(final Class<T> rawType,
                                     final InjectionManager injectionManager,
                                     final BeanManager beanManager,
                                     final boolean cdiManaged) {

        this.clazz = rawType;
        this.qualifiers = CdiUtil.getQualifiers(clazz.getAnnotations());
        this.referenceProvider = cdiManaged ? new InstanceManager<T>() {

            final Iterator<Bean<?>> beans = beanManager.getBeans(clazz, qualifiers).iterator();
            final Bean bean = beans.hasNext() ? beans.next() : null;

            @Override
            public T getInstance(final Class<T> clazz) {
                return (bean != null) ? CdiUtil.getBeanReference(clazz, bean, beanManager) : null;
            }

            @Override
            public void preDestroy(final T instance) {
                // do nothing
            }
        } : new InstanceManager<T>() {

            final AnnotatedType<T> annotatedType = beanManager.createAnnotatedType(clazz);
            final InjectionTargetFactory<T> injectionTargetFactory = beanManager.getInjectionTargetFactory(annotatedType);
            final InjectionTarget<T> injectionTarget = injectionTargetFactory.createInjectionTarget(null);

            @Override
            public T getInstance(final Class<T> clazz) {
                final CreationalContext<T> creationalContext = beanManager.createCreationalContext(null);
                final T instance = injectionTarget.produce(creationalContext);
                injectionTarget.inject(instance, creationalContext);
                if (injectionManager != null) {
                    injectionManager.inject(instance, CdiComponentProvider.CDI_CLASS_ANALYZER);
                }
                injectionTarget.postConstruct(instance);
                return instance;
            }

            @Override
            public void preDestroy(final T instance) {
                injectionTarget.preDestroy(instance);
            }
        };
    }

    @SuppressWarnings(value = "unchecked")
    /* package */ T _provide() {
        final T instance = referenceProvider.getInstance(clazz);
        if (instance != null) {
            return instance;
        }
        throw new NoSuchElementException(LocalizationMessages.CDI_LOOKUP_FAILED(clazz));
    }

    @Override
    public void dispose(final T instance) {
        referenceProvider.preDestroy(instance);
    }

    private interface InstanceManager<T> {

        /**
         * Get me correctly instantiated and injected instance.
         *
         * @param clazz type of the component to instantiate.
         * @return injected component instance.
         */
        T getInstance(Class<T> clazz);

        /**
         * Do whatever needs to be done before given instance is destroyed.
         *
         * @param instance to be destroyed.
         */
        void preDestroy(T instance);
    }
}
