/*
 * Copyright (c) 2014, 2018 Oracle and/or its affiliates. All rights reserved.
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
 * Caching connector provider.
 *
 * This utility provider can be used to serve as a lazily initialized provider of the same connector instance.
 * <p>
 * Note however that the connector instance will be configured using the runtime configuration of the first client instance that
 * has invoked the {@link #getConnector(javax.ws.rs.client.Client, javax.ws.rs.core.Configuration)} method.
 * {@link javax.ws.rs.client.Client} and {@link javax.ws.rs.core.Configuration} instance passed to subsequent
 * {@code getConnector(...)} invocations will be ignored. As such, this connector provider should not be shared among client
 * instances that have significantly different connector-specific settings.
 * </p>
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 * @since 2.10
 */
public class CachingConnectorProvider implements ConnectorProvider {

    private final ConnectorProvider delegate;
    private Connector connector;


    /**
     * Create the caching connector provider.
     *
     * @param delegate delegate connector provider that will be used to initialize and create the connector instance which
     *                 will be subsequently cached and reused.
     */
    public CachingConnectorProvider(final ConnectorProvider delegate) {
        this.delegate = delegate;
    }

    @Override
    public synchronized Connector getConnector(Client client, Configuration runtimeConfig) {
        if (connector == null) {
            connector = delegate.getConnector(client, runtimeConfig);
        }
        return connector;
    }
}
