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

package org.glassfish.jersey.helidon.connector;

import io.helidon.common.LazyValue;
import io.helidon.common.Version;
import io.helidon.common.tls.Tls;
import io.helidon.config.Config;
import io.helidon.http.Header;
import io.helidon.http.HeaderNames;
import io.helidon.http.Method;
import io.helidon.http.media.ReadableEntity;
import io.helidon.service.registry.ServiceRegistryException;
import io.helidon.service.registry.Services;
import io.helidon.webclient.api.HttpClientRequest;
import io.helidon.webclient.api.HttpClientResponse;
import io.helidon.webclient.api.Proxy;
import io.helidon.webclient.api.WebClient;
import io.helidon.webclient.api.WebClientConfig;
import io.helidon.webclient.spi.ProtocolConfig;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.core.Response;


import java.net.URI;
import java.security.AccessController;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.ClientRequest;
import org.glassfish.jersey.client.ClientResponse;
import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.client.spi.AsyncConnectorCallback;
import org.glassfish.jersey.client.spi.Connector;
import org.glassfish.jersey.innate.VirtualThreadUtil;
import org.glassfish.jersey.internal.util.PropertiesHelper;

class HelidonConnector implements Connector {
    static final System.Logger LOGGER = System.getLogger(HelidonConnector.class.getName());
    private static final String CONNECTOR_CONFIG_ROOT = "jersey.connector.webclient";

    private static final String HELIDON_VERSION = "Helidon/" + Version.VERSION + " (java "
            + AccessController.doPrivileged(PropertiesHelper.getSystemProperty("java.runtime.version")) + ")";

    private final LazyValue<ExecutorService> executorService;
    private final WebClient webClient;
    private final Proxy proxy;

    @SuppressWarnings("unchecked")
    HelidonConnector(Client client, Configuration configuration) {
        executorService = LazyValue.create(() -> VirtualThreadUtil.withConfig(configuration,
                        VirtualThreadUtil.threadFactoryBuilder("helidon-connector-", 0L) ,true).newCachedThreadPool());

        // create underlying HTTP client
        Map<String, Object> properties = configuration.getProperties();
        WebClientConfig.Builder builder = WebClientConfig.builder();

        // use config from property first then registry
        var helidonConfig = configuration.getProperty(HelidonClientProperties.CONFIG);
        if (helidonConfig != null) {
            if (helidonConfig instanceof Config) {
                builder.config((Config) helidonConfig);
            } else {
                LOGGER.log(System.Logger.Level.WARNING,
                        LocalizationMessages.HELIDON_CONFIG_IGNORED(HelidonClientProperties.CONFIG));
                builder.config(configFromRegistry());
            }
        } else {
            builder.config(configFromRegistry());
        }

        // proxy support
        proxy = ProxyBuilder.createProxy(configuration).orElse(Proxy.create());

        // possibly override config with properties defined in Jersey client
        // property values are ignored if cannot be converted to correct type
        if (properties.containsKey(ClientProperties.CONNECT_TIMEOUT)) {
            Integer connectTimeout = ClientProperties.getValue(properties, ClientProperties.CONNECT_TIMEOUT, Integer.class);
            if (connectTimeout != null) {
                builder.connectTimeout(Duration.ofMillis(connectTimeout));
            }
        }
        if (properties.containsKey(ClientProperties.READ_TIMEOUT)) {
            Integer readTimeout = ClientProperties.getValue(properties, ClientProperties.READ_TIMEOUT, Integer.class);
            if (readTimeout != null) {
                builder.readTimeout(Duration.ofMillis(readTimeout));
            }
        }
        if (properties.containsKey(ClientProperties.FOLLOW_REDIRECTS)) {
            Boolean followRedirects = ClientProperties.getValue(properties, ClientProperties.FOLLOW_REDIRECTS, Boolean.class);
            if (followRedirects != null) {
                builder.followRedirects(followRedirects);
            }
        }
        if (properties.containsKey(ClientProperties.EXPECT_100_CONTINUE)) {
            Boolean expect100Continue = ClientProperties
                    .getValue(properties, ClientProperties.EXPECT_100_CONTINUE, Boolean.class);
            if (expect100Continue != null) {
                builder.sendExpectContinue(expect100Continue);
            }
        }

        // first check property and then the Jersey client SSL config
        boolean isTlsSet = false;
        if (properties.containsKey(HelidonClientProperties.TLS)) {
            Tls tls = ClientProperties.getValue(properties, HelidonClientProperties.TLS, Tls.class);
            if (tls != null) {
                builder.tls(tls);
                isTlsSet = true;
            }
        }
        if (!isTlsSet && client.getSslContext() != null) {
            boolean isJerseyClient = client instanceof JerseyClient;
            boolean jerseyHasDefaultSsl = isJerseyClient && ((JerseyClient) client).isDefaultSslContext();
            if (!isJerseyClient || !jerseyHasDefaultSsl) {
                builder.tls(Tls.builder().sslContext(client.getSslContext()).build());
            }
        }

        // protocol configs
        if (properties.containsKey(HelidonClientProperties.PROTOCOL_CONFIGS)) {
            List<? extends ProtocolConfig> protocolConfigs =
                    (List<? extends ProtocolConfig>) properties.get(HelidonClientProperties.PROTOCOL_CONFIGS);
            if (protocolConfigs != null) {
                builder.addProtocolConfigs(protocolConfigs);
            }
        }

        // default headers
        if (properties.containsKey(HelidonClientProperties.DEFAULT_HEADERS)) {
            Map<String, String> defaultHeaders = ClientProperties
                    .getValue(properties, HelidonClientProperties.DEFAULT_HEADERS, Map.class);
            if (defaultHeaders != null) {
                builder.defaultHeadersMap(defaultHeaders);
            }
        }

        // connection sharing defaults to false in this connector
        if (properties.containsKey(HelidonClientProperties.SHARE_CONNECTION_CACHE)) {
            Boolean shareConnectionCache = ClientProperties
                    .getValue(properties, HelidonClientProperties.SHARE_CONNECTION_CACHE, Boolean.class);
            if (shareConnectionCache != null) {
                builder.shareConnectionCache(shareConnectionCache);
            }
        }

        webClient = builder.build();
    }

    /**
     * Map a Jersey request to a Helidon HTTP request.
     *
     * @param request the request to map
     * @return the mapped request
     */
    private HttpClientRequest mapRequest(ClientRequest request) {
        // possibly override proxy in request
        Proxy requestProxy = ProxyBuilder.createProxy(request).orElse(proxy);

        // create WebClient request
        URI uri = request.getUri();
        HttpClientRequest httpRequest = webClient
                .method(Method.create(request.getMethod()))
                .proxy(requestProxy)
                .uri(uri);

        // map request headers
        request.getRequestHeaders().forEach((key, value) -> {
            String[] values = value.toArray(new String[0]);
            httpRequest.header(HeaderNames.create(key), values);
        });

        // request config
        Boolean followRedirects = request.resolveProperty(ClientProperties.FOLLOW_REDIRECTS, Boolean.class);
        if (followRedirects != null) {
            httpRequest.followRedirects(followRedirects);
        }
        Integer readTimeout = request.resolveProperty(ClientProperties.READ_TIMEOUT, Integer.class);
        if (readTimeout != null) {
            httpRequest.readTimeout(Duration.ofMillis(readTimeout));
        }
        String protocolId = request.resolveProperty(HelidonClientProperties.PROTOCOL_ID, String.class);
        if (protocolId != null) {
            httpRequest.protocolId(protocolId);
        }

        // copy properties
        for (String name : request.getConfiguration().getPropertyNames()) {
            Object value = request.getConfiguration().getProperty(name);
            if (!name.startsWith("jersey") && value instanceof String) {
                httpRequest.property(name, (String) value);
            }
        }
        for (String propertyName : request.getPropertyNames()) {
            Object value = request.resolveProperty(propertyName, Object.class);
            if (!propertyName.startsWith("jersey") && value instanceof String) {
                httpRequest.property(propertyName, (String) value);
            }
        }

        return httpRequest;
    }

    /**
     * Map a Helidon HTTP/1.1 response to a Jersey response.
     *
     * @param httpResponse the response to map
     * @param request the request
     * @return the mapped response
     */
    private ClientResponse mapResponse(HttpClientResponse httpResponse, ClientRequest request) {
        Response.StatusType statusType = new Response.StatusType() {
            @Override
            public int getStatusCode() {
                return httpResponse.status().code();
            }

            @Override
            public Response.Status.Family getFamily() {
                return Response.Status.Family.familyOf(getStatusCode());
            }

            @Override
            public String getReasonPhrase() {
                return httpResponse.status().reasonPhrase();
            }
        };
        ClientResponse response = new ClientResponse(statusType, request) {
            @Override
            public void close() {
                super.close();
                httpResponse.close();       // closes WebClient's response
            }
        };

        // copy headers
        for (Header header : httpResponse.headers()) {
            for (String v : header.allValues()) {
                response.getHeaders().add(header.name(), v);
            }
        }

        // last URI, possibly after redirections
        response.setResolvedRequestUri(httpResponse.lastEndpointUri().toUri());

        // handle entity
        ReadableEntity entity = httpResponse.entity();
        if (entity.hasEntity()) {
            response.setEntityStream(entity.inputStream());
        }
        return response;
    }

    /**
     * Execute Jersey request using WebClient.
     *
     * @param request the Jersey request
     * @return a Jersey response
     */
    @Override
    public ClientResponse apply(ClientRequest request) {
        HttpClientResponse httpResponse;
        HttpClientRequest httpRequest = mapRequest(request);

        if (request.hasEntity()) {
            httpResponse = httpRequest.outputStream(os -> {
                request.setStreamProvider(length -> os);
                request.writeEntity();
            });
        } else {
            httpResponse = httpRequest.request();
        }

        return mapResponse(httpResponse, request);
    }

    /**
     * Asynchronously execute Jersey request using WebClient.
     *
     * @param request the Jersey request
     * @return a Jersey response
     */
    @Override
    public Future<?> apply(ClientRequest request, AsyncConnectorCallback callback) {
        return executorService.get().submit(() -> {
            try {
                ClientResponse response = apply(request);
                callback.response(response);
            } catch (Throwable t) {
                callback.failure(t);
            }
        });
    }

    @Override
    public String getName() {
        return HELIDON_VERSION;
    }

    @Override
    public void close() {
    }

    WebClient client() {
        return webClient;
    }

    Proxy proxy() {
        return proxy;
    }

    Config configFromRegistry() {
        try {
            io.helidon.common.config.Config cfg = Services.get(io.helidon.common.config.Config.class);
            if (cfg instanceof Config) {
                return ((Config) cfg).get(CONNECTOR_CONFIG_ROOT);
            }
        } catch (ServiceRegistryException e) {
            // falls through
        }
        LOGGER.log(System.Logger.Level.TRACE, LocalizationMessages.NO_CONFIG_IN_REGISTERY());
        return Config.empty();
    }
}