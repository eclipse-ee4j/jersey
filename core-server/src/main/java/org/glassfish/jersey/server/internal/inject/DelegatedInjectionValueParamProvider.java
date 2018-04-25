/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.server.internal.inject;

import java.lang.reflect.ParameterizedType;
import java.util.function.Function;

import javax.inject.Singleton;

import org.glassfish.jersey.internal.inject.Binding;
import org.glassfish.jersey.internal.inject.Bindings;
import org.glassfish.jersey.internal.inject.ContextInjectionResolver;
import org.glassfish.jersey.internal.inject.ForeignDescriptor;
import org.glassfish.jersey.internal.inject.Injectee;
import org.glassfish.jersey.internal.inject.InjecteeImpl;
import org.glassfish.jersey.internal.util.collection.Cache;
import org.glassfish.jersey.internal.util.collection.LazyValue;
import org.glassfish.jersey.process.internal.RequestScoped;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.model.Parameter;
import org.glassfish.jersey.server.model.Parameter.Source;
import org.glassfish.jersey.server.spi.internal.ValueParamProvider;

/**
 * Value factory provider that delegates the injection target lookup to the underlying injection provider.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
@Singleton
class DelegatedInjectionValueParamProvider implements ValueParamProvider {

    private final LazyValue<ContextInjectionResolver> resolver;

    private final Function<Binding, ForeignDescriptor> foreignDescriptorFactory;

    /**
     * Injection constructor.
     *
     * @param resolver                 context injection resolver.
     * @param foreignDescriptorFactory function that is able to create a new foreign descriptor.
     */
    public DelegatedInjectionValueParamProvider(LazyValue<ContextInjectionResolver> resolver,
            Function<Binding, ForeignDescriptor> foreignDescriptorFactory) {
        this.resolver = resolver;
        this.foreignDescriptorFactory = foreignDescriptorFactory;
    }

    @Override
    public Function<ContainerRequest, ?> getValueProvider(final Parameter parameter) {
        Source paramSource = parameter.getSource();
        if (paramSource == Parameter.Source.CONTEXT) {
            return containerRequest -> resolver.get().resolve(getInjectee(parameter));
        }
        return null;
    }

    @Override
    public PriorityType getPriority() {
        return Priority.LOW;
    }

    /**
     * Creates a new object {@link Injectee} corresponding to the injecting point. The injectee contains basic information
     * about the injection point types and {@link ForeignDescriptor} of the underlying DI provider to make delegated injection
     * resolver as simple as possible.
     *
     * @param parameter jersey-like parameter corresponding to one resource-method's parameter.
     * @return injectee instance as a source of the information about the injecting point.
     */
    private Injectee getInjectee(Parameter parameter) {
        InjecteeImpl injectee = new InjecteeImpl();
        injectee.setRequiredType(parameter.getType());
        injectee.setInjecteeClass(parameter.getRawType());
        ForeignDescriptor proxyDescriptor = descriptorCache.apply(parameter);
        if (proxyDescriptor != null) {
            injectee.setInjecteeDescriptor(proxyDescriptor);
        }
        return injectee;
    }

    /**
     * We do not want to create a new descriptor instance for every and each method invocation.
     * If the underlying DI descriptor {@link ForeignDescriptor} is already created for the given {@link Parameter} then
     * used the already created descriptor.
     */
    private final Cache<Parameter, ForeignDescriptor> descriptorCache =
            new Cache<>(parameter -> {
                Class<?> rawType = parameter.getRawType();
                if (rawType.isInterface() && !(parameter.getType() instanceof ParameterizedType)) {
                    return createDescriptor(rawType);
                }
                return null;
            });

    /**
     * Method is able to create form incoming class and {@link Binding jersey descriptor} a {@link ForeignDescriptor} which is
     * provided by underlying DI provider.
     *
     * @param clazz class from which jersey-like descriptor is created.
     * @return foreign descriptor of the underlying DI provider.
     */
    private ForeignDescriptor createDescriptor(Class<?> clazz) {
        return foreignDescriptorFactory.apply(Bindings.serviceAsContract(clazz).in(RequestScoped.class));
    }
}
