/*
 * Copyright (c) 2023, 2024 Oracle and/or its affiliates. All rights reserved.
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
import jakarta.ws.rs.core.HttpHeaders;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;
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
     * Create {@link SniConfigurator} when {@code sniHost} is set different from the request URI host
     * (or {@code whenDiffer}.is false).
     * @param hostUri the Uri of the HTTP request
     * @param sniHost the preferred host name to create the {@link SNIHostName}
     * @param whenDiffer create {@SniConfigurator only when different from the request URI host}
     * @return Optional {@link SniConfigurator} or empty when {@code sniHost} is equal to the requestHost
     */
    static Optional<SniConfigurator> createWhenHostHeader(URI hostUri, String sniHost, boolean whenDiffer) {
        final String trimmedHeader;
        if (sniHost != null) {
            int index = sniHost.indexOf(':'); // RFC 7230  Host = uri-host [ ":" port ] ;
            final String trimmedHeader0 = index != -1 ? sniHost.substring(0, index).trim() : sniHost.trim();
            trimmedHeader = trimmedHeader0.isEmpty() ? sniHost : trimmedHeader0;
        } else {
            return Optional.empty();
        }

        if (hostUri != null) {
            final String hostUriString = hostUri.getHost();
            if (!whenDiffer && hostUriString.equals(trimmedHeader)) {
                return Optional.empty();
            }
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

    SSLParameters updateSSLParameters(SSLParameters sslParameters) {
        SNIHostName serverName = new SNIHostName(hostName);
        List<SNIServerName> serverNames = new LinkedList<>();
        serverNames.add(serverName);

        sslParameters.setServerNames(serverNames);
        LOGGER.finer(LocalizationMessages.SNI_UPDATE_SSLPARAMS(hostName));

        return sslParameters;
    }

}
