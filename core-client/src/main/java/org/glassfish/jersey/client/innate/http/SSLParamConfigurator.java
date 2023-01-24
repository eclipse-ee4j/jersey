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

import org.glassfish.jersey.client.ClientRequest;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocket;
import javax.ws.rs.core.UriBuilder;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A unified routines to configure {@link SSLParameters}.
 * To be reused in connectors.
 */
public final class SSLParamConfigurator {
    private final URI uri;
    private final Map<String, List<Object>> httpHeaders;
    private final Optional<SniConfigurator> sniConfigurator;

    /**
     * Builder of the {@link SSLParamConfigurator} instance.
     */
    public static final class Builder {
        private ClientRequest clientRequest;
        private URI uri;
        private Map<String, List<Object>> httpHeaders;
        private boolean setAlways = false;

        /**
         * Sets the {@link ClientRequest} instance.
         * @param clientRequest the {@link ClientRequest}
         * @return the builder instance
         */
        public Builder request(ClientRequest clientRequest) {
            this.clientRequest = clientRequest;
            this.httpHeaders = null;
            this.uri = null;
            return this;
        }

        /**
         * Sets the HTTP request {@link URI} instance.
         * @param uri The request uri
         * @return the builder instance
         */
        public Builder uri(URI uri) {
            this.clientRequest = null;
            this.uri = uri;
            return this;
        }

        /**
         * Sets the HTTP request headers
         * @param httpHeaders the http request headers
         * @return the builder instance
         */
        public Builder headers(Map<String, List<Object>> httpHeaders) {
            this.clientRequest = null;
            this.httpHeaders = httpHeaders;
            return this;
        }

        /**
         * Sets SNI only when {@link javax.ws.rs.core.HttpHeaders#HOST} differs from the request host name if set to
         * {@code false}. Default is {@code false}.
         * @param setAlways set SNI always (default)
         * @return the builder instance
         */
        public Builder setSNIAlways(boolean setAlways) {
            this.setAlways = setAlways;
            return this;
        }

        /**
         * Builds the {@link SSLParamConfigurator} instance.
         * @return the configured {@link SSLParamConfigurator} instance.
         */
        public SSLParamConfigurator build() {
            return new SSLParamConfigurator(this);
        }
    }

    private SSLParamConfigurator(SSLParamConfigurator.Builder builder) {
        this.uri = builder.clientRequest != null ? builder.clientRequest.getUri() : builder.uri;
        this.httpHeaders = builder.clientRequest != null ? builder.clientRequest.getHeaders() : builder.httpHeaders;
        sniConfigurator = SniConfigurator.createWhenHostHeader(uri, httpHeaders, builder.setAlways);
    }

    /**
     * Create a new instance of TlsSupport class
     **/
    public static SSLParamConfigurator.Builder builder() {
        return new SSLParamConfigurator.Builder();
    }

    /**
     * Get the host name either set by the request URI or by
     * {@link javax.ws.rs.core.HttpHeaders#HOST} header if it differs from HTTP request host name.
     * @return the hostName the {@link SSLEngine} is to use.
     */
    public String getSNIHostName() {
        return sniConfigurator.isPresent() ? sniConfigurator.get().getHostName() : uri.getHost();
    }

    /**
     * Replaces hostname within the {@link ClientRequest} uri with a resolved IP address. Should the hostname be not known,
     * the original request URI is returned. The purpose of this method is to replace the host with the IP so that
     * {code HttpUrlConnection} does not replace user defined {@link javax.net.ssl.SNIHostName} with the host from the request
     * uri.
     * @return the request uri with ip address of the resolved host.
     */
    public URI toIPRequestUri() {
        String host = uri.getHost();
        try {
            InetAddress ip = InetAddress.getByName(host);
            return UriBuilder.fromUri(uri).host(ip.getHostAddress()).build();
        } catch (UnknownHostException e) {
            return uri;
        }
    }

    /**
     * Return true iff SNI is to be set, i.e.
     * {@link javax.ws.rs.core.HttpHeaders#HOST} header if it differs from HTTP request host name.
     * @return Return {@code true} when {@link javax.net.ssl.SNIHostName} is to be set.
     */
    public boolean isSNIRequired() {
        return sniConfigurator.isPresent();
    }

    /**
     * Get the request URI or altered by {@link javax.ws.rs.core.HttpHeaders#HOST} header.
     * @return The possibly altered request URI.
     * @see #getSNIHostName()
     */
    public URI getSNIUri() {
        return sniConfigurator.isPresent() ? UriBuilder.fromUri(uri).host(getSNIHostName()).build() : uri;
    }

    /**
     * Set {@link javax.net.ssl.SNIServerName} for the {@link SSLParameters} when SNI should be used
     * (i.e. {@link javax.ws.rs.core.HttpHeaders#HOST} differs from HTTP request host name)
     * @param sslEngine the {@link SSLEngine} the {@link SSLParameters} are set for.
     */
    public void setSNIServerName(SSLEngine sslEngine) {
        sniConfigurator.ifPresent(sni -> sni.setServerNames(sslEngine));
    }


    /**
     * Set {@link javax.net.ssl.SNIServerName} for the {@link SSLParameters} when SNI should be used
     * (i.e. {@link javax.ws.rs.core.HttpHeaders#HOST} differs from HTTP request host name)
     * @param sslSocket the {@link SSLSocket} the {@link SSLParameters} are set for.
     */
    public void setSNIServerName(SSLSocket sslSocket) {
        sniConfigurator.ifPresent(sni -> sni.setServerNames(sslSocket));
    }

    /**
     * Set setEndpointIdentificationAlgorithm to HTTPS. This is to prevent man-in-the-middle attacks.
     * @param sslEngine the {@link SSLEngine} the algorithm is set for.
     * @see SSLParameters#setEndpointIdentificationAlgorithm(String)
     */
    public void setEndpointIdentificationAlgorithm(SSLEngine sslEngine) {
        SSLParameters sslParameters = sslEngine.getSSLParameters();
        sslParameters.setEndpointIdentificationAlgorithm("HTTPS");
        sslEngine.setSSLParameters(sslParameters);
    }
}
