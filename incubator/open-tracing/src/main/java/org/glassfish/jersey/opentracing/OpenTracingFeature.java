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

import java.util.logging.Logger;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

import org.glassfish.jersey.Beta;

import io.opentracing.util.GlobalTracer;

/**
 * A feature that enables OpenTracing support on server and client.
 *
 * @author Adam Lindenthal (adam.lindenthal at oracle.com)
 * @since 2.26
 */
@Beta
public class OpenTracingFeature implements Feature {
    private static final Logger LOGGER = Logger.getLogger(OpenTracingFeature.class.getName());
    private final Verbosity verbosity;

    /**
     * Creates feature instance with default ({@link Verbosity#INFO} verbosity level.
     */
    public OpenTracingFeature() {
        verbosity = Verbosity.INFO;
    }

    /**
     * Creates feature instance with given ({@link Verbosity} level.
     * @param verbosity desired level of logging verbosity
     */
    public OpenTracingFeature(Verbosity verbosity) {
        this.verbosity = verbosity;
    }

    /**
     * Stored span's {@link ContainerRequestContext} property key.
     */
    public static final String SPAN_CONTEXT_PROPERTY = "span";

    /**
     * Default resource span name.
     */
    public static final String DEFAULT_RESOURCE_SPAN_NAME = "jersey-resource";

    /**
     * Default child span name.
     */
    public static final String DEFAULT_CHILD_SPAN_NAME = "jersey-resource-app";

    /**
     * Default request "root" span name.
     */
    public static final String DEFAULT_REQUEST_SPAN_NAME = "jersey-server";

    @Override
    public boolean configure(FeatureContext context) {
        if (!GlobalTracer.isRegistered()) {
            LOGGER.warning(LocalizationMessages.OPENTRACING_TRACER_NOT_REGISTERED());
        }

        switch (context.getConfiguration().getRuntimeType()) {
            case CLIENT:
                context.register(OpenTracingClientRequestFilter.class).register(OpenTracingClientResponseFilter.class);
                break;
            case SERVER:
                context.register(new OpenTracingApplicationEventListener(verbosity));
        }
        return true;
    }

    /**
     * OpenTracing Jersey event logging verbosity.
     */
    public enum Verbosity {
        /**
         * Only logs basic Jersey processing related events.
         */
        INFO,

        /**
         * Logs more fine grained events related to Jersey processing.
         */
        TRACE
    }

}
