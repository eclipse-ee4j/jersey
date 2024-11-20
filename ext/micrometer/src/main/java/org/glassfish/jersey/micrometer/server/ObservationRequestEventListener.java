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

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.monitoring.RequestEvent;
import org.glassfish.jersey.server.monitoring.RequestEventListener;

import static java.util.Objects.requireNonNull;

/**
 * {@link RequestEventListener} recording observations for Jersey server requests.
 *
 * @author Marcin Grzejszczak
 * @since 2.41
 */
public class ObservationRequestEventListener implements RequestEventListener {

    private final Map<ContainerRequest, ObservationScopeAndContext> observations = Collections
        .synchronizedMap(new IdentityHashMap<>());

    private final ObservationRegistry registry;

    private final JerseyObservationConvention customConvention;

    private final String metricName;

    private final JerseyObservationConvention defaultConvention;

    public ObservationRequestEventListener(ObservationRegistry registry, String metricName) {
        this(registry, metricName, null);
    }

    public ObservationRequestEventListener(ObservationRegistry registry, String metricName,
            JerseyObservationConvention customConvention) {
        this.registry = requireNonNull(registry);
        this.metricName = requireNonNull(metricName);
        this.customConvention = customConvention;
        this.defaultConvention = new DefaultJerseyObservationConvention(this.metricName);
    }

    @Override
    public void onEvent(RequestEvent event) {
        ContainerRequest containerRequest = event.getContainerRequest();

        switch (event.getType()) {
            case ON_EXCEPTION:
                if (!isClientError(event) || observations.get(containerRequest) != null) {
                    break;
                }
                startObservation(event);
                break;
            case REQUEST_MATCHED:
                startObservation(event);
                break;
            case RESP_FILTERS_START:
                ObservationScopeAndContext observationScopeAndContext = observations.get(containerRequest);
                if (observationScopeAndContext != null) {
                    observationScopeAndContext.jerseyContext.setResponse(event.getContainerResponse());
                    observationScopeAndContext.jerseyContext.setRequestEvent(event);
                }
                break;
            case FINISHED:
                ObservationScopeAndContext finishedObservation = observations.remove(containerRequest);
                if (finishedObservation != null) {
                    finishedObservation.jerseyContext.setRequestEvent(event);
                    Observation.Scope observationScope = finishedObservation.observationScope;
                    observationScope.close();
                    observationScope.getCurrentObservation().stop();
                }
                break;
            default:
                break;
        }
    }

    private void startObservation(RequestEvent event) {
        JerseyContext jerseyContext = new JerseyContext(event);
        Observation observation = JerseyObservationDocumentation.DEFAULT.start(this.customConvention,
                this.defaultConvention, () -> jerseyContext, this.registry);
        Observation.Scope scope = observation.openScope();
        observations.put(event.getContainerRequest(), new ObservationScopeAndContext(scope, jerseyContext));
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

    private static class ObservationScopeAndContext {

        final Observation.Scope observationScope;

        final JerseyContext jerseyContext;

        ObservationScopeAndContext(Observation.Scope observationScope, JerseyContext jerseyContext) {
            this.observationScope = observationScope;
            this.jerseyContext = jerseyContext;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            ObservationScopeAndContext that = (ObservationScopeAndContext) o;
            return Objects.equals(observationScope, that.observationScope)
                    && Objects.equals(jerseyContext, that.jerseyContext);
        }

        @Override
        public int hashCode() {
            return Objects.hash(observationScope, jerseyContext);
        }

    }

}
