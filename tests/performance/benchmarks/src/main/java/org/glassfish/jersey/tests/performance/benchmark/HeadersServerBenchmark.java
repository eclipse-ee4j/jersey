/*
 * Copyright (c) 2022 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.performance.benchmark;

import org.glassfish.jersey.server.ApplicationHandler;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.ContainerResponse;
import org.glassfish.jersey.test.util.server.ContainerRequestBuilder;
import org.glassfish.jersey.tests.performance.benchmark.headers.HeadersApplication;
import org.glassfish.jersey.tests.performance.benchmark.headers.HeadersResource;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import javax.ws.rs.core.MediaType;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 5, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@Threads(4)
@State(Scope.Benchmark)
public class HeadersServerBenchmark {
    private static final AtomicInteger counter = new AtomicInteger();
    private static final MediaType MEDIA_PLAIN = MediaType.valueOf(HeadersResource.MEDIA_PLAIN);
    private static final MediaType MEDIA_JSON = MediaType.valueOf(HeadersResource.MEDIA_JSON);

    private volatile ApplicationHandler handler;

    @Setup
    public void start() throws Exception {
        handler = new ApplicationHandler(new HeadersApplication());
    }

    @TearDown
    public void shutdown() {
        if (counter.get() != 0) {
            System.out.append("Executed ").append(String.valueOf(counter.get())).append(" requests");
        }
    }

    @Benchmark
    public void testGetPlainText() throws ExecutionException, InterruptedException {
        ContainerRequest request = ContainerRequestBuilder
                .from("headers/getPlain", "GET", handler.getConfiguration())
                .accept(MEDIA_PLAIN)
                .build();

        ContainerResponse response = handler.apply(request).get();
        consume(response, HeadersResource.CONTENT_PLAIN, MEDIA_PLAIN);
    }

    @Benchmark
    public void testGetJson() throws ExecutionException, InterruptedException {
        ContainerRequest request = ContainerRequestBuilder
                .from("headers/getJson", "GET", handler.getConfiguration())
                .accept(MEDIA_JSON)
                .build();

        ContainerResponse response = handler.apply(request).get();
        consume(response, HeadersResource.CONTENT_PLAIN, MEDIA_JSON);
    }

    @Benchmark
    public void testPostPlainText() throws ExecutionException, InterruptedException {
        ContainerRequest request = ContainerRequestBuilder
                .from("headers/postPlain", "POST", handler.getConfiguration())
                .accept(MEDIA_PLAIN)
                .type(MEDIA_PLAIN)
                .entity(HeadersResource.CONTENT_PLAIN, handler)
                .build();

        ContainerResponse response = handler.apply(request).get();
        consume(response, HeadersResource.CONTENT_PLAIN, MEDIA_PLAIN);
    }

    @Benchmark
    public void testPostJson() throws ExecutionException, InterruptedException {
        ContainerRequest request = ContainerRequestBuilder
                .from("headers/postJson", "POST", handler.getConfiguration())
                .accept(MEDIA_JSON)
                .type(MEDIA_JSON)
                .entity(HeadersResource.CONTENT_PLAIN, handler)
                .build();

        ContainerResponse response = handler.apply(request).get();
        consume(response, HeadersResource.CONTENT_PLAIN, MEDIA_JSON);
    }

    @Benchmark
    public void testRandomClient() throws ExecutionException, InterruptedException {
        switch (counter.incrementAndGet() % 4) {
            case 0:
                testGetJson();
                break;
            case 1:
                testGetPlainText();
                break;
            case 2:
                testPostJson();
                break;
            case 3:
                testPostPlainText();
                break;
        }
    }

    private void consume(ContainerResponse response, String expectedContent, MediaType expectedMedia) {
        if (response.getStatus() != 200) {
            throw new IllegalStateException("Status:" + response.getStatus());
        }
        String content = response.getEntity().toString();
        if (!expectedContent.equals(content)) {
            throw new IllegalStateException("Content:" + content);
        }
        if (!expectedMedia.equals(response.getMediaType())) {
            throw new IllegalStateException("ContentType:" + response.getMediaType());
        }
    }

    public static void main(String[] args) throws RunnerException {
        final Options opt = new OptionsBuilder()
                // Register our benchmarks.
                .include(HeadersServerBenchmark.class.getSimpleName())
//                .addProfiler(org.openjdk.jmh.profile.JavaFlightRecorderProfiler.class)
                .build();

        new Runner(opt).run();

// DEBUG:

//        try {
//            HeadersServerBenchmark benchmark = new HeadersServerBenchmark();
//            benchmark.start();
//            for (int i = 0; i != 5; i++) {
//                benchmark.testPostPlainText();
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

}
