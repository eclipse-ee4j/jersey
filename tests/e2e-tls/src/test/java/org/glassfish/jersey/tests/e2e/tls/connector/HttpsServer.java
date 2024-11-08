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

package org.glassfish.jersey.tests.e2e.tls.connector;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;

class HttpsServer implements Runnable {
    private final SSLServerSocket sslServerSocket;
    private boolean closed = false;

    public HttpsServer(SSLContext context) throws Exception {
        sslServerSocket = (SSLServerSocket) context.getServerSocketFactory().createServerSocket(0);
    }

    public int getPort() {
        return sslServerSocket.getLocalPort();
    }

    @Override
    public void run() {
        System.out.printf("Server started on port %d%n", getPort());
        while (!closed) {
            SSLSocket s;
            try {
                s = (SSLSocket) sslServerSocket.accept();
            } catch (IOException e2) {
                s = null;
            }
            final SSLSocket socket = s;
            new Thread(new Runnable() {
                public void run() {
                    try {
                        if (socket != null) {
                            InputStream is = new BufferedInputStream(socket.getInputStream());
                            byte[] data = new byte[2048];
                            int len = is.read(data);
                            if (len <= 0) {
                                throw new IOException("no data received");
                            }
                            //System.out.printf("Server received: %s\n", new String(data, 0, len));
                            PrintWriter writer = new PrintWriter(socket.getOutputStream());
                            writer.println("HTTP/1.1 200 OK");
                            writer.println("Content-Type: text/html");
                            writer.println();
                            writer.println("Hello from server!");
                            writer.flush();
                            writer.close();
                            socket.close();
                        }
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
            }).start();
        }
    }

    void stop() {
        try {
            closed = true;
            sslServerSocket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
