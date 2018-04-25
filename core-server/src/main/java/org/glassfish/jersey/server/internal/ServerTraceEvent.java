/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.server.internal;

import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import org.glassfish.jersey.message.internal.TracingLogger;

/**
 * Server side tracing events.
 *
 * @author Libor Kramolis (libor.kramolis at oracle.com)
 * @since 2.3
 */
public enum ServerTraceEvent implements TracingLogger.Event {
    /**
     * Request processing started.
     */
    START(TracingLogger.Level.SUMMARY, "START", null),
    /**
     * All HTTP request headers.
     */
    START_HEADERS(TracingLogger.Level.VERBOSE, "START", null),
    /**
     * {@link PreMatching} {@link ContainerRequestFilter} invoked.
     */
    PRE_MATCH(TracingLogger.Level.TRACE, "PRE-MATCH", "Filter by %s"),
    /**
     * {@link PreMatching} {@link ContainerRequestFilter} invocation summary.
     */
    PRE_MATCH_SUMMARY(TracingLogger.Level.SUMMARY, "PRE-MATCH", "PreMatchRequest summary: %s filters"),
    /**
     * Matching path pattern.
     */
    MATCH_PATH_FIND(TracingLogger.Level.TRACE, "MATCH", "Matching path [%s]"),
    /**
     * Path pattern not matched.
     */
    MATCH_PATH_NOT_MATCHED(TracingLogger.Level.VERBOSE, "MATCH", "Pattern [%s] is NOT matched"),
    /**
     * Path pattern matched/selected.
     */
    MATCH_PATH_SELECTED(TracingLogger.Level.TRACE, "MATCH", "Pattern [%s] IS selected"),
    /**
     * Path pattern skipped as higher-priority pattern has been selected already.
     */
    MATCH_PATH_SKIPPED(TracingLogger.Level.VERBOSE, "MATCH", "Pattern [%s] is skipped"),
    /**
     * Matched sub-resource locator method.
     */
    MATCH_LOCATOR(TracingLogger.Level.TRACE, "MATCH", "Matched locator : %s"),
    /**
     * Matched resource method.
     */
    MATCH_RESOURCE_METHOD(TracingLogger.Level.TRACE, "MATCH", "Matched method  : %s"),
    /**
     * Matched runtime resource.
     */
    MATCH_RUNTIME_RESOURCE(TracingLogger.Level.TRACE, "MATCH",
            "Matched resource: template=[%s] regexp=[%s] matches=[%s] from=[%s]"),
    /**
     * Matched resource instance.
     */
    MATCH_RESOURCE(TracingLogger.Level.TRACE, "MATCH", "Resource instance: %s"),
    /**
     * Matching summary.
     */
    MATCH_SUMMARY(TracingLogger.Level.SUMMARY, "MATCH", "RequestMatching summary"),
    /**
     * Global {@link ContainerRequestFilter} invoked.
     */
    REQUEST_FILTER(TracingLogger.Level.TRACE, "REQ-FILTER", "Filter by %s"),
    /**
     * Global {@link ContainerRequestFilter} invocation summary.
     */
    REQUEST_FILTER_SUMMARY(TracingLogger.Level.SUMMARY, "REQ-FILTER", "Request summary: %s filters"),
    /**
     * Resource method invoked.
     */
    METHOD_INVOKE(TracingLogger.Level.SUMMARY, "INVOKE", "Resource %s method=[%s]"),
    /**
     * Resource method invocation results to JAX-RS {@link Response}.
     */
    DISPATCH_RESPONSE(TracingLogger.Level.TRACE, "INVOKE", "Response: %s"),
    /**
     * {@link ContainerResponseFilter} invoked.
     */
    RESPONSE_FILTER(TracingLogger.Level.TRACE, "RESP-FILTER", "Filter by %s"),
    /**
     * {@link ContainerResponseFilter} invocation summary.
     */
    RESPONSE_FILTER_SUMMARY(TracingLogger.Level.SUMMARY, "RESP-FILTER", "Response summary: %s filters"),
    /**
     * Request processing finished.
     */
    FINISHED(TracingLogger.Level.SUMMARY, "FINISHED", "Response status: %s"),
    /**
     * {@link ExceptionMapper} invoked.
     */
    EXCEPTION_MAPPING(TracingLogger.Level.SUMMARY, "EXCEPTION", "Exception mapper %s maps %s ('%s') to <%s>");

    private final TracingLogger.Level level;
    private final String category;
    private final String messageFormat;

    private ServerTraceEvent(TracingLogger.Level level, String category, String messageFormat) {
        this.level = level;
        this.category = category;
        this.messageFormat = messageFormat;
    }

    @Override
    public String category() {
        return category;
    }

    @Override
    public TracingLogger.Level level() {
        return level;
    }

    @Override
    public String messageFormat() {
        return messageFormat;
    }
}
