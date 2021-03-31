/*
 * Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.microprofile.restclient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.enterprise.inject.spi.BeanManager;
import javax.ws.rs.ext.ParamConverterProvider;

import org.eclipse.microprofile.rest.client.ext.AsyncInvocationInterceptor;
import org.eclipse.microprofile.rest.client.ext.AsyncInvocationInterceptorFactory;
import org.eclipse.microprofile.rest.client.ext.ResponseExceptionMapper;
import org.glassfish.jersey.internal.inject.InjectionManager;

/**
 * RestClientContext stores all of the date needed in RestClient runtime.
 */
class RestClientContext {

    private final Class<?> restClientClass;
    private final Set<ResponseExceptionMapper<?>> responseExceptionMappers;
    private final Set<ParamConverterProvider> paramConverterProviders;
    private final Set<InboundHeadersProvider> inboundHeadersProviders;
    private final List<AsyncInvocationInterceptorFactory> asyncInterceptorFactories;
    private final InjectionManager injectionManager;
    private final BeanManager beanManager;

    RestClientContext(Builder builder) {
        this.restClientClass = builder.restClientClass;
        this.responseExceptionMappers = builder.responseExceptionMappers;
        this.paramConverterProviders = builder.paramConverterProviders;
        this.inboundHeadersProviders = builder.inboundHeadersProviders;
        this.asyncInterceptorFactories = builder.asyncInterceptorFactories;
        this.injectionManager = builder.injectionManager;
        this.beanManager = builder.beanManager;
    }

    static Builder builder(Class<?> restClientClass) {
        return new Builder(restClientClass);
    }

    Class<?> restClientClass() {
        return restClientClass;
    }

    /**
     * Return {@link Set} of registered {@link ResponseExceptionMapper}
     *
     * @return registered exception mappers
     */
    Set<ResponseExceptionMapper<?>> responseExceptionMappers() {
        return responseExceptionMappers;
    }

    /**
     * Returns {@link Set} of registered {@link ParamConverterProvider}
     *
     * @return registered param converter providers
     */
    Set<ParamConverterProvider> paramConverterProviders() {
        return paramConverterProviders;
    }

    /**
     * Returns {@link Set} of registered {@link InboundHeadersProvider}
     *
     * @return registered inbound header providers
     */
    Set<InboundHeadersProvider> inboundHeadersProviders() {
        return inboundHeadersProviders;
    }

    /**
     * Return {@link List} of registered {@link AsyncInvocationInterceptor}
     *
     * @return registered async interceptors
     */
    List<AsyncInvocationInterceptorFactory> asyncInterceptorFactories() {
        return asyncInterceptorFactories;
    }

    /**
     * Return current {@link InjectionManager}.
     *
     * @return injection manager
     */
    InjectionManager injectionManager() {
        return injectionManager;
    }

    /**
     * Return current {@link BeanManager}.
     *
     * @return bean manager
     */
    BeanManager beanManager() {
        return beanManager;
    }

    /**
     * {@link RestClientContext} builder.
     */
    static class Builder {

        private final Class<?> restClientClass;
        private Set<ResponseExceptionMapper<?>> responseExceptionMappers = Collections.emptySet();
        private Set<ParamConverterProvider> paramConverterProviders = Collections.emptySet();
        private Set<InboundHeadersProvider> inboundHeadersProviders = Collections.emptySet();
        private List<AsyncInvocationInterceptorFactory> asyncInterceptorFactories = Collections.emptyList();
        private InjectionManager injectionManager;
        private BeanManager beanManager;

        private Builder(Class<?> restClientClass) {
            this.restClientClass = Objects.requireNonNull(restClientClass);
        }

        Builder copyFrom(RestClientContext context) {
            responseExceptionMappers = context.responseExceptionMappers;
            paramConverterProviders = context.paramConverterProviders;
            inboundHeadersProviders = context.inboundHeadersProviders;
            asyncInterceptorFactories = context.asyncInterceptorFactories;
            injectionManager = context.injectionManager;
            beanManager = context.beanManager;
            return this;
        }

        Builder responseExceptionMappers(Set<ResponseExceptionMapper<?>> responseExceptionMappers) {
            this.responseExceptionMappers = new HashSet<>(responseExceptionMappers);
            return this;
        }

        Builder paramConverterProviders(Set<ParamConverterProvider> paramConverterProviders) {
            this.paramConverterProviders = new HashSet<>(paramConverterProviders);
            return this;
        }

        Builder inboundHeadersProviders(Set<InboundHeadersProvider> inboundHeadersProviders) {
            this.inboundHeadersProviders = new HashSet<>(inboundHeadersProviders);
            return this;
        }

        Builder asyncInterceptorFactories(List<AsyncInvocationInterceptorFactory> asyncInterceptorFactories) {
            this.asyncInterceptorFactories = new ArrayList<>(asyncInterceptorFactories);
            return this;
        }

        Builder injectionManager(InjectionManager injectionManager) {
            this.injectionManager = injectionManager;
            return this;
        }

        Builder beanManager(BeanManager beanManager) {
            this.beanManager = beanManager;
            return this;
        }

        RestClientContext build() {
            return new RestClientContext(this);
        }

    }

}
