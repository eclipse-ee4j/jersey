/*
 * Copyright (c) 2021, 2022 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.jnh.connector;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.core.Configurable;
import jakarta.ws.rs.core.Configuration;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.Initializable;
import org.glassfish.jersey.client.spi.Connector;
import org.glassfish.jersey.client.spi.ConnectorProvider;

import java.net.http.HttpClient;

/**
 * A provider class for a Jersey client {@link Connector} using Java's {@link HttpClient}.
 * <p>
 *     The following configuration properties are available:
 *     <ul>
 *         <li>{@link ClientProperties#CONNECT_TIMEOUT}</li>
 *         <li>{@link ClientProperties#FOLLOW_REDIRECTS} (defaults to {@link java.net.http.HttpClient.Redirect#NORMAL} when unset)</li>
 *         <li>{@link JavaNetHttpClientProperties#COOKIE_HANDLER}</li>
 *         <li>{@link JavaNetHttpClientProperties#SSL_PARAMETERS}</li>
 *     </ul>
 * </p>
 *
 * @author Steffen Nießing
 */
public class JavaNetHttpConnectorProvider implements ConnectorProvider {
    @Override
    public Connector getConnector(Client client, Configuration runtimeConfig) {
        return new JavaNetHttpConnector(client, runtimeConfig);
    }

    /**
     * Retrieve the Java {@link HttpClient} used by the provided {@link JavaNetHttpConnector}.
     *
     * @param component the component from which the {@link JavaNetHttpConnector} should be retrieved
     * @return a Java {@link HttpClient} instance
     * @throws java.lang.IllegalArgumentException if a {@link JavaNetHttpConnector} cannot be provided from the given {@code component}
     */
    public static HttpClient getHttpClient(Configurable<?> component) {
        try {
            final Initializable<?> initializable = (Initializable<?>) component;

            Connector connector = initializable.getConfiguration().getConnector() != null
                    ? initializable.getConfiguration().getConnector()
                    : initializable.preInitialize().getConfiguration().getConnector();

            if (connector instanceof JavaNetHttpConnector) {
                return ((JavaNetHttpConnector) connector).getHttpClient();
            } else {
                throw new IllegalArgumentException(LocalizationMessages.EXPECTED_CONNECTOR_PROVIDER_NOT_USED());
            }
        } catch (ClassCastException classCastException) {
            throw new IllegalArgumentException(
                    LocalizationMessages.INVALID_CONFIGURABLE_COMPONENT_TYPE(component.getClass().getName()),
                    classCastException
            );
        }
    }
}
