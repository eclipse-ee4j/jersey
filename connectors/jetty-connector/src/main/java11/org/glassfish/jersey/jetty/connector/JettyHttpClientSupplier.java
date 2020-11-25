/*
 * Copyright (c) 2019 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.jersey.jetty.connector;

import org.eclipse.jetty.client.HttpClient;

/**
 * Jetty HttpClient supplier to be registered into Jersey configuration to be used by {@link JettyConnector}.
 * Not every possible configuration option is covered by the Jetty Connector and this supplier offers a way to provide
 * an HttpClient that has configured the options not covered by the Jetty Connector.
 * <p>
 *     Typical usage:
 * </p>
 * <pre>
 * {@code
 * HttpClient httpClient = ...
 *
 * ClientConfig config = new ClientConfig();
 * config.connectorProvider(new JettyConnectorProvider());
 * config.register(new JettyHttpClientSupplier(httpClient));
 * Client client = ClientBuilder.newClient(config);
 * }
 * </pre>
 * <p>
 *     The {@code HttpClient} is configured as if it was created by {@link JettyConnector} the usual way.
 * </p>
 */
public class JettyHttpClientSupplier implements JettyHttpClientContract {
    private final HttpClient httpClient;

    /**
     * {@code HttpClient} supplier to be optionally registered to a {@link org.glassfish.jersey.client.ClientConfig}
     * @param httpClient a HttpClient to be supplied when {@link JettyConnector#getHttpClient()} is called.
     */
    public JettyHttpClientSupplier(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public HttpClient getHttpClient() {
        return httpClient;
    }
}
