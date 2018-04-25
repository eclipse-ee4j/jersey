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

package org.glassfish.jersey.tests.e2e.client.connector.ssl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.logging.Logger;

import javax.ws.rs.core.UriBuilder;

import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.ssl.SSLContextConfigurator;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;

import com.google.common.io.ByteStreams;

/**
 * A simple SSL-secured HTTP server for testing purposes.
 *
 * @author Pavel Bucek (pavel.bucek at oracle.com)
 */
final class Server {

    private static final Logger LOGGER = Logger.getLogger(Server.class.getName());

    private static final String SERVER_TRUST_STORE = "truststore-server";

    /**
     * Base server URI.
     */
    public static final URI BASE_URI = getBaseURI();

    private final HttpServer webServer;

    private Server(final HttpServer webServer) {
        this.webServer = webServer;
    }

    private static URI getBaseURI() {
        return UriBuilder.fromUri("https://localhost/").port(getPort(8463)).build();
    }

    private static int getPort(int defaultPort) {
        final String port = System.getProperty("jersey.config.test.container.port");
        if (null != port) {
            try {
                return Integer.parseInt(port);
            } catch (NumberFormatException e) {
                LOGGER.warning("Value of jersey.config.test.container.port property"
                        + " is not a valid positive integer [" + port + "]."
                        + " Reverting to default [" + defaultPort + "].");
            }
        }
        return defaultPort;
    }

    /**
     * Start SSL-secured HTTP test server.
     *
     * @throws IOException in case there is an error while reading server key store or trust store.
     * @return an instance of the started SSL-secured HTTP test server.
     */
    public static Server start(String keystore) throws IOException {
        final InputStream trustStore = Server.class.getResourceAsStream(SERVER_TRUST_STORE);
        final InputStream keyStore = Server.class.getResourceAsStream(keystore);

        // Grizzly ssl configuration
        SSLContextConfigurator sslContext = new SSLContextConfigurator();

        // set up security context
        sslContext.setKeyStoreBytes(ByteStreams.toByteArray(keyStore));  // contains server key pair
        sslContext.setKeyStorePass("asdfgh");
        sslContext.setTrustStoreBytes(ByteStreams.toByteArray(trustStore)); // contains client certificate
        sslContext.setTrustStorePass("asdfgh");

        ResourceConfig rc = new ResourceConfig();
        rc.register(new LoggingFeature(LOGGER, LoggingFeature.Verbosity.PAYLOAD_ANY));
        rc.registerClasses(RootResource.class, SecurityFilter.class, AuthenticationExceptionMapper.class);

        final HttpServer grizzlyServer = GrizzlyHttpServerFactory.createHttpServer(
                getBaseURI(),
                rc,
                true,
                new SSLEngineConfigurator(sslContext).setClientMode(false).setNeedClientAuth(true)
        );

        // start Grizzly embedded server //
        LOGGER.info("Jersey app started. Try out " + BASE_URI + "\nHit CTRL + C to stop it...");
        grizzlyServer.start();

        return new Server(grizzlyServer);
    }

    /**
     * Stop SSL-secured HTTP test server.
     */
    public void stop() {
        webServer.shutdownNow();
    }
}
