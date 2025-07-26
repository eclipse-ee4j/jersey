/*
 * Copyright (c) 2020, 2025 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.helidon.connector;

import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.core.Configuration;

import java.io.OutputStream;

import org.glassfish.jersey.Beta;
import org.glassfish.jersey.client.spi.Connector;
import org.glassfish.jersey.client.spi.ConnectorProvider;
import org.glassfish.jersey.internal.util.JdkVersion;
import org.glassfish.jersey.internal.util.collection.LazyValue;
import org.glassfish.jersey.internal.util.collection.Value;
import org.glassfish.jersey.internal.util.collection.Values;

/**
 * Provider for Helidon WebClient {@link Connector} that utilizes the Helidon HTTP Client to send and receive
 * HTTP request and responses. JDK 8 is not supported by the Helidon Connector.
 * <p>
 * The following properties are only supported at construction of this class:
 * <ul>
 * <li>{@link org.glassfish.jersey.client.ClientProperties#CONNECT_TIMEOUT}</li>
 * <li>{@link org.glassfish.jersey.client.ClientProperties#FOLLOW_REDIRECTS}</li>
 * <li>{@link org.glassfish.jersey.client.ClientProperties#PROXY_URI}</li>
 * <li>{@link org.glassfish.jersey.client.ClientProperties#PROXY_USERNAME}</li>
 * <li>{@link org.glassfish.jersey.client.ClientProperties#PROXY_PASSWORD}</li>
 * <li>{@link org.glassfish.jersey.client.ClientProperties#READ_TIMEOUT}</li>
 * <li>{@link HelidonClientProperties#CONFIG}</li>
 * </ul>
 * <p>
 * If a {@link org.glassfish.jersey.client.ClientResponse} is obtained and an
 * entity is not read from the response then
 * {@link org.glassfish.jersey.client.ClientResponse#close()} MUST be called
 * after processing the response to release connection-based resources.
 * </p>
 * <p>
 * Client operations are thread safe, the HTTP connection may
 * be shared between different threads.
 * </p>
 * <p>
 * If a response entity is obtained that is an instance of {@link java.io.Closeable}
 * then the instance MUST be closed after processing the entity to release
 * connection-based resources.
 * </p>
 * <p>
 * This connector uses {@link org.glassfish.jersey.client.ClientProperties#OUTBOUND_CONTENT_LENGTH_BUFFER} to buffer the entity
 * written for instance by {@link jakarta.ws.rs.core.StreamingOutput}. Should the buffer be small and
 * {@link jakarta.ws.rs.core.StreamingOutput#write(OutputStream)} be called many times, the performance can drop. The Content-Length
 * or the Content_Encoding header is set by the underlaying Helidon WebClient regardless of the
 * {@link org.glassfish.jersey.client.ClientProperties#OUTBOUND_CONTENT_LENGTH_BUFFER} size, however.
 * </p>
 *
 * @since 2.31
 */
@Beta
public class HelidonConnectorProvider implements ConnectorProvider {
    private static final LazyValue<Helidon3ConnectorProvider> helidon3ConnectorProvider =
            Values.lazy((Value<Helidon3ConnectorProvider>) Helidon3ConnectorProvider::new);

    public HelidonConnectorProvider() {
    }

    @Override
    public Connector getConnector(Client client, Configuration runtimeConfig) {
        if (HelidonVersionChecker.VERSION.get() == HelidonVersionChecker.Version.VERSION_3) {
            return helidon3ConnectorProvider.get().getConnector(client, runtimeConfig);
        } else if (JdkVersion.getJdkVersion().getMajor() < 21) {
            throw new ProcessingException(LocalizationMessages.HELIDON_4_NOT_SUPPORTED());
        }
        return new HelidonConnector(client, runtimeConfig);
    }
}
