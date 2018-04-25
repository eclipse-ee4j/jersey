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

package org.glassfish.jersey.apache.connector;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Configurable;
import javax.ws.rs.core.Configuration;

import org.glassfish.jersey.client.Initializable;
import org.glassfish.jersey.client.spi.Connector;
import org.glassfish.jersey.client.spi.ConnectorProvider;

import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;

/**
 * Connector provider for Jersey {@link Connector connectors} that utilize
 * Apache HTTP Client to send and receive HTTP request and responses.
 * <p>
 * The following connector configuration properties are supported:
 * <ul>
 * <li>{@link ApacheClientProperties#CONNECTION_MANAGER}</li>
 * <li>{@link ApacheClientProperties#REQUEST_CONFIG}</li>
 * <li>{@link ApacheClientProperties#CREDENTIALS_PROVIDER}</li>
 * <li>{@link ApacheClientProperties#DISABLE_COOKIES}</li>
 * <li>{@link org.glassfish.jersey.client.ClientProperties#PROXY_URI}</li>
 * <li>{@link org.glassfish.jersey.client.ClientProperties#PROXY_USERNAME}</li>
 * <li>{@link org.glassfish.jersey.client.ClientProperties#PROXY_PASSWORD}</li>
 * <li>{@link org.glassfish.jersey.client.ClientProperties#REQUEST_ENTITY_PROCESSING}
 * - default value is {@link org.glassfish.jersey.client.RequestEntityProcessing#CHUNKED}</li>
 * <li>{@link ApacheClientProperties#PREEMPTIVE_BASIC_AUTHENTICATION}</li>
 * <li>{@link ApacheClientProperties#RETRY_HANDLER}</li>
 * </ul>
 * </p>
 * <p>
 * Connector instances created via this connector provider use
 * {@link org.glassfish.jersey.client.RequestEntityProcessing#CHUNKED chunked encoding} as a default setting.
 * This can be overridden by the {@link org.glassfish.jersey.client.ClientProperties#REQUEST_ENTITY_PROCESSING}.
 * By default the {@link org.glassfish.jersey.client.ClientProperties#CHUNKED_ENCODING_SIZE} property is only supported
 * when using the default {@code org.apache.http.conn.HttpClientConnectionManager} instance. If custom
 * connection manager is used, then chunked encoding size can be set by providing a custom
 * {@code org.apache.http.HttpClientConnection} (via custom {@code org.apache.http.impl.conn.ManagedHttpClientConnectionFactory})
 * and overriding it's {@code createOutputStream} method.
 * </p>
 * <p>
 * Use of authorization by the AHC-based connectors is dependent on the chunk encoding setting.
 * If the entity buffering is enabled, the entity is buffered and authorization can be performed
 * automatically in response to a 401 by sending the request again. When entity buffering
 * is disabled (chunked encoding is used) then the property
 * {@link org.glassfish.jersey.apache.connector.ApacheClientProperties#PREEMPTIVE_BASIC_AUTHENTICATION} must
 * be set to {@code true}.
 * </p>
 * <p>
 * If a {@link org.glassfish.jersey.client.ClientResponse} is obtained and an entity is not read from the response then
 * {@link org.glassfish.jersey.client.ClientResponse#close()} MUST be called after processing the response to release
 * connection-based resources.
 * </p>
 * <p>
 * If a response entity is obtained that is an instance of {@link java.io.Closeable}
 * then the instance MUST be closed after processing the entity to release
 * connection-based resources.
 * <p/>
 * <p>
 * The following methods are currently supported: HEAD, GET, POST, PUT, DELETE, OPTIONS, PATCH and TRACE.
 * <p/>
 *
 * @author Pavel Bucek (pavel.bucek at oracle.com)
 * @author Arul Dhesiaseelan (aruld at acm.org)
 * @author jorgeluisw at mac.com
 * @author Marek Potociar (marek.potociar at oracle.com)
 * @author Paul Sandoz
 * @author Maksim Mukosey (mmukosey at gmail.com)
 * @since 2.5
 */
public class ApacheConnectorProvider implements ConnectorProvider {

    @Override
    public Connector getConnector(final Client client, final Configuration runtimeConfig) {
        return new ApacheConnector(client, runtimeConfig);
    }

    /**
     * Retrieve the underlying Apache {@link HttpClient} instance from
     * {@link org.glassfish.jersey.client.JerseyClient} or {@link org.glassfish.jersey.client.JerseyWebTarget}
     * configured to use {@code ApacheConnectorProvider}.
     *
     * @param component {@code JerseyClient} or {@code JerseyWebTarget} instance that is configured to use
     *                  {@code ApacheConnectorProvider}.
     * @return underlying Apache {@code HttpClient} instance.
     *
     * @throws java.lang.IllegalArgumentException in case the {@code component} is neither {@code JerseyClient}
     *                                            nor {@code JerseyWebTarget} instance or in case the component
     *                                            is not configured to use a {@code ApacheConnectorProvider}.
     * @since 2.8
     */
    public static HttpClient getHttpClient(final Configurable<?> component) {
        return getConnector(component).getHttpClient();
    }

    /**
     * Retrieve the underlying Apache {@link CookieStore} instance from
     * {@link org.glassfish.jersey.client.JerseyClient} or {@link org.glassfish.jersey.client.JerseyWebTarget}
     * configured to use {@code ApacheConnectorProvider}.
     *
     * @param component {@code JerseyClient} or {@code JerseyWebTarget} instance that is configured to use
     *                  {@code ApacheConnectorProvider}.
     * @return underlying Apache {@code CookieStore} instance.
     * @throws java.lang.IllegalArgumentException in case the {@code component} is neither {@code JerseyClient}
     *                                            nor {@code JerseyWebTarget} instance or in case the component
     *                                            is not configured to use a {@code ApacheConnectorProvider}.
     * @since 2.16
     */
    public static CookieStore getCookieStore(final Configurable<?> component) {
        return getConnector(component).getCookieStore();
    }

    private static ApacheConnector getConnector(final Configurable<?> component) {
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

        if (connector instanceof ApacheConnector) {
            return (ApacheConnector) connector;
        } else {
            throw new IllegalArgumentException(LocalizationMessages.EXPECTED_CONNECTOR_PROVIDER_NOT_USED());
        }
    }
}
