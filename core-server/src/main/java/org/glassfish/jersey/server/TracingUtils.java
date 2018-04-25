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

package org.glassfish.jersey.server;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.HttpHeaders;

import org.glassfish.jersey.message.internal.TracingLogger;
import org.glassfish.jersey.server.internal.ServerTraceEvent;

/**
 * Utilities for tracing support.
 *
 * @author Libor Kramolis (libor.kramolis at oracle.com)
 * @since 2.3
 */
public final class TracingUtils {

    private static final List<String> SUMMARY_HEADERS = new ArrayList<>();

    static {
        SUMMARY_HEADERS.add(HttpHeaders.ACCEPT.toLowerCase());
        SUMMARY_HEADERS.add(HttpHeaders.ACCEPT_ENCODING.toLowerCase());
        SUMMARY_HEADERS.add(HttpHeaders.ACCEPT_CHARSET.toLowerCase());
        SUMMARY_HEADERS.add(HttpHeaders.ACCEPT_LANGUAGE.toLowerCase());
        SUMMARY_HEADERS.add(HttpHeaders.CONTENT_TYPE.toLowerCase());
        SUMMARY_HEADERS.add(HttpHeaders.CONTENT_LENGTH.toLowerCase());
    }

    private static final TracingConfig DEFAULT_CONFIGURATION_TYPE = TracingConfig.OFF;

    private TracingUtils() {
    }

    /**
     * According to configuration/request header it initialize {@link TracingLogger} and put it to the request properties.
     *
     * @param type             application-wide tracing configuration type.
     * @param appThreshold     application-wide tracing level threshold.
     * @param containerRequest request instance to get runtime properties to store {@link TracingLogger} instance to
     *                         if tracing support is enabled for the request.
     */
    public static void initTracingSupport(TracingConfig type,
                                          TracingLogger.Level appThreshold,
                                          ContainerRequest containerRequest) {
        final TracingLogger tracingLogger;
        if (isTracingSupportEnabled(type, containerRequest)) {
            tracingLogger = TracingLogger.create(
                    getTracingThreshold(appThreshold, containerRequest),
                    getTracingLoggerNameSuffix(containerRequest));
        } else {
            tracingLogger = TracingLogger.empty();
        }

        containerRequest.setProperty(TracingLogger.PROPERTY_NAME, tracingLogger);
    }

    /**
     * Log tracing messages START events.
     *
     * @param request container request instance to get runtime properties
     *                to check if tracing support is enabled for the request.
     */
    public static void logStart(ContainerRequest request) {
        TracingLogger tracingLogger = TracingLogger.getInstance(request);
        if (tracingLogger.isLogEnabled(ServerTraceEvent.START)) {
            StringBuilder textSB = new StringBuilder();
            textSB.append(String.format("baseUri=[%s] requestUri=[%s] method=[%s] authScheme=[%s]",
                    request.getBaseUri(), request.getRequestUri(), request.getMethod(),
                    toStringOrNA(request.getSecurityContext().getAuthenticationScheme())));
            for (String header : SUMMARY_HEADERS) {
                textSB.append(String.format(" %s=%s", header, toStringOrNA(request.getRequestHeaders().get(header))));
            }
            tracingLogger.log(ServerTraceEvent.START, textSB.toString());
        }
        if (tracingLogger.isLogEnabled(ServerTraceEvent.START_HEADERS)) {
            StringBuilder textSB = new StringBuilder();
            for (String header : request.getRequestHeaders().keySet()) {
                if (!SUMMARY_HEADERS.contains(header)) {
                    textSB.append(String.format(" %s=%s", header, toStringOrNA(request.getRequestHeaders().get(header))));
                }
            }
            if (textSB.length() > 0) {
                textSB.insert(0, "Other request headers:");
            }
            tracingLogger.log(ServerTraceEvent.START_HEADERS, textSB.toString());
        }
    }

    /**
     * Test if application and request settings enabled tracing support.
     *
     * @param type             application tracing configuration type.
     * @param containerRequest request instance to check request headers.
     * @return {@code true} if tracing support is switched on for the request.
     */
    private static boolean isTracingSupportEnabled(TracingConfig type, ContainerRequest containerRequest) {
        return (type == TracingConfig.ALL)
                || ((type == TracingConfig.ON_DEMAND) && (containerRequest.getHeaderString(TracingLogger.HEADER_ACCEPT) != null));
    }

    /**
     * Return configuration type of tracing support according to application configuration.
     *
     * By default tracing support is switched OFF.
     *
     * @param configuration application configuration.
     * @return configuration type, transformed text value to enum read from configuration or default.
     */
    /*package*/
    static TracingConfig getTracingConfig(Configuration configuration) {
        final String tracingText = ServerProperties.getValue(configuration.getProperties(),
                ServerProperties.TRACING, String.class);

        final TracingConfig result;
        if (tracingText != null) {
            result = TracingConfig.valueOf(tracingText);
        } else {
            result = DEFAULT_CONFIGURATION_TYPE;
        }
        return result;
    }

    /**
     * Get request header specified JDK logger name suffix.
     *
     * @param request container request instance to get request header {@link TracingLogger#HEADER_LOGGER} value.
     * @return Logger name suffix or {@code null} if not set.
     */
    private static String getTracingLoggerNameSuffix(ContainerRequest request) {
        return request.getHeaderString(TracingLogger.HEADER_LOGGER);
    }

    /**
     * Get application-wide tracing level threshold.
     *
     * @param configuration application configuration.
     * @return tracing level threshold.
     */
    /*package*/
    static TracingLogger.Level getTracingThreshold(Configuration configuration) {
        final String thresholdText = ServerProperties.getValue(
                configuration.getProperties(),
                ServerProperties.TRACING_THRESHOLD, String.class);

        return (thresholdText == null) ? TracingLogger.DEFAULT_LEVEL : TracingLogger.Level.valueOf(thresholdText);
    }

    private static TracingLogger.Level getTracingThreshold(TracingLogger.Level appThreshold, ContainerRequest containerRequest) {
        final String thresholdText = containerRequest.getHeaderString(TracingLogger.HEADER_THRESHOLD);

        return (thresholdText == null) ? appThreshold : TracingLogger.Level.valueOf(thresholdText);
    }

    private static String toStringOrNA(Object object) {
        if (object == null) {
            return "n/a";
        } else {
            return String.valueOf(object);
        }
    }

}
