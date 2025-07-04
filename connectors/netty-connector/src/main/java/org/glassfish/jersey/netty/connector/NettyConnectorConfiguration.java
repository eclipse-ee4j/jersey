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

package org.glassfish.jersey.netty.connector;

import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.proxy.HttpProxyHandler;
import io.netty.handler.proxy.ProxyHandler;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.ClientRequest;
import org.glassfish.jersey.client.innate.ClientProxy;
import org.glassfish.jersey.internal.util.collection.Ref;
import org.glassfish.jersey.client.innate.ConnectorConfiguration;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Map;

class NettyConnectorConfiguration<N extends NettyConnectorConfiguration<N>> extends ConnectorConfiguration<N> {

    /* package */ final NullableRef<NettyConnectionController> connectionController = NullableRef.empty();
    /* package */ final NullableRef<Boolean> enableHostnameVerification = NullableRef.of(Boolean.TRUE);
    /* package */ final Ref<Integer> expect100ContTimeout = NullableRef.of(
                                                                NettyClientProperties.DEFAULT_EXPECT_100_CONTINUE_TIMEOUT_VALUE);
    /* package */ final NullableRef<Boolean> filterHeadersForProxy = NullableRef.of(Boolean.TRUE);
    /* package */ final NullableRef<Integer> firstHttpHeaderLineLength = NullableRef.of(
                                                                NettyClientProperties.DEFAULT_INITIAL_LINE_LENGTH);
    /* package */ final NullableRef<Integer> maxChunkSize = NullableRef.of(NettyClientProperties.DEFAULT_CHUNK_SIZE);
    /* package */ final NullableRef<Integer> maxHeaderSize = NullableRef.of(NettyClientProperties.DEFAULT_HEADER_SIZE);
    // either from Jersey config, or default
    /* package */ final Ref<Integer> maxPoolSizeTotal = NullableRef.of(DEFAULT_MAX_POOL_SIZE_TOTAL);
    // either from Jersey config, or default
    /* package */ final Ref<Integer> maxPoolIdle = NullableRef.of(DEFAULT_MAX_POOL_IDLE);
    // either from system property, or from Jersey config, or default
    /* package */ final Ref<Integer> maxPoolSize = NullableRef.of(HTTP_KEEPALIVE ? MAX_POOL_SIZE : DEFAULT_MAX_POOL_SIZE);
    /* package */ final Ref<Integer> maxRedirects = NullableRef.of(DEFAULT_MAX_REDIRECTS);
    /* package */ final NullableRef<Boolean> preserveMethodOnRedirect = NullableRef.of(Boolean.TRUE);
    /* package */ final NullableRef<NettyHttpRedirectController> redirectController = NullableRef.empty();

    // If HTTP keepalive is enabled the value of "http.maxConnections" determines the maximum number
    // of idle connections that will be simultaneously kept alive, per destination.
    private static final String HTTP_KEEPALIVE_STRING = System.getProperty("http.keepAlive");
    // http.keepalive (default: true)
    private static final Boolean HTTP_KEEPALIVE =
            HTTP_KEEPALIVE_STRING == null ? Boolean.TRUE : Boolean.parseBoolean(HTTP_KEEPALIVE_STRING);

    // http.maxConnections (default: 5)
    private static final int DEFAULT_MAX_POOL_SIZE = 5;
    private static final int MAX_POOL_SIZE = Integer.getInteger("http.maxConnections", DEFAULT_MAX_POOL_SIZE);
    private static final int DEFAULT_MAX_POOL_IDLE = 60; // seconds
    private static final int DEFAULT_MAX_POOL_SIZE_TOTAL = 60; // connections

    private static final int DEFAULT_MAX_REDIRECTS = 5;

    @Override
    protected <X extends ConnectorConfiguration<?>> void setNonEmpty(X otherCC) {
        NettyConnectorConfiguration<?> other = (NettyConnectorConfiguration<?>) otherCC;
        super.setNonEmpty(other);
        this.connectionController.setNonEmpty(other.connectionController);
        this.redirectController.setNonEmpty(other.redirectController);
        this.connectionController.setNonEmpty(other.connectionController);
        this.enableHostnameVerification.setNonEmpty(other.enableHostnameVerification);
        ((NullableRef<Integer>) this.expect100ContTimeout).setNonEmpty((NullableRef<Integer>) other.expect100ContTimeout);
        this.filterHeadersForProxy.setNonEmpty(other.filterHeadersForProxy);
        this.firstHttpHeaderLineLength.setNonEmpty(other.firstHttpHeaderLineLength);
        this.maxChunkSize.setNonEmpty(other.maxChunkSize);
        this.maxHeaderSize.setNonEmpty(other.maxHeaderSize);
        ((NullableRef<Integer>) this.maxPoolIdle).setNonEmpty((NullableRef<Integer>) other.maxPoolIdle);
        ((NullableRef<Integer>) this.maxPoolSize).setNonEmpty((NullableRef<Integer>) other.maxPoolSize);
        ((NullableRef<Integer>) this.maxPoolSizeTotal).setNonEmpty((NullableRef<Integer>) other.maxPoolSizeTotal);
        ((NullableRef<Integer>) this.maxRedirects).setNonEmpty((NullableRef<Integer>) other.maxRedirects);
        this.preserveMethodOnRedirect.setNonEmpty(other.preserveMethodOnRedirect);
        this.redirectController.setNonEmpty(other.redirectController);
    }

    /**
     * Set the connection pooling controller for the Netty Connector.
     *
     * @param controller the connection pooling controller.
     * @return updated configuration.
     */
    public N connectionController(NettyConnectionController controller) {
        connectionController.set(controller);
        return self();
    }

    /**
     * This setting determines waiting time in milliseconds for 100-Continue response when 100-Continue is sent by the client.
     * The property {@link NettyClientProperties#EXPECT_100_CONTINUE_TIMEOUT} has precedence over this setting.
     *
     * @param millis the timeout for 100-Continue response.
     * @return updated configuration.
     */
    public N expect100ContinueTimeout(int millis) {
        expect100ContTimeout.set(millis);
        return self();
    }

    /**
     * Enable or disable the endpoint identification algorithm to HTTPS. The property
     * {@link NettyClientProperties#ENABLE_SSL_HOSTNAME_VERIFICATION} has  over this setting.
     *
     * @param enable enable or disable the hostname verification.
     * @return updated configuration.
     */
    public N enableSslHostnameVerification(boolean enable) {
        enableHostnameVerification.set(enable);
        return self();
    }

    /**
     * Filter the HTTP headers for requests (CONNECT) towards the proxy except for PROXY-prefixed
     * and HOST headers when {@code true}.
     * The property {@link NettyClientProperties#FILTER_HEADERS_FOR_PROXY} has precedence over this setting.
     *
     * @param filter to filter or not. The default is {@code true}.
     * @return updated configuration.
     */
    public N filterHeadersForProxy(boolean filter) {
        filterHeadersForProxy.set(filter);
        return self();
    }

    /**
     * This property determines the number of seconds the idle connections are kept in the pool before pruned.
     * The default is 60. Specify 0 to disable. The property {@link NettyClientProperties#IDLE_CONNECTION_PRUNE_TIMEOUT}
     * has precedence over this setting.
     *
     * @param seconds the timeout in seconds.
     * @return updated configuration.
     */
    public N idleConnectionPruneTimeout(int seconds) {
        maxPoolIdle.set(seconds);
        return self();
    }

    /**
     * Set the maximum length of the first line of the HTTP header.
     * The property {@link NettyClientProperties#MAX_INITIAL_LINE_LENGTH} has precedence over this setting.
     *
     * @param length the length of the first line of the HTTP header.
     * @return updated configuration.
     */
    public N initialHttpHeaderLineLength(int length) {
        firstHttpHeaderLineLength.set(length);
        return self();
    }

    /**
     * Set the maximum chunk size for the Netty connector. The property {@link NettyClientProperties#MAX_CHUNK_SIZE}
     * has precedence over this setting.
     *
     * @param size the new size of chunks.
     * @return updated configuration.
     */
    public N maxChunkSize(int size) {
        maxChunkSize.set(size);
        return self();
    }

    /**
     * This setting determines the maximum number of idle connections that will be simultaneously kept alive, per destination.
     * The default is 5. The property {@link NettyClientProperties#MAX_CONNECTIONS} takes precedence over this setting.
     *
     * @param maxCount maximum number of idle connections per destination.
     * @return updated configuration.
     */
    public N maxConnectionsPerDestination(int maxCount) {
        maxPoolSize.set(maxCount);
        return self();
    }

    /**
     * Set the maximum header size in bytes for the HTTP headers processed by Netty.
     * The property {@link NettyClientProperties#MAX_HEADER_SIZE} has precedence over this setting.
     *
     * @param size the new maximum header size.
     * @return updated configuration.
     */
    public N maxHeaderSize(int size) {
        maxHeaderSize.set(size);
        return self();
    }

    /**
     * Set the maximum number of redirects to prevent infinite redirect loop. The default is 5.
     * The property {@link NettyClientProperties#MAX_REDIRECTS} has precedence over this setting.
     *
     * @param max the maximum number of redirects.
     * @return updated configuration.
     */
    public N maxRedirects(int max) {
        maxRedirects.set(max);
        return self();
    }

    /**
     * Set the maximum number of idle connections that will be simultaneously kept alive. The property
     * {@link NettyClientProperties#MAX_CONNECTIONS_TOTAL} has precedence over this setting.
     *
     * @param max the maximum number of idle connections.
     * @return updated configuration.
     */
    public N maxTotalConnections(int max) {
        maxPoolSizeTotal.set(max);
        return self();
    }

    /**
     * Set the preservation of methods during HTTP redirect.
     * By default, the HTTP POST request are not transformed into HTTP GET for status 301 & 302.
     * The property {@link NettyClientProperties#PRESERVE_METHOD_ON_REDIRECT} has precedence over this setting.
     *
     * @param preserve to preserve or not to preserve.
     * @return updated configuration.
     */
    public N preserveMethodOnRedirect(boolean preserve) {
        preserveMethodOnRedirect.set(preserve);
        return self();
    }

    /**
     * Set the Netty Connector HTTP redirect controller.
     * The property {@link NettyClientProperties#HTTP_REDIRECT_CONTROLLER} has precedence over this setting.
     *
     * @param controller the HTTP redirect controller.
     * @return updated configuration.
     */
    public N redirectController(NettyHttpRedirectController controller) {
        redirectController.set(controller);
        return self();
    }

    @SuppressWarnings("unchecked")
    protected N self() {
        return (N) this;
    }

    abstract static class ReadWrite<N extends ReadWrite<N>>
            extends NettyConnectorConfiguration<N>
            implements ConnectorConfiguration.Read<N> {

        /**
         * Get the preset {@link NettyConnectionController} or create an instance of the default one, if not preset.
         * @return the {@link NettyConnectionController} instance.
         */
        /* package */ NettyConnectionController connectionController() {
            return connectionController.isPresent() ? connectionController.get() : new NettyConnectionController();
        }

        /**
         * Update {@link #expect100ContinueTimeout(int) expect 100-Continue timeout} based on current http request properties.
         *
         * @param clientRequest the current http client request.
         * @return updated configuration.
         */
        /* package */ N expect100ContinueTimeout(ClientRequest clientRequest) {
            expect100ContTimeout.set(
                    clientRequest.resolveProperty(
                            prefixed(NettyClientProperties.EXPECT_100_CONTINUE_TIMEOUT), expect100ContTimeout.get()));
            return this.self();
        }

        /**
         * Return value of {@link #enableSslHostnameVerification(boolean)} setting either from the configuration of from the
         * HTTP client request properties. The default is {@code true}.
         *
         * @param properties the HTTP client request properties.
         * @return the value of SSL hostname verification setting.
         */
        /* package */ boolean isSslHostnameVerificationEnabled(Map<String, Object> properties) {
            return ClientProperties.getValue(properties,
                    prefixed(NettyClientProperties.ENABLE_SSL_HOSTNAME_VERIFICATION),
                    enableHostnameVerification.get());
        }

        /**
         * Update {@link #maxRedirects(int)} value from the HTTP Client request.
         * @param request the HTTP Client request.
         * @return maximum redirects value.
         */
        /* package */ int maxRedirects(ClientRequest request) {
            maxRedirects.set(
                    request.resolveProperty(prefixed(NettyClientProperties.MAX_REDIRECTS), maxRedirects.get()));
            return maxRedirects.get();
        }

        /**
         * Update the {@link #preserveMethodOnRedirect(boolean) preservation} of HTTP method during HTTP redirect
         * by HTTP client request properties.
         *
         * @param request HTTP client request.
         * @return the value of preservation.
         */
        /* package */ boolean preserveMethodOnRedirect(ClientRequest request) {
            preserveMethodOnRedirect.set(
                    request.resolveProperty(
                            prefixed(NettyClientProperties.PRESERVE_METHOD_ON_REDIRECT), preserveMethodOnRedirect.get()));
            return preserveMethodOnRedirect.get();
        }

        /**
         * Get the instance of preset {@link NettyHttpRedirectController} either from configuration,
         * or from the HTTP client request, or the default if non set.
         * @param request the HTTP client request.
         * @return an instance of {@link NettyHttpRedirectController}.
         */
        /* package */ NettyHttpRedirectController redirectController(ClientRequest request) {
            NettyHttpRedirectController customRedirectController =
                    request.resolveProperty(
                            prefixed(NettyClientProperties.HTTP_REDIRECT_CONTROLLER), NettyHttpRedirectController.class);
            if (customRedirectController == null) {
                customRedirectController = redirectController.get();
            }
            if (customRedirectController == null) {
                customRedirectController = new NettyHttpRedirectController();
            }

            return customRedirectController;
        }

        /**
         * <p>
         *  Return a new instance of configuration updated by the merged settings from this and client properties.
         *  Only properties unresolved during the request are updated.
         * </p><p>
         *  {@code This} is meant to be settings from the connector.
         *  The priorities should go DEFAULTS -> CONNECTOR -> CLIENT -> REQUEST.
         * </p>
         *
         * @param client the REST client.
         * @return a new instance of configuration.
         */
        /* package */ N fromClient(Client client) {
            final Map<String, Object> properties = client.getConfiguration().getProperties();
            final N clientConfiguration = copy();
            Object configProp = properties.get(prefixed(ClientProperties.CONNECTOR_CONFIGURATION));
            if (configProp != null) {
                NettyConnectorConfiguration<?> nettyCfg = (NettyConnectorConfiguration<?>) configProp;
                if (prefix.equals(nettyCfg.prefix) || "".equals(nettyCfg.prefix.get())) {
                    clientConfiguration.setNonEmpty(nettyCfg);
                    clientConfiguration.prefix(prefix.get());
                }
            } else {
                configProp = properties.get(ClientProperties.CONNECTOR_CONFIGURATION);
                if (configProp != null && prefix.equals(((NettyConnectorConfiguration<?>) configProp).prefix)) {
                    clientConfiguration.setNonEmpty((NettyConnectorConfiguration<?>) configProp);
                }
            }

            final Object threadPoolSize = properties.get(prefixed(ClientProperties.ASYNC_THREADPOOL_SIZE));
            if (threadPoolSize instanceof Integer && (Integer) threadPoolSize > 0) {
                clientConfiguration.asyncThreadPoolSize((Integer) threadPoolSize);
            }

            final Object maxPoolSizeTotalProperty = properties.get(prefixed(NettyClientProperties.MAX_CONNECTIONS_TOTAL));
            final Object maxPoolIdleProperty = properties.get(prefixed(NettyClientProperties.IDLE_CONNECTION_PRUNE_TIMEOUT));
            final Object maxPoolSizeProperty = properties.get(prefixed(NettyClientProperties.MAX_CONNECTIONS));

            if (maxPoolSizeTotalProperty != null) {
                clientConfiguration.maxPoolSizeTotal.set((Integer) maxPoolSizeTotalProperty);
            }

            if (maxPoolIdleProperty != null) {
                clientConfiguration.maxPoolIdle.set((Integer) maxPoolIdleProperty);
            }

            if (maxPoolSizeProperty != null) {
                clientConfiguration.maxPoolSize.set((Integer) maxPoolSizeProperty);
            }

            if (clientConfiguration.maxPoolSizeTotal.get() < 0) {
                throw new ProcessingException(LocalizationMessages.WRONG_MAX_POOL_TOTAL(maxPoolSizeTotal.get()));
            }

            if (clientConfiguration.maxPoolSize.get() < 0) {
                throw new ProcessingException(LocalizationMessages.WRONG_MAX_POOL_SIZE(maxPoolSize.get()));
            }

            return clientConfiguration;
        }

        /**
         * <p>
         *  Return a new instance of configuration updated by the merged settings from this and HTTP client request properties.
         *  Only properties unresolved during the request are updated.
         * </p><p>
         *  {@code This} is meant to be settings from the connector.
         *  The priorities should go DEFAULTS -> CONNECTOR -> CLIENT -> REQUEST.
         * </p>

         * @param request the HTTP client request.
         * @return a new instance of configuration.
         */
        /* package */ N fromRequest(ClientRequest request) {
            final N requestConfiguration = copy();
            Object configProp = request.getProperty(prefixed(ClientProperties.CONNECTOR_CONFIGURATION));
            if (configProp != null) {
                NettyConnectorConfiguration<?> nettyCfg = (NettyConnectorConfiguration<?>) configProp;
                if (prefix.equals(nettyCfg.prefix) || "".equals(nettyCfg.prefix.get())) {
                    requestConfiguration.setNonEmpty(nettyCfg);
                    requestConfiguration.prefix(prefix.get());
                }
            } else {
                configProp = request.getProperty(ClientProperties.CONNECTOR_CONFIGURATION);
                if (configProp != null && prefix.equals(((NettyConnectorConfiguration<?>) configProp).prefix)) {
                    requestConfiguration.setNonEmpty((NettyConnectorConfiguration<?>) configProp);
                }
            }

            return requestConfiguration;
        }


        /**
         * Create an instance of {@link HttpClientCodec} based on preset settings {@link #initialHttpHeaderLineLength(int)},
         * {@link #maxHeaderSize} and {@link #maxChunkSize}. The settings can be preset in the configuration or
         * on the HTTP client request.
         *
         * @param properties The HTTP client request properties.
         * @return the {@link HttpClientCodec} instance.
         */
        /* package */ HttpClientCodec createHttpClientCodec(Map<String, Object> properties) {
            firstHttpHeaderLineLength.set(ClientProperties.getValue(properties,
                    prefixed(NettyClientProperties.MAX_INITIAL_LINE_LENGTH), firstHttpHeaderLineLength.get()));
            maxHeaderSize.set(
                    ClientProperties.getValue(properties, prefixed(NettyClientProperties.MAX_HEADER_SIZE), maxHeaderSize.get()));
            maxChunkSize.set(
                    ClientProperties.getValue(properties, prefixed(NettyClientProperties.MAX_CHUNK_SIZE), maxChunkSize.get()));

            return new HttpClientCodec(firstHttpHeaderLineLength.get(), maxHeaderSize.get(), maxChunkSize.get());
        }

        /**
         * Create an instance of {@link ProxyHandler} based on HTTP request URI's port and address,
         * the preset proxy {@link #proxyUri(URI) uri}, {@link #proxyUserName(String) username},
         * and {@link #proxyPassword(String) password}.
         *
         * Can filter headers {@link #filterHeadersForProxy(boolean)}.
         *
         * @param clientProxy the Jersey {@link ClientProxy} instance.
         * @param jerseyRequest the HTTP client request containing HTTP headers to be filtered.
         * @return a Netty {@link ProxyHandler} instance.
         */
        /* package */ ProxyHandler createProxyHandler(ClientProxy clientProxy, ClientRequest jerseyRequest) {
            final URI u = clientProxy.uri();
            InetSocketAddress proxyAddr = new InetSocketAddress(u.getHost(), u.getPort() == -1 ? 8080 : u.getPort());

            filterHeadersForProxy.set(jerseyRequest
                    .resolveProperty(prefixed(NettyClientProperties.FILTER_HEADERS_FOR_PROXY), filterHeadersForProxy.get()));
            HttpHeaders httpHeaders = NettyConnector.setHeaders(
                    jerseyRequest, new DefaultHttpHeaders(), Boolean.TRUE.equals(filterHeadersForProxy.get()));

            ProxyHandler proxy = clientProxy.userName() == null ? new HttpProxyHandler(proxyAddr, httpHeaders)
                    : new HttpProxyHandler(proxyAddr, clientProxy.userName(), clientProxy.password(), httpHeaders);
            if (connectTimeout.get() > 0) {
                proxy.setConnectTimeoutMillis(connectTimeout.get());
            }

            return proxy;
        }

        @Override
        public abstract N self();
    }

}
