/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.simple;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.SecurityContext;

import javax.inject.Inject;
import javax.inject.Provider;

import org.glassfish.jersey.internal.MapPropertiesDelegate;
import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.internal.inject.ReferencingFactory;
import org.glassfish.jersey.internal.util.ExtendedLogger;
import org.glassfish.jersey.internal.util.collection.Ref;
import org.glassfish.jersey.process.internal.RequestScoped;
import org.glassfish.jersey.server.ApplicationHandler;
import org.glassfish.jersey.server.ContainerException;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.ContainerResponse;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.internal.ContainerUtils;
import org.glassfish.jersey.server.spi.Container;
import org.glassfish.jersey.server.spi.ContainerResponseWriter;
import org.glassfish.jersey.server.spi.ContainerResponseWriter.TimeoutHandler;

import org.simpleframework.common.thread.DaemonFactory;
import org.simpleframework.http.Address;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.Status;

/**
 * Jersey {@code Container} implementation based on Simple framework
 * {@link org.simpleframework.http.core.Container}.
 *
 * @author Arul Dhesiaseelan (aruld@acm.org)
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public final class SimpleContainer implements org.simpleframework.http.core.Container, Container {

    private static final ExtendedLogger logger =
            new ExtendedLogger(Logger.getLogger(SimpleContainer.class.getName()), Level.FINEST);

    private final Type RequestTYPE = (new GenericType<Ref<Request>>() { }).getType();
    private final Type ResponseTYPE = (new GenericType<Ref<Response>>() { }).getType();

    /**
     * Referencing factory for Simple request.
     */
    private static class SimpleRequestReferencingFactory extends ReferencingFactory<Request> {

        @Inject
        public SimpleRequestReferencingFactory(final Provider<Ref<Request>> referenceFactory) {
            super(referenceFactory);
        }
    }

    /**
     * Referencing factory for Simple response.
     */
    private static class SimpleResponseReferencingFactory extends ReferencingFactory<Response> {

        @Inject
        public SimpleResponseReferencingFactory(final Provider<Ref<Response>> referenceFactory) {
            super(referenceFactory);
        }
    }

    /**
     * An internal binder to enable Simple HTTP container specific types injection. This binder allows
     * to inject underlying Grizzly HTTP request and response instances.
     */
    private static class SimpleBinder extends AbstractBinder {

        @Override
        protected void configure() {
            bindFactory(SimpleRequestReferencingFactory.class).to(Request.class).proxy(true)
                                                              .proxyForSameScope(false).in(RequestScoped.class);
            bindFactory(ReferencingFactory.<Request>referenceFactory())
                    .to(new GenericType<Ref<Request>>() {
                    }).in(RequestScoped.class);

            bindFactory(SimpleResponseReferencingFactory.class).to(Response.class).proxy(true)
                                                               .proxyForSameScope(false).in(RequestScoped.class);
            bindFactory(ReferencingFactory.<Response>referenceFactory())
                    .to(new GenericType<Ref<Response>>() {
                    }).in(RequestScoped.class);
        }
    }

    private volatile ScheduledExecutorService scheduler;
    private volatile ApplicationHandler appHandler;

    private static final class ResponseWriter implements ContainerResponseWriter {

        private final AtomicReference<TimeoutTimer> reference;
        private final ScheduledExecutorService scheduler;
        private final Response response;

        ResponseWriter(final Response response, final ScheduledExecutorService scheduler) {
            this.reference = new AtomicReference<TimeoutTimer>();
            this.response = response;
            this.scheduler = scheduler;
        }

        @Override
        public OutputStream writeResponseStatusAndHeaders(final long contentLength,
                                                          final ContainerResponse context) throws ContainerException {
            final javax.ws.rs.core.Response.StatusType statusInfo = context.getStatusInfo();

            final int code = statusInfo.getStatusCode();
            final String reason = statusInfo.getReasonPhrase() == null
                    ? Status.getDescription(code)
                    : statusInfo.getReasonPhrase();
            response.setCode(code);
            response.setDescription(reason);

            if (contentLength != -1) {
                response.setContentLength(contentLength);
            }
            for (final Map.Entry<String, List<String>> e : context.getStringHeaders().entrySet()) {
                for (final String value : e.getValue()) {
                    response.addValue(e.getKey(), value);
                }
            }

            try {
                return response.getOutputStream();
            } catch (final IOException ioe) {
                throw new ContainerException("Error during writing out the response headers.", ioe);
            }
        }

        @Override
        public boolean suspend(final long timeOut, final TimeUnit timeUnit,
                               final TimeoutHandler timeoutHandler) {
            try {
                TimeoutTimer timer = reference.get();

                if (timer == null) {
                    TimeoutDispatcher task = new TimeoutDispatcher(this, timeoutHandler);
                    ScheduledFuture<?> future =
                            scheduler.schedule(task, timeOut == 0 ? Integer.MAX_VALUE : timeOut,
                                    timeOut == 0 ? TimeUnit.SECONDS : timeUnit);
                    timer = new TimeoutTimer(scheduler, future, task);
                    reference.set(timer);
                    return true;
                }
                return false;
            } catch (final IllegalStateException ex) {
                return false;
            } finally {
                logger.debugLog("suspend(...) called");
            }
        }

        @Override
        public void setSuspendTimeout(final long timeOut, final TimeUnit timeUnit)
                throws IllegalStateException {
            try {
                TimeoutTimer timer = reference.get();

                if (timer == null) {
                    throw new IllegalStateException("Response has not been suspended");
                }
                timer.reschedule(timeOut, timeUnit);
            } finally {
                logger.debugLog("setTimeout(...) called");
            }
        }

        @Override
        public void commit() {
            try {
                response.close();
            } catch (final IOException e) {
                logger.log(Level.SEVERE, "Unable to send 500 error response.", e);
            } finally {
                logger.debugLog("commit() called");
            }
        }

        public boolean isSuspended() {
            return reference.get() != null;
        }

        @Override
        public void failure(final Throwable error) {
            try {
                if (!response.isCommitted()) {
                    response.setCode(javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
                    response.setDescription(error.getMessage());
                }
            } finally {
                logger.debugLog("failure(...) called");
                commit();
                rethrow(error);
            }

        }

        @Override
        public boolean enableResponseBuffering() {
            return false;
        }

        /**
         * Rethrow the original exception as required by JAX-RS, 3.3.4
         *
         * @param error throwable to be re-thrown
         */
        private void rethrow(final Throwable error) {
            if (error instanceof RuntimeException) {
                throw (RuntimeException) error;
            } else {
                throw new ContainerException(error);
            }
        }

    }

    private static final class TimeoutTimer {

        private final AtomicReference<ScheduledFuture<?>> reference;
        private final ScheduledExecutorService service;
        private final TimeoutDispatcher task;

        public TimeoutTimer(ScheduledExecutorService service, ScheduledFuture<?> future,
                            TimeoutDispatcher task) {
            this.reference = new AtomicReference<ScheduledFuture<?>>();
            this.service = service;
            this.task = task;
        }

        public void reschedule(long timeOut, TimeUnit timeUnit) {
            ScheduledFuture<?> future = reference.getAndSet(null);

            if (future != null) {
                if (future.cancel(false)) {
                    future = service.schedule(task, timeOut == 0 ? Integer.MAX_VALUE : timeOut,
                            timeOut == 0 ? TimeUnit.SECONDS : timeUnit);
                    reference.set(future);
                }
            } else {
                future = service.schedule(task, timeOut == 0 ? Integer.MAX_VALUE : timeOut,
                        timeOut == 0 ? TimeUnit.SECONDS : timeUnit);
                reference.set(future);
            }
        }
    }

    private static final class TimeoutDispatcher implements Runnable {

        private final ResponseWriter writer;
        private final TimeoutHandler handler;

        public TimeoutDispatcher(ResponseWriter writer, TimeoutHandler handler) {
            this.writer = writer;
            this.handler = handler;
        }

        public void run() {
            try {
                handler.onTimeout(writer);
            } catch (Exception e) {
                logger.log(Level.INFO, "Failed to call timeout handler", e);
            }
        }
    }

    @Override
    public void handle(final Request request, final Response response) {
        final ResponseWriter responseWriter = new ResponseWriter(response, scheduler);
        final URI baseUri = getBaseUri(request);
        final URI requestUri = getRequestUri(request, baseUri);

        try {
            final ContainerRequest requestContext = new ContainerRequest(baseUri, requestUri,
                    request.getMethod(), getSecurityContext(request), new MapPropertiesDelegate());
            requestContext.setEntityStream(request.getInputStream());
            for (final String headerName : request.getNames()) {
                requestContext.headers(headerName, request.getValue(headerName));
            }
            requestContext.setWriter(responseWriter);
            requestContext.setRequestScopedInitializer(injectionManager -> {
                injectionManager.<Ref<Request>>getInstance(RequestTYPE).set(request);
                injectionManager.<Ref<Response>>getInstance(ResponseTYPE).set(response);
            });

            appHandler.handle(requestContext);
        } catch (final Exception ex) {
            throw new RuntimeException(ex);
        } finally {
            if (!responseWriter.isSuspended()) {
                close(response);
            }
        }
    }

    private URI getRequestUri(final Request request, final URI baseUri) {
        try {
            final String serverAddress = getServerAddress(baseUri);
            String uri = ContainerUtils.getHandlerPath(request.getTarget());

            final String queryString = request.getQuery().toString();
            if (queryString != null) {
                uri = uri + "?" + ContainerUtils.encodeUnsafeCharacters(queryString);
            }

            return new URI(serverAddress + uri);
        } catch (URISyntaxException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    private String getServerAddress(final URI baseUri) throws URISyntaxException {
        return new URI(baseUri.getScheme(), null, baseUri.getHost(), baseUri.getPort(), null, null,
                null).toString();
    }

    private URI getBaseUri(final Request request) {
        try {
            final String hostHeader = request.getValue("Host");

            if (hostHeader != null) {
                final String scheme = request.isSecure() ? "https" : "http";
                return new URI(scheme + "://" + hostHeader + "/");
            } else {
                final Address address = request.getAddress();
                return new URI(address.getScheme(), null, address.getDomain(), address.getPort(), "/", null,
                        null);
            }
        } catch (final URISyntaxException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    private SecurityContext getSecurityContext(final Request request) {
        return new SecurityContext() {

            @Override
            public boolean isUserInRole(final String role) {
                return false;
            }

            @Override
            public boolean isSecure() {
                return request.isSecure();
            }

            @Override
            public Principal getUserPrincipal() {
                return null;
            }

            @Override
            public String getAuthenticationScheme() {
                return null;
            }
        };
    }

    private void close(final Response response) {
        try {
            response.close();
        } catch (final Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public ResourceConfig getConfiguration() {
        return appHandler.getConfiguration();
    }

    @Override
    public void reload() {
        reload(getConfiguration());
    }

    @Override
    public void reload(final ResourceConfig configuration) {
        appHandler.onShutdown(this);

        appHandler = new ApplicationHandler(configuration.register(new SimpleBinder()));
        scheduler = new ScheduledThreadPoolExecutor(2, new DaemonFactory(TimeoutDispatcher.class));
        appHandler.onReload(this);
        appHandler.onStartup(this);
    }

    @Override
    public ApplicationHandler getApplicationHandler() {
        return appHandler;
    }

    /**
     * Inform this container that the server has been started.
     * <p/>
     * This method must be implicitly called after the server containing this container is started.
     */
    void onServerStart() {
        appHandler.onStartup(this);
    }

    /**
     * Inform this container that the server is being stopped.
     * <p/>
     * This method must be implicitly called before the server containing this container is stopped.
     */
    void onServerStop() {
        appHandler.onShutdown(this);
        scheduler.shutdown();
    }

    /**
     * Create a new Simple framework HTTP container.
     *
     * @param application   JAX-RS / Jersey application to be deployed on Simple framework HTTP container.
     * @param parentContext DI provider specific context with application's registered bindings.
     */
    SimpleContainer(final Application application, final Object parentContext) {
        this.appHandler = new ApplicationHandler(application, new SimpleBinder(), parentContext);
        this.scheduler = new ScheduledThreadPoolExecutor(2, new DaemonFactory(TimeoutDispatcher.class));
    }

    /**
     * Create a new Simple framework HTTP container.
     *
     * @param application JAX-RS / Jersey application to be deployed on Simple framework HTTP
     *                    container.
     */
    SimpleContainer(final Application application) {
        this.appHandler = new ApplicationHandler(application, new SimpleBinder());
        this.scheduler = new ScheduledThreadPoolExecutor(2, new DaemonFactory(TimeoutDispatcher.class));
    }
}
