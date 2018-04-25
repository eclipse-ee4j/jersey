/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.client.spi;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Configuration;

/**
 * Jersey client connector provider contract.
 *
 * Connector provider is invoked by Jersey client runtime to provide a client connector
 * to be used to send client requests over the wire to server-side resources.
 * There can be only one connector provider registered in a single Jersey client instance.
 * <p>
 * Note that unlike most of the other {@link org.glassfish.jersey.spi.Contract Jersey SPI extension contracts},
 * {@code ConnectorProvider} is not a typical runtime extension and as such cannot be registered
 * using a configuration {@code register(...)} method. Instead, it must be registered using via
 * {@link org.glassfish.jersey.client.JerseyClientBuilder} using it's
 * {@link org.glassfish.jersey.client.ClientConfig#connectorProvider(ConnectorProvider)}
 * initializer method.
 * </p>
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 * @since 2.5
 */
// Must not be annotated with @Contract
public interface ConnectorProvider {

    /**
     * Get a Jersey client connector instance for a given {@link Client client} instance
     * and Jersey client runtime {@link Configuration configuration}.
     * <p>
     * Note that the supplied runtime configuration can be different from the client instance
     * configuration as a single client can be used to serve multiple differently configured runtimes.
     * While the {@link Client#getSslContext() SSL context} or {@link Client#getHostnameVerifier() hostname verifier}
     * are shared, other configuration properties may change in each runtime.
     * </p>
     * <p>
     * Based on the supplied client and runtime configuration data, it is up to each connector provider
     * implementation to decide whether a new dedicated connector instance is required or if the existing,
     * previously create connector instance can be reused.
     * </p>
     *
     * @param client        Jersey client instance.
     * @param runtimeConfig Jersey client runtime configuration.
     * @return configured {@link org.glassfish.jersey.client.spi.Connector} instance to be used by the client.
     */
    public Connector getConnector(Client client, Configuration runtimeConfig);
}
