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

package org.glassfish.jersey.jetty.http2.connector;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpClientTransport;
import org.eclipse.jetty.http2.client.HTTP2Client;
import org.eclipse.jetty.http2.client.http.HttpClientTransportOverHTTP2;
import org.glassfish.jersey.jetty.connector.JettyConnector;
import org.glassfish.jersey.jetty.connector.JettyHttpClientContract;
import org.glassfish.jersey.jetty.connector.JettyHttpClientSupplier;

/**
 * HTTP/2 enabled version of the {@link JettyHttpClientSupplier}
 *
 * @since 2.41
 */
public class JettyHttp2ClientSupplier implements JettyHttpClientContract {
    private final HttpClient http2Client;

    /**
     * default Http2Client created for the supplier.
     */
    public JettyHttp2ClientSupplier() {
        this(createHttp2Client());
    }
    /**
     * supplier for the {@code HttpClient} with {@code HttpClientTransportOverHTTP2} to be optionally registered
     * to a {@link org.glassfish.jersey.client.ClientConfig}
     * @param http2Client a HttpClient to be supplied when {@link JettyConnector#getHttpClient()} is called.
     */
    public JettyHttp2ClientSupplier(HttpClient http2Client) {
        this.http2Client = http2Client;
    }

    private static final HttpClient createHttp2Client() {
        final HttpClientTransport transport =  new HttpClientTransportOverHTTP2(new HTTP2Client());
        return new HttpClient(transport);
    }

    @Override
    public HttpClient getHttpClient() {
        return http2Client;
    }
}