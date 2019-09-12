/*
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
 * Contributors:
 *    Marcelo Rubim
 */
package org.glassfish.jersey.jetty.connector;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Base64;

import static org.junit.Assert.assertEquals;

/**
 * @author Marcelo Rubim
\ */
public class ProxyTest extends JerseyTest {
    private static final Charset CHARACTER_SET = Charset.forName("iso-8859-1");
    private static final String PROXY_URI = "http://127.0.0.1:9997";
    private static final String PROXY_USERNAME = "proxy-user";
    private static final String PROXY_PASSWORD = "proxy-password";


    @Path("")
    public static class ProxyResource {

        @GET
        public Response getProxy() {
            return Response.status(407).header("Proxy-Authenticate", "Basic").build();
        }

    }

    @Path("proxyTest")
    public static class ProxyTestResource {

        @GET
        public Response getOK() {
            return Response.ok().build();
        }

    }

    @Override
    protected Application configure() {
        ResourceConfig config = new ResourceConfig(ProxyResource.class, ProxyTestResource.class);
        return config;
    }

    @Override
    protected void configureClient(ClientConfig config) {
        config.connectorProvider(new JettyConnectorProvider());
    }

    @Test
    public void testGet407() {
        startFakeProxy();
        client().property(ClientProperties.PROXY_URI, ProxyTest.PROXY_URI);
        Response response = target("proxyTest").request().get();
        assertEquals(407, response.getStatus());
    }

    @Test
    public void testGetSuccess() {
        startFakeProxy();
        client().property(ClientProperties.PROXY_URI, ProxyTest.PROXY_URI);
        client().property(ClientProperties.PROXY_USERNAME, ProxyTest.PROXY_USERNAME);
        client().property(ClientProperties.PROXY_PASSWORD, ProxyTest.PROXY_PASSWORD);
        Response response = target("proxyTest").request().get();
        assertEquals(200, response.getStatus());
    }

    private void startFakeProxy(){
        Server server = new Server(9997);
        server.setHandler(new ProxyHandler());
        try {
            server.start();
        } catch (Exception e) {

        }
    }

    class ProxyHandler extends AbstractHandler {
        @Override
        public void handle(String target,
                           Request baseRequest,
                           HttpServletRequest request,
                           HttpServletResponse response) throws IOException,
                ServletException {

            if (request.getHeader("Proxy-Authorization") != null) {
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
                response.setStatus(200);
                //TODO Add redirect to requestURI
            } else {
                response.setStatus(407);
                response.addHeader("Proxy-Authenticate", "Basic");
            }


            baseRequest.setHandled(true);
        }
    }
}
