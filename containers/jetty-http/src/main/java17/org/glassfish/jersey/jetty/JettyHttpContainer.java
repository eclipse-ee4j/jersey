/*
 * Copyright (c) 2013, 2024 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.jetty;

import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.SecurityContext;

import jakarta.inject.Inject;
import jakarta.inject.Provider;

import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.io.Content;
import org.eclipse.jetty.security.AuthenticationState;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.util.Callback;
import org.eclipse.jetty.util.thread.Scheduler;
import org.glassfish.jersey.internal.MapPropertiesDelegate;
import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.internal.inject.ReferencingFactory;
import org.glassfish.jersey.internal.util.ExtendedLogger;
import org.glassfish.jersey.internal.util.collection.Ref;
import org.glassfish.jersey.jetty.internal.LocalizationMessages;
import org.glassfish.jersey.process.internal.RequestScoped;
import org.glassfish.jersey.server.ApplicationHandler;
import org.glassfish.jersey.server.ContainerException;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.ContainerResponse;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.server.internal.ContainerUtils;
import org.glassfish.jersey.server.spi.Container;
import org.glassfish.jersey.server.spi.ContainerResponseWriter;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;

/**
 * Jersey {@code Container} implementation based on Jetty {@link org.eclipse.jetty.server.Handler}.
 *
 * @author Arul Dhesiaseelan (aruld@acm.org)
 * @author Libor Kramolis
 * @author Marek Potociar
 */
public final class JettyHttpContainer extends Handler.Abstract implements Container {

    private static final ExtendedLogger LOGGER =
            new ExtendedLogger(Logger.getLogger(JettyHttpContainer.class.getName()), Level.FINEST);

    private static final Type REQUEST_TYPE = (new GenericType<Ref<Request>>() {}).getType();
    private static final Type RESPONSE_TYPE = (new GenericType<Ref<Response>>() {}).getType();

    private static final int INTERNAL_SERVER_ERROR = jakarta.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
    private static final jakarta.ws.rs.core.Response.Status BAD_REQUEST_STATUS = jakarta.ws.rs.core.Response.Status.BAD_REQUEST;

    /**
     * Cached value of configuration property
     * {@link org.glassfish.jersey.server.ServerProperties#RESPONSE_SET_STATUS_OVER_SEND_ERROR}.
     * If {@code true} method {@link Response#setStatus(int)} is used over {@link Response#writeError(Request, Response, Callback, int)}
     */
    private boolean configSetStatusOverSendError;

    /**
     * Referencing factory for Jetty request.
     */
    private static class JettyRequestReferencingFactory extends ReferencingFactory<Request> {
        @Inject
        public JettyRequestReferencingFactory(final Provider<Ref<Request>> referenceFactory) {
            super(referenceFactory);
        }
    }

    /**
     * Referencing factory for Jetty response.
     */
    private static class JettyResponseReferencingFactory extends ReferencingFactory<Response> {
        @Inject
        public JettyResponseReferencingFactory(final Provider<Ref<Response>> referenceFactory) {
            super(referenceFactory);
        }
    }

    /**
     * An internal binder to enable Jetty HTTP container specific types injection.
     * This binder allows to inject underlying Jetty HTTP request and response instances.
     * Note that since Jetty {@code Request} class is not proxiable as it does not expose an empty constructor,
     * the injection of Jetty request instance into singleton JAX-RS and Jersey providers is only supported via
     * {@link jakarta.inject.Provider injection provider}.
     */
    private static class JettyBinder extends AbstractBinder {

        @Override
        protected void configure() {
            bindFactory(JettyRequestReferencingFactory.class).to(Request.class)
                    .proxy(false).in(RequestScoped.class);
            bindFactory(ReferencingFactory.<Request>referenceFactory()).to(new GenericType<Ref<Request>>() {})
                    .in(RequestScoped.class);

            bindFactory(JettyResponseReferencingFactory.class).to(Response.class)
                    .proxy(false).in(RequestScoped.class);
            bindFactory(ReferencingFactory.<Response>referenceFactory()).to(new GenericType<Ref<Response>>() {})
                    .in(RequestScoped.class);
        }
    }

    private volatile ApplicationHandler appHandler;

    @Override
    public boolean handle(Request request, Response response, Callback callback) throws Exception {

        final ResponseWriter responseWriter = new ResponseWriter(request, response, callback, configSetStatusOverSendError);
        try {
            LOGGER.debugLog(LocalizationMessages.CONTAINER_STARTED());
            final URI baseUri = getBaseUri(request);
            final URI requestUri = getRequestUri(request, baseUri);
            final ContainerRequest requestContext = new ContainerRequest(
                    baseUri,
                    requestUri,
                    request.getMethod(),
                    getSecurityContext(request),
                    new MapPropertiesDelegate(),
                    appHandler.getConfiguration());
            requestContext.setEntityStream(Request.asInputStream(request));
            request.getHeaders().forEach(httpField ->
                    requestContext.headers(httpField.getName(), httpField.getValue() == null ? "" : httpField.getValue()));
            requestContext.setWriter(responseWriter);
            requestContext.setRequestScopedInitializer(injectionManager -> {
                injectionManager.<Ref<Request>>getInstance(REQUEST_TYPE).set(request);
                injectionManager.<Ref<Response>>getInstance(RESPONSE_TYPE).set(response);
            });

            appHandler.handle(requestContext);
            return true;
        } catch (URISyntaxException e) {
            setResponseForInvalidUri(request, response, callback, e);
            return true;
        } catch (final Exception ex) {
            callback.failed(ex);
            throw new RuntimeException(ex);
        }
    }

    private URI getRequestUri(final Request request, final URI baseUri) throws URISyntaxException {
        final String serverAddress = getServerAddress(baseUri);
        String uri = request.getHttpURI().getPath();

        final String queryString = request.getHttpURI().getQuery();
        if (queryString != null) {
            uri = uri + "?" + ContainerUtils.encodeUnsafeCharacters(queryString);
        }

        return new URI(serverAddress + uri);
    }

    private void setResponseForInvalidUri(final Request request, final Response response,
                                          final Callback callback, final Throwable throwable) {
        LOGGER.log(Level.FINER, "Error while processing request.", throwable);

        if (configSetStatusOverSendError) {
            response.reset();
            response.setStatus(BAD_REQUEST_STATUS.getStatusCode());
            callback.failed(throwable);
        } else {
            Response.writeError(request, response, callback, BAD_REQUEST_STATUS.getStatusCode(),
                    BAD_REQUEST_STATUS.getReasonPhrase(), throwable);
        }
    }

    private String getServerAddress(URI baseUri) {
        String serverAddress = baseUri.toString();
        if (serverAddress.charAt(serverAddress.length() - 1) == '/') {
            return serverAddress.substring(0, serverAddress.length() - 1);
        }
        return serverAddress;
    }

    private SecurityContext getSecurityContext(final Request request) {

        AuthenticationState.Succeeded authenticationState = AuthenticationState.authenticate(request);

        return new SecurityContext() {

            @Override
            public boolean isUserInRole(final String role) {
                return authenticationState != null && authenticationState.isUserInRole(role);
            }

            @Override
            public boolean isSecure() {
                return request.isSecure();
            }

            @Override
            public Principal getUserPrincipal() {
                return authenticationState != null ? authenticationState.getUserIdentity().getUserPrincipal() : null;
            }

            @Override
            public String getAuthenticationScheme() {
                return authenticationState != null ? authenticationState.getAuthenticationType() : null;
            }
        };
    }


    private URI getBaseUri(final Request request) throws URISyntaxException {
        return new URI(request.getHttpURI().getScheme(), null, Request.getServerName(request),
                Request.getServerPort(request), getBasePath(request), null, null);
    }

    private String getBasePath(final Request request) {
        final String contextPath = Request.getContextPath(request);

        if (contextPath == null || contextPath.isEmpty()) {
            return "/";
        } else if (contextPath.charAt(contextPath.length() - 1) != '/') {
            return contextPath + "/";
        } else {
            return contextPath;
        }
    }

    private static class ResponseWriter implements ContainerResponseWriter {

        private final Request request;
        private final Response response;
        private final Callback callback;
        private final boolean configSetStatusOverSendError;
        private final long asyncStartTimeNanos;
        private final Scheduler scheduler;
        private final ConcurrentLinkedQueue<TimeoutHandler> timeoutHandlerQueue = new ConcurrentLinkedQueue<>();
        private Scheduler.Task currentTimerTask;

        ResponseWriter(final Request request, final Response response,
                       final Callback callback, final boolean configSetStatusOverSendError) {
            this.request = request;
            this.response = response;
            this.callback = callback;
            this.asyncStartTimeNanos = System.nanoTime();
            this.configSetStatusOverSendError = configSetStatusOverSendError;

            this.scheduler = request.getComponents().getScheduler();
        }

        private synchronized void setNewTimeout(long timeOut, TimeUnit timeUnit) {
            long timeOutNanos = timeUnit.toNanos(timeOut);
            if (currentTimerTask != null) {
                // Do not interrupt, see callTimeoutHandlers()
                currentTimerTask.cancel();
            }
            // Use System.nanoTime() as the clock source here, because the returned value is not prone to wall-clock
            // drift - unlike System.currentTimeMillis().
            long delayNanos = Math.max(asyncStartTimeNanos - System.nanoTime() + timeOutNanos, 0L);
            currentTimerTask = scheduler.schedule(this::callTimeoutHandlers, delayNanos, TimeUnit.NANOSECONDS);
        }

        private void callTimeoutHandlers() {
            // Note: Although it might not happen in practice, it is in theory possible that this function is
            // called multiple times concurrently. To prevent any timeout handler being called twice, we poll()
            // timeout handlers from the queue, instead of iterating over the queue.
            while (true) {
                TimeoutHandler handler = timeoutHandlerQueue.poll();
                if (handler == null) {
                    break;
                }
                handler.onTimeout(ResponseWriter.this);
            }
        }

        @Override
        public OutputStream writeResponseStatusAndHeaders(final long contentLength, final ContainerResponse context)
                throws ContainerException {

            final jakarta.ws.rs.core.Response.StatusType statusInfo = context.getStatusInfo();

            final int code = statusInfo.getStatusCode();

            response.setStatus(code);

            if (contentLength != -1 && contentLength < Integer.MAX_VALUE && !"HEAD".equals(request.getMethod())) {
                response.getHeaders().add(new HttpField(HttpHeader.CONTENT_LENGTH, String.valueOf((int) contentLength)));
            }
            for (final Map.Entry<String, List<String>> e : context.getStringHeaders().entrySet()) {
                for (final String value : e.getValue()) {
                    response.getHeaders().add(new HttpField(e.getKey(), value));
                }
            }

            return Content.Sink.asOutputStream(response);
        }

        @Override
        public boolean suspend(final long timeOut, final TimeUnit timeUnit, final TimeoutHandler timeoutHandler) {
            if (timeOut > 0) {
                setNewTimeout(timeOut, timeUnit);
            }
            if (timeoutHandler != null) {
                timeoutHandlerQueue.add(timeoutHandler);
            }
            return true;
        }

        @Override
        public void setSuspendTimeout(final long timeOut, final TimeUnit timeUnit) throws IllegalStateException {
            if (timeOut > 0) {
                setNewTimeout(timeOut, timeUnit);
            }
        }

        @Override
        public void commit() {
            callback.succeeded();
            LOGGER.log(Level.FINEST, "commit() called");
        }

        @Override
        public void failure(final Throwable error) {
            try {
                if (!response.isCommitted()) {
                    try {
                        if (configSetStatusOverSendError) {
                            response.reset();
                            response.setStatus(INTERNAL_SERVER_ERROR);
                            callback.failed(error);
                        } else {
                            Response.writeError(request, response, callback, INTERNAL_SERVER_ERROR, "Request failed.", error);
                        }
                    } catch (final IllegalStateException ex) {
                        // a race condition externally committing the response can still occur...
                        LOGGER.log(Level.FINER, "Unable to reset failed response.", ex);
                    }
                }
            } finally {
                LOGGER.log(Level.FINEST, "failure(...) called");
                rethrow(error);
            }
        }

        @Override
        public boolean enableResponseBuffering() {
            return false;
        }

        /**
         * Rethrow the original exception as required by JAX-RS, 3.3.4.
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

    @Override
    public ResourceConfig getConfiguration() {
        return appHandler.getConfiguration();
    }

    @Override
    public void reload() {
        reload(new ResourceConfig(getConfiguration()));
    }

    @Override
    public void reload(final ResourceConfig configuration) {
        appHandler.onShutdown(this);

        appHandler = new ApplicationHandler(configuration.register(new JettyBinder()));
        appHandler.onReload(this);
        appHandler.onStartup(this);
        cacheConfigSetStatusOverSendError();
    }

    @Override
    public ApplicationHandler getApplicationHandler() {
        return appHandler;
    }

    /**
     * Inform this container that the server has been started.
     * This method must be implicitly called after the server containing this container is started.
     *
     * @throws java.lang.Exception if a problem occurred during server startup.
     */
    @Override
    protected void doStart() throws Exception {
        super.doStart();
        appHandler.onStartup(this);
    }

    /**
     * Inform this container that the server is being stopped.
     * This method must be implicitly called before the server containing this container is stopped.
     *
     * @throws java.lang.Exception if a problem occurred during server shutdown.
     */
    @Override
    public void doStop() throws Exception {
        super.doStop();
        appHandler.onShutdown(this);
        appHandler = null;

        boolean needInterrupt = false;
        if (needInterrupt) {
            Thread.currentThread().interrupt();
        }
    }

    private static final AtomicInteger TIMEOUT_HANDLER_ID_GEN = new AtomicInteger();

    /**
     * Create a new Jetty HTTP container.
     *
     * @param application   JAX-RS / Jersey application to be deployed on Jetty HTTP container.
     * @param parentContext DI provider specific context with application's registered bindings.
     */
    JettyHttpContainer(final Application application, final Object parentContext) {
        this.appHandler = new ApplicationHandler(application, new JettyBinder(), parentContext);
    }

    /**
     * Create a new Jetty HTTP container.
     *
     * @param application JAX-RS / Jersey application to be deployed on Jetty HTTP container.
     */
    JettyHttpContainer(final Application application) {
        this.appHandler = new ApplicationHandler(application, new JettyBinder());

        cacheConfigSetStatusOverSendError();
    }

    /**
     * Create a new Jetty HTTP container.
     *
     * @param applicationClass JAX-RS / Jersey class of application to be deployed on Jetty HTTP container.
     */
    JettyHttpContainer(final Class<? extends Application> applicationClass) {
        this.appHandler = new ApplicationHandler(applicationClass, new JettyBinder());

        cacheConfigSetStatusOverSendError();
    }

    /**
     * The method reads and caches value of configuration property
     * {@link ServerProperties#RESPONSE_SET_STATUS_OVER_SEND_ERROR} for future purposes.
     */
    private void cacheConfigSetStatusOverSendError() {
        this.configSetStatusOverSendError = ServerProperties.getValue(getConfiguration().getProperties(),
                ServerProperties.RESPONSE_SET_STATUS_OVER_SEND_ERROR, false, Boolean.class);
    }

}
