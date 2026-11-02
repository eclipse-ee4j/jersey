/*
 * Copyright (c) 2026 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */
package org.glassfish.jersey.netty.connector;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Regression test for issue #4794: the Netty client threw a {@code NullPointerException}
 * when a non-empty response had neither a {@code Content-Length} header nor a
 * {@code Transfer-Encoding: chunked} header, i.e. a response delimited by the
 * server closing the connection (see RFC 2616, section 4.4).
 */
public class Issue4794Test {

    private static final String BODY = "Issue 4794 Response Body";

    private ServerSocket serverSocket;
    private Thread serverThread;

    private void startServer() throws IOException {
        serverSocket = new ServerSocket(0);
        serverThread = new Thread(() -> {
            try (Socket socket = serverSocket.accept()) {
                socket.getInputStream().read(new byte[8192]);
                OutputStream out = socket.getOutputStream();
                byte[] response = ("HTTP/1.1 200 OK\r\n"
                        + "Content-Type: text/plain\r\n"
                        + "\r\n"
                        + BODY).getBytes(StandardCharsets.UTF_8);
                out.write(response);
                out.flush();
                // Delimit the response body by closing the connection (no Content-Length, no chunked encoding).
                socket.shutdownOutput();
                // Drain until the client closes its side, so the FIN is delivered after the body.
                while (socket.getInputStream().read() != -1) {
                    // discard
                }
            } catch (IOException e) {
                // server socket closed; nothing to do
            }
        });
        serverThread.setDaemon(true);
        serverThread.start();
    }

    @AfterEach
    public void tearDown() throws IOException {
        if (serverSocket != null && !serverSocket.isClosed()) {
            serverSocket.close();
        }
    }

    @Test
    public void testResponseWithoutContentLengthAndChunkedEncoding() throws Exception {
        startServer();
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.connectorProvider(new NettyConnectorProvider());
        Client client = ClientBuilder.newBuilder().withConfig(clientConfig).build();
        try {
            Response response = client.target("http://localhost:" + serverSocket.getLocalPort() + "/test")
                    .request()
                    .get();
            assertEquals(200, response.getStatus());
            assertEquals(BODY, response.readEntity(String.class));
        } finally {
            client.close();
        }
    }
}
