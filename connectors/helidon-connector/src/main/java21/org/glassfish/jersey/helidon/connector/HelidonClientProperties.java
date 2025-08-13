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

import io.helidon.common.tls.Tls;
import io.helidon.config.Config;
import io.helidon.webclient.api.WebClient;
import org.glassfish.jersey.internal.util.PropertiesClass;

import java.util.List;
import java.util.Map;

/**
 * Configuration options specific to the Client API that utilizes {@code HelidonConnectorProvider}
 * @since 2.31
 */
@PropertiesClass
public final class HelidonClientProperties {

    private HelidonClientProperties() {
    }

    /**
     * Property name to set a {@link Config} instance to by used by underlying {@link WebClient}.
     * This property is settable on {@link jakarta.ws.rs.core.Configurable#property(String, Object)}.
     *
     * @see io.helidon.webclient.api.WebClientConfig.Builder#config(io.helidon.common.config.Config)
     */
    public static final String CONFIG = "jersey.connector.helidon.config";

    /**
     * Property name to set a {@link Tls} instance to be used by underlying {@link WebClient}.
     * This property is settable on {@link jakarta.ws.rs.core.Configurable#property(String, Object)}.
     *
     * @see io.helidon.webclient.api.WebClientConfig.Builder#tls(Tls)
     */
    public static final String TLS = "jersey.connector.helidon.tls";

    /**
     * Property name to set a {@code List<? extends  ProtocolConfig>} instance with a list of
     * protocol configs to be used by underlying {@link WebClient}.
     * This property is settable on {@link jakarta.ws.rs.core.Configurable#property(String, Object)}.
     *
     * @see io.helidon.webclient.api.WebClientConfig.Builder#protocolConfigs(List)
     */
    public static final String PROTOCOL_CONFIGS = "jersey.connector.helidon.protocolConfigs";

    /**
     * Property name to set a {@code Map<String, String>} instance with a list of
     * default headers to be used by underlying {@link WebClient}.
     * This property is settable on {@link jakarta.ws.rs.core.Configurable#property(String, Object)}.
     *
     * @see io.helidon.webclient.api.WebClientConfig.Builder#defaultHeadersMap(Map)
     */
    public static final String DEFAULT_HEADERS = "jersey.connector.helidon.defaultHeaders";

    /**
     * Property name to set a protocol ID for each request. You can use this property
     * to request an HTTP/2 upgrade from HTTP/1.1 by setting its value to {@code "h2"}.
     * When using TLS, Helidon uses negotiation via the ALPN extension instead of this
     * property.
     *
     * @see io.helidon.webclient.api.HttpClientRequest#protocolId(String)
     */
    public static final String PROTOCOL_ID = "jersey.connector.helidon.protocolId";

    /**
     * Property name to enable or disable connection caching in the underlying {@link WebClient}.
     * The default for the Helidon connector is {@code false}, or no sharing (which is the
     * opposite of {@link WebClient}). Set this property to {@code true} to enable connection
     * caching.
     *
     * @see io.helidon.webclient.api.WebClientConfig.Builder#shareConnectionCache(boolean)
     */
    public static final String SHARE_CONNECTION_CACHE = "jersey.connector.helidon.shareConnectionCache";
}
