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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.MediaType;

import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMapInjectAdapter;
import io.opentracing.tag.Tags;
import io.opentracing.util.GlobalTracer;

/**
 * Client-side request filter, that creates the client request {@code Span}.
 * <p>
 * Stores request-related metadata into the {@code Span} as {@code Tags}
 * and {@link GlobalTracer#inject(SpanContext, Format, Object) injects} it into http headers.
 *
 * @author Adam Lindenthal (adam.lindenthal at oracle.com)
 * @since 2.26
 */
class OpenTracingClientRequestFilter implements ClientRequestFilter {

    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {
        Tracer.SpanBuilder spanBuilder = GlobalTracer.get()
                .buildSpan(LocalizationMessages.OPENTRACING_SPAN_PREFIX_CLIENT() + requestContext.getMethod())
                .withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT)
                .withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT)
                .withTag(Tags.HTTP_URL.getKey(), requestContext.getUri().toASCIIString())
                .withTag(Tags.HTTP_METHOD.getKey(), requestContext.getMethod())
                .withTag(LocalizationMessages.OPENTRACING_TAG_HAS_REQUEST_ENTITY(), requestContext.hasEntity())
                .withTag(LocalizationMessages.OPENTRACING_TAG_ACCEPTABLE_MEDIA_TYPES(), requestContext.getAcceptableMediaTypes()
                        .stream()
                        .map(MediaType::toString)
                        .collect(Collectors.joining(", ")))
                .withTag(LocalizationMessages.OPENTRACING_TAG_REQUEST_HEADERS(),
                        OpenTracingUtils.headersAsString(requestContext.getHeaders()));

        // if pre-stored "span" property is found, propagate the stored context
        final Object property = requestContext.getProperty(OpenTracingFeature.SPAN_CONTEXT_PROPERTY);
        if (property != null && property instanceof SpanContext) {
            spanBuilder = spanBuilder.asChildOf((SpanContext) property);
        }
        Span span = spanBuilder.startManual();

        requestContext.setProperty(OpenTracingFeature.SPAN_CONTEXT_PROPERTY, span);
        Map<String, String> addedHeaders = new HashMap<>();
        GlobalTracer.get().inject(span.context(), Format.Builtin.HTTP_HEADERS, new TextMapInjectAdapter(addedHeaders));
        addedHeaders.forEach((key, value) -> requestContext.getHeaders().add(key, value));
    }
}
