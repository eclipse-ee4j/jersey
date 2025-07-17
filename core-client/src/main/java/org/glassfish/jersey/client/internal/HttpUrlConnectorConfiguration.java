/*
 * Copyright (c) 2025 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.client.internal;

import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.ClientRequest;
import org.glassfish.jersey.client.HttpUrlConnectorProvider;
import org.glassfish.jersey.client.innate.ConnectorConfiguration;
import org.glassfish.jersey.internal.util.PropertiesHelper;
import org.glassfish.jersey.internal.util.collection.Ref;

import javax.net.ssl.SSLContext;
import javax.ws.rs.core.Configuration;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.security.AccessController;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.logging.Logger;

public abstract class HttpUrlConnectorConfiguration<C extends HttpUrlConnectorConfiguration<C>>
        extends ConnectorConfiguration<C> {
    private static final Logger LOGGER = Logger.getLogger(HttpUrlConnectorConfiguration.class.getName());
    /* package */ static final String ALLOW_RESTRICTED_HEADERS_SYSTEM_PROPERTY = "sun.net.http.allowRestrictedHeaders";
    /**
     * Default connection factory to be used.
     */
    protected static final HttpUrlConnectorProvider.ConnectionFactory DEFAULT_CONNECTION_FACTORY =
            new DefaultConnectionFactory();

    protected NullableRef<HttpUrlConnectorProvider.ConnectionFactory> connectionFactory = NullableRef.empty();
    protected Ref<Integer> chunkSize = NullableRef.empty();
    /* package */ Ref<Boolean> isRestrictedHeaderPropertySet = NullableRef.empty();
    protected Ref<Boolean> useFixedLengthStreaming = NullableRef.empty();
    protected Ref<Boolean> useSetMethodWorkaround = NullableRef.empty();

    protected void preInit(Map<String, Object> properties) {
        connectionFactory.ifEmptySet(DEFAULT_CONNECTION_FACTORY);
        ((NullableRef<Integer>) chunkSize).ifEmptySet(ClientProperties.DEFAULT_CHUNK_SIZE);
        ((NullableRef<Boolean>) useFixedLengthStreaming).ifEmptySet(Boolean.FALSE);
        ((NullableRef<Boolean>) useSetMethodWorkaround).ifEmptySet(Boolean.FALSE);

        int computedChunkSize = ClientProperties.getValue(properties,
                _prefixed(ClientProperties.CHUNKED_ENCODING_SIZE), chunkSize.get(), Integer.class);
        if (computedChunkSize < 0) {
            LOGGER.warning(LocalizationMessages.NEGATIVE_CHUNK_SIZE(computedChunkSize, chunkSize.get()));
        } else {
            chunkSize.set(computedChunkSize);
        }

        useFixedLengthStreaming(ClientProperties.getValue(properties,
                _prefixed(HttpUrlConnectorProvider.USE_FIXED_LENGTH_STREAMING),
                useFixedLengthStreaming.get(), Boolean.class));
        useSetMethodWorkaround(ClientProperties.getValue(properties,
                _prefixed(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND),
                useSetMethodWorkaround.get(), Boolean.class));
    }

    private String _prefixed(String property) {
        return prefix.ifPresentOrElse("") + property;
    }

    /**
     * Set a custom {@link java.net.HttpURLConnection} factory.
     *
     * @param connectionFactory custom HTTP URL connection factory. Must not be {@code null}.
     * @return updated configuration.
     * @throws java.lang.NullPointerException in case the supplied connectionFactory is {@code null}.
     */
    public C connectionFactory(final HttpUrlConnectorProvider.ConnectionFactory connectionFactory) {
        if (connectionFactory == null) {
            throw new NullPointerException(LocalizationMessages.NULL_INPUT_PARAMETER("connectionFactory"));
        }

        this.connectionFactory.set(connectionFactory);
        return self();
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
     * @return updated configuration.
     * @throws java.lang.IllegalArgumentException in case the specified chunk size is negative.
     */
    public C chunkSize(final int chunkSize) {
        if (chunkSize < 0) {
            throw new IllegalArgumentException(LocalizationMessages.NEGATIVE_INPUT_PARAMETER("chunkSize"));
        }
        this.chunkSize.set(chunkSize);
        return self();
    }

    /**
     * Instruct the provided connectors to use the {@link java.net.HttpURLConnection#setFixedLengthStreamingMode(int)
     * fixed-length streaming mode} on the underlying HTTP URL connection instance when sending requests.
     * See {@link HttpUrlConnectorProvider#USE_FIXED_LENGTH_STREAMING} property documentation for more details.
     * <p>
     * Note that this programmatically set value can be overridden by
     * setting the {@code USE_FIXED_LENGTH_STREAMING} property specified in the Jersey client instance configuration.
     * </p>
     *
     * @return updated configuration.
     */
    public C useFixedLengthStreaming(boolean use) {
        this.useFixedLengthStreaming.set(use);
        return self();
    }

    /**
     * Instruct the provided connectors to use reflection when setting the
     * HTTP method value. See {@link HttpUrlConnectorProvider#SET_METHOD_WORKAROUND} property documentation for more details.
     * <p>
     * Note that this programmatically set value can be overridden by
     * setting the {@code SET_METHOD_WORKAROUND} property specified in the Jersey client instance configuration
     * or in the request properties.
     * </p>
     *
     * @return updated configuration.
     */
    public C useSetMethodWorkaround(boolean use) {
        this.useSetMethodWorkaround.set(use);
        return self();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final HttpUrlConnectorConfiguration<?> that = (HttpUrlConnectorConfiguration<?>) o;

        if (!chunkSize.equals(that.chunkSize)) {
            return false;
        }
        if (!useFixedLengthStreaming.equals(that.useFixedLengthStreaming)) {
            return false;
        }
        if (!useSetMethodWorkaround.equals(that.useSetMethodWorkaround)) {
            return false;
        }
        if (!isRestrictedHeaderPropertySet.equals(that.isRestrictedHeaderPropertySet)) {
            return false;
        }

        return connectionFactory.equals(that.connectionFactory);
    }

    @Override
    public int hashCode() {
        return Objects.hash(connectionFactory, chunkSize, useFixedLengthStreaming,
                useSetMethodWorkaround, isRestrictedHeaderPropertySet);
    }

    /* package */ ReadWrite rw() {
        final ReadWrite readWrite = this instanceof ReadWrite ? ((ReadWrite) this).instance() : new ReadWrite();
        readWrite.setNonEmpty(this);
        return readWrite;
    }

    protected static class ReadWrite
        extends HttpUrlConnectorConfiguration<ReadWrite>
        implements ConnectorConfiguration.Read<ReadWrite> {

        @Override
        public <X extends ConnectorConfiguration<?>> void setNonEmpty(X otherC) {
            HttpUrlConnectorConfiguration<?> other = (HttpUrlConnectorConfiguration<?>) otherC;
            Read.super.setNonEmpty(other);

            this.connectionFactory.setNonEmpty(other.connectionFactory);
            ((NullableRef<Integer>) this.chunkSize).setNonEmpty((NullableRef<Integer>) other.chunkSize);
            ((NullableRef<Boolean>) this.isRestrictedHeaderPropertySet).setNonEmpty(
                    (NullableRef<Boolean>) other.isRestrictedHeaderPropertySet);
            ((NullableRef<Boolean>) this.useFixedLengthStreaming).setNonEmpty(
                    (NullableRef<Boolean>) other.useFixedLengthStreaming);
            ((NullableRef<Boolean>) this.useSetMethodWorkaround).setNonEmpty((NullableRef<Boolean>) other.useSetMethodWorkaround);
        }

        @Override
        public ReadWrite init() {
            ConnectorConfiguration.Read.super.init();
            preInit(Collections.emptyMap());
            isRestrictedHeaderPropertySet.set(Boolean.FALSE);
            return self();
        }

        ReadWrite fromClient(Configuration configuration) {
            ReadWrite clientConfiguration = copyFromClient(configuration);
            clientConfiguration.preInit(configuration.getProperties());

            // check if sun.net.http.allowRestrictedHeaders system property has been set and log the result
            // the property is being cached in the HttpURLConnection, so this is only informative - there might
            // already be some connection(s), that existed before the property was set/changed.
            isRestrictedHeaderPropertySet.set(Boolean.valueOf(AccessController.doPrivileged(
                    PropertiesHelper.getSystemProperty(ALLOW_RESTRICTED_HEADERS_SYSTEM_PROPERTY, "false")
            )));

            LOGGER.config(isRestrictedHeaderPropertySet.get()
                    ? LocalizationMessages.RESTRICTED_HEADER_PROPERTY_SETTING_TRUE(ALLOW_RESTRICTED_HEADERS_SYSTEM_PROPERTY)
                    : LocalizationMessages.RESTRICTED_HEADER_PROPERTY_SETTING_FALSE(ALLOW_RESTRICTED_HEADERS_SYSTEM_PROPERTY)
            );

            return clientConfiguration;
        }

        ReadWrite fromRequest(ClientRequest request) {
            ReadWrite requestConfiguration = copyFromRequest(request);
            requestConfiguration.chunkSize(
                    request.resolveProperty(prefixed(ClientProperties.CHUNKED_ENCODING_SIZE),
                                            requestConfiguration.chunkSize.get()));
            requestConfiguration.useFixedLengthStreaming(
                    request.resolveProperty(prefixed(HttpUrlConnectorProvider.USE_FIXED_LENGTH_STREAMING),
                                            requestConfiguration.useFixedLengthStreaming.get()));
            requestConfiguration.useSetMethodWorkaround(
                    request.resolveProperty(prefixed(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND),
                                            requestConfiguration.useSetMethodWorkaround.get()));

            return requestConfiguration;
        }

        @Override
        public ReadWrite instance() {
            return new ReadWrite();
        }

        @Override
        public ReadWrite me() {
            return this;
        }

        public boolean isMethodWorkaround(ClientRequest request) {
            return request.resolveProperty(
                    prefixed(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND), useSetMethodWorkaround.get());
        }

        public boolean isPrefixed() {
            return !prefix.get().isEmpty();
        }

        public boolean isSslContextSupplier() {
            return sslContextSupplier.get() != null;
        }

        /**
         * Get {@link SSLContext} either from the {@link ClientProperties#SSL_CONTEXT_SUPPLIER}, or from this configuration.
         *
         * @param request the request used to get the {@link SSLContext}.
         * @return the {@link SSLContext} supplier.
         */
        public Supplier<SSLContext> sslContext(ClientRequest request) {
            @SuppressWarnings("unchecked")
            Supplier<SSLContext> supplier =
                    request.resolveProperty(prefixed(ClientProperties.SSL_CONTEXT_SUPPLIER), Supplier.class);
            if (supplier == null) {
                supplier = self().sslContextSupplier.get();
            }
            return supplier;
        }

        @Override
        public ReadWrite self() {
            return this;
        }
    }

    private static class DefaultConnectionFactory implements HttpUrlConnectorProvider.ConnectionFactory {

        @Override
        public HttpURLConnection getConnection(final URL url) throws IOException {
            return connect(url, null);
        }

        @Override
        public HttpURLConnection getConnection(URL url, Proxy proxy) throws IOException {
            return connect(url, proxy);
        }

        private HttpURLConnection connect(URL url, Proxy proxy) throws IOException {
            return (proxy == null) ? (HttpURLConnection) url.openConnection() : (HttpURLConnection) url.openConnection(proxy);
        }
    }
}
