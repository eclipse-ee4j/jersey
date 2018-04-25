/*
 * Copyright (c) 2014, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.server.internal.process;

import java.util.function.Function;

import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.internal.util.collection.Ref;
import org.glassfish.jersey.internal.util.collection.Refs;
import org.glassfish.jersey.internal.util.collection.Value;
import org.glassfish.jersey.internal.util.collection.Values;
import org.glassfish.jersey.process.internal.ChainableStage;
import org.glassfish.jersey.process.internal.Stage;
import org.glassfish.jersey.server.AsyncContext;
import org.glassfish.jersey.server.CloseableService;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.ContainerResponse;
import org.glassfish.jersey.server.internal.monitoring.RequestEventBuilder;
import org.glassfish.jersey.server.internal.routing.RoutingContext;
import org.glassfish.jersey.server.internal.routing.UriRoutingContext;
import org.glassfish.jersey.server.monitoring.RequestEvent;
import org.glassfish.jersey.server.monitoring.RequestEventListener;

/**
 * Request processing context.
 *
 * Serves as a hub for all request processing related information and is being passed between stages.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
// TODO replace also ContainerResponse in stages with this guy.
public final class RequestProcessingContext implements RespondingContext {

    private final InjectionManager injectionManager;

    private final ContainerRequest request;
    private final UriRoutingContext routingContext;
    private final RespondingContext respondingContext;
    private final CloseableService closeableService;

    private final RequestEventBuilder monitoringEventBuilder;
    private final RequestEventListener monitoringEventListener;

    private final Ref<Value<AsyncContext>> asyncContextValueRef;

    /**
     * Create new request processing context.
     *
     * @param injectionManager        injection manager / injector.
     * @param request                 container request.
     * @param routingContext          routing context.
     * @param monitoringEventBuilder  request monitoring event builder.
     * @param monitoringEventListener registered request monitoring event listener.
     */
    public RequestProcessingContext(
            final InjectionManager injectionManager,
            final ContainerRequest request,
            final UriRoutingContext routingContext,
            final RequestEventBuilder monitoringEventBuilder,
            final RequestEventListener monitoringEventListener) {
        this.injectionManager = injectionManager;

        this.request = request;
        this.routingContext = routingContext;
        this.respondingContext = new DefaultRespondingContext();
        this.closeableService = new DefaultCloseableService();

        this.monitoringEventBuilder = monitoringEventBuilder;
        this.monitoringEventListener = monitoringEventListener;

        this.asyncContextValueRef = Refs.threadSafe(Values.<AsyncContext>empty());
    }

    /**
     * Get the processed container request.
     *
     * @return processed container request.
     */
    public ContainerRequest request() {
        return request;
    }

    /**
     * Get the routing context for the processed container request.
     *
     * @return request routing context.
     */
    public RoutingContext routingContext() {
        return routingContext;
    }

    /**
     * Get the underlying {@link UriRoutingContext} instance for the processed
     * container request.
     * <p>
     * This instance is used  by {@link RequestProcessingConfigurator} to satisfy injection of multiple types, namely:
     * <ul>
     * <li>{@link javax.ws.rs.core.UriInfo}<li>
     * </li>{@link org.glassfish.jersey.server.ExtendedUriInfo}<li>
     * </li>{@link javax.ws.rs.container.ResourceInfo}</li>
     * </ul>
     * </p>
     *
     * @return request routing context.
     */
    UriRoutingContext uriRoutingContext() {
        return routingContext;
    }

    /**
     * Get closeable service associated with the request.
     *
     * @return closeable service associated with the request.
     */
    public CloseableService closeableService() {
        return closeableService;
    }


    /**
     * Lazily initialize {@link AsyncContext} for this
     * request processing context.
     * <p>
     * The {@code lazyContextValue} will be only invoked once during the first call to {@link #asyncContext()}.
     * As such, the asynchronous context for this request can be initialized lazily, on demand.
     * </p>
     *
     * @param lazyContextValue lazily initialized {@code AsyncContext} instance bound to this request processing context.
     */
    // TODO figure out how to make this package-private.
    public void initAsyncContext(Value<AsyncContext> lazyContextValue) {
        asyncContextValueRef.set(Values.lazy(lazyContextValue));
    }

    /**
     * Get the asynchronous context associated with this request processing context.
     *
     * May return {@code null} if no asynchronous context has been initialized in this request processing context yet.
     *
     * @return asynchronous context associated with this request processing context, or {@code null} if the
     * asynchronous context has not been initialized yet
     * (see {@link #initAsyncContext(org.glassfish.jersey.internal.util.collection.Value)}).
     */
    public AsyncContext asyncContext() {
        return asyncContextValueRef.get().get();
    }

    /**
     * Get a {@link Value} instance holding the asynchronous context associated with this request processing context.
     *
     * May return an empty value if no asynchronous context has been initialized in this request processing context yet.
     *
     * @return value instance holding the asynchronous context associated with this request processing context.
     * The returned value may be empty, if no asynchronous context has been initialized yet
     * (see {@link #initAsyncContext(org.glassfish.jersey.internal.util.collection.Value)}).
     */
    public Value<AsyncContext> asyncContextValue() {
        return asyncContextValueRef.get();
    }

    /**
     * Get injection manager.
     *
     * The returned instance is application-scoped.
     *
     * @return application-scoped injection manager.
     */
    public InjectionManager injectionManager() {
        return injectionManager;
    }

    /**
     * Get request monitoring event builder.
     *
     * @return request monitoring event builder.
     */
    // TODO perhaps this method can be completely removed or replaced by setting values on the context directly?
    public RequestEventBuilder monitoringEventBuilder() {
        return monitoringEventBuilder;
    }

    /**
     * Trigger a new monitoring event for the currently processed request.
     *
     * @param eventType request event type.
     */
    public void triggerEvent(RequestEvent.Type eventType) {
        if (monitoringEventListener != null) {
            monitoringEventListener.onEvent(monitoringEventBuilder.build(eventType));
        }
    }

    @Override
    public void push(final Function<ContainerResponse, ContainerResponse> responseTransformation) {
        respondingContext.push(responseTransformation);
    }

    @Override
    public void push(final ChainableStage<ContainerResponse> stage) {
        respondingContext.push(stage);
    }

    @Override
    public Stage<ContainerResponse> createRespondingRoot() {
        return respondingContext.createRespondingRoot();
    }
}
