/*
 * Copyright (c) 2023 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.examples.micrometer;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.glassfish.jersey.micrometer.server.DefaultJerseyTagsProvider;
import org.glassfish.jersey.micrometer.server.MetricsApplicationEventListener;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.jupiter.api.Test;

import javax.ws.rs.core.Application;

import java.util.concurrent.TimeUnit;

import static org.glassfish.jersey.examples.micrometer.MicrometerResource.CLICHED_MESSAGE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class MicrometerTest extends JerseyTest {

    static final String TIMER_METRIC_NAME = "http.server.requests";

    MeterRegistry registry;

    @Override
    protected Application configure() {
        registry = new SimpleMeterRegistry();
        MetricsApplicationEventListener metricsListener = new MetricsApplicationEventListener(registry,
                new DefaultJerseyTagsProvider(), TIMER_METRIC_NAME, true);
        return new ResourceConfig(MicrometerResource.class)
                .register(metricsListener)
                .register(new MetricsResource(registry));
    }

    @Test
    void meterResourceTest() throws InterruptedException {
        String response = target("/meter").request().get(String.class);
        assertEquals(response, CLICHED_MESSAGE);
        // Jersey metrics are recorded asynchronously to the request completing
        Thread.sleep(10);
        Timer timer = registry.get(TIMER_METRIC_NAME)
                .tags("method", "GET", "uri", "/meter", "status", "200", "exception", "None", "outcome", "SUCCESS")
                .timer();
        assertEquals(timer.count(), 1);
        assertNotNull(timer.totalTime(TimeUnit.NANOSECONDS));
    }

}