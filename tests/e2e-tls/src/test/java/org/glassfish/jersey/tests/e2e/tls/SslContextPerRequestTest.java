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

package org.glassfish.jersey.tests.e2e.tls;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.HttpUrlConnectorProvider;
import org.glassfish.jersey.client.RequestEntityProcessing;
import org.glassfish.jersey.client.SslContextClientBuilder;
import org.glassfish.jersey.client.spi.ConnectorProvider;
import org.glassfish.jersey.netty.connector.NettyClientProperties;
import org.glassfish.jersey.netty.connector.NettyConnectorProvider;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.grizzly.GrizzlyTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriBuilder;
import java.io.InputStream;
import java.net.URI;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class SslContextPerRequestTest extends JerseyTest {

    private SSLContext serverSslContext;
    private SSLParameters serverSslParameters;
    private static final String MESSAGE = "Message for Netty with SSL";

    @Override
    protected TestContainerFactory getTestContainerFactory() {
        return new GrizzlyTestContainerFactory();
    }

    @Path("secure")
    public static class TestResource {
        @GET
        public String get(@Context HttpHeaders headers) {
            return MESSAGE;
        }
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(TestResource.class);
    }

    @Override
    protected URI getBaseUri() {
        return UriBuilder
                .fromUri("https://localhost")
                .port(getPort())
                .build();
    }

    @Override
    protected Optional<SSLContext> getSslContext() {
        if (serverSslContext == null) {
            serverSslContext = SslUtils.createServerSslContext();
        }

        return Optional.of(serverSslContext);
    }

    @Override
    protected Optional<SSLParameters> getSslParameters() {
        if (serverSslParameters == null) {
            serverSslParameters = new SSLParameters();
            serverSslParameters.setNeedClientAuth(false);
        }

        return Optional.of(serverSslParameters);
    }

    public static Stream<ConnectorProvider> connectorProviders() {
        return Stream.of(
                new HttpUrlConnectorProvider(),
                new NettyConnectorProvider()
        );
    }

    @ParameterizedTest
    @MethodSource("connectorProviders")
    public void sslOnRequestTest(ConnectorProvider connectorProvider) throws NoSuchAlgorithmException {
        Supplier<SSLContext> clientSslContext = SslUtils.createClientSslContext();

        ClientConfig config = new ClientConfig();
        config.connectorProvider(connectorProvider);
        config.property(ClientProperties.REQUEST_ENTITY_PROCESSING, RequestEntityProcessing.BUFFERED);

        Client client = ClientBuilder.newBuilder().withConfig(config).build();

        WebTarget target = client.target(getBaseUri()).path("secure");

        String s;

        s = target.request()
                .property(ClientProperties.SSL_CONTEXT_SUPPLIER, clientSslContext)
                .get(String.class);
        Assertions.assertEquals(MESSAGE, s);

        try {
            Invocation.Builder builder = target.request()
                    .property(ClientProperties.SSL_CONTEXT_SUPPLIER,
                            new SslContextClientBuilder().sslContext(SSLContext.getDefault()));

            if (NettyConnectorProvider.class.isInstance(connectorProvider)) {
                    builder = builder.header(HttpHeaders.HOST, "TestHost"); // New Netty channel without SSL yet
            }
            s = builder.get(String.class);
            Assertions.fail("The SSL Exception has not been thrown");
        } catch (ProcessingException pe) {
            // expected
        }

        s = target.request()
                .property(ClientProperties.SSL_CONTEXT_SUPPLIER, clientSslContext)
                .get(String.class);
        Assertions.assertEquals(MESSAGE, s);
    }

    @ParameterizedTest
    @MethodSource("connectorProviders")
    public void testSslOnClient(ConnectorProvider connectorProvider) {
        Supplier<SSLContext> clientSslContext = SslUtils.createClientSslContext();

        ClientConfig config = new ClientConfig();
        config.connectorProvider(connectorProvider);

        Client client = ClientBuilder.newBuilder().withConfig(config)
                .sslContext(clientSslContext.get())
                .build();

        WebTarget target = client.target(getBaseUri()).path("secure");

        String s = target.request().get(String.class);
        Assertions.assertEquals(MESSAGE, s);
    }

    private static class SslUtils {

        private static final String SERVER_IDENTITY_PATH = "server-identity.jks";
        private static final char[] SERVER_IDENTITY_PASSWORD = "secret".toCharArray();

        private static final String CLIENT_TRUSTSTORE_PATH = "client-truststore.jks";
        private static final char[] CLIENT_TRUSTSTORE_PASSWORD = "secret".toCharArray();

        private static final String KEYSTORE_TYPE = "PKCS12";

        private SslUtils() {}

        public static SSLContext createServerSslContext() {
            return new SslContextClientBuilder()
                    .keyStore(getKeyStore(SERVER_IDENTITY_PATH, SERVER_IDENTITY_PASSWORD), SERVER_IDENTITY_PASSWORD)
                    .get();
        }

        public static Supplier<SSLContext> createClientSslContext() {
            return new SslContextClientBuilder()
                    .trustStore(getKeyStore(CLIENT_TRUSTSTORE_PATH, CLIENT_TRUSTSTORE_PASSWORD));

        }

        private static KeyStore getKeyStore(String path, char[] keyStorePassword) {
            try (InputStream inputStream = getResource(path)) {
                KeyStore keyStore = KeyStore.getInstance(KEYSTORE_TYPE);
                keyStore.load(inputStream, keyStorePassword);
                return keyStore;
            } catch (Exception e) {
                throw new ProcessingException(e);
            }
        }

        private static InputStream getResource(String path) {
            return SslUtils.class.getClassLoader().getResourceAsStream(path);
        }
    }
}
