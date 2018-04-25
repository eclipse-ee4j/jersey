/*
 * Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.helloworld;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.glassfish.jersey.server.ApplicationHandler;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.ContainerResponse;
import org.glassfish.jersey.test.util.server.ContainerRequestBuilder;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

/**
 * "Hello world!" Jersey {@link org.glassfish.jersey.server.ApplicationHandler} benchmark.
 *
 * @author Michal Gajdos
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 8, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 8, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@State(Scope.Benchmark)
public class HelloWorldBenchmark {

    @Param(value = {"helloworld", "helloworld/locator"})
    private String path;

    @Param(value = {"GET", "POST", "PUT"})
    private String method;

    private volatile ApplicationHandler handler;
    private volatile ContainerRequest request;

    @Setup
    public void start() throws Exception {
        handler = new ApplicationHandler(new Application());
    }

    @Setup(Level.Iteration)
    public void request() {
        request = ContainerRequestBuilder
                .from(path, method)
                .entity("GET".equals(method) ? null : HelloWorldResource.CLICHED_MESSAGE, handler)
                .build();
    }

    @TearDown
    public void shutdown() {
    }

    @Benchmark
    public Future<ContainerResponse> measure() throws Exception {
        return handler.apply(request);
    }

    public static void main(final String[] args) throws Exception {
        final Options opt = new OptionsBuilder()
                // Register our benchmarks.
                .include(HelloWorldBenchmark.class.getSimpleName())
                .build();

        new Runner(opt).run();
    }
}
