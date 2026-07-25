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

package org.glassfish.jersey.apache.connector;

import org.apache.http.ConnectionReuseStrategy;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CookieStore;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.ManagedHttpClientConnection;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.entity.ContentLengthStrategy;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.DefaultManagedHttpClientConnection;
import org.apache.http.impl.conn.ManagedHttpClientConnectionFactory;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.io.ChunkedOutputStream;
import org.apache.http.io.SessionOutputBuffer;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.TextUtils;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.ClientRequest;
import org.glassfish.jersey.client.innate.ClientProxy;
import org.glassfish.jersey.client.innate.ConnectorConfiguration;
import org.glassfish.jersey.client.innate.http.SSLParamConfigurator;
import org.glassfish.jersey.client.internal.HttpUrlConnectorConfiguration;
import org.glassfish.jersey.internal.util.PropertiesHelper;
import org.glassfish.jersey.internal.util.collection.Ref;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Configuration;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URI;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.stream.Collectors;

class ApacheConnectorConfiguration<A extends ApacheConnectorConfiguration<A>> extends ConnectorConfiguration<A> {
    private static final String JERSEY_REQUEST_ATTR_NAME = "JerseyRequestAttribute";

    /* package */ Ref<CookieStore> cookieStore = NullableRef.empty();
    /* package */ Ref<HttpClientConnectionManager> connectionManager = NullableRef.empty();
    /* package */ Ref<Boolean> connectionManagerShared = NullableRef.empty();
    /* package */ Ref<ConnectionReuseStrategy> connectionReuseStrategy = NullableRef.empty();
    /* package */ Ref<ApacheConnectionClosingStrategy> connectionClosingStrategy = NullableRef.empty();
    /* package */ Ref<CredentialsProvider> credentialsProvider = NullableRef.empty();
    /* package */ Ref<Boolean> disableCookies = NullableRef.empty();
    /* package */ Ref<ConnectionKeepAliveStrategy> keepAliveStrategy = NullableRef.empty();
    /* package */ Ref<Boolean> preemptiveBasicAuthentication = NullableRef.empty();
    /* package */ Ref<RequestConfig> requestConfig = NullableRef.empty();
    /* package */ Ref<HttpRequestRetryHandler> httpRequestRetryHandler = NullableRef.empty();
    /* package */ Ref<Boolean> useSystemProperties = NullableRef.empty();

    /**
     * <p>
     *   Connection Manager which will be used to create {@link org.apache.http.client.HttpClient}.
     * </p>
     * <p>
     *   If the property is absent a default Connection Manager will be used
     *   ({@link org.apache.http.impl.conn.BasicHttpClientConnectionManager}).
     *   If you want to use this client in multithreaded environment, be sure you override default value with
     *   {@link org.apache.http.impl.conn.PoolingHttpClientConnectionManager} instance.
     * </p>
     * <p>
     *   The property {@link ApacheClientProperties#CONNECTION_MANAGER} takes precedence over this configuration.
     * </p>
     *
     * @param httpClientConnectionManager the client connection manager used to create the
     * {@link org.apache.http.client.HttpClient}.
     * @return updated configuration.
     */
    public A connectionManager(HttpClientConnectionManager httpClientConnectionManager) {
        connectionManager.set(httpClientConnectionManager);
        return self();
    }

    /**
     * <p>
     *     A value of {@code true} indicates that configured connection manager should be shared
     *     among multiple Jersey {@code ClientRuntime} instances. It means that closing
     *     a particular {@code ClientRuntime} instance does not shut down the underlying
     *     connection manager automatically. In such case, the connection manager life-cycle
     *     should be fully managed by the application code. To release all allocated resources,
     *     caller code should especially ensure {@link org.apache.http.conn.HttpClientConnectionManager#shutdown()} gets
     *     invoked eventually.
     * </p>
     * <p>
     *     The default value is {@code false}.
     * </p>
     * <p>
     *     The property {@link ApacheClientProperties#CONNECTION_MANAGER_SHARED} takes precedence over this configuration.
     * </p>
     *
     * @param shared enable or disable sharing among multiple Jersey {@code ClientRuntime} instances.
     * @return updated configuration.
     */
    public A connectionManagerShared(boolean shared) {
        this.connectionManagerShared.set(shared);
        return self();
    }

    /**
     * <p>
     *     The credential provider that should be used to retrieve
     *     credentials from a user. Credentials needed for proxy authentication
     *     are stored here as well.
     * </p>
     * <p>
     *     If the property is absent a default provider will be used.
     * </p>
     * <p>
     *     The property {@link ApacheClientProperties#CREDENTIALS_PROVIDER} takes precedence over this configuration.
     * </p>
     *
     * @param credentialsProvider the credential provider.
     * @return updated configuration.
     */
    public A credentialsProvider(CredentialsProvider credentialsProvider) {
        this.credentialsProvider.set(credentialsProvider);
        return self();
    }

    /**
     * <p>
     *     A value of {@code false} indicates the client should handle cookies
     *     automatically using HttpClient's default cookie policy. A value
     *     of {@code true} will cause the client to ignore all cookies.
     * </p>
     * <p>
     *     The default value is {@code false}.
     * </p>
     * <p>
     *     The property {@link ApacheClientProperties#DISABLE_COOKIES} takes precedence over this configuration.
     * </p>
     *
     * @param disable whether to disable and ignore the cookies.
     * @return updated configuration.
     */
    public A disableCookies(boolean disable) {
        this.disableCookies.set(disable);
        return self();
    }

    /**
     * <p>
     *     Set connection keep alive strategy for the {@link org.apache.http.client.HttpClient}.
     * </p>
     * <p>
     *     If the property is absent the default keep alive strategy of the Apache HTTP library will be used.
     * </p>
     * <p>
     *     The property {@link ApacheClientProperties#KEEPALIVE_STRATEGY} takes precedence over this configuration.
     * </p>
     *
     * @param keepAliveStrategy the keep alive strategy.
     * @return updated configuration.
     */
    public A keepAliveStrategy(ConnectionKeepAliveStrategy keepAliveStrategy) {
        this.keepAliveStrategy.set(keepAliveStrategy);
        return self();
    }

    /**
     * <p>
     *      A value of {@code true} indicates that a client should send an
     *      authentication request even before the server gives a 401
     *      response.
     * </p>
     * <p>
     *     The default value is {@code false}.
     * </p>
     * <p>
     *     The property {@link ApacheClientProperties#PREEMPTIVE_BASIC_AUTHENTICATION} takes precedence over this configuration.
     * </p>
     *
     * @param enable indicate whether a client should send authentication request even before the server gives a 401
     *      response.
     * @return updated configuration.
     */
    public A preemptiveBasicAuthentication(boolean enable) {
        this.preemptiveBasicAuthentication.set(enable);
        return self();
    }

    /**
     * <p>
     *     Request configuration for the {@link org.apache.http.client.HttpClient}.
     *     Http parameters which will be used to create {@link org.apache.http.client.HttpClient}.
     * </p>
     * <p>
     *     If the property is absent the default request configuration will be used.
     * </p>
     * <p>
     *     The property {@link ApacheClientProperties#REQUEST_CONFIG} takes precedence over this configuration.
     * </p>
     *
     * @param requestConfig the apache request config.
     * @return updated configuration.
     */
    public A requestConfig(RequestConfig requestConfig) {
        this.requestConfig.set(requestConfig);
        return self();
    }

    /**
     * <p>
     *     HttpRequestRetryHandler which will be used to create {@link org.apache.http.client.HttpClient}.
     * </p>
     * <p>
     *     If the property is absent a default retry handler will be used
     *     ({@link org.apache.http.impl.client.DefaultHttpRequestRetryHandler}).
     * </p>
     * <p>
     *     The property {@link ApacheClientProperties#RETRY_HANDLER} takes precedence over this configuration.
     * </p>
     *
     * @param retryHandler the HTTP request retry handler.
     * @return updated configuration.
     */
    public A retryHandler(HttpRequestRetryHandler retryHandler) {
        this.httpRequestRetryHandler.set(retryHandler);
        return self();
    }

    /**
     * <p>
     *     Set the connection reuse strategy for the {@link org.apache.http.client.HttpClient}.
     * </p>
     * <p>
     *     If the property is absent the default reuse strategy of the Apache HTTP library will be used.
     * </p>
     * <p>
     *     The property {@link ApacheClientProperties#REUSE_STRATEGY} takes precedence over this configuration.
     * </p>
     *
     * @param reuseStrategy the connection reuse strategy.
     * @return updated configuration.
     */
    public A reuseStrategy(ConnectionReuseStrategy reuseStrategy) {
        this.connectionReuseStrategy.set(reuseStrategy);
        return self();
    }

    /**
     * <p>
     *     A value of {@code false} indicates the client will use default ApacheConnector params. A value
     *     of {@code true} will cause the client to take into account the system properties
     *     {@code https.protocols}, {@code https.cipherSuites}, {@code http.keepAlive},
     *     {@code http.maxConnections}.
     * </p>
     * <p>
     *     Default value is {@code false}.
     * </p>
     * <p>
     *     The property {@link ApacheClientProperties#USE_SYSTEM_PROPERTIES} takes precedence over this configuration.
     * </p>
     *
     * @param enable enable or disable usage of the system properties.
     * @return updated configuration.
     */
    public A useSystemProperties(boolean enable) {
        this.useSystemProperties.set(enable);
        return self();
    }

    /* package */ ApacheConnectorConfiguration.ReadWrite rw() {
        final ApacheConnectorConfiguration.ReadWrite readWrite = this instanceof ApacheConnectorConfiguration.ReadWrite
                ? ((ApacheConnectorConfiguration.ReadWrite) this).instance()
                : new ApacheConnectorConfiguration.ReadWrite();
        readWrite.setNonEmpty(this);
        return readWrite;
    }


    protected static class ReadWrite
            extends ApacheConnectorConfiguration<ReadWrite>
            implements ConnectorConfiguration.Read<ReadWrite> {

        @Override
        public <X extends ConnectorConfiguration<?>> void setNonEmpty(X otherC) {
            ApacheConnectorConfiguration<?> other = (ApacheConnectorConfiguration<?>) otherC;
            Read.super.setNonEmpty(other);

            ((NullableRef<CookieStore>) this.cookieStore)
                    .setNonEmpty((NullableRef<CookieStore>) other.cookieStore);
            ((NullableRef<HttpClientConnectionManager>) this.connectionManager)
                    .setNonEmpty((NullableRef<HttpClientConnectionManager>) other.connectionManager);
            ((NullableRef<Boolean>) this.connectionManagerShared)
                    .setNonEmpty((NullableRef<Boolean>) other.connectionManagerShared);
            ((NullableRef<ConnectionReuseStrategy>) this.connectionReuseStrategy).setNonEmpty(
                    (NullableRef<ConnectionReuseStrategy>) other.connectionReuseStrategy);
            ((NullableRef<CredentialsProvider>) this.credentialsProvider).setNonEmpty(
                    (NullableRef<CredentialsProvider>) other.credentialsProvider);
            ((NullableRef<Boolean>) this.disableCookies).setNonEmpty(
                    (NullableRef<Boolean>) other.disableCookies);
            ((NullableRef<ConnectionKeepAliveStrategy>) this.keepAliveStrategy).setNonEmpty(
                    (NullableRef<ConnectionKeepAliveStrategy>) other.keepAliveStrategy);
            ((NullableRef<Boolean>) this.preemptiveBasicAuthentication).setNonEmpty(
                    (NullableRef<Boolean>) other.preemptiveBasicAuthentication);
            ((NullableRef<RequestConfig>) this.requestConfig).setNonEmpty(
                    (NullableRef<RequestConfig>) other.requestConfig);
            ((NullableRef<HttpRequestRetryHandler>) this.httpRequestRetryHandler).setNonEmpty(
                    (NullableRef<HttpRequestRetryHandler>) other.httpRequestRetryHandler);
            ((NullableRef<Boolean>) this.useSystemProperties).setNonEmpty(
                    (NullableRef<Boolean>) other.useSystemProperties);
        }

        @Override
        public ReadWrite init() {
            connectTimeout.ifEmptySet(-1);
            readTimeout.ifEmptySet(-1);
            Read.super.init();
            ((NullableRef<Boolean>) connectionManagerShared).ifEmptySet(false);
            ((NullableRef<Boolean>) disableCookies).ifEmptySet(false);
            ((NullableRef<Boolean>) preemptiveBasicAuthentication).ifEmptySet(false);
            ((NullableRef<Boolean>) useSystemProperties).ifEmptySet(false);
            return self();
        }

        ReadWrite fromClient(Client client, Configuration configuration) {
            final ReadWrite clientConfiguration = copyFromClient(configuration);
            clientConfiguration.chunkSize(configuration.getProperties());
            return fromClient(client, configuration, clientConfiguration);
        }

        private static ReadWrite fromClient(Client client, Configuration configuration, ReadWrite rw) {
            final Map<String, Object> properties = configuration.getProperties();

            final Object connectionManager = properties.get(rw.prefixed(ApacheClientProperties.CONNECTION_MANAGER));
            if (connectionManager != null) {
                if (!(connectionManager instanceof HttpClientConnectionManager)) {
                    LOGGER.log(
                            Level.WARNING,
                            org.glassfish.jersey.apache.connector.LocalizationMessages.IGNORING_VALUE_OF_PROPERTY(
                                    rw.prefixed(ApacheClientProperties.CONNECTION_MANAGER),
                                    connectionManager.getClass().getName(),
                                    HttpClientConnectionManager.class.getName())
                    );
                } else {
                    rw.connectionManager.set((HttpClientConnectionManager) connectionManager);
                }
            }

            final Object keepAliveStrategy = properties.get(rw.prefixed(ApacheClientProperties.KEEPALIVE_STRATEGY));
            if (keepAliveStrategy != null) {
                if (!(keepAliveStrategy instanceof ConnectionKeepAliveStrategy)) {
                    LOGGER.log(
                            Level.WARNING,
                            org.glassfish.jersey.apache.connector.LocalizationMessages.IGNORING_VALUE_OF_PROPERTY(
                                    rw.prefixed(ApacheClientProperties.KEEPALIVE_STRATEGY),
                                    keepAliveStrategy.getClass().getName(),
                                    ConnectionKeepAliveStrategy.class.getName())
                    );
                } else {
                    rw.keepAliveStrategy.set((ConnectionKeepAliveStrategy) keepAliveStrategy);
                }
            }

            final Object reuseStrategy = properties.get(rw.prefixed(ApacheClientProperties.REUSE_STRATEGY));
            if (reuseStrategy != null) {
                if (!(reuseStrategy instanceof ConnectionReuseStrategy)) {
                    LOGGER.log(
                            Level.WARNING,
                            org.glassfish.jersey.apache.connector.LocalizationMessages.IGNORING_VALUE_OF_PROPERTY(
                                    rw.prefixed(ApacheClientProperties.REUSE_STRATEGY),
                                    reuseStrategy.getClass().getName(),
                                    ConnectionReuseStrategy.class.getName())
                    );
                } else {
                    rw.reuseStrategy((ConnectionReuseStrategy) reuseStrategy);
                }
            }

            final Object reqConfig = properties.get(rw.prefixed(ApacheClientProperties.REQUEST_CONFIG));
            if (reqConfig != null) {
                if (!(reqConfig instanceof RequestConfig)) {
                    LOGGER.log(
                            Level.WARNING,
                            LocalizationMessages.IGNORING_VALUE_OF_PROPERTY(
                                    rw.prefixed(ApacheClientProperties.REQUEST_CONFIG),
                                    reqConfig.getClass().getName(),
                                    RequestConfig.class.getName())
                    );
                } else {
                    rw.requestConfig.set((RequestConfig) reqConfig);
                }
            }

            if (properties.containsKey(rw.prefixed(ApacheClientProperties.USE_SYSTEM_PROPERTIES))) {
                rw.useSystemProperties.set(
                        PropertiesHelper.isProperty(properties.get(rw.prefixed(ApacheClientProperties.USE_SYSTEM_PROPERTIES)))
                );
            }
            return rw;
        }

        @Override
        protected ReadWrite chunkSize(Map<String, Object> properties) {
            return super.chunkSize(properties);
        }

        /* package */ boolean connectionManagerShared(Map<String, Object> properties) {
            connectionManagerShared.set(
                    PropertiesHelper.getValue(properties, prefixed(ApacheClientProperties.CONNECTION_MANAGER_SHARED),
                            connectionManagerShared.get(), null)
            );
            return connectionManagerShared.get();
        }

        /* package */ CredentialsProvider credentialsProvider(Map<String, Object> properties) {
            final Object credentialsProvider = properties.get(prefixed(ApacheClientProperties.CREDENTIALS_PROVIDER));
            if ((credentialsProvider instanceof CredentialsProvider)) {
                credentialsProvider((CredentialsProvider) credentialsProvider);
            }
            return this.credentialsProvider.get();
        }

        /* package */ CredentialsProvider credentialsProvider(ClientRequest clientRequest) {
            final CredentialsProvider credentialsProvider = clientRequest.resolveProperty(
                    prefixed(ApacheClientProperties.CREDENTIALS_PROVIDER), CredentialsProvider.class);
            if (credentialsProvider != null) {
                this.credentialsProvider.set(credentialsProvider);
            }
            return this.credentialsProvider.get();
        }

        /* package */ Boolean disableCookies(Map<String, Object> properties) {
            Object property = properties.get(prefixed(ApacheClientProperties.DISABLE_COOKIES));
            if (property != null) {
                this.disableCookies.set(PropertiesHelper.isProperty(property));
            }
            return disableCookies.get();
        }

        /* package */ boolean preemptiveBasicAuthentication(Map<String, Object> properties) {
            final Boolean preemptiveBasicAuthProperty = (Boolean) properties.get(
                    prefixed(ApacheClientProperties.PREEMPTIVE_BASIC_AUTHENTICATION));
            if (preemptiveBasicAuthProperty != null) {
                preemptiveBasicAuthentication(preemptiveBasicAuthProperty);
            }
            return preemptiveBasicAuthentication.get();
        }

        /* package */ HttpRequestRetryHandler retryHandler(Map<String, Object> properties) {
            final Object retryHandler = properties.get(prefixed(ApacheClientProperties.RETRY_HANDLER));
            if ((retryHandler instanceof HttpRequestRetryHandler)) {
                this.retryHandler((HttpRequestRetryHandler) retryHandler);
            }
            return this.httpRequestRetryHandler.get();
        }

        @Override
        public ReadWrite instance() {
            return new ReadWrite();
        }

        @Override
        public ReadWrite me() {
            return this;
        }
    }
}
