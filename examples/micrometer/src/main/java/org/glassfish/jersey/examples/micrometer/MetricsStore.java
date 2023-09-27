/*
 * Copyright (c) 2023 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.micrometer;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.jersey.server.DefaultJerseyTagsProvider;
import io.micrometer.core.instrument.binder.jersey.server.MetricsApplicationEventListener;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

public class MetricsStore {

    public static final String REGISTRY_NAME = "http.shared.metrics";
    private final MetricsApplicationEventListener metricsApplicationEventListener;
    private final MeterRegistry registry = new SimpleMeterRegistry();

    public MetricsStore() {
        metricsApplicationEventListener = new MetricsApplicationEventListener(registry,
                new DefaultJerseyTagsProvider(),
                REGISTRY_NAME, true);
    }

    public MetricsApplicationEventListener getMetricsApplicationEventListener() {
        return metricsApplicationEventListener;
    }

    public MeterRegistry getRegistry() {
        return registry;
    }


    public TimedAspect timedAspect() {
        return new TimedAspect(registry);
    }
}