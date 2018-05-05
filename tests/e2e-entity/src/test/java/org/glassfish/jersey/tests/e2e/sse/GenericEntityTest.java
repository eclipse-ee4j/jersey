/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.e2e.sse;

import java.util.Objects;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.sse.Sse;
import javax.ws.rs.sse.SseEventSink;
import javax.ws.rs.sse.SseEventSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

public class GenericEntityTest extends JerseyTest {
    private static final int BUFFER_SIZE = 20;

    @Override
    protected Application configure() {
        return new ResourceConfig(GenericEntityTest.SSEGenericEntityResource.class, ListPairMBRW.class, PairMBRW.class);
    }

    @Test
    public void testGenericString() throws InterruptedException {
        WebTarget sseTarget = target("genericentityresource/string");
        CountDownLatch countDownLatch = new CountDownLatch(1);
        MessageLatch<String> messageLatch = new MessageLatch<>(countDownLatch);
        try (SseEventSource source = SseEventSource.target(sseTarget).build()) {
            source.register(event -> messageLatch.consume(event.readData()));
            source.open();
            assertTrue(countDownLatch.await(5, TimeUnit.SECONDS));
            assertEquals("Cindy", messageLatch.data().get(0));
        }
    }

    @Test
    public void testGenericPair() throws InterruptedException {
        WebTarget sseTarget = target("genericentityresource/pair").register(PairMBRW.class);
        CountDownLatch countDownLatch = new CountDownLatch(1);
        MessageLatch<Pair<String, Integer>> messageLatch = new MessageLatch<>(countDownLatch);
        try (SseEventSource source = SseEventSource.target(sseTarget).build()) {
            source.register(event -> messageLatch.consume(event.readData(Pair.class)));
            source.open();
            assertTrue(countDownLatch.await(5, TimeUnit.SECONDS));
            Pair<String, Integer> pair = messageLatch.data().get(0);
            assertEquals("Cindy", pair.getKey());
            assertEquals(30, pair.getValue().intValue());
        }
    }

    @Test
    public void testGenericList() throws InterruptedException {
        WebTarget sseTarget = target("genericentityresource/list").register(ListPairMBRW.class);
        CountDownLatch countDownLatch = new CountDownLatch(2);
        MessageLatch<Pair<String, Integer>> messageLatch = new MessageLatch<>(countDownLatch);
        try (SseEventSource source = SseEventSource.target(sseTarget).build()) {
            source.register(event -> messageLatch.consume((List<Pair<String, Integer>>) event.readData(List.class)));
            source.open();
            assertTrue(countDownLatch.await(5, TimeUnit.SECONDS));
            Pair<String, Integer> cindy = messageLatch.data().get(0);
            Pair<String, Integer> jack = messageLatch.data().get(1);
            assertEquals("Cindy", cindy.getKey());
            assertEquals(30, cindy.getValue().intValue());
            assertEquals("Jack", jack.getKey());
            assertEquals(32, jack.getValue().intValue());
        }
    }


    @Test(expected = IllegalArgumentException.class)
    public void testRegisterNull() {
        final SseEventSource source = SseEventSource.target(target("genericentityresource/string")).build();
        source.register(null);
    }

    @Singleton
    @Path("genericentityresource")
    public static class SSEGenericEntityResource {
        @GET
        @Path("string")
        @Produces(MediaType.SERVER_SENT_EVENTS)
        public void sendString(@Context SseEventSink sink, @Context Sse sse) {
            GenericEntity<String> ges = new GenericEntity<String>("Cindy") {
            };
            try (SseEventSink s = sink) {
                s.send(sse.newEventBuilder().data(ges).build());
            }
        }

        @GET
        @Path("pair")
        @Produces(MediaType.SERVER_SENT_EVENTS)
        public void sendPair(@Context SseEventSink sink, @Context Sse sse) {
            Pair<String, Integer> person = new Pair<>("Cindy", 30);
            GenericEntity<Pair<String, Integer>> entity = new GenericEntity<Pair<String, Integer>>(person) {
            };
            try (SseEventSink s = sink) {
                s.send(sse.newEventBuilder().data(entity).build());
            }
        }

        @GET
        @Path("list")
        @Produces(MediaType.SERVER_SENT_EVENTS)
        public void sendList(@Context SseEventSink sink, @Context Sse sse) {
            Pair<String, Integer> person1 = new Pair<>("Cindy", 30);
            Pair<String, Integer> person2 = new Pair<>("Jack", 32);
            List<Pair<String, Integer>> people = new LinkedList<>();
            people.add(person1);
            people.add(person2);
            GenericEntity<List<Pair<String, Integer>>> entity = new GenericEntity<List<Pair<String, Integer>>>(people) {
            };
            try (SseEventSink s = sink) {
                s.send(sse.newEventBuilder().data(entity).build());
            }
        }
    }

    private static class PairMBRW implements
            MessageBodyWriter<Pair<String, Integer>>, MessageBodyReader<Pair<String, Integer>> {

        @Override
        public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return type == Pair.class;
        }

        @Override
        public Pair<String, Integer> readFrom(Class<Pair<String, Integer>> type, Type genericType,
                                              Annotation[] annotations, MediaType mediaType,
                                              MultivaluedMap<String, String> httpHeaders,
                                              InputStream entityStream) throws IOException, WebApplicationException {
            byte[] buffer = new byte[GenericEntityTest.BUFFER_SIZE];
            entityStream.read(buffer);
            return readFrom(new String(buffer, Charset.defaultCharset()).trim());
        }

        static Pair<String, Integer> readFrom(String from) {
            String[] split = from.split(",", 2);
            return new Pair<String, Integer>(split[0], Integer.parseInt(split[1]));
        }

        @Override
        public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return type == Pair.class;
        }

        @Override
        public void writeTo(Pair<String, Integer> stringIntegerPair, Class<?> type, Type genericType,
                            Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders,
                            OutputStream entityStream) throws IOException, WebApplicationException {
            writeTo(stringIntegerPair, entityStream);
        }

        static void writeTo(Pair<String, Integer> stringIntegerPair, OutputStream entityStream) throws IOException {
            StringBuilder sb = new StringBuilder();
            sb.append(stringIntegerPair.getKey()).append(",").append(stringIntegerPair.getValue());
            entityStream.write(sb.toString().getBytes(Charset.defaultCharset()));
        }
    }

    private static class ListPairMBRW implements MessageBodyWriter<List<Pair<String, Integer>>>,
            MessageBodyReader<List<Pair<String, Integer>>> {

        @Override
        public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return type == List.class;
        }

        @Override
        public List<Pair<String, Integer>> readFrom(Class<List<Pair<String, Integer>>> type, Type genericType,
                                                    Annotation[] annotations, MediaType mediaType,
                                                    MultivaluedMap<String, String> httpHeaders,
                                                    InputStream entityStream)
                throws IOException, WebApplicationException {
            List<Pair<String, Integer>> list = new LinkedList<>();
            byte[] buffer = new byte[20];
            entityStream.read(buffer);
            StringTokenizer st = new StringTokenizer(new String(buffer, Charset.defaultCharset()).trim(), ";", false);
            while (st.hasMoreTokens()) {
                list.add(PairMBRW.readFrom(st.nextToken()));
            }
            return list;
        }

        @Override
        public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return type == List.class;
        }

        @Override
        public void writeTo(List<Pair<String, Integer>> pairs, Class<?> type, Type genericType,
                            Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders,
                            OutputStream entityStream) throws IOException, WebApplicationException {
            for (Pair<String, Integer> pair : pairs) {
                PairMBRW.writeTo(pair, entityStream);
                entityStream.write(";".getBytes());
            }
        }
    }

    private static class MessageLatch<T> {
        private CountDownLatch countDownLatch;
        private List<T> data = new LinkedList<>();

        private MessageLatch(CountDownLatch countDownLatch) {
            this.countDownLatch = countDownLatch;
        }

        private void consume(List<T> list) {
            for (T o : list) {
                data.add(o);
                countDownLatch.countDown();
            }
        }

        private void consume(T o) {
            data.add(o);
            countDownLatch.countDown();
        }

        private List<T> data() {
            return data;
        }
    }

    private static class Pair<K, V> {

        private final K key;
        private final V value;

        public Pair(K key, V value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Pair<?, ?> pair = (Pair<?, ?>) o;
            return Objects.equals(key, pair.key) && Objects.equals(value, pair.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(key, value);
        }

        @Override
        public String toString() {
            return "Pair{key=" + key + ", value=" + value + '}';
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }

    }

}
