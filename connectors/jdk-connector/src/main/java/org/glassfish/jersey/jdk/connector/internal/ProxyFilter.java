/*
 * Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.jdk.connector.internal;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.util.List;

/**
 * @author Petr Janouch (petr.janouch at oracle.com)
 */
class ProxyFilter extends Filter<HttpRequest, HttpResponse, HttpRequest, HttpResponse> {

    private final ConnectorConfiguration.ProxyConfiguration proxyConfiguration;
    private final ProxyDigestAuthenticator proxyDigestAuthenticator = new ProxyDigestAuthenticator();
    private volatile State state = State.CONNECTING;
    private volatile InetSocketAddress originalDestinationAddress;

    /**
     * Constructor.
     *
     * @param downstreamFilter downstream filter. Accessible directly as {@link #downstreamFilter} protected field.
     */
    ProxyFilter(final Filter<HttpRequest, HttpResponse, ?, ?> downstreamFilter,
                ConnectorConfiguration.ProxyConfiguration proxyConfiguration) {
        super(downstreamFilter);
        this.proxyConfiguration = proxyConfiguration;
    }

    @Override
    void connect(final SocketAddress address, final Filter<?, ?, HttpRequest, HttpResponse> upstreamFilter) {
        this.upstreamFilter = upstreamFilter;
        this.originalDestinationAddress = (InetSocketAddress) address;
        downstreamFilter.connect(new InetSocketAddress(proxyConfiguration.getHost(), proxyConfiguration.getPort()), this);
    }

    @Override
    void onConnect() {
        HttpRequest connect = createConnectRequest();
        downstreamFilter.write(connect, new CompletionHandler<HttpRequest>() {
            @Override
            public void failed(final Throwable throwable) {
                upstreamFilter.processError(throwable);
            }
        });
    }

    @Override
    boolean processRead(HttpResponse httpResponse) {
        if (state == State.CONNECTED) {
            // if we have stop the connection phase, just pass through
            return true;
        }

        switch (httpResponse.getStatusCode()) {

            case 200: {
                state = State.CONNECTED;
                upstreamFilter.onConnect();
                break;
            }

            case 407: {
                if (state == State.AUTHENTICATED) {
                    upstreamFilter.onError(new ProxyAuthenticationException(LocalizationMessages.PROXY_407_TWICE()));
                    return false;
                }

                try {
                    state = State.AUTHENTICATED;
                    HttpRequest authenticatingRequest = createAuthenticatingRequest(httpResponse);
                    downstreamFilter.write(authenticatingRequest, new CompletionHandler<HttpRequest>() {
                        @Override
                        public void failed(final Throwable throwable) {
                            upstreamFilter.processError(throwable);
                        }
                    });
                } catch (ProxyAuthenticationException e) {
                    handleError(e);
                    return false;
                }

                break;
            }

            default: {
                handleError(new IOException(LocalizationMessages.PROXY_CONNECT_FAIL(httpResponse.getStatusCode())));
            }
        }

        return false;
    }

    @Override
    void write(final HttpRequest data, final CompletionHandler<HttpRequest> completionHandler) {
        downstreamFilter.write(data, completionHandler);
    }

    private void handleError(Throwable t) {
        upstreamFilter.onError(t);
    }

    private HttpRequest createAuthenticatingRequest(HttpResponse httpResponse) throws ProxyAuthenticationException {
        String authenticateHeader = null;
        final List<String> authHeader = httpResponse.getHeader(Constants.PROXY_AUTHENTICATE);
        if (authHeader != null && !authHeader.isEmpty()) {
            authenticateHeader = authHeader.get(0);
        }

        if (authenticateHeader == null || authenticateHeader.equals("")) {
            throw new ProxyAuthenticationException(LocalizationMessages.PROXY_MISSING_AUTH_HEADER());
        }

        final String[] tokens = authenticateHeader.trim().split("\\s+", 2);
        final String scheme = tokens[0];

        String authorizationHeader;
        if (Constants.BASIC.equals(scheme)) {
            authorizationHeader = ProxyBasicAuthenticator
                    .generateAuthorizationHeader(proxyConfiguration.getUserName(), proxyConfiguration.getPassword());
        } else if (Constants.DIGEST.equals(scheme)) {
            String originalDestinationUri = getOriginalDestinationUri();
            URI uri = URI.create(originalDestinationUri);
            authorizationHeader = proxyDigestAuthenticator
                    .generateAuthorizationHeader(uri, Constants.CONNECT, authenticateHeader,
                            proxyConfiguration.getUserName(), proxyConfiguration.getPassword());
        } else {
            throw new ProxyAuthenticationException(LocalizationMessages.PROXY_UNSUPPORTED_SCHEME(scheme));
        }

        HttpRequest connectRequest = createConnectRequest();
        connectRequest.addHeaderIfNotPresent(Constants.PROXY_AUTHORIZATION, authorizationHeader);
        return connectRequest;
    }

    private HttpRequest createConnectRequest() {
        String originalDestinationUri = getOriginalDestinationUri();
        URI uri = URI.create(originalDestinationUri);
        HttpRequest connect = HttpRequest.createBodyless(Constants.CONNECT, uri);
        connect.addHeaderIfNotPresent(Constants.HOST, originalDestinationUri);
        connect.addHeaderIfNotPresent(Constants.PROXY_CONNECTION, Constants.KEEP_ALIVE);
        return connect;
    }

    private String getOriginalDestinationUri() {
        return String.format("%s:%d", originalDestinationAddress.getHostString(), originalDestinationAddress.getPort());
    }

    enum State {
        CONNECTING,
        AUTHENTICATED,
        CONNECTED
    }
}
