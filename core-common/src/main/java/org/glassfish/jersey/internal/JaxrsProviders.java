/*
 * Copyright (c) 2012, 2024 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import jakarta.ws.rs.RuntimeType;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.MessageBodyReader;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.ext.Providers;

import jakarta.inject.Inject;
import jakarta.inject.Provider;

import org.glassfish.jersey.innate.inject.Bindings;
import org.glassfish.jersey.innate.inject.InjectionIds;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.internal.inject.PerLookup;
import org.glassfish.jersey.message.MessageBodyWorkers;
import org.glassfish.jersey.spi.ContextResolvers;
import org.glassfish.jersey.spi.ExceptionMappers;

/**
 * Jersey implementation of JAX-RS {@link Providers} contract.
 *
 * @author Marek Potociar
 */
public final class JaxrsProviders implements Providers {

    /**
     * Configurator which initializes and registers {@link Providers} instance into {@link InjectionManager} and
     * {@link BootstrapBag}.
     * Instances of these interfaces are processed, configured and provided using this configurator:
     * <ul>
     * <li>{@link Providers}</li>
     * </ul>
     */
    public static class ProvidersConfigurator implements BootstrapConfigurator {

        @Override
        public void init(InjectionManager injectionManager, BootstrapBag bootstrapBag) {
            injectionManager.register(
                    Bindings.service(JaxrsProviders.class)
                            .to(Providers.class)
                            .in(PerLookup.class)
                            .id(injectionManager.getRuntimeType() == RuntimeType.CLIENT
                                    ? InjectionIds.COMMON_PROVIDERS_CLIENT.id()
                                    : InjectionIds.COMMON_PROVIDERS_SERVER.id()));
        }
    }

    private final Provider<MessageBodyWorkers> workers;
    private final Provider<ContextResolvers> resolvers;
    private final Provider<ExceptionMappers> mappers;

    @Inject
    public JaxrsProviders(@Context Provider<MessageBodyWorkers> workers,
                          @Context Provider<ContextResolvers> resolvers,
                          @Context Provider<ExceptionMappers> mappers) {
        this.workers = workers;
        this.resolvers = resolvers;
        this.mappers = mappers;
    }

    @Override
    public <T> MessageBodyReader<T> getMessageBodyReader(Class<T> type,
                                                         Type genericType,
                                                         Annotation[] annotations,
                                                         MediaType mediaType) {
        return workers.get().getMessageBodyReader(type, genericType, annotations, mediaType);
    }

    @Override
    public <T> MessageBodyWriter<T> getMessageBodyWriter(Class<T> type,
                                                         Type genericType,
                                                         Annotation[] annotations,
                                                         MediaType mediaType) {
        return workers.get().getMessageBodyWriter(type, genericType, annotations, mediaType);
    }

    @Override
    public <T extends Throwable> ExceptionMapper<T> getExceptionMapper(Class<T> type) {
        // exception mappers are not supported on the client side
        final ExceptionMappers actualMappers = mappers.get();
        return (actualMappers != null) ? actualMappers.find(type) : null;
    }

    @Override
    public <T> ContextResolver<T> getContextResolver(Class<T> contextType, MediaType mediaType) {
        return resolvers.get().resolve(contextType, mediaType);
    }
}
