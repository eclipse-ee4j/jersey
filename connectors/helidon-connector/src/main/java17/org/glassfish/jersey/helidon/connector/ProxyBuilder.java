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

import java.net.URI;
import java.util.Locale;
import java.util.Optional;

import io.helidon.webclient.api.Proxy;

import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.core.Configuration;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.ClientRequest;

class ProxyBuilder {

    static Optional<Proxy> createProxy(Configuration config) {
        Object proxyUri = config.getProperty(ClientProperties.PROXY_URI);
        String userName = ClientProperties.getValue(config.getProperties(), ClientProperties.PROXY_USERNAME, String.class);
        String password = ClientProperties.getValue(config.getProperties(), ClientProperties.PROXY_PASSWORD, String.class);
        return createProxy(proxyUri, userName, password);
    }

    static Optional<Proxy> createProxy(ClientRequest clientRequest) {
        Object proxyUri = clientRequest.resolveProperty(ClientProperties.PROXY_URI, Object.class);
        String userName = clientRequest.resolveProperty(ClientProperties.PROXY_USERNAME, String.class);
        String password = clientRequest.resolveProperty(ClientProperties.PROXY_PASSWORD, String.class);
        return createProxy(proxyUri, userName, password);
    }

    private ProxyBuilder() {
    }

    private static Optional<Proxy> createProxy(Object proxyUri, String userName, String password) {
        if (proxyUri != null) {
            URI u = getProxyUri(proxyUri);
            Proxy.Builder builder = Proxy.builder();
            if (u.getScheme().toUpperCase(Locale.ROOT).equals("DIRECT")) {
                builder.type(Proxy.ProxyType.NONE);
            } else {
                builder.host(u.getHost()).port(u.getPort());
                if ("HTTP".equals(u.getScheme().toUpperCase(Locale.ROOT))) {
                    builder.type(Proxy.ProxyType.HTTP);
                } else {
                    HelidonConnector.LOGGER.log(System.Logger.Level.WARNING,
                            LocalizationMessages.PROXY_SCHEMA_NOT_SUPPORTED(u.getScheme()));
                    return Optional.empty();
                }
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

    private static URI getProxyUri(Object proxy) {
        if (proxy instanceof URI) {
            return (URI) proxy;
        } else if (proxy instanceof String) {
            return URI.create((String) proxy);
        } else {
            throw new ProcessingException(LocalizationMessages.WRONG_PROXY_URI_TYPE(proxy));
        }
    }
}
