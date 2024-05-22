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

package org.glassfish.jersey.tests.integration.tracing;

import org.glassfish.jersey.message.internal.TracingLogger;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.server.TracingConfig;
import org.glassfish.jersey.server.internal.ServerTraceEvent;
import org.glassfish.jersey.test.JerseyTest;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class TracingMatchResourceMethodTest extends JerseyTest {
    GatheringHandler handler = new GatheringHandler();
    Logger logger;

    @Path("/echo")
    public static class TracingMatchResourceMethodResource {
        @Path("echo")
        @POST
        public String echo(String entity) {
            return entity;
        }
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(TracingMatchResourceMethodResource.class)
                .property(ServerProperties.TRACING, TracingConfig.ALL.name())
                .property(ServerProperties.TRACING_THRESHOLD, TracingLogger.Level.VERBOSE.name());
    }

    @Test
    public void testEcho() {
        logger = Logger.getLogger("org.glassfish.jersey.tracing.general");
        logger.addHandler(handler);

        try (Response r = target("echo").path("echo").request().post(Entity.entity("ECHO", MediaType.TEXT_PLAIN_TYPE))) {
            MatcherAssert.assertThat(r.getStatus(), Matchers.equalTo(200));
            MatcherAssert.assertThat(r.readEntity(String.class), Matchers.equalTo("ECHO"));
        }

        List<LogRecord> matched = handler.logRecords.stream()
                .filter(logRecord -> logRecord.getMessage().startsWith(ServerTraceEvent.MATCH_RESOURCE_METHOD.name()))
                .collect(Collectors.toList());
        MatcherAssert.assertThat(matched.size(), Matchers.equalTo(1));
    }

    private static class GatheringHandler extends Handler {

        List<LogRecord> logRecords = new ArrayList<>();

        @Override
        public void publish(LogRecord record) {
            logRecords.add(record);
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }
    }
}
