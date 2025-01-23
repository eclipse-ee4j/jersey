/*
 * Copyright (c) 2020, 2025 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.e2e.client.nettyconnector;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.RequestEntityProcessing;
import org.glassfish.jersey.client.http.Expect100ContinueFeature;
import org.glassfish.jersey.netty.connector.NettyClientProperties;
import org.glassfish.jersey.netty.connector.NettyConnectorProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.net.ServerSocketFactory;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
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
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class Expect100ContinueTest /*extends JerseyTest*/ {

    private static final String RESOURCE_PATH = "expect";

    private static final String RESOURCE_PATH_NOT_SUPPORTED = "fail417";

    private static final String RESOURCE_PATH_UNAUTHORIZED = "fail401";

    private static final String RESOURCE_PATH_PAYLOAD_TOO_LARGE = "fail413";

    private static final String RESOURCE_PATH_METHOD_NOT_SUPPORTED = "fail405";

    private static final String ENTITY_STRING = "1234567890123456789012345678901234567890123456789012"
            + "3456789012345678901234567890";

    private static final Integer portNumber = 9997;

    private static TestSocketServer server;

    private static Client client;

    @BeforeEach
    public void beforeEach() {
        final ClientConfig config = new ClientConfig();
        this.configureClient(config);
        client = ClientBuilder.newClient(config);
    }

    private Client client() {
        return client;
    }

    public WebTarget target(String path) {
        return client().target(String.format("http://localhost:%d", portNumber)).path(path);
    }

    protected void configureClient(ClientConfig config) {
        config.connectorProvider(new NettyConnectorProvider());
    }

    @Test
    public void testExpect100Continue() throws Exception {
        final TestSocketServer server = new TestSocketServer(portNumber);
        try {
            server.runServer();

            final Response response = target(RESOURCE_PATH).request().post(Entity.text(ENTITY_STRING));
            assertEquals(200, response.getStatus(), "Expected 200"); //no Expect header sent - response OK
        } finally {
            server.stop();
        }
    }

    @Test
    public void testExpect100ContinueChunked() throws Exception {
        final TestSocketServer server = new TestSocketServer(portNumber);
        try {
            server.runServer();

            final Response response = target(RESOURCE_PATH).register(Expect100ContinueFeature.basic())
                    .property(ClientProperties.REQUEST_ENTITY_PROCESSING,
                            RequestEntityProcessing.CHUNKED)
                    .request().post(Entity.text(ENTITY_STRING));
            assertEquals(204, response.getStatus(), "Expected 204"); //Expect header sent - No Content response
        } finally {
            server.stop();
        }
    }

    @Test
    public void testExpect100ContinueBuffered() throws Exception {
        final TestSocketServer server = new TestSocketServer(portNumber);
        try {
            server.runServer();

            final Response response = target(RESOURCE_PATH).register(Expect100ContinueFeature.basic())
                    .property(ClientProperties.REQUEST_ENTITY_PROCESSING,
                            RequestEntityProcessing.BUFFERED).request().header(HttpHeaders.CONTENT_LENGTH, 67000L)
                    .post(Entity.text(generateStringByContentLength(67000)));
            assertEquals(204, response.getStatus(), "Expected 204"); //Expect header sent - No Content response
        } finally {
            server.stop();
        }
    }

    @Test
    public void testExpect100ContinueCustomLength() throws Exception {
        final TestSocketServer server = new TestSocketServer(portNumber);
        try {
            server.runServer();

            final Response response = target(RESOURCE_PATH).register(Expect100ContinueFeature.withCustomThreshold(100L))
                    .request().header(HttpHeaders.CONTENT_LENGTH, 200)
                    .post(Entity.text(generateStringByContentLength(200)));
            assertEquals(204, response.getStatus(), "Expected 204"); //Expect header sent - No Content response
        } finally {
            server.stop();
        }
    }

    @Test
    public void testExpect100ContinueCustomLengthWrong() throws Exception {
        final TestSocketServer server = new TestSocketServer(portNumber);
        try {
            server.runServer();

            final Response response = target(RESOURCE_PATH).register(Expect100ContinueFeature.withCustomThreshold(100L))
                    .request().header(HttpHeaders.CONTENT_LENGTH, 99L)
                    .post(Entity.text(generateStringByContentLength(99)));
            assertEquals(200, response.getStatus(), "Expected 200"); //Expect header NOT sent - low request size
        } finally {
            server.stop();
        }
    }

    @Test
    public void testExpect100ContinueCustomLengthProperty() throws Exception {
        final TestSocketServer server = new TestSocketServer(portNumber);
        try {
            server.runServer();

            final Response response = target(RESOURCE_PATH)
                    .property(ClientProperties.EXPECT_100_CONTINUE_THRESHOLD_SIZE, 555L)
                    .property(ClientProperties.EXPECT_100_CONTINUE, Boolean.TRUE)
                    .register(Expect100ContinueFeature.withCustomThreshold(555L))
                    .request().header(HttpHeaders.CONTENT_LENGTH, 666L)
                    .post(Entity.text(generateStringByContentLength(666)));
            assertNotNull(response.getStatus()); //Expect header sent - No Content response
        } finally {
            server.stop();
        }
    }

    @Test
    public void testExpect100ContinueRegisterViaCustomProperty() throws Exception {
        final TestSocketServer server = new TestSocketServer(portNumber);
        try {
            server.runServer();
        final Response response = target(RESOURCE_PATH)
                .property(ClientProperties.EXPECT_100_CONTINUE_THRESHOLD_SIZE, 43L)
                .property(ClientProperties.EXPECT_100_CONTINUE, Boolean.TRUE)
                .request().header(HttpHeaders.CONTENT_LENGTH, 44L)
                .post(Entity.text(generateStringByContentLength(44)));
        assertEquals(204, response.getStatus(), "Expected 204"); //Expect header sent - No Content response
        } finally {
            server.stop();
        }
    }

    @Test
    public void testExpect100ContinueNotSupported() throws Exception {
        final TestSocketServer server = new TestSocketServer(portNumber);
        try {
            server.runServer();

            final Response response = target(RESOURCE_PATH_NOT_SUPPORTED)
                    .property(ClientProperties.EXPECT_100_CONTINUE_THRESHOLD_SIZE, 43L)
                    .property(ClientProperties.EXPECT_100_CONTINUE, Boolean.TRUE)
                    .request().header(HttpHeaders.CONTENT_LENGTH, 44L)
                    .post(Entity.text(generateStringByContentLength(44)));
            assertEquals(204, response.getStatus(),
                    "This should re-send request without expect and obtain the 204 response code"); //Expectations not supported
        } finally {
            server.stop();
        }
    }

    @Test
    public void testExpect100ContinueUnauthorized() throws Exception {
        final TestSocketServer server = new TestSocketServer(portNumber);
        try {
            server.runServer();

            assertThrows(ProcessingException.class, () -> target(RESOURCE_PATH_UNAUTHORIZED)
                    .property(ClientProperties.EXPECT_100_CONTINUE_THRESHOLD_SIZE, 43L)
                    .property(ClientProperties.EXPECT_100_CONTINUE, Boolean.TRUE)
                    .property(NettyClientProperties.EXPECT_100_CONTINUE_TIMEOUT, 10000)
                    .request().header(HttpHeaders.CONTENT_LENGTH, 44L)
                    .post(Entity.text(generateStringByContentLength(44))));
        } finally {
            server.stop();
        }
    }

    @Test
    public void testExpect100ContinuePayloadTooLarge() {
        assertThrows(ProcessingException.class, () -> target(RESOURCE_PATH_PAYLOAD_TOO_LARGE)
                .property(ClientProperties.EXPECT_100_CONTINUE_THRESHOLD_SIZE, 43L)
                .property(ClientProperties.EXPECT_100_CONTINUE, Boolean.TRUE)
                .property(NettyClientProperties.EXPECT_100_CONTINUE_TIMEOUT, 10000)
                .request().header(HttpHeaders.CONTENT_LENGTH, 44L)
                .post(Entity.text(generateStringByContentLength(44))));
    }

    @Test
    public void testExpect100ContinueMethodNotSupported() throws Exception {
        final TestSocketServer server = new TestSocketServer(portNumber);
        try {
            server.runServer();

            assertThrows(ProcessingException.class, () ->  target(RESOURCE_PATH_METHOD_NOT_SUPPORTED)
                    .property(ClientProperties.EXPECT_100_CONTINUE_THRESHOLD_SIZE, 43L)
                    .property(ClientProperties.EXPECT_100_CONTINUE, Boolean.TRUE)
                    .property(NettyClientProperties.EXPECT_100_CONTINUE_TIMEOUT, 10000)
                    .request().header(HttpHeaders.CONTENT_LENGTH, 44L)
                    .post(Entity.text(generateStringByContentLength(44))));

        } finally {
            server.stop();
        }
    }

    private String generateStringByContentLength(int length) {
        final char[] array = new char[length];
        final Random r = new Random();
        for (int i = 0; i < length; i++) {
            array[i] = ENTITY_STRING.charAt(r.nextInt(ENTITY_STRING.length()));
        }
        return String.valueOf(array);
    }

    private static final class TestSocketServer {

        private static final String NO_CONTENT_HEADER = "HTTP/1.1 204 No Content";
        private static final String OK_HEADER = "HTTP/1.1 200 OK";
        private static final String EXPECT_HEADER = "HTTP/1.1 100 Continue";
        private static final String UNAUTHORIZED_HEADER = "HTTP/1.1 401 Unauthorized";
        private static final String NOT_SUPPORTED_HEADER = "HTTP/1.1 405 Method Not Allowed";

        private final ExecutorService executorService = Executors.newCachedThreadPool();
        private AtomicBoolean unauthorized = new AtomicBoolean(false);
        private AtomicBoolean not_supported = new AtomicBoolean(false);

        private AtomicBoolean expect_processed = new AtomicBoolean(false);

        private ServerSocket server;

        private volatile boolean stopped = false;

        public TestSocketServer(int port) throws IOException {
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

                    boolean failed = processExpect100Continue(headers, writer);

                    if (failed) {
                        continue;
                    }

                    final String http_header = expect_processed.get() ? NO_CONTENT_HEADER : OK_HEADER;
                    boolean read = readBody(reader, headers);

                    final StringBuffer responseBuffer = new StringBuffer(http_header);
                    addNewLineToResponse(responseBuffer);
                    addServerHeaderToResponse(responseBuffer);
                    addNewLineToResponse(responseBuffer);
                    addNewLineToResponse(responseBuffer);

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
            addToResponse("Server: SocketServer v.0.0.1", responseBuffer);
            addNewLineToResponse(responseBuffer);
        }

        private boolean processExpect100Continue(Map<String, String> headers, BufferedWriter writer) throws IOException {
            String http_header = EXPECT_HEADER;
            boolean failed = false;
            final String continueHeader = headers.remove("expect");

            if (continueHeader != null && continueHeader.contains("100-continue")) {

                if (unauthorized.get()) {
                    http_header = UNAUTHORIZED_HEADER;
                    unauthorized.set(false);
                    failed = true;
                }

                if (not_supported.get()) {
                    http_header = NOT_SUPPORTED_HEADER;
                    not_supported.set(false);
                    failed = true;
                }

                expect_processed.set(http_header.equals(EXPECT_HEADER));


                final StringBuffer responseBuffer = new StringBuffer(http_header);

                addNewLineToResponse(responseBuffer);
                addToResponse("Connection: keep-alive", responseBuffer);
                addNewLineToResponse(responseBuffer);
                addNewLineToResponse(responseBuffer);

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

                if (line.contains(RESOURCE_PATH_UNAUTHORIZED)) {
                    unauthorized.set(true);
                }

                if (line.contains(RESOURCE_PATH_METHOD_NOT_SUPPORTED)) {
                    not_supported.set(true);
                }
                int pos = line.indexOf(':');
                if (pos > -1) {
                    headers.put(
                            line.substring(0, pos).toLowerCase(Locale.ROOT),
                            line.substring(pos + 2).toLowerCase(Locale.ROOT).trim());
                }
            }

            return headers;
        }

        private boolean readBody(BufferedReader reader, Map<String, String> headers) throws IOException, InterruptedException {
            if (headers.containsKey("content-length")) {
                int contentLength = Integer.valueOf(headers.get("content-length"));
                int actualLength = 0, readingByte = 0;
                int[] buffer = new int[contentLength];
                while (actualLength < contentLength && (readingByte = reader.read()) != -1) {
                    buffer[actualLength++] = readingByte;
                }
                return (actualLength == contentLength);
            } else if (headers.containsKey("transfer-encoding")) {
                String line;
                while ((line = reader.readLine()) != null && !line.equals("0")) {
                }
                return true;
            }
            return false;
        }

    }
}