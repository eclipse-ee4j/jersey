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

package org.glassfish.jersey.netty.connector;

import org.glassfish.jersey.internal.util.PropertiesClass;

/**
 * Configuration options specific to the Client API that utilizes {@link NettyConnectorProvider}.
 *
 * @since 2.32
 */
@PropertiesClass
public class NettyClientProperties {

    /**
     * <p>
     *     Sets the endpoint identification algorithm to HTTPS.
     * </p>
     * <p>
     *     The default value is {@code true} (for HTTPS uri scheme).
     * </p>
     * <p>
     *     The name of the configuration property is <tt>{@value}</tt>.
     * </p>
     * @since 2.35
     * @see javax.net.ssl.SSLParameters#setEndpointIdentificationAlgorithm(String)
     */
    public static final String ENABLE_SSL_HOSTNAME_VERIFICATION = "jersey.config.client.tls.enableHostnameVerification";

    /**
     * <p>
     *     Filter the HTTP headers for requests (CONNECT) towards the proxy except for PROXY-prefixed and HOST headers when {@code true}.
     * </p>
     * <p>
     *     The default value is {@code true} and the headers are filtered out.
     * </p>
     * <p>
     *     The name of the configuration property is <tt>{@value}</tt>.
     * </p>
     * @since 2.41
     */
    public static final String FILTER_HEADERS_FOR_PROXY = "jersey.config.client.filter.headers.proxy";

    /**
     * <p>
     *     The implementation of custom {@link NettyHttpRedirectController} redirect logic.
     * </p>
     *
     * @since 2.47
     */
    public static final String HTTP_REDIRECT_CONTROLLER = "jersey.config.client.netty.http.redirect.controller";

    /**
     * <p>
     *    This property determines the number of seconds the idle connections are kept in the pool before pruned.
     *    The default is 60. Specify 0 to disable.
     *  </p>
     */
    public static final String IDLE_CONNECTION_PRUNE_TIMEOUT = "jersey.config.client.idleConnectionPruneTimeout";

    /**
     * Enable or disable the Netty logging by {@code LoggingHandler(Level.DEBUG)}. Disabled by default.
     */
    public static final String LOGGING_ENABLED = "jersey.config.client.netty.loggingEnabled";

    /**
     *  <p>
     *    This property determines the maximum number of idle connections that will be simultaneously kept alive, per destination.
     *    The default is 5.
     *  </p>
     *  <p>
     *    This property is a Jersey alternative to System property {@code}http.maxConnections{@code}. The Jersey property takes
     *    precedence over the system property.
     *  </p>
     */
    public static final String MAX_CONNECTIONS = "jersey.config.client.maxConnections";

    /**
     * <p>
     *    This property determines the maximum number of idle connections that will be simultaneously kept alive
     *    in total, rather than per destination. The default is 60. Specify 0 to disable.
     * </p>
     */
    public static final String MAX_CONNECTIONS_TOTAL = "jersey.config.client.maxTotalConnections";

    /**
     * The maximal number of redirects during single request.
     * <p/>
     * Value is expected to be positive {@link Integer}. Default value is 5.
     * <p/>
     * HTTP redirection must be enabled by property {@link org.glassfish.jersey.client.ClientProperties#FOLLOW_REDIRECTS},
     * otherwise {@code MAX_REDIRECTS} is not applied.
     *
     * @since 2.36
     * @see org.glassfish.jersey.client.ClientProperties#FOLLOW_REDIRECTS
     * @see org.glassfish.jersey.netty.connector.internal.RedirectException
     */
    public static final String MAX_REDIRECTS = "jersey.config.client.NettyConnectorProvider.maxRedirects";

    /**
     * <p>
     *     Sets the HTTP POST method to be preserved on HTTP status 301 (MOVED PERMANENTLY) or status 302 (FOUND) when {@code true}
     *     or redirected as GET when {@code false}.
     * </p>
     * <p>
     *     The default value is {@code true} and the HTTP POST request is not redirected as GET.
     * </p>
     * <p>
     *     The name of the configuration property is <tt>{@value}</tt>.
     * </p>
     * @since 2.41
     */
    public static final String PRESERVE_METHOD_ON_REDIRECT = "jersey.config.client.redirect.preserve.method";


    /**
     * This timeout is used for waiting for 100-Continue response when 100-Continue is sent by the client.
     *
     * @since 2.41
     */
    public static final String
            EXPECT_100_CONTINUE_TIMEOUT = "jersey.config.client.request.expect.100.continue.timeout";

    /**
     * The default value of EXPECT_100_CONTINUE_TIMEOUT.
     *
     * @since 2.41
     */
    public static final Integer
            DEFAULT_EXPECT_100_CONTINUE_TIMEOUT_VALUE = 500;


    /**
     * Parameter which allows extending of the header size for the Netty connector
     *
     * @since 2.44
     */
    public static final String
            MAX_HEADER_SIZE = "jersey.config.client.netty.maxHeaderSize";

    /**
     * Default header size for Netty Connector.
     * Taken from {@link io.netty.handler.codec.http.HttpClientCodec#HttpClientCodec(int, int, int)}
     *
     * @since 2.44
     */
    public static final Integer
        DEFAULT_HEADER_SIZE = 8192;

    /**
     * Parameter which allows extending of the first line length of the HTTP header for the Netty connector.
     * Taken from {@link io.netty.handler.codec.http.HttpClientCodec#HttpClientCodec(int, int, int)}.
     *
     * @since 2.44
     */
    public static final String
            MAX_INITIAL_LINE_LENGTH = "jersey.config.client.netty.maxInitialLineLength";

    /**
     * Default initial line length for Netty Connector.
     * Typically, set this to the same value as {@link #MAX_HEADER_SIZE}.
     *
     * @since 2.44
     */
    public static final Integer
        DEFAULT_INITIAL_LINE_LENGTH = 8192;

    /**
     * Parameter which allows extending of the chunk size for the Netty connector
     *
     * @since 2.44
     */
    public static final String
            MAX_CHUNK_SIZE = "jersey.config.client.netty.maxChunkSize";

    /**
     * Default chunk size for Netty Connector.
     * Taken from {@link io.netty.handler.codec.http.HttpClientCodec#HttpClientCodec(int, int, int)}
     *
     * @since 2.44
     */
    public static final Integer
        DEFAULT_CHUNK_SIZE = 8192;

}
