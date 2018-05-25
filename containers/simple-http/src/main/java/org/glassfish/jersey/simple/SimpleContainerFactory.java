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

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.util.Optional;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.ws.rs.JAXRS.Configuration.SSLClientAuthentication;
import javax.ws.rs.ProcessingException;

import org.glassfish.jersey.internal.util.collection.UnsafeValue;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.simple.internal.LocalizationMessages;
import org.simpleframework.http.core.Container;
import org.simpleframework.http.core.ContainerSocketProcessor;
import org.simpleframework.transport.Socket;
import org.simpleframework.transport.SocketProcessor;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;

/**
 * Factory for creating and starting Simple server containers. This returns a handle to the started
 * server as {@link Closeable} instances, which allows the server to be stopped by invoking the
 * {@link Closeable#close} method.
 * <p/>
 * To start the server in HTTPS mode an {@link SSLContext} can be provided. This will be used to
 * decrypt and encrypt information sent over the connected TCP socket channel.
 *
 * @author Arul Dhesiaseelan (aruld at acm.org)
 * @author Marek Potociar (marek.potociar at oracle.com)
 * @author Paul Sandoz
 */
public final class SimpleContainerFactory {

    private SimpleContainerFactory() {
    }

    /**
     * Create a {@link Closeable} that registers an {@link Container} that in turn manages all root
     * resource and provider classes declared by the resource configuration.
     *
     * @param address the URI to create the http server. The URI scheme must be equal to "http". The
     *                URI user information and host are ignored If the URI port is not present then port 80
     *                will be used. The URI path, query and fragment components are ignored.
     * @param config  the resource configuration.
     * @return the closeable connection, with the endpoint started.
     * @throws ProcessingException      thrown when problems during server creation.
     * @throws IllegalArgumentException if {@code address} is {@code null}.
     */
    public static SimpleServer create(final URI address, final ResourceConfig config) {
        return create(address, null, new SimpleContainer(config));
    }

    /**
     * Create a {@link Closeable} that registers an {@link Container} that in turn manages all root
     * resource and provider classes declared by the resource configuration.
     *
     * @param address the URI to create the http server. The URI scheme must be equal to "http". The
     *                URI user information and host are ignored If the URI port is not present then port 80
     *                will be used. The URI path, query and fragment components are ignored.
     * @param config  the resource configuration.
     * @param count   this is the number of threads to be used.
     * @param select  this is the number of selector threads to use.
     * @return the closeable connection, with the endpoint started.
     * @throws ProcessingException      thrown when problems during server creation.
     * @throws IllegalArgumentException if {@code address} is {@code null}.
     */
    public static SimpleServer create(final URI address, final ResourceConfig config, final int count,
                                      final int select) {
        return create(address, null, new SimpleContainer(config), count, select);
    }

    /**
     * Create a {@link Closeable} that registers an {@link Container} that in turn manages all root
     * resource and provider classes declared by the resource configuration.
     *
     * @param address the URI to create the http server. The URI scheme must be equal to {@code https}
     *                . The URI user information and host are ignored. If the URI port is not present then
     *                port {@value org.glassfish.jersey.server.spi.Container#DEFAULT_HTTPS_PORT} will be used.
     *                The URI path, query and fragment components are ignored.
     * @param context this is the SSL context used for SSL connections.
     * @param config  the resource configuration.
     * @return the closeable connection, with the endpoint started.
     * @throws ProcessingException      thrown when problems during server creation.
     * @throws IllegalArgumentException if {@code address} is {@code null}.
     */
    public static SimpleServer create(final URI address, final SSLContext context,
                                      final ResourceConfig config) {
        return create(address, context, new SimpleContainer(config));
    }

    /**
     * Create a {@link Closeable} that registers an {@link Container} that in turn manages all root
     * resource and provider classes declared by the resource configuration.
     *
     * @param address the URI to create the http server. The URI scheme must be equal to {@code https}
     *                . The URI user information and host are ignored. If the URI port is not present then
     *                port {@value org.glassfish.jersey.server.spi.Container#DEFAULT_HTTPS_PORT} will be used.
     *                The URI path, query and fragment components are ignored.
     * @param context this is the SSL context used for SSL connections.
     * @param config  the resource configuration.
     * @param count   this is the number of threads to be used.
     * @param select  this is the number of selector threads to use.
     * @return the closeable connection, with the endpoint started.
     * @throws ProcessingException      thrown when problems during server creation.
     * @throws IllegalArgumentException if {@code address} is {@code null}.
     */
    public static SimpleServer create(final URI address, final SSLContext context,
                                      final ResourceConfig config, final int count, final int select) {
        return create(address, context, new SimpleContainer(config), count, select);
    }

    /**
     * Create a {@link Closeable} that registers an {@link Container} that in turn manages all root
     * resource and provider classes found by searching the classes referenced in the java classpath.
     *
     * @param address   the URI to create the http server. The URI scheme must be equal to {@code https}
     *                  . The URI user information and host are ignored. If the URI port is not present then
     *                  port {@value org.glassfish.jersey.server.spi.Container#DEFAULT_HTTPS_PORT} will be used.
     *                  The URI path, query and fragment components are ignored.
     * @param context   this is the SSL context used for SSL connections.
     * @param container the container that handles all HTTP requests.
     * @return the closeable connection, with the endpoint started.
     * @throws ProcessingException      thrown when problems during server creation.
     * @throws IllegalArgumentException if {@code address} is {@code null}.
     */
    public static SimpleServer create(final URI address, final SSLContext context,
                                      final SimpleContainer container) {
        return _create(address, context, container, new UnsafeValue<SocketProcessor, IOException>() {
            @Override
            public SocketProcessor get() throws IOException {
                return new ContainerSocketProcessor(container);
            }
        }, true);
    }

    /**
     * Create a {@link Closeable} that registers an {@link Container} that in turn manages all root
     * resource and provider classes found by searching the classes referenced in the java classpath.
     *
     * @param address   the URI to create the http server. The URI scheme must be equal to {@code https}
     *                  . The URI user information and host are ignored. If the URI port is not present then
     *                  port {@value org.glassfish.jersey.server.spi.Container#DEFAULT_HTTPS_PORT} will be used.
     *                  The URI path, query and fragment components are ignored.
     * @param context   this is the SSL context used for SSL connections.
     * @param config    the resource configuration.
     * @param sslClientAuthentication Secure socket client authentication policy.
     * @param start whether the server shall listen to connections immediately
     * @return the closeable connection, with the endpoint started.
     * @throws ProcessingException      thrown when problems during server creation.
     * @throws IllegalArgumentException if {@code address} is {@code null}.
     */
    public static SimpleServer create(final URI address, final SSLContext context,
            final SSLClientAuthentication sslClientAuthentication, final SimpleContainer container, final boolean start) {
        return _create(address, context, container, new UnsafeValue<SocketProcessor, IOException>() {
            @Override
            public SocketProcessor get() throws IOException {
                return new ContainerSocketProcessor(container) {
                    @Override
                    public final void process(final Socket socket) throws IOException {
                        final SSLEngine sslEngine = socket.getEngine();
                        if (sslEngine != null) {
                            switch (sslClientAuthentication) {
                            case MANDATORY: {
                                sslEngine.setNeedClientAuth(true);
                                break;
                            }
                            case OPTIONAL: {
                                sslEngine.setWantClientAuth(true);
                                break;
                            }
                            default: {
                                sslEngine.setNeedClientAuth(false);
                                break;
                            }
                            }
                        }
                        super.process(socket);
                    }
                };
            }
        }, start);
    }

    /**
     * Create a {@link Closeable} that registers an {@link Container} that in turn manages all root
     * resource and provider classes declared by the resource configuration.
     *
     * @param address       the URI to create the http server. The URI scheme must be equal to {@code https}
     *                      . The URI user information and host are ignored. If the URI port is not present then
     *                      port {@value org.glassfish.jersey.server.spi.Container#DEFAULT_HTTPS_PORT} will be used.
     *                      The URI path, query and fragment components are ignored.
     * @param context       this is the SSL context used for SSL connections.
     * @param config        the resource configuration.
     * @param parentContext DI provider specific context with application's registered bindings.
     * @param count         this is the number of threads to be used.
     * @param select        this is the number of selector threads to use.
     * @return the closeable connection, with the endpoint started.
     * @throws ProcessingException      thrown when problems during server creation.
     * @throws IllegalArgumentException if {@code address} is {@code null}.
     * @since 2.12
     */
    public static SimpleServer create(final URI address, final SSLContext context,
                                      final ResourceConfig config, final Object parentContext, final int count,
                                      final int select) {
        return create(address, context, new SimpleContainer(config, parentContext), count, select);
    }

    /**
     * Create a {@link Closeable} that registers an {@link Container} that in turn manages all root
     * resource and provider classes found by searching the classes referenced in the java classpath.
     *
     * @param address   the URI to create the http server. The URI scheme must be equal to {@code https}
     *                  . The URI user information and host are ignored. If the URI port is not present then
     *                  port {@value org.glassfish.jersey.server.spi.Container#DEFAULT_HTTPS_PORT} will be used.
     *                  The URI path, query and fragment components are ignored.
     * @param context   this is the SSL context used for SSL connections.
     * @param container the container that handles all HTTP requests.
     * @param count     this is the number of threads to be used.
     * @param select    this is the number of selector threads to use.
     * @return the closeable connection, with the endpoint started.
     * @throws ProcessingException      thrown when problems during server creation.
     * @throws IllegalArgumentException if {@code address} is {@code null}.
     */
    public static SimpleServer create(final URI address, final SSLContext context,
                                      final SimpleContainer container, final int count, final int select)
            throws ProcessingException {

        return _create(address, context, container, new UnsafeValue<SocketProcessor, IOException>() {
            @Override
            public SocketProcessor get() throws IOException {
                return new ContainerSocketProcessor(container, count, select);
            }
        }, true);
    }

    private static SimpleServer _create(final URI address, final SSLContext context,
                                        final SimpleContainer container,
                                        final UnsafeValue<SocketProcessor, IOException> serverProvider,
                                        final boolean start)
            throws ProcessingException {
        if (address == null) {
            throw new IllegalArgumentException(LocalizationMessages.URI_CANNOT_BE_NULL());
        }
        final String scheme = address.getScheme();
        int defaultPort = org.glassfish.jersey.server.spi.Container.DEFAULT_HTTP_PORT;

        if (context == null) {
            if (!scheme.equalsIgnoreCase("http")) {
                throw new IllegalArgumentException(LocalizationMessages.WRONG_SCHEME_WHEN_USING_HTTP());
            }
        } else {
            if (!scheme.equalsIgnoreCase("https")) {
                throw new IllegalArgumentException(LocalizationMessages.WRONG_SCHEME_WHEN_USING_HTTPS());
            }
            defaultPort = org.glassfish.jersey.server.spi.Container.DEFAULT_HTTPS_PORT;
        }
        int port = address.getPort();

        if (port == -1) {
            port = defaultPort;
        }
        final InetSocketAddress listen = new InetSocketAddress(port);
        final Connection connection;
        try {
            final SimpleTraceAnalyzer analyzer = new SimpleTraceAnalyzer();
            final SocketProcessor server = serverProvider.get();
            connection = new SocketConnection(server, analyzer);

            final SimpleServer simpleServer = new SimpleServer() {
                private InetSocketAddress socketAddr = listen;

                @Override
                public final void start() throws IOException {
                    this.socketAddr = (InetSocketAddress) connection.connect(listen, context);
                    container.onServerStart();
                }

                @Override
                public void close() throws IOException {
                    container.onServerStop();
                    analyzer.stop();
                    connection.close();
                    this.socketAddr = listen;
                }

                @Override
                public int getPort() {
                    return this.socketAddr.getPort();
                }

                @Override
                public boolean isDebug() {
                    return analyzer.isActive();
                }

                @Override
                public void setDebug(boolean enable) {
                    if (enable) {
                        analyzer.start();
                    } else {
                        analyzer.stop();
                    }
                }
            };

            if (start) {
                simpleServer.start();
            }

            return simpleServer;
        } catch (final IOException ex) {
            throw new ProcessingException(LocalizationMessages.ERROR_WHEN_CREATING_SERVER(), ex);
        }
    }
}
