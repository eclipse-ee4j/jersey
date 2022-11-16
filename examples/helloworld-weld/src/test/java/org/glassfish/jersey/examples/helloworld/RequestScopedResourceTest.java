/*
 * Copyright (c) 2014, 2022 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.helloworld;

import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.jboss.weld.environment.se.Weld;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Test request scoped resource. Number of various requests will be made in parallel
 * against a single Grizzly instance. This is to ensure server side external request scope
 * binding does not mix different request data.
 *
 * @author Jakub Podlesak
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RequestScopedResourceTest extends JerseyTest {

    // Total number of requests to make
    static final int REQUEST_COUNT = 1000;
    // basis for test data sequence
    static final AtomicInteger dataFeed = new AtomicInteger();

    // to help us randomily select resource method to test
    static final Random RANDOMIZER = new Random();

    // our Weld container instance
    static Weld weld;

    /**
     * Take test data sequence from here
     *
     * @return iterable test input data
     */
    public static Stream<Arguments> data() {
        Iterable<Arguments> iterable = new Iterable<Arguments>() {
            @Override
            public Iterator<Arguments> iterator() {
                return new Iterator<Arguments>() {

                    @Override
                    public boolean hasNext() {
                        return dataFeed.get() < REQUEST_COUNT;
                    }

                    @Override
                    public Arguments next() {
                        int nextValue = dataFeed.getAndIncrement();
                        return Arguments.of(String.format("%02d", nextValue));
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };
        return StreamSupport.stream(iterable.spliterator(), false);
    }

    @BeforeAll
    public static void before() throws Exception {
        weld = new Weld();
        weld.initialize();
    }

    @AfterAll
    public static void after() throws Exception {
        weld.shutdown();
    }

    @Override
    @AfterEach
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @AfterAll
    public void report() {
        System.out.printf("SYNC: %d, ASYNC: %d, STRAIGHT: %d%n",
                parameterizedCounter.intValue(), parameterizedAsyncCounter.intValue(), straightCounter.intValue());
    }

    @Override
    protected ResourceConfig configure() {
        //        enable(TestProperties.LOG_TRAFFIC);
        return App.createJaxRsApp();
    }

    // we want to keep some statistics
    final AtomicInteger parameterizedCounter = new AtomicInteger(0);
    final AtomicInteger parameterizedAsyncCounter = new AtomicInteger(0);
    final AtomicInteger straightCounter = new AtomicInteger(0);

    @Execution(ExecutionMode.CONCURRENT)
    @ParameterizedTest
    @MethodSource("data")
    public void testRequestScopedResource(final String param) {

        String path;
        String expected = param;

        // select one of the three resource methods available
        switch (RANDOMIZER.nextInt(3)) {
            case 0:
                path = "req/parameterized";
                parameterizedCounter.incrementAndGet();
                break;
            case 1:
                path = "req/parameterized-async";
                parameterizedAsyncCounter.incrementAndGet();
                break;
            default:
                path = "req/straight";
                expected = String.format("straight: %s", param);
                straightCounter.incrementAndGet();
                break;
        }

        final Response response = target().path(path).queryParam("q", param).request("text/plain").get();

        assertNotNull(response, String.format("Request failed for %s", path));
        assertEquals(200, response.getStatus());
        assertEquals(expected, response.readEntity(String.class));
    }
}
