/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved.
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

import io.helidon.common.http.Headers;
import io.helidon.common.http.Http;
import io.helidon.common.http.ReadOnlyParameters;
import io.helidon.config.Config;
import io.helidon.config.ConfigSources;
import io.helidon.media.common.InputStreamBodyReader;
import io.helidon.media.common.MessageBodyReader;
import io.helidon.webclient.Proxy;
import io.helidon.webclient.Ssl;
import io.helidon.webclient.WebClientResponse;
import io.netty.handler.codec.http.HttpHeaderValues;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.ClientRequest;

import javax.net.ssl.SSLContext;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.Configuration;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Helidon specific classes and implementations.
 */
class HelidonStructures {

    static Headers createHeaders(Map<String, List<String>> data) {
        return new ReadOnlyHeaders(data);
    }

    static MessageBodyReader<InputStream> createInputStreamBodyReader() {
        return InputStreamBodyReader.create();
    }

    static Optional<Config> helidonConfig(Configuration configuration) {
        final Object helidonConfig = configuration.getProperty(HelidonProperties.CONFIG);
        if (helidonConfig != null) {
            if (!Config.class.isInstance(helidonConfig)) {
                HelidonConnector.LOGGER.warning(LocalizationMessages.NOT_HELIDON_CONFIG(helidonConfig.getClass().getName()));
                return Optional.empty();
            } else {
                return Optional.of((Config) helidonConfig);
            }
        }
        return Optional.empty();
    }

    static Optional<Proxy> createProxy(Configuration config) {
        return ProxyBuilder.createProxy(config);
    }

    static Optional<Proxy> createProxy(ClientRequest request) {
        return ProxyBuilder.createProxy(request);
    }

    static Optional<Ssl> createSSL(SSLContext context) {
        return context == null ? Optional.empty() : Optional.of(Ssl.builder().sslContext(context).build());
    }

    static boolean hasEntity(WebClientResponse webClientResponse) {
        final ReadOnlyParameters headers = webClientResponse.content().readerContext().headers();
        final Optional<String> contentLenth = headers.first(Http.Header.CONTENT_LENGTH);
        final Optional<String> encoding = headers.first(Http.Header.TRANSFER_ENCODING);

        return ((contentLenth.isPresent() && !contentLenth.get().equals("0"))
                || (encoding.isPresent() && encoding.get().equals(HttpHeaderValues.CHUNKED.toString())));
    }

    private static class ReadOnlyHeaders extends ReadOnlyParameters implements Headers {
        public ReadOnlyHeaders(Map<String, List<String>> data) {
            super(data);
        }
    }

    private static class ProxyBuilder {
        private static Optional<Proxy> createProxy(Configuration config) {
            final Object proxyUri = config.getProperty(ClientProperties.PROXY_URI);
            final String userName
                    = ClientProperties.getValue(config.getProperties(), ClientProperties.PROXY_USERNAME, String.class);
            final String password
                    = ClientProperties.getValue(config.getProperties(), ClientProperties.PROXY_PASSWORD, String.class);
            return createProxy(proxyUri, userName, password);
        }

        private static Optional<Proxy> createProxy(ClientRequest clientRequest) {
            final Object proxyUri = clientRequest.resolveProperty(ClientProperties.PROXY_URI, Object.class);
            final String userName = clientRequest.resolveProperty(ClientProperties.PROXY_USERNAME, String.class);
            final String password = clientRequest.resolveProperty(ClientProperties.PROXY_PASSWORD, String.class);
            return createProxy(proxyUri, userName, password);
        }

        private static Optional<Proxy> createProxy(Object proxyUri, String userName, String password) {
            if (proxyUri != null) {
                final URI u = getProxyUri(proxyUri);
                final Proxy.Builder builder = Proxy.builder();
                Map<String, String> proxyMap;
                if (u.getScheme().toUpperCase(Locale.ROOT).equals("DIRECT")) {
                    proxyMap = Map.of("type", "NONE");
                    //builder.type(Proxy.ProxyType.NONE);
                } else {
                    builder.host(u.getHost()).port(u.getPort());
                    switch (u.getScheme().toUpperCase(Locale.ROOT)) {
                        case "HTTP":
                            proxyMap = Map.of("type", "HTTP");
                            //builder.type(Proxy.ProxyType.HTTP);
                            break;
                        case "SOCKS":
                            proxyMap = Map.of("type", "SOCKS_4");
                            //builder.type(Proxy.ProxyType.SOCKS_4);
                            break;
                        case "SOCKS5":
                            proxyMap = Map.of("type", "SOCKS_5");
                            //builder.type(Proxy.ProxyType.SOCKS_5);
                            break;
                        default:
                            HelidonConnector.LOGGER.warning(LocalizationMessages.UNSUPPORTED_PROXY_SCHEMA(u.getScheme()));
                            return Optional.empty();
                    }
                    builder.config(Config.create(ConfigSources.create(proxyMap)));
                }
                if (userName != null) {
                    builder.username(userName);

                    if (password != null) {
                        builder.password(password.toCharArray());
                    }
                }
                return Optional.of(builder.build());
            } else {
                return Optional.empty();
            }
        }

        private static URI getProxyUri(final Object proxy) {
            if (proxy instanceof URI) {
                return (URI) proxy;
            } else if (proxy instanceof String) {
                return URI.create((String) proxy);
            } else {
                throw new ProcessingException(LocalizationMessages.WRONG_PROXY_URI_TYPE(ClientProperties.PROXY_URI));
            }
        }
    }
}
