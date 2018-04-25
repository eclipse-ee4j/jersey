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

import java.util.function.Function;

import javax.ws.rs.BeanParam;

import javax.inject.Provider;
import javax.inject.Singleton;

import org.glassfish.jersey.internal.inject.Bindings;
import org.glassfish.jersey.internal.inject.ForeignDescriptor;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.internal.util.collection.Cache;
import org.glassfish.jersey.process.internal.RequestScoped;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.model.Parameter;

/**
 * Value factory provider for {@link BeanParam bean parameters}.
 *
 * @author Miroslav Fuksa
 */
@Singleton
final class BeanParamValueParamProvider extends AbstractValueParamProvider {

    private final InjectionManager injectionManager;

    private static final class BeanParamValueProvider implements Function<ContainerRequest, Object> {
        private final Parameter parameter;
        private final InjectionManager injectionManager;

        private final Cache<Class<?>, ForeignDescriptor> descriptorCache
                = new Cache<>(new Function<Class<?>, ForeignDescriptor>() {
                    @Override
                    public ForeignDescriptor apply(Class<?> key) {
                        // below we make sure HK2 behaves as if injection happens into a request scoped type
                        // this is to avoid having proxies injected (see JERSEY-2386)
                        // before touching the following statement, check BeanParamMemoryLeakTest first!
                        return injectionManager
                                .createForeignDescriptor(Bindings.serviceAsContract(key).in(RequestScoped.class));
                    }
                });

        private BeanParamValueProvider(InjectionManager injectionManager, Parameter parameter) {
            this.injectionManager = injectionManager;
            this.parameter = parameter;
        }

        @Override
        public Object apply(ContainerRequest request) {
            Class<?> rawType = parameter.getRawType();
            Object fromHk2 = injectionManager.getInstance(rawType);
            if (fromHk2 != null) { // the bean parameter type is already bound in HK2, let's just take it from there
                return fromHk2;
            }
            ForeignDescriptor foreignDescriptor = descriptorCache.apply(rawType);
            return injectionManager.getInstance(foreignDescriptor);
        }
    }

    /**
     * Creates new instance initialized from parameters injected by HK2.
     *
     * @param mpep            multivalued parameter extractor provider.
     */
    public BeanParamValueParamProvider(Provider<MultivaluedParameterExtractorProvider> mpep,
            InjectionManager injectionManager) {
        super(mpep, Parameter.Source.BEAN_PARAM);
        this.injectionManager = injectionManager;
    }

    @Override
    public Function<ContainerRequest, ?> createValueProvider(Parameter parameter) {
        return new BeanParamValueProvider(injectionManager, parameter);
    }
}
