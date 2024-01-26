/*
 * Copyright (c) 2022, 2024 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.apache5.connector.test;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.InvocationCallback;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.apache.hc.client5.http.HttpRoute;
import org.apache.hc.client5.http.impl.io.BasicHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.io.ConnectionEndpoint;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.io.LeaseRequest;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.glassfish.jersey.apache5.connector.Apache5ClientProperties;
import org.glassfish.jersey.apache5.connector.Apache5ConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author Jakub Podlesak
 */
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

        @GET
        @Produces("text/plain")
        @Path("error")
        public Response getError() {
            return Response.serverError().entity("Error.").build();
        }

        @GET
        @Produces("text/plain")
        @Path("error2")
        public Response getError2() {
            return Response.serverError().entity("Error2.").build();
        }

    }

    @Override
    protected Application configure() {
        ResourceConfig config = new ResourceConfig(HelloWorldResource.class);
        config.register(new LoggingFeature(LOGGER, Level.INFO, LoggingFeature.Verbosity.PAYLOAD_ANY,
                LoggingFeature.DEFAULT_MAX_ENTITY_SIZE));
        return config;
    }

    @Override
    protected void configureClient(ClientConfig config) {
        config.connectorProvider(new Apache5ConnectorProvider());
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
    public void testConnectionPoolSharingEnabled() throws Exception {
        _testConnectionPoolSharing(true);
    }

    @Test
    public void testConnectionPoolSharingDisabled() throws Exception {
        _testConnectionPoolSharing(false);
    }

    public void _testConnectionPoolSharing(final boolean sharingEnabled) throws Exception {

        final HttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();

        final ClientConfig cc = new ClientConfig();
        cc.property(Apache5ClientProperties.CONNECTION_MANAGER, connectionManager);
        cc.property(Apache5ClientProperties.CONNECTION_MANAGER_SHARED, sharingEnabled);
        cc.connectorProvider(new Apache5ConnectorProvider());

        final Client clientOne = ClientBuilder.newClient(cc);
        WebTarget target = clientOne.target(getBaseUri()).path(ROOT_PATH);
        target.request().get();
        clientOne.close();

        final boolean exceptionExpected = !sharingEnabled;

        final Client clientTwo = ClientBuilder.newClient(cc);
        target = clientTwo.target(getBaseUri()).path(ROOT_PATH);
        try {
            target.request().get();
            if (exceptionExpected) {
                Assertions.fail("Exception expected");
            }
        } catch (Exception e) {
            if (!exceptionExpected) {
                Assertions.fail("Exception not expected");
            }
        } finally {
            clientTwo.close();
        }

        if (sharingEnabled) {
            connectionManager.close();
        }
    }

    @Test
    public void testAsyncClientRequests() throws InterruptedException {
        HttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        ClientConfig cc = new ClientConfig();
        cc.property(Apache5ClientProperties.CONNECTION_MANAGER, connectionManager);
        cc.connectorProvider(new Apache5ConnectorProvider());
        Client client = ClientBuilder.newClient(cc);
        WebTarget target = client.target(getBaseUri());
        final int REQUESTS = 20;
        final CountDownLatch latch = new CountDownLatch(REQUESTS);
        final long tic = System.currentTimeMillis();
        final Map<Integer, String> results = new ConcurrentHashMap<Integer, String>();
        for (int i = 0; i < REQUESTS; i++) {
            final int id = i;
            target.path(ROOT_PATH).request().async().get(new InvocationCallback<Response>() {
                @Override
                public void completed(Response response) {
                    try {
                        final String result = response.readEntity(String.class);
                        results.put(id, result);
                    } finally {
                        latch.countDown();
                    }
                }

                @Override
                public void failed(Throwable error) {
                    Logger.getLogger(HelloWorldTest.class.getName()).log(Level.SEVERE, "Failed on throwable", error);
                    results.put(id, "error: " + error.getMessage());
                    latch.countDown();
                }
            });
        }
        assertTrue(latch.await(10 * getAsyncTimeoutMultiplier(), TimeUnit.SECONDS));
        final long toc = System.currentTimeMillis();
        Logger.getLogger(HelloWorldTest.class.getName()).info("Executed in: " + (toc - tic));

        StringBuilder resultInfo = new StringBuilder("Results:\n");
        for (int i = 0; i < REQUESTS; i++) {
            String result = results.get(i);
            resultInfo.append(i).append(": ").append(result).append('\n');
        }
        Logger.getLogger(HelloWorldTest.class.getName()).info(resultInfo.toString());

        for (int i = 0; i < REQUESTS; i++) {
            String result = results.get(i);
            assertEquals(HelloWorldResource.CLICHED_MESSAGE, result);
        }
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

    @Test
    public void testLoggingFilterClientClass() {
        Client client = client();
        client.register(CustomLoggingFilter.class).property("foo", "bar");
        CustomLoggingFilter.preFilterCalled = CustomLoggingFilter.postFilterCalled = 0;
        String s = target().path(ROOT_PATH).request().get(String.class);
        assertEquals(HelloWorldResource.CLICHED_MESSAGE, s);
        assertEquals(1, CustomLoggingFilter.preFilterCalled);
        assertEquals(1, CustomLoggingFilter.postFilterCalled);
    }

    @Test
    public void testLoggingFilterClientInstance() {
        Client client = client();
        client.register(new CustomLoggingFilter()).property("foo", "bar");
        CustomLoggingFilter.preFilterCalled = CustomLoggingFilter.postFilterCalled = 0;
        String s = target().path(ROOT_PATH).request().get(String.class);
        assertEquals(HelloWorldResource.CLICHED_MESSAGE, s);
        assertEquals(1, CustomLoggingFilter.preFilterCalled);
        assertEquals(1, CustomLoggingFilter.postFilterCalled);
    }

    @Test
    public void testLoggingFilterTargetClass() {
        WebTarget target = target().path(ROOT_PATH);
        target.register(CustomLoggingFilter.class).property("foo", "bar");
        CustomLoggingFilter.preFilterCalled = CustomLoggingFilter.postFilterCalled = 0;
        String s = target.request().get(String.class);
        assertEquals(HelloWorldResource.CLICHED_MESSAGE, s);
        assertEquals(1, CustomLoggingFilter.preFilterCalled);
        assertEquals(1, CustomLoggingFilter.postFilterCalled);
    }

    @Test
    public void testLoggingFilterTargetInstance() {
        WebTarget target = target().path(ROOT_PATH);
        target.register(new CustomLoggingFilter()).property("foo", "bar");
        CustomLoggingFilter.preFilterCalled = CustomLoggingFilter.postFilterCalled = 0;
        String s = target.request().get(String.class);
        assertEquals(HelloWorldResource.CLICHED_MESSAGE, s);
        assertEquals(1, CustomLoggingFilter.preFilterCalled);
        assertEquals(1, CustomLoggingFilter.postFilterCalled);
    }

    @Test
    public void testConfigurationUpdate() {
        Client client1 = client();
        client1.register(CustomLoggingFilter.class).property("foo", "bar");

        Client client = ClientBuilder.newClient(client1.getConfiguration());
        CustomLoggingFilter.preFilterCalled = CustomLoggingFilter.postFilterCalled = 0;
        String s = client.target(getBaseUri()).path(ROOT_PATH).request().get(String.class);
        assertEquals(HelloWorldResource.CLICHED_MESSAGE, s);
        assertEquals(1, CustomLoggingFilter.preFilterCalled);
        assertEquals(1, CustomLoggingFilter.postFilterCalled);
    }

    /**
     * JERSEY-2157 reproducer.
     * <p>
     * The test ensures that entities of the error responses which cause
     * WebApplicationException being thrown by a JAX-RS client are buffered
     * and that the underlying input connections are automatically released
     * in such case.
     */
    @Test
    public void testConnectionClosingOnExceptionsForErrorResponses() {
        final BasicHttpClientConnectionManager cm = new BasicHttpClientConnectionManager();
        final AtomicInteger connectionCounter = new AtomicInteger(0);

        final ClientConfig config = new ClientConfig().property(Apache5ClientProperties.CONNECTION_MANAGER,
                new HttpClientConnectionManager() {
                    @Override
                    public LeaseRequest lease(String id, HttpRoute route, Timeout requestTimeout, Object state) {
                        connectionCounter.incrementAndGet();
                        return cm.lease(id, route, requestTimeout, state);
                    }

                    @Override
                    public void release(ConnectionEndpoint endpoint, Object newState, TimeValue validDuration) {
                        connectionCounter.decrementAndGet();
                        cm.release(endpoint, newState, validDuration);
                    }

                    @Override
                    public void connect(
                            ConnectionEndpoint endpoint,
                            TimeValue connectTimeout,
                            HttpContext context
                    ) throws IOException {
                        cm.connect(endpoint, connectTimeout, context);
                    }

                    @Override
                    public void upgrade(ConnectionEndpoint endpoint, HttpContext context) throws IOException {
                        cm.upgrade(endpoint, context);
                    }

                    @Override
                    public void close(CloseMode closeMode) {
                        cm.close(closeMode);
                    }

                    @Override
                    public void close() throws IOException {
                        cm.close();
                    }
                });
        config.connectorProvider(new Apache5ConnectorProvider());

        final Client client = ClientBuilder.newClient(config);
        final WebTarget rootTarget = client.target(getBaseUri()).path(ROOT_PATH);

        // Test that connection is getting closed properly for error responses.
        try {
            final String response = rootTarget.path("error").request().get(String.class);
            fail("Exception expected. Received: " + response);
        } catch (InternalServerErrorException isee) {
            // do nothing - connection should be closed properly by now
        }

        // Fail if the previous connection has not been closed automatically.
        assertEquals(0, connectionCounter.get());

        try {
            final String response = rootTarget.path("error2").request().get(String.class);
            fail("Exception expected. Received: " + response);
        } catch (InternalServerErrorException isee) {
            assertEquals("Error2.", isee.getResponse().readEntity(String.class), "Received unexpected data.");
            // Test buffering:
            // second read would fail if entity was not buffered
            assertEquals("Error2.", isee.getResponse().readEntity(String.class), "Unexpected data in the entity buffer.");
        }

        assertEquals(0, connectionCounter.get());
    }
}
