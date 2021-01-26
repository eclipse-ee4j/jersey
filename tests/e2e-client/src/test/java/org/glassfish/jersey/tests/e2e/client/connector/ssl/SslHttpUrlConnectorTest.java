/*
 * Copyright (c) 2015, 2021 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.e2e.client.connector.ssl;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.HttpUrlConnectorProvider;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.logging.LoggingFeature;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test custom socket factory in HttpUrlConnection using SSL
 *
 * @author Petr Bouda
 */
public class SslHttpUrlConnectorTest extends AbstractConnectorServerTest {

    /**
     * Test to see that the correct Http status is returned.
     *
     * @throws Exception in case of a test failure.
     */
    @Test
    public void testSSLWithCustomSocketFactory() throws Exception {
        final SSLContext sslContext = getSslContext();
        final CustomSSLSocketFactory socketFactory = new CustomSSLSocketFactory(sslContext);

        final ClientConfig cc = new ClientConfig()
                .connectorProvider(new HttpUrlConnectorProvider().connectionFactory(
                        new HttpUrlConnectorProvider.ConnectionFactory() {
                            @Override
                            public HttpURLConnection getConnection(final URL url) throws IOException {
                                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                                connection.setSSLSocketFactory(socketFactory);
                                return connection;
                            }
                        }));

        final Client client = ClientBuilder.newBuilder()
                .withConfig(cc)
                .sslContext(sslContext)
                .register(HttpAuthenticationFeature.basic("user", "password"))
                .register(LoggingFeature.class)
                .build();

        final Response response = client.target(Server.BASE_URI).path("/").request().get();
        assertEquals(200, response.getStatus());
        assertTrue(socketFactory.isVisited());
    }

    /**
     * Test for https://github.com/jersey/jersey/issues/3293
     *
     * @author Kevin Conaway
     */
    @Test
    public void testConcurrentRequestsWithCustomSSLContext() throws Exception {
        final SSLContext sslContext = getSslContext();

        final Client client = ClientBuilder.newBuilder()
            .sslContext(sslContext)
            .register(HttpAuthenticationFeature.basic("user", "password"))
            .register(LoggingFeature.class)
            .build();

        int numThreads = 5;
        CyclicBarrier barrier = new CyclicBarrier(numThreads);
        ExecutorService service = Executors.newFixedThreadPool(numThreads);
        List<Exception> exceptions = new CopyOnWriteArrayList<>();

        for (int i = 0; i < numThreads; i++) {
            service.submit(() -> {
                try {
                    barrier.await(1, TimeUnit.MINUTES);
                    for (int call = 0; call < 10; call++) {
                        final Response response = client.target(Server.BASE_URI).path("/").request().get();
                        assertEquals(200, response.getStatus());
                    }
                } catch (Exception ex) {
                    exceptions.add(ex);
                }
            });
        }

        service.shutdown();

        assertTrue(
            service.awaitTermination(1, TimeUnit.MINUTES)
        );

        assertTrue(
            toString(exceptions),
            exceptions.isEmpty()
        );
    }

    private String toString(List<Exception> exceptions) {
        StringWriter writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);

        exceptions.forEach(e -> e.printStackTrace(printWriter));

        return writer.toString();
    }

    public static class CustomSSLSocketFactory extends SSLSocketFactory {

        private boolean visited = false;

        private final SSLContext sslContext;

        protected CustomSSLSocketFactory(SSLContext sslContext) {
            this.sslContext = sslContext;
        }

        public boolean isVisited() {
            return visited;
        }

        @Override
        public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
            this.visited = true;
            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
            return sslSocketFactory.createSocket(s, host, port, autoClose);
        }

        @Override
        public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
            throw new UnsupportedOperationException("This createSocket method should not be invoked.");
        }

        @Override
        public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException {
            throw new UnsupportedOperationException("This createSocket method should not be invoked.");
        }

        @Override
        public Socket createSocket(InetAddress host, int port) throws IOException {
            throw new UnsupportedOperationException("This createSocket method should not be invoked.");
        }

        @Override
        public Socket createSocket(String host, int port) throws IOException {
            throw new UnsupportedOperationException("This createSocket method should not be invoked.");
        }

        @Override
        public String[] getDefaultCipherSuites() {
            return null;
        }

        @Override
        public String[] getSupportedCipherSuites() {
            return null;
        }
    }
}
