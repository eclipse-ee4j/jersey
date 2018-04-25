/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.jdk.connector;

import java.net.CookiePolicy;
import java.util.Map;

import org.glassfish.jersey.internal.util.PropertiesClass;
import org.glassfish.jersey.internal.util.PropertiesHelper;

/**
 * Configuration options specific to {@link org.glassfish.jersey.jdk.connector.internal.JdkConnector}.
 *
 * @author Adam Lindenthal (adam.lindenthal at oracle.com)
 */
@PropertiesClass
public final class JdkConnectorProperties {

    /**
     * Configuration of the connector thread pool.
     * <p/>
     * An instance of {@link org.glassfish.jersey.jdk.connector.internal.ThreadPoolConfig} is expected.
     */
    public static final String WORKER_THREAD_POOL_CONFIG = "jersey.config.client.JdkConnectorProvider.workerThreadPoolConfig";

    /**
     * Container idle timeout in milliseconds ({@link Integer} value).
     * <p/>
     * When the timeout elapses, the shared thread pool will be destroyed.
     * <p/>
     * The default value is {@value #DEFAULT_CONNECTION_CLOSE_WAIT}
     */
    public static final String CONTAINER_IDLE_TIMEOUT = "jersey.config.client.JdkConnectorProvider.containerIdleTimeout";

    /**
     * A configurable property of HTTP parser. It defines the maximal acceptable size of HTTP response initial line,
     * each header and chunk header.
     * <p/>
     * The default value is {@value #DEFAULT_MAX_HEADER_SIZE}
     */
    public static final String MAX_HEADER_SIZE = "jersey.config.client.JdkConnectorProvider.maxHeaderSize";

    /**
     * The maximal number of redirects during single request.
     * <p/>
     * Value is expected to be positive {@link Integer}. Default value is {@value #DEFAULT_MAX_REDIRECTS}.
     * <p/>
     * HTTP redirection must be enabled by property {@link org.glassfish.jersey.client.ClientProperties#FOLLOW_REDIRECTS},
     * otherwise {@code MAX_HEADER_SIZE} is not applied.
     *
     * @see org.glassfish.jersey.client.ClientProperties#FOLLOW_REDIRECTS
     * @see org.glassfish.jersey.jdk.connector.internal.RedirectException
     */
    public static final String MAX_REDIRECTS = "jersey.config.client.JdkConnectorProvider.maxRedirects";

    /**
     * To set the cookie policy of this cookie manager.
     * <p/>
     * The default value is ACCEPT_ORIGINAL_SERVER.
     *
     * @see java.net.CookieManager
     */
    public static final String COOKIE_POLICY = "jersey.config.client.JdkConnectorProvider.cookiePolicy";

    /**
     * A maximal number of open connection to each destination. A destination is determined by the following triple:
     * <ul>
     * <li>host</li>
     * <li>port</li>
     * <li>protocol (HTTP/HTTPS)</li>
     * <ul/>
     * <p/>
     * The default value is {@value #DEFAULT_MAX_CONNECTIONS_PER_DESTINATION}
     */
    public static final String MAX_CONNECTIONS_PER_DESTINATION = "jersey.config.client.JdkConnectorProvider"
            + ".maxConnectionsPerDestination";

    /**
     * An amount of time in milliseconds ({@link Integer} value) during which an idle connection will be kept open.
     * <p/>
     * The default value is {@value #DEFAULT_CONNECTION_IDLE_TIMEOUT}
     */
    public static final String CONNECTION_IDLE_TIMEOUT = "jersey.config.client.JdkConnectorProvider.connectionIdleTimeout";

    /**
     * Default value for the {@link org.glassfish.jersey.client.ClientProperties#CHUNKED_ENCODING_SIZE} property.
     */
    public static final int DEFAULT_HTTP_CHUNK_SIZE = 4096;

    /**
     * Default value for the {@link #MAX_HEADER_SIZE} property.
     */
    public static final int DEFAULT_MAX_HEADER_SIZE = 8192;

    /**
     * Default value for the {@link #MAX_REDIRECTS} property.
     */
    public static final int DEFAULT_MAX_REDIRECTS = 5;

    /**
     * Default value for the {@link #COOKIE_POLICY} property.
     */
    public static final CookiePolicy DEFAULT_COOKIE_POLICY = CookiePolicy.ACCEPT_ORIGINAL_SERVER;

    /**
     * Default value for the {@link #MAX_CONNECTIONS_PER_DESTINATION} property.
     */
    public static final int DEFAULT_MAX_CONNECTIONS_PER_DESTINATION = 20;

    /**
     * Default value for the {@link #CONNECTION_IDLE_TIMEOUT} property.
     */
    public static final int DEFAULT_CONNECTION_IDLE_TIMEOUT = 1000000;

    /**
     * Default value for the {@link #CONTAINER_IDLE_TIMEOUT} property.
     */
    public static final int DEFAULT_CONNECTION_CLOSE_WAIT = 30_000;

    public static <T> T getValue(final Map<String, ?> properties, final String key, final Class<T> type) {
        return PropertiesHelper.getValue(properties, key, type, null);
    }

    public static <T> T getValue(final Map<String, ?> properties, final String key, T defaultValue, final Class<T> type) {
        return PropertiesHelper.getValue(properties, key, defaultValue, type, null);
    }

    /**
     * Prevents instantiation.
     */
    private JdkConnectorProperties() {
        throw new AssertionError("No instances allowed.");
    }
}
