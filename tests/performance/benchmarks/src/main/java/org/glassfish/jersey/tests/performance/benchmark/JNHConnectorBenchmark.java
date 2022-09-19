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

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.nio.transport.TCPNIOTransport;
import org.glassfish.grizzly.threadpool.ThreadPoolConfig;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.jnh.connector.JavaNetHttpConnectorProvider;
import org.glassfish.jersey.tests.performance.benchmark.entity.json.JacksonApplication;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.net.URI;
import java.util.concurrent.TimeUnit;

/**
 * jersey-jnh-connector {@link org.glassfish.jersey.jnh.connector.JavaNetHttpConnector} benchmark.
 *
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 1, time = 1)
@Measurement(iterations = 160, time = 1)
@Fork(8)
@State(Scope.Benchmark)
public class JNHConnectorBenchmark {

    private static final URI BASE_URI = URI.create("http://localhost:8080/");

    private static final String REQUEST_TARGET = "http://localhost:8080/projects/detailed";
    private volatile HttpServer server;

    private volatile Client client;
    private volatile Client defaultClient;

    @Setup
    public void start() throws Exception {
        server =
                GrizzlyHttpServerFactory.createHttpServer(BASE_URI, new JacksonApplication(Boolean.FALSE), false);
        final TCPNIOTransport transport = server.getListener("grizzly").getTransport();
        transport.setSelectorRunnersCount(4);
        transport.setWorkerThreadPoolConfig(ThreadPoolConfig.defaultConfig().setCorePoolSize(8).setMaxPoolSize(8));

        server.start();
        client = ClientBuilder.newClient(
                new ClientConfig()
                        .connectorProvider(new JavaNetHttpConnectorProvider())
                        .register(JacksonFeature.class)
        );
        defaultClient = ClientBuilder.newClient(
                new ClientConfig()
                        .register(JacksonFeature.class)
        );

    }

    @Setup(Level.Iteration)
    public void request() {
    }

    @TearDown
    public void shutdown() {
        server.shutdownNow();
    }

    @Benchmark
    public void measureEmptyGetResource() {
        client.target(REQUEST_TARGET).request(MediaType.APPLICATION_XML_TYPE).get();
    }

    @Benchmark
    public void measureEntityGetResource() {
        client.target(REQUEST_TARGET).request(MediaType.APPLICATION_JSON_TYPE).get();
    }
    @Benchmark
    public void measureEntityGetDefaultResource() {
        defaultClient.target(REQUEST_TARGET).request(MediaType.APPLICATION_JSON_TYPE).get();
    }

    @Benchmark
    public void measureEmptyGetDefaultResource() {
        defaultClient.target(REQUEST_TARGET).request(MediaType.APPLICATION_XML_TYPE).get();
    }

    public static void main(final String[] args) throws Exception {
        final Options opt = new OptionsBuilder()
                // Register our benchmarks.
                .include(JNHConnectorBenchmark.class.getSimpleName())
                .build();

        new Runner(opt).run();
    }
}