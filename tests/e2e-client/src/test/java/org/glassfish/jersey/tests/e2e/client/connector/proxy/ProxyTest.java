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
import org.glassfish.jersey.grizzly.connector.GrizzlyConnectorProvider;
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
import java.nio.charset.Charset;
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
public class ProxyTest {
    private static final Charset CHARACTER_SET = Charset.forName("iso-8859-1");
    private static final String PROXY_URI = "http://127.0.0.1:9997";
    private static final String PROXY_USERNAME = "proxy-user";
    private static final String PROXY_PASSWORD = "proxy-password";
    private static final String NO_PASS = "no-pass";

    @Parameterized.Parameters(name = "{index}: {0}")
    public static List<Object[]> testData() {
        return Arrays.asList(new Object[][]{
                {ApacheConnectorProvider.class},
                {Apache5ConnectorProvider.class},
                {GrizzlyConnectorProvider.class},
                {JettyConnectorProvider.class},
                {NettyConnectorProvider.class},
                {HttpUrlConnectorProvider.class},
        });
    }

    private final ConnectorProvider connectorProvider;

    public ProxyTest(Class<? extends ConnectorProvider> connectorProviderClass)
            throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        this.connectorProvider = connectorProviderClass.getConstructor().newInstance();
    }

    protected void configureClient(ClientConfig config) {
        config.connectorProvider(connectorProvider);
    }

    @Test
    public void testGetNoPass() {
        client().property(ClientProperties.PROXY_URI, ProxyTest.PROXY_URI);
        try (Response response = target("proxyTest").request().header(NO_PASS, 200).get()) {
            assertEquals(200, response.getStatus());
        }
    }

    @Test
    public void testGet407() {
        // Grizzly sends (String)null password and username
        int expected = GrizzlyConnectorProvider.class.isInstance(connectorProvider) ? 400 : 407;
        client().property(ClientProperties.PROXY_URI, ProxyTest.PROXY_URI);
        try (Response response = target("proxyTest").request().get()) {
            assertEquals(expected, response.getStatus());
        } catch (ProcessingException pe) {
            Assert.assertTrue(pe.getMessage().contains("407")); // netty
        }
    }

    @Test
    public void testGetSuccess() {
        client().property(ClientProperties.PROXY_URI, ProxyTest.PROXY_URI);
        client().property(ClientProperties.PROXY_USERNAME, ProxyTest.PROXY_USERNAME);
        client().property(ClientProperties.PROXY_PASSWORD, ProxyTest.PROXY_PASSWORD);
        Response response = target("proxyTest").request().get();
        response.bufferEntity();
        assertEquals(response.readEntity(String.class), 200, response.getStatus());
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
    }

    @AfterClass
    public static void tearDownProxy() {
        try {
            server.stop();
        } catch (Exception e) {

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
        return client().target("http://localhost:9998").path(path);
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
                String decoded = new String(Base64.getDecoder().decode(proxyAuthorization.substring(6).getBytes()),
                        CHARACTER_SET);
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
                //TODO Add redirect to requestURI
            } else {
                if (httpConnect.contains(baseRequest.getHttpChannel())) {
                    response.setStatus(200);
                } else {
                    response.setStatus(407);
                    response.addHeader("Proxy-Authenticate", "Basic");
                }
            }

            baseRequest.setHandled(true);
        }
    }
}
