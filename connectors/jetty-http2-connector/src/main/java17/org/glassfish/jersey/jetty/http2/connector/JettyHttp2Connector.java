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

package org.glassfish.jersey.jetty.http2.connector;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.core.Configuration;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpClientTransport;
import org.eclipse.jetty.http2.client.HTTP2Client;
import org.eclipse.jetty.http2.client.transport.HttpClientTransportOverHTTP2;
import org.eclipse.jetty.io.ClientConnector;
import org.glassfish.jersey.jetty.connector.JettyConnector;

import java.util.Optional;

/**
 * Extends {@link JettyConnector} with HTTP/2 transport support
 *
 * @since 2.41
 */
class JettyHttp2Connector extends JettyConnector {


    /**
     * Create the new Jetty HTTP/2 client connector.
     *
     * @param jaxrsClient JAX-RS client instance, for which the connector is created.
     * @param config      client configuration.
     */
    JettyHttp2Connector(Client jaxrsClient, Configuration config) {
        super(jaxrsClient, config);
    }

    /**
     * provides required {@link HttpClientTransport} for client
     *
     * The overriden method provides {@link HttpClientTransportOverHTTP2} with initialized {@link HTTP2Client}
     *
     * @return {@link HttpClientTransportOverHTTP2}
     * @since 2.41
     */
    @Override
    protected HttpClientTransport initClientTransport(ClientConnector clientConnector) {
        return new HttpClientTransportOverHTTP2(new HTTP2Client(clientConnector));
    }

    /**
     * provides custom registered {@link HttpClient} (if any) with HTTP/2 support
     *
     * @param config configuration where {@link HttpClient} could be registered
     * @return {@link HttpClient} instance if any was previously registered or NULL
     *
     * @since 2.41
     */
    @Override
    protected HttpClient getRegisteredHttpClient(Configuration config) {
        if (config.isRegistered(JettyHttp2ClientSupplier.class)) {
            Optional<Object> contract = config.getInstances().stream()
                    .filter(a-> JettyHttp2ClientSupplier.class.isInstance(a)).findFirst();
            if (contract.isPresent()) {
                return  ((JettyHttp2ClientSupplier) contract.get()).getHttpClient();
            }
        }
        return null;
    }
}
