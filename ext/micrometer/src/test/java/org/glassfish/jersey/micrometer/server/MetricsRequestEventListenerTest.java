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

import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.MediaType;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.glassfish.jersey.micrometer.server.mapper.ResourceGoneExceptionMapper;
import org.glassfish.jersey.micrometer.server.resources.TestResource;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link MetricsApplicationEventListener}.
 *
 * @author Michael Weirauch
 * @author Johnny Lim
 */
class MetricsRequestEventListenerTest extends JerseyTest {

    static {
        Logger.getLogger("org.glassfish.jersey").setLevel(Level.OFF);
    }

    private static final String METRIC_NAME = "http.server.requests";

    private MeterRegistry registry;

    @Override
    protected Application configure() {
        registry = new SimpleMeterRegistry();

        final MetricsApplicationEventListener listener = new MetricsApplicationEventListener(registry,
                new DefaultJerseyTagsProvider(), METRIC_NAME, true);

        final ResourceConfig config = new ResourceConfig();
        config.register(listener);
        config.register(TestResource.class);
        config.register(ResourceGoneExceptionMapper.class);

        return config;
    }

    @Test
    void resourcesAreTimed() {
        target("/").request().get();
        target("hello").request().get();
        target("hello/").request().get();
        target("hello/peter").request().get();
        target("sub-resource/sub-hello/peter").request().get();

        assertThat(registry.get(METRIC_NAME).tags(tagsFrom("root", "200", "SUCCESS", null)).timer().count())
            .isEqualTo(1);

        assertThat(registry.get(METRIC_NAME).tags(tagsFrom("/hello", "200", "SUCCESS", null)).timer().count())
            .isEqualTo(2);

        assertThat(registry.get(METRIC_NAME).tags(tagsFrom("/hello/{name}", "200", "SUCCESS", null)).timer().count())
            .isEqualTo(1);

        assertThat(registry.get(METRIC_NAME)
            .tags(tagsFrom("/sub-resource/sub-hello/{name}", "200", "SUCCESS", null))
            .timer()
            .count()).isEqualTo(1);

        // assert we are not auto-timing long task @Timed
        assertThat(registry.getMeters()).hasSize(4);
    }

    @Test
    void notFoundIsAccumulatedUnderSameUri() {
        try {
            target("not-found").request().get();
        }
        catch (NotFoundException ignored) {
        }

        assertThat(registry.get(METRIC_NAME).tags(tagsFrom("NOT_FOUND", "404", "CLIENT_ERROR", null)).timer().count())
            .isEqualTo(1);
    }

    @Test
    void notFoundIsReportedWithUriOfMatchedResource() {
        try {
            target("throws-not-found-exception").request().get();
        }
        catch (NotFoundException ignored) {
        }

        assertThat(registry.get(METRIC_NAME)
            .tags(tagsFrom("/throws-not-found-exception", "404", "CLIENT_ERROR", null))
            .timer()
            .count()).isEqualTo(1);
    }

    @Test
    void redirectsAreReportedWithUriOfMatchedResource() {
        target("redirect/302").request().get();
        target("redirect/307").request().get();

        assertThat(registry.get(METRIC_NAME)
            .tags(tagsFrom("/redirect/{status}", "302", "REDIRECTION", null))
            .timer()
            .count()).isEqualTo(1);

        assertThat(registry.get(METRIC_NAME)
            .tags(tagsFrom("/redirect/{status}", "307", "REDIRECTION", null))
            .timer()
            .count()).isEqualTo(1);
    }

    @Test
    void exceptionsAreMappedCorrectly() {
        try {
            target("throws-exception").request().get();
        }
        catch (Exception ignored) {
        }
        try {
            target("throws-webapplication-exception").request().get();
        }
        catch (Exception ignored) {
        }
        try {
            target("throws-mappable-exception").request().get();
        }
        catch (Exception ignored) {
        }
        try {
            target("produces-text-plain").request(MediaType.APPLICATION_JSON).get();
        }
        catch (Exception ignored) {
        }

        assertThat(registry.get(METRIC_NAME)
            .tags(tagsFrom("/throws-exception", "500", "SERVER_ERROR", "IllegalArgumentException"))
            .timer()
            .count()).isEqualTo(1);

        assertThat(registry.get(METRIC_NAME)
            .tags(tagsFrom("/throws-webapplication-exception", "401", "CLIENT_ERROR", "NotAuthorizedException"))
            .timer()
            .count()).isEqualTo(1);

        assertThat(registry.get(METRIC_NAME)
            .tags(tagsFrom("/throws-mappable-exception", "410", "CLIENT_ERROR", "ResourceGoneException"))
            .timer()
            .count()).isEqualTo(1);

        assertThat(registry.get(METRIC_NAME)
                       .tags(tagsFrom("UNKNOWN", "406", "CLIENT_ERROR", "NotAcceptableException"))
                       .timer()
                       .count()).isEqualTo(1);
    }

    private static Iterable<Tag> tagsFrom(String uri, String status, String outcome, String exception) {
        return Tags.of("method", "GET", "uri", uri, "status", status, "outcome", outcome, "exception",
                exception == null ? "None" : exception);
    }

}
