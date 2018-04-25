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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.security.PrivilegedAction;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import javax.validation.ValidationException;

import org.glassfish.jersey.message.internal.TracingLogger;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.SubjectSecurityContext;
import org.glassfish.jersey.server.internal.LocalizationMessages;
import org.glassfish.jersey.server.internal.ServerTraceEvent;
import org.glassfish.jersey.server.internal.inject.ConfiguredValidator;
import org.glassfish.jersey.server.internal.process.MappableException;
import org.glassfish.jersey.server.model.Invocable;
import org.glassfish.jersey.server.spi.internal.ResourceMethodDispatcher;

/**
 * Abstract resource method dispatcher that provides skeleton implementation of
 * dispatching requests to a particular {@link Method Java method} using supplied
 * {@link InvocationHandler Java method invocation handler}.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
abstract class AbstractJavaResourceMethodDispatcher implements ResourceMethodDispatcher {

    private final Method method;
    private final InvocationHandler methodHandler;
    private final Invocable resourceMethod;
    private final ConfiguredValidator validator;

    /**
     * Initialize common java resource method dispatcher structures.
     *
     * @param resourceMethod invocable resource class Java method.
     * @param methodHandler  method invocation handler.
     * @param validator      input/output parameter validator.
     */
    AbstractJavaResourceMethodDispatcher(final Invocable resourceMethod,
                                         final InvocationHandler methodHandler,
                                         final ConfiguredValidator validator) {
        this.method = resourceMethod.getDefinitionMethod();
        this.methodHandler = methodHandler;
        this.resourceMethod = resourceMethod;
        this.validator = validator;
    }

    @Override
    public final Response dispatch(Object resource, ContainerRequest request) throws ProcessingException {
        Response response = null;
        try {
            response = doDispatch(resource, request);
        } finally {
            TracingLogger.getInstance(request).log(ServerTraceEvent.DISPATCH_RESPONSE, response);
        }
        return response;
    }

    /**
     * Dispatching functionality to be implemented by a concrete dispatcher
     * implementation sub-class.
     *
     * @param resource resource class instance.
     * @param request  request to be dispatched.
     * @return response for the dispatched request.
     * @throws ProcessingException in case of a processing error.
     * @see ResourceMethodDispatcher#dispatch(Object, org.glassfish.jersey.server.ContainerRequest)
     */
    protected abstract Response doDispatch(Object resource, ContainerRequest request) throws ProcessingException;

    /**
     * Use the underlying invocation handler to invoke the underlying Java method
     * with the supplied input method argument values on a given resource instance.
     *
     * @param containerRequest container request.
     * @param resource         resource class instance.
     * @param args             input argument values for the invoked Java method.
     * @return invocation result.
     * @throws ProcessingException (possibly {@link MappableException mappable})
     *                             container exception in case the invocation failed.
     */
    final Object invoke(final ContainerRequest containerRequest, final Object resource, final Object... args)
            throws ProcessingException {
        try {
            // Validate resource class & method input parameters.
            if (validator != null) {
                validator.validateResourceAndInputParams(resource, resourceMethod, args);
            }

            final PrivilegedAction invokeMethodAction = new PrivilegedAction() {
                @Override
                public Object run() {
                    final TracingLogger tracingLogger = TracingLogger.getInstance(containerRequest);
                    final long timestamp = tracingLogger.timestamp(ServerTraceEvent.METHOD_INVOKE);
                    try {

                        Object result = methodHandler.invoke(resource, method, args);

                        // if a response is a CompletionStage and is done, we don't need to suspend and resume
                        if (result instanceof CompletionStage) {
                            CompletableFuture resultFuture;
                            try {
                                resultFuture = ((CompletionStage) result).toCompletableFuture();
                            } catch (UnsupportedOperationException e) {
                                // CompletionStage is not required to implement "toCompletableFuture". If it doesn't
                                // we treat it as "uncompleted" future.
                                return result;
                            }

                            if (resultFuture != null && resultFuture.isDone()) {
                                if (resultFuture.isCancelled()) {
                                    return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
                                } else {
                                    try {
                                        return resultFuture.get();
                                    } catch (ExecutionException e) {
                                        throw new InvocationTargetException(e.getCause());
                                    }
                                }
                            }
                        }

                        return result;

                    } catch (IllegalAccessException | IllegalArgumentException | UndeclaredThrowableException ex) {
                        throw new ProcessingException(LocalizationMessages.ERROR_RESOURCE_JAVA_METHOD_INVOCATION(), ex);
                    } catch (InvocationTargetException ex) {
                        throw mapTargetToRuntimeEx(ex.getCause());
                    } catch (Throwable t) {
                        throw new ProcessingException(t);
                    } finally {
                        tracingLogger.logDuration(ServerTraceEvent.METHOD_INVOKE, timestamp, resource, method);
                    }
                }
            };

            final SecurityContext securityContext = containerRequest.getSecurityContext();

            final Object invocationResult = (securityContext instanceof SubjectSecurityContext)
                    ? ((SubjectSecurityContext) securityContext).doAsSubject(invokeMethodAction) : invokeMethodAction.run();

            // Validate response entity.
            if (validator != null) {
                validator.validateResult(resource, resourceMethod, invocationResult);
            }

            return invocationResult;
        } catch (ValidationException ex) { // handle validation exceptions -> potentially mappable
            throw new MappableException(ex);
        }
    }

    private static RuntimeException mapTargetToRuntimeEx(Throwable throwable) {
        if (throwable instanceof WebApplicationException) {
            return (WebApplicationException) throwable;
        }
        // handle all exceptions as potentially mappable (incl. ProcessingException)
        return new MappableException(throwable);
    }

    @Override
    public String toString() {
        return method.toString();
    }

}
