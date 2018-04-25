/*
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.server.model.internal;

import java.io.Flushable;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Response;
import javax.ws.rs.sse.SseEventSink;

import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.internal.inject.ConfiguredValidator;
import org.glassfish.jersey.server.model.Invocable;
import org.glassfish.jersey.server.model.Parameter;
import org.glassfish.jersey.server.spi.internal.ParamValueFactoryWithSource;
import org.glassfish.jersey.server.spi.internal.ParameterValueHelper;
import org.glassfish.jersey.server.spi.internal.ResourceMethodDispatcher;
import org.glassfish.jersey.server.spi.internal.ValueParamProvider;

/**
 * An implementation of {@link ResourceMethodDispatcher.Provider} that
 * creates instances of {@link ResourceMethodDispatcher}.
 *
 * @author Paul Sandoz
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
class JavaResourceMethodDispatcherProvider implements ResourceMethodDispatcher.Provider {

    private final Collection<ValueParamProvider> allValueProviders;

    JavaResourceMethodDispatcherProvider(Collection<ValueParamProvider> allValueProviders) {
        this.allValueProviders = allValueProviders;
    }

    @Override
    public ResourceMethodDispatcher create(final Invocable resourceMethod,
            final InvocationHandler invocationHandler,
            final ConfiguredValidator validator) {
        final List<ParamValueFactoryWithSource<?>> valueProviders =
                ParameterValueHelper.createValueProviders(allValueProviders, resourceMethod);
        final Class<?> returnType = resourceMethod.getHandlingMethod().getReturnType();

        ResourceMethodDispatcher resourceMethodDispatcher = null;
        if (Response.class.isAssignableFrom(returnType)) {
            resourceMethodDispatcher =
                    new ResponseOutInvoker(resourceMethod, invocationHandler, valueProviders, validator);
        } else if (returnType != void.class) {
            if (returnType == Object.class || GenericEntity.class.isAssignableFrom(returnType)) {
                resourceMethodDispatcher =
                        new ObjectOutInvoker(resourceMethod, invocationHandler, valueProviders, validator);
            } else {
                resourceMethodDispatcher =
                        new TypeOutInvoker(resourceMethod, invocationHandler, valueProviders, validator);
            }
        } else {
            // return type is void
            int i = 0;
            for (final Parameter parameter : resourceMethod.getParameters()) {
                if (SseEventSink.class.equals(parameter.getRawType())) {
                    resourceMethodDispatcher =
                            new SseEventSinkInvoker(resourceMethod, invocationHandler, valueProviders, validator, i);
                    break;
                }
                i++;
            }

            if (resourceMethodDispatcher == null) {
                resourceMethodDispatcher = new VoidOutInvoker(resourceMethod, invocationHandler, valueProviders, validator);
            }
        }

        return resourceMethodDispatcher;
    }

    private abstract static class AbstractMethodParamInvoker extends AbstractJavaResourceMethodDispatcher {

        private final List<ParamValueFactoryWithSource<?>> valueProviders;

        AbstractMethodParamInvoker(
                final Invocable resourceMethod,
                final InvocationHandler handler,
                final List<ParamValueFactoryWithSource<?>> valueProviders,
                final ConfiguredValidator validator) {
            super(resourceMethod, handler, validator);
            this.valueProviders = valueProviders;
        }

        final Object[] getParamValues(ContainerRequest request) {
            return ParameterValueHelper.getParameterValues(valueProviders, request);
        }
    }

    private static final class SseEventSinkInvoker extends AbstractMethodParamInvoker {

        private final int parameterIndex;

        SseEventSinkInvoker(
                final Invocable resourceMethod,
                final InvocationHandler handler,
                final List<ParamValueFactoryWithSource<?>> valueProviders,
                final ConfiguredValidator validator,
                final int parameterIndex) {
            super(resourceMethod, handler, valueProviders, validator);
            this.parameterIndex = parameterIndex;
        }

        @Override
        protected Response doDispatch(final Object resource, final ContainerRequest request) throws ProcessingException {
            final Object[] paramValues = getParamValues(request);
            invoke(request, resource, paramValues);

            final SseEventSink eventSink = (SseEventSink) paramValues[parameterIndex];

            if (eventSink == null) {
                throw new IllegalArgumentException("SseEventSink parameter detected, but not found.");
            } else if (eventSink instanceof Flushable) {
                try {
                    ((Flushable) eventSink).flush();
                } catch (IOException e) {
                    // ignore.
                }
            }
            return Response.ok().entity(eventSink).build();
        }
    }

    private static final class VoidOutInvoker extends AbstractMethodParamInvoker {

        VoidOutInvoker(
                final Invocable resourceMethod,
                final InvocationHandler handler,
                final List<ParamValueFactoryWithSource<?>> valueProviders,
                final ConfiguredValidator validator) {
            super(resourceMethod, handler, valueProviders, validator);
        }

        @Override
        protected Response doDispatch(final Object resource, final ContainerRequest containerRequest) throws ProcessingException {
            invoke(containerRequest, resource, getParamValues(containerRequest));
            return Response.noContent().build();
        }
    }

    private static final class ResponseOutInvoker extends AbstractMethodParamInvoker {

        ResponseOutInvoker(
                final Invocable resourceMethod,
                final InvocationHandler handler,
                final List<ParamValueFactoryWithSource<?>> valueProviders,
                final ConfiguredValidator validator) {
            super(resourceMethod, handler, valueProviders, validator);
        }

        @Override
        protected Response doDispatch(Object resource, final ContainerRequest containerRequest) throws ProcessingException {
            return Response.class.cast(invoke(containerRequest, resource, getParamValues(containerRequest)));
        }
    }

    private static final class ObjectOutInvoker extends AbstractMethodParamInvoker {

        ObjectOutInvoker(
                final Invocable resourceMethod,
                final InvocationHandler handler,
                final List<ParamValueFactoryWithSource<?>> valueProviders,
                final ConfiguredValidator validator) {
            super(resourceMethod, handler, valueProviders, validator);
        }

        @Override
        protected Response doDispatch(final Object resource, final ContainerRequest containerRequest) throws ProcessingException {
            final Object o = invoke(containerRequest, resource, getParamValues(containerRequest));

            if (o instanceof Response) {
                return Response.class.cast(o);
            } else if (o != null) {
                return Response.ok().entity(o).build();
            } else {
                return Response.noContent().build();
            }
        }
    }

    private static final class TypeOutInvoker extends AbstractMethodParamInvoker {

        private final Type t;

        TypeOutInvoker(
                final Invocable resourceMethod,
                final InvocationHandler handler,
                final List<ParamValueFactoryWithSource<?>> valueProviders,
                final ConfiguredValidator validator) {
            super(resourceMethod, handler, valueProviders, validator);
            this.t = resourceMethod.getHandlingMethod().getGenericReturnType();
        }

        @Override
        protected Response doDispatch(final Object resource, final ContainerRequest containerRequest) throws ProcessingException {
            final Object o = invoke(containerRequest, resource, getParamValues(containerRequest));
            if (o != null) {
                if (o instanceof Response) {
                    return Response.class.cast(o);
                }
                return Response.ok().entity(o).build();
            } else {
                return Response.noContent().build();
            }
        }
    }
}
