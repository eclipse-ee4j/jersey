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

package org.glassfish.jersey.server.spi.internal;

import java.lang.reflect.InvocationHandler;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.internal.inject.Binder;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.internal.inject.ConfiguredValidator;
import org.glassfish.jersey.server.internal.process.MappableException;
import org.glassfish.jersey.server.model.Invocable;

/**
 * A resource method dispatcher responsible for consuming a JAX-RS {@link Request request}
 * by invoking the configured {@link Invocable resource method} on a given
 * resource instance and returning the method invocation result in a form of a
 * JAX-RS {@link Response response}.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
public interface ResourceMethodDispatcher {

    /**
     * Provider interface for creating a {@link ResourceMethodDispatcher resource
     * method dispatcher} instances.
     *
     * A provider examines the model of the Web resource method and
     * determines if an invoker can be created for that Web resource method.
     * <p>
     * Multiple providers can specify the support for different Web resource method
     * patterns, ranging from simple patterns (such as void return and input
     * parameters) to complex patterns that take type URI and query arguments
     * and HTTP request headers as typed parameters.
     * </p>
     * <p>
     * Resource method dispatcher provider implementations can be registered in Jersey application
     * by supplying a custom HK2 {@link Binder} that binds the
     * custom service implementation(s) to the {@code ResourceMethodDispatcher.Provider} contract.
     * </p>
     *
     * @author Paul Sandoz
     * @author Marek Potociar (marek.potociar at oracle.com)
     */
    public static interface Provider {

        /**
         * Create a {@link ResourceMethodDispatcher resource method dispatcher} for
         * a given {@link Invocable invocable resource method}.
         * <p/>
         * If the provider supports the invocable resource method, it will
         * return a new non-null dispatcher instance configured to invoke the supplied
         * invocable resource method via the provided {@link InvocationHandler
         * invocation handler} whenever the
         * {@link #dispatch(Object, org.glassfish.jersey.server.ContainerRequest) dispatch(...)}
         * method is called on that dispatcher instance.
         *
         * @param method  the invocable resource method.
         * @param handler invocation handler to be used for the resource method invocation.
         * @param validator configured validator to be used for validation during resource method invocation
         * @return the resource method dispatcher, or {@code null} if it could not be
         *         created for the given resource method.
         */
        public ResourceMethodDispatcher create(final Invocable method,
                                               final InvocationHandler handler,
                                               final ConfiguredValidator validator);
    }

    /**
     * Reflectively dispatch a request to the underlying {@link Invocable
     * invocable resource method} via the configured {@link InvocationHandler
     * invocation handler} using the provided resource class instance.
     * <p />
     * In summary, the main job of the dispatcher is to convert a request into
     * an array of the Java method input parameters and subsequently convert the
     * returned response of an arbitrary Java type to a JAX-RS {@link Response response}
     * instance.
     * <p />
     * When the method is invoked, the dispatcher will extract the
     * {@link java.lang.reflect.Method Java method} information from the invocable
     * resource method and use the information to retrieve the required input
     * parameters from either the request instance or any other available run-time
     * information. Once the set of input parameter values is computed, the underlying
     * invocation handler instance is invoked to process (invoke) the Java resource
     * method with the computed input parameter values. The returned response is
     * subsequently converted into a JAX-RS {@code Response} type and returned
     * from the dispatcher.
     * <p />
     * It is assumed that the supplied resource implements the invocable method.
     * Dispatcher implementation should not need to do any additional checks in
     * that respect.
     *
     * @param resource the resource class instance.
     * @param request  request to be dispatched.
     * @return {@link Response response} for the dispatched request.
     * @throws ProcessingException (possibly {@link MappableException mappable})
     *                             container exception that will be handled by the Jersey server container.
     */
    public Response dispatch(final Object resource, final ContainerRequest request) throws ProcessingException;
}
