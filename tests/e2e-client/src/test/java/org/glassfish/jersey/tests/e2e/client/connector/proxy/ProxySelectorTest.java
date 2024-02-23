/*
 * Copyright (c) 2020, 2023 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2019 Banco do Brasil S/A. All rights reserved.
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

package org.glassfish.jersey.tests.e2e.client.connector.proxy;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpChannel;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.Callback;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.netty.connector.NettyClientProperties;
import org.glassfish.jersey.netty.connector.NettyConnectorProvider;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Moved from jetty-connector
 * @author Marcelo Rubim
 */
public class ProxySelectorTest {
    private static final String NO_PASS = "no-pass";

    protected void configureClient(ClientConfig config) {
        config.connectorProvider(new NettyConnectorProvider()).property(NettyClientProperties.FILTER_HEADERS_FOR_PROXY, false);
    }

    @Test
    public void testGetNoPass() {
        try (Response response = target("proxyTest").request().header(NO_PASS, 200).get()) {
            assertEquals(200, response.getStatus());
        }
    }

    @Test
    public void testGet407() {
        try (Response response = target("proxyTest").request().get()) {
            assertEquals(407, response.getStatus());
        } catch (ProcessingException pe) {
            Assertions.assertTrue(pe.getMessage().contains("407")); // netty
        }
    }

    private static Server server;
    @BeforeAll
    public static void startFakeProxy() {
        server = new Server(9997);
        server.setHandler(new ProxyHandler());
        try {
            server.start();
        } catch (Exception e) {

        }

        System.setProperty("http.proxyHost", "http://localhost");
        System.setProperty("http.proxyPort", "9997");
    }

    @AfterAll
    public static void tearDownProxy() {
        try {
            server.stop();
        } catch (Exception e) {

        } finally {
            System.clearProperty("http.proxyHost");
            System.clearProperty("http.proxyPort");
        }
    }

    private static Client client;
    @BeforeEach
    public void beforeEach() {
        ClientConfig config = new ClientConfig();
        this.configureClient(config);
        client = ClientBuilder.newClient(config);
    }

    private Client client() {
        return client;
    }

    private WebTarget target(String path) {
        // ProxySelector goes DIRECT to localhost, no matter the proxy
        return client().target("http://eclipse.org:9998").path(path);
    }

    static class ProxyHandler extends Handler.Abstract {
        Set<HttpChannel> httpConnect = new HashSet<>();

        @Override
        public boolean handle(Request request, org.eclipse.jetty.server.Response response, Callback callback) throws Exception {
            if (request.getHeaders().get(NO_PASS) != null) {
                response.setStatus(Integer.parseInt(request.getHeaders().get(NO_PASS)));
            } else {
                response.setStatus(407);
                response.getHeaders().add("Proxy-Authenticate", "Basic");
            }

            callback.succeeded();
            return true;
        }
    }
}
