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

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.InjectionException;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;

import org.glassfish.jersey.internal.inject.InjectionResolver;
import org.glassfish.jersey.internal.util.collection.LazyValue;
import org.glassfish.jersey.internal.util.collection.Value;
import org.glassfish.jersey.internal.util.collection.Values;

/**
 * An implementation of {@link InjectionTarget} that just wraps the provided {@code InjectionTarget} because of additional
 * features in an injection phase.
 *
 * @author Petr Bouda
 */
public class WrappingJerseyInjectionTarget<T> extends AbstractInjectionTarget<T> {

    private static InjectionTarget NOOP_INJECTION_TARGET = new NoOpInjectionTarget();

    private final Bean<T> bean;
    private final LazyValue<JerseyInstanceInjector<T>> injector;
    private final InjectionTarget<T> delegate;
    private Collection<InjectionResolver> resolvers;

    /**
     * Creates a new jersey injection target with delegate as a {@link NoOpInjectionTarget} that creates no operation that
     * means that only jersey injection is available as a additional feature.
     *
     * @param bean      bean as descriptor of the class which will be injected.
     * @param resolvers all resolvers that can provide a valued for Jersey-specific injection.
     */
    public WrappingJerseyInjectionTarget(Bean<T> bean, Collection<InjectionResolver> resolvers) {
        this(NOOP_INJECTION_TARGET, bean, resolvers);
    }

    /**
     * An implementation of {@link InjectionTarget} for classes that do not fulfill bean class requirements
     * (e.g. are abstract or non-static inner classes). Instances of these class can be injected using this implementation. If the
     * application attempts to {@link #produce(CreationalContext)} a new instance of the class, {@code CreationException} is
     * thrown.
     *
     * @param delegate  CDI specific injection target.
     * @param bean      bean as descriptor of the class which will be injected.
     * @param resolvers all resolvers that can provide a valued for Jersey-specific injection.
     */
    public WrappingJerseyInjectionTarget(InjectionTarget<T> delegate, Bean<T> bean, Collection<InjectionResolver> resolvers) {
        this.bean = bean;
        this.delegate = delegate;
        this.resolvers = resolvers;
        this.injector = Values.lazy((Value<JerseyInstanceInjector<T>>)
                () -> new JerseyInstanceInjector<>(bean, this.resolvers));
    }

    @Override
    public void inject(T instance, CreationalContext<T> ctx) {
        /*
         * If an instance contains any fields which be injected by Jersey then Jersey attempts to inject them using annotations
         * retrieves from registered InjectionResolvers.
         */
        try {
            injector.get().inject(instance);
        } catch (Throwable cause) {
            throw new InjectionException(
                    "Exception occurred during Jersey/JAX-RS annotations processing in the class: " + bean.getBeanClass(), cause);
        }

        /*
         * The rest of the fields (annotated by @Inject) are injected using CDI.
         */
        super.inject(instance, ctx);
    }

    @Override
    InjectionTarget<T> delegate() {
        return delegate;
    }

    private static class NoOpInjectionTarget implements InjectionTarget<Object> {

        @Override
        public void inject(Object instance, CreationalContext<Object> ctx) {
        }

        @Override
        public void postConstruct(Object instance) {
        }

        @Override
        public void preDestroy(Object instance) {
        }

        @Override
        public Object produce(CreationalContext<Object> ctx) {
            return null;
        }

        @Override
        public void dispose(Object instance) {
        }

        @Override
        public Set<InjectionPoint> getInjectionPoints() {
            return Collections.emptySet();
        }
    }
}
