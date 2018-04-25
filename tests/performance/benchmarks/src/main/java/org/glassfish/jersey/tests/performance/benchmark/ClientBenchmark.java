/*
 * Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.InvocationCallback;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.test.util.client.LoopBackConnectorProvider;

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
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

/**
 * Locator {@link org.glassfish.jersey.server.ApplicationHandler} benchmark.
 *
 * @author Michal Gajdos
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 16, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 16, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@State(Scope.Benchmark)
public class ClientBenchmark {

    private volatile Client client;

    @Setup
    public void start() throws Exception {
        client = ClientBuilder.newClient(LoopBackConnectorProvider.getClientConfig());
    }

    @TearDown
    public void shutdown() {
        client.close();
    }

    @Benchmark
    public Response get() throws Exception {
        return client.target("foo").request().get();
    }

    @Benchmark
    public Response post() throws Exception {
        return client.target("foo").request().post(Entity.text("bar"));
    }

    @Benchmark
     public Response asyncBlock() throws Exception {
        return client.target("foo").request().async().get().get();
    }

    @Benchmark
    public Future<Response> asyncIgnore() throws Exception {
        return client.target("foo").request().async().get(new InvocationCallback<Response>() {
            @Override
            public void completed(final Response response) {
                // NOOP
            }

            @Override
            public void failed(final Throwable throwable) {
                // NOOP
            }
        });
    }

    @Benchmark
    public Future<Response> asyncEntityIgnore() throws Exception {
        return client.target("foo").request().async().post(Entity.text("bar"), new InvocationCallback<Response>() {
            @Override
            public void completed(final Response response) {
                // NOOP
            }

            @Override
            public void failed(final Throwable throwable) {
                // NOOP
            }
        });
    }

    public static void main(final String[] args) throws Exception {
        final Options opt = new OptionsBuilder()
                // Register our benchmarks.
                .include(ClientBenchmark.class.getSimpleName())
                .build();

        new Runner(opt).run();
    }
}
