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

package org.glassfish.jersey.jetty11.http2.connector;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.core.Configurable;
import jakarta.ws.rs.core.Configuration;
import org.eclipse.jetty.client.HttpClient;
import org.glassfish.jersey.client.Initializable;
import org.glassfish.jersey.client.spi.Connector;
import org.glassfish.jersey.jetty11.connector.Jetty11ConnectorProvider;
import org.glassfish.jersey.jetty11.connector.LocalizationMessages;

/**
 * Provides HTTP2 enabled version of the {@link Jetty11ConnectorProvider} for a client
 *
 * @since 2.41
 */
public class Jetty11Http2ConnectorProvider extends Jetty11ConnectorProvider {
    @Override
    public Connector getConnector(Client client, Configuration runtimeConfig) {
        return new Jetty11Http2Connector(client, runtimeConfig);
    }

    public static HttpClient getHttpClient(Configurable<?> component) {
        if (!(component instanceof Initializable)) {
            throw new IllegalArgumentException(
                    LocalizationMessages.INVALID_CONFIGURABLE_COMPONENT_TYPE(component.getClass().getName()));
        }

        final Initializable<?> initializable = (Initializable<?>) component;
        Connector connector = initializable.getConfiguration().getConnector();
        if (connector == null) {
            initializable.preInitialize();
            connector = initializable.getConfiguration().getConnector();
        }

        if (connector instanceof Jetty11Http2Connector) {
            return ((Jetty11Http2Connector) connector).getHttpClient();
        }

        throw new IllegalArgumentException(LocalizationMessages.EXPECTED_CONNECTOR_PROVIDER_NOT_USED());
    }
}