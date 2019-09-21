/*
 * Copyright (c) 2014, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.rx;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.ext.ContextResolver;

import org.glassfish.jersey.examples.rx.agent.AsyncAgentResource;
import org.glassfish.jersey.examples.rx.agent.CompletionStageAgentResource;
import org.glassfish.jersey.examples.rx.agent.FlowableAgentResource;
import org.glassfish.jersey.examples.rx.agent.ListenableFutureAgentResource;
import org.glassfish.jersey.examples.rx.agent.ObservableAgentResource;
import org.glassfish.jersey.examples.rx.agent.SyncAgentResource;
import org.glassfish.jersey.examples.rx.remote.CalculationResource;
import org.glassfish.jersey.examples.rx.remote.DestinationResource;
import org.glassfish.jersey.examples.rx.remote.ForecastResource;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * @author Michal Gajdos
 */
@ApplicationPath("rx")
public class RxApplication extends ResourceConfig {

    public RxApplication() {
        // Remote (Server) Resources.
        register(DestinationResource.class);
        register(CalculationResource.class);
        register(ForecastResource.class);

        // Agent (Client) Resources.
        register(SyncAgentResource.class);
        register(AsyncAgentResource.class);
        register(ObservableAgentResource.class);
        register(FlowableAgentResource.class);
        register(ListenableFutureAgentResource.class);
        register(CompletionStageAgentResource.class);

        // Providers.
        register(JacksonFeature.class);
        register(ObjectMapperProvider.class);
    }

    public static class ObjectMapperProvider implements ContextResolver<ObjectMapper> {

        @Override
        public ObjectMapper getContext(final Class<?> type) {
            return new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        }
    }
}
