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
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Qualifier;

import org.glassfish.jersey.ext.cdi1x.internal.spi.BeanManagerProvider;
import org.glassfish.jersey.ext.cdi1x.internal.spi.InjectionManagerStore;
import org.glassfish.jersey.internal.ServiceFinder;
import org.glassfish.jersey.model.internal.RankedComparator;
import org.glassfish.jersey.model.internal.RankedProvider;

/**
 * Common CDI utility methods.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 * @author Michal Gajdos
 */
public final class CdiUtil {

    private static final BeanManagerProvider BEAN_MANAGER_PROVIDER = new DefaultBeanManagerProvider();

    /**
     * Prevent instantiation.
     */
    private CdiUtil() {
        throw new AssertionError("No instances allowed.");
    }

    /**
     * Get me list of qualifiers included in given annotation list.
     *
     * @param annotations list of annotations to introspect
     * @return annotations from the input list that are marked as qualifiers
     */
    public static Annotation[] getQualifiers(final Annotation[] annotations) {
        final List<Annotation> result = new ArrayList<>(annotations.length);
        for (final Annotation a : annotations) {
            if (a.annotationType().isAnnotationPresent(Qualifier.class)) {
                result.add(a);
            }
        }
        return result.toArray(new Annotation[result.size()]);
    }

    /**
     * Get me current bean manager. Method first tries to lookup available providers via {@code META-INF/services}. If not found
     * the bean manager is returned from the default provider.
     *
     * @return bean manager
     */
    public static BeanManager getBeanManager() {
        final BeanManagerProvider provider = lookupService(BeanManagerProvider.class);
        if (provider != null) {
            return provider.getBeanManager();
        }

        return BEAN_MANAGER_PROVIDER.getBeanManager();
    }

    /**
     * Create new instance of {@link InjectionManagerStore}. Method first tries to lookup
     * available manager via {@code META-INF/services} and if not found a new instance of default one is returned.
     *
     * @return an instance of injection manager store.
     */
    static InjectionManagerStore createHk2InjectionManagerStore() {
        final InjectionManagerStore manager = lookupService(InjectionManagerStore.class);
        return manager != null ? manager : new SingleInjectionManagerStore();
    }

    /**
     * Look for a service of given type. If more then one service is found the method sorts them are returns the one with highest
     * priority.
     *
     * @param clazz type of service to look for.
     * @param <T>   type of service to look for
     * @return instance of service with highest priority or {@code null} if service of given type cannot be found.
     * @see javax.annotation.Priority
     */
    static <T> T lookupService(final Class<T> clazz) {
        final List<RankedProvider<T>> providers = new LinkedList<>();

        for (final T provider : ServiceFinder.find(clazz)) {
            providers.add(new RankedProvider<>(provider));
        }
        Collections.sort(providers, new RankedComparator<T>(RankedComparator.Order.ASCENDING));

        return providers.isEmpty() ? null : providers.get(0).getProvider();
    }

    /**
     * Obtain a bean reference of given type from the bean manager.
     *
     * @param clazz         type of the bean to get reference to.
     * @param bean          the {@link Bean} object representing the managed bean.
     * @param beanManager   bean manager used to obtain an instance of the requested bean.
     * @param <T>           type of the bean to be returned.
     * @return a bean reference or {@code null} if a bean instance cannot be found.
     */
    static <T> T getBeanReference(final Class<T> clazz, final Bean bean, final BeanManager beanManager) {
        final CreationalContext<?> creationalContext = beanManager.createCreationalContext(bean);
        final Object result = beanManager.getReference(bean, clazz, creationalContext);

        return clazz.cast(result);
    }

    /**
     * Get me scope of a bean corresponding to given class.
     *
     * @param beanClass bean class in question.
     * @param beanManager actual bean manager.
     * @return actual bean scope or null, if the scope could not be determined.
     */
    public static Class<? extends Annotation> getBeanScope(final Class<?> beanClass, final BeanManager beanManager) {
        final Set<Bean<?>> beans = beanManager.getBeans(beanClass);
        if (beans.isEmpty()) {
            return null;
        }
        for (Bean b : beans) {
            return b.getScope();
        }
        return null;
    }
}
