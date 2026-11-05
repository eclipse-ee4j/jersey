/*
 * Copyright (c) 2026 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.jetty;

import java.io.IOException;
import java.net.Socket;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.StreamingOutput;

import org.glassfish.jersey.server.ResourceConfig;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * A client that disconnects while its response is written must not leave its connection open on the server.
 */
public class ClientDisconnectTest {

    @Path("stream")
    public static class StreamResource {
        @GET
        public StreamingOutput get() {
            return output -> {
                final byte[] chunk = new byte[64 * 1024];
                for (int i = 0; i < 100_000; i++) {
                    output.write(chunk);
                    output.flush();
                }
            };
        }
    }

    @Test
    public void connectionIsReleasedWhenClientDisconnectsDuringResponse() throws Exception {
        final Server server = JettyHttpContainerFactory.createServer(
                URI.create("http://localhost:0/"), new ResourceConfig(StreamResource.class));
        try {
            final ServerConnector connector = (ServerConnector) server.getConnectors()[0];
            disconnectDuringResponse(connector.getLocalPort());

            final long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
            while (!connector.getConnectedEndPoints().isEmpty() && System.nanoTime() < deadline) {
                Thread.sleep(10);
            }
            assertEquals(0, connector.getConnectedEndPoints().size());
        } finally {
            server.stop();
        }
    }

    private static void disconnectDuringResponse(final int port) throws IOException {
        try (Socket socket = new Socket("localhost", port)) {
            socket.getOutputStream().write(
                    "GET /stream HTTP/1.1\r\nHost: localhost\r\n\r\n".getBytes(StandardCharsets.US_ASCII));
            socket.getOutputStream().flush();
            assertNotEquals(-1, socket.getInputStream().read());
            // Abort the connection, instead of closing it gracefully, so the next write fails immediately
            socket.setSoLinger(true, 0);
        }
    }
}
