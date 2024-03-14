/*
 * Copyright (c) 2024 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.e2e.client.connector;

import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.apache5.connector.Apache5ConnectorProvider;
import org.glassfish.jersey.client.AbstractRxInvoker;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.HttpUrlConnectorProvider;
import org.glassfish.jersey.client.JerseyInvocation;
import org.glassfish.jersey.client.RequestEntityProcessing;
import org.glassfish.jersey.client.spi.ConnectorProvider;
import org.glassfish.jersey.netty.connector.NettyConnectorProvider;
import org.glassfish.jersey.server.ChunkedOutput;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.CompletionStageRxInvoker;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.RxInvokerProvider;
import jakarta.ws.rs.client.SyncInvoker;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.function.Supplier;

public class FutureCancelTest extends JerseyTest {

    public static final long SLEEP = 100L;

    public static List<ConnectorProvider> testData() {
        return Arrays.asList(
                new ApacheConnectorProvider(),
                new Apache5ConnectorProvider(),
                new HttpUrlConnectorProvider(),
                new NettyConnectorProvider()
        );
    }

    @Path("/")
    public static class FutureCancelResource {
        @GET
        public ChunkedOutput<String> sendData() {
            ChunkedOutput<String> chunkedOutput = new ChunkedOutput<>(String.class);
            Thread newThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i != 100; i++) {
                        try {
                            chunkedOutput.write(String.valueOf(i));
                            Thread.sleep(SLEEP);
                        } catch (Exception e) {
                            // consume
                        }
                    }
                }
            });
            newThread.start();

            return chunkedOutput;
        }
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(FutureCancelResource.class);
    }

    @ParameterizedTest
    @MethodSource("testData")
    public void testFutureCancel(ConnectorProvider connectorProvider) throws InterruptedException, ExecutionException {
        ClientConfig config = new ClientConfig();
        config.connectorProvider(connectorProvider);

        Future<List<String>> future = ClientBuilder.newClient(config)
                .register(new FutureCancelRxInvokerProvider())
                .property(ClientProperties.REQUEST_ENTITY_PROCESSING, RequestEntityProcessing.CHUNKED)
                .target(target().getUri()).request().rx(FutureCancelRxInvoker.class).get().toCompletableFuture();

        int expectedSize = 2;

        while (RX_LIST.size() < expectedSize) {
            Thread.sleep(SLEEP);
        }
        future.cancel(true);

        Thread.sleep(2 * SLEEP); // wait to see no new messages arrive
        int size = RX_LIST.size(); // some might have beween RX_LIST.size() and cancel()
        while (size > expectedSize) { // be sure no more come
            Thread.sleep(SLEEP);
            expectedSize = size;
            size = RX_LIST.size();
        }

        Assertions.assertTrue(size < 10, "Received " + size + " messages");
    }

    private static List<String> RX_LIST = new LinkedList<>();

    public static class FutureCancelRxInvokerProvider implements RxInvokerProvider<FutureCancelRxInvoker> {

        Function<InputStream, Object> function = new Function<InputStream, Object>() {
            @Override
            public Object apply(InputStream inputStream) {
                byte[] number = new byte[8];
                int len = 0;
                do {
                    try {
                        if ((len = inputStream.read(number)) != -1) {
                            RX_LIST.add(new String(number).substring(0, len));
                        } else {
                            break;
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } while (true);
                return RX_LIST;
            }
        };

        @Override
        public boolean isProviderFor(Class<?> clazz) {
            return FutureCancelRxInvoker.class.equals(clazz);
        }

        @Override
        public FutureCancelRxInvoker getRxInvoker(SyncInvoker syncInvoker, ExecutorService executorService) {
            return new FutureCancelRxInvoker(syncInvoker, executorService, function);
        }
    }

    private static class FutureCancelRxInvoker extends AbstractRxInvoker<CompletionStage> implements CompletionStageRxInvoker {
        private final Function<InputStream, Object> consumer;

        public FutureCancelRxInvoker(SyncInvoker syncInvoker, ExecutorService executor, Function<InputStream, Object> consumer) {
            super(syncInvoker, executor);
            this.consumer = consumer;
        }

        @Override
        public <R> CompletionStage method(String name, Entity<?> entity, Class<R> responseType) {
            CompletableFuture<R> completableFuture = CompletableFuture.supplyAsync(new Supplier<R>() {
                @Override
                public R get() {
                    Response r = getSyncInvoker().get();
                    InputStream is = r.readEntity(InputStream.class);
                    Object o = consumer.apply(is);
                    return (R) o;
                }
            }, getExecutorService());
            ((JerseyInvocation.Builder) getSyncInvoker()).setCancellable(completableFuture);
            return completableFuture;
        }

        @Override
        public <R> CompletionStage method(String name, Entity<?> entity, GenericType<R> responseType) {
            CompletableFuture<R> completableFuture = CompletableFuture.supplyAsync(new Supplier<R>() {
                @Override
                public R get() {
                    Response r = getSyncInvoker().get();
                    InputStream is = r.readEntity(InputStream.class);
                    Object o = consumer.apply(is);
                    return (R) o;
                }
            }, getExecutorService());
            return completableFuture;
        }
    }
}
