/*
 * Copyright (c) 2020, 2022 Oracle and/or its affiliates. All rights reserved.
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

import org.eclipse.jetty.server.HttpChannel;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.apache5.connector.Apache5ConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.spi.ConnectorProvider;
import org.glassfish.jersey.jetty.connector.JettyConnectorProvider;
import org.glassfish.jersey.netty.connector.NettyConnectorProvider;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 * Moved from jetty-connector
 * @author Marcelo Rubim
 */
@RunWith(Parameterized.class)
public class ProxySelectorTest {
    private static final String NO_PASS = "no-pass";

    @Parameterized.Parameters(name = "{index}: {0}")
    public static List<Object[]> testData() {
        return Arrays.asList(new Object[][]{
//                {ApacheConnectorProvider.class},
//                {Apache5ConnectorProvider.class},
//                {JettyConnectorProvider.class},
                {NettyConnectorProvider.class},
        });
    }

    private final ConnectorProvider connectorProvider;

    public ProxySelectorTest(Class<? extends ConnectorProvider> connectorProviderClass)
            throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        this.connectorProvider = connectorProviderClass.getConstructor().newInstance();
    }

    protected void configureClient(ClientConfig config) {
        config.connectorProvider(connectorProvider);
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
            Assert.assertTrue(pe.getMessage().contains("407")); // netty
        }
    }

    private static Server server;
    @BeforeClass
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

    @AfterClass
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
    @Before
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

    static class ProxyHandler extends AbstractHandler {
        Set<HttpChannel> httpConnect = new HashSet<>();
        @Override
        public void handle(String target,
                           Request baseRequest,
                           HttpServletRequest request,
                           HttpServletResponse response) {
            if (request.getHeader(NO_PASS) != null) {
                response.setStatus(Integer.parseInt(request.getHeader(NO_PASS)));
            } else {
                response.setStatus(407);
                response.addHeader("Proxy-Authenticate", "Basic");
            }

            baseRequest.setHandled(true);
        }
    }
}
