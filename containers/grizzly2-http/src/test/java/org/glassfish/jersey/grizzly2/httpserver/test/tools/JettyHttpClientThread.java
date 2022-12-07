/*
 * Copyright (c) 2021, 2022 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2021 Payara Foundation and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.grizzly2.httpserver.test.tools;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpClientTransport;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.http.HttpClientTransportOverHTTP;
import org.eclipse.jetty.http2.client.HTTP2Client;
import org.eclipse.jetty.http2.client.http.HttpClientTransportOverHTTP2;
import org.eclipse.jetty.io.ClientConnector;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Jetty {@link HttpClient} supports both HTTP 1.1 and HTTP/2.
 *
 * @author David Matejcek
 */
public class JettyHttpClientThread extends ClientThread {

    private final HttpClient client;

    public JettyHttpClientThread(final ClientThreadSettings settings, final AtomicInteger counter,
        final AtomicReference<Throwable> error) throws Exception {
        super(settings, counter, error);
        this.client = createJettyClient(settings.secured, settings.useHttp2);
    }


    @Override
    protected void disconnect() {
        try {
            this.client.stop();
        } catch (Exception e) {
            throw new IllegalStateException("Could not stop the client: " + getName(), e);
        }
    }

    @Override
    public void doGetAndCheckResponse() throws Throwable {
        final ContentResponse response = this.client.GET(getSettings().targetUri);
        assertEquals(200, response.getStatus());
        assertEquals("Got it!", response.getContentAsString());
    }


    private static HttpClient createJettyClient(final boolean secured, final boolean useHttp2) throws Exception {
        if (!secured && !useHttp2) {
            final HttpClient httpClient = new HttpClient();
            httpClient.start();
            return httpClient;
        }

        final SslContextFactory.Client sslContextFactory = new SslContextFactory.Client();
        sslContextFactory.setSslContext(createSslContext());
        final ClientConnector connector = new ClientConnector();
        connector.setSslContextFactory(sslContextFactory);

        final HttpClientTransport transport;
        if (useHttp2) {
            transport = new HttpClientTransportOverHTTP2(new HTTP2Client(connector));
        } else {
            transport = new HttpClientTransportOverHTTP(connector);
        }

        final HttpClient httpClient = new HttpClient(transport);
        httpClient.start();
        return httpClient;
    }
}
