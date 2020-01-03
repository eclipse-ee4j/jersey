/*
 * Copyright (c) 2012, 2019 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.client;

import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;

import javax.inject.Provider;

import org.glassfish.jersey.client.internal.LocalizationMessages;
import org.glassfish.jersey.client.spi.AsyncConnectorCallback;
import org.glassfish.jersey.client.spi.Connector;
import org.glassfish.jersey.internal.BootstrapBag;
import org.glassfish.jersey.internal.Version;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.internal.inject.Providers;
import org.glassfish.jersey.internal.util.collection.LazyValue;
import org.glassfish.jersey.internal.util.collection.Ref;
import org.glassfish.jersey.internal.util.collection.Value;
import org.glassfish.jersey.internal.util.collection.Values;
import org.glassfish.jersey.message.MessageBodyWorkers;
import org.glassfish.jersey.model.internal.ManagedObjectsFinalizer;
import org.glassfish.jersey.process.internal.ChainableStage;
import org.glassfish.jersey.process.internal.RequestContext;
import org.glassfish.jersey.process.internal.RequestScope;
import org.glassfish.jersey.process.internal.Stage;
import org.glassfish.jersey.process.internal.Stages;

/**
 * Client-side request processing runtime.
 *
 * @author Marek Potociar
 */
class ClientRuntime implements JerseyClient.ShutdownHook, ClientExecutor {

    private static final Logger LOG = Logger.getLogger(ClientRuntime.class.getName());

    private final Stage<ClientRequest> requestProcessingRoot;
    private final Stage<ClientResponse> responseProcessingRoot;

    private final Connector connector;
    private final ClientConfig config;

    private final RequestScope requestScope;
    private final LazyValue<ExecutorService> asyncRequestExecutor;
    private final LazyValue<ScheduledExecutorService> backgroundScheduler;

    private final Iterable<ClientLifecycleListener> lifecycleListeners;

    private final AtomicBoolean closed = new AtomicBoolean(false);
    private final ManagedObjectsFinalizer managedObjectsFinalizer;
    private final InjectionManager injectionManager;

    private final InvocationInterceptorStages.PreInvocationInterceptorStage preInvocationInterceptorStage;
    private final InvocationInterceptorStages.PostInvocationInterceptorStage postInvocationInterceptorStage;

    /**
     * Create new client request processing runtime.
     *
     * @param config           client runtime configuration.
     * @param connector        client transport connector.
     * @param injectionManager injection manager.
     */
    public ClientRuntime(final ClientConfig config, final Connector connector, final InjectionManager injectionManager,
            final BootstrapBag bootstrapBag) {
        Provider<Ref<ClientRequest>> clientRequest =
                () -> injectionManager.getInstance(new GenericType<Ref<ClientRequest>>() {}.getType());

        RequestProcessingInitializationStage requestProcessingInitializationStage =
                new RequestProcessingInitializationStage(clientRequest, bootstrapBag.getMessageBodyWorkers(), injectionManager);

        Stage.Builder<ClientRequest> requestingChainBuilder = Stages.chain(requestProcessingInitializationStage);

        preInvocationInterceptorStage = InvocationInterceptorStages.createPreInvocationInterceptorStage(injectionManager);
        postInvocationInterceptorStage = InvocationInterceptorStages.createPostInvocationInterceptorStage(injectionManager);

        ChainableStage<ClientRequest> requestFilteringStage = preInvocationInterceptorStage.hasPreInvocationInterceptors()
                ? ClientFilteringStages.createRequestFilteringStage(
                        preInvocationInterceptorStage.createPreInvocationInterceptorFilter(), injectionManager)
                : ClientFilteringStages.createRequestFilteringStage(injectionManager);

        this.requestProcessingRoot = requestFilteringStage != null
                ? requestingChainBuilder.build(requestFilteringStage) : requestingChainBuilder.build();

        ChainableStage<ClientResponse> responseFilteringStage = ClientFilteringStages.createResponseFilteringStage(
                injectionManager);
        this.responseProcessingRoot = responseFilteringStage != null ? responseFilteringStage : Stages.identity();
        this.managedObjectsFinalizer = bootstrapBag.getManagedObjectsFinalizer();
        this.config = config;
        this.connector = connector;
        this.requestScope = bootstrapBag.getRequestScope();
        this.asyncRequestExecutor = Values.lazy((Value<ExecutorService>) () ->
                config.getExecutorService() == null
                        ? injectionManager.getInstance(ExecutorService.class, ClientAsyncExecutorLiteral.INSTANCE)
                        : config.getExecutorService());
        this.backgroundScheduler = Values.lazy((Value<ScheduledExecutorService>) () ->
                config.getScheduledExecutorService() == null
                        ? injectionManager.getInstance(ScheduledExecutorService.class, ClientBackgroundSchedulerLiteral.INSTANCE)
                        : config.getScheduledExecutorService());

        this.injectionManager = injectionManager;
        this.lifecycleListeners = Providers.getAllProviders(injectionManager, ClientLifecycleListener.class);

        for (final ClientLifecycleListener listener : lifecycleListeners) {
            try {
                listener.onInit();
            } catch (final Throwable t) {
                LOG.log(Level.WARNING, LocalizationMessages.ERROR_LISTENER_INIT(listener.getClass().getName()), t);
            }
        }
    }

    /**
     * Prepare a {@code Runnable} to be used to submit a {@link ClientRequest client request} for asynchronous processing.
     * <p>
     *
     * @param request  client request to be sent.
     * @param callback asynchronous response callback.
     * @return {@code Runnable} to be submitted for async processing using {@link #submit(Runnable)}.
     */
    Runnable createRunnableForAsyncProcessing(ClientRequest request, final ResponseCallback callback) {
        try {
            requestScope.runInScope(() -> preInvocationInterceptorStage.beforeRequest(request));
        } catch (Throwable throwable) {
            return () -> requestScope.runInScope(() -> processFailure(request, throwable, callback));
        }

        return () -> requestScope.runInScope(() -> {
            RuntimeException runtimeException = null;
            try {
                ClientRequest processedRequest;

                try {
                    processedRequest = Stages.process(request, requestProcessingRoot);
                    processedRequest = addUserAgent(processedRequest, connector.getName());
                } catch (final AbortException aborted) {
                    processResponse(request, aborted.getAbortResponse(), callback);
                    return;
                }

                final AsyncConnectorCallback connectorCallback = new AsyncConnectorCallback() {

                    @Override
                    public void response(final ClientResponse response) {
                        requestScope.runInScope(() -> processResponse(request, response, callback));
                    }

                    @Override
                    public void failure(final Throwable failure) {
                        requestScope.runInScope(() -> processFailure(request, failure, callback));
                    }
                };

                connector.apply(processedRequest, connectorCallback);
            } catch (final Throwable throwable) {
                processFailure(request, throwable, callback);
            }
        });
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        return asyncRequestExecutor.get().submit(task);
    }

    @Override
    public Future<?> submit(Runnable task) {
        return asyncRequestExecutor.get().submit(task);
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        return asyncRequestExecutor.get().submit(task, result);
    }

    @Override
    public <T> ScheduledFuture<T> schedule(Callable<T> callable, long delay, TimeUnit unit) {
        return backgroundScheduler.get().schedule(callable, delay, unit);
    }

    @Override
    public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        return backgroundScheduler.get().schedule(command, delay, unit);
    }

    private void processResponse(final ClientRequest request, final ClientResponse response, final ResponseCallback callback) {
        ClientResponse processedResponse = null;
        Throwable caught = null;
        try {
            processedResponse = Stages.process(response, responseProcessingRoot);
        } catch (final Throwable throwable) {
            caught = throwable;
        }

        try {
            processedResponse = postInvocationInterceptorStage.afterRequest(request, processedResponse, caught);
        } catch (Throwable throwable) {
            processFailure(throwable, callback);
            return;
        }
        callback.completed(processedResponse, requestScope);
    }

    private void processFailure(final ClientRequest request, final Throwable failure, final ResponseCallback callback) {
        if (postInvocationInterceptorStage.hasPostInvocationInterceptor()) {
            try {
                final ClientResponse clientResponse = postInvocationInterceptorStage.afterRequest(request, null, failure);
                callback.completed(clientResponse, requestScope);
            } catch (RuntimeException e) {
                final Throwable t = e.getSuppressed().length == 1 && e.getSuppressed()[0] == failure ? failure : e;
                processFailure(t, callback);
            }
        } else {
            processFailure(failure, callback);
        }
    }

    private void processFailure(final Throwable failure, final ResponseCallback callback) {
        callback.failed(failure instanceof ProcessingException
                ? (ProcessingException) failure : new ProcessingException(failure));
    }

    private Future<?> submit(final ExecutorService executor, final Runnable task) {
        return executor.submit(() -> requestScope.runInScope(task));
    }

    private ClientRequest addUserAgent(final ClientRequest clientRequest, final String connectorName) {
        final MultivaluedMap<String, Object> headers = clientRequest.getHeaders();

        if (headers.containsKey(HttpHeaders.USER_AGENT)) {
            // Check for explicitly set null value and if set, then remove the header - see JERSEY-2189
            if (clientRequest.getHeaderString(HttpHeaders.USER_AGENT) == null) {
                headers.remove(HttpHeaders.USER_AGENT);
            }
        } else if (!clientRequest.ignoreUserAgent()) {
            if (connectorName != null && !connectorName.isEmpty()) {
                headers.put(HttpHeaders.USER_AGENT,
                        Collections.singletonList(String.format("Jersey/%s (%s)", Version.getVersion(), connectorName)));
            } else {
                headers.put(HttpHeaders.USER_AGENT,
                        Collections.singletonList(String.format("Jersey/%s", Version.getVersion())));
            }
        }

        return clientRequest;
    }

    /**
     * Invoke a request processing synchronously in the context of the caller's thread.
     * <p>
     * NOTE: the method does not explicitly start a new request scope context. Instead
     * it is assumed that the method is invoked from within a context of a proper, running
     * {@link RequestContext request context}. A caller may use the
     * {@link #getRequestScope()} method to retrieve the request scope instance and use it to
     * initialize the proper request scope context prior the method invocation.
     * </p>
     *
     * @param request client request to be invoked.
     * @return client response.
     * @throws javax.ws.rs.ProcessingException in case of an invocation failure.
     */
    public ClientResponse invoke(final ClientRequest request) {
        ProcessingException processingException = null;
        ClientResponse response = null;
        try {
            preInvocationInterceptorStage.beforeRequest(request);

            try {
                response = connector.apply(addUserAgent(Stages.process(request, requestProcessingRoot), connector.getName()));
            } catch (final AbortException aborted) {
                response = aborted.getAbortResponse();
            }

            response = Stages.process(response, responseProcessingRoot);
        } catch (final ProcessingException pe) {
            processingException = pe;
        } catch (final Throwable t) {
            processingException = new ProcessingException(t.getMessage(), t);
        } finally {
            response = postInvocationInterceptorStage.afterRequest(request, response, processingException);
            return response;
        }
    }

    /**
     * Get the request scope instance configured for the runtime.
     *
     * @return request scope instance.
     */
    public RequestScope getRequestScope() {
        return requestScope;
    }

    /**
     * Get runtime configuration.
     *
     * @return runtime configuration.
     */
    public ClientConfig getConfig() {
        return config;
    }

    /**
     * This will be used as the last resort to clean things up
     * in the case that this instance gets garbage collected
     * before the client itself gets released.
     * <p>
     * Close will be invoked either via finalizer
     * or via JerseyClient onShutdown hook, whatever comes first.
     */
    @Override
    protected void finalize() throws Throwable {
        try {
            close();
        } finally {
            super.finalize();
        }
    }

    @Override
    public void onShutdown() {
        close();
    }

    private void close() {
        if (closed.compareAndSet(false, true)) {
            try {
                for (final ClientLifecycleListener listener : lifecycleListeners) {
                    try {
                        listener.onClose();
                    } catch (final Throwable t) {
                        LOG.log(Level.WARNING, LocalizationMessages.ERROR_LISTENER_CLOSE(listener.getClass().getName()), t);
                    }
                }
            } finally {
                try {
                    connector.close();
                } finally {
                    managedObjectsFinalizer.preDestroy();
                    injectionManager.shutdown();
                }
            }
        }
    }

    /**
     * Pre-initialize the client runtime.
     */
    public void preInitialize() {
        // pre-initialize MessageBodyWorkers
        injectionManager.getInstance(MessageBodyWorkers.class);
    }

    /**
     * Runtime connector.
     *
     * @return runtime connector.
     */
    public Connector getConnector() {
        return connector;
    }

    /**
     * Get injection manager.
     *
     * @return injection manager.
     */
    InjectionManager getInjectionManager() {
        return injectionManager;
    }
}
