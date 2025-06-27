/*
 * Copyright (c) 2023, 2025 Oracle and/or its affiliates. All rights reserved.
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

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import brave.Tracing;
import brave.Tracing.Builder;
import brave.context.slf4j.MDCScopeDecorator;
import brave.handler.SpanHandler;
import brave.propagation.B3Propagation;
import brave.propagation.B3Propagation.Format;
import brave.propagation.ThreadLocalCurrentTraceContext;
import brave.sampler.Sampler;
import brave.test.TestSpanHandler;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.tracing.CurrentTraceContext;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.brave.bridge.BraveBaggageManager;
import io.micrometer.tracing.brave.bridge.BraveCurrentTraceContext;
import io.micrometer.tracing.brave.bridge.BraveFinishedSpan;
import io.micrometer.tracing.brave.bridge.BravePropagator;
import io.micrometer.tracing.brave.bridge.BraveTracer;
import io.micrometer.tracing.exporter.FinishedSpan;
import io.micrometer.tracing.otel.bridge.ArrayListSpanProcessor;
import io.micrometer.tracing.otel.bridge.OtelBaggageManager;
import io.micrometer.tracing.otel.bridge.OtelCurrentTraceContext;
import io.micrometer.tracing.otel.bridge.OtelFinishedSpan;
import io.micrometer.tracing.otel.bridge.OtelPropagator;
import io.micrometer.tracing.otel.bridge.OtelTracer;
import io.micrometer.tracing.otel.bridge.Slf4JBaggageEventListener;
import io.micrometer.tracing.otel.bridge.Slf4JEventListener;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.exporter.zipkin.ZipkinSpanExporterBuilder;
import io.opentelemetry.extension.trace.propagation.B3Propagator;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.SdkTracerProviderBuilder;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import io.opentelemetry.semconv.ServiceAttributes;
import org.glassfish.jersey.micrometer.server.ObservationApplicationEventListener;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import zipkin2.Span;
import zipkin2.reporter.AsyncReporter;
import zipkin2.reporter.brave.ZipkinSpanHandler;

import static io.opentelemetry.sdk.trace.samplers.Sampler.alwaysOn;

/**
 * Tests for {@link ObservationApplicationEventListener}.
 *
 * @author Marcin Grzejsczak
 */
class ObservationApplicationEventListenerTest {

    @Nested
    class BraveObservationRequestEventListenerTest extends AbstractObservationRequestEventListenerTest {

        Tracing tracing;

        TestSpanHandler testSpanHandler;

        AsyncReporter<Span> reporter;

        @Override
        void configureRegistry(ObservationRegistry registry) {
            testSpanHandler = new TestSpanHandler();

            reporter = AsyncReporter.create(sender);

            SpanHandler spanHandler = ZipkinSpanHandler
                    .create(reporter);

            ThreadLocalCurrentTraceContext braveCurrentTraceContext = ThreadLocalCurrentTraceContext.newBuilder()
                    .addScopeDecorator(MDCScopeDecorator.get()) // Example of Brave's
                    // automatic MDC setup
                    .build();

            CurrentTraceContext bridgeContext = new BraveCurrentTraceContext(braveCurrentTraceContext);

            Builder builder = Tracing.newBuilder()
                    .currentTraceContext(braveCurrentTraceContext)
                    .supportsJoin(false)
                    .traceId128Bit(true)
                    .propagationFactory(B3Propagation.newFactoryBuilder().injectFormat(Format.SINGLE).build())
                    .sampler(Sampler.ALWAYS_SAMPLE)
                    .addSpanHandler(testSpanHandler)
                    .localServiceName("brave-test");

            if (isZipkinAvailable()) {
                builder.addSpanHandler(spanHandler);
            }

            tracing = builder
                    .build();
            brave.Tracer braveTracer = tracing.tracer();
            Tracer tracer = new BraveTracer(braveTracer, bridgeContext, new BraveBaggageManager());
            BravePropagator bravePropagator = new BravePropagator(tracing);
            setupTracing(tracer, bravePropagator);
        }

        @Override
        List<FinishedSpan> getFinishedSpans() {
            return testSpanHandler.spans().stream().map(BraveFinishedSpan::new).collect(Collectors.toList());
        }

        @AfterEach
        void cleanup() {
            if (isZipkinAvailable()) {
                reporter.flush();
                reporter.close();
            }
            tracing.close();
        }
    }

    @Nested
    class OtelObservationRequestEventListenerTest extends AbstractObservationRequestEventListenerTest {

        SdkTracerProvider sdkTracerProvider;

        ArrayListSpanProcessor processor;

        @Override
        void configureRegistry(ObservationRegistry registry) {
            processor = new ArrayListSpanProcessor();

            SpanExporter spanExporter = new ZipkinSpanExporterBuilder()
                    .setSender(sender)
                    .build();

            SdkTracerProviderBuilder builder = SdkTracerProvider.builder()
                    .setSampler(alwaysOn())
                    .addSpanProcessor(processor)
                    .setResource(Resource.create(Attributes.of(ServiceAttributes.SERVICE_NAME, "otel-test")));

            if (isZipkinAvailable()) {
                builder.addSpanProcessor(SimpleSpanProcessor.create(spanExporter));
            }

            sdkTracerProvider = builder
                    .build();

            ContextPropagators contextPropagators = ContextPropagators.create(B3Propagator.injectingSingleHeader());

            OpenTelemetrySdk openTelemetrySdk = OpenTelemetrySdk.builder()
                    .setTracerProvider(sdkTracerProvider)
                    .setPropagators(contextPropagators)
                    .build();

            io.opentelemetry.api.trace.Tracer otelTracer = openTelemetrySdk.getTracerProvider()
                    .get("io.micrometer.micrometer-tracing");

            OtelCurrentTraceContext otelCurrentTraceContext = new OtelCurrentTraceContext();

            Slf4JEventListener slf4JEventListener = new Slf4JEventListener();

            Slf4JBaggageEventListener slf4JBaggageEventListener = new Slf4JBaggageEventListener(Collections.emptyList());

            OtelTracer tracer = new OtelTracer(otelTracer, otelCurrentTraceContext, event -> {
                slf4JEventListener.onEvent(event);
                slf4JBaggageEventListener.onEvent(event);
            }, new OtelBaggageManager(otelCurrentTraceContext, Collections.emptyList(), Collections.emptyList()));
            OtelPropagator otelPropagator = new OtelPropagator(contextPropagators, otelTracer);
            setupTracing(tracer, otelPropagator);
        }

        @Override
        List<FinishedSpan> getFinishedSpans() {
            return processor.spans().stream().map(OtelFinishedSpan::fromOtel).collect(Collectors.toList());
        }

        @AfterEach
        void cleanup() {
            sdkTracerProvider.close();
        }
    }
}
