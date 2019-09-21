/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.api;

import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test setting headers that are restricted by {@link java.net.HttpURLConnection}.
 *
 * @author Miroslav Fuksa
 */
public class RestrictedHeaderTest extends JerseyTest {

    private static final Logger LOGGER = Logger.getLogger(RestrictedHeaderTest.class.getName());

    @Path("/")
    public static class MyResource {

        @GET
        public Response getOptions(@Context HttpHeaders headers) {
            MultivaluedMap<String, String> requestHeaders = headers.getRequestHeaders();
            System.out.println("Headers: " + requestHeaders);
            if (requestHeaders.containsKey("Origin") || requestHeaders.containsKey("Access-Control-Request-Method")) {
                LOGGER.info("CORS headers found.");
                return Response.ok().build();
            }
            LOGGER.info("CORS headers are missing. ");
            return Response.serverError().entity("CORS headers are missing").build();
        }
    }

    @Override
    protected ResourceConfig configure() {
        enable(TestProperties.LOG_TRAFFIC);
        return new ResourceConfig(MyResource.class, LoggingFeature.class);
    }

    @Ignore("The setting of allowRestrictedHeaders system property is global and cached. Only "
            + "one of both testForbiddenHeadersNotAllowed() and testForbiddenHeadersAllowed() can be run during one test.")
    @Test
    public void testForbiddenHeadersNotAllowed() {
        Client client = ClientBuilder.newClient();
        Response response = testHeaders(client);
        Assert.assertEquals(500, response.getStatus());
    }

    /**
     * Tests sending of restricted headers (Origin and Access-Control-Request-Method) which are
     * used for CORS. These headers are by default skipped by the {@link java.net.HttpURLConnection}.
     * The system property {@code sun.net.http.allowRestrictedHeaders} must be defined in order to
     * allow these headers.
     */
    @Test
    public void testForbiddenHeadersAllowed() {
        Client client = ClientBuilder.newClient();
        System.setProperty("sun.net.http.allowRestrictedHeaders", "true");

        Response response = testHeaders(client);
        System.out.println(response.readEntity(String.class));
        Assert.assertEquals(200, response.getStatus());
    }

    /**
     * Same as {@link #testForbiddenHeadersAllowed()} ()} but uses {@link org.glassfish.jersey.apache.connector
     * .ApacheConnector} connector
     * which allows modification of these headers.
     */
    @Test
    public void testForbiddenHeadersWithApacheConnector() {
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.connectorProvider(new ApacheConnectorProvider());
        Client client = ClientBuilder.newClient(clientConfig);
        testHeaders(client);
    }

    private Response testHeaders(Client client) {
        client.register(LoggingFeature.class);
        Invocation.Builder builder = client.target(getBaseUri()).path("/").request()
                .header("Origin", "http://example.com")
                .header("Access-Control-Request-Method", "POST")
                .header("Testus", "Hello");
        return builder.get();
    }
}
