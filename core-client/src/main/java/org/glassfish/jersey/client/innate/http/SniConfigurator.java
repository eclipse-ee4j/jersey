/*
 * Copyright (c) 2023 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.client.innate.http;

import org.glassfish.jersey.client.internal.LocalizationMessages;

import javax.net.ssl.SNIHostName;
import javax.net.ssl.SNIServerName;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocket;
import javax.ws.rs.core.HttpHeaders;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * A unified routines to set {@link SNIHostName} for the {@link javax.net.ssl.SSLContext}.
 * To be reused in connectors.
 */
final class SniConfigurator {
    private static final Logger LOGGER = Logger.getLogger(SniConfigurator.class.getName());
    private final String hostName;
    private SniConfigurator(String hostName) {
        this.hostName = hostName;
    }

    /**
     * Get the hostName from the {@link HttpHeaders#HOST} header.
     * @return
     */
    String getHostName() {
        return hostName;
    }

    /**
     * Create ClientSNI when {@link HttpHeaders#HOST} is set different from the request URI host (or {@code whenDiffer}.is false).
     * @param hostUri the Uri of the HTTP request
     * @param headers the HttpHeaders
     * @param whenDiffer create {@SniConfigurator only when different from the request URI host}
     * @return ClientSNI or empty when {@link HttpHeaders#HOST}
     */
    static Optional<SniConfigurator> createWhenHostHeader(URI hostUri, Map<String, List<Object>> headers, boolean whenDiffer) {
        List<Object> hostHeaders = headers.get(HttpHeaders.HOST);
        if (hostHeaders == null || hostHeaders.get(0) == null) {
            return Optional.empty();
        }

        final String hostHeader = hostHeaders.get(0).toString();
        final String trimmedHeader;
        if (hostHeader != null) {
            int index = hostHeader.indexOf(':'); // RFC 7230  Host = uri-host [ ":" port ] ;
            final String trimmedHeader0 = index != -1 ? hostHeader.substring(0, index).trim() : hostHeader.trim();
            trimmedHeader = trimmedHeader0.isEmpty() ? hostHeader : trimmedHeader0;
        } else {
            return Optional.empty();
        }

        final String hostUriString = hostUri.getHost();
        if (!whenDiffer && hostUriString.equals(trimmedHeader)) {
            return Optional.empty();
        }

        return Optional.of(new SniConfigurator(trimmedHeader));
    }

    /**
     * Set {@link SNIServerName} for the given {@link SSLEngine} SSLParameters.
     * @param sslEngine
     */
    void setServerNames(SSLEngine sslEngine) {
        SSLParameters sslParameters = sslEngine.getSSLParameters();
        updateSSLParameters(sslParameters);
        sslEngine.setSSLParameters(sslParameters);
        LOGGER.fine(LocalizationMessages.SNI_ON_SSLENGINE());
    }

    /**
     * Set {@link SNIServerName} for the given {@link SSLSocket} SSLParameters.
     * @param sslSocket
     */
    void setServerNames(SSLSocket sslSocket) {
        SSLParameters sslParameters = sslSocket.getSSLParameters();
        updateSSLParameters(sslParameters);
        sslSocket.setSSLParameters(sslParameters);
        LOGGER.fine(LocalizationMessages.SNI_ON_SSLSOCKET());
    }

    private SSLParameters updateSSLParameters(SSLParameters sslParameters) {
        SNIHostName serverName = new SNIHostName(hostName);
        List<SNIServerName> serverNames = new LinkedList<>();
        serverNames.add(serverName);

        sslParameters.setServerNames(serverNames);
        LOGGER.finer(LocalizationMessages.SNI_UPDATE_SSLPARAMS(hostName));

        return sslParameters;
    }

}
