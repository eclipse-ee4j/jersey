/*
 * Copyright (c) 2024 Oracle and/or its affiliates. All rights reserved.
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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.LongTaskTimer;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.model.ResourceMethod;
import org.glassfish.jersey.server.monitoring.RequestEvent;
import org.glassfish.jersey.server.monitoring.RequestEventListener;

import static java.util.Objects.requireNonNull;

/**
 * {@link RequestEventListener} recording timings for Jersey server requests.
 *
 * @author Michael Weirauch
 * @author Jon Schneider
 * @since 2.41
 */
public class MetricsRequestEventListener implements RequestEventListener {

    private final Map<ContainerRequest, Timer.Sample> shortTaskSample = Collections
        .synchronizedMap(new IdentityHashMap<>());

    private final Map<ContainerRequest, Collection<LongTaskTimer.Sample>> longTaskSamples = Collections
        .synchronizedMap(new IdentityHashMap<>());

    private final Map<ContainerRequest, Set<Timed>> timedAnnotationsOnRequest = Collections
        .synchronizedMap(new IdentityHashMap<>());

    private final MeterRegistry registry;

    private final JerseyTagsProvider tagsProvider;

    private boolean autoTimeRequests;

    private final TimedFinder timedFinder;

    private final String metricName;

    public MetricsRequestEventListener(MeterRegistry registry, JerseyTagsProvider tagsProvider, String metricName,
            boolean autoTimeRequests, AnnotationFinder annotationFinder) {
        this.registry = requireNonNull(registry);
        this.tagsProvider = requireNonNull(tagsProvider);
        this.metricName = requireNonNull(metricName);
        this.autoTimeRequests = autoTimeRequests;
        this.timedFinder = new TimedFinder(annotationFinder);
    }

    @Override
    public void onEvent(RequestEvent event) {
        ContainerRequest containerRequest = event.getContainerRequest();
        Set<Timed> timedAnnotations;

        switch (event.getType()) {
            case ON_EXCEPTION:
                if (!isClientError(event)) {
                    break;
                }
                time(event, containerRequest);
                break;
            case REQUEST_MATCHED:
                time(event, containerRequest);
                break;
            case FINISHED:
                timedAnnotations = timedAnnotationsOnRequest.remove(containerRequest);
                Timer.Sample shortSample = shortTaskSample.remove(containerRequest);

                if (shortSample != null) {
                    for (Timer timer : shortTimers(timedAnnotations, event)) {
                        shortSample.stop(timer);
                    }
                }

                Collection<LongTaskTimer.Sample> longSamples = this.longTaskSamples.remove(containerRequest);
                if (longSamples != null) {
                    for (LongTaskTimer.Sample longSample : longSamples) {
                        longSample.stop();
                    }
                }
                break;
        }
    }

    private void time(RequestEvent event, ContainerRequest containerRequest) {
        Set<Timed> timedAnnotations;
        timedAnnotations = annotations(event);

        timedAnnotationsOnRequest.put(containerRequest, timedAnnotations);
        shortTaskSample.put(containerRequest, Timer.start(registry));

        List<LongTaskTimer.Sample> longTaskSamples = longTaskTimers(timedAnnotations, event).stream()
            .map(LongTaskTimer::start)
            .collect(Collectors.toList());
        if (!longTaskSamples.isEmpty()) {
            this.longTaskSamples.put(containerRequest, longTaskSamples);
        }
    }

    private boolean isClientError(RequestEvent event) {
        Throwable t = event.getException();
        if (t == null) {
            return false;
        }
        String className = t.getClass().getSuperclass().getCanonicalName();
        return className.equals("jakarta.ws.rs.ClientErrorException")
                || className.equals("javax.ws.rs.ClientErrorException");
    }

    private Set<Timer> shortTimers(Set<Timed> timed, RequestEvent event) {
        /*
         * Given we didn't find any matching resource method, 404s will be only recorded
         * when auto-time-requests is enabled. On par with WebMVC instrumentation.
         */
        if ((timed == null || timed.isEmpty()) && autoTimeRequests) {
            return Collections.singleton(registry.timer(metricName, tagsProvider.httpRequestTags(event)));
        }

        if (timed == null) {
            return Collections.emptySet();
        }

        return timed.stream()
            .filter(annotation -> !annotation.longTask())
            .map(t -> Timer.builder(t, metricName).tags(tagsProvider.httpRequestTags(event)).register(registry))
            .collect(Collectors.toSet());
    }

    private Set<LongTaskTimer> longTaskTimers(Set<Timed> timed, RequestEvent event) {
        return timed.stream()
            .filter(Timed::longTask)
            .map(LongTaskTimer::builder)
            .map(b -> b.tags(tagsProvider.httpLongRequestTags(event)).register(registry))
            .collect(Collectors.toSet());
    }

    private Set<Timed> annotations(RequestEvent event) {
        final Set<Timed> timed = new HashSet<>();

        final ResourceMethod matchingResourceMethod = event.getUriInfo().getMatchedResourceMethod();
        if (matchingResourceMethod != null) {
            // collect on method level
            timed.addAll(timedFinder.findTimedAnnotations(matchingResourceMethod.getInvocable().getHandlingMethod()));

            // fallback on class level
            if (timed.isEmpty()) {
                timed.addAll(timedFinder.findTimedAnnotations(
                        matchingResourceMethod.getInvocable().getHandlingMethod().getDeclaringClass()));
            }
        }
        return timed;
    }

}
