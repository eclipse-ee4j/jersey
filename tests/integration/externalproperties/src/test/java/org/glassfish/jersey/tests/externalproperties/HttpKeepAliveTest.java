/*
 * Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved.
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
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URI;

public class HttpKeepAliveTest {

    private final URI BASE_URI = URI.create("http://localhost:9997/");
    private final String CONNECTION_HEADER = "Connection";
    private HttpServer server;

    @Test
    public void testHttpKeepAlive() {
        Response response = ClientBuilder.newClient()
                .target(BASE_URI)
                .request()
                .get();

        Assert.assertEquals("close", response.readEntity(String.class));
        Assert.assertEquals(200, response.getStatus());
    }

    @Before
    public void startServer()  {
        server = GrizzlyHttpServerFactory.createHttpServer(BASE_URI);

        try {
            server.start();
        } catch (IOException ioe) {
            throw new ProcessingException("Grizzly server failed to start");
        }

        server.getServerConfiguration().addHttpHandler(new HttpHandler() {
            @Override
            public void service(Request request,
                                org.glassfish.grizzly.http.server.Response response) throws Exception {

                String connectionHeader = request.getHeader(CONNECTION_HEADER);

                response.setContentType("text/plain");
                response.setContentLength(connectionHeader.length());
                response.getWriter().write(connectionHeader);
                response.setStatus(200);
            }
        });
    }

    @After
    public void stopServer() {
        try {
            server.shutdownNow();
        } catch (Exception e) {

        }
    }

}
