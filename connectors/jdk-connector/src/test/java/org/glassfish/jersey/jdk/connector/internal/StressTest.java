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

package org.glassfish.jersey.jdk.connector.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.glassfish.jersey.SslConfigurator;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jdk.connector.JdkConnectorProvider;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.Test;

public class StressTest extends JerseyTest {

    private static final int SSL_PORT = 29999;
    private static final int ITERATIONS = 10000;
    // Must be less than the server thread pool. Is there any way to get that value?.
    private static final int N_REQUESTS = 10;
    private static final SSLContext SSL_CONTEXT;
    private static final ExecutorService executor = Executors.newFixedThreadPool(N_REQUESTS);
    private static CountDownLatch requests;
    private static CountDownLatch latch;

    static {
        System.setProperty("javax.net.ssl.keyStore", SslFilterTest.class.getResource("/keystore_server").getPath());
        System.setProperty("javax.net.ssl.keyStorePassword", "asdfgh");
        System.setProperty("javax.net.ssl.trustStore", SslFilterTest.class.getResource("/truststore_server").getPath());
        System.setProperty("javax.net.ssl.trustStorePassword", "asdfgh");
        try {
            SSL_CONTEXT = SslConfigurator.newInstance()
                    .trustStoreFile(SslFilterTest.class.getResource("/truststore_server").getPath())
                    .trustStorePassword("asdfgh")
                    .keyStoreFile(SslFilterTest.class.getResource("/keystore_server").getPath())
                    .keyStorePassword("asdfgh").createSSLContext();
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }

    @Path("/test")
    public static class TestResource {
        @GET
        @Path("/1")
        public String test1() throws InterruptedException {
            requests.countDown();
            if (latch.await(100, TimeUnit.SECONDS)) {
                return "test1";
            } else {
                throw new IllegalStateException("Timeout");
            }
        }
        @GET
        @Path("/2")
        public String test2() {
            return "test2";
        }
    }

    @Override
    protected Application configure() {
        enable(TestProperties.LOG_TRAFFIC);
        enable(TestProperties.DUMP_ENTITY);
        return new ResourceConfig(TestResource.class);
    }

    @Override
    protected void configureClient(ClientConfig config) {
        config.connectorProvider(new JdkConnectorProvider());
    }

    @Override
    protected Optional<SSLContext> getSslContext() {
        return Optional.of(SSL_CONTEXT);
    }

    @Override
    protected Optional<SSLParameters> getSslParameters() {
        return Optional.of(SSL_CONTEXT.createSSLEngine("localhost", SSL_PORT).getSSLParameters());
    }

    @Override
    protected URI getBaseUri() {
        return UriBuilder.fromUri("https://localhost/").port(SSL_PORT).build();
    }

    @Test
    public void hangAllRequestsStatus200() throws InterruptedException, ExecutionException {
        assertEquals("https", getBaseUri().getScheme());
        for (int i = 0; i < ITERATIONS; i++) {
            requests = new CountDownLatch(N_REQUESTS);
            latch = new CountDownLatch(1);
            List<Future<Response>> responses = new ArrayList<>();
            for (int j = 0; j < N_REQUESTS; j++) {
                Future<Response> future = executor.submit(() -> target("/test/1").request().get());
                responses.add(future);
            }
            assertTrue(requests.await(100, TimeUnit.SECONDS));
            latch.countDown();
            for (Future<Response> response : responses) {
                assertEquals(200, response.get().getStatus());
            }
        }
    }

    @Test
    public void randomnessStatus200() throws InterruptedException, ExecutionException {
        assertEquals("https", getBaseUri().getScheme());
        for (int i = 0; i < ITERATIONS; i++) {
            List<Future<Response>> responses = new ArrayList<>();
            for (int j = 0; j < N_REQUESTS; j++) {
                Future<Response> future = executor.submit(() -> target("/test/2").request().get());
                responses.add(future);
            }
            for (Future<Response> response : responses) {
                assertEquals(200, response.get().getStatus());
            }
        }
    }

}
