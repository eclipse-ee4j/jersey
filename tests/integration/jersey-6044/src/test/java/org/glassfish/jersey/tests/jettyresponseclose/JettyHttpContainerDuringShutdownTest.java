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

package org.glassfish.jersey.tests.jettyresponseclose;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.Response;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.StatisticsHandler;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class JettyHttpContainerDuringShutdownTest {

    private static Server server;
    private static final String URL = "http://localhost:9080/test/get-me-204";

    @BeforeAll
    public static void setup() throws Exception {
        ResourceConfig resourceConfig = new ResourceConfig(Collections.singleton(Resource204.class));
        ServletContextHandler ctx = new ServletContextHandler();
        ServletHolder servlet = new ServletHolder(new ServletContainer(resourceConfig));
        ctx.addServlet(servlet, "/test/*");
        ContextHandlerCollection handlers = new ContextHandlerCollection();
        server = new Server();

        handlers.addHandler(ctx);
        StatisticsHandler statisticsHandler = new StatisticsHandler();
        statisticsHandler.setHandler(handlers);
        server.setHandler(statisticsHandler);

        ServerConnector http = new ServerConnector(server, new HttpConnectionFactory(new HttpConfiguration()));
        http.setPort(9080);
        server.addConnector(http);
        server.setStopTimeout(50);
        server.start();

        waitStartUp();
    }

    private static void waitStartUp() {
        for (int i = 0; i < 10; i++) {
            try {
                Response response = ClientBuilder.newClient().target(URL).request().get();
                if (response.getStatus() == 204) {
                    return;
                }
            } catch (Throwable ignore) {
            }
        }
    }

    @AfterAll
    public static void shutdown() throws Exception {
        if (server != null) {
            try {
                server.stop();
                server = null;
            } catch (Throwable ignore) {
            }
        }
    }

    @Test
    public void testResponseClose() throws Exception {
        Client client = ClientBuilder.newClient();
        try {
            List<Future<Response>> waitingResponses = new ArrayList<>();
            for (int i = 0; i < 100; i++) {
                waitingResponses.add(client.target(URL).request().async().get());
            }
            shutdown();
            for (Future<Response> responseFuture : waitingResponses) {
                Response response = responseFuture.get(30, TimeUnit.SECONDS);
                response.close();
                Assertions.assertNotEquals(200, response.getStatus());
                if (response.getStatus() / 100 != 2 && response.getStatus() / 100 != 5) {
                    Assertions.fail("Unexpected response code: " + response.getStatus());
                }
            }
        } finally {
            try {
                client.close();
            } catch (Throwable ignored) {
            }
        }
    }
}
