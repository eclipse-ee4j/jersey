/*
 * Copyright (c) 2024 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.client;

import org.glassfish.jersey.client.internal.HttpUrlConnector;
import org.glassfish.jersey.client.spi.Connector;
import org.glassfish.jersey.internal.MapPropertiesDelegate;
import org.glassfish.jersey.internal.PropertiesDelegate;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.Response;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public class SSLSocketFactoryTest {
    static final AtomicReference<SSLSocketFactory> factoryHolder = new AtomicReference<>();
    static SSLSocketFactory defaultSocketFactory = HttpsURLConnection.getDefaultSSLSocketFactory();

    // @Test
    // Alternative test
    // Check KeepAliveCache#get(URL url, Object obj)
    public void testSingleConnection() throws InterruptedException, IOException {
        Client client = ClientBuilder.newClient();

        for (int i = 0; i < 3; i++) {
            try (Response response = client.target("https://www.spiegel.de")
                    .request()
                    .get()) {

                response.readEntity(String.class);
                System.out.println(String.format("response = %s", response));
                Thread.sleep(1000);
            }
        }

        System.in.read();
    }

    @Test
    public void testSslContextFactoryOnClientIsSameForConsecutiveRequests() throws IOException, URISyntaxException {
        int firstRequestFactory, secondRequestFactory = 0;
        Client client = ClientBuilder.newClient();
        HttpUrlConnectorProvider.ConnectionFactory connectionFactory = (url) -> (HttpURLConnection) url.openConnection();
        SSLSocketFactoryConnector connector = (SSLSocketFactoryConnector) new SSlSocketFactoryUrlConnectorProvider()
                .createHttpUrlConnector(client, connectionFactory, 4096, true, false);
        URL url = new URL("https://somewhere.whereever:8080");
        URLConnection urlConnection = url.openConnection();

        // First Request
        connector.setSslContextFactory(client, new ClientRequest(url.toURI(),
                (ClientConfig) client.getConfiguration(), new MapPropertiesDelegate()));
        connector.secureConnection((JerseyClient) client, (HttpURLConnection) urlConnection);
        firstRequestFactory = factoryHolder.get().hashCode();

        // reset to the default socketFactory
        ((HttpsURLConnection) urlConnection).setSSLSocketFactory(defaultSocketFactory);

        // Second Request
        connector.setSslContextFactory(client, new ClientRequest(url.toURI(),
                (ClientConfig) client.getConfiguration(), new MapPropertiesDelegate()));
        connector.secureConnection((JerseyClient) client, (HttpURLConnection) urlConnection);
        secondRequestFactory = factoryHolder.get().hashCode();

        MatcherAssert.assertThat(firstRequestFactory, Matchers.equalTo(secondRequestFactory));
    }

    @Test
    public void testSslContextFactoryOnRequestIsSameForConsecutiveRequests() throws IOException, URISyntaxException {
        SSLSocketFactory firstRequestFactory, secondRequestFactory = null;
        Client client = ClientBuilder.newClient();
        SSLContext sslContext = new SslContextClientBuilder().build();
        HttpUrlConnectorProvider.ConnectionFactory connectionFactory = (url) -> (HttpURLConnection) url.openConnection();
        SSLSocketFactoryConnector connector = (SSLSocketFactoryConnector) new SSlSocketFactoryUrlConnectorProvider()
                .createHttpUrlConnector(client, connectionFactory, 4096, true, false);
        URL url = new URL("https://somewhere.whereever:8080");
        URLConnection urlConnection = url.openConnection();
        PropertiesDelegate propertiesDelegate = new MapPropertiesDelegate();
        propertiesDelegate.setProperty(ClientProperties.SSL_CONTEXT_SUPPLIER, (Supplier<SSLContext>) () -> sslContext);

        // First Request
        connector.setSslContextFactory(client, new ClientRequest(url.toURI(),
                (ClientConfig) client.getConfiguration(), propertiesDelegate));
        connector.secureConnection((JerseyClient) client, (HttpURLConnection) urlConnection);
        firstRequestFactory = factoryHolder.get();

        // reset to the default socketFactory
        ((HttpsURLConnection) urlConnection).setSSLSocketFactory(defaultSocketFactory);

        // Second Request
        connector.setSslContextFactory(client, new ClientRequest(url.toURI(),
                (ClientConfig) client.getConfiguration(), propertiesDelegate));
        connector.secureConnection((JerseyClient) client, (HttpURLConnection) urlConnection);
        secondRequestFactory = factoryHolder.get();

        MatcherAssert.assertThat(firstRequestFactory, Matchers.equalTo(secondRequestFactory));
    }

    private static class SSLSocketFactoryConnector extends HttpUrlConnector {
        public SSLSocketFactoryConnector(Client client, HttpUrlConnectorProvider.ConnectionFactory connectionFactory,
                                         int chunkSize, boolean fixLengthStreaming, boolean setMethodWorkaround) {
            super(client, connectionFactory, chunkSize, fixLengthStreaming, setMethodWorkaround);
        }

        @Override
        protected void secureConnection(JerseyClient client, HttpURLConnection uc) {
            super.secureConnection(client, uc);
            if (HttpsURLConnection.class.isInstance(uc)) {
                SSLSocketFactory factory = ((HttpsURLConnection) uc).getSSLSocketFactory();
                factoryHolder.set(factory);
            }
        }

        @Override
        protected void setSslContextFactory(Client client, ClientRequest request) {
            super.setSslContextFactory(client, request);
        }
    }

    private static class SSlSocketFactoryUrlConnectorProvider extends HttpUrlConnectorProvider {
        @Override
        protected Connector createHttpUrlConnector(Client client, ConnectionFactory connectionFactory, int chunkSize,
                                                   boolean fixLengthStreaming, boolean setMethodWorkaround) {
            return new SSLSocketFactoryConnector(
                    client,
                    connectionFactory,
                    chunkSize,
                    fixLengthStreaming,
                    setMethodWorkaround);
        }
    }
}
