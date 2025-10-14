/*
 * Copyright (c) 2025 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.helidon;


import io.helidon.common.context.Context;
import io.helidon.common.context.Contexts;
import io.helidon.common.uri.UriInfo;
import io.helidon.common.uri.UriPath;
import io.helidon.http.Header;
import io.helidon.http.HeaderNames;
import io.helidon.http.HeaderValues;
import io.helidon.http.InternalServerException;
import io.helidon.http.ServerResponseHeaders;
import io.helidon.http.Status;
import io.helidon.webserver.KeyPerformanceIndicatorSupport;
import io.helidon.webserver.http.HttpRules;
import io.helidon.webserver.http.HttpService;
import io.helidon.webserver.http.RoutingResponse;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.glassfish.jersey.internal.MapPropertiesDelegate;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.internal.util.collection.Ref;
import org.glassfish.jersey.server.ApplicationHandler;
import org.glassfish.jersey.server.ContainerException;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.ContainerResponse;
import org.glassfish.jersey.server.spi.Container;
import org.glassfish.jersey.server.spi.ContainerResponseWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 *
 * The code is inspired by the Helidon 3.x JerseySupport class and the Helidon 4.x JaxRsService class
 * Current class is a combination of those 2 classes adopted for Jersey needs
 *
 */
class HelidonJerseyRoutingService implements HttpService {

    private static final System.Logger LOGGER = System.getLogger(HelidonJerseyRoutingService.class.getName());
    private static final Type REQUEST_TYPE = (new GenericType<Ref<ServerRequest>>() { }).getType();
    private static final Type RESPONSE_TYPE = (new GenericType<Ref<ServerResponse>>() { }).getType();
    private final HelidonJerseyBridge bridge;
    private HelidonJerseyRoutingService(HelidonJerseyBridge bridge) {
        this.bridge = bridge;
    }

    static HelidonJerseyRoutingService create(HelidonJerseyBridge bridge) {
        return new HelidonJerseyRoutingService(bridge);
    }

    private static String basePath(UriPath path) {
        final String reqPath = path.path();
        final String absPath = path.absolute().path();
        final String basePath = absPath.substring(0, absPath.length() - reqPath.length() + 1);

        if (absPath.isEmpty() || basePath.isEmpty()) {
            return "/";
        } else if (basePath.charAt(basePath.length() - 1) != '/') {
            return basePath + "/";
        } else {
            return basePath;
        }
    }

    private ApplicationHandler appHandler() {
        return container().getApplicationHandler();
    }

    private Container container() {
        return bridge.getContainer();
    }

    @Override
    public void routing(HttpRules rules) {
        rules.any(this::handle);
    }

    @Override
    public void beforeStart() {
        appHandler().onStartup(container());
    }

    @Override
    public void afterStop() {
        try {
            appHandler().onShutdown(container());
        } catch (Exception e) {
            if (LOGGER.isLoggable(System.Logger.Level.DEBUG)) {
                LOGGER.log(System.Logger.Level.DEBUG, "Exception during shutdown of Jersey", e);
            }
            LOGGER.log(System.Logger.Level.WARNING, "Exception while shutting down Jersey's application handler "
                    + e.getMessage());
        }
    }

    private void handle(final ServerRequest req, final ServerResponse res) {
        final Context context = req.context();

        // make these available in context for ServerCdiExtension
        context.supply(ServerRequest.class, () -> req);
        context.supply(ServerResponse.class, () -> res);

        // call doHandle in active context
        Contexts.runInContext(context, () -> doHandle(context, req, res));
    }

    private void doHandle(final Context ctx, final ServerRequest req, final ServerResponse res) {
        ServerResponseHeaders savedResponseHeaders = null;
        if (req.listenerContext().config().restoreResponseHeaders()) {
            savedResponseHeaders = ServerResponseHeaders.create(res.headers());
        }

        final BaseUriRequestUri uris = BaseUriRequestUri.resolve(req);
        final ContainerRequest requestContext = new ContainerRequest(uris.baseUri,
                uris.requestUri,
                req.prologue().method().text(),
                new HelidonMpSecurityContext(),
                new MapPropertiesDelegate(),
                container().getConfiguration());
        /*
         MP CORS supports needs a way to obtain the UriInfo from the request context.
         */
        requestContext.setProperty(UriInfo.class.getName(), ((Supplier<UriInfo>) req::requestedUri));

        for (final Header header : req.headers()) {
            requestContext.headers(header.name(),
                    header.allValues());
        }

        final JaxRsResponseWriter writer = new JaxRsResponseWriter(res);
        requestContext.setWriter(writer);
        requestContext.setEntityStream(req.content().inputStream());
        requestContext.setProperty("io.helidon.jaxrs.remote-host", req.remotePeer().host());
        requestContext.setProperty("io.helidon.jaxrs.remote-port", req.remotePeer().port());
        requestContext.setRequestScopedInitializer(ij -> {
            ij.<Ref<ServerRequest>>getInstance(REQUEST_TYPE).set(req);
            ij.<Ref<ServerResponse>>getInstance(RESPONSE_TYPE).set(res);
        });

        final Optional<KeyPerformanceIndicatorSupport.DeferrableRequestContext> kpiMetricsContext =
                req.context().get(KeyPerformanceIndicatorSupport.DeferrableRequestContext.class);
        if (LOGGER.isLoggable(System.Logger.Level.TRACE)) {
            LOGGER.log(System.Logger.Level.TRACE, "[" + req.serverSocketId()
                    + " " + req.socketId() + "] Handling in Jersey started");
        }

        ctx.register(container().getConfiguration());

        try {
            kpiMetricsContext.ifPresent(KeyPerformanceIndicatorSupport.DeferrableRequestContext::requestProcessingStarted);
            appHandler().handle(requestContext);
            writer.await();
            if (res.status() == Status.NOT_FOUND_404 && requestContext.getUriInfo().getMatchedResourceMethod() == null) {
                // Jersey will not throw an exception, it will complete the request - but we must
                // continue looking for the next route
                // this is a tricky piece of code - the next can only be called if reset was successful
                // reset may be impossible if data has already been written over the network
                if (res instanceof RoutingResponse) {
                    final RoutingResponse routing = (RoutingResponse) res;
                    if (routing.reset()) {
                        res.status(Status.OK_200);
                        if (savedResponseHeaders != null) {
                            savedResponseHeaders.forEach(res::header);
                        }
                        routing.next();
                    }
                }
            }
        } catch (UncheckedIOException e) {
            throw e;
        } catch (io.helidon.http.NotFoundException | NotFoundException e) {
            // continue execution, maybe there is a non-JAX-RS route (such as static content)
            res.next();
        } catch (Exception e) {
            throw new InternalServerException("Internal exception in JAX-RS processing", e);
        }
    }

    private static class HelidonMpSecurityContext implements SecurityContext {
        @Override
        public Principal getUserPrincipal() {
            return null;
        }

        @Override
        public boolean isUserInRole(String role) {
            return false;
        }

        @Override
        public boolean isSecure() {
            return false;
        }

        @Override
        public String getAuthenticationScheme() {
            return null;
        }
    }

    private static class JaxRsResponseWriter implements ContainerResponseWriter {
        private final CountDownLatch cdl = new CountDownLatch(1);
        private final ServerResponse res;
        private OutputStream outputStream;

        private JaxRsResponseWriter(ServerResponse res) {
            this.res = res;
        }

        @Override
        public OutputStream writeResponseStatusAndHeaders(long contentLengthParam,
                                                          ContainerResponse containerResponse) throws ContainerException {
            long contentLength = contentLengthParam;
            if (contentLength <= 0) {
                String headerString = containerResponse.getHeaderString("Content-Length");
                if (headerString != null) {
                    contentLength = Long.parseLong(headerString);
                }
            }
            for (Map.Entry<String, List<String>> entry : containerResponse.getStringHeaders().entrySet()) {
                String name = entry.getKey();
                List<String> values = entry.getValue();
                if (values.size() == 1) {
                    res.header(HeaderValues.create(HeaderNames.create(name), values.get(0)));
                } else {
                    res.header(HeaderValues.create(entry.getKey(), entry.getValue()));
                }
            }
            Response.StatusType statusInfo = containerResponse.getStatusInfo();
            res.status(Status.create(statusInfo.getStatusCode(), statusInfo.getReasonPhrase()));

            if (contentLength > 0) {
                res.header(HeaderValues.create(HeaderNames.CONTENT_LENGTH, String.valueOf(contentLength)));
            }
            this.outputStream = res.outputStream();
            return outputStream;
        }

        @Override
        public boolean suspend(long timeOut, TimeUnit timeUnit, TimeoutHandler timeoutHandler) {
            if (timeOut != 0) {
                try {
                    cdl.await(timeOut, timeUnit);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                timeoutHandler.onTimeout(this);
                //throw new UnsupportedOperationException("Currently, time limited suspension is not supported!");
            }
            return true;
        }

        @Override
        public void setSuspendTimeout(long l, TimeUnit timeUnit) throws IllegalStateException {
            //throw new UnsupportedOperationException("Currently, extending the suspension time is not supported!");
        }

        @Override
        public void commit() {
            try {
                if (outputStream == null) {
                    res.outputStream().close();
                } else {
                    outputStream.close();
                }
                cdl.countDown();
            } catch (IOException e) {
                cdl.countDown();
                throw new UncheckedIOException(e);
            }
        }

        @Override
        public void failure(Throwable throwable) {
            cdl.countDown();

            if (throwable instanceof RuntimeException) {
                throw (RuntimeException) throwable;
            }
            throw new InternalServerException("Failed to process JAX-RS request", throwable);
        }

        @Override
        public boolean enableResponseBuffering() {
            return true;        // enable buffering in Jersey
        }

        public void await() {
            try {
                cdl.await();
            } catch (InterruptedException e) {
                throw new RuntimeException("Failed to wait for Jersey to write response");
            }
        }
    }

    private static class BaseUriRequestUri {
        private final URI baseUri;
        private final URI requestUri;

        private BaseUriRequestUri(URI baseUri, URI requestUri) {
            this.baseUri = baseUri;
            this.requestUri = requestUri;
        }

        private static BaseUriRequestUri resolve(ServerRequest req) {
            final String processedBasePath = basePath(req.path());
            final String rawPath = req.path().absolute().rawPath();
            final String prefix = (req.isSecure() ? "https" : "http") + "://" + req.authority();
            final String serverBasePath = prefix + processedBasePath;
            String requestPath = prefix + rawPath;
            if (!req.query().isEmpty()) {
                requestPath = requestPath + "?" + req.query().rawValue();
            }
            return new BaseUriRequestUri(URI.create(serverBasePath), URI.create(requestPath));
        }
    }
}
