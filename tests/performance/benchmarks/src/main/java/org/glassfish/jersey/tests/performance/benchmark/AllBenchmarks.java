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

import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

/**
 * @author Michal Gajdos
 */
public class AllBenchmarks {

    public static void main(final String[] args) throws Exception {
        final Options opt = new OptionsBuilder()
                // Register our benchmarks.
                .include(ClientBenchmark.class.getSimpleName())
                .include(JacksonBenchmark.class.getSimpleName())
                .include(LocatorBenchmark.class.getSimpleName())
                .include(JerseyUriBuilderBenchmark.class.getSimpleName())
                // Measure throughput in seconds (ops/s).
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.SECONDS)
                // Warm-up setup.
                .warmupIterations(16)
                .warmupTime(TimeValue.milliseconds(2500))
                // Measurement setup.
                .measurementIterations(16)
                .measurementTime(TimeValue.milliseconds(2500))
                // Fork! (Invoke benchmarks in separate JVM)
                .forks(1)
                .build();

        new Runner(opt).run();
    }
}
