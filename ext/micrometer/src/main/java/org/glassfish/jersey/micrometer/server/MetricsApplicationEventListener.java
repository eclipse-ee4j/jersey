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

import io.micrometer.core.instrument.MeterRegistry;
import org.glassfish.jersey.server.monitoring.ApplicationEvent;
import org.glassfish.jersey.server.monitoring.ApplicationEventListener;
import org.glassfish.jersey.server.monitoring.RequestEvent;
import org.glassfish.jersey.server.monitoring.RequestEventListener;

import static java.util.Objects.requireNonNull;

/**
 * The Micrometer {@link ApplicationEventListener} which registers
 * {@link RequestEventListener} for instrumenting Jersey server requests.
 *
 * @author Michael Weirauch
 * @since 2.41
 */
public class MetricsApplicationEventListener implements ApplicationEventListener {

    private final MeterRegistry meterRegistry;

    private final JerseyTagsProvider tagsProvider;

    private final String metricName;

    private final AnnotationFinder annotationFinder;

    private final boolean autoTimeRequests;

    public MetricsApplicationEventListener(MeterRegistry registry, JerseyTagsProvider tagsProvider, String metricName,
            boolean autoTimeRequests) {
        this(registry, tagsProvider, metricName, autoTimeRequests, AnnotationFinder.DEFAULT);
    }

    public MetricsApplicationEventListener(MeterRegistry registry, JerseyTagsProvider tagsProvider, String metricName,
            boolean autoTimeRequests, AnnotationFinder annotationFinder) {
        this.meterRegistry = requireNonNull(registry);
        this.tagsProvider = requireNonNull(tagsProvider);
        this.metricName = requireNonNull(metricName);
        this.annotationFinder = requireNonNull(annotationFinder);
        this.autoTimeRequests = autoTimeRequests;
    }

    @Override
    public void onEvent(ApplicationEvent event) {
    }

    @Override
    public RequestEventListener onRequest(RequestEvent requestEvent) {
        return new MetricsRequestEventListener(meterRegistry, tagsProvider, metricName, autoTimeRequests,
                annotationFinder);
    }

}
