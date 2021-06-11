/*
 * Copyright (c) 2021 Payara Foundation and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.grizzly2.httpserver.test.tools;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.NetworkListener;
import org.glassfish.grizzly.http2.Http2AddOn;
import org.glassfish.grizzly.http2.Http2Configuration;
import org.glassfish.grizzly.ssl.SSLContextConfigurator;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.grizzly2.httpserver.test.application.TestedEndpoint;
import org.glassfish.jersey.server.ResourceConfig;

/**
 * This manager maintains the lifecycle of the {@link HttpServer} and trivial rest application.
 *
 * @author David Matejcek
 */
public class ServerManager implements Closeable {

    private static final String LISTENER_NAME_GRIZZLY = "grizzly";
    private static final String PROTOCOL_HTTPS = "https";
    private static final String PROTOCOL_HTTP = "http";
    private static final String APPLICATION_CONTEXT = "/test-application";
    private static final String SERVICE_CONTEXT = "/tested-endpoint";

    private final URI endpointUri;
    private final HttpServer server;


    /**
     * Initializes the server environment and starts the server.
     *
     * @param secured - set true to enable network encryption
     * @param useHttp2 - set true to enable HTTP/2; then secured must be set to true too.
     * @throws IOException
     */
    public ServerManager(final boolean secured, final boolean useHttp2) throws IOException {
        this.endpointUri = getEndpointUri(secured, APPLICATION_CONTEXT);
        final NetworkListener listener = createListener(secured, useHttp2, endpointUri.getHost(), endpointUri.getPort());
        final ResourceConfig resourceConfig = createResourceConfig();
        this.server = startServer(listener, this.endpointUri, resourceConfig);
    }


    /**
     * @return {@link URI} of the application endpoint.
     */
    public URI getApplicationEndpoint() {
        return this.endpointUri;
    }


    /**
     * @return {@link URI} of the deployed service endpoint.
     */
    public URI getApplicationServiceEndpoint() {
        return URI.create(getApplicationEndpoint() + SERVICE_CONTEXT);
    }


    /**
     * Calls the {@link HttpServer#shutdownNow()}. The server and all it's resources are destroyed.
     */
    @Override
    public void close() {
        if (server != null) {
            server.shutdownNow();
        }
    }


    private static URI getEndpointUri(final boolean secured, final String applicationContext) {
        try {
            final String protocol = secured ? PROTOCOL_HTTPS : PROTOCOL_HTTP;
            return new URL(protocol, getLocalhost(), getFreePort(), applicationContext).toURI();
        } catch (MalformedURLException | URISyntaxException e) {
            throw new IllegalStateException("Unable to create an endpoint URI.", e);
        }
    }


    private static String getLocalhost() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (final UnknownHostException e) {
            return "localhost";
        }
    }


    /**
     * Tries to alocate a free local port.
     *
     * @return a free local port number.
     * @throws IllegalStateException if it fails for 20 times
     */
    private static int getFreePort() {
        int attempts = 0;
        while (true) {
            attempts++;
            try (ServerSocket socket = new ServerSocket(0)) {
                final int port = socket.getLocalPort();
                socket.setSoTimeout(1);
                socket.setReuseAddress(true);
                return port;
            } catch (final IOException e) {
                if (attempts >= 20) {
                    throw new IllegalStateException("Cannot open random port, tried 20 times.", e);
                }
            }
        }
    }


    private static NetworkListener createListener(final boolean secured, final boolean useHttp2, final String host,
        final int port) {
        if (useHttp2 && !secured) {
            throw new IllegalArgumentException("HTTP/2 cannot be used without encryption");
        }
        final NetworkListener listener = new NetworkListener(LISTENER_NAME_GRIZZLY, host, port);
        listener.setSecure(secured);
        if (secured) {
            listener.setSSLEngineConfig(createSSLEngineConfigurator(host));
        }
        if (useHttp2) {
            listener.registerAddOn(createHttp2AddOn());
        }
        return listener;
    }


    private static SSLEngineConfigurator createSSLEngineConfigurator(final String host) {
        final KeyStoreManager keyStoreManager = new KeyStoreManager(host);
        final SSLContextConfigurator configurator = new SSLContextConfigurator();
        configurator.setKeyStoreBytes(keyStoreManager.getKeyStoreBytes());
        configurator.setKeyStorePass(keyStoreManager.getKeyStorePassword());
        configurator.setTrustStoreBytes(keyStoreManager.getKeyStoreBytes());
        configurator.setTrustStorePass(keyStoreManager.getKeyStorePassword());
        final SSLEngineConfigurator sslEngineConfigurator = new SSLEngineConfigurator(configurator)
            .setClientMode(false).setNeedClientAuth(false);
        return sslEngineConfigurator;
    }


    private static Http2AddOn createHttp2AddOn() {
        final Http2Configuration configuration = Http2Configuration.builder().build();
        return new Http2AddOn(configuration);
    }


    private static ResourceConfig createResourceConfig() {
        return new ResourceConfig().registerClasses(TestedEndpoint.class);
    }


    private static HttpServer startServer(final NetworkListener listener, final URI endpointUri,
        final ResourceConfig resourceConfig) {
        final HttpServer srv = GrizzlyHttpServerFactory.createHttpServer(endpointUri, resourceConfig, false);
        try {
            srv.addListener(listener);
            srv.start();
            return srv;
        } catch (final IOException e) {
            throw new IllegalStateException("Could not start the server!", e);
        }
    }
}
