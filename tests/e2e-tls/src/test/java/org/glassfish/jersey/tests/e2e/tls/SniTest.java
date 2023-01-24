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

import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.apache5.connector.Apache5ConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.HttpUrlConnectorProvider;
import org.glassfish.jersey.client.spi.ConnectorProvider;
import org.glassfish.jersey.jdk.connector.JdkConnectorProvider;
import org.glassfish.jersey.netty.connector.NettyConnectorProvider;
import org.glassfish.jersey.tests.e2e.tls.explorer.SSLCapabilities;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import javax.net.ssl.SNIServerName;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class SniTest {
    private static final int PORT = 8443;
    private static final String LOCALHOST = "127.0.0.1";


    static {
        // JDK specific settings
        System.setProperty("jdk.net.hosts.file", SniTest.class.getResource("/hosts").getPath());
    }

    public static ConnectorProvider[] getConnectors() {
        return new ConnectorProvider[] {
                new NettyConnectorProvider(),
                new ApacheConnectorProvider(),
                new Apache5ConnectorProvider(),
                new JdkConnectorProvider(),
                new HttpUrlConnectorProvider()
        };
    }

    @ParameterizedTest
    @MethodSource("getConnectors")
    public void server1Test(ConnectorProvider provider) {
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.connectorProvider(provider);
        serverTest(clientConfig, "www.host1.com");
    }

    public void serverTest(ClientConfig clientConfig, String hostName) {
        String newHostName = replaceWhenHostNotKnown(hostName);
        final List<SNIServerName> serverNames = new LinkedList<>();
        final String[] requestHostName = new String[1];
        ClientHelloTestServer server = new ClientHelloTestServer() {
            @Override
            protected void afterHandshake(Socket socket, SSLCapabilities capabilities) {
                serverNames.addAll(capabilities.getServerNames());
            }
        };
        server.init(PORT);
        server.start();

        clientConfig.property(ClientProperties.READ_TIMEOUT, 2000);
        clientConfig.property(ClientProperties.CONNECT_TIMEOUT, 2000);
        try (Response r = ClientBuilder.newClient(clientConfig)
                .register(new ClientRequestFilter() {
                    @Override
                    public void filter(ClientRequestContext requestContext) throws IOException {
                        requestHostName[0] = requestContext.getUri().getHost();
                    }
                })
                .target("https://" + (newHostName.equals(LOCALHOST) ? LOCALHOST : "www.host0.com") + ":" + PORT)
                .path("host")
                .request()
                .header(HttpHeaders.HOST, hostName + ":8080")
                .get()) {
            // empty
        } catch (Exception e) {
            Throwable cause = e;
            while (cause != null
                    && !SocketTimeoutException.class.isInstance(cause)
                    && TimeoutException.class.isInstance(cause)) {
                cause = cause.getCause();
            }
            if (cause == null && /*IOE*/ !e.getMessage().contains("Stream closed")) {
                throw e;
            }
        }

        server.stop();

        if (serverNames.isEmpty()) {
            throw new IllegalStateException("ServerNames are empty");
        }

        String clientSniName = new String(serverNames.get(0).getEncoded());
        if (!hostName.equals(clientSniName)) {
            throw new IllegalStateException("Unexpected client SNI name " + clientSniName);
        }

        if (!LOCALHOST.equals(newHostName) && requestHostName[0].equals(hostName)) {
            throw new IllegalStateException("The HTTP Request is made with the same");
        }

        System.out.append("Found expected Client SNI ").println(serverNames.get(0));
    }

    /*
     * The method checks whether the JDK-dependent property "jdk.net.hosts.file" works.
     * If it does, the request is made with the hostname, so that the 3rd party client has
     * the request with the hostname. If a real address is returned or UnknownHostException
     * is thrown, the property did not work and the request needs to use 127.0.0.1.
     */
    private static String replaceWhenHostNotKnown(String hostName) {
        try {
            InetAddress inetAddress = InetAddress.getByName(hostName);
            return LOCALHOST.equals(inetAddress.getHostAddress()) ? hostName : LOCALHOST;
        } catch (UnknownHostException e) {
            return LOCALHOST;
        }
    }
}
