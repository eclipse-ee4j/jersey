/*
 * Copyright (c) 2025 Oracle and/or its affiliates. All rights reserved.
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
import io.netty.handler.stream.ChunkedInput;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.ClientRequest;
import org.glassfish.jersey.client.spi.Connector;
import org.glassfish.jersey.client.spi.ConnectorProvider;
import org.glassfish.jersey.netty.connector.internal.NettyEntityWriter;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.MessageBodyWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Bug 5837 reproducer
 */
public class ChunkedInputClosedOnErrorTest extends JerseyTest {

    private static Client initClient(ConnectorProvider provider) {
        ClientConfig defaultConfig = new ClientConfig();
        defaultConfig.property(ClientProperties.CONNECT_TIMEOUT, 10 * 1000);
        defaultConfig.property(ClientProperties.READ_TIMEOUT, 10 * 1000);
        defaultConfig.connectorProvider(provider);
        Client client = ClientBuilder.newBuilder()
                .withConfig(defaultConfig)
                .build();
        return client;
    }

    @Override
    protected Application configure() {
        return new ResourceConfig();
    }

    @Test
    public void testChunkedInputNotStuckedTimes() throws InterruptedException {
        for (int i = 0; i != 10; i++) {
            boolean ret = testChunkedInputNotStucked();
            Assertions.assertTrue(ret, "JerseyChunkedInput was not closed on error");
        }
    }

    public boolean testChunkedInputNotStucked() throws InterruptedException {
        final AtomicReference<NettyEntityWriter> writer = new AtomicReference<>();
        final CountDownLatch writerSetLatch = new CountDownLatch(1);
        final CountDownLatch flushLatch = new CountDownLatch(1);
        ConnectorProvider provider = new ConnectorProvider() {
            @Override
            public Connector getConnector(Client client, Configuration runtimeConfig) {
                return new NettyConnector(client, NettyConnectorProvider.config().rw()) {
                    @Override
                    NettyEntityWriter nettyEntityWriter(ClientRequest clientRequest, Channel channel,
                                                        NettyConnectorProvider.Config.RW config) {
                        writer.set(super.nettyEntityWriter(clientRequest, channel, config));
                        writerSetLatch.countDown();
                        return new NettyEntityWriter() {
                            private boolean slept = false;

                            @Override
                            public void write(Object object) {
                                writer.get().write(object);
                            }

                            @Override
                            public void writeAndFlush(Object object) {
                                writer.get().writeAndFlush(object);
                            }

                            @Override
                            public void flush() throws IOException {
                                writer.get().flush();
                                flushLatch.countDown();
                            }

                            @Override
                            public ChunkedInput getChunkedInput() {
                                for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
                                    // caught from catch block in executorService.execute(new Runnable() {
                                    // "sleep" to simulate race condition
                                    if (element.getClassName().contains("NettyConnector")
                                            && element.getMethodName().equals("run")) {
                                        try {
                                            flushLatch.await();
                                        } catch (InterruptedException e) {
                                            throw new RuntimeException(e);
                                        }
                                    }
                                }
                                return writer.get().getChunkedInput();
                            }

                            @Override
                            public OutputStream getOutputStream() {
                                return writer.get().getOutputStream();
                            }

                            @Override
                            public long getLength() {
                                return writer.get().getLength();
                            }

                            @Override
                            public Type getType() {
                                return writer.get().getType();
                            }
                        };
                    }
                };
            }
        };

        Client client = initClient(provider);
        try {
            Response r = client
                    .register(new MultipartWriter())
                    .target(target().getUri()).request()
                    .post(Entity.entity(new MultipartWriter(), MediaType.MULTIPART_FORM_DATA_TYPE));
        } catch (ProcessingException expected) {

        }
        writerSetLatch.await();
        try {
            return writer.get().getChunkedInput().isEndOfInput();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static class MultipartWriter implements MessageBodyWriter<Object> {

        @Override
        public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return mediaType.equals(MediaType.MULTIPART_FORM_DATA_TYPE);
        }

        @Override
        public void writeTo(Object object, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
                            MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException,
                WebApplicationException {
            throw new IllegalArgumentException("TestException");
        }
    }

}
