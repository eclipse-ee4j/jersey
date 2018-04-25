/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.opentracing;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.MultivaluedMap;

import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.util.GlobalTracer;

/**
 * Utility methods for Jersey OpenTracing integration.
 *
 * @author Adam Lindenthal (adam.lindenthal at oracle.com)
 * @since 2.26
 */
public class OpenTracingUtils {

    private OpenTracingUtils() {
    }

    /**
     * Resolve resource-level span.
     * <p>
     * If open tracing is enabled and {@link GlobalTracer} is registered, resource-level span should be stored in the
     * {@link OpenTracingFeature#SPAN_CONTEXT_PROPERTY}. This span is resolved and returned as an {@link Optional}.
     *
     * @param context {@link ContainerRequestContext} instance, can be obtained via {@code @Context} injection
     * @return {@link Optional} of the resolved span, if found; empty optional if not
     */
    public static Optional<Span> getRequestSpan(final ContainerRequestContext context) {
        if (context != null) {
            final Object spanProperty = context.getProperty(OpenTracingFeature.SPAN_CONTEXT_PROPERTY);
            if (spanProperty != null && spanProperty instanceof Span) {
                return Optional.of((Span) spanProperty);
            }
        }
        return Optional.empty();
    }

    /**
     * Create and start ad-hoc custom span with the default name as a child span of the request span (if available).
     *
     * @param context {@link ContainerRequestContext} instance, can be obtained via {@code @Context} injection
     * @return If parent span ("request span") instance is stored in the {@code ContainerRequestContext}, new span is created
     * as a child span of the found span. If no parent span found, new "root" span is created. In both cases, the returned span
     * is already started. In order to successfully store the tracing, {@link Span#finish()} needs to be invoked explicitly,
     * after the traced code finishes.
     */
    public static Span getRequestChildSpan(final ContainerRequestContext context) {
        return getRequestChildSpan(context, OpenTracingFeature.DEFAULT_CHILD_SPAN_NAME);
    }

    /**
     * Create and start ad-hoc custom span with a custom name as a child span of the request span (if available).
     *
     * @param context  {@link ContainerRequestContext} instance, can be obtained via {@code @Context} injection
     * @param spanName name to be used for the created span
     * @return If parent span ("request span") instance is stored in the {@code ContainerRequestContext}, new span is created
     * as a child span of the found span. If no parent span found, new "root" span is created. In both cases, the returned span
     * is already started. In order to successfully store the tracing, {@link Span#finish()} needs to be invoked explicitly,
     * after the traced code finishes.
     */
    public static Span getRequestChildSpan(final ContainerRequestContext context, final String spanName) {
        Tracer.SpanBuilder spanBuilder = GlobalTracer.get().buildSpan(spanName);
        if (context != null) {
            final Object spanProperty = context.getProperty(OpenTracingFeature.SPAN_CONTEXT_PROPERTY);
            if (spanProperty != null && spanProperty instanceof Span) {
                spanBuilder = spanBuilder.asChildOf((Span) spanProperty);
            }
        }
        return spanBuilder.startManual();
    }

    /**
     * Convert request/response headers from {@link MultivaluedMap} into printable form.
     *
     * @param headers multi-valued map of request or response headers
     * @return {@code String} representation, e.g. "[header1=foo]; [header2=bar, baz]"
     */
    static String headersAsString(final MultivaluedMap<String, ?> headers) {
        return headers.entrySet()
                .stream()
                .map((entry) -> "["
                        + entry.getKey() + "="
                        + entry.getValue()
                        .stream()
                        .map(Object::toString)
                        .collect(Collectors.joining(", "))
                        + "]")
                .collect(Collectors.joining("; "));
    }

    static String formatList(List<?> list) {
        return list.stream().map(Object::toString).collect(Collectors.joining(", "));
    }

    static String formatProviders(Iterable<?> providers) {
        return StreamSupport.stream(providers.spliterator(), false)
                .map((provider) -> provider.getClass().getName())
                .collect(Collectors.joining(", "));
    }
}
