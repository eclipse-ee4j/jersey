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

package org.glassfish.jersey.client;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.logging.Logger;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Configuration;

import org.glassfish.jersey.client.internal.HttpUrlConnector;
import org.glassfish.jersey.client.internal.LocalizationMessages;
import org.glassfish.jersey.client.spi.Connector;
import org.glassfish.jersey.client.spi.ConnectorProvider;

/**
 * Default Jersey client {@link org.glassfish.jersey.client.spi.Connector connector} provider
 * that provides connector instances which delegate HTTP requests to {@link java.net.HttpURLConnection}
 * for processing.
 * <p>
 * The provided connector instances override default behaviour of the property
 * {@link ClientProperties#REQUEST_ENTITY_PROCESSING} and use {@link RequestEntityProcessing#BUFFERED}
 * request entity processing by default.
 * </p>
 * <p>
 * Due to a bug in the chunked transport coding support of {@code HttpURLConnection} that causes
 * requests to fail unpredictably, this connector provider allows to configure the provided connector
 * instances to use {@code HttpURLConnection}'s fixed-length streaming mode as a workaround. This
 * workaround can be enabled via {@link #useFixedLengthStreaming()} method or via
 * {@link #USE_FIXED_LENGTH_STREAMING} Jersey client configuration property.
 * </p>
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public class HttpUrlConnectorProvider implements ConnectorProvider {
    /**
     * If {@code true}, the {@link HttpUrlConnector} (if used) will assume the content length
     * from the value of {@value javax.ws.rs.core.HttpHeaders#CONTENT_LENGTH} request
     * header (if present).
     * <p>
     * When this property is enabled and the request has a valid non-zero content length
     * value specified in its {@value javax.ws.rs.core.HttpHeaders#CONTENT_LENGTH} request
     * header, that this value will be used as an input to the
     * {@link java.net.HttpURLConnection#setFixedLengthStreamingMode(int)} method call
     * invoked on the underlying {@link java.net.HttpURLConnection connection}.
     * This will also suppress the entity buffering in the @{code HttpURLConnection},
     * which is undesirable in certain scenarios, e.g. when streaming large entities.
     * </p>
     * <p>
     * Note that the content length value defined in the request header must exactly match
     * the real size of the entity. If the {@link javax.ws.rs.core.HttpHeaders#CONTENT_LENGTH} header
     * is explicitly specified in a request, this property will be ignored and the
     * request entity will be still buffered by the underlying @{code HttpURLConnection} infrastructure.
     * </p>
     * <p>
     * This property also overrides the behaviour enabled by the
     * {@link org.glassfish.jersey.client.ClientProperties#CHUNKED_ENCODING_SIZE} property.
     * Chunked encoding will only be used, if the size is not specified in the header of the request.
     * </p>
     * <p>
     * Note that this property only applies to client run-times that are configured to use the default
     * {@link HttpUrlConnector} as the client connector. The property is ignored by other connectors.
     * </p>
     * <p>
     * The default value is {@code false}.
     * </p>
     * <p>
     * The name of the configuration property is <tt>{@value}</tt>.
     * </p>
     *
     * @since 2.5
     */
    public static final String USE_FIXED_LENGTH_STREAMING =
            "jersey.config.client.httpUrlConnector.useFixedLengthStreaming";

    /**
     * A value of {@code true} declares that the client will try to set
     * unsupported HTTP method to {@link java.net.HttpURLConnection} via
     * reflection.
     * <p>
     * NOTE: Enabling this property may cause security related warnings/errors
     * and it may break when other JDK implementation is used. <b>Use only
     * when you know what you are doing.</b>
     * </p>
     * <p>The value MUST be an instance of {@link java.lang.Boolean}.</p>
     * <p>The default value is {@code false}.</p>
     * <p>The name of the configuration property is <tt>{@value}</tt>.</p>
     */
    public static final String SET_METHOD_WORKAROUND =
            "jersey.config.client.httpUrlConnection.setMethodWorkaround";
    /**
     * Default connection factory to be used.
     */
    private static final ConnectionFactory DEFAULT_CONNECTION_FACTORY = new DefaultConnectionFactory();

    private static final Logger LOGGER = Logger.getLogger(HttpUrlConnectorProvider.class.getName());

    private ConnectionFactory connectionFactory;
    private int chunkSize;
    private boolean useFixedLengthStreaming;
    private boolean useSetMethodWorkaround;

    /**
     * Create new {@link java.net.HttpURLConnection}-based Jersey client connector provider.
     */
    public HttpUrlConnectorProvider() {
        this.connectionFactory = DEFAULT_CONNECTION_FACTORY;
        this.chunkSize = ClientProperties.DEFAULT_CHUNK_SIZE;
        this.useFixedLengthStreaming = false;
        this.useSetMethodWorkaround = false;
    }

    /**
     * Set a custom {@link java.net.HttpURLConnection} factory.
     *
     * @param connectionFactory custom HTTP URL connection factory. Must not be {@code null}.
     * @return updated connector provider instance.
     * @throws java.lang.NullPointerException in case the supplied connectionFactory is {@code null}.
     */
    public HttpUrlConnectorProvider connectionFactory(final ConnectionFactory connectionFactory) {
        if (connectionFactory == null) {
            throw new NullPointerException(LocalizationMessages.NULL_INPUT_PARAMETER("connectionFactory"));
        }

        this.connectionFactory = connectionFactory;
        return this;
    }

    /**
     * Set chunk size for requests transferred using a
     * <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec3.html#sec3.6.1">HTTP chunked transfer coding</a>.
     *
     * If no value is set, the default chunk size of 4096 bytes will be used.
     * <p>
     * Note that this programmatically set value can be overridden by
     * setting the {@link org.glassfish.jersey.client.ClientProperties#CHUNKED_ENCODING_SIZE} property
     * specified in the Jersey client instance configuration.
     * </p>
     *
     * @param chunkSize chunked transfer coding chunk size to be used.
     * @return updated connector provider instance.
     * @throws java.lang.IllegalArgumentException in case the specified chunk size is negative.
     */
    public HttpUrlConnectorProvider chunkSize(final int chunkSize) {
        if (chunkSize < 0) {
            throw new IllegalArgumentException(LocalizationMessages.NEGATIVE_INPUT_PARAMETER("chunkSize"));
        }
        this.chunkSize = chunkSize;
        return this;
    }

    /**
     * Instruct the provided connectors to use the {@link java.net.HttpURLConnection#setFixedLengthStreamingMode(int)
     * fixed-length streaming mode} on the underlying HTTP URL connection instance when sending requests.
     * See {@link #USE_FIXED_LENGTH_STREAMING} property documentation for more details.
     * <p>
     * Note that this programmatically set value can be overridden by
     * setting the {@code USE_FIXED_LENGTH_STREAMING} property specified in the Jersey client instance configuration.
     * </p>
     *
     * @return updated connector provider instance.
     */
    public HttpUrlConnectorProvider useFixedLengthStreaming() {
        this.useFixedLengthStreaming = true;
        return this;
    }

    /**
     * Instruct the provided connectors to use reflection when setting the
     * HTTP method value.See {@link #SET_METHOD_WORKAROUND} property documentation for more details.
     * <p>
     * Note that this programmatically set value can be overridden by
     * setting the {@code SET_METHOD_WORKAROUND} property specified in the Jersey client instance configuration
     * or in the request properties.
     * </p>
     *
     * @return updated connector provider instance.
     */
    public HttpUrlConnectorProvider useSetMethodWorkaround() {
        this.useSetMethodWorkaround = true;
        return this;
    }

    @Override
    public Connector getConnector(final Client client, final Configuration config) {
        final Map<String, Object> properties = config.getProperties();

        int computedChunkSize = ClientProperties.getValue(properties,
                ClientProperties.CHUNKED_ENCODING_SIZE, chunkSize, Integer.class);
        if (computedChunkSize < 0) {
            LOGGER.warning(LocalizationMessages.NEGATIVE_CHUNK_SIZE(computedChunkSize, chunkSize));
            computedChunkSize = chunkSize;
        }

        final boolean computedUseFixedLengthStreaming = ClientProperties.getValue(properties,
                USE_FIXED_LENGTH_STREAMING, useFixedLengthStreaming, Boolean.class);
        final boolean computedUseSetMethodWorkaround = ClientProperties.getValue(properties,
                SET_METHOD_WORKAROUND, useSetMethodWorkaround, Boolean.class);

        return createHttpUrlConnector(client, connectionFactory, computedChunkSize, computedUseFixedLengthStreaming,
                                      computedUseSetMethodWorkaround);
    }

    /**
     * Create {@link HttpUrlConnector}.
     *
     * @param client              JAX-RS client instance for which the connector is being created.
     * @param connectionFactory   {@link javax.net.ssl.HttpsURLConnection} factory to be used when creating
     *                            connections.
     * @param chunkSize           chunk size to use when using HTTP chunked transfer coding.
     * @param fixLengthStreaming  specify if the the {@link java.net.HttpURLConnection#setFixedLengthStreamingMode(int)
     *                            fixed-length streaming mode} on the underlying HTTP URL connection instances should
     *                            be used when sending requests.
     * @param setMethodWorkaround specify if the reflection workaround should be used to set HTTP URL connection method
     *                            name. See {@link HttpUrlConnectorProvider#SET_METHOD_WORKAROUND} for details.
     * @return created {@link HttpUrlConnector} instance.
     */
    protected Connector createHttpUrlConnector(Client client, ConnectionFactory connectionFactory,
                                               int chunkSize, boolean fixLengthStreaming,
                                               boolean setMethodWorkaround) {
        return new HttpUrlConnector(
                client,
                connectionFactory,
                chunkSize,
                fixLengthStreaming,
                setMethodWorkaround);
    }

    /**
     * A factory for {@link java.net.HttpURLConnection} instances.
     * <p>
     * A factory may be used to create a {@link java.net.HttpURLConnection} and configure
     * it in a custom manner that is not possible using the Client API.
     * <p>
     * A custom factory instance may be registered in the {@code HttpUrlConnectorProvider} instance
     * via {@link #connectionFactory(ConnectionFactory)} method.
     */
    public interface ConnectionFactory {

        /**
         * Get a {@link java.net.HttpURLConnection} for a given URL.
         * <p>
         * Implementation of the method MUST be thread-safe and MUST ensure that
         * a dedicated {@link java.net.HttpURLConnection} instance is returned for concurrent
         * requests.
         * </p>
         *
         * @param url the endpoint URL.
         * @return the {@link java.net.HttpURLConnection}.
         * @throws java.io.IOException in case the connection cannot be provided.
         */
        public HttpURLConnection getConnection(URL url) throws IOException;
    }

    private static class DefaultConnectionFactory implements ConnectionFactory {

        @Override
        public HttpURLConnection getConnection(final URL url) throws IOException {
            return (HttpURLConnection) url.openConnection();
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final HttpUrlConnectorProvider that = (HttpUrlConnectorProvider) o;

        if (chunkSize != that.chunkSize) {
            return false;
        }
        if (useFixedLengthStreaming != that.useFixedLengthStreaming) {
            return false;
        }

        return connectionFactory.equals(that.connectionFactory);
    }

    @Override
    public int hashCode() {
        int result = connectionFactory.hashCode();
        result = 31 * result + chunkSize;
        result = 31 * result + (useFixedLengthStreaming ? 1 : 0);
        return result;
    }
}
