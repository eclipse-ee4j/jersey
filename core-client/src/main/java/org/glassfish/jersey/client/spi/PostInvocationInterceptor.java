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
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.core.Response;
import java.util.Deque;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

/**
 * The interceptor of a client request invocation that is executed after the request invocation itself, i.e. after the
 * {@link javax.ws.rs.client.ClientResponseFilter ClientResponseFilters} are executed.
 * <p/>
 * It is ensured that all {@code PostInvocationInterceptors} are executed after the request, in the reverse order given by the
 * {@link javax.annotation.Priority}, the higher the priority the later the execution. Any {@code Throwable} thrown when
 * the {@link PostInvocationInterceptor#afterRequest(ClientRequestContext, ClientResponseContext)} or
 * {@link PostInvocationInterceptor#onException(ClientRequestContext, ExceptionContext)} is being processed is accumulated and
 * a multi RuntimeException with other {@link Throwable#addSuppressed(Throwable) exceptions supressed} is being thrown at the end
 * (possibly encapsulated in a {@link javax.ws.rs.ProcessingException} if not a single {@code RuntimeException}),
 * unless resolved by {@link PostInvocationInterceptor#onException(ClientRequestContext, ExceptionContext)}. During the
 * {@link PostInvocationInterceptor} processing, the accumulated {@link Deque} of the {@code Throwables} is available in the
 * {@link ExceptionContext}.
 * <p/>
 * For asynchronous invocation, the {@code PostInvocationInterceptor} is invoked in the request thread, i.e. in the thread
 * provided by {@link javax.ws.rs.client.ClientBuilder#executorService(ExecutorService) ExecutorService}.
 * <p/>
 * When the lowest priority {@code PostInvocationInterceptor} is executed first, one of the two methods can be invoked.
 * {@link PostInvocationInterceptor#afterRequest(ClientRequestContext, ClientResponseContext)} in a usual case when no previous
 * {@code Throwable} was caught, or {@link PostInvocationInterceptor#onException(ClientRequestContext, ExceptionContext)} when
 * the {@code Throwable} was caught. Should the {@link ExceptionContext#resolve(Response)} be utilized in that case,
 * the next {@code PostInvocationInterceptor}'s
 * {@link PostInvocationInterceptor#afterRequest(ClientRequestContext, ClientResponseContext) afterRequest} method will be
 * invoked. Similarly, when a {@code Throwable} is caught during the {@code PostInvocationInterceptor} execution, the next
 * {@code PostInvocationInterceptor}'s
 * {@link PostInvocationInterceptor#onException(ClientRequestContext, ExceptionContext) onException} method will be invoked.
 *
 * @since 2.30
 */
@Beta
@Contract
@ConstrainedTo(RuntimeType.CLIENT)
public interface PostInvocationInterceptor {

    /**
     * The context providing information when the {@code Throwable} (typically, the {@code RuntimeException}) is caught.
     */
    interface ExceptionContext {
        /**
         * If the {@link ClientResponseContext} has been available at the time of the {@code Throwable} occurrence,
         * such as when the {@link PostInvocationInterceptor} is processed, it will be available.
         *
         * @return {@link ClientResponseContext} if available.
         */
        Optional<ClientResponseContext> getResponseContext();

        /**
         * Get the mutable {@link Deque} of unhandled {@code Throwables} occurred during the request (including previous
         * {@code PostInvocationInterceptor} processing).
         *
         * @return Unhandled {@code Throwables} occurred during the request.
         */
        Deque<Throwable> getThrowables();

        /**
         * Resolve the {@code Throwables} with a provided {@link Response}. The Throwables in the {@code ExceptionContext}
         * will be cleared.
         *
         * @param response the provided {@link Response} to be passed to a next {@code PostInvocationInterceptor} or the
         * {@link javax.ws.rs.client.Client}.
         */
        void resolve(Response response);
    }

    /**
     * The method is invoked after a request when no {@code Throwable} is thrown, or the {@code Throwables} are
     * {@link ExceptionContext#resolve(Response) resolved} by previous {@code PostInvocationInterceptor}.
     *
     * @param requestContext the request context.
     * @param responseContext the response context of the original {@link javax.ws.rs.core.Response} or response context
     *                        defined by the new {@link ExceptionContext#resolve(Response) resolving}
     *                        {@link javax.ws.rs.core.Response}.
     */
    void afterRequest(ClientRequestContext requestContext, ClientResponseContext responseContext);

    /**
     * The method is invoked after a {@code Throwable} is caught during the client request chain processing.
     *
     * @param requestContext the request context.
     * @param exceptionContext the context available to handle the caught {@code Throwables}.
     */
    void onException(ClientRequestContext requestContext, ExceptionContext exceptionContext);
}
