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

package org.glassfish.jersey.netty.connector;

import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpRequest;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.ClientRequest;
import org.glassfish.jersey.client.ClientResponse;
import org.glassfish.jersey.client.RequestEntityProcessing;
import org.glassfish.jersey.client.innate.ClientProxy;
import org.glassfish.jersey.internal.MapPropertiesDelegate;
import org.glassfish.jersey.internal.util.collection.Ref;
import org.glassfish.jersey.client.innate.ConnectorConfiguration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.net.ssl.SSLContext;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Configuration;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ConnectorConfigTest {

    private static final String PREFIX = "test.";

    private static ClientRequest createRequest(Client client) {
        return new ClientRequest(URI.create("http://localhost:8080"),
                (ClientConfig) client.getConfiguration(), new MapPropertiesDelegate()) {
        };
    }

    @Test
    public void testPrefixedConfig() {
        ConnectorConfig.RW r = new ConnectorConfig.RW();
        Configuration prefixed, unprefixed;

        unprefixed = r.config();
        unprefixed.getProperties().put(ClientProperties.CONNECT_TIMEOUT, 1000);
        prefixed = r.prefix(PREFIX).prefixedConfiguration(unprefixed);
        Assertions.assertNull(prefixed.getProperty(ClientProperties.CONNECT_TIMEOUT));
        Assertions.assertNull(prefixed.getProperties().get(ClientProperties.CONNECT_TIMEOUT));

        unprefixed = r.config();
        unprefixed.getProperties().put(PREFIX + ClientProperties.CONNECT_TIMEOUT, 2000);
        prefixed = r.prefix(PREFIX).prefixedConfiguration(unprefixed);
        Assertions.assertEquals(2000, prefixed.getProperty(ClientProperties.CONNECT_TIMEOUT));
        Assertions.assertEquals(2000, prefixed.getProperties().get(ClientProperties.CONNECT_TIMEOUT));

        unprefixed = r.config();
        prefixed = r.prefix(PREFIX).prefixedConfiguration(unprefixed);
        unprefixed.getProperties().put(ClientProperties.CONNECT_TIMEOUT, 2000);
        prefixed.getProperties().putAll(unprefixed.getProperties());
        Assertions.assertNull(prefixed.getProperty(ClientProperties.CONNECT_TIMEOUT));
        Assertions.assertNull(prefixed.getProperties().get(ClientProperties.CONNECT_TIMEOUT));

        unprefixed = r.config();
        prefixed = r.prefix(PREFIX).prefixedConfiguration(unprefixed);
        unprefixed.getProperties().put(PREFIX + ClientProperties.CONNECT_TIMEOUT, 2000);
        prefixed.getProperties().putAll(unprefixed.getProperties());
        Assertions.assertEquals(2000, prefixed.getProperty(ClientProperties.CONNECT_TIMEOUT));
        Assertions.assertEquals(2000, prefixed.getProperties().get(ClientProperties.CONNECT_TIMEOUT));

        unprefixed = r.config();
        prefixed = r.prefix(PREFIX).prefixedConfiguration(unprefixed);
        prefixed.getProperties().put(PREFIX + ClientProperties.PROXY_USERNAME, "USERNAME");
        Assertions.assertEquals("USERNAME", prefixed.getProperty(ClientProperties.PROXY_USERNAME));
        prefixed.getProperties().put(ClientProperties.PROXY_PASSWORD, "PASSWORD");
        Assertions.assertNull(prefixed.getProperty(ClientProperties.PROXY_PASSWORD));
    }

    @Test
    public void testAsyncThreadPoolSize() {
        AtomicInteger result = new AtomicInteger(0);
        class RWAsync extends RW {
            @Override
            public Integer asyncThreadPoolSize() {
                result.set(super.asyncThreadPoolSize());
                return super.asyncThreadPoolSize();
            }

            @Override
            public RWAsync instance() {
                return new RWAsync();
            }
        }

        Client client = ClientBuilder.newClient();
        NettyConnectorProvider.Config.RW rw0 = new RWAsync().asyncThreadPoolSize(10);
        new NettyConnector(client, rw0);
        Assertions.assertEquals(10, result.get());

        result.set(0);
        NettyConnectorProvider.Config.RW rw1 = new RWAsync().asyncThreadPoolSize(20);
        client.property(ClientProperties.CONNECTOR_CONFIGURATION, rw1);
        new NettyConnector(client, rw0);
        Assertions.assertEquals(20, result.get());

        result.set(0);
        client.property(ClientProperties.ASYNC_THREADPOOL_SIZE, 30);
        new NettyConnector(client, rw0);
        Assertions.assertEquals(30, result.get());
    }

    @Test
    public void testConnectTimeout() {
        final AtomicInteger result = new AtomicInteger(0);
        class RWConnect extends NettyConnectorProvider.Config.RW {
            @Override
            public int connectTimeout() {
                result.set(super.connectTimeout());
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
                .request(r -> r.setProperty(ClientProperties.CONNECTOR_CONFIGURATION, new RWConnect().connectTimeout(4000)))
                .apply();
        Assertions.assertEquals(3000, result.get());

        result.set(0);
        new TestClient(new RWConnect()).rw(rw -> rw.connectTimeout(1000))
                .client(c -> c.property(ClientProperties.CONNECTOR_CONFIGURATION, new RWConnect().connectTimeout(2000)))
                .request(r -> r.setProperty(ClientProperties.CONNECTOR_CONFIGURATION, new RWConnect().connectTimeout(4000)))
                .apply();
        Assertions.assertEquals(4000, result.get());

        result.set(0);
        new TestClient(new RWConnect()).rw(rw -> rw.connectTimeout(1000))
                .client(c -> c.property(ClientProperties.CONNECTOR_CONFIGURATION, new RWConnect().connectTimeout(2000)))
                .request(r -> r.setProperty(ClientProperties.CONNECTOR_CONFIGURATION, new RWConnect().connectTimeout(4000)))
                .request(r -> r.setProperty(ClientProperties.CONNECT_TIMEOUT, 5000))
                .apply();
        Assertions.assertEquals(5000, result.get());

        result.set(0);
        new TestClient(new RWConnect()).rw(rw -> rw.connectTimeout(1000).prefix(PREFIX))
                .client(c -> c.property(ClientProperties.CONNECTOR_CONFIGURATION,
                        new RWConnect().connectTimeout(2000).prefix(PREFIX)))
                .request(r -> r.setProperty(ClientProperties.CONNECTOR_CONFIGURATION,
                        new RWConnect().connectTimeout(4000)))
                .apply();
        Assertions.assertEquals(2000, result.get());

        result.set(0);
        new TestClient(new RWConnect()).rw(rw -> rw.connectTimeout(1000).prefix(PREFIX))
                .client(c -> c.property(ClientProperties.CONNECTOR_CONFIGURATION, new RWConnect().connectTimeout(2000)))
                .request(r -> r.setProperty(ClientProperties.CONNECTOR_CONFIGURATION,
                        new RWConnect().connectTimeout(4000).prefix(PREFIX)))
                .request(r -> r.setProperty(ClientProperties.CONNECT_TIMEOUT, 5000))
                .apply();
        Assertions.assertEquals(4000, result.get());
    }

    @Test
    public void testExpect100Continue() {
        final AtomicReference<Boolean> result = new AtomicReference<>();
        final AtomicReference<NettyConnectorProvider.Config.RW> config = new AtomicReference<>();
        class RWExpect extends RW {

            @Override
            public Boolean expect100Continue(ClientRequest request) {
                result.set(super.expect100Continue(request));
                return result.get();
            }

            @Override
            public int connectTimeout() {
                config.set(this);
                return super.connectTimeout();
            }

            @Override
            public RWExpect instance() {
                return new RWExpect();
            }
        }

        Request req = new TestClient(new RWExpect()).request(r -> r.setEntity(PREFIX)).apply();
        try {
            new Expect100ContinueConnectorExtension(config.get()).invoke(req.request, null);
        } catch (NullPointerException expected) {
        }
        Assertions.assertNull(result.get());

        result.set(null);
        req = new TestClient(new RWExpect().expect100Continue(true)).request(r -> r.setEntity(PREFIX)).apply();
        try {
            new Expect100ContinueConnectorExtension(config.get()).invoke(req.request, null);
        } catch (NullPointerException expected) {
        }
        Assertions.assertEquals(true, result.get());

        result.set(null);
        req = new TestClient(new RWExpect().expect100Continue(true).prefix(PREFIX))
                .request(r -> r.setProperty(PREFIX + ClientProperties.EXPECT_100_CONTINUE, false))
                .request(r -> r.setEntity(PREFIX)).apply();
        try {
            new Expect100ContinueConnectorExtension(config.get()).invoke(req.request, null);
        } catch (NullPointerException expected) {
        }
        Assertions.assertEquals(false, result.get());

        result.set(null);
        req = new TestClient(new RWExpect().expect100Continue(true).prefix(PREFIX))
                .request(r -> r.setProperty(
                        PREFIX + ClientProperties.CONNECTOR_CONFIGURATION, new RWExpect().expect100Continue(false)))
                .request(r -> r.setEntity(PREFIX)).apply();
        try {
            new Expect100ContinueConnectorExtension(config.get()).invoke(req.request, null);
        } catch (NullPointerException expected) {
        }
        Assertions.assertEquals(false, result.get());

        result.set(null);
        req = new TestClient(new RWExpect())
                .client(c -> c.property(ClientProperties.CONNECTOR_CONFIGURATION, new RWExpect().expect100Continue(true)))
                .request(r -> r.setProperty(ClientProperties.CONNECTOR_CONFIGURATION, new RWExpect().expect100Continue(false)))
                .request(r -> r.setEntity(PREFIX)).apply();
        try {
            new Expect100ContinueConnectorExtension(config.get()).invoke(req.request, null);
        } catch (NullPointerException expected) {
        }
        Assertions.assertEquals(false, result.get());

        result.set(null);
        req = new TestClient(new RWExpect().prefix(PREFIX))
                .client(c -> c.property(PREFIX + ClientProperties.EXPECT_100_CONTINUE, true))
                .request(r -> r.setProperty(PREFIX + ClientProperties.CONNECTOR_CONFIGURATION,
                        new RWExpect().expect100Continue(false)))
                .request(r -> r.setEntity(PREFIX)).apply();
        try {
            new Expect100ContinueConnectorExtension(config.get()).invoke(req.request, null);
        } catch (NullPointerException expected) {
        }
        Assertions.assertEquals(true, result.get());
    }

    @Test
    public void testExpect100ContinueThreshold() {
        final AtomicReference<Long> result = new AtomicReference<>();
        final AtomicReference<NettyConnectorProvider.Config.RW> config = new AtomicReference<>();
        class RWExpect extends RW {

            @Override
            public long expect100ContinueThreshold(ClientRequest request) {
                result.set(super.expect100ContinueThreshold(request));
                return result.get();
            }

            @Override
            public int connectTimeout() {
                config.set(this);
                return super.connectTimeout();
            }

            @Override
            public RWExpect instance() {
                return new RWExpect();
            }
        }

        Request req = new TestClient(new RWExpect()).request(r -> r.setEntity(PREFIX)).apply();
        try {
            new Expect100ContinueConnectorExtension(config.get()).invoke(req.request, (HttpRequest) null);
        } catch (NullPointerException expected) {
        }
        Assertions.assertEquals(ClientProperties.DEFAULT_EXPECT_100_CONTINUE_THRESHOLD_SIZE, result.get());

        result.set(null);
        req = new TestClient(new RWExpect().expect100ContinueThreshold(1000)).request(r -> r.setEntity(PREFIX)).apply();
        try {
            new Expect100ContinueConnectorExtension(config.get()).invoke(req.request, (HttpRequest) null);
        } catch (NullPointerException expected) {
        }
        Assertions.assertEquals(1000, result.get());

        result.set(null);
        req = new TestClient(new RWExpect().expect100ContinueThreshold(1000).prefix(PREFIX))
                .request(r -> r.setProperty(PREFIX + ClientProperties.EXPECT_100_CONTINUE_THRESHOLD_SIZE, 2000))
                .request(r -> r.setEntity(PREFIX)).apply();
        try {
            new Expect100ContinueConnectorExtension(config.get()).invoke(req.request, (HttpRequest) null);
        } catch (NullPointerException expected) {
        }
        Assertions.assertEquals(2000, result.get());

        result.set(null);
        req = new TestClient(new RWExpect().expect100ContinueThreshold(1000).prefix(PREFIX))
                .request(r -> r.setProperty(
                        PREFIX + ClientProperties.CONNECTOR_CONFIGURATION, new RWExpect().expect100ContinueThreshold(2000)))
                .request(r -> r.setEntity(PREFIX)).apply();
        try {
            new Expect100ContinueConnectorExtension(config.get()).invoke(req.request, (HttpRequest) null);
        } catch (NullPointerException expected) {
        }
        Assertions.assertEquals(2000, result.get());

        result.set(null);
        req = new TestClient(new RWExpect())
                .client(c -> c.property(ClientProperties.CONNECTOR_CONFIGURATION,
                        new RWExpect().expect100ContinueThreshold(1000)))
                .request(r -> r.setProperty(ClientProperties.CONNECTOR_CONFIGURATION,
                        new RWExpect().expect100ContinueThreshold(2000)))
                .request(r -> r.setEntity(PREFIX)).apply();
        try {
            new Expect100ContinueConnectorExtension(config.get()).invoke(req.request, (HttpRequest) null);
        } catch (NullPointerException expected) {
        }
        Assertions.assertEquals(2000, result.get());

        result.set(null);
        req = new TestClient(new RWExpect().prefix(PREFIX))
                .client(c -> c.property(PREFIX + ClientProperties.EXPECT_100_CONTINUE_THRESHOLD_SIZE, 1000))
                .request(r -> r.setProperty(PREFIX + ClientProperties.CONNECTOR_CONFIGURATION,
                        new RWExpect().expect100ContinueThreshold(2000)))
                .request(r -> r.setEntity(PREFIX)).apply();
        try {
            new Expect100ContinueConnectorExtension(config.get()).invoke(req.request, (HttpRequest) null);
        } catch (NullPointerException expected) {
        }
        Assertions.assertEquals(1000, result.get());
    }

    @Test
    public void testExpect100ContinueTimeout() {
        final AtomicReference<Integer> result = new AtomicReference<>();
        final AtomicReference<NettyConnectorProvider.Config.RW> config = new AtomicReference<>();
        class RWExpect extends RW {

            @Override
            public NettyConnectorProvider.Config.RW expect100ContinueTimeout(ClientRequest request) {
                super.expect100ContinueTimeout(request);
                result.set(this.expect100ContTimeout.get());
                return this;
            }

            @Override
            public int connectTimeout() {
                config.set(this);
                return super.connectTimeout();
            }

            @Override
            public RWExpect instance() {
                return new RWExpect();
            }
        }

        Request req = new TestClient(new RWExpect()).request(r -> r.setEntity(PREFIX)).apply();
        try {
            new Expect100ContinueConnectorExtension(config.get()).invoke(req.request, (HttpRequest) null);
        } catch (NullPointerException expected) {
        }
        Assertions.assertEquals(NettyClientProperties.DEFAULT_EXPECT_100_CONTINUE_TIMEOUT_VALUE, result.get());

        result.set(null);
        req = new TestClient(new RWExpect().expect100ContinueTimeout(1000)).request(r -> r.setEntity(PREFIX)).apply();
        try {
            new Expect100ContinueConnectorExtension(config.get()).invoke(req.request, (HttpRequest) null);
        } catch (NullPointerException expected) {
        }
        Assertions.assertEquals(1000, result.get());

        result.set(null);
        req = new TestClient(new RWExpect().expect100ContinueTimeout(1000).prefix(PREFIX))
                .request(r -> r.setProperty(PREFIX + NettyClientProperties.EXPECT_100_CONTINUE_TIMEOUT, 2000))
                .request(r -> r.setEntity(PREFIX)).apply();
        try {
            new Expect100ContinueConnectorExtension(config.get()).invoke(req.request, (HttpRequest) null);
        } catch (NullPointerException expected) {
        }
        Assertions.assertEquals(2000, result.get());

        result.set(null);
        req = new TestClient(new RWExpect().expect100ContinueTimeout(1000).prefix(PREFIX))
                .request(r -> r.setProperty(
                        PREFIX + ClientProperties.CONNECTOR_CONFIGURATION, new RWExpect().expect100ContinueTimeout(2000)))
                .request(r -> r.setEntity(PREFIX)).apply();
        try {
            new Expect100ContinueConnectorExtension(config.get()).invoke(req.request, (HttpRequest) null);
        } catch (NullPointerException expected) {
        }
        Assertions.assertEquals(2000, result.get());

        result.set(null);
        req = new TestClient(new RWExpect())
                .client(c -> c.property(ClientProperties.CONNECTOR_CONFIGURATION, new RWExpect().expect100ContinueTimeout(1000)))
                .request(r -> r.setProperty(ClientProperties.CONNECTOR_CONFIGURATION,
                        new RWExpect().expect100ContinueTimeout(2000)))
                .request(r -> r.setEntity(PREFIX)).apply();
        try {
            new Expect100ContinueConnectorExtension(config.get()).invoke(req.request, null);
        } catch (NullPointerException expected) {
        }
        Assertions.assertEquals(2000, result.get());

        result.set(null);
        req = new TestClient(new RWExpect().prefix(PREFIX))
                .client(c -> c.property(PREFIX + NettyClientProperties.EXPECT_100_CONTINUE_TIMEOUT, 1000))
                .request(r -> r.setProperty(PREFIX + ClientProperties.CONNECTOR_CONFIGURATION,
                        new RWExpect().expect100ContinueTimeout(2000)))
                .request(r -> r.setEntity(PREFIX)).apply();
        try {
            new Expect100ContinueConnectorExtension(config.get()).invoke(req.request, null);
        } catch (NullPointerException expected) {
        }
        Assertions.assertEquals(1000, result.get());
    }

    @Test
    public void testFollowRedirects() {
        final AtomicReference<Boolean> result = new AtomicReference<>();
        final AtomicReference<NettyConnectorProvider.Config.RW> config = new AtomicReference<>();
        class RWFollow extends RW {

            @Override
            public boolean followRedirects() {
                result.set(super.followRedirects());
                return result.get();
            }

            @Override
            public int connectTimeout() {
                config.set(this);
                return super.connectTimeout();
            }

            @Override
            public RWFollow instance() {
                return new RWFollow();
            }
        }

        Request req = new TestClient(new RWFollow()).request().apply();
        new JerseyClientHandler(req.request, new CompletableFuture<ClientResponse>(),
                new CompletableFuture<Object>(), new HashSet<>(), req.connector, config.get());
        config.get().followRedirects();
        Assertions.assertEquals(new RWFollow().followRedirects(), result.get());

        result.set(null);
        req = new TestClient(new RWFollow().followRedirects(false)).request().apply();
        new JerseyClientHandler(req.request, new CompletableFuture<ClientResponse>(),
                new CompletableFuture<Object>(), new HashSet<>(), req.connector, config.get());
        config.get().followRedirects();
        Assertions.assertEquals(false, result.get());

        result.set(null);
        req = new TestClient(new RWFollow().followRedirects(false).prefix(PREFIX))
                .request(r -> r.setProperty(PREFIX + ClientProperties.FOLLOW_REDIRECTS, true))
                .request(r -> r.setEntity(PREFIX)).apply();
        new JerseyClientHandler(req.request, new CompletableFuture<ClientResponse>(),
                new CompletableFuture<Object>(), new HashSet<>(), req.connector, config.get());
        config.get().followRedirects();
        Assertions.assertEquals(true, result.get());

        result.set(null);
        req = new TestClient(new RWFollow().followRedirects(false).prefix(PREFIX))
                .request(r -> r.setProperty(
                        PREFIX + ClientProperties.CONNECTOR_CONFIGURATION, new RWFollow().followRedirects(true)))
                .apply();
        new JerseyClientHandler(req.request, new CompletableFuture<ClientResponse>(),
                new CompletableFuture<Object>(), new HashSet<>(), req.connector, config.get());
        config.get().followRedirects();
        Assertions.assertEquals(true, result.get());

        result.set(null);
        req = new TestClient(new RWFollow())
                .client(c -> c.property(ClientProperties.CONNECTOR_CONFIGURATION, new RWFollow().followRedirects(false)))
                .request(r -> r.setProperty(ClientProperties.CONNECTOR_CONFIGURATION,
                        new RWFollow().followRedirects(true)))
                .request(r -> r.setEntity(PREFIX)).apply();
        new JerseyClientHandler(req.request, new CompletableFuture<ClientResponse>(),
                new CompletableFuture<Object>(), new HashSet<>(), req.connector, config.get());
        config.get().followRedirects();
        Assertions.assertEquals(true, result.get());

        result.set(null);
        req = new TestClient(new RWFollow().prefix(PREFIX))
                .client(c -> c.property(PREFIX + ClientProperties.FOLLOW_REDIRECTS, false))
                .request(r -> r.setProperty(PREFIX + ClientProperties.CONNECTOR_CONFIGURATION,
                        new RWFollow().followRedirects(true)))
                .request(r -> r.setEntity(PREFIX)).apply();
        new JerseyClientHandler(req.request, new CompletableFuture<ClientResponse>(),
                new CompletableFuture<Object>(), new HashSet<>(), req.connector, config.get());
        config.get().followRedirects();
        Assertions.assertEquals(false, result.get());
    }

    @Test
    public void testProxy() {
        final AtomicReference<ClientProxy> result = new AtomicReference<>();
        final AtomicReference<NettyConnectorProvider.Config.RW> config = new AtomicReference<>();
        class RWProxy extends RW {

            @Override
            public Optional<ClientProxy> proxy(ClientRequest request, URI requestUri) {
                Optional<ClientProxy> proxy = super.proxy(request, requestUri);
                result.set(proxy.orElse(null));
                return proxy;
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
        new TestClient(new RWProxy().proxyUri(proxyUri).proxyUserName(userName).proxyPassword(password)).request().apply();
        Assertions.assertEquals(proxyUri, result.get().uri().toASCIIString());
        Assertions.assertEquals(userName, result.get().userName());
        Assertions.assertEquals(password, result.get().password());

        result.set(null);
        new TestClient(new RWProxy().prefix(PREFIX).proxyUri(proxyUri).proxyUserName("XXX").proxyPassword(password))
                .request(r -> r.setProperty(PREFIX + ClientProperties.PROXY_USERNAME, userName))
                .request(r -> r.setProperty(ClientProperties.PROXY_PASSWORD, "XXX"))
                .apply();
        Assertions.assertEquals(proxyUri, result.get().uri().toASCIIString());
        Assertions.assertEquals(userName, result.get().userName());
        Assertions.assertEquals(password, result.get().password());

        result.set(null);
        new TestClient(new RWProxy().prefix(PREFIX).proxyUri(proxyUri).proxyUserName("XXX").proxyPassword(password))
                .request(r -> r.setProperty(PREFIX + ClientProperties.CONNECTOR_CONFIGURATION,
                        new RWProxy().proxyUserName(userName).proxyPassword(null)))
                .apply();
        Assertions.assertEquals(proxyUri, result.get().uri().toASCIIString());
        Assertions.assertEquals(userName, result.get().userName());
        Assertions.assertNull(result.get().password());

        result.set(null);
        new TestClient(new RWProxy().prefix(PREFIX)
                .proxy(new java.net.Proxy(java.net.Proxy.Type.HTTP,
                        new InetSocketAddress(proxyUri.split("g:")[0] + "g", Integer.parseInt(proxyUri.split("g:")[1])))))
                .request(r -> r.setProperty(ClientProperties.CONNECTOR_CONFIGURATION,
                        new RWProxy().proxyUserName(userName).proxyPassword(password).prefix(PREFIX)))
                .apply();
        Assertions.assertEquals(proxyUri, result.get().uri().toASCIIString());
        Assertions.assertEquals(userName, result.get().userName());
        Assertions.assertEquals(password, result.get().password());
    }

    @Test
    public void testReadTimeout() {
        final AtomicInteger result = new AtomicInteger(0);
        class RWRead extends NettyConnectorProvider.Config.RW {

            @Override
            public int readTimeout() {
                result.set(super.readTimeout());
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
                        new RWRead().readTimeout(2000).prefix(PREFIX)))
                .request(r -> r.setProperty(ClientProperties.CONNECTOR_CONFIGURATION,
                        new RWRead().readTimeout(4000)))
                .apply();
        Assertions.assertEquals(2000, result.get());

        result.set(0);
        new TestClient(new RWRead()).rw(rw -> rw.readTimeout(1000).prefix(PREFIX))
                .client(c -> c.property(ClientProperties.CONNECTOR_CONFIGURATION, new RWRead().readTimeout(2000)))
                .request(r -> r.setProperty(ClientProperties.CONNECTOR_CONFIGURATION,
                        new RWRead().readTimeout(4000).prefix(PREFIX)))
                .request(r -> r.setProperty(ClientProperties.READ_TIMEOUT, 5000))
                .apply();
        Assertions.assertEquals(4000, result.get());
    }

    @Test
    public void testRequestEntityProcessing() {
        final AtomicReference<RequestEntityProcessing> result = new AtomicReference<>();
        final AtomicReference<RW> config = new AtomicReference<>();
        class RWRP extends RW {

            @Override
            public RequestEntityProcessing requestEntityProcessing(ClientRequest request) {
               result.set(super.requestEntityProcessing(request));
               return result.get();
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
        try {
            req.connector.nettyEntityWriter(req.request, null, config.get());
        } catch (NullPointerException ignore) {
        }
        Assertions.assertEquals(RequestEntityProcessing.CHUNKED, result.get());

        result.set(null);
        req = new TestClient(new RWRP().prefix(PREFIX).requestEntityProcessing(RequestEntityProcessing.CHUNKED))
                .request(r -> r.setEntity(PREFIX))
                .request(r -> r.setProperty(ClientProperties.CONNECTOR_CONFIGURATION,
                        new RWRP().requestEntityProcessing(RequestEntityProcessing.BUFFERED).prefix(PREFIX)))
                .apply();
        try {
            req.connector.nettyEntityWriter(req.request, null, config.get());
        } catch (NullPointerException ignore) {
        }
        Assertions.assertEquals(RequestEntityProcessing.BUFFERED, result.get());

        result.set(null);
        req = new TestClient(new RWRP().prefix(PREFIX).requestEntityProcessing(RequestEntityProcessing.CHUNKED))
                .request(r -> r.setEntity(PREFIX))
                .request(r -> r.setProperty(PREFIX + ClientProperties.REQUEST_ENTITY_PROCESSING,
                        RequestEntityProcessing.BUFFERED))
                .apply();
        try {
            req.connector.nettyEntityWriter(req.request, null, config.get());
        } catch (NullPointerException ignore) {
        }
        Assertions.assertEquals(RequestEntityProcessing.BUFFERED, result.get());
    }

    @Test
    public void testSslContext() {
        final SSLContext testContext = new SSLContext(null, null, null){};
        final AtomicReference<SSLContext> result = new AtomicReference<>();
        final AtomicReference<RW> config = new AtomicReference<>();
        class RWSsl extends RW {

            @Override
            public SSLContext sslContext(Client client, ClientRequest request) {
                result.set(super.sslContext(client, request));
                return result.get();
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
    public void testConnectionController() {
        final NettyConnectionController connectionController = new NettyConnectionController();
        final AtomicReference<NettyConnectionController> result = new AtomicReference<>();

        class RWController extends RW {

            @Override
            NettyConnectionController connectionController() {
                result.set(super.connectionController());
                return result.get();
            }

            @Override
            public RWController instance() {
                return new RWController();
            }
        }

        new TestClient(new RWController().connectionController(connectionController)).request().apply();
        Assertions.assertEquals(connectionController, result.get());

        result.set(null);
        new TestClient(new RWController().prefix(PREFIX).connectionController(new NettyConnectionController()))
                .request(r -> r.setProperty(ClientProperties.CONNECTOR_CONFIGURATION,
                        new RWController().connectionController(connectionController).prefix(PREFIX))).apply();
        Assertions.assertEquals(connectionController, result.get());
    }

    @Test
    public void testEnableSslHostnameVerification() {
        final AtomicReference<Boolean> result = new AtomicReference<>();
        final AtomicReference<NettyConnectorProvider.Config.RW> config = new AtomicReference<>();
        class RWEnabled extends RW {

            @Override
            boolean isSslHostnameVerificationEnabled(Map<String, Object> properties) {
               result.set(super.isSslHostnameVerificationEnabled(properties));
               return result.get();
            }

            @Override
            public RWEnabled instance() {
                RWEnabled enabled = new RWEnabled();
                config.set(enabled);
                return enabled;
            }
        }

        Request req;
        req = new TestClient(new RWEnabled()).request().apply();
        config.get().isSslHostnameVerificationEnabled(req.request.getConfiguration().getProperties());
        Assertions.assertEquals(new RWEnabled().followRedirects(), result.get());

        result.set(null);
        req = new TestClient(new RWEnabled().enableSslHostnameVerification(false)).request().apply();
        config.get().isSslHostnameVerificationEnabled(req.request.getConfiguration().getProperties());
        Assertions.assertEquals(false, result.get());

//        NOT PER REQUEST
//        result.set(null);
//        req = new TestClient(new RWEnabled().enableSslHostnameVerification(false).prefix(PREFIX))
//                .request(r -> r.setProperty(PREFIX + NettyClientProperties.ENABLE_SSL_HOSTNAME_VERIFICATION, true))
//                .apply();
//        config.get().isSslHostnameVerificationEnabled(req.request.getConfiguration().getProperties());
//        Assertions.assertEquals(true, result.get());

        result.set(null);
        req = new TestClient(new RWEnabled().enableSslHostnameVerification(false).prefix(PREFIX))
                .request(r -> r.setProperty(
                        PREFIX + ClientProperties.CONNECTOR_CONFIGURATION, new RWEnabled().enableSslHostnameVerification(true)))
                .apply();
        config.get().isSslHostnameVerificationEnabled(req.request.getConfiguration().getProperties());
        Assertions.assertEquals(true, result.get());

        result.set(null);
        req = new TestClient(new RWEnabled())
                .client(c -> c.property(ClientProperties.CONNECTOR_CONFIGURATION,
                        new RWEnabled().enableSslHostnameVerification(false)))
                .request(r -> r.setProperty(ClientProperties.CONNECTOR_CONFIGURATION,
                        new RWEnabled().enableSslHostnameVerification(true)))
                .apply();
        config.get().isSslHostnameVerificationEnabled(req.request.getConfiguration().getProperties());
        Assertions.assertEquals(true, result.get());

        result.set(null);
        req = new TestClient(new RWEnabled().prefix(PREFIX))
                .client(c -> c.property(PREFIX + NettyClientProperties.ENABLE_SSL_HOSTNAME_VERIFICATION, false))
                .request(r -> r.setProperty(PREFIX + ClientProperties.CONNECTOR_CONFIGURATION,
                        new RWEnabled().enableSslHostnameVerification(true)))
                .request(r -> r.setEntity(PREFIX)).apply();
        config.get().isSslHostnameVerificationEnabled(req.request.getConfiguration().getProperties());
        Assertions.assertEquals(false, result.get());
    }

    @Test
    public void testMaxRedirects() {
        final AtomicReference<Integer> result = new AtomicReference<>();
        final AtomicReference<NettyConnectorProvider.Config.RW> config = new AtomicReference<>();
        class RWMaxRed extends RW {

            @Override
            int maxRedirects(ClientRequest request) {
                result.set(super.maxRedirects(request));
                return result.get();
            }

            @Override
            public RWMaxRed instance() {
                RWMaxRed max = new RWMaxRed();
                config.set(max);
                return max;
            }
        }

        Request req = new TestClient(new RWMaxRed()).request().apply();
        new JerseyClientHandler(req.request, new CompletableFuture<ClientResponse>(),
                new CompletableFuture<Object>(), new HashSet<>(), req.connector, config.get());
        Assertions.assertEquals(new RWMaxRed().maxRedirects(req.request), result.get());

        result.set(null);
        req = new TestClient(new RWMaxRed().maxRedirects(2)).request().apply();
        new JerseyClientHandler(req.request, new CompletableFuture<ClientResponse>(),
                new CompletableFuture<Object>(), new HashSet<>(), req.connector, config.get());
        Assertions.assertEquals(2, result.get());

        result.set(null);
        req = new TestClient(new RWMaxRed().maxRedirects(2).prefix(PREFIX))
                .request(r -> r.setProperty(PREFIX + NettyClientProperties.MAX_REDIRECTS, 3))
                .request(r -> r.setEntity(PREFIX)).apply();
        new JerseyClientHandler(req.request, new CompletableFuture<ClientResponse>(),
                new CompletableFuture<Object>(), new HashSet<>(), req.connector, config.get());
        Assertions.assertEquals(3, result.get());

        result.set(null);
        req = new TestClient(new RWMaxRed().maxRedirects(2).prefix(PREFIX))
                .request(r -> r.setProperty(
                        PREFIX + ClientProperties.CONNECTOR_CONFIGURATION, new RWMaxRed().maxRedirects(3)))
                .apply();
        new JerseyClientHandler(req.request, new CompletableFuture<ClientResponse>(),
                new CompletableFuture<Object>(), new HashSet<>(), req.connector, config.get());
        Assertions.assertEquals(3, result.get());

        result.set(null);
        req = new TestClient(new RWMaxRed())
                .client(c -> c.property(ClientProperties.CONNECTOR_CONFIGURATION, new RWMaxRed().maxRedirects(2)))
                .request(r -> r.setProperty(ClientProperties.CONNECTOR_CONFIGURATION,
                        new RWMaxRed().maxRedirects(3)))
                .request(r -> r.setEntity(PREFIX)).apply();
        new JerseyClientHandler(req.request, new CompletableFuture<ClientResponse>(),
                new CompletableFuture<Object>(), new HashSet<>(), req.connector, config.get());
        Assertions.assertEquals(3, result.get());

        result.set(null);
        req = new TestClient(new RWMaxRed().prefix(PREFIX))
                .client(c -> c.property(PREFIX + NettyClientProperties.MAX_REDIRECTS, 2))
                .request(r -> r.setProperty(PREFIX + ClientProperties.CONNECTOR_CONFIGURATION,
                        new RWMaxRed().maxRedirects(3)))
                .request(r -> r.setEntity(PREFIX)).apply();
        new JerseyClientHandler(req.request, new CompletableFuture<ClientResponse>(),
                new CompletableFuture<Object>(), new HashSet<>(), req.connector, config.get());
        Assertions.assertEquals(2, result.get());
    }

    @Test
    public void testRedirectController() {
        final NettyHttpRedirectController controller = new NettyHttpRedirectController();
        final AtomicReference<NettyHttpRedirectController> result = new AtomicReference<>();
        final AtomicReference<NettyConnectorProvider.Config.RW> config = new AtomicReference<>();
        class RWController extends RW {

            @Override
            NettyHttpRedirectController redirectController(ClientRequest request) {
                result.set(super.redirectController(request));
                return result.get();
            }

            @Override
            public RWController instance() {
                RWController max = new RWController();
                config.set(max);
                return max;
            }
        }

        Request req;

        result.set(null);
        req = new TestClient(new RWController().redirectController(controller)).request().apply();
        new JerseyClientHandler(req.request, new CompletableFuture<ClientResponse>(),
                new CompletableFuture<Object>(), new HashSet<>(), req.connector, config.get());
        config.get().redirectController(req.request);
        Assertions.assertEquals(controller, result.get());

        result.set(null);
        req = new TestClient(new RWController().redirectController(new NettyHttpRedirectController()).prefix(PREFIX))
                .request(r -> r.setProperty(PREFIX + NettyClientProperties.HTTP_REDIRECT_CONTROLLER, controller))
                .request(r -> r.setEntity(PREFIX)).apply();
        new JerseyClientHandler(req.request, new CompletableFuture<ClientResponse>(),
                new CompletableFuture<Object>(), new HashSet<>(), req.connector, config.get());
        config.get().redirectController(req.request);
        Assertions.assertEquals(controller, result.get());

        result.set(null);
        req = new TestClient(new RWController().redirectController(new NettyHttpRedirectController()).prefix(PREFIX))
                .request(r -> r.setProperty(
                        PREFIX + ClientProperties.CONNECTOR_CONFIGURATION, new RWController().redirectController(controller)))
                .apply();
        new JerseyClientHandler(req.request, new CompletableFuture<ClientResponse>(),
                new CompletableFuture<Object>(), new HashSet<>(), req.connector, config.get());
        config.get().redirectController(req.request);
        Assertions.assertEquals(controller, result.get());

        result.set(null);
        req = new TestClient(new RWController())
                .client(c -> c.property(ClientProperties.CONNECTOR_CONFIGURATION,
                        new RWController().redirectController(new NettyHttpRedirectController())))
                .request(r -> r.setProperty(ClientProperties.CONNECTOR_CONFIGURATION,
                        new RWController().redirectController(controller)))
                .request(r -> r.setEntity(PREFIX)).apply();
        new JerseyClientHandler(req.request, new CompletableFuture<ClientResponse>(),
                new CompletableFuture<Object>(), new HashSet<>(), req.connector, config.get());
//        config.get().redirectController(req.request);
        Assertions.assertEquals(controller, result.get());

        result.set(null);
        req = new TestClient(new RWController().prefix(PREFIX))
                .client(c -> c.property(PREFIX + NettyClientProperties.HTTP_REDIRECT_CONTROLLER, controller))
                .request(r -> r.setProperty(PREFIX + ClientProperties.CONNECTOR_CONFIGURATION,
                        new RWController().redirectController(new NettyHttpRedirectController())))
                .request(r -> r.setEntity(PREFIX)).apply();
        new JerseyClientHandler(req.request, new CompletableFuture<ClientResponse>(),
                new CompletableFuture<Object>(), new HashSet<>(), req.connector, config.get());
//        config.get().redirectController(req.request);
        Assertions.assertEquals(controller, result.get());
    }

    @Test
    public void testPreserveMethodOnRedirect() {
        final AtomicReference<Boolean> result = new AtomicReference<>();
        final AtomicReference<NettyHttpRedirectController> controller = new AtomicReference<>();
        final AtomicReference<NettyConnectorProvider.Config.RW> config = new AtomicReference<>();
        class RWController extends RW {

            @Override
            boolean preserveMethodOnRedirect(ClientRequest request) {
                result.set(super.preserveMethodOnRedirect(request));
                return result.get();
            }

            @Override
            NettyHttpRedirectController redirectController(ClientRequest request) {
                controller.set(super.redirectController(request));
                return controller.get();
            }

            @Override
            public RWController instance() {
                RWController max = new RWController();
                config.set(max);
                return max;
            }
        }

        Request req;
        req = new TestClient(new RWController()).request().apply();
        new JerseyClientHandler(req.request, new CompletableFuture<ClientResponse>(),
                new CompletableFuture<Object>(), new HashSet<>(), req.connector, config.get());
        try {
            controller.get().prepareRedirect(req.request, null);
        } catch (NullPointerException expected) {
        }
        Assertions.assertEquals(new RWController().preserveMethodOnRedirect(req.request), result.get());

        result.set(null);
        req = new TestClient(new RWController().preserveMethodOnRedirect(false)).request().apply();
        new JerseyClientHandler(req.request, new CompletableFuture<ClientResponse>(),
                new CompletableFuture<Object>(), new HashSet<>(), req.connector, config.get());
        try {
            controller.get().prepareRedirect(req.request, null);
        } catch (NullPointerException expected) {
        }
        Assertions.assertEquals(false, result.get());

        result.set(null);
        req = new TestClient(new RWController().preserveMethodOnRedirect(true).prefix(PREFIX))
                .request(r -> r.setProperty(PREFIX + NettyClientProperties.PRESERVE_METHOD_ON_REDIRECT, false))
                .request(r -> r.setEntity(PREFIX)).apply();
        new JerseyClientHandler(req.request, new CompletableFuture<ClientResponse>(),
                new CompletableFuture<Object>(), new HashSet<>(), req.connector, config.get());
        try {
            controller.get().prepareRedirect(req.request, null);
        } catch (NullPointerException expected) {
        }
        Assertions.assertEquals(false, result.get());

        result.set(null);
        req = new TestClient(new RWController().preserveMethodOnRedirect(true).prefix(PREFIX))
                .request(r -> r.setProperty(
                        PREFIX + ClientProperties.CONNECTOR_CONFIGURATION, new RWController().preserveMethodOnRedirect(false)))
                .apply();
        new JerseyClientHandler(req.request, new CompletableFuture<ClientResponse>(),
                new CompletableFuture<Object>(), new HashSet<>(), req.connector, config.get());
        try {
            controller.get().prepareRedirect(req.request, null);
        } catch (NullPointerException expected) {
        }
        Assertions.assertEquals(false, result.get());

        result.set(null);
        req = new TestClient(new RWController())
                .client(c -> c.property(ClientProperties.CONNECTOR_CONFIGURATION,
                        new RWController().preserveMethodOnRedirect(true)))
                .request(r -> r.setProperty(ClientProperties.CONNECTOR_CONFIGURATION,
                        new RWController().preserveMethodOnRedirect(false)))
                .request(r -> r.setEntity(PREFIX)).apply();
        new JerseyClientHandler(req.request, new CompletableFuture<ClientResponse>(),
                new CompletableFuture<Object>(), new HashSet<>(), req.connector, config.get());
        try {
            controller.get().prepareRedirect(req.request, null);
        } catch (NullPointerException expected) {
        }
        Assertions.assertEquals(false, result.get());

        result.set(null);
        req = new TestClient(new RWController().prefix(PREFIX))
                .client(c -> c.property(PREFIX + NettyClientProperties.PRESERVE_METHOD_ON_REDIRECT, false))
                .request(r -> r.setProperty(PREFIX + ClientProperties.CONNECTOR_CONFIGURATION,
                        new RWController().preserveMethodOnRedirect(true)))
                .request(r -> r.setEntity(PREFIX)).apply();
        new JerseyClientHandler(req.request, new CompletableFuture<ClientResponse>(),
                new CompletableFuture<Object>(), new HashSet<>(), req.connector, config.get());
        try {
            controller.get().prepareRedirect(req.request, null);
        } catch (NullPointerException expected) {
        }
        Assertions.assertEquals(false, result.get());
    }

    @Test
    public void testFilterHeadersForProxy() {
        final AtomicReference<ClientProxy> proxy = new AtomicReference<>();
        final AtomicReference<NettyConnectorProvider.Config.RW> config = new AtomicReference<>();
        class RWProxy extends RW {
            @Override
            public Optional<ClientProxy> proxy(ClientRequest request, URI requestUri) {
                Optional<ClientProxy> oProxy = super.proxy(request, requestUri);
                proxy.set(oProxy.orElse(null));
                return oProxy;
            }

            @Override
            public RWProxy instance() {
                RWProxy rw = new RWProxy();
                config.set(rw);
                return rw;
            }
        }
        String proxyUri = "http://proxy.org:8080";
        String userName = "USERNAME";
        String password = "PASSWORD";

        Request req;
        req = new TestClient(new RWProxy().proxyUri(proxyUri).proxyUserName(userName).proxyPassword(password)).request().apply();
        config.get().createProxyHandler(proxy.get(), req.request);
        Assertions.assertEquals(((Ref<Boolean>) new RWProxy().filterHeadersForProxy).get(),
                ((Ref<Boolean>) config.get().filterHeadersForProxy).get());

        config.set(null);
        req = new TestClient(new RWProxy().proxyUri(proxyUri).proxyUserName(userName).proxyPassword(password)
                .filterHeadersForProxy(false)).request().apply();
        config.get().createProxyHandler(proxy.get(), req.request);
        Assertions.assertEquals(false, ((Ref<Boolean>) config.get().filterHeadersForProxy).get());

        config.set(null);
        req = new TestClient(new RWProxy().proxyUri(proxyUri).proxyUserName(userName).proxyPassword(password)
                .prefix(PREFIX))
                .request(r -> r.setProperty(PREFIX + NettyClientProperties.FILTER_HEADERS_FOR_PROXY, false))
                .apply();
        config.get().createProxyHandler(proxy.get(), req.request);
        Assertions.assertEquals(false, ((Ref<Boolean>) config.get().filterHeadersForProxy).get());

        config.set(null);
        new TestClient(new RWProxy().proxyUri(proxyUri).proxyUserName(userName).proxyPassword(password)
                .prefix(PREFIX))
                .client(c -> c.property(PREFIX + NettyClientProperties.FILTER_HEADERS_FOR_PROXY, false))
                .request().apply();
        config.get().createProxyHandler(proxy.get(), req.request);
        Assertions.assertEquals(false, ((Ref<Boolean>) config.get().filterHeadersForProxy).get());

        config.set(null);
        new TestClient(new RWProxy().proxyUri(proxyUri).proxyUserName(userName).proxyPassword(password)
                .prefix(PREFIX))
                .client(c -> c.property(ClientProperties.CONNECTOR_CONFIGURATION,
                        NettyConnectorProvider.config().filterHeadersForProxy(false).prefix(PREFIX)))
                .request().apply();
        config.get().createProxyHandler(proxy.get(), req.request);
        Assertions.assertEquals(false, ((Ref<Boolean>) config.get().filterHeadersForProxy).get());

        config.set(null);
        new TestClient(new RWProxy().proxyUri(proxyUri).proxyUserName(userName).proxyPassword(password)
                .prefix(PREFIX))
                .request(r -> r.setProperty(ClientProperties.CONNECTOR_CONFIGURATION,
                        NettyConnectorProvider.config().filterHeadersForProxy(false).prefix(PREFIX)))
                .apply();
        config.get().createProxyHandler(proxy.get(), req.request);
        Assertions.assertEquals(false, ((Ref<Boolean>) config.get().filterHeadersForProxy).get());
    }

    @Test
    public void testFirstHttpHeaderLineLength() {
        final AtomicReference<NettyConnectorProvider.Config.RW> config = new AtomicReference<>();
        final AtomicReference<Integer> result = new AtomicReference<>();
        class RWFHHLL extends RW {

            @Override
            HttpClientCodec createHttpClientCodec(Map<String, Object> properties) {
                HttpClientCodec codec = super.createHttpClientCodec(properties);
                result.set(firstHttpHeaderLineLength.get());
                return codec;
            }

            @Override
            public RWFHHLL instance() {
                RWFHHLL rw = new RWFHHLL();
                config.set(rw);
                return rw;
            }
        }

        Request req;
        req = new TestClient(new RWFHHLL()).request().apply();
        config.get().createHttpClientCodec(req.request.getConfiguration().getProperties());
        Assertions.assertEquals(NettyClientProperties.DEFAULT_INITIAL_LINE_LENGTH, result.get());

        result.set(null);
        req = new TestClient(new RWFHHLL().initialHttpHeaderLineLength(5555)).request().apply();
        config.get().createHttpClientCodec(req.request.getConfiguration().getProperties());
        Assertions.assertEquals(5555, result.get());

        result.set(null);
        req = new TestClient(new RWFHHLL().initialHttpHeaderLineLength(1111).prefix(PREFIX))
                .request(r -> r.setProperty(ClientProperties.CONNECTOR_CONFIGURATION,
                        new RWFHHLL().initialHttpHeaderLineLength(5555).prefix(PREFIX))).apply();
        config.get().createHttpClientCodec(req.request.getConfiguration().getProperties());
        Assertions.assertEquals(5555, result.get());

        result.set(null);
        req = new TestClient(new RWFHHLL().initialHttpHeaderLineLength(1111).prefix(PREFIX))
                .client(c -> c.property(PREFIX + NettyClientProperties.MAX_INITIAL_LINE_LENGTH, 5555))
                .request().apply();
        config.get().createHttpClientCodec(req.request.getConfiguration().getProperties());
        Assertions.assertEquals(5555, result.get());

        result.set(null);
        req = new TestClient(new RWFHHLL().prefix(PREFIX))
                .client(c -> c.property(PREFIX + NettyClientProperties.MAX_INITIAL_LINE_LENGTH, 5555))
                .request(r -> r.setProperty(ClientProperties.CONNECTOR_CONFIGURATION,
                        new RWFHHLL().initialHttpHeaderLineLength(8888).prefix(PREFIX)))
                .apply();
        config.get().createHttpClientCodec(req.request.getConfiguration().getProperties());
        Assertions.assertEquals(5555, result.get());
    }

    @Test
    public void testMaxHeaderSize() {
        final AtomicReference<NettyConnectorProvider.Config.RW> config = new AtomicReference<>();
        final AtomicReference<Integer> result = new AtomicReference<>();
        class RWSize extends RW {

            @Override
            HttpClientCodec createHttpClientCodec(Map<String, Object> properties) {
                HttpClientCodec codec = super.createHttpClientCodec(properties);
                result.set(maxHeaderSize.get());
                return codec;
            }

            @Override
            public RWSize instance() {
                RWSize rw = new RWSize();
                config.set(rw);
                return rw;
            }
        }

        Request req;
        req = new TestClient(new RWSize()).request().apply();
        config.get().createHttpClientCodec(req.request.getConfiguration().getProperties());
        Assertions.assertEquals(NettyClientProperties.DEFAULT_HEADER_SIZE, result.get());

        result.set(null);
        req = new TestClient(new RWSize().maxHeaderSize(5555)).request().apply();
        config.get().createHttpClientCodec(req.request.getConfiguration().getProperties());
        Assertions.assertEquals(5555, result.get());

        result.set(null);
        req = new TestClient(new RWSize().maxHeaderSize(1111).prefix(PREFIX))
                .request(r -> r.setProperty(ClientProperties.CONNECTOR_CONFIGURATION,
                        new RWSize().maxHeaderSize(5555).prefix(PREFIX))).apply();
        config.get().createHttpClientCodec(req.request.getConfiguration().getProperties());
        Assertions.assertEquals(5555, result.get());

        result.set(null);
        req = new TestClient(new RWSize().maxHeaderSize(1111).prefix(PREFIX))
                .client(c -> c.property(PREFIX + NettyClientProperties.MAX_HEADER_SIZE, 5555))
                .request().apply();
        config.get().createHttpClientCodec(req.request.getConfiguration().getProperties());
        Assertions.assertEquals(5555, result.get());

        result.set(null);
        req = new TestClient(new RWSize().prefix(PREFIX))
                .client(c -> c.property(PREFIX + NettyClientProperties.MAX_HEADER_SIZE, 5555))
                .request(r -> r.setProperty(ClientProperties.CONNECTOR_CONFIGURATION,
                        new RWSize().maxHeaderSize(8888).prefix(PREFIX)))
                .apply();
        config.get().createHttpClientCodec(req.request.getConfiguration().getProperties());
        Assertions.assertEquals(5555, result.get());
    }

    @Test
    public void testMaxChunkSize() {
        final AtomicReference<NettyConnectorProvider.Config.RW> config = new AtomicReference<>();
        final AtomicReference<Integer> result = new AtomicReference<>();
        class RWSize extends RW {

            @Override
            HttpClientCodec createHttpClientCodec(Map<String, Object> properties) {
                HttpClientCodec codec = super.createHttpClientCodec(properties);
                result.set(maxChunkSize.get());
                return codec;
            }

            @Override
            public RWSize instance() {
                RWSize rw = new RWSize();
                config.set(rw);
                return rw;
            }
        }

        Request req;
        req = new TestClient(new RWSize()).request().apply();
        config.get().createHttpClientCodec(req.request.getConfiguration().getProperties());
        Assertions.assertEquals(NettyClientProperties.DEFAULT_CHUNK_SIZE, result.get());

        result.set(null);
        req = new TestClient(new RWSize().maxChunkSize(5555)).request().apply();
        config.get().createHttpClientCodec(req.request.getConfiguration().getProperties());
        Assertions.assertEquals(5555, result.get());

        result.set(null);
        req = new TestClient(new RWSize().maxChunkSize(1111).prefix(PREFIX))
                .request(r -> r.setProperty(ClientProperties.CONNECTOR_CONFIGURATION,
                        new RWSize().maxChunkSize(5555).prefix(PREFIX))).apply();
        config.get().createHttpClientCodec(req.request.getConfiguration().getProperties());
        Assertions.assertEquals(5555, result.get());

        result.set(null);
        req = new TestClient(new RWSize().maxChunkSize(1111).prefix(PREFIX))
                .client(c -> c.property(PREFIX + NettyClientProperties.MAX_CHUNK_SIZE, 5555))
                .request().apply();
        config.get().createHttpClientCodec(req.request.getConfiguration().getProperties());
        Assertions.assertEquals(5555, result.get());

        result.set(null);
        req = new TestClient(new RWSize().prefix(PREFIX))
                .client(c -> c.property(PREFIX + NettyClientProperties.MAX_CHUNK_SIZE, 5555))
                .request(r -> r.setProperty(ClientProperties.CONNECTOR_CONFIGURATION,
                        new RWSize().maxChunkSize(8888).prefix(PREFIX)))
                .apply();
        config.get().createHttpClientCodec(req.request.getConfiguration().getProperties());
        Assertions.assertEquals(5555, result.get());
    }

    @Test
    public void testMaxTotalConnections() {
        final AtomicReference<Integer> result = new AtomicReference<>();
        class RWConn extends RW {

            @Override
            NettyConnectorProvider.Config.RW fromClient(Client client) {
                NettyConnectorProvider.Config.RW rw = super.fromClient(client);
                result.set(rw.maxPoolSizeTotal.get());
                return rw;
            }

            @Override
            public RWConn instance() {
                return new RWConn();
            }
        }

        new TestClient(new RWConn().maxTotalConnections(55)).request().apply();
        Assertions.assertEquals(55, result.get());

        result.set(null);
        new TestClient(new RWConn().prefix(PREFIX))
                .client(c -> c.property(PREFIX + NettyClientProperties.MAX_CONNECTIONS_TOTAL, 55))
                .request().apply();
        Assertions.assertEquals(55, result.get());

        result.set(null);
        new TestClient(new RWConn().prefix(PREFIX))
                .client(c -> c.property(ClientProperties.CONNECTOR_CONFIGURATION,
                        NettyConnectorProvider.config().prefix(PREFIX).maxTotalConnections(55)))
                .request().apply();
        Assertions.assertEquals(55, result.get());

        result.set(null);
        new TestClient(new RWConn().prefix(PREFIX))
                // NOT PER REQUEST
                .request(r -> r.setProperty(ClientProperties.CONNECTOR_CONFIGURATION,
                        NettyConnectorProvider.config().prefix(PREFIX).maxTotalConnections(55)))
                .apply();
        Assertions.assertEquals(new RWConn().maxPoolSizeTotal.get(), result.get());
    }

    @Test
    public void testMaxConnectionsPerDestination() {
        final AtomicReference<Integer> result = new AtomicReference<>();
        class RWConn extends RW {

            @Override
            NettyConnectorProvider.Config.RW fromClient(Client client) {
                NettyConnectorProvider.Config.RW rw = super.fromClient(client);
                result.set(rw.maxPoolSize.get());
                return rw;
            }

            @Override
            public RWConn instance() {
                return new RWConn();
            }
        }

        new TestClient(new RWConn().maxConnectionsPerDestination(15)).request().apply();
        Assertions.assertEquals(15, result.get());

        result.set(null);
        new TestClient(new RWConn().prefix(PREFIX))
                .client(c -> c.property(PREFIX + NettyClientProperties.MAX_CONNECTIONS, 15))
                .request().apply();
        Assertions.assertEquals(15, result.get());

        result.set(null);
        new TestClient(new RWConn().prefix(PREFIX))
                .client(c -> c.property(ClientProperties.CONNECTOR_CONFIGURATION,
                        NettyConnectorProvider.config().prefix(PREFIX).maxConnectionsPerDestination(15)))
                .request().apply();
        Assertions.assertEquals(15, result.get());

        result.set(null);
        new TestClient(new RWConn().prefix(PREFIX))
                // NOT PER REQUEST
                .request(r -> r.setProperty(ClientProperties.CONNECTOR_CONFIGURATION,
                        NettyConnectorProvider.config().prefix(PREFIX).maxConnectionsPerDestination(15)))
                .apply();
        Assertions.assertEquals(new RWConn().maxPoolSize.get(), result.get());
    }

    @Test
    public void testIdleConnectionPruneTimeout() {
        final AtomicReference<Integer> result = new AtomicReference<>();
        class RWConn extends RW {

            @Override
            NettyConnectorProvider.Config.RW fromClient(Client client) {
                NettyConnectorProvider.Config.RW rw = super.fromClient(client);
                result.set(rw.maxPoolIdle.get());
                return rw;
            }

            @Override
            public RWConn instance() {
                return new RWConn();
            }
        }

        new TestClient(new RWConn().idleConnectionPruneTimeout(15)).request().apply();
        Assertions.assertEquals(15, result.get());

        result.set(null);
        new TestClient(new RWConn().prefix(PREFIX))
                .client(c -> c.property(PREFIX + NettyClientProperties.IDLE_CONNECTION_PRUNE_TIMEOUT, 15))
                .request().apply();
        Assertions.assertEquals(15, result.get());

        result.set(null);
        new TestClient(new RWConn().prefix(PREFIX))
                .client(c -> c.property(ClientProperties.CONNECTOR_CONFIGURATION,
                        NettyConnectorProvider.config().prefix(PREFIX).idleConnectionPruneTimeout(15)))
                .request().apply();
        Assertions.assertEquals(15, result.get());

        result.set(null);
        new TestClient(new RWConn().prefix(PREFIX))
                // NOT PER REQUEST
                .request(r -> r.setProperty(ClientProperties.CONNECTOR_CONFIGURATION,
                        NettyConnectorProvider.config().prefix(PREFIX).idleConnectionPruneTimeout(15)))
                .apply();
        Assertions.assertEquals(new RWConn().maxPoolIdle.get(), result.get());
    }

    @Test
    public void testPrecedence() {
        NettyConnectorProvider.Config.RW builderLower = NettyConnectorProvider.config().rw();
        builderLower.maxTotalConnections(55);

        NettyConnectorProvider.Config.RW builderUpper = builderLower.copy();
        builderUpper.maxTotalConnections(56);
        Assertions.assertEquals(56, builderUpper.maxPoolSizeTotal.get());

        Client client = ClientBuilder.newClient();
        client.property(NettyClientProperties.MAX_CONNECTIONS_TOTAL, 57);
        NettyConnectorProvider.Config.RW result = builderUpper.fromClient(client);
        Assertions.assertEquals(57, result.maxPoolSizeTotal.get());
        Assertions.assertEquals(60, result.maxPoolIdle.get());
    }

    private static class ConnectorConfig extends ConnectorConfiguration<ConnectorConfig> {
        private static class RW extends ConnectorConfiguration<RW> implements Read<RW> {
            @Override
            public RW instance() {
                return new RW();
            }

            @Override
            public RW self() {
                return this;
            }

            public Configuration config() {
                Map<String, Object> empty = new HashMap<>();
                Configuration configuration = (Configuration) Proxy.newProxyInstance(
                        getClass().getClassLoader(),
                        new Class[]{Configuration.class}, new InvocationHandler() {
                            @Override
                            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                                switch (method.getName()) {
                                    case "getProperties":
                                        return empty;
                                    case "getProperty":
                                        return empty.get(args[0]);
                                }
                                return null;
                            }
                        });

                return configuration;
            }
        }
    }

    private static class RW extends NettyConnectorProvider.Config.RW {
        @Override
        public int connectTimeout() {
            throw new IllegalStateException();
        }
    }


    private static class TestClient {
        private final NettyConnectorProvider.Config.RW rw;
        private final Client client;

        private TestClient(NettyConnectorProvider.Config.RW rw) {
            this.rw = rw;
            this.client = ClientBuilder.newClient();
        }

        public TestClient client(Consumer<Client> consumer) {
            consumer.accept(client);
            return this;
        }

        public TestClient rw(Consumer<NettyConnectorProvider.Config.RW> consumer) {
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
        final NettyConnectorProvider.Config.RW rw;
        NettyConnector connector;

        Request(Client client, NettyConnectorProvider.Config.RW rw) {
            this.client = client;
            this.rw = rw;
            request = createRequest(client);
        }

        public Request request(Consumer<ClientRequest> consumer) {
            consumer.accept(request);
            return this;
        }

        public Request apply() {
            try {
                connector = new NettyConnector(client, rw);
                connector.apply(request);
            } catch (ProcessingException expected) {
            }
            return this;
        }
    }
}
