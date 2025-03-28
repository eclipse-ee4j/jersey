/*
 * Copyright (c) 2025 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.e2e.client;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import java.util.concurrent.Executors;


/**
 * Server for the file upload test that redirects from /submit to /upload.
 */
class RedirectFileUploadServerTest {
    private static final String UPLOAD_DIRECTORY = "target/uploads";
    private static final String BOUNDARY_PREFIX = "boundary=";
    private static final Path uploadDir = Paths.get(UPLOAD_DIRECTORY);

    private static HttpServer server;


    static void start(int port) throws IOException {
        // Create upload directory if it doesn't exist
        if (!Files.exists(uploadDir)) {
            Files.createDirectory(uploadDir);
        }

        // Create HTTP server
        server = HttpServer.create(new InetSocketAddress(port), 0);

        // Create contexts for different endpoints
        server.createContext("/submit", new SubmitHandler());
        server.createContext("/upload", new UploadHandler());

        // Set executor and start server
        server.setExecutor(Executors.newFixedThreadPool(10));
        server.start();
        System.out.println("Server running on port " + port);
    }

    public static void stop() {
        server.stop(0);
    }


    // Handler for /submit endpoint that redirects to /upload
    static class SubmitHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                if (!"POST".equals(exchange.getRequestMethod())) {
                    sendResponse(exchange, 405, "Method Not Allowed. Only POST is supported.");
                    return;
                }

                final BufferedReader reader
                        = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8));
                while (reader.readLine() != null) {
                    //discard payload - required for JDK 1.8
                }
                reader.close();

                // Send a 307 Temporary Redirect to /upload
                // This preserves the POST method and body in the redirect
                exchange.getResponseHeaders().add("Location", "/upload");
                exchange.sendResponseHeaders(307, -1);
            } finally {
                exchange.close();
            }
        }
    }

    // Handler for /upload endpoint that processes file uploads
    static class UploadHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                if (!"POST".equals(exchange.getRequestMethod())) {
                    sendResponse(exchange, 405, "Method Not Allowed. Only POST is supported.");
                    return;
                }

                // Check if the request contains multipart form data
                String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
                if (contentType == null || !contentType.startsWith("multipart/form-data")) {
                    sendResponse(exchange, 400, "Bad Request. Content type must be multipart/form-data.");
                    return;
                }

                // Extract boundary from content type
                String boundary = extractBoundary(contentType);
                if (boundary == null) {
                    sendResponse(exchange, 400, "Bad Request. Could not determine boundary.");
                    return;
                }

                // Process the multipart request and save the file
                String fileName = processMultipartRequest(exchange, boundary);

                if (fileName != null) {
                    sendResponse(exchange, 200, "File uploaded successfully: " + fileName);
                } else {
                    sendResponse(exchange, 400, "Bad Request. No file found in request.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                sendResponse(exchange, 500, "Internal Server Error: " + e.getMessage());
            } finally {
                exchange.close();
                Files.deleteIfExists(uploadDir);
            }
        }

        private String extractBoundary(String contentType) {
            int boundaryIndex = contentType.indexOf(BOUNDARY_PREFIX);
            if (boundaryIndex != -1) {
                return "--" + contentType.substring(boundaryIndex + BOUNDARY_PREFIX.length());
            }
            return null;
        }

        private String processMultipartRequest(HttpExchange exchange, String boundary) throws IOException {
            InputStream requestBody = exchange.getRequestBody();
            BufferedReader reader = new BufferedReader(new InputStreamReader(requestBody, StandardCharsets.UTF_8));

            String line;
            String fileName = null;
            Path tempFile = null;
            boolean isFileContent = false;

            // Generate a random filename for the temporary file
            String tempFileName = UUID.randomUUID().toString();
            tempFile = Files.createTempFile(tempFileName, ".tmp");

            try (OutputStream fileOut = Files.newOutputStream(tempFile)) {
                while ((line = reader.readLine()) != null) {
                    // Check for the boundary
                    if (line.startsWith(boundary)) {
                        if (isFileContent) {
                            // We've reached the end of the file content
                            break;
                        }

                        // Read the next line (Content-Disposition)
                        line = reader.readLine();
                        if (line != null && line.startsWith("Content-Type")) {
                            line = reader.readLine();
                        }
                        if (line != null && line.contains("filename=")) {
                            // Extract filename
                            int filenameStart = line.indexOf("filename=\"") + 10;
                            int filenameEnd = line.indexOf("\"", filenameStart);
                            fileName = line.substring(filenameStart, filenameEnd);

                            // Skip Content-Type line and empty line
                            reader.readLine(); // Content-Type
//                            System.out.println(reader.readLine()); // Empty line
                            isFileContent = true;
                        }
                    } else if (isFileContent) {
                        // If we're reading file content and this line is not a boundary,
                        // write it to the file (append a newline unless it's the first line)
                        fileOut.write(line.getBytes(StandardCharsets.UTF_8));
                        fileOut.write('\n');
                    }
                }
            }

            // If we found a file, move it from the temp location to the uploads directory
            if (fileName != null && !fileName.isEmpty()) {
                Path targetPath = Paths.get(UPLOAD_DIRECTORY, fileName);
                Files.move(tempFile, targetPath, StandardCopyOption.REPLACE_EXISTING);
                return fileName;
            } else {
                // If no file was found, delete the temp file
                Files.deleteIfExists(tempFile);
                return null;
            }
        }
    }

    // Helper method to send HTTP responses
    private static void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, response.length());
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes(StandardCharsets.UTF_8));
        }
    }
}