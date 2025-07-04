/*
 * Copyright (c) 2022, 2025 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.jersey.client.innate;

import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.internal.LocalizationMessages;
import org.glassfish.jersey.internal.PropertiesResolver;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.MultivaluedMap;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Default client Proxy information internal object. It is used for parsing the proxy information in all connectors.
 */
public abstract class ClientProxy {

    private ClientProxy() {
      // do not instantiate
    };

    public static Optional<ClientProxy> proxyFromRequest(PropertiesResolver request) {
        return getProxy(request);
    }

    public static Optional<ClientProxy> proxyFromUri(URI requestUri) {
        return getSystemPropertiesProxy(requestUri);
    }

    public static Optional<ClientProxy> proxyFromProperties(Map<String, Object> properties) {
        return getProxy(properties);
    }

    public static Optional<ClientProxy> proxyFromConfiguration(Configuration configuration) {
        return getProxy(configuration.getProperties());
    }

    public static ClientProxy proxy(Proxy proxy) {
        return new ProxyClientProxy(proxy);
    }

    public static void setBasicAuthorizationHeader(MultivaluedMap<String, Object> headers, ClientProxy proxy) {
        if (proxy.userName() != null) {
            StringBuilder auth = new StringBuilder().append(proxy.userName()).append(":");
            if (proxy.password() != null) {
                auth.append(proxy.password());
            }
            String encoded = "Basic " + Base64.getEncoder().encodeToString(auth.toString().getBytes());
            headers.put("Proxy-Authorization", Arrays.asList(encoded));
        }
    }

    protected String userName;
    protected String password;

    private static ClientProxy toProxy(Object proxy) {
        if (proxy instanceof String) {
            return new UriClientProxy(URI.create((String) proxy));
        } else if (proxy instanceof URI) {
            return new UriClientProxy((URI) proxy);
        } else if (Proxy.class.isInstance(proxy)) {
            Proxy netProxy = Proxy.class.cast(proxy);
            if (Proxy.Type.HTTP.equals(netProxy.type())) {
                return new ProxyClientProxy(Proxy.class.cast(proxy));
            } else {
                return null;
            }
        } else {
            throw new ProcessingException(LocalizationMessages.WRONG_PROXY_URI_TYPE(ClientProperties.PROXY_URI));
        }
    }

    public abstract Proxy proxy();

    public abstract URI uri();

    public abstract Proxy.Type type();

    public String password() {
        return password;
    }

    public String userName() {
        return userName;
    };

    private static Optional<ClientProxy> getProxy(PropertiesResolver request) {
        Object proxyUri = request.resolveProperty(ClientProperties.PROXY_URI, Object.class);
        if (proxyUri != null) {
            ClientProxy proxy = toProxy(proxyUri);
            if (proxy != null) {
                proxy.userName = request.resolveProperty(ClientProperties.PROXY_USERNAME, String.class);
                proxy.password = request.resolveProperty(ClientProperties.PROXY_PASSWORD, String.class);
                return Optional.of(proxy);
            } else {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    private static Optional<ClientProxy> getProxy(Map<String, Object> properties) {
        Object proxyUri = properties.get(ClientProperties.PROXY_URI);
        if (proxyUri != null) {
            ClientProxy proxy = toProxy(proxyUri);
            if (proxy != null) {
                proxy.userName = ClientProperties.getValue(properties, ClientProperties.PROXY_USERNAME, String.class);
                proxy.password = ClientProperties.getValue(properties, ClientProperties.PROXY_PASSWORD, String.class);
                return Optional.of(proxy);
            } else {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    private static Optional<ClientProxy> getSystemPropertiesProxy(URI requestUri) {
        ProxySelector sel = ProxySelector.getDefault();
        for (Proxy proxy: sel.select(requestUri)) {
            if (Proxy.Type.HTTP.equals(proxy.type())) {
                return Optional.of(new ProxyClientProxy(proxy));
            }
        }
        return Optional.empty();
    }

    private static final class ProxyClientProxy extends ClientProxy {

        private final Proxy proxy;

        private ProxyClientProxy(Proxy proxy) {
            this.proxy = proxy;
        }

        @Override
        public Proxy proxy() {
            return proxy;
        }

        @Override
        public Proxy.Type type() {
            return proxy.type();
        }

        @Override
        public URI uri() {
            URI uri = null;
            if (Proxy.Type.HTTP.equals(proxy.type())) {
                SocketAddress proxyAddress = proxy.address();
                if (InetSocketAddress.class.isInstance(proxy.address())) {
                    InetSocketAddress proxyAddr = (InetSocketAddress) proxyAddress;
                    try {
                        if (proxyAddr.isUnresolved()
                                && proxyAddr.getHostName() != null
                                && proxyAddr.getHostName().toLowerCase(Locale.ROOT).startsWith("http://")) {
                            String hostString = proxyAddr.getHostString().substring(7);
                            uri = new URI("http", null, hostString, proxyAddr.getPort(), null, null, null);
                        } else {
                            uri = new URI("http", null, proxyAddr.getHostString(), proxyAddr.getPort(), null, null, null);
                        }
                    } catch (URISyntaxException e) {
                        throw new ProcessingException(e);
                    }
                }
            }
            return uri;
        }
    }

    private static final class UriClientProxy extends ClientProxy {
        private final URI uri;

        private UriClientProxy(URI uri) {
            this.uri = uri;
        }

        @Override
        public Proxy proxy() {
            return new Proxy(type(), new InetSocketAddress(uri.getHost(), uri.getPort()));
        }

        @Override
        public Proxy.Type type() {
            return Proxy.Type.HTTP;
        }

        @Override
        public URI uri() {
            return uri;
        }
    }
}

