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

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.inject.Provider;

import org.glassfish.jersey.internal.inject.Injectee;
import org.glassfish.jersey.internal.inject.InjecteeImpl;
import org.glassfish.jersey.internal.inject.InjectionResolver;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedConstructor;
import org.jboss.weld.injection.ConstructorInjectionPoint;
import org.jboss.weld.injection.InjectionPointFactory;
import org.jboss.weld.injection.ParameterInjectionPoint;
import org.jboss.weld.manager.BeanManagerImpl;

/**
 * Class that creates a new instance using the provided constructor, selects and injects the values.
 *
 * @author Petr Bouda
 */
public class JerseyConstructorInjectionPoint<T> extends ConstructorInjectionPoint<T> {

    private final JerseyProxyResolver proxyResolver = new JerseyProxyResolver();

    private List<Supplier<Object>> cachedSuppliers;

    private Object[] cachedProxies;

    /**
     * Creates a new constructor injection point suitable for Jersey components.
     *
     * @param constructor resolved constructor that can be injected using Jersey.
     * @param bean        bean descriptor dedicated to the parent class.
     * @param manager     current bean manager.
     * @param resolvers   all registered resolvers.
     */
    public JerseyConstructorInjectionPoint(EnhancedAnnotatedConstructor<T> constructor, Bean<T> bean, BeanManagerImpl manager,
            Collection<InjectionResolver> resolvers) {
        super(constructor, null, constructor.getJavaClass(), InjectionPointFactory.instance(), manager);

        List<InjecteeToSupplier> valueSuppliers =
                createValueSuppliers(constructor.getJavaMember(), getParameterInjectionPoints(), resolvers);

        /*
         * Caches either created proxies if the component class is not RequestScoped or caches the supplier that just create
         * values every component creates.
         */
        if (proxyResolver.isPrixiable(bean.getScope())) {
            this.cachedProxies = generateProxies(valueSuppliers);
        } else {
            this.cachedSuppliers = valueSuppliers.stream()
                    .map(is -> is.supplier)
                    .collect(Collectors.toList());
        }
    }

    /**
     * Helper method for getting the current parameter values from a list of annotated parameters.
     *
     * @param manager The Bean manager
     * @return The object array of looked up values
     */
    public Object[] getParameterValues(BeanManagerImpl manager, CreationalContext<?> ctx, CreationalContext<?> ctxTransient) {
        if (cachedProxies == null) {
            return generateValues(cachedSuppliers);
        } else {
            return cachedProxies;
        }
    }

    private Object[] generateValues(List<Supplier<Object>> suppliers) {
        Object[] parameterValues = new Object[getParameterInjectionPoints().size()];
        for (int i = 0; i < parameterValues.length; i++) {
            parameterValues[i] = suppliers.get(i).get();
        }
        return parameterValues;
    }

    private Object[] generateProxies(List<InjecteeToSupplier> suppliers) {
        Object[] proxies = new Object[suppliers.size()];
        for (int i = 0; i < proxies.length; i++) {
            InjecteeToSupplier injecteeToSupplier = suppliers.get(i);
            if (injecteeToSupplier.injectee.isProvider()) {
                proxies[i] = new Provider<Object>() {
                    @Override
                    public Object get() {
                        return injecteeToSupplier.supplier.get();
                    }
                };
            } else {
                proxies[i] = proxyResolver.noCachedProxy(injecteeToSupplier.injectee, injecteeToSupplier.supplier);
            }
        }
        return proxies;
    }

    /**
     * Maps the parameters of the selected constructor to the injection resolver.
     *
     * @param params    all parameters of a constructor.
     * @param resolvers registered injection resolvers.
     * @return map of the parameter to injection resolver.
     */
    private List<InjecteeToSupplier> createValueSuppliers(Constructor<T> constructor,
            List<ParameterInjectionPoint<?, T>> params, Collection<InjectionResolver> resolvers) {

        List<InjecteeToSupplier> suppliers = new ArrayList<>();
        Map<? extends Class<?>, InjectionResolver> injectAnnotations = InjectionUtils.mapAnnotationToResolver(resolvers);
        for (int i = 0; i < params.size(); i++) {
            Parameter parameter = params.get(i).getAnnotated().getJavaParameter();
            InjectionResolver resolver = InjectionUtils.findResolver(injectAnnotations, parameter);
            Injectee injectee = parameterToInjectee(constructor, parameter, i);
            suppliers.add(new InjecteeToSupplier(injectee, () -> resolver.resolve(injectee)));
        }

        return suppliers;
    }

    private Injectee parameterToInjectee(Constructor<T> constructor, Parameter parameter, int position) {
        InjecteeImpl injectee = new InjecteeImpl();
        injectee.setParent(constructor);
        if (parameter.getParameterizedType() instanceof ParameterizedType
                && InjectionUtils.isProvider(parameter.getParameterizedType())) {
            ParameterizedType paramType = (ParameterizedType) parameter.getParameterizedType();
            injectee.setRequiredType(paramType.getActualTypeArguments()[0]);
            injectee.setProvider(true);
        } else {
            injectee.setRequiredType(parameter.getType());
        }
        injectee.setPosition(position);
        return injectee;
    }

    /**
     * Holder for Injectee and Supplier types. Internal class.
     */
    private static class InjecteeToSupplier {

        private final Injectee injectee;
        private final Supplier<Object> supplier;

        private InjecteeToSupplier(Injectee injectee, Supplier<Object> supplier) {
            this.injectee = injectee;
            this.supplier = supplier;
        }
    }
}
