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

package org.glassfish.jersey.jdkhttp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import javax.ws.rs.ProcessingException;

import javax.net.ssl.SSLContext;

import org.glassfish.jersey.internal.guava.ThreadFactoryBuilder;
import org.glassfish.jersey.jdkhttp.internal.LocalizationMessages;
import org.glassfish.jersey.process.JerseyProcessingUncaughtExceptionHandler;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.spi.Container;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;

/**
 * Factory for creating {@link HttpServer JDK HttpServer} instances to run Jersey applications.
 *
 * @author Miroslav Fuksa
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public final class JdkHttpServerFactory {

    private static final Logger LOG = Logger.getLogger(JdkHttpServerFactory.class.getName());

    /**
     * Create and start the {@link HttpServer JDK HttpServer} with the Jersey application deployed
     * at the given {@link URI}.
     * <p>
     * The returned {@link HttpServer JDK HttpServer} is started.
     * </p>
     *
     * @param uri           the {@link URI uri} on which the Jersey application will be deployed.
     * @param configuration the Jersey server-side application configuration.
     * @return Newly created {@link HttpServer}.
     * @throws ProcessingException thrown when problems during server creation
     *                             occurs.
     */
    public static HttpServer createHttpServer(final URI uri, final ResourceConfig configuration) {
        return createHttpServer(uri, configuration, true);
    }

    /**
     * Create (and possibly start) the {@link HttpServer JDK HttpServer} with the JAX-RS / Jersey application deployed
     * on the given {@link URI}.
     * <p>
     * The {@code start} flag controls whether or not the returned {@link HttpServer JDK HttpServer} is started.
     * </p>
     *
     * @param uri           the {@link URI uri} on which the Jersey application will be deployed.
     * @param configuration the Jersey server-side application configuration.
     * @param start         if set to {@code false}, the created server will not be automatically started.
     * @return Newly created {@link HttpServer}.
     * @throws ProcessingException thrown when problems during server creation occurs.
     * @since 2.8
     */
    public static HttpServer createHttpServer(final URI uri, final ResourceConfig configuration, final boolean start) {
        return createHttpServer(uri, new JdkHttpHandlerContainer(configuration), start);
    }

    /**
     * Create (and possibly start) the {@link HttpServer JDK HttpServer} with the JAX-RS / Jersey application deployed
     * on the given {@link URI}.
     * <p/>
     *
     * @param uri           the {@link URI uri} on which the Jersey application will be deployed.
     * @param configuration the Jersey server-side application configuration.
     * @param parentContext DI provider specific context with application's registered bindings.
     * @return Newly created {@link HttpServer}.
     * @throws ProcessingException thrown when problems during server creation occurs.
     * @see org.glassfish.jersey.jdkhttp.JdkHttpHandlerContainer
     * @since 2.12
     */
    public static HttpServer createHttpServer(final URI uri, final ResourceConfig configuration,
                                              final Object parentContext) {
        return createHttpServer(uri, new JdkHttpHandlerContainer(configuration, parentContext), true);
    }

    /**
     * Create and start the {@link HttpServer JDK HttpServer}, eventually {@code HttpServer}'s subclass
     * {@link HttpsServer JDK HttpsServer} with the JAX-RS / Jersey application deployed on the given {@link URI}.
     * <p>
     * The returned {@link HttpServer JDK HttpServer} is started.
     * </p>
     *
     * @param uri           the {@link URI uri} on which the Jersey application will be deployed.
     * @param configuration the Jersey server-side application configuration.
     * @param sslContext    custom {@link SSLContext} to be passed to the server
     * @return Newly created {@link HttpServer}.
     * @throws ProcessingException thrown when problems during server creation occurs.
     * @since 2.18
     */
    public static HttpServer createHttpServer(final URI uri, final ResourceConfig configuration,
                                              final SSLContext sslContext) {
        return createHttpServer(uri, new JdkHttpHandlerContainer(configuration),
                sslContext, true);
    }

    /**
     * Create (and possibly start) the {@link HttpServer JDK HttpServer}, eventually {@code HttpServer}'s subclass
     * {@link HttpsServer JDK HttpsServer} with the JAX-RS / Jersey application deployed on the given {@link URI}.
     * <p>
     * The {@code start} flag controls whether or not the returned {@link HttpServer JDK HttpServer} is started.
     * </p>
     *
     * @param uri           the {@link URI uri} on which the Jersey application will be deployed.
     * @param configuration the Jersey server-side application configuration.
     * @param sslContext    custom {@link SSLContext} to be passed to the server
     * @param start         if set to {@code false}, the created server will not be automatically started.
     * @return Newly created {@link HttpServer}.
     * @throws ProcessingException thrown when problems during server creation occurs.
     * @since 2.17
     */
    public static HttpServer createHttpServer(final URI uri, final ResourceConfig configuration,
                                              final SSLContext sslContext, final boolean start) {
        return createHttpServer(uri,
                new JdkHttpHandlerContainer(configuration),
                sslContext,
                start);
    }

    /**
     * Create (and possibly start) the {@link HttpServer JDK HttpServer}, eventually {@code HttpServer}'s subclass
     * {@link HttpsServer} with the JAX-RS / Jersey application deployed on the given {@link URI}.
     * <p>
     * The {@code start} flag controls whether or not the returned {@link HttpServer JDK HttpServer} is started.
     * </p>
     *
     * @param uri           the {@link URI uri} on which the Jersey application will be deployed.
     * @param configuration the Jersey server-side application configuration.
     * @param parentContext DI provider specific context with application's registered bindings.
     * @param sslContext    custom {@link SSLContext} to be passed to the server
     * @param start         if set to {@code false}, the created server will not be automatically started.
     * @return Newly created {@link HttpServer}.
     * @throws ProcessingException thrown when problems during server creation occurs.
     * @since 2.18
     */
    public static HttpServer createHttpServer(final URI uri, final ResourceConfig configuration,
                                              final Object parentContext,
                                              final SSLContext sslContext, final boolean start) {
        return createHttpServer(uri,
                new JdkHttpHandlerContainer(configuration, parentContext),
                sslContext,
                start
        );
    }

    private static HttpServer createHttpServer(final URI uri, final JdkHttpHandlerContainer handler,
                                               final boolean start) {
        return createHttpServer(uri, handler, null, start);
    }

    private static HttpServer createHttpServer(final URI uri,
                                               final JdkHttpHandlerContainer handler,
                                               final SSLContext sslContext,
                                               final boolean start) {
        if (uri == null) {
            throw new IllegalArgumentException(LocalizationMessages.ERROR_CONTAINER_URI_NULL());
        }

        final String scheme = uri.getScheme();
        final boolean isHttp = "http".equalsIgnoreCase(scheme);
        final boolean isHttps = "https".equalsIgnoreCase(scheme);
        final HttpsConfigurator httpsConfigurator = sslContext != null ? new HttpsConfigurator(sslContext) : null;

        if (isHttp) {
            if (httpsConfigurator != null) {
                // attempt to use https with http scheme
                LOG.warning(LocalizationMessages.WARNING_CONTAINER_URI_SCHEME_SECURED());
            }
        } else if (isHttps) {
            if (httpsConfigurator == null) {
                if (start) {
                    // The SSLContext (via HttpsConfigurator) has to be set before the server starts.
                    // Starting https server w/o SSL is invalid, it will lead to error anyway.
                    throw new IllegalArgumentException(LocalizationMessages.ERROR_CONTAINER_HTTPS_NO_SSL());
                } else {
                    // Creating the https server w/o SSL context, but not starting it is valid.
                    // However, server.setHttpsConfigurator() must be called before the start.
                    LOG.info(LocalizationMessages.INFO_CONTAINER_HTTPS_NO_SSL());
                }
            }
        } else {
            throw new IllegalArgumentException(LocalizationMessages.ERROR_CONTAINER_URI_SCHEME_UNKNOWN(uri));
        }

        final String path = uri.getPath();
        if (path == null) {
            throw new IllegalArgumentException(LocalizationMessages.ERROR_CONTAINER_URI_PATH_NULL(uri));
        } else if (path.isEmpty()) {
            throw new IllegalArgumentException(LocalizationMessages.ERROR_CONTAINER_URI_PATH_EMPTY(uri));
        } else if (path.charAt(0) != '/') {
            throw new IllegalArgumentException(LocalizationMessages.ERROR_CONTAINER_URI_PATH_START(uri));
        }

        final int port = (uri.getPort() == -1)
                ? (isHttp ? Container.DEFAULT_HTTP_PORT : Container.DEFAULT_HTTPS_PORT)
                : uri.getPort();

        final HttpServer server;
        try {
            server = isHttp
                    ? HttpServer.create(new InetSocketAddress(port), 0)
                    : HttpsServer.create(new InetSocketAddress(port), 0);
        } catch (final IOException ioe) {
            throw new ProcessingException(LocalizationMessages.ERROR_CONTAINER_EXCEPTION_IO(), ioe);
        }

        if (isHttps && httpsConfigurator != null) {
            ((HttpsServer) server).setHttpsConfigurator(httpsConfigurator);
        }

        server.setExecutor(Executors.newCachedThreadPool(new ThreadFactoryBuilder()
                .setNameFormat("jdk-http-server-%d")
                .setUncaughtExceptionHandler(new JerseyProcessingUncaughtExceptionHandler())
                .build()));
        server.createContext(path, handler);

        final HttpServer wrapper = isHttp
                ? createHttpServerWrapper(server, handler)
                : createHttpsServerWrapper((HttpsServer) server, handler);

        if (start) {
            wrapper.start();
        }

        return wrapper;
    }

    private static HttpServer createHttpsServerWrapper(final HttpsServer delegate, final JdkHttpHandlerContainer handler) {
        return new HttpsServer() {

            @Override
            public void setHttpsConfigurator(final HttpsConfigurator httpsConfigurator) {
                delegate.setHttpsConfigurator(httpsConfigurator);
            }

            @Override
            public HttpsConfigurator getHttpsConfigurator() {
                return delegate.getHttpsConfigurator();
            }

            @Override
            public void bind(final InetSocketAddress inetSocketAddress, final int i) throws IOException {
                delegate.bind(inetSocketAddress, i);
            }

            @Override
            public void start() {
                delegate.start();
                handler.onServerStart();
            }

            @Override
            public void setExecutor(final Executor executor) {
                delegate.setExecutor(executor);
            }

            @Override
            public Executor getExecutor() {
                return delegate.getExecutor();
            }

            @Override
            public void stop(final int i) {
                handler.onServerStop();
                delegate.stop(i);
            }

            @Override
            public HttpContext createContext(final String s, final HttpHandler httpHandler) {
                return delegate.createContext(s, httpHandler);
            }

            @Override
            public HttpContext createContext(final String s) {
                return delegate.createContext(s);
            }

            @Override
            public void removeContext(final String s) throws IllegalArgumentException {
                delegate.removeContext(s);
            }

            @Override
            public void removeContext(final HttpContext httpContext) {
                delegate.removeContext(httpContext);
            }

            @Override
            public InetSocketAddress getAddress() {
                return delegate.getAddress();
            }
        };
    }

    private static HttpServer createHttpServerWrapper(final HttpServer delegate, final JdkHttpHandlerContainer handler) {
        return new HttpServer() {

            @Override
            public void bind(final InetSocketAddress inetSocketAddress, final int i) throws IOException {
                delegate.bind(inetSocketAddress, i);
            }

            @Override
            public void start() {
                delegate.start();
                handler.onServerStart();
            }

            @Override
            public void setExecutor(final Executor executor) {
                delegate.setExecutor(executor);
            }

            @Override
            public Executor getExecutor() {
                return delegate.getExecutor();
            }

            @Override
            public void stop(final int i) {
                handler.onServerStop();
                delegate.stop(i);
            }

            @Override
            public HttpContext createContext(final String s, final HttpHandler httpHandler) {
                return delegate.createContext(s, httpHandler);
            }

            @Override
            public HttpContext createContext(final String s) {
                return delegate.createContext(s);
            }

            @Override
            public void removeContext(final String s) throws IllegalArgumentException {
                delegate.removeContext(s);
            }

            @Override
            public void removeContext(final HttpContext httpContext) {
                delegate.removeContext(httpContext);
            }

            @Override
            public InetSocketAddress getAddress() {
                return delegate.getAddress();
            }
        };
    }

    /**
     * Prevents instantiation.
     */
    private JdkHttpServerFactory() {
        throw new AssertionError("Instantiation not allowed.");
    }
}
