/*
 * Copyright (c) 2023 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.jetty.http2;

import org.eclipse.jetty.alpn.server.ALPNServerConnectionFactory;
import org.eclipse.jetty.http2.server.HTTP2CServerConnectionFactory;
import org.eclipse.jetty.http2.server.HTTP2ServerConnectionFactory;
import org.eclipse.jetty.server.ConnectionFactory;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.glassfish.jersey.jetty.JettyHttpContainer;
import org.glassfish.jersey.jetty.JettyHttpContainerFactory;
import org.glassfish.jersey.jetty.JettyHttpContainerProvider;
import org.glassfish.jersey.jetty.http2.LocalizationMessages;
import org.glassfish.jersey.server.ContainerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.ProcessingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public final class JettyHttp2ContainerFactory {

    private JettyHttp2ContainerFactory() {

    }

    /**
     * Creates HTTP/2 enabled  {@link Server} instance that registers an {@link org.eclipse.jetty.server.Handler}.
     *
     * @param uri uri on which the {@link org.glassfish.jersey.server.ApplicationHandler} will be deployed. Only first path
     *            segment will be used as context path, the rest will be ignored.
     * @return newly created {@link Server}.
     *
     * @throws ProcessingException      in case of any failure when creating a new Jetty {@code Server} instance.
     * @throws IllegalArgumentException if {@code uri} is {@code null}.
     */
    public static Server createHttp2Server(final URI uri) throws ProcessingException {
        return createHttp2Server(uri, null, null, true);
    }

    /**
     * Create HTTP/2 enabled {@link Server} that registers an {@link org.eclipse.jetty.server.Handler} that
     * in turn manages all root resource and provider classes declared by the
     * resource configuration.
     * <p/>
     * This implementation defers to the
     * {@link org.glassfish.jersey.server.ContainerFactory#createContainer(Class, javax.ws.rs.core.Application)} method
     * for creating an Container that manages the root resources.
     *
     * @param uri           URI on which the Jersey web application will be deployed. Only first path segment will be
     *                      used as context path, the rest will be ignored.
     * @param configuration web application configuration.
     * @param start         if set to false, server will not get started, which allows to configure the underlying
     *                      transport layer, see above for details.
     * @return newly created {@link Server}.
     *
     * @throws ProcessingException      in case of any failure when creating a new Jetty {@code Server} instance.
     * @throws IllegalArgumentException if {@code uri} is {@code null}.
     */
    public static Server createHttp2Server(final URI uri, final ResourceConfig configuration, final boolean start)
            throws ProcessingException {
        return createHttp2Server(uri, null,
                ContainerFactory.createContainer(JettyHttpContainer.class, configuration), start);
    }

    /**
     * Creates HTTP/2 enabled  {@link Server} instance that registers an {@link org.eclipse.jetty.server.Handler}.
     *
     * @param uri   uri on which the {@link org.glassfish.jersey.server.ApplicationHandler} will be deployed. Only first path
     *              segment will be used as context path, the rest will be ignored.
     * @param start if set to false, server will not get started, which allows to configure the underlying transport
     *              layer, see above for details.
     * @return newly created {@link Server}.
     *
     * @throws ProcessingException      in case of any failure when creating a new Jetty {@code Server} instance.
     * @throws IllegalArgumentException if {@code uri} is {@code null}.
     *
     * @since 2.40
     */

    public static Server createHttp2Server(final URI uri, final boolean start) throws ProcessingException {
        return createHttp2Server(uri, null, null, start);
    }

    /**
     * Create HTTP/2 enabled {@link Server} that registers an {@link org.eclipse.jetty.server.Handler} that
     * in turn manages all root resource and provider classes declared by the
     * resource configuration.
     *
     * @param uri           the URI to create the http server. The URI scheme must be
     *                      equal to "https". The URI user information and host
     *                      are ignored If the URI port is not present then port 143 will be
     *                      used. The URI path, query and fragment components are ignored.
     * @param config        the resource configuration.
     * @param parentContext DI provider specific context with application's registered bindings.
     * @param start         if set to false, server will not get started, this allows end users to set
     *                      additional properties on the underlying listener.
     * @return newly created {@link Server}.
     *
     * @throws ProcessingException      in case of any failure when creating a new Jetty {@code Server} instance.
     * @throws IllegalArgumentException if {@code uri} is {@code null}.
     * @see JettyHttpContainer
     *
     * @since 2.40
     */
    public static Server createHttp2Server(final URI uri, final ResourceConfig config, final boolean start,
                                           final Object parentContext) {
        return createHttp2Server(uri, null,
                new JettyHttpContainerProvider().createContainer(JettyHttpContainer.class,
                        config, parentContext), start);
    }

    /**
     * Create HTTP/2 enabled {@link Server} that registers an {@link org.eclipse.jetty.server.Handler} that
     * in turn manages all root resource and provider classes found by searching the
     * classes referenced in the java classpath.
     *
     * @param uri               the URI to create the http server. The URI scheme must be
     *                          equal to {@code https}. The URI user information and host
     *                          are ignored. If the URI port is not present then port
     *                          {@value org.glassfish.jersey.server.spi.Container#DEFAULT_HTTPS_PORT} will be
     *                          used. The URI path, query and fragment components are ignored.
     * @param sslContextFactory this is the SSL context factory used to configure SSL connector
     * @param handler           the container that handles all HTTP requests
     * @param start             if set to false, server will not get started, this allows end users to set
     *                          additional properties on the underlying listener.
     * @return newly created {@link Server}.
     *
     * @throws ProcessingException      in case of any failure when creating a new Jetty {@code Server} instance.
     * @throws IllegalArgumentException if {@code uri} is {@code null}.
     * @see JettyHttpContainer
     *
     * @since 2.40
     */
    public static Server createHttp2Server(final URI uri,
                                           final SslContextFactory sslContextFactory,
                                           final JettyHttpContainer handler,
                                           final boolean start) {

        /**
         * Creating basic Jetty HTTP/1.1 container (but always not started)
         */
        final Server server = JettyHttpContainerFactory.createServer(uri, sslContextFactory, handler, false);
        /**
         * Obtain configured HTTP connection factory
         */
        final ServerConnector httpServerConnector = (ServerConnector) server.getConnectors()[0];
        final HttpConnectionFactory httpConnectionFactory = httpServerConnector.getConnectionFactory(HttpConnectionFactory.class);

        /**
         * Obtain prepared config
         */
        final HttpConfiguration config = httpConnectionFactory.getHttpConfiguration();

        /**
         * Add required H2/H2C connection factories using pre-configured config from the HTTP/1.1 server
         */
        final List<ConnectionFactory> factories = getConnectionFactories(config, sslContextFactory);
        factories.add(httpConnectionFactory);

        /**
         * replacing connectors for H2/H2C protocol
         */
        httpServerConnector.setConnectionFactories(factories);
        server.setConnectors(new Connector[]{httpServerConnector});

        /**
         * Starting the server if required
         */
        if (start) {
            try {
                // Start the server.
                server.start();
            } catch (final Exception e) {
                throw new ProcessingException(LocalizationMessages.ERROR_WHEN_CREATING_SERVER(), e);
            }
        }
        return server;
    }

    private static List<ConnectionFactory> getConnectionFactories(final HttpConfiguration config,
                                                                  final SslContextFactory sslContextFactory) {
        final List<ConnectionFactory> factories = new ArrayList<>();
        if (sslContextFactory != null) {
            factories.add(new HTTP2ServerConnectionFactory(config));
            final ALPNServerConnectionFactory alpn = new ALPNServerConnectionFactory();
            alpn.setDefaultProtocol("h2");
            factories.add(new SslConnectionFactory(sslContextFactory, alpn.getProtocol()));
            factories.add(alpn);
        } else {
            factories.add(new HTTP2CServerConnectionFactory(config));
        }

        return factories;
    }
}
