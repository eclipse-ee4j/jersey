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

package org.glassfish.jersey.tests.e2e.client.connector;

import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.apache5.connector.Apache5ConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.HttpUrlConnectorProvider;
import org.glassfish.jersey.client.spi.ConnectorProvider;
import org.glassfish.jersey.grizzly.connector.GrizzlyConnectorProvider;
import org.glassfish.jersey.jdk.connector.JdkConnectorProvider;
import org.glassfish.jersey.jetty.connector.JettyConnectorProvider;
import org.glassfish.jersey.netty.connector.NettyConnectorProvider;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class NoContentLengthTest {

    private static final String MSG = "12345678901234567890123456789012345678901234567890";

    private static int port;
    private static AtomicBoolean running = new AtomicBoolean(false);

    @BeforeEach
    void beforeEach() {
        while (!running.compareAndSet(false, true)) {
            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    String _port = System.getProperty("jersey.config.test.container.port");
                    port = Integer.parseInt(_port == null || _port.isEmpty() ? "8080" : _port);
                    ServerSocket serverSocket = new ServerSocket(port);
                    System.err.println("Starting server on port : " + port);

                    Socket clientSocket = serverSocket.accept();

                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

                    String s;
                    while ((s = in.readLine()) != null) {
                        // System.out.println(s);
                        if (s.isEmpty()) {
                            break;
                        }
                    }

                    out.write("HTTP/1.0 200 OK\r\n");
                    out.write("Content-Type: text/plain\r\n");
                    out.write("\r\n");
                    out.write(MSG);

                    out.close();
                    in.close();
                    clientSocket.close();
                    serverSocket.close();
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                } finally {
                    running.set(false);
                }
            }
        };
        Thread newThread = new Thread(runnable);
        newThread.start();
    }

    public static List<ConnectorProvider> providers() {
        return Arrays.asList(
                new ApacheConnectorProvider(),
                new Apache5ConnectorProvider(),
                new HttpUrlConnectorProvider(),
                new NettyConnectorProvider(),
                new JettyConnectorProvider(),
                new GrizzlyConnectorProvider(),
                new JdkConnectorProvider()
        );
    }

    @ParameterizedTest
    @MethodSource("providers")
    public void testNoContentLength(ConnectorProvider connectorProvider) {
        try (Response r = target(connectorProvider).request().get()) {
            MatcherAssert.assertThat(r.getStatus(), Matchers.is(200));
            MatcherAssert.assertThat(r.getHeaderString(HttpHeaders.CONTENT_LENGTH), Matchers.nullValue());
            MatcherAssert.assertThat(r.hasEntity(), Matchers.is(true));
            MatcherAssert.assertThat(r.readEntity(String.class), Matchers.is(MSG));
        }
    }

    private WebTarget target(ConnectorProvider connectorProvider) {
        ClientConfig config = new ClientConfig();
        config.connectorProvider(connectorProvider);
        return ClientBuilder.newClient(config).target("http://localhost:" + port);
    }
}
