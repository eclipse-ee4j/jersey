/*
 * Copyright (c) 2013, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.test.grizzly.web.ssl;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import java.io.InputStream;
import java.security.KeyStore;

/**
 * Utility to easily create a SSLContext for a server or a client
 *
 * The utility class uses the keystore and truststore from the
 * test resources to generate the KeyManagers, TrustManagers
 * and the SSLContext
 *
 * @author Hakan Altindag
 */
public final class SslUtils {

    private static final String SERVER_IDENTITY_PATH = "server-identity.jks";
    private static final char[] SERVER_IDENTITY_PASSWORD = "secret".toCharArray();
    private static final String SERVER_TRUSTSTORE_PATH = "server-truststore.jks";
    private static final char[] SERVER_TRUSTSTORE_PASSWORD = "secret".toCharArray();

    private static final String CLIENT_IDENTITY_PATH = "client-identity.jks";
    private static final char[] CLIENT_IDENTITY_PASSWORD = "secret".toCharArray();
    private static final String CLIENT_TRUSTSTORE_PATH = "client-truststore.jks";
    private static final char[] CLIENT_TRUSTSTORE_PASSWORD = "secret".toCharArray();

    private static final String KEYSTORE_TYPE = "PKCS12";

    private SslUtils() {}

    protected static SSLContext createServerSslContext(boolean includeKeyMaterial, boolean includeTrustMaterial) {
        return createSslContext(
                includeKeyMaterial,
                includeTrustMaterial,
                SERVER_IDENTITY_PATH,
                SERVER_IDENTITY_PASSWORD,
                SERVER_TRUSTSTORE_PATH,
                SERVER_TRUSTSTORE_PASSWORD
        );
    }

    protected static SSLContext createClientSslContext(boolean includeKeyMaterial, boolean includeTrustMaterial) {
        return createSslContext(
                includeKeyMaterial,
                includeTrustMaterial,
                CLIENT_IDENTITY_PATH,
                CLIENT_IDENTITY_PASSWORD,
                CLIENT_TRUSTSTORE_PATH,
                CLIENT_TRUSTSTORE_PASSWORD
        );
    }

    private static SSLContext createSslContext(
            boolean includeKeyMaterial,
            boolean includeTrustMaterial,
            String keyStorePath,
            char[] keyStorePassword,
            String trustStorePath,
            char[] trustStorePassword) {

        try {
            KeyManager[] keyManagers = null;
            TrustManager[] trustManagers = null;

            if (includeKeyMaterial) {
                keyManagers = createKeyManagers(keyStorePath, keyStorePassword);
            }

            if (includeTrustMaterial) {
                trustManagers = createTrustManagers(trustStorePath, trustStorePassword);
            }

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagers, trustManagers, null);
            return sslContext;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static TrustManager[] createTrustManagers(String keyStorePath, char[] keyStorePassword) throws Exception {
        KeyStore trustStore = getKeyStore(keyStorePath, keyStorePassword);

        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(trustStore);
        return trustManagerFactory.getTrustManagers();
    }

    private static KeyManager[] createKeyManagers(String keyStorePath, char[] keyStorePassword) throws Exception {
        KeyStore keyStore = getKeyStore(keyStorePath, keyStorePassword);

        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, keyStorePassword);
        return keyManagerFactory.getKeyManagers();
    }

    private static KeyStore getKeyStore(String path, char[] keyStorePassword) throws Exception {
        try (InputStream inputStream = getResource(path)) {
            KeyStore keyStore = KeyStore.getInstance(KEYSTORE_TYPE);
            keyStore.load(inputStream, keyStorePassword);
            return keyStore;
        }
    }

    private static InputStream getResource(String path) {
        return SslUtils.class.getClassLoader().getResourceAsStream(path);
    }

}
