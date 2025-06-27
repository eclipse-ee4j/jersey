/*
 * Copyright (c) 2024, 2025 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.jersey.micrometer.server.observation;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.MediaType;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.observation.DefaultMeterObservationHandler;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.micrometer.observation.ObservationHandler.FirstMatchingCompositeObservationHandler;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.exporter.FinishedSpan;
import io.micrometer.tracing.handler.DefaultTracingObservationHandler;
import io.micrometer.tracing.handler.PropagatingReceiverTracingObservationHandler;
import io.micrometer.tracing.handler.PropagatingSenderTracingObservationHandler;
import io.micrometer.tracing.propagation.Propagator;
import io.micrometer.tracing.test.simple.SpanAssert;
import io.micrometer.tracing.test.simple.SpansAssert;
import org.glassfish.jersey.micrometer.server.ObservationApplicationEventListener;
import org.glassfish.jersey.micrometer.server.ObservationRequestEventListener;
import org.glassfish.jersey.micrometer.server.mapper.ResourceGoneExceptionMapper;
import org.glassfish.jersey.micrometer.server.resources.TestResource;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.Test;
import zipkin2.CheckResult;
import zipkin2.reporter.BytesMessageSender;
import zipkin2.reporter.urlconnection.URLConnectionSender;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ObservationRequestEventListener}.
 *
 * @author Marcin Grzejsczak
 */
abstract class AbstractObservationRequestEventListenerTest extends JerseyTest {

    static {
        Logger.getLogger("org.glassfish.jersey").setLevel(Level.OFF);
    }

    private static final String METRIC_NAME = "http.server.requests";

    ObservationRegistry observationRegistry;

    MeterRegistry registry;

    Boolean zipkinAvailable;

    BytesMessageSender sender;

    @Override
    protected Application configure() {
        observationRegistry = ObservationRegistry.create();
        registry = new SimpleMeterRegistry();
        sender = URLConnectionSender.create("http://localhost:9411/api/v2/spans");

        observationRegistry.observationConfig().observationHandler(new DefaultMeterObservationHandler(registry));

        configureRegistry(observationRegistry);

        final ObservationApplicationEventListener listener =
                new ObservationApplicationEventListener(observationRegistry, METRIC_NAME);

        final ResourceConfig config = new ResourceConfig();
        config.register(listener);
        config.register(TestResource.class);
        config.register(ResourceGoneExceptionMapper.class);

        return config;
    }

    abstract void configureRegistry(ObservationRegistry registry);

    abstract List<FinishedSpan> getFinishedSpans();

    boolean isZipkinAvailable() {
        if (zipkinAvailable == null) {
            CheckResult checkResult = CheckResult.OK;
            zipkinAvailable = checkResult.ok();
        }
        return zipkinAvailable;
    }

    void setupTracing(Tracer tracer, Propagator propagator) {
        observationRegistry.observationConfig()
                .observationHandler(new FirstMatchingCompositeObservationHandler(
                new PropagatingSenderTracingObservationHandler<>(tracer, propagator),
                new PropagatingReceiverTracingObservationHandler<>(tracer, propagator),
                new DefaultTracingObservationHandler(tracer)));
    }

    @Test
    void resourcesAreTimed() {
        target("sub-resource/sub-hello/peter").request().get();

        assertThat(registry.get(METRIC_NAME)
                .tags(tagsFrom("/sub-resource/sub-hello/{name}", "200", "SUCCESS", null))
                .timer()
                .count()).isEqualTo(1);
        // Timer and Long Task Timer
        assertThat(registry.getMeters()).hasSize(2);

        List<FinishedSpan> finishedSpans = getFinishedSpans();
        SpansAssert.assertThat(finishedSpans).hasSize(1);
        FinishedSpan finishedSpan = finishedSpans.get(0);
        System.out.println("Trace Id [" + finishedSpan.getTraceId() + "]");
        SpanAssert.assertThat(finishedSpan)
                .hasNameEqualTo("HTTP GET")
                .hasTag("exception", "None")
                .hasTag("method", "GET")
                .hasTag("outcome", "SUCCESS")
                .hasTag("status", "200")
                .hasTag("uri", "/sub-resource/sub-hello/{name}");
        assertThat(observationRegistry.getCurrentObservation()).isNull();
    }

    @Test
    void errorResourcesAreTimed() {
        try {
            target("throws-exception").request().get();
        }
        catch (Exception ignored) {
        }
        try {
            target("throws-webapplication-exception").request().get();
        }
        catch (Exception ignored) {
        }
        try {
            target("throws-mappable-exception").request().get();
        }
        catch (Exception ignored) {
        }
        try {
            target("produces-text-plain").request(MediaType.APPLICATION_JSON).get();
        }
        catch (Exception ignored) {
        }

        assertThat(registry.get(METRIC_NAME)
                       .tags(tagsFrom("/throws-exception", "500", "SERVER_ERROR", "IllegalArgumentException"))
                       .timer()
                       .count()).isEqualTo(1);

        assertThat(registry.get(METRIC_NAME)
                       .tags(tagsFrom("/throws-webapplication-exception", "401", "CLIENT_ERROR", "NotAuthorizedException"))
                       .timer()
                       .count()).isEqualTo(1);

        assertThat(registry.get(METRIC_NAME)
                       .tags(tagsFrom("/throws-mappable-exception", "410", "CLIENT_ERROR", "ResourceGoneException"))
                       .timer()
                       .count()).isEqualTo(1);

        assertThat(registry.get(METRIC_NAME)
                       .tags(tagsFrom("UNKNOWN", "406", "CLIENT_ERROR", "NotAcceptableException"))
                       .timer()
                       .count()).isEqualTo(1);

        assertThat(observationRegistry.getCurrentObservation()).isNull();
    }

    private static Iterable<Tag> tagsFrom(String uri, String status, String outcome, String exception) {
        return Tags.of("method", "GET", "uri", uri, "status", status, "outcome", outcome, "exception",
                exception == null ? "None" : exception);
    }
}
