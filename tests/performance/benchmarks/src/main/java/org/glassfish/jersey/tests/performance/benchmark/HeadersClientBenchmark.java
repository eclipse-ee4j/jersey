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

import com.sun.net.httpserver.HttpServer;
import org.glassfish.jersey.CommonProperties;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.tests.performance.benchmark.headers.HeadersMBRW;
import org.glassfish.jersey.tests.performance.benchmark.headers.HeadersResource;
import org.glassfish.jersey.tests.performance.benchmark.headers.HeadersApplication;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 5, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@Threads(4)
@State(Scope.Benchmark)
public class HeadersClientBenchmark {

    static final String BASE_URI = "http://localhost:9009/headers";

    private static final AtomicInteger counter = new AtomicInteger();
    private static final MediaType MEDIA_PLAIN = MediaType.valueOf(HeadersResource.MEDIA_PLAIN);
    private static final MediaType MEDIA_JSON = MediaType.valueOf(HeadersResource.MEDIA_JSON);

    private static final boolean INCLUDE_INIT = false;

    private volatile WebTarget webTarget;

    @Setup
    public void setUp() {
        if (!INCLUDE_INIT) {
            webTarget = ClientBuilder.newClient(config()).target(BASE_URI);
        }
    }

    private WebTarget webTarget() {
        return INCLUDE_INIT ? ClientBuilder.newClient(config()).target(BASE_URI) : webTarget;
    }

    private static class JdkServer {
        private HttpServer server;
        void start() {
            server = JdkHttpServerFactory.createHttpServer(URI.create(BASE_URI), new HeadersApplication(), null, false);
            server.start();
            try {
                TimeUnit.SECONDS.sleep(1L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        void stop() {
            server.stop(1);
        }
    }

    private static class GrizzlyServer {
        private org.glassfish.grizzly.http.server.HttpServer httpServer;
        void start() {
            httpServer = GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), new HeadersApplication(), null, false);
            try {
                httpServer.start();
                TimeUnit.SECONDS.sleep(1L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        void stop() {
            httpServer.shutdownNow();
        }
    }

    @Benchmark
    public void testGetPlainTextClient() {
        WebTarget target = webTarget().path("headers/getPlain");
        try (Response r = target.request(MEDIA_PLAIN).get()) {
            consume(r, HeadersResource.CONTENT_PLAIN, MEDIA_PLAIN);
        }
    }

    @Benchmark
    public void testGetJsonClient() {
        WebTarget target = webTarget().path("headers/getJson");
        try (Response r = target.request(MEDIA_JSON).get()) {
            consume(r, HeadersResource.CONTENT_PLAIN, MEDIA_JSON);
        }
    }

    @Benchmark
    public void testPostPlainTextClient() {
        WebTarget target = webTarget().path("headers/postPlain");
        try (Response r = target.request(MEDIA_PLAIN).post(Entity.entity(HeadersResource.CONTENT_PLAIN, MEDIA_PLAIN))) {
            consume(r, HeadersResource.CONTENT_PLAIN, MEDIA_PLAIN);
        }
    }

    @Benchmark
    public void testPostJsonClient() {
        WebTarget target = webTarget().path("headers/postJson");
        try (Response r = target.request(MEDIA_JSON).post(Entity.entity(HeadersResource.CONTENT_PLAIN, MEDIA_JSON))) {
            consume(r, HeadersResource.CONTENT_PLAIN, MEDIA_JSON);
        }
    }

    @Benchmark
    public void testRandomClient() {
        switch (counter.incrementAndGet() % 4) {
            case 0:
                testGetJsonClient();
                break;
            case 1:
                testGetPlainTextClient();
                break;
            case 2:
                testPostJsonClient();
                break;
            case 3:
                testPostPlainTextClient();
                break;
        }
    }

    private ClientConfig config() {
        ClientConfig config = new ClientConfig();
        config.property(CommonProperties.PROVIDER_DEFAULT_DISABLE, "ALL");
        config.register(HeadersMBRW.class);
        return config;
    }

    private void consume(Response response, String expectedContent, MediaType expectedMedia) {
        if (response.getStatus() != 200) {
            throw new IllegalStateException("Status:" + response.getStatus());
        }
        String content = response.readEntity(String.class);
        if (!expectedContent.equals(content)) {
            throw new IllegalStateException("Content:" + content);
        }
        if (!expectedMedia.equals(response.getMediaType())) {
            throw new IllegalStateException("ContentType:" + response.getMediaType());
        }
    }

    public static void main(String[] args) throws RunnerException {
//        JdkServer server = new JdkServer();
        GrizzlyServer server = new GrizzlyServer();
        server.start();

        final Options opt = new OptionsBuilder()
                // Register our benchmarks.
                .include(HeadersClientBenchmark.class.getSimpleName())
//               .addProfiler(org.openjdk.jmh.profile.JavaFlightRecorderProfiler.class)
                .build();

        try {
            new Runner(opt).run();
            //new HeadersBenchmark().testGetJsonClient();
        } finally {
            server.stop();
        }

    }
}
