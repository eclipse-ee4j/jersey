/*
 * Copyright (c) 2023, 2024 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.micrometer;

import org.glassfish.jersey.inject.hk2.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;

import jakarta.ws.rs.ApplicationPath;

@ApplicationPath("/")
public class MetricsResourceConfig extends ResourceConfig {

    private final MetricsStore store = new MetricsStore();

    public MetricsResourceConfig() {
        register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(store).to(MetricsStore.class);
            }
        });
        register(store.getMetricsApplicationEventListener());
        register(TimedResource.class);
        register(MetricsResource.class);
        register(SummaryResource.class);
    }

    public MetricsStore getStore() {
        return store;
    }
}
