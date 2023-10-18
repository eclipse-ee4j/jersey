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

import io.micrometer.core.instrument.Timer;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.Test;

import javax.ws.rs.core.Application;
import java.util.concurrent.TimeUnit;

import static org.glassfish.jersey.examples.micrometer.TimedResource.MESSAGE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class MicrometerTest extends JerseyTest {

    static final int REQUESTS_COUNT = 10;

    private MetricsResourceConfig resourceConfig;

    @Override
    protected Application configure() {
        resourceConfig = new MetricsResourceConfig();
        assertNotNull(this.resourceConfig);
        return this.resourceConfig;
    }

    @Test
    void meterResourceTest() throws InterruptedException {
        final String response = target("/timed").request().get(String.class);
        assertEquals(response, MESSAGE);
        for (int i = 0; i < REQUESTS_COUNT; i++) {
            target("/metrics").request().get(String.class);
        }
        // Jersey metrics are recorded asynchronously to the request completing
        Thread.sleep(10);
        Timer timer = resourceConfig.getStore().getRegistry()
                .get(MetricsStore.REGISTRY_NAME)
                .tags("method", "GET", "uri", "/metrics", "status", "200", "exception", "None", "outcome", "SUCCESS")
                .timer();
        assertEquals(REQUESTS_COUNT, timer.count());
        assertNotNull(timer.totalTime(TimeUnit.NANOSECONDS));
    }

}