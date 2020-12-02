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

package org.glassfish.jersey.media.sse.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.inject.Singleton;
import javax.json.Json;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.sse.SseEventSource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import io.reactivex.Flowable;
import org.glassfish.jersey.internal.jsr166.Flow;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;

/**
 * @author Daniel Kec
 */
public class SseSubscriberTest extends JerseyTest {

    private static final int NUMBER_OF_TEST_MESSAGES = 5;
    private static final String TEST_MESSAGE = "Jersey";
    private static final JsonBuilderFactory JSON_BUILDER = Json.createBuilderFactory(Collections.emptyMap());

    @Override
    protected Application configure() {
        return new ResourceConfig(SseEndpoint.class);
    }

    @Singleton
    @Path("sse")
    public static class SseEndpoint {

        @GET
        @Path("short")
        @Produces(MediaType.SERVER_SENT_EVENTS)
        public void sseShort(@Context Flow.Subscriber<Short> subscriber) {
            Flowable.interval(20, TimeUnit.MILLISECONDS)
                    .take(NUMBER_OF_TEST_MESSAGES)
                    .map(Long::shortValue)
                    .subscribe(JerseyFlowAdapters.toSubscriber(subscriber));
        }

        @GET
        @Path("double")
        @Produces(MediaType.SERVER_SENT_EVENTS)
        public void sseDouble(@Context Flow.Subscriber<Double> subscriber) {
            Flowable.interval(20, TimeUnit.MILLISECONDS)
                    .take(NUMBER_OF_TEST_MESSAGES)
                    .map(Long::doubleValue)
                    .subscribe(JerseyFlowAdapters.toSubscriber(subscriber));
        }

        @GET
        @Path("byte")
        @Produces(MediaType.SERVER_SENT_EVENTS)
        public void sseByte(@Context Flow.Subscriber<Byte> subscriber) {
            Flowable.interval(20, TimeUnit.MILLISECONDS)
                    .take(NUMBER_OF_TEST_MESSAGES)
                    .map(Long::byteValue)
                    .subscribe(JerseyFlowAdapters.toSubscriber(subscriber));
        }

        @GET
        @Path("integer")
        @Produces(MediaType.SERVER_SENT_EVENTS)
        public void sseInteger(@Context Flow.Subscriber<Integer> subscriber) {
            Flowable.interval(20, TimeUnit.MILLISECONDS)
                    .take(NUMBER_OF_TEST_MESSAGES)
                    .map(Long::intValue)
                    .subscribe(JerseyFlowAdapters.toSubscriber(subscriber));
        }

        @GET
        @Path("long")
        @Produces(MediaType.SERVER_SENT_EVENTS)
        public void sseLong(@Context Flow.Subscriber<Long> subscriber) {
            Flowable.just(0L, 1L, 2L, 3L, 4L)
                    .take(NUMBER_OF_TEST_MESSAGES)
                    .subscribe(JerseyFlowAdapters.toSubscriber(subscriber));
        }

        @GET
        @Path("string")
        @Produces(MediaType.SERVER_SENT_EVENTS)
        public void sseString(@Context Flow.Subscriber<String> subscriber) {
            Flowable.interval(20, TimeUnit.MILLISECONDS)
                    .take(NUMBER_OF_TEST_MESSAGES)
                    .map(l -> TEST_MESSAGE + l)
                    .subscribe(JerseyFlowAdapters.toSubscriber(subscriber));
        }

        @GET
        @Path("boolean")
        @Produces(MediaType.SERVER_SENT_EVENTS)
        public void sseBoolean(@Context Flow.Subscriber<Boolean> subscriber) {
            Flowable.interval(20, TimeUnit.MILLISECONDS)
                    .take(NUMBER_OF_TEST_MESSAGES)
                    .map(l -> (l % 2) == 0)
                    .subscribe(JerseyFlowAdapters.toSubscriber(subscriber));
        }

        @GET
        @Path("char")
        @Produces(MediaType.SERVER_SENT_EVENTS)
        public void sseChar(@Context Flow.Subscriber<Character> subscriber) {
            Flowable.just("FRANK")
                    .flatMap(s -> Flowable.fromArray(s.chars().mapToObj(ch -> (char) ch).toArray(Character[]::new)))
                    .subscribe(JerseyFlowAdapters.toSubscriber(subscriber));
        }

        @GET
        @Path("json-obj")
        @Produces(MediaType.SERVER_SENT_EVENTS)
        public void sseJsonObj(@Context Flow.Subscriber<JsonObject> subscriber) {
            Flowable.interval(20, TimeUnit.MILLISECONDS)
                    .take(NUMBER_OF_TEST_MESSAGES)
                    .map(l -> JSON_BUILDER.createObjectBuilder()
                            .add("brand", TEST_MESSAGE)
                            .add("model", "Model " + l)
                            .build())
                    .subscribe(JerseyFlowAdapters.toSubscriber(subscriber));
        }

        @GET
        @Path("json")
        @Produces(MediaType.SERVER_SENT_EVENTS)
        public void sseJson(@Context Flow.Subscriber<Car> subscriber) {
            Flowable.interval(20, TimeUnit.MILLISECONDS)
                    .take(NUMBER_OF_TEST_MESSAGES)
                    .map(l -> new Car(TEST_MESSAGE, "Model " + l))
                    .subscribe(JerseyFlowAdapters.toSubscriber(subscriber));
        }
    }


    @Test
    public void testShort() throws InterruptedException {
        assertEquals(Arrays.asList((short) 0, (short) 1, (short) 2, (short) 3, (short) 4), receive(Short.class, "sse/short"));
    }

    @Test
    public void testDouble() throws InterruptedException {
        assertEquals(Arrays.asList(0.0, 1.0, 2.0, 3.0, 4.0), receive(Double.class, "sse/double"));
    }

    @Test
    public void testByte() throws InterruptedException {
        assertEquals(Arrays.asList((byte) 0, (byte) 1, (byte) 2, (byte) 3, (byte) 4), receive(Byte.class, "sse/byte"));
    }

    @Test
    public void testInteger() throws InterruptedException {
        assertEquals(Arrays.asList(0, 1, 2, 3, 4), receive(Integer.class, "sse/integer"));
    }

    @Test
    public void testBoolean() throws InterruptedException {
        assertEquals(Arrays.asList(true, false, true, false, true), receive(Boolean.class, "sse/boolean"));
    }

    @Test
    public void testLong() throws InterruptedException {
        assertEquals(Arrays.asList(0L, 1L, 2L, 3L, 4L), receive(Long.class, "sse/long"));
    }

    @Test
    public void testString() throws InterruptedException {
        assertEquals(Arrays.asList(TEST_MESSAGE + 0, TEST_MESSAGE + 1, TEST_MESSAGE + 2, TEST_MESSAGE + 3, TEST_MESSAGE + 4),
                receive(String.class, "sse/string"));
    }

    @Test
    public void testChar() throws InterruptedException {
        assertEquals(Arrays.asList('F', 'R', 'A', 'N', 'K'),
                receive(Character.class, "sse/char"));
    }

    @Test
    public void testJsonObj() throws InterruptedException {
        Jsonb jsonb = JsonbBuilder.create();
        assertEquals(Arrays.asList(
                new Car(TEST_MESSAGE, "Model 0"),
                new Car(TEST_MESSAGE, "Model 1"),
                new Car(TEST_MESSAGE, "Model 2"),
                new Car(TEST_MESSAGE, "Model 3"),
                new Car(TEST_MESSAGE, "Model 4")
                ),
                receive(String.class, "sse/json-obj")
                        .stream()
                        .map(s -> jsonb.fromJson(s, Car.class))
                        .collect(Collectors.toList()));
    }

    @Test
    public void testJson() throws InterruptedException {
        Jsonb jsonb = JsonbBuilder.create();
        assertEquals(Arrays.asList(
                new Car(TEST_MESSAGE, "Model 0"),
                new Car(TEST_MESSAGE, "Model 1"),
                new Car(TEST_MESSAGE, "Model 2"),
                new Car(TEST_MESSAGE, "Model 3"),
                new Car(TEST_MESSAGE, "Model 4")
                ),
                receive(String.class, "sse/json")
                        .stream()
                        .map(s -> jsonb.fromJson(s, Car.class))
                        .collect(Collectors.toList()));
    }

    private <T> List<T> receive(Class<T> type, String path) throws InterruptedException {
        WebTarget sseTarget = target(path);

        ArrayList<T> result = new ArrayList<>(NUMBER_OF_TEST_MESSAGES);

        final CountDownLatch eventLatch = new CountDownLatch(NUMBER_OF_TEST_MESSAGES);
        SseEventSource eventSource = SseEventSource.target(sseTarget).build();
        eventSource.register((event) -> {
            System.out.println("### Client received: " + event);
            result.add(event.readData(type));
            eventLatch.countDown();
        });
        eventSource.open();

        // client waiting for confirmation that resource method ended.
        assertTrue(eventLatch.await(2, TimeUnit.SECONDS));
        return result;
    }

    public static class Car {
        private String brand;
        private String model;

        public Car() {
        }

        public Car(final String brand, final String model) {
            this.brand = brand;
            this.model = model;
        }

        public String getBrand() {
            return brand;
        }

        public void setBrand(final String brand) {
            this.brand = brand;
        }

        public String getModel() {
            return model;
        }

        public void setModel(final String model) {
            this.model = model;
        }

        @Override
        public String toString() {
            return "Car{brand='" + brand + "', model='" + model + "'}";
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Car car = (Car) o;
            return Objects.equals(brand, car.brand)
                    && Objects.equals(model, car.model);
        }

        @Override
        public int hashCode() {
            return Objects.hash(brand, model);
        }
    }
}
