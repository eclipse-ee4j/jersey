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

package org.glassfish.jersey.client;

import org.glassfish.jersey.client.innate.ClientProxy;
import org.glassfish.jersey.client.innate.ConnectorConfiguration;
import org.glassfish.jersey.client.internal.HttpUrlConnector;
import org.glassfish.jersey.client.internal.HttpUrlConnectorConfiguration;
import org.glassfish.jersey.http.HttpHeaders;
import org.glassfish.jersey.internal.MapPropertiesDelegate;
import org.glassfish.jersey.internal.util.JdkVersion;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.net.ssl.SSLContext;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MultivaluedMap;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.ProtocolException;
import java.net.URI;
import java.net.URL;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class HttpUrlConnectorConfigurationTest {

    public static final String PREFIX = "test.";

    @Test
    public void testConnectTimeout() {
        final AtomicInteger result = new AtomicInteger(0);
        class RWConnect extends Conf.RW {
            @Override
            public int connectTimeout(ClientRequest request) {
                result.set(super.connectTimeout(request));
                throw new IllegalStateException();
            }

            @Override
            public RWConnect instance() {
                return new RWConnect();
            }

        }
        new TestClient(new RWConnect()).rw(rw -> rw.connectTimeout(1000)).request().apply();
        Assertions.assertEquals(1000, result.get());

        result.set(0);
        new TestClient(new RWConnect()).rw(rw -> rw.connectTimeout(1000))
                .client(c -> c.property(ClientProperties.CONNECT_TIMEOUT, 3000))
                .client(c -> c.property(ClientProperties.CONNECTOR_CONFIGURATION, new RWConnect().connectTimeout(2000)))
                .request().apply();
        Assertions.assertEquals(3000, result.get());

        result.set(0);
        new TestClient(new RWConnect()).rw(rw -> rw.connectTimeout(1000))
                .client(c -> c.property(ClientProperties.CONNECT_TIMEOUT, 3000))
                .client(c -> c.property(ClientProperties.CONNECTOR_CONFIGURATION, new RWConnect().connectTimeout(2000)))
                .request(r -> r.setProperty(ClientProperties.CONNECTOR_CONFIGURATION,
                        new RWConnect().connectTimeout(4000)))
                .apply();
        Assertions.assertEquals(3000, result.get());

        result.set(0);
        new TestClient(new RWConnect()).rw(rw -> rw.connectTimeout(1000))
                .client(c -> c.property(ClientProperties.CONNECTOR_CONFIGURATION, new RWConnect().connectTimeout(2000)))
                .request(r -> r.setProperty(ClientProperties.CONNECTOR_CONFIGURATION,
                        new RWConnect().connectTimeout(4000)))
                .apply();
        Assertions.assertEquals(4000, result.get());

        result.set(0);
        new TestClient(new RWConnect()).rw(rw -> rw.connectTimeout(1000))
                .client(c -> c.property(ClientProperties.CONNECTOR_CONFIGURATION, new RWConnect().connectTimeout(2000)))
                .request(r -> r.setProperty(ClientProperties.CONNECTOR_CONFIGURATION,
                        new RWConnect().connectTimeout(4000)))
                .request(r -> r.setProperty(ClientProperties.CONNECT_TIMEOUT, 5000))
                .apply();
        Assertions.assertEquals(5000, result.get());

        result.set(0);
        new TestClient(new RWConnect()).rw(rw -> rw.connectTimeout(1000).prefix(PREFIX))
                .client(c -> c.property(ClientProperties.CONNECTOR_CONFIGURATION,
                        HttpUrlConnectorProvider.config().connectTimeout(2000).prefix(PREFIX)))
                .request(r -> r.setProperty(ClientProperties.CONNECTOR_CONFIGURATION,
                        new RWConnect().connectTimeout(4000)))
                .apply();
        Assertions.assertEquals(2000, result.get());

        result.set(0);
        new TestClient(new RWConnect()).rw(rw -> rw.connectTimeout(1000).prefix(PREFIX))
                .client(c -> c.property(ClientProperties.CONNECTOR_CONFIGURATION, new RWConnect().connectTimeout(2000)))
                .request(r -> r.setProperty(ClientProperties.CONNECTOR_CONFIGURATION,
                        HttpUrlConnectorProvider.config().connectTimeout(4000).prefix(PREFIX)))
                .request(r -> r.setProperty(ClientProperties.CONNECT_TIMEOUT, 5000))
                .apply();
        Assertions.assertEquals(4000, result.get());
    }

    @Test
    public void testFollowRedirects() {
        final AtomicReference<Boolean> result = new AtomicReference<>();
        class RWFollow extends Conf.RW {

            @Override
            public boolean followRedirects(ClientRequest request) {
                result.set(super.followRedirects(request));
                throw new IllegalStateException();
            }

            @Override
            public RWFollow instance() {
                return new RWFollow();
            }
        }

        Request req;
        req = new TestClient(new RWFollow()).request().apply();
        Assertions.assertEquals(((RWFollow) new RWFollow().copy()).followRedirects(), result.get());

        result.set(null);
        req = new TestClient(new RWFollow().followRedirects(false)).request().apply();
        Assertions.assertEquals(false, result.get());

        result.set(null);
        req = new TestClient(new RWFollow().prefix(PREFIX).followRedirects(false))
                .request(r -> r.setProperty(PREFIX + ClientProperties.FOLLOW_REDIRECTS, true))
                .apply();
        Assertions.assertEquals(true, result.get());

        result.set(null);
        req = new TestClient(new RWFollow().prefix(PREFIX).followRedirects(false))
                .request(r -> r.setProperty(
                        PREFIX + ClientProperties.CONNECTOR_CONFIGURATION, new RWFollow().followRedirects(true)))
                .apply();
        Assertions.assertEquals(true, result.get());

        result.set(null);
        req = new TestClient(new RWFollow())
                .client(c -> c.property(ClientProperties.CONNECTOR_CONFIGURATION, new RWFollow().followRedirects(false)))
                .request(r -> r.setProperty(ClientProperties.CONNECTOR_CONFIGURATION,
                        new RWFollow().followRedirects(true)))
                .request(r -> r.setEntity(PREFIX)).apply();
        Assertions.assertEquals(true, result.get());

        result.set(null);
        req = new TestClient(new RWFollow().prefix(PREFIX))
                .client(c -> c.property(PREFIX + ClientProperties.FOLLOW_REDIRECTS, false))
                .request(r -> r.setProperty(PREFIX + ClientProperties.CONNECTOR_CONFIGURATION,
                        new RWFollow().followRedirects(true)))
                .request(r -> r.setEntity(PREFIX)).apply();
        Assertions.assertEquals(false, result.get());
    }

    @Test
    public void testProxy() {
        final AtomicReference<ClientProxy> result = new AtomicReference<>();
        class RWProxy extends Conf.RW {

            @Override
            public Optional<ClientProxy> proxy(ClientRequest request, URI requestUri) {
                Optional<ClientProxy> proxy = super.proxy(request, requestUri);
                result.set(proxy.orElse(null));
                throw new IllegalStateException();
            }

            @Override
            public RWProxy instance() {
                return new RWProxy();
            }
        }
        String proxyUri = "http://proxy.org:8080";
        String userName = "USERNAME";
        String password = "PASSWORD";

        new TestClient(new RWProxy()).request().apply();
        Assertions.assertNull(result.get());

        result.set(null);
        new TestClient(new RWProxy()).rw(rw -> rw.proxyUri(proxyUri).proxyUserName(userName).proxyPassword(password))
                .request().apply();
        Assertions.assertEquals(proxyUri, result.get().uri().toASCIIString());
        Assertions.assertEquals(userName, result.get().userName());
        Assertions.assertEquals(password, result.get().password());

        result.set(null);
        new TestClient(new RWProxy()).rw(rw -> rw.prefix(PREFIX).proxyUri(proxyUri).proxyUserName("XXX").proxyPassword(password))
                .request(r -> r.setProperty(PREFIX + ClientProperties.PROXY_USERNAME, userName))
                .request(r -> r.setProperty(ClientProperties.PROXY_PASSWORD, "XXX"))
                .apply();
        Assertions.assertEquals(proxyUri, result.get().uri().toASCIIString());
        Assertions.assertEquals(userName, result.get().userName());
        Assertions.assertEquals(password, result.get().password());

        result.set(null);
        new TestClient(new RWProxy()).rw(rw -> rw.prefix(PREFIX).proxyUri(proxyUri).proxyUserName("XXX").proxyPassword(password))
                .request(r -> r.setProperty(PREFIX + ClientProperties.CONNECTOR_CONFIGURATION,
                        HttpUrlConnectorProvider.config().proxyUserName(userName).proxyPassword(null)))
                .apply();
        Assertions.assertEquals(proxyUri, result.get().uri().toASCIIString());
        Assertions.assertEquals(userName, result.get().userName());
        Assertions.assertNull(result.get().password());

        result.set(null);
        new TestClient(new RWProxy().prefix(PREFIX)
                .proxy(new java.net.Proxy(java.net.Proxy.Type.HTTP,
                        new InetSocketAddress(proxyUri.split("g:")[0] + "g", Integer.parseInt(proxyUri.split("g:")[1])))))
                .request(r -> r.setProperty(ClientProperties.CONNECTOR_CONFIGURATION,
                        HttpUrlConnectorProvider.config().proxyUserName(userName).proxyPassword(password).prefix(PREFIX)))
                .apply();
        Assertions.assertEquals(proxyUri, result.get().uri().toASCIIString());
        Assertions.assertEquals(userName, result.get().userName());
        Assertions.assertEquals(password, result.get().password());
    }

    @Test
    public void testReadTimeout() {
        final AtomicInteger result = new AtomicInteger(0);
        class RWRead extends Conf.RW {

            @Override
            public RWRead readTimeout(ClientRequest clientRequest) {
                super.readTimeout(clientRequest);
                result.set(readTimeout());
                throw new IllegalStateException();
            }

            @Override
            public RWRead instance() {
                return new RWRead();
            }
        }
        new TestClient(new RWRead()).rw(rw -> rw.readTimeout(1000)).request().apply();
        Assertions.assertEquals(1000, result.get());

        result.set(0);
        new TestClient(new RWRead()).rw(rw -> rw.readTimeout(1000))
                .client(c -> c.property(ClientProperties.READ_TIMEOUT, 3000))
                .client(c -> c.property(ClientProperties.CONNECTOR_CONFIGURATION, new RWRead().readTimeout(2000)))
                .request().apply();
        Assertions.assertEquals(3000, result.get());

        result.set(0);
        new TestClient(new RWRead()).rw(rw -> rw.readTimeout(1000))
                .client(c -> c.property(ClientProperties.READ_TIMEOUT, 3000))
                .client(c -> c.property(ClientProperties.CONNECTOR_CONFIGURATION, new RWRead().connectTimeout(2000)))
                .request(r -> r.setProperty(ClientProperties.CONNECTOR_CONFIGURATION, new RWRead().readTimeout(4000)))
                .apply();
        Assertions.assertEquals(3000, result.get());

        result.set(0);
        new TestClient(new RWRead()).rw(rw -> rw.readTimeout(1000))
                .client(c -> c.property(ClientProperties.CONNECTOR_CONFIGURATION, new RWRead().readTimeout(2000)))
                .request(r -> r.setProperty(ClientProperties.CONNECTOR_CONFIGURATION, new RWRead().readTimeout(4000)))
                .apply();
        Assertions.assertEquals(4000, result.get());

        result.set(0);
        new TestClient(new RWRead()).rw(rw -> rw.readTimeout(1000))
                .client(c -> c.property(ClientProperties.CONNECTOR_CONFIGURATION, new RWRead().readTimeout(2000)))
                .request(r -> r.setProperty(ClientProperties.CONNECTOR_CONFIGURATION, new RWRead().readTimeout(4000)))
                .request(r -> r.setProperty(ClientProperties.READ_TIMEOUT, 5000))
                .apply();
        Assertions.assertEquals(5000, result.get());

        result.set(0);
        new TestClient(new RWRead()).rw(rw -> rw.readTimeout(1000).prefix(PREFIX))
                .client(c -> c.property(ClientProperties.CONNECTOR_CONFIGURATION,
                        HttpUrlConnectorProvider.config().readTimeout(2000).prefix(PREFIX)))
                .request(r -> r.setProperty(ClientProperties.CONNECTOR_CONFIGURATION,
                        new RWRead().readTimeout(4000)))
                .apply();
        Assertions.assertEquals(2000, result.get());

        result.set(0);
        new TestClient(new RWRead()).rw(rw -> rw.readTimeout(1000).prefix(PREFIX))
                .client(c -> c.property(ClientProperties.CONNECTOR_CONFIGURATION, new RWRead().readTimeout(2000)))
                .request(r -> r.setProperty(ClientProperties.CONNECTOR_CONFIGURATION,
                        HttpUrlConnectorProvider.config().readTimeout(4000).prefix(PREFIX)))
                .request(r -> r.setProperty(ClientProperties.READ_TIMEOUT, 5000))
                .apply();
        Assertions.assertEquals(4000, result.get());
    }

    @Test
    public void testRequestEntityProcessing() {
        final AtomicReference<RequestEntityProcessing> result = new AtomicReference<>();
        final AtomicReference<Conf.RW> config = new AtomicReference<>();
        class RWRP extends Conf.RW {

            @Override
            public RequestEntityProcessing requestEntityProcessing(ClientRequest request) {
                result.set(super.requestEntityProcessing(request));
                throw new IllegalStateException();
            }

            @Override
            public RWRP instance() {
                config.set(new RWRP());
                return (RWRP) config.get();
            }
        }

        Request req;
        req = new TestClient(new RWRP().requestEntityProcessing(RequestEntityProcessing.CHUNKED))
                .request(r -> r.setEntity(PREFIX)).apply();
        Assertions.assertEquals(RequestEntityProcessing.CHUNKED, result.get());

        result.set(null);
        req = new TestClient(new RWRP().prefix(PREFIX).requestEntityProcessing(RequestEntityProcessing.CHUNKED))
                .request(r -> r.setEntity(PREFIX))
                .request(r -> r.setProperty(ClientProperties.CONNECTOR_CONFIGURATION,
                        HttpUrlConnectorProvider.config()
                                .requestEntityProcessing(RequestEntityProcessing.BUFFERED).prefix(PREFIX)))
                .apply();
        Assertions.assertEquals(RequestEntityProcessing.BUFFERED, result.get());

        result.set(null);
        req = new TestClient(new RWRP().prefix(PREFIX).requestEntityProcessing(RequestEntityProcessing.CHUNKED))
                .request(r -> r.setEntity(PREFIX))
                .request(r -> r.setProperty(PREFIX + ClientProperties.REQUEST_ENTITY_PROCESSING,
                        RequestEntityProcessing.BUFFERED))
                .apply();
        Assertions.assertEquals(RequestEntityProcessing.BUFFERED, result.get());
    }

    @Test
    public void testSslContext() {
        final SSLContext testContext = new SSLContext(null, null, null){};
        final AtomicReference<SSLContext> result = new AtomicReference<>();
        final AtomicReference<Conf.RW> config = new AtomicReference<>();
        class RWSsl extends Conf.RW {

            @Override
            public SSLContext sslContext(Client client, ClientRequest request) {
                result.set(super.sslContext(client, request));
                return result.get();
            }

            @Override
            public boolean isSslContextSupplier() {
                throw new IllegalStateException();
            }

            @Override
            public RWSsl instance() {
                RWSsl rw = new RWSsl();
                config.set(rw);
                return rw;
            }
        }

        Request req;
        req = new TestClient(new RWSsl().sslContextSupplier(() -> testContext)).request().apply();
        config.get().sslContext(req.client, req.request);
        Assertions.assertEquals(testContext, result.get());

        result.set(null);
        req = new TestClient(new RWSsl().prefix(PREFIX))
                .request(r -> r.setProperty(PREFIX + ClientProperties.SSL_CONTEXT_SUPPLIER,
                        (Supplier<SSLContext>) () -> testContext)).apply();
        config.get().sslContext(req.client, req.request);
        Assertions.assertEquals(testContext, result.get());

        result.set(null);
        req = new TestClient(new RWSsl().prefix(PREFIX))
                .request(r -> r.setProperty(ClientProperties.CONNECTOR_CONFIGURATION,
                        new RWSsl().prefix(PREFIX).sslContextSupplier(() -> testContext))).apply();
        config.get().sslContext(req.client, req.request);
        Assertions.assertEquals(testContext, result.get());
    }

    @Test
    public void testConnectionFactory() {
        final AtomicReference<Boolean> result = new AtomicReference<>();
        final HttpUrlConnectorProvider.ConnectionFactory connectionFactory = new HttpUrlConnectorProvider.ConnectionFactory() {
            @Override
            public HttpURLConnection getConnection(URL url) throws IOException {
                result.set(true);
                throw new IllegalStateException();
            }
        };

        new TestClient(HttpUrlConnectorProvider.config().connectionFactory(connectionFactory)).request().apply();
        Assertions.assertTrue(result.get());

        result.set(null);
        new TestClient(HttpUrlConnectorProvider.config().prefix(PREFIX))
                .client(c -> c.property(ClientProperties.CONNECTOR_CONFIGURATION,
                        HttpUrlConnectorProvider.config().connectionFactory(connectionFactory)))
                .request().apply();
        Assertions.assertNull(result.get());

        result.set(null);
        new TestClient(HttpUrlConnectorProvider.config().prefix(PREFIX))
                .client(c -> c.property(ClientProperties.CONNECTOR_CONFIGURATION,
                        HttpUrlConnectorProvider.config().connectionFactory(connectionFactory).prefix(PREFIX)))
                .request().apply();
        Assertions.assertTrue(result.get());

        result.set(null);
        new TestClient(HttpUrlConnectorProvider.config().prefix(PREFIX))
                .request(r -> r.setProperty(ClientProperties.CONNECTOR_CONFIGURATION,
                        HttpUrlConnectorProvider.config().connectionFactory(connectionFactory).prefix(PREFIX)))
                .apply();
        Assertions.assertTrue(result.get());

        result.set(null);
        Client client = ClientBuilder.newClient();
        try {
            HttpUrlConnectorProvider.config().build()
                    .connectionFactory(connectionFactory)
                    .getConnector(client, client.getConfiguration()).apply(Request.createRequest(client));
        } catch (IllegalStateException ise) {
            // expected
        }
        Assertions.assertTrue(result.get());
    }

    @Test
    public void testChunkSize() {
        final AtomicReference<Integer> result = new AtomicReference<>();
        final HttpUrlConnectorProvider.ConnectionFactory connectionFactory = new HttpUrlConnectorProvider.ConnectionFactory() {
            @Override
            public HttpURLConnection getConnection(URL url) throws IOException {
                return new HttpURLConnection(url) {
                    @Override
                    public void disconnect() {

                    }

                    @Override
                    public boolean usingProxy() {
                        return false;
                    }

                    @Override
                    public void connect() throws IOException {

                    }

                    @Override
                    public void setChunkedStreamingMode(int chunklen) {
                        result.set(chunklen);
                        throw new IllegalStateException();
                    }
                };
            }
        };

        new TestClient(HttpUrlConnectorProvider.config()
                .requestEntityProcessing(RequestEntityProcessing.CHUNKED)
                .connectionFactory(connectionFactory))
                .request(r -> r.setEntity(PREFIX)).apply();
        Assertions.assertEquals(ClientProperties.DEFAULT_CHUNK_SIZE, result.get());

        result.set(null);
        new TestClient(HttpUrlConnectorProvider.config()
                .requestEntityProcessing(RequestEntityProcessing.CHUNKED)
                .connectionFactory(connectionFactory)
                .chunkSize(1000))
                .request(r -> r.setEntity(PREFIX)).apply();
        Assertions.assertEquals(1000, result.get());

        result.set(null);
        new TestClient(HttpUrlConnectorProvider.config()
                .requestEntityProcessing(RequestEntityProcessing.CHUNKED)
                .connectionFactory(connectionFactory)
                .chunkSize(1000)
                .prefix(PREFIX))
                .client(c -> c.property(PREFIX + ClientProperties.CHUNKED_ENCODING_SIZE, 2000))
                .request(r -> r.setEntity(PREFIX)).apply();
        Assertions.assertEquals(2000, result.get());

        result.set(null);
        new TestClient(HttpUrlConnectorProvider.config()
                .requestEntityProcessing(RequestEntityProcessing.CHUNKED)
                .connectionFactory(connectionFactory)
                .chunkSize(1000)
                .prefix(PREFIX))
                .request(r -> r.setProperty(PREFIX + ClientProperties.CHUNKED_ENCODING_SIZE, 2000))
                .request(r -> r.setEntity(PREFIX)).apply();
        Assertions.assertEquals(2000, result.get());

        result.set(null);
        new TestClient(HttpUrlConnectorProvider.config()
                .requestEntityProcessing(RequestEntityProcessing.CHUNKED)
                .connectionFactory(connectionFactory)
                .chunkSize(1000)
                .prefix(PREFIX))
                .client(c -> c.property(ClientProperties.CONNECTOR_CONFIGURATION,
                        HttpUrlConnectorProvider.config().chunkSize(2000).prefix(PREFIX)))
                .request(r -> r.setEntity(PREFIX)).apply();
        Assertions.assertEquals(2000, result.get());

        result.set(null);
        new TestClient(HttpUrlConnectorProvider.config()
                .requestEntityProcessing(RequestEntityProcessing.CHUNKED)
                .connectionFactory(connectionFactory)
                .chunkSize(1000)
                .prefix(PREFIX))
                .request(r -> r.setProperty(ClientProperties.CONNECTOR_CONFIGURATION,
                        HttpUrlConnectorProvider.config().chunkSize(2000).prefix(PREFIX)))
                .request(r -> r.setEntity(PREFIX)).apply();
        Assertions.assertEquals(2000, result.get());
    }

    @Test
    public void testUseFixedLengthStreaming() {
        final AtomicReference<Integer> result = new AtomicReference<>();
        final class FixedLengthStreamingUrlConnection extends HttpURLConnection {
            private FixedLengthStreamingUrlConnection(URL u) {
                super(u);
            }

            @Override
            public void disconnect() {

            }

            @Override
            public boolean usingProxy() {
                return false;
            }

            @Override
            public void connect() throws IOException {

            }

            @Override
            public void setFixedLengthStreamingMode(long contentLength) {
                super.setFixedLengthStreamingMode(contentLength);
                result.set((int) contentLength);
                throw new IllegalStateException();
            }
        }
        final HttpUrlConnectorProvider.ConnectionFactory connectionFactory = new HttpUrlConnectorProvider.ConnectionFactory() {
            @Override
            public HttpURLConnection getConnection(URL url) throws IOException {
                return new FixedLengthStreamingUrlConnection(url);
            }
        };

        new TestClient(HttpUrlConnectorProvider.config()
                .connectionFactory(connectionFactory))
                .request(r -> r.getRequestHeaders().add(HttpHeaders.CONTENT_LENGTH, String.valueOf(PREFIX.length())))
                .request(r -> r.setEntity(PREFIX)).apply();
        Assertions.assertNull(result.get());

        result.set(null);
        new TestClient(HttpUrlConnectorProvider.config()
                .connectionFactory(connectionFactory)
                .useFixedLengthStreaming(true))
                .request(r -> r.getRequestHeaders().add(HttpHeaders.CONTENT_LENGTH, String.valueOf(PREFIX.length())))
                .request(r -> r.setEntity(PREFIX)).apply();
        Assertions.assertEquals(PREFIX.length(), result.get());

        result.set(null);
        new TestClient(HttpUrlConnectorProvider.config()
                .connectionFactory(connectionFactory)
                .useFixedLengthStreaming(false)
                .prefix(PREFIX))
                .client(c -> c.property(PREFIX + HttpUrlConnectorProvider.USE_FIXED_LENGTH_STREAMING, true))
                .request(r -> r.getRequestHeaders().add(HttpHeaders.CONTENT_LENGTH, String.valueOf(PREFIX.length())))
                .request(r -> r.setEntity(PREFIX)).apply();
        Assertions.assertEquals(PREFIX.length(), result.get());

        result.set(null);
        new TestClient(HttpUrlConnectorProvider.config()
                .connectionFactory(connectionFactory)
                .useFixedLengthStreaming(false)
                .prefix(PREFIX))
                .request(r -> r.setProperty(PREFIX + HttpUrlConnectorProvider.USE_FIXED_LENGTH_STREAMING, true))
                .request(r -> r.getRequestHeaders().add(HttpHeaders.CONTENT_LENGTH, String.valueOf(PREFIX.length())))
                .request(r -> r.setEntity(PREFIX)).apply();
        Assertions.assertEquals(PREFIX.length(), result.get());

        result.set(null);
        new TestClient(HttpUrlConnectorProvider.config()
                .connectionFactory(connectionFactory)
                .useFixedLengthStreaming(false)
                .prefix(PREFIX))
                .client(c -> c.property(ClientProperties.CONNECTOR_CONFIGURATION,
                        HttpUrlConnectorProvider.config().useFixedLengthStreaming(true).prefix(PREFIX)))
                .request(r -> r.getRequestHeaders().add(HttpHeaders.CONTENT_LENGTH, String.valueOf(PREFIX.length())))
                .request(r -> r.setEntity(PREFIX)).apply();
        Assertions.assertEquals(PREFIX.length(), result.get());

        result.set(null);
        new TestClient(HttpUrlConnectorProvider.config()
                .connectionFactory(connectionFactory)
                .useFixedLengthStreaming(false)
                .prefix(PREFIX))
                .request(r -> r.setProperty(ClientProperties.CONNECTOR_CONFIGURATION,
                        HttpUrlConnectorProvider.config().useFixedLengthStreaming(true).prefix(PREFIX)))
                .request(r -> r.getRequestHeaders().add(HttpHeaders.CONTENT_LENGTH, String.valueOf(PREFIX.length())))
                .request(r -> r.setEntity(PREFIX)).apply();
        Assertions.assertEquals(PREFIX.length(), result.get());
    }

    @Test
    public void testUseMethodWorkaround() {
        JdkVersion version = JdkVersion.getJdkVersion();
        if (!version.isReflectiveAccessToJdkSupported()) {
            return;
        }
        final String method = "XYZ";
        final AtomicReference<HttpURLConnection> result = new AtomicReference<>();
        final HttpUrlConnectorProvider.ConnectionFactory connectionFactory = new HttpUrlConnectorProvider.ConnectionFactory() {
            @Override
            public HttpURLConnection getConnection(URL url) throws IOException {
                return new HttpURLConnection(url) {
                    @Override
                    public void disconnect() {

                    }

                    @Override
                    public boolean usingProxy() {
                        return false;
                    }

                    @Override
                    public void connect() throws IOException {

                    }

                    @Override
                    public void setRequestMethod(String method) throws ProtocolException {
                        result.set(this);
                        super.setRequestMethod(method);
                        throw new IllegalStateException();
                    }
                };
            }
        };

        new TestClient(HttpUrlConnectorProvider.config()
                .connectionFactory(connectionFactory))
                .request(r -> r.setMethod(method))
                .apply();
        Assertions.assertNotEquals(method, result.get().getRequestMethod());

        new TestClient(HttpUrlConnectorProvider.config()
                .connectionFactory(connectionFactory)
                .useSetMethodWorkaround(true))
                .request(r -> r.setMethod(method))
                .apply();
        Assertions.assertEquals(method, result.get().getRequestMethod());

        HttpUrlConnectorProvider.Config config = HttpUrlConnectorProvider.config();
        config.build().useSetMethodWorkaround();
        new TestClient(config
                .connectionFactory(connectionFactory))
                .request(r -> r.setMethod(method))
                .apply();
        Assertions.assertEquals(method, result.get().getRequestMethod());

        new TestClient(HttpUrlConnectorProvider.config().prefix(PREFIX)
                .connectionFactory(connectionFactory))
                .request(r -> r.setMethod(method))
                .request(r -> r.setProperty(ClientProperties.CONNECTOR_CONFIGURATION,
                        HttpUrlConnectorProvider.config().useSetMethodWorkaround(true).prefix(PREFIX)))
                .apply();
        Assertions.assertEquals(method, result.get().getRequestMethod());

        new TestClient(HttpUrlConnectorProvider.config().prefix(PREFIX)
                .connectionFactory(connectionFactory))
                .client(c -> c.property(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true))
                .request(r -> r.setMethod(method))
                .apply();
        Assertions.assertNotEquals(method, result.get().getRequestMethod());

        new TestClient(HttpUrlConnectorProvider.config().prefix(PREFIX)
                .connectionFactory(connectionFactory))
                .client(c -> c.property(PREFIX + HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true))
                .request(r -> r.setMethod(method))
                .apply();
        Assertions.assertEquals(method, result.get().getRequestMethod());
    }

    private static class Conf extends HttpUrlConnectorConfiguration<Conf> {
        private static class RW extends ReadWrite {
            @Override
            public <X extends ConnectorConfiguration<?>> void setNonEmpty(X otherC) {
                super.setNonEmpty(otherC);
            }

            @Override
            public RW prefix(String prefix) {
                super.prefix(prefix);
                return this;
            }
        }
    }

    private static class TestClient {
        final Conf.RW rw;
        final Client client;

        private TestClient(ConnectorConfiguration<?> config) {
            this.rw = config instanceof Conf.RW ? (Conf.RW) config : new Conf.RW() {
                {
                    setNonEmpty(config);
                }
            };
            this.client = ClientBuilder.newClient(new ClientConfig());
        }

        public TestClient client(Consumer<Client> consumer) {
            consumer.accept(client);
            return this;
        }

        public TestClient rw(Consumer<HttpUrlConnectorConfiguration<?>> consumer) {
            consumer.accept(rw);
            return this;
        }

        public Request request() {
            return new Request(client, rw);
        }

        public Request request(Consumer<ClientRequest> consumer) {
            return request().request(consumer);
        }
    }

    private static class Request {
        final ClientRequest request;
        final Client client;
        final Conf.RW rw;

        Request(Client client, Conf.RW rw) {
            this.client = client;
            this.rw = rw;
            request = createRequest(client);
        }

        private static ClientRequest createRequest(Client client) {
            ClientRequest request =  new ClientRequest(URI.create("http://localhost:8080"),
                    (ClientConfig) client.getConfiguration(), new MapPropertiesDelegate()) {
                @Override
                public void writeEntity() throws IOException {
                    throw new IllegalStateException();
                }

                @Override
                public MultivaluedMap<String, String> getStringHeaders() {
                    throw new IllegalStateException();
                }
            };
            request.setMethod("POST");
            return request;
        }

        public Request request(Consumer<ClientRequest> consumer) {
            consumer.accept(request);
            return this;
        }

        public Request apply() {
            try {
                HttpUrlConnector connector = new HttpUrlConnector(client, client.getConfiguration(), rw);
                connector.apply(request);
            } catch (ProcessingException | IllegalStateException expected) {
            }
            return this;
        }
    }
}
