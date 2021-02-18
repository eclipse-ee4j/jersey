/*
 * Copyright (c) 2019, 2021 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2019 Payara Foundation and/or its affiliates. All rights reserved.
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

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ParamConverterProvider;

import org.eclipse.microprofile.rest.client.RestClientDefinitionException;
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.ext.AsyncInvocationInterceptor;
import org.eclipse.microprofile.rest.client.ext.AsyncInvocationInterceptorFactory;
import org.eclipse.microprofile.rest.client.ext.ClientHeadersFactory;
import org.eclipse.microprofile.rest.client.ext.ResponseExceptionMapper;
import org.glassfish.jersey.client.inject.ParameterUpdater;
import org.glassfish.jersey.client.inject.ParameterUpdaterProvider;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.internal.inject.Providers;
import org.glassfish.jersey.model.Parameter;

/**
 * Model of interface and its annotation.
 *
 * @author David Kral
 * @author Patrik Dudits
 * @author Tomas Langer
 */
class InterfaceModel {

    private static final Logger LOGGER = Logger.getLogger(InterfaceModel.class.getName());

    private final Class<?> restClientClass;
    private final String[] produces;
    private final String[] consumes;
    private final String path;
    private final ClientHeadersFactory clientHeadersFactory;
    private final CreationalContext<?> creationalContext;
    private final RestClientContext context;

    private final List<ClientHeaderParamModel> clientHeaders;
    private final Set<Annotation> interceptorAnnotations;

    /**
     * Creates new model based on interface class. Interface is parsed according to specific annotations.
     *
     * @param context RestClient data context
     * @return new model instance
     */
    static InterfaceModel from(RestClientContext context) {
        return new Builder(context).build();
    }

    private InterfaceModel(Builder builder) {
        this.restClientClass = builder.restClientClass;
        this.context = builder.context;
        this.path = builder.pathValue;
        this.produces = builder.produces;
        this.consumes = builder.consumes;
        this.clientHeaders = builder.clientHeaders;
        this.clientHeadersFactory = builder.clientHeadersFactory;
        this.interceptorAnnotations = builder.interceptorAnnotations;
        this.creationalContext = builder.creationalContext;
    }

    /**
     * Returns rest client interface class.
     *
     * @return interface class
     */
    Class<?> getRestClientClass() {
        return restClientClass;
    }

    /**
     * Returns defined produces media types.
     *
     * @return produces
     */
    String[] getProduces() {
        return produces;
    }

    /**
     * Returns defined consumes media types.
     *
     * @return consumes
     */
    String[] getConsumes() {
        return consumes;
    }

    /**
     * Returns path value defined on interface level.
     *
     * @return path value
     */
    String getPath() {
        return path;
    }

    /**
     * Returns registered instance of {@link ClientHeadersFactory}.
     *
     * @return registered factory
     */
    Optional<ClientHeadersFactory> getClientHeadersFactory() {
        return Optional.ofNullable(clientHeadersFactory);
    }

    /**
     * Returns {@link List} of processed annotation {@link ClientHeaderParam} to {@link ClientHeaderParamModel}
     *
     * @return registered factories
     */
    List<ClientHeaderParamModel> getClientHeaders() {
        return clientHeaders;
    }

    /**
     * Return context of the RestClient.
     *
     * @return context
     */
    RestClientContext context() {
        return context;
    }

    /**
     * Returns {@link Set} of interceptor annotations
     *
     * @return interceptor annotations
     */
    Set<Annotation> getInterceptorAnnotations() {
        return interceptorAnnotations;
    }

    /**
     * Context bound to this model.
     *
     * @return context
     */
    CreationalContext<?> getCreationalContext() {
        return creationalContext;
    }

    /**
     * Resolves value of the method argument.
     *
     * @param arg actual argument value
     * @return converted value of argument
     */
    Object resolveParamValue(Object arg, Parameter parameter) {
        final Iterable<ParameterUpdaterProvider> parameterUpdaterProviders
                = Providers.getAllProviders(context.injectionManager(), ParameterUpdaterProvider.class);
        for (final ParameterUpdaterProvider parameterUpdaterProvider : parameterUpdaterProviders) {
            if (parameterUpdaterProvider != null) {
                ParameterUpdater<Object, Object> updater =
                        (ParameterUpdater<Object, Object>) parameterUpdaterProvider.get(parameter);
                return updater.update(arg);
            }
        }
        return arg;
    }

    private static class Builder {

        private final Class<?> restClientClass;
        private final RestClientContext context;
        private String pathValue;
        private String[] produces;
        private String[] consumes;
        private ClientHeadersFactory clientHeadersFactory;
        private CreationalContext<?> creationalContext;
        private List<ClientHeaderParamModel> clientHeaders;
        private Set<Annotation> interceptorAnnotations;

        private Builder(RestClientContext context) {
            this.restClientClass = context.restClientClass();
            this.context = context;
            filterAllInterceptorAnnotations();
        }

        private void filterAllInterceptorAnnotations() {
            creationalContext = null;
            interceptorAnnotations = new HashSet<>();
            BeanManager beanManager = context.beanManager();
            if (beanManager != null) {
                creationalContext = beanManager.createCreationalContext(null);
                for (Annotation annotation : restClientClass.getAnnotations()) {
                    if (beanManager.isInterceptorBinding(annotation.annotationType())) {
                        interceptorAnnotations.add(annotation);
                    }
                }
            }
        }

        /**
         * Path value from {@link Path} annotation. If annotation is null, empty String is set as path.
         *
         * @param path {@link Path} annotation
         * @return updated Builder instance
         */
        Builder pathValue(Path path) {
            this.pathValue = path != null ? path.value() : "";
            //if only / is added to path like this "localhost:80/test" it makes invalid path "localhost:80/test/"
            this.pathValue = pathValue.equals("/") ? "" : pathValue;
            return this;
        }

        /**
         * Extracts MediaTypes from {@link Produces} annotation.
         * If annotation is null, new String array with {@link MediaType#APPLICATION_JSON} is set.
         *
         * @param produces {@link Produces} annotation
         * @return updated Builder instance
         */
        Builder produces(Produces produces) {
            this.produces = produces != null ? produces.value() : new String[] {MediaType.APPLICATION_JSON};
            return this;
        }

        /**
         * Extracts MediaTypes from {@link Consumes} annotation.
         * If annotation is null, new String array with {@link MediaType#APPLICATION_JSON} is set.
         *
         * @param consumes {@link Consumes} annotation
         * @return updated Builder instance
         */
        Builder consumes(Consumes consumes) {
            this.consumes = consumes != null ? consumes.value() : new String[] {MediaType.APPLICATION_JSON};
            return this;
        }

        /**
         * Process data from {@link ClientHeaderParam} annotation to extract methods and values.
         *
         * @param clientHeaderParams {@link ClientHeaderParam} annotations
         * @return updated Builder instance
         */
        Builder clientHeaders(ClientHeaderParam[] clientHeaderParams) {
            clientHeaders = Arrays.stream(clientHeaderParams)
                    .map(clientHeaderParam -> new ClientHeaderParamModel(restClientClass, clientHeaderParam))
                    .collect(Collectors.toList());
            return this;
        }

        Builder clientHeadersFactory(RegisterClientHeaders registerClientHeaders) {
            if (registerClientHeaders != null) {
                Class<? extends ClientHeadersFactory> value = registerClientHeaders.value();
                try {
                    clientHeadersFactory = CDI.current().select(value).get();
                } catch (Exception ex) {
                    LOGGER.log(Level.FINEST, ex, () -> "This class is not a CDI bean. " + value);
                    clientHeadersFactory = ReflectionUtil.createInstance(value);
                }
            }
            return this;
        }

        /**
         * Creates new InterfaceModel instance.
         *
         * @return new instance
         */
        InterfaceModel build() {
            pathValue(restClientClass.getAnnotation(Path.class));
            produces(restClientClass.getAnnotation(Produces.class));
            consumes(restClientClass.getAnnotation(Consumes.class));
            clientHeaders(restClientClass.getAnnotationsByType(ClientHeaderParam.class));
            clientHeadersFactory(restClientClass.getAnnotation(RegisterClientHeaders.class));
            validateHeaderDuplicityNames();
            return new InterfaceModel(this);
        }

        private void validateHeaderDuplicityNames() {
            ArrayList<String> names = new ArrayList<>();
            for (ClientHeaderParamModel clientHeaderParamModel : clientHeaders) {
                String headerName = clientHeaderParamModel.getHeaderName();
                if (names.contains(headerName)) {
                    throw new RestClientDefinitionException("Header name cannot be registered more then once on the same target."
                                                                    + "See " + restClientClass.getName());
                }
                names.add(headerName);
            }
        }
    }
}
