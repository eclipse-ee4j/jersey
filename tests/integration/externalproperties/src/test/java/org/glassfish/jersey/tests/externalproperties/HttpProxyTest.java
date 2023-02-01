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

package org.glassfish.jersey.tests.externalproperties;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.glassfish.jersey.ExternalProperties;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

public class HttpProxyTest extends JerseyTest {
    public HttpProxyTest() {
        set(TestProperties.CONTAINER_PORT, 0);
    }

    private static final String PROXY_HOST = "localhost";
    private static final String PROXY_PORT = "0";
    private static boolean proxyHit = false;

    @Path("resource")
    public static class ProxyTestResource {

        @GET
        public String getOK() {
            return "OK";
        }

    }

    @Override
    protected Application configure() {
        return new ResourceConfig(ProxyTestResource.class);
    }

    @BeforeEach
    public void startFakeProxy() {
        System.setProperty(ExternalProperties.HTTP_PROXY_HOST, PROXY_HOST);
        Server server = new Server(Integer.parseInt(PROXY_PORT));
        server.setHandler(new ProxyHandler(false));
        try {
            server.start();
        } catch (Exception e) {

        }
        System.setProperty(ExternalProperties.HTTP_PROXY_PORT, String.valueOf(server.getURI().getPort()));
    }

    @Test
    public void testProxy() {
        System.setProperty(ExternalProperties.HTTP_NON_PROXY_HOSTS, "");

        Response response = target("resource").request().get();

        Assertions.assertEquals(407, response.getStatus());
    }

    @Test
    public void testNonProxy() {
        System.setProperty(ExternalProperties.HTTP_NON_PROXY_HOSTS, "localhost");

        Response response = target("resource").request().get();

        Assertions.assertEquals(200, response.getStatus());
        Assertions.assertEquals("OK", response.readEntity(String.class));
        Assertions.assertFalse(proxyHit);
    }

    class ProxyHandler extends AbstractHandler {
        @Override
        public void handle(String target,
                           Request baseRequest,
                           HttpServletRequest request,
                           HttpServletResponse response) {
            proxyHit = true;
            response.setStatus(407);
            baseRequest.setHandled(true);
        }

        ProxyHandler(boolean pProxyHit) {
            super();
            proxyHit = pProxyHit;
        }
    }

}

