/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Secondary Licenses as described below and for which a solution has been
 * implemented to allow for such availability set forth in the Eclipse Public
 * License v. 2.0 are satisfied: GNU General Public License, version 2 with the
 * GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package org.glassfish.jersey.netty.connector;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.net.ServerSocketFactory;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.Response;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Tests that {@code NettyConnector} respects {@link ClientProperties#READ_TIMEOUT} against a server
 * that accepts the connection but never sends a response.
 *
 * @see <a href="https://github.com/eclipse-ee4j/jersey/issues/5600">issue #5600</a>
 */
public class ReadTimeoutRespectedTest {

    private static final int READ_TIMEOUT = 2_000;

    /**
     * Margin for thread scheduling and garbage collection on a loaded build machine, added to the
     * configured read timeout when asserting the elapsed time; the assertion still verifies the
     * configured timeout, not merely an eventual termination of the request.
     */
    private static final int SCHEDULING_MARGIN_MILLIS = 2_000;

    private SilentServer server;

    private Client client;

    /**
     * A server that accepts connections but never writes a response.
     */
    private static final class SilentServer implements AutoCloseable {

        private final ExecutorService executorService = Executors.newCachedThreadPool();
        private final AtomicBoolean stopped = new AtomicBoolean(false);
        private final ServerSocket serverSocket;
        private final List<Socket> connections = new CopyOnWriteArrayList<>();

        private SilentServer() throws IOException {
            // Port 0 lets the OS pick a free port, so the test does not fail if a fixed port is taken.
            serverSocket = ServerSocketFactory.getDefault().createServerSocket(0);
        }

        private int getPort() {
            return serverSocket.getLocalPort();
        }

        private void start() {
            executorService.execute(() -> {
                while (!stopped.get()) {
                    try {
                        final Socket socket = serverSocket.accept();
                        connections.add(socket);
                        executorService.submit(() -> {
                            try (Socket accepted = socket) {
                                // Consume the request but never respond; blocks until the client closes the connection.
                                accepted.getInputStream().readAllBytes();
                            } catch (IOException e) {
                                // Client closed the connection; that is the expected exit path.
                            } finally {
                                connections.remove(socket);
                            }
                        });
                    } catch (IOException e) {
                        if (!stopped.get()) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            });
        }

        @Override
        public void close() throws IOException {
            stopped.set(true);
            try {
                serverSocket.close();
                // Unblock the workers that still wait for data from the client.
                for (final Socket socket : connections) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        // Ignore, the socket is likely closed already.
                    }
                }
                executorService.shutdown();
                if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException(e);
            }
        }
    }

    @BeforeEach
    public void startServerAndClient() throws IOException {
        server = new SilentServer();
        server.start();
        final ClientConfig clientConfig = new ClientConfig();
        clientConfig.connectorProvider(new NettyConnectorProvider());
        client = ClientBuilder.newClient(clientConfig);
    }

    @AfterEach
    public void stopServerAndClient() throws IOException {
        if (client != null) {
            client.close();
        }
        if (server != null) {
            server.close();
        }
    }

    private Response get() {
        return client.target(String.format("http://localhost:%d/test", server.getPort()))
                .property(ClientProperties.READ_TIMEOUT, READ_TIMEOUT)
                .request()
                .get();
    }

    @Test
    @Timeout(10)
    public void testReadTimeoutIsRespectedWhenServerDoesNotRespond() {
        final long startTime = System.currentTimeMillis();
        try {
            get();
        } catch (ProcessingException e) {
            final long elapsed = System.currentTimeMillis() - startTime;
            assertThat("ProcessingException must be caused by a read timeout",
                       e.getCause(), instanceOf(java.util.concurrent.TimeoutException.class));
            assertThat("The read timeout was not respected; the request took " + elapsed + " ms",
                       elapsed <= READ_TIMEOUT + SCHEDULING_MARGIN_MILLIS, is(true));
            return;
        }
        throw new AssertionError("Expected a ProcessingException caused by the read timeout");
    }
}
