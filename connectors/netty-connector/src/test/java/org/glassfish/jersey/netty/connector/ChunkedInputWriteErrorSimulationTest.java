/*
 * Copyright (c) 2024, 2025 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.netty.connector;

import io.netty.channel.Channel;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.ClientRequest;
import org.glassfish.jersey.client.spi.Connector;
import org.glassfish.jersey.client.spi.ConnectorProvider;
import org.glassfish.jersey.netty.connector.internal.JerseyChunkedInput;
import org.glassfish.jersey.netty.connector.internal.NettyEntityWriter;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.Response;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ChunkedInputWriteErrorSimulationTest extends JerseyTest {
    private static final String EXCEPTION_MSG = "BOGUS BUFFER OVERFLOW";
    private static final AtomicReference<Throwable> caught = new AtomicReference<>(null);

    public static class ClientThread extends Thread {

        public static AtomicInteger count = new AtomicInteger();
        public static String url;
        public static int nLoops;

        private static Client client;

        public static void main(DequeOffer offer, String[] args) throws InterruptedException {
            url = args[0];
            int nThreads = Integer.parseInt(args[1]);
            nLoops = Integer.parseInt(args[2]);
            initClient(offer);
            Thread[] threads = new Thread[nThreads];
            for (int i = 0; i < nThreads; i++) {
                threads[i] = new ClientThread();
                threads[i].start();
            }

            for (int i = 0; i < nThreads; i++) {
                threads[i].join();
            }
            // System.out.println("Processed calls: " + count);
        }

        private static void initClient(DequeOffer offer) {
            ClientConfig defaultConfig = new ClientConfig();
            defaultConfig.property(ClientProperties.CONNECT_TIMEOUT, 10 * 1000);
            defaultConfig.property(ClientProperties.READ_TIMEOUT, 10 * 1000);
            defaultConfig.connectorProvider(getJerseyChunkedInputModifiedNettyConnector(offer));
            client = ClientBuilder.newBuilder()
                    .withConfig(defaultConfig)
                    .build();
        }

        public void doCall() {
            CompletableFuture<Response> cf = invokeResponse().toCompletableFuture()
                    .whenComplete((rsp, t) -> {
                        if (t != null) {
//                            System.out.println(Thread.currentThread() + " async complete. Caught exception " + t);
//                            t.printStackTrace();
                            while (t.getCause() != null) {
                                t = t.getCause();
                            }
                            caught.set(t);
                        }
                    })
                    .handle((rsp, t) -> {
                        if (rsp != null) {
                            rsp.readEntity(String.class);
                        } else {
                            System.out.println(Thread.currentThread().getName() + " response is null");
                        }
                        return rsp;
                    }).exceptionally(t -> {
                        System.out.println("async complete. completed exceptionally " + t);
                        throw new RuntimeException(t);
                    });

            try {
                cf.get();
                System.out.println("Done call " + count.incrementAndGet());
            } catch (InterruptedException | ExecutionException ex) {
                Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        private static CompletionStage<Response> invokeResponse() {
            WebTarget target = client.target(url);
            MultivaluedHashMap hdrs = new MultivaluedHashMap<>();
            StringBuilder sb = new StringBuilder("{");
            for (int i = 0; i < 10000; i++) {
                sb.append("\"fname\":\"foo\", \"lname\":\"bar\"");
            }
            sb.append("}");
            String jsonPayload = sb.toString();
            Invocation.Builder builder = ((WebTarget) target).request().headers(hdrs);
            return builder.rx().method("POST", Entity.entity(jsonPayload, MediaType.APPLICATION_JSON_TYPE));
        }

        @Override
        public void run() {
            for (int i = 0; i < nLoops; i++) {
                try {
                    doCall();
                } catch (Throwable t) {
                    throw new RuntimeException(t);
                }
            }
        }
    }

    @Path("/console")
    public static class HangingEndpoint {
        @Path("/login")
        @POST
        public String post(String entity) {
            return "Welcome";
        }
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(HangingEndpoint.class);
    }

    @Test
    public void testNoHangOnOfferInterrupt() throws InterruptedException {
        String path = getBaseUri() + "console/login";
        ClientThread.main(new InterruptedExceptionOffer(), new String[] {path, "5", "10"});
        Assertions.assertTrue(caught.get().getMessage().contains(EXCEPTION_MSG));
    }

    @Test
    public void testNoHangOnPollInterrupt() throws InterruptedException {
        String path = getBaseUri() + "console/login";
        ClientThread.main(new DequePoll(), new String[] {path, "5", "10"});
        Assertions.assertNotNull(caught.get());
    }

    @Test
    public void testNoHangOnOfferNoData() throws InterruptedException {
        String path = getBaseUri() + "console/login";
        ClientThread.main(new ReturnFalseOffer(), new String[] {path, "5", "10"});
        Assertions.assertTrue(caught.get().getMessage().contains("Buffer overflow")); //JerseyChunkedInput
        Thread.sleep(1_000L); // Sleep for the server to finish
    }

    private interface DequeOffer {
        public boolean offer(ByteBuffer e, long timeout, TimeUnit unit) throws InterruptedException;
    }

    private static class InterruptedExceptionOffer implements DequeOffer {
        private AtomicInteger ai = new AtomicInteger(0);

        @Override
        public boolean offer(ByteBuffer e, long timeout, TimeUnit unit) throws InterruptedException {
            if ((ai.getAndIncrement() % 10) == 0) {
                throw new InterruptedException(EXCEPTION_MSG);
            }
            return true;
        }
    }

    private static class ReturnFalseOffer implements DequeOffer {
        private AtomicInteger ai = new AtomicInteger(0);
        @Override
        public boolean offer(ByteBuffer e, long timeout, TimeUnit unit) throws InterruptedException {
            return !((ai.getAndIncrement() % 10) == 1);
        }
    }

    private static class DequePoll extends InterruptedExceptionOffer {
    }


    private static ConnectorProvider getJerseyChunkedInputModifiedNettyConnector(DequeOffer offer) {
        return new ConnectorProvider() {
            @Override
            public Connector getConnector(Client client, Configuration runtimeConfig) {
                return new NettyConnector(client, NettyConnectorProvider.config().rw()) {
                    @Override
                    NettyEntityWriter nettyEntityWriter(
                            ClientRequest clientRequest, Channel channel, NettyConnectorProvider.Config.RW config) {
                        NettyEntityWriter wrapped = NettyEntityWriter.getInstance(
                                clientRequest, channel, () -> config.requestEntityProcessing(clientRequest));

                        JerseyChunkedInput chunkedInput = (JerseyChunkedInput) wrapped.getChunkedInput();
                        try {
                            Field field = JerseyChunkedInput.class.getDeclaredField("queue");
                            field.setAccessible(true);

                            removeFinal(field);

                            field.set(chunkedInput, new LinkedBlockingDeque<ByteBuffer>() {
                                @Override
                                public boolean offer(ByteBuffer e, long timeout, TimeUnit unit) throws InterruptedException {
                                    if (!DequePoll.class.isInstance(offer) && !offer.offer(e, timeout, unit)) {
                                        return false;
                                    }
                                    return super.offer(e, timeout, unit);
                                }

                                @Override
                                public ByteBuffer poll(long timeout, TimeUnit unit) throws InterruptedException {
                                    if (DequePoll.class.isInstance(offer)) {
                                        offer.offer(null, timeout, unit);
                                    }
                                    return super.poll(timeout, unit);
                                }
                            });

                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }

                        NettyEntityWriter proxy = (NettyEntityWriter) Proxy.newProxyInstance(
                                ConnectorProvider.class.getClassLoader(), new Class[]{NettyEntityWriter.class},
                                (proxy1, method, args) -> {
                                    if (method.getName().equals("readChunk")) {
                                        try {
                                            return method.invoke(wrapped, args);
                                        } catch (RuntimeException e) {
                                            // consume
                                        }
                                    }
                                    return method.invoke(wrapped, args);
                                });
                        return proxy;
                    }
                };
            }
        };
    }

    public static void removeFinal(Field field) throws RuntimeException {
        try {
            Method[] classMethods = Class.class.getDeclaredMethods();
            Method declaredFieldMethod = Arrays
                    .stream(classMethods).filter(x -> Objects.equals(x.getName(), "getDeclaredFields0"))
                    .findAny().orElseThrow(() -> new NoSuchElementException("No value present"));
            declaredFieldMethod.setAccessible(true);
            Field[] declaredFieldsOfField = (Field[]) declaredFieldMethod.invoke(Field.class, false);
            Field modifiersField = Arrays
                    .stream(declaredFieldsOfField).filter(x -> Objects.equals(x.getName(), "modifiers"))
                    .findAny().orElseThrow(() -> new NoSuchElementException("No value present"));
            modifiersField.setAccessible(true);
            modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        } catch (RuntimeException re) {
            throw re;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
