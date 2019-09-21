/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.jetty.connector;

import java.util.List;
import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * Tests the headers.
 *
 * @author Stepan Kopriva
 */
public class HttpHeadersTest extends JerseyTest {

    private static final Logger LOGGER = Logger.getLogger(HttpHeadersTest.class.getName());

    @Path("/test")
    public static class HttpMethodResource {
        @POST
        public String post(
                @HeaderParam("Transfer-Encoding") String transferEncoding,
                @HeaderParam("X-CLIENT") String xClient,
                @HeaderParam("X-WRITER") String xWriter,
                String entity) {
            assertEquals("client", xClient);
            return "POST";
        }

        @GET
        public String testUserAgent(@Context HttpHeaders httpHeaders) {
            final List<String> requestHeader = httpHeaders.getRequestHeader(HttpHeaders.USER_AGENT);
            if (requestHeader.size() != 1) {
                return "FAIL";
            }
            return requestHeader.get(0);
        }
    }

    @Override
    protected Application configure() {
        ResourceConfig config = new ResourceConfig(HttpMethodResource.class);
        config.register(new LoggingFeature(LOGGER, LoggingFeature.Verbosity.PAYLOAD_ANY));
        return config;
    }

    @Override
    protected void configureClient(ClientConfig config) {
        config.connectorProvider(new JettyConnectorProvider());
    }

    @Test
    public void testPost() {
        Response response = target().path("test").request().header("X-CLIENT", "client").post(null);

        assertEquals(200, response.getStatus());
        assertTrue(response.hasEntity());
    }

    /**
     * Test, that {@code User-agent} header is as set by Jersey, not by underlying Jetty client.
     */
    @Test
    public void testUserAgent() {
        String response = target().path("test").request().get(String.class);
        assertTrue("User-agent header should start with 'Jersey', but was " + response, response.startsWith("Jersey"));
    }
}
