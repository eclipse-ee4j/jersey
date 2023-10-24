/*
 * Copyright (c) 2020, 2023 Oracle and/or its affiliates. All rights reserved.
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

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.RequestEntityProcessing;
import org.glassfish.jersey.client.http.Expect100ContinueFeature;
import org.glassfish.jersey.netty.connector.NettyClientProperties;
import org.glassfish.jersey.netty.connector.NettyConnectorProvider;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import java.io.IOException;

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

    private static Server server;
    @BeforeAll
    public static void startExpect100ContinueTestServer() {
        server = new Server(portNumber);
        server.setHandler(new Expect100ContinueTestHandler());
        try {
            server.start();
        } catch (Exception e) {

        }
    }

    @AfterAll
    public static void stopExpect100ContinueTestServer() {
        try {
            server.stop();
        } catch (Exception e) {
        }
    }

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
    public void testExpect100Continue() {
       final Response response =  target(RESOURCE_PATH).request().post(Entity.text(ENTITY_STRING));
       assertEquals(200, response.getStatus(), "Expected 200"); //no Expect header sent - response OK
    }

    @Test
    public void testExpect100ContinueChunked() {
       final Response response =  target(RESOURCE_PATH).register(Expect100ContinueFeature.basic())
               .property(ClientProperties.REQUEST_ENTITY_PROCESSING,
               RequestEntityProcessing.CHUNKED).request().post(Entity.text(ENTITY_STRING));
       assertEquals(204, response.getStatus(), "Expected 204"); //Expect header sent - No Content response
    }

    @Test
    public void testExpect100ContinueBuffered() {
       final Response response =  target(RESOURCE_PATH).register(Expect100ContinueFeature.basic())
               .property(ClientProperties.REQUEST_ENTITY_PROCESSING,
               RequestEntityProcessing.BUFFERED).request().header(HttpHeaders.CONTENT_LENGTH, 67000L)
               .post(Entity.text(ENTITY_STRING));
       assertEquals(204, response.getStatus(), "Expected 204"); //Expect header sent - No Content response
    }

    @Test
    public void testExpect100ContinueCustomLength() {
       final Response response =  target(RESOURCE_PATH).register(Expect100ContinueFeature.withCustomThreshold(100L))
               .request().header(HttpHeaders.CONTENT_LENGTH, Integer.MAX_VALUE)
               .post(Entity.text(ENTITY_STRING));
       assertEquals(204, response.getStatus(), "Expected 204"); //Expect header sent - No Content response
    }

    @Test
    public void testExpect100ContinueCustomLengthWrong() {
       final Response response =  target(RESOURCE_PATH).register(Expect100ContinueFeature.withCustomThreshold(100L))
               .request().header(HttpHeaders.CONTENT_LENGTH, 99L)
               .post(Entity.text(ENTITY_STRING));
       assertEquals(200, response.getStatus(), "Expected 200"); //Expect header NOT sent - low request size
    }

    @Test
    public void testExpect100ContinueCustomLengthProperty() {
       final Response response =  target(RESOURCE_PATH)
               .property(ClientProperties.EXPECT_100_CONTINUE_THRESHOLD_SIZE, 555L)
               .property(ClientProperties.EXPECT_100_CONTINUE, Boolean.TRUE)
               .register(Expect100ContinueFeature.withCustomThreshold(555L))
               .request().header(HttpHeaders.CONTENT_LENGTH, 666L)
               .post(Entity.text(ENTITY_STRING));
       assertNotNull(response.getStatus()); //Expect header sent - No Content response
    }

    @Test
    public void testExpect100ContinueRegisterViaCustomProperty() {
       final Response response =  target(RESOURCE_PATH)
               .property(ClientProperties.EXPECT_100_CONTINUE_THRESHOLD_SIZE, 43L)
               .property(ClientProperties.EXPECT_100_CONTINUE, Boolean.TRUE)
               .request().header(HttpHeaders.CONTENT_LENGTH, 44L)
               .post(Entity.text(ENTITY_STRING));
       assertEquals(204, response.getStatus(), "Expected 204"); //Expect header sent - No Content response
    }

    @Test
    public void testExpect100ContinueNotSupported() {
       final Response response =  target(RESOURCE_PATH_NOT_SUPPORTED)
               .property(ClientProperties.EXPECT_100_CONTINUE_THRESHOLD_SIZE, 43L)
               .property(ClientProperties.EXPECT_100_CONTINUE, Boolean.TRUE)
               .request().header(HttpHeaders.CONTENT_LENGTH, 44L)
               .post(Entity.text(ENTITY_STRING));
       assertEquals(417, response.getStatus(), "Expected 417"); //Expectations not supported
    }

    @Test
    public void testExpect100ContinueUnauthorized() {
       assertThrows(ProcessingException.class, () -> target(RESOURCE_PATH_UNAUTHORIZED)
               .property(ClientProperties.EXPECT_100_CONTINUE_THRESHOLD_SIZE, 43L)
               .property(ClientProperties.EXPECT_100_CONTINUE, Boolean.TRUE)
               .property(NettyClientProperties.EXPECT_100_CONTINUE_TIMEOUT, 10000)
               .request().header(HttpHeaders.CONTENT_LENGTH, 44L)
               .post(Entity.text(ENTITY_STRING)));
    }

    @Test
    public void testExpect100ContinuePayloadTooLarge() {
        assertThrows(ProcessingException.class, () -> target(RESOURCE_PATH_PAYLOAD_TOO_LARGE)
               .property(ClientProperties.EXPECT_100_CONTINUE_THRESHOLD_SIZE, 43L)
               .property(ClientProperties.EXPECT_100_CONTINUE, Boolean.TRUE)
               .property(NettyClientProperties.EXPECT_100_CONTINUE_TIMEOUT, 10000)
               .request().header(HttpHeaders.CONTENT_LENGTH, 44L)
               .post(Entity.text(ENTITY_STRING)));
    }

    @Test
    public void testExpect100ContinueMethodNotSupported() {
        assertThrows(ProcessingException.class, () -> target(RESOURCE_PATH_METHOD_NOT_SUPPORTED)
               .property(ClientProperties.EXPECT_100_CONTINUE_THRESHOLD_SIZE, 43L)
               .property(ClientProperties.EXPECT_100_CONTINUE, Boolean.TRUE)
               .property(NettyClientProperties.EXPECT_100_CONTINUE_TIMEOUT, 10000)
               .request().header(HttpHeaders.CONTENT_LENGTH, 44L)
               .post(Entity.text(ENTITY_STRING)));
    }

    static class Expect100ContinueTestHandler extends AbstractHandler {
        @Override
        public void handle(String target,
                           Request baseRequest,
                           HttpServletRequest request,
                           HttpServletResponse response) throws IOException {
            boolean expected = request.getHeader("Expect") != null;
            boolean failed = false;
            if (target.equals("/" + RESOURCE_PATH_NOT_SUPPORTED)) {
                response.sendError(417);
                failed = true;
            }
            if (target.equals("/" + RESOURCE_PATH_UNAUTHORIZED)) {
                response.sendError(401);
                failed = true;
            }
            if (target.equals("/" + RESOURCE_PATH_PAYLOAD_TOO_LARGE)) {
                response.sendError(413);
                failed = true;
            }
            if (target.equals("/" + RESOURCE_PATH_METHOD_NOT_SUPPORTED)) {
                response.sendError(405);
                failed = true;
            }
            if (expected && !failed) {
                System.out.println("Expect:100-continue found, sending response header");
                response.setStatus(204);
            }
            response.getWriter().println();
            response.flushBuffer();
            baseRequest.setHandled(true);

            request.getReader().lines().forEach(System.out::println);
        }
    }
}