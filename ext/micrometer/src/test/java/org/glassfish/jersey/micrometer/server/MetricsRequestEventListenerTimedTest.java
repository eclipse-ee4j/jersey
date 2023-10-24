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
package org.glassfish.jersey.micrometer.server;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Response;

import io.micrometer.core.Issue;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.glassfish.jersey.micrometer.server.resources.TimedOnClassResource;
import org.glassfish.jersey.micrometer.server.resources.TimedResource;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * @author Michael Weirauch
 */
class MetricsRequestEventListenerTimedTest extends JerseyTest {

    static {
        Logger.getLogger("org.glassfish.jersey").setLevel(Level.OFF);
    }

    private static final String METRIC_NAME = "http.server.requests";

    private MeterRegistry registry;

    private CountDownLatch longTaskRequestStartedLatch;

    private CountDownLatch longTaskRequestReleaseLatch;

    @Override
    protected Application configure() {
        registry = new SimpleMeterRegistry();
        longTaskRequestStartedLatch = new CountDownLatch(1);
        longTaskRequestReleaseLatch = new CountDownLatch(1);

        final MetricsApplicationEventListener listener = new MetricsApplicationEventListener(registry,
                new DefaultJerseyTagsProvider(), METRIC_NAME, false);

        final ResourceConfig config = new ResourceConfig();
        config.register(listener);
        config.register(new TimedResource(longTaskRequestStartedLatch, longTaskRequestReleaseLatch));
        config.register(TimedOnClassResource.class);

        return config;
    }

    @Test
    void resourcesAndNotFoundsAreNotAutoTimed() {
        target("not-timed").request().get();
        target("not-found").request().get();

        assertThat(registry.find(METRIC_NAME).tags(tagsFrom("/not-timed", 200)).timer()).isNull();

        assertThat(registry.find(METRIC_NAME).tags(tagsFrom("NOT_FOUND", 404)).timer()).isNull();
    }

    @Test
    void resourcesWithAnnotationAreTimed() {
        target("timed").request().get();
        target("multi-timed").request().get();

        assertThat(registry.get(METRIC_NAME).tags(tagsFrom("/timed", 200)).timer().count()).isEqualTo(1);

        assertThat(registry.get("multi1").tags(tagsFrom("/multi-timed", 200)).timer().count()).isEqualTo(1);

        assertThat(registry.get("multi2").tags(tagsFrom("/multi-timed", 200)).timer().count()).isEqualTo(1);
    }

    @Test
    void longTaskTimerSupported() throws InterruptedException, ExecutionException, TimeoutException {
        final Future<Response> future = target("long-timed").request().async().get();

        /*
         * Wait until the request has arrived at the server side. (Async client processing
         * might be slower in triggering the request resulting in the assertions below to
         * fail. Thread.sleep() is not an option, so resort to CountDownLatch.)
         */
        longTaskRequestStartedLatch.await(5, TimeUnit.SECONDS);

        // the request is not timed, yet
        assertThat(registry.find(METRIC_NAME).tags(tagsFrom("/timed", 200)).timer()).isNull();

        // the long running task is timed
        assertThat(registry.get("long.task.in.request")
            .tags(Tags.of("method", "GET", "uri", "/long-timed"))
            .longTaskTimer()
            .activeTasks()).isEqualTo(1);

        // finish the long running request
        longTaskRequestReleaseLatch.countDown();
        future.get(5, TimeUnit.SECONDS);

        // the request is timed after the long running request completed
        assertThat(registry.get(METRIC_NAME).tags(tagsFrom("/long-timed", 200)).timer().count()).isEqualTo(1);
    }

    @Test
    @Issue("gh-2861")
    void longTaskTimerOnlyOneMeter() throws InterruptedException, ExecutionException, TimeoutException {
        final Future<Response> future = target("just-long-timed").request().async().get();

        /*
         * Wait until the request has arrived at the server side. (Async client processing
         * might be slower in triggering the request resulting in the assertions below to
         * fail. Thread.sleep() is not an option, so resort to CountDownLatch.)
         */
        longTaskRequestStartedLatch.await(5, TimeUnit.SECONDS);

        // the long running task is timed
        assertThat(registry.get("long.task.in.request")
            .tags(Tags.of("method", "GET", "uri", "/just-long-timed"))
            .longTaskTimer()
            .activeTasks()).isEqualTo(1);

        // finish the long running request
        longTaskRequestReleaseLatch.countDown();
        future.get(5, TimeUnit.SECONDS);

        // no meters registered except the one checked above
        assertThat(registry.getMeters().size()).isOne();
    }

    @Test
    void unnamedLongTaskTimerIsNotSupported() {
        assertThatExceptionOfType(ProcessingException.class)
            .isThrownBy(() -> target("long-timed-unnamed").request().get())
            .withCauseInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void classLevelAnnotationIsInherited() {
        target("/class/inherited").request().get();

        assertThat(registry.get(METRIC_NAME)
            .tags(Tags.concat(tagsFrom("/class/inherited", 200), Tags.of("on", "class")))
            .timer()
            .count()).isEqualTo(1);
    }

    @Test
    void methodLevelAnnotationOverridesClassLevel() {
        target("/class/on-method").request().get();

        assertThat(registry.get(METRIC_NAME)
            .tags(Tags.concat(tagsFrom("/class/on-method", 200), Tags.of("on", "method")))
            .timer()
            .count()).isEqualTo(1);

        // class level annotation is not picked up
        assertThat(registry.getMeters()).hasSize(1);
    }

    private static Iterable<Tag> tagsFrom(String uri, int status) {
        return Tags.of("method", "GET", "uri", uri, "status", String.valueOf(status), "exception", "None");
    }

}
