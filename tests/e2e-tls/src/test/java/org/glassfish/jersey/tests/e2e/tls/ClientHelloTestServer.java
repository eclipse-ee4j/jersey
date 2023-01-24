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

import org.glassfish.jersey.tests.e2e.tls.explorer.SSLCapabilities;
import org.glassfish.jersey.tests.e2e.tls.explorer.SSLExplorer;

import javax.net.ServerSocketFactory;
import javax.net.ssl.SNIHostName;
import javax.net.ssl.SNIMatcher;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class ClientHelloTestServer {
    private ServerSocket serverSocket;
    private Thread serverThread;
    private volatile State state = State.NONE;

    private enum State {
        NONE,
        INIT,
        STARTED,
        STOPPED
    }

    protected ServerSocketFactory getServerSocketFactory() {
        return ServerSocketFactory.getDefault();
    }

    protected void afterHandshake(Socket socket, SSLCapabilities capabilities) {

    }

    public void init(int port) {
        ServerSocketFactory factory = getServerSocketFactory();
        try {
            serverSocket = factory.createServerSocket(port);

            state = State.INIT;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        if (state != State.INIT) {
            System.out.println("Server has not been initialized");
        }
        Thread thread = new Thread(() -> {
            while (state == State.INIT) {
                Socket socket = null;
                try {
                    socket = serverSocket.accept();

                    inspect(socket);
                } catch (SocketException e) {
                    if (!e.getMessage().equals("Interrupted function call: accept failed")) {
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        serverThread = thread;
        thread.start();
    }

    public void stop() {
        try {
            state = State.STOPPED;
            serverSocket.close();
            serverThread.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void inspect(Socket socket) throws IOException {
        InputStream ins = socket.getInputStream();

        byte[] buffer = new byte[0xFF];
        int position = 0;
        SSLCapabilities capabilities = null;

// Read the header of TLS record
        while (position < SSLExplorer.RECORD_HEADER_SIZE) {
            int count = SSLExplorer.RECORD_HEADER_SIZE - position;
            int n = ins.read(buffer, position, count);
            if (n < 0) {
                throw new IOException("unexpected end of stream!");
            }
            position += n;
        }

// Get the required size to explore the SSL capabilities
        int recordLength = SSLExplorer.getRequiredSize(buffer, 0, position);
        if (buffer.length < recordLength) {
            buffer = Arrays.copyOf(buffer, recordLength);
        }

        while (position < recordLength) {
            int count = recordLength - position;
            int n = ins.read(buffer, position, count);
            if (n < 0) {
                throw new IOException("unexpected end of stream!");
            }
            position += n;
        }

// Explore
        capabilities = SSLExplorer.explore(buffer, 0, recordLength);
        if (capabilities != null) {
            System.out.println("Record version: " + capabilities.getRecordVersion());
            System.out.println("Hello version: " + capabilities.getHelloVersion());
        }

        afterHandshake(socket, capabilities);
    }
}
