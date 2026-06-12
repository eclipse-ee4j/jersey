/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
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

import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.AsyncInvoker;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.Response;

import java.lang.System.Logger;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static java.lang.System.Logger.Level.INFO;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class JettyHttpContainerDuringShutdownTest {

    private static final Logger LOG = System.getLogger(JettyHttpContainerDuringShutdownTest.class.getName());
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
        server.start();

        waitStartUp();
    }

    private static void waitStartUp() {
        for (int i = 0; i < 10; i++) {
            try (Client client = ClientBuilder.newClient();
                Response response = client.target(URL).request().get()) {
                if (response.getStatus() == 204) {
                    return;
                }
                LOG.log(INFO, "Waiting for the startup ... response status: " + response.getStatus());
            } catch (Exception e) {
                LOG.log(INFO, "Waiting for the startup ... response status: ", e);
            }
        }
    }

    @AfterAll
    public static void shutdown() throws Exception {
        if (server != null) {
            shutdownServer(10_000);
        }
    }


    /**
     * Tests that all responses are closed properly when the server is shutting down. The test sends
     * a large number of requests to the server and then initiates the shutdown process. It verifies
     * that all responses are either successfully received with a 204 status code or result in a
     * ProcessingException caused by a ConnectException, indicating that the connection was closed
     * during shutdown.
     *
     * @throws Exception
     */
    @Test
    public void testResponseClose() throws Exception {
        AtomicInteger countOf204 = new AtomicInteger();
        AtomicInteger countOfConnException = new AtomicInteger();
        try (Client client = ClientBuilder.newClient()) {
            // create requests but don't invoke them
            List<AsyncInvoker> invokers = new ArrayList<>();
            for (int i = 0; i < 1_000; i++) {
                invokers.add(client.target(URL).request().async());
            }
            // invoke them fast as possible.
            List<Future<Response>> waitingResponses = invokers.stream().map(AsyncInvoker::get).collect(Collectors.toList());
            assertThrows(TimeoutException.class, () -> shutdownServer(100));
            for (Future<Response> responseFuture : waitingResponses) {
                try (Response response = responseFuture.get(30, TimeUnit.SECONDS)) {
                    assertThat("Unexpected response code", response.getStatus(), equalTo(204));
                    countOf204.incrementAndGet();
                } catch (ExecutionException e) {
                    Throwable cause = e.getCause();
                    assertThat(cause, instanceOf(ProcessingException.class));
                    assertThat(cause.getCause(), instanceOf(ConnectException.class));
                    countOfConnException.incrementAndGet();
                }
            }
        }
        // There is no guarantee that shutdown would not make it before requests.
        // But we should be able to process at least some of HTTP requests.
        LOG.log(INFO, "Count of results:\n  HTTP 204: {0}\n  ConnectException: {1}", countOf204, countOfConnException);
        assertAll(
            () -> assertThat("Count of HTTP 204", countOf204.get(), greaterThan(0)),
            () -> assertThat("Count of ConnectException", countOfConnException.get(), greaterThanOrEqualTo(0))
        );
    }


    private static void shutdownServer(int timeoutInMillis) throws Exception {
        LOG.log(INFO, "Stopping the server: " + server);
        server.setStopTimeout(timeoutInMillis);
        server.stop();
        server = null;
    }
}
