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

package org.glassfish.jersey.tests.externalproperties;

import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.internal.Version;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.HttpHeaders;
import java.io.IOException;
import java.net.URI;

public class HttpAgentTest {

    private final String AGENT = "Custom-agent";
    private final URI BASE_URI = URI.create("http://localhost:9997/");
    private HttpServer server;

    @Test
    public void testHttpAgentSetBySystemProperty() {
        javax.ws.rs.core.Response response = JerseyClientBuilder.newClient()
                .target(BASE_URI)
                .request()
                .header(HttpHeaders.USER_AGENT, null)
                .get();

        Assertions.assertTrue(response.readEntity(String.class).contains(AGENT));
    }

    @Test
    public void testUserAgentJerseyHeader() {
        javax.ws.rs.core.Response response = JerseyClientBuilder.newClient()
                .target(BASE_URI)
                .request()
                .get();

        String agentHeader = response.readEntity(String.class);
        Assertions.assertFalse(agentHeader.contains(AGENT));
        Assertions.assertTrue(agentHeader.contains("Jersey/" + Version.getVersion()));
    }

    @BeforeEach
    public void startAgentServer() {
        server = GrizzlyHttpServerFactory.createHttpServer(BASE_URI);
        Runtime.getRuntime().addShutdownHook(new Thread(server::shutdownNow));

        try {
            server.start();
        } catch (IOException ioe) {
            throw new ProcessingException("Grizzly server failed to start");
        }

        server.getServerConfiguration().addHttpHandler(new HttpHandler() {
            @Override
            public void service(Request request, Response response) throws Exception {
                String agentHeader = request.getHeader(HttpHeaders.USER_AGENT);
                response.setContentType("text/plain");
                response.setContentLength(agentHeader.length());
                response.getWriter().write(agentHeader);
                response.setStatus(200);
            }
        });
    }

    @AfterEach
    public void stopAgentServer() {
        server.shutdownNow();
    }

}