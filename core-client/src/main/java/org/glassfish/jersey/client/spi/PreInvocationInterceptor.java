/*
 * Copyright (c) 2019 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.client.spi;

import org.glassfish.jersey.Beta;
import org.glassfish.jersey.spi.Contract;

import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.RuntimeType;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.core.Response;
import java.util.concurrent.ExecutorService;

/**
 * The interceptor of a client request invocation that is executed before the invocation itself, i.e. before the
 * {@link javax.ws.rs.client.ClientRequestFilter} is invoked.
 * <p/>
 * It is ensured that all {@code PreInvocationInterceptors} are executed before the request, in the order given by the
 * {@link javax.annotation.Priority}, the higher the priority the sooner the execution. Any {@code RuntimeException} thrown when
 * the {@link PreInvocationInterceptor#beforeRequest(ClientRequestContext)} is being processed is accumulated and
 * a multi RuntimeException with other {@link Throwable#addSuppressed(Throwable) exceptions supressed} is being thrown.
 * <p/>
 * For asynchronous invocation, the {@code PreInvocationInterceptor} is invoked in the main thread, i.e. not in the thread
 * provided by {@link javax.ws.rs.client.ClientBuilder#executorService(ExecutorService) ExecutorService}. For reactive
 * invocations, this depends on the provided {@link javax.ws.rs.client.RxInvoker}. For the default Jersey asynchronous
 * {@link org.glassfish.jersey.client.JerseyCompletionStageRxInvoker}, {@code PreInvocationInterceptor} is invoked in the
 * main thread, too.
 * <p/>
 * Should the {@link ClientRequestContext#abortWith(Response)} be utilized, the request abort is performed  after every
 * registered {@code PreInvocationInterceptor} is processed. If multiple
 * {@code PreInvocationInterceptors PreInvocationInterceptor} tries to utilize {@link ClientRequestContext#abortWith(Response)}
 * method, the second and every next throws {@code IllegalStateException}.
 *
 * @since 2.30
 */
@Beta
@Contract
@ConstrainedTo(RuntimeType.CLIENT)
public interface PreInvocationInterceptor {

    /**
     * The method invoked before the request starts.
     * @param requestContext the request context shared with {@link javax.ws.rs.client.ClientRequestFilter}.
     */
    void beforeRequest(ClientRequestContext requestContext);
}
