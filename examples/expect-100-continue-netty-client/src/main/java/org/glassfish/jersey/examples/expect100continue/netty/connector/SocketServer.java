/*
 * Copyright (c) 2025 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.expect100continue.netty.connector;

import javax.net.ServerSocketFactory;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class SocketServer {
    private static final String NO_CONTENT_HEADER = "HTTP/1.1 204 No Content";
    private static final String OK_HEADER = "HTTP/1.1 200 OK";
    private static final String EXPECT_HEADER = "HTTP/1.1 100 Continue";
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private AtomicBoolean expect_processed = new AtomicBoolean(false);

    private ServerSocket server;

    private static final boolean debug = true;

    private static final int port = 3000;

    private volatile boolean stopped = false;

    public static void main(String args[]) throws IOException {
        new SocketServer(port).runServer();
    }

    SocketServer(int port) throws IOException {
        final ServerSocketFactory socketFactory = ServerSocketFactory.getDefault();
        server = socketFactory.createServerSocket(port);
    }

    void stop() {
        stopped = true;
        try {
            server.close();
            executorService.shutdown();
            while (!executorService.isTerminated()) {
                executorService.awaitTermination(100, TimeUnit.MILLISECONDS);
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    void runServer() {

        executorService.execute(() -> {
            try {
                dumpServerReadMe();
                while (!stopped) {
                    final Socket socket = server.accept();
                    executorService.submit(() -> processRequest(socket));
                }
            } catch (IOException e) {
                if (!stopped) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void processRequest(final Socket request) {

        try (final BufferedReader reader = new BufferedReader(new InputStreamReader(request.getInputStream()));
             final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(request.getOutputStream()))) {


            while (!stopped) {
                final Map<String, String> headers = mapHeaders(reader);

                if (headers.isEmpty()) {
                    continue;
                }
                if (debug) {
                    dumpHeaders(headers);
                }

                boolean failed = processExpect100Continue(headers, writer);

                if (failed) {
                    continue;
                }

                final String http_header = expect_processed.get() ? NO_CONTENT_HEADER : OK_HEADER;
                boolean read = readBody(reader, headers);

                final StringBuffer responseBuffer = new StringBuffer(http_header);
                addNewLineToResponse(responseBuffer);
                addServerHeaderToResponse(responseBuffer);
//                    addToResponse("Content-Length: 0", responseBuffer);
                addNewLineToResponse(responseBuffer);
                addNewLineToResponse(responseBuffer);
                if (debug) {
                    dumpResponse(responseBuffer);
                }

                writer.write(responseBuffer.toString());

                writer.flush();
                if (read) {
                    break;
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                request.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void addNewLineToResponse(StringBuffer responseBuffer) {
        addToResponse("\r\n", responseBuffer);
    }

    private void addToResponse(String toBeAdded, StringBuffer responseBuffer) {
        responseBuffer.append(toBeAdded);
    }

    private void addServerHeaderToResponse(StringBuffer responseBuffer) {
        addToResponse("Server: Example Socket Server v.0.0.1", responseBuffer);
        addNewLineToResponse(responseBuffer);
    }

    private boolean processExpect100Continue(Map<String, String> headers, BufferedWriter writer) throws IOException {
        String http_header = EXPECT_HEADER;
        boolean failed = false;
        final String continueHeader = headers.remove("expect");

        if (continueHeader != null && continueHeader.contains("100-continue")) {

            expect_processed.set(http_header.equals(EXPECT_HEADER));


            final StringBuffer responseBuffer = new StringBuffer(http_header);

            addNewLineToResponse(responseBuffer);
            addToResponse("Connection: keep-alive", responseBuffer);
            addNewLineToResponse(responseBuffer);
            addNewLineToResponse(responseBuffer);
            if (debug) {
                dumpResponse(responseBuffer);
            }

            writer.write(responseBuffer.toString());
            writer.flush();
        }
        return failed;
    }

    private Map<String, String> mapHeaders(BufferedReader reader) throws IOException {
        String line;
        final Map<String, String> headers = new HashMap<>();


        if (!reader.ready()) {
            return headers;
        }

        while ((line = reader.readLine()) != null && !line.isEmpty()) {


            int pos = line.indexOf(':');
            if (pos > -1) {
                headers.put(
                        line.substring(0, pos).toLowerCase(Locale.ROOT),
                        line.substring(pos + 2).toLowerCase(Locale.ROOT).trim());
            }
        }

        return headers;
    }

    private boolean readBody(BufferedReader reader, Map<String, String> headers) throws IOException {
        if (headers.containsKey("content-length")) {
            int contentLength = Integer.valueOf(headers.get("content-length"));
            int actualLength = 0, readingByte = 0;
            int[] buffer = new int[contentLength];
            while (actualLength < contentLength && (readingByte = reader.read()) != -1) {
                buffer[actualLength++] = readingByte;
            }
            if (debug) {
                System.out.println("Reading " + actualLength + " of " + contentLength + " bytes/chars");
            }
            return (actualLength == contentLength);
        } else if (headers.containsKey("transfer-encoding")) {
            String line;
            while ((line = reader.readLine()) != null && !line.equals("0")) {
                if (debug) {
                    System.out.println(line);
                }
            }
            return true;
        }
        return false;
    }

    private void dumpHeaders(Map<String, String> headers) {
        System.out.println("==== DUMPING HEADERS ====");
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            System.out.println(entry.getKey() + ", " + entry.getValue());
        }
        System.out.println("==== HEADERS DUMPED =====");
    }

    private void dumpResponse(StringBuffer responseBuffer) {
        System.out.println("==== DUMPING RESPONSE ====");
        System.out.println(responseBuffer);
        System.out.println("==== RESPONSE DUMPED =====");
    }

    private void dumpServerReadMe() {
        System.out.println("==================================Server is running========================================");
        System.out.println("=                                       ***                                               =");
        System.out.println("= ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ =");
        System.out.println("=  You can send requests to it either using Netty Client or curl or any other http tool.  =");
        System.out.println("=  Try to modify it to see how Expect: 100-continue header works.                         =");
        System.out.println("= ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ =");
        System.out.println("=                               stop server by Ctrl-c                                     =");
        System.out.println("= ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ =");
        System.out.println("=  Run server using maven:                                                                =");
        System.out.println("=          mvn clean package exec:java                                                    =");
        System.out.println("=  Run client using maven:                                                                =");
        System.out.println("=          mvn clean package exec:java -Pclient                                           =");
        System.out.println("= ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ =");
        System.out.println("=                                       ***                                               =");
        System.out.println("===========================================================================================");
    }

}
