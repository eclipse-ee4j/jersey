/*
 * Copyright (c) 2015, 2022 Oracle and/or its affiliates. All rights reserved.
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import javax.net.ssl.SSLContext;

import org.glassfish.jersey.SslConfigurator;
import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.apache5.connector.Apache5ConnectorProvider;
import org.glassfish.jersey.client.HttpUrlConnectorProvider;
import org.glassfish.jersey.client.spi.ConnectorProvider;
import org.glassfish.jersey.grizzly.connector.GrizzlyConnectorProvider;
import org.glassfish.jersey.jetty.connector.JettyConnectorProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * SSL connector hostname verification tests.
 *
 * @author Petr Bouda
 */
@RunWith(Parameterized.class)
public abstract class AbstractConnectorServerTest {

    // Default truststore and keystore
    private static final String CLIENT_TRUST_STORE = "truststore-localhost-client";
    private static final String SERVER_KEY_STORE = "keystore-localhost-server";
    private static final String CLIENT_KEY_STORE = "keystore-client";

    /**
     * Test parameters provider.
     *
     * @return test parameters.
     */
    @Parameterized.Parameters(name = "{index}: {0}")
    public static Iterable<Object[]> testData() {
        return Arrays.asList(new Object[][] {
                {new HttpUrlConnectorProvider()},
                {new GrizzlyConnectorProvider()},
                {new JettyConnectorProvider()},
                {new ApacheConnectorProvider()},
                {new Apache5ConnectorProvider()}
        });
    }

    @Parameterized.Parameter(0)
    public ConnectorProvider connectorProvider;

    private final Object serverGuard = new Object();
    private Server server = null;

    @Before
    public void setUp() throws Exception {
        synchronized (serverGuard) {
            if (server != null) {
                throw new IllegalStateException(
                        "Test run sync issue: Another instance of the SSL-secured HTTP test server has been already started.");
            }
            server = Server.start(serverKeyStore());
        }
    }

    @After
    public void tearDown() throws Exception {
        synchronized (serverGuard) {
            if (server == null) {
                throw new IllegalStateException("Test run sync issue: There is no SSL-secured HTTP test server to stop.");
            }
            server.stop();
            server = null;
        }
    }

    protected SSLContext getSslContext() throws IOException {
        try (InputStream trustStore = SslConnectorConfigurationTest.class.getResourceAsStream(clientTrustStore());
                InputStream keyStore = SslConnectorConfigurationTest.class.getResourceAsStream(CLIENT_KEY_STORE);) {
            return SslConfigurator.newInstance()
                    .trustStoreBytes(toBytes(trustStore))
                    .trustStorePassword("asdfgh")
                    .keyStoreBytes(toBytes(keyStore))
                    .keyPassword("asdfgh")
                    .createSSLContext();
        }
    }

    public static byte[] toBytes(InputStream is) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[16384];
        while ((nRead = is.read(data, 0, data.length)) != -1) {
          buffer.write(data, 0, nRead);
        }
        return buffer.toByteArray();
    }

    protected String serverKeyStore() {
        return SERVER_KEY_STORE;
    }

    protected String clientTrustStore() {
        return CLIENT_TRUST_STORE;
    }
}
