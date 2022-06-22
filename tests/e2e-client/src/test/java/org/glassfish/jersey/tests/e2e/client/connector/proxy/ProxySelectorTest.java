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
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.HttpUrlConnectorProvider;
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Base64;
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
    private static final String PROXY_USERNAME = "proxy-user";
    private static final String PROXY_PASSWORD = "proxy-password";

    @Parameterized.Parameters(name = "{index}: {0}")
    public static List<Object[]> testData() {
        return Arrays.asList(new Object[][]{
//                {ApacheConnectorProvider.class},
//                {Apache5ConnectorProvider.class},
//                {JettyConnectorProvider.class},
                {NettyConnectorProvider.class},
                {HttpUrlConnectorProvider.class},
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

    @Test
    public void testGetSuccess() {
        // Next applies for HttpUrlConnectorProvider. It seems Netty is not supporting user/pass in System properties
        if (connectorProvider.getClass() == HttpUrlConnectorProvider.class) {
            try {
                System.setProperty("http.proxyUser", PROXY_USERNAME);
                System.setProperty("http.proxyPassword", PROXY_PASSWORD);
                Response response = target("proxyTest").request().get();
                response.bufferEntity();
                assertEquals(response.readEntity(String.class), 200, response.getStatus());
            } finally {
                System.clearProperty("http.proxyUser");
                System.clearProperty("http.proxyPassword");
            }
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
            } else if (request.getHeader("Proxy-Authorization") != null) {
                String proxyAuthorization = request.getHeader("Proxy-Authorization");
                String decoded = new String(Base64.getDecoder().decode(proxyAuthorization.substring(6).getBytes()));
                final String[] split = decoded.split(":");
                final String username = split[0];
                final String password = split[1];

                if (!username.equals(PROXY_USERNAME)) {
                    response.setStatus(400);
                    System.out.println("Found unexpected username: " + username);
                }

                if (!password.equals(PROXY_PASSWORD)) {
                    response.setStatus(400);
                    System.out.println("Found unexpected password: " + username);
                }

                if (response.getStatus() != 400) {
                    response.setStatus(200);
                    if ("CONNECT".equalsIgnoreCase(baseRequest.getMethod())) { // NETTY way of doing proxy
                        httpConnect.add(baseRequest.getHttpChannel());
                    }
                }
            } else {
                response.setStatus(407);
                response.addHeader("Proxy-Authenticate", "Basic");
            }

            baseRequest.setHandled(true);
        }
    }
}
