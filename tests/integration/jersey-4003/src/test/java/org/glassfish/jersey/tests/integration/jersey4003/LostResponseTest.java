/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.integration.jersey4003;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.HttpUrlConnectorProvider;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.glassfish.jersey.client.JerseyCompletionStageRxInvoker;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.InvocationCallback;
import javax.ws.rs.client.ResponseProcessingException;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class LostResponseTest {

    private static final String DUMMY_URL = "http://foo";
    private static final int RESPONSE_CODE = 503;

    private Client client;
    private Entity<?> bodyEntity;

    @Before
    public void setup() throws IOException {
        HttpUrlConnectorProvider.ConnectionFactory connectionFactory =
                Mockito.mock(HttpUrlConnectorProvider.ConnectionFactory.class);
        HttpURLConnection connection = Mockito.mock(HttpURLConnection.class);
        Mockito.when(connectionFactory.getConnection(Mockito.any(URL.class))).thenReturn(connection);

        OutputStream outputStream = Mockito.mock(OutputStream.class);
        Mockito.when(connection.getOutputStream()).thenReturn(outputStream);

        Mockito.when(connection.getURL()).thenReturn(new URL(DUMMY_URL));
        Mockito.when(connection.getResponseCode()).thenReturn(RESPONSE_CODE);

        // When the below line is commented, the test succeeds.
        Mockito.doThrow(new IOException("Injected Write Failure"))
                .when(outputStream)
                .write(Mockito.any(byte[].class), Mockito.anyInt(), Mockito.anyInt());

        ClientConfig clientConfig = new ClientConfig();
        clientConfig.connectorProvider(
                new HttpUrlConnectorProvider().connectionFactory(connectionFactory));
        client = JerseyClientBuilder.newBuilder().withConfig(clientConfig).build();

        ByteArrayInputStream bodyStream = new ByteArrayInputStream(new byte[100]);
        bodyEntity = Entity.entity(bodyStream, MediaType.APPLICATION_OCTET_STREAM_TYPE);
    }

    @Test
    public void putEntityFailure() {
        try {
            client.target(DUMMY_URL).request().put(bodyEntity);
            Assert.fail("Expected ResponseProcessing exception has not been thrown");
        } catch (ResponseProcessingException rpe) {
            try (Response response = rpe.getResponse()) {
                Assert.assertEquals(RESPONSE_CODE, response.getStatus());
            }
        }
    }

    @Test
    public void putEntityAndClassTypeFailure() {
        try {
            client.target(DUMMY_URL).request().put(bodyEntity, String.class);
            Assert.fail("Expected ResponseProcessing exception has not been thrown");
        } catch (ResponseProcessingException rpe) {
            try (Response response = rpe.getResponse()) {
                Assert.assertEquals(RESPONSE_CODE, response.getStatus());
            }
        }
    }

    @Test
    public void putEntityAndGenericTypeTypeFailure() {
        try {
            client.target(DUMMY_URL).request().put(bodyEntity, new GenericType<String>(){});
            Assert.fail("Expected ResponseProcessing exception has not been thrown");
        } catch (ResponseProcessingException rpe) {
            try (Response response = rpe.getResponse()) {
                Assert.assertEquals(RESPONSE_CODE, response.getStatus());
            }
        }
    }

    @Test
    public void asyncPutEntityFailure() throws InterruptedException {
        try {
            Future<Response> future = client.target(DUMMY_URL).request().async().put(bodyEntity);
            future.get();
            Assert.fail("Expected ResponseProcessing exception has not been thrown");
        } catch (ExecutionException ee) {
            try {
                throw (RuntimeException) ee.getCause();
            } catch (ResponseProcessingException rpe) {
                try (Response response = rpe.getResponse()) {
                    Assert.assertEquals(RESPONSE_CODE, response.getStatus());
                }
            }
        }
    }

    @Test
    public void asyncPutEntityAndClassFailure() throws InterruptedException {
        try {
            Future<String> future = client.target(DUMMY_URL).request().async().put(bodyEntity, String.class);
            future.get();
            Assert.fail("Expected ResponseProcessing exception has not been thrown");
        } catch (ExecutionException ee) {
            try {
                throw (RuntimeException) ee.getCause();
            } catch (ResponseProcessingException rpe) {
                try (Response response = rpe.getResponse()) {
                    Assert.assertEquals(RESPONSE_CODE, response.getStatus());
                }
            }
        }
    }

    @Test
    public void asyncPutEntityAndGenericTypeTypeFailure() throws InterruptedException {
        try {
            Future<String> future = client.target(DUMMY_URL).request().async().put(bodyEntity, new GenericType<String>(){});
            future.get();
            Assert.fail("Expected ResponseProcessing exception has not been thrown");
        } catch (ExecutionException ee) {
            try {
                throw (RuntimeException) ee.getCause();
            } catch (ResponseProcessingException rpe) {
                try (Response response = rpe.getResponse()) {
                    Assert.assertEquals(RESPONSE_CODE, response.getStatus());
                }
            }
        }
    }

    @Test
    public void asyncPutEntityWithCallbackFailure() throws InterruptedException {
        AtomicReference<Throwable> callbackThrowable = new AtomicReference<>();
        CountDownLatch failedLatch = new CountDownLatch(1);
        try {
            Future<Response> future =
                    client.target(DUMMY_URL).request().async().put(bodyEntity, new InvocationCallback<Response>() {
                @Override
                public void completed(Response response) {
                    Assert.fail("Expected ResponseProcessing exception has not been thrown");
                }

                @Override
                public void failed(Throwable throwable) {
                    callbackThrowable.set(throwable);
                    failedLatch.countDown();
                }
            });
            future.get();
            Assert.fail("Expected ResponseProcessing exception has not been thrown");
        } catch (ExecutionException ee) {
            try {
                throw (RuntimeException) ee.getCause();
            } catch (ResponseProcessingException rpe) {
                try (Response response = rpe.getResponse()) {
                    Assert.assertEquals(RESPONSE_CODE, response.getStatus());
                }
            }
            failedLatch.await(5000, TimeUnit.MILLISECONDS);
            Throwable ct = callbackThrowable.get();
            Assert.assertTrue("Callback has not been hit", ct != null);
            Assert.assertTrue("The exception is " + ct.getClass().getName(),
                    ResponseProcessingException.class.isInstance(ct));
        }
    }

    @Test
    public void rxPutEntityFailure() throws InterruptedException {
        try {
            CompletionStage<Response> future = client.target(DUMMY_URL).request().rx().put(bodyEntity);
            future.toCompletableFuture().get();
            Assert.fail("Expected ResponseProcessing exception has not been thrown");
        } catch (ExecutionException ee) {
            try {
                throw (RuntimeException) ee.getCause();
            } catch (ResponseProcessingException rpe) {
                try (Response response = rpe.getResponse()) {
                    Assert.assertEquals(RESPONSE_CODE, response.getStatus());
                }
            }
        }
    }

    @Test
    public void rxPutEntityWithCallbackFailure() throws InterruptedException {
        AtomicReference<Throwable> callbackThrowable = new AtomicReference<>();
        CountDownLatch failedLatch = new CountDownLatch(1);
        try {
            Future<Response> future =
                    client.target(DUMMY_URL).request().rx(JerseyCompletionStageRxInvoker.class)
                            .put(bodyEntity, new InvocationCallback<Response>() {
                        @Override
                        public void completed(Response response) {
                            Assert.fail("Expected ResponseProcessing exception has not been thrown");
                        }

                        @Override
                        public void failed(Throwable throwable) {
                            callbackThrowable.set(throwable);
                            failedLatch.countDown();
                        }
                    });
            future.get();
            Assert.fail("Expected ResponseProcessing exception has not been thrown");
        } catch (ExecutionException ee) {
            try {
                throw (RuntimeException) ee.getCause();
            } catch (ResponseProcessingException rpe) {
                try (Response response = rpe.getResponse()) {
                    Assert.assertEquals(RESPONSE_CODE, response.getStatus());
                }
            }
            failedLatch.await(5000, TimeUnit.MILLISECONDS);
            Throwable ct = callbackThrowable.get();
            Assert.assertTrue("Callback has not been hit", ct != null);
            Assert.assertTrue("The exception is " + ct.getClass().getName(),
                    ResponseProcessingException.class.isInstance(ct));
        }
    }
}
