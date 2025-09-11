/*
 * Copyright (c) 2022, 2025 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.jnh.connector.test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.client.InvocationCallback;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jnh.connector.JavaNetHttpConnectorProvider;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HelloWorldTest extends JerseyTest {

    private static final Logger LOGGER = Logger.getLogger(HelloWorldTest.class.getName());
    private static final String ROOT_PATH = "helloworld";

    @Path("helloworld")
    public static class HelloWorldResource {
        public static final String CLICHED_MESSAGE = "Hello World!";

        @GET
        @Produces("text/plain")
        public String getHello() {
            return CLICHED_MESSAGE;
        }

    }

    @Override
    protected Application configure() {
        ResourceConfig config = new ResourceConfig(HelloWorldResource.class);
        config.register(new LoggingFeature(LOGGER, LoggingFeature.Verbosity.PAYLOAD_ANY));
        return config;
    }

    @Override
    protected void configureClient(ClientConfig config) {
        config.connectorProvider(new JavaNetHttpConnectorProvider());
    }

    @Test
    public void testConnection() {
        Response response = target().path(ROOT_PATH).request("text/plain").get();
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testClientStringResponse() {
        String s = target().path(ROOT_PATH).request().get(String.class);
        assertEquals(HelloWorldResource.CLICHED_MESSAGE, s);
    }

    @Test
    public void testAsyncClientRequests() throws InterruptedException {
        final int REQUESTS = 20;
        final CountDownLatch latch = new CountDownLatch(REQUESTS);
        final long tic = System.currentTimeMillis();
        for (int i = 0; i < REQUESTS; i++) {
            final int id = i;
            target().path(ROOT_PATH).request().async().get(new InvocationCallback<Response>() {
                @Override
                public void completed(Response response) {
                    try {
                        final String result = response.readEntity(String.class);
                        assertEquals(HelloWorldResource.CLICHED_MESSAGE, result);
                    } finally {
                        latch.countDown();
                    }
                }

                @Override
                public void failed(Throwable error) {
                    error.printStackTrace();
                    latch.countDown();
                }
            });
        }
        latch.await(10 * getAsyncTimeoutMultiplier(), TimeUnit.SECONDS);
        final long toc = System.currentTimeMillis();
        Logger.getLogger(HelloWorldTest.class.getName()).info("Executed in: " + (toc - tic));
    }

    @Test
    public void testHead() {
        Response response = target().path(ROOT_PATH).request().head();
        assertEquals(200, response.getStatus());
        assertEquals(MediaType.TEXT_PLAIN_TYPE, response.getMediaType());
    }

    @Test
    public void testFooBarOptions() {
        Response response = target().path(ROOT_PATH).request().header("Accept", "foo/bar").options();
        assertEquals(200, response.getStatus());
        final String allowHeader = response.getHeaderString("Allow");
        _checkAllowContent(allowHeader);
        assertEquals("foo/bar", response.getMediaType().toString());
        assertEquals(0, response.getLength());
    }

    @Test
    public void testTextPlainOptions() {
        Response response = target().path(ROOT_PATH).request().header("Accept", MediaType.TEXT_PLAIN).options();
        assertEquals(200, response.getStatus());
        final String allowHeader = response.getHeaderString("Allow");
        _checkAllowContent(allowHeader);
        assertEquals(MediaType.TEXT_PLAIN_TYPE, response.getMediaType());
        final String responseBody = response.readEntity(String.class);
        _checkAllowContent(responseBody);
    }

    private void _checkAllowContent(final String content) {
        assertTrue(content.contains("GET"));
        assertTrue(content.contains("HEAD"));
        assertTrue(content.contains("OPTIONS"));
    }

    @Test
    public void testMissingResourceNotFound() {
        Response response;

        response = target().path(ROOT_PATH + "arbitrary").request().get();
        assertEquals(404, response.getStatus());
        response.close();

        response = target().path(ROOT_PATH).path("arbitrary").request().get();
        assertEquals(404, response.getStatus());
        response.close();
    }

}
