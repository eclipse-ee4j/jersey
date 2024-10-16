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

package org.glassfish.jersey.tests.e2e.tls;

import org.glassfish.jersey.client.SslContextClientBuilder;
import org.glassfish.jersey.test.JerseyTest;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.core.UriBuilder;
import java.io.InputStream;
import java.net.URI;
import java.security.KeyStore;
import java.util.Optional;
import java.util.function.Supplier;

public class SslParentTest extends JerseyTest {

    protected SSLContext serverSslContext;
    protected SSLParameters serverSslParameters;

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

    @Override
    protected URI getBaseUri() {
        return UriBuilder
                .fromUri("https://localhost")
                .port(getPort())
                .build();
    }

    protected static class SslUtils {

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
