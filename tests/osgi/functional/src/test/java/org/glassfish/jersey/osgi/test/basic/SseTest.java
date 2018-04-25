/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.osgi.test.basic;

import java.io.IOException;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.UriBuilder;

import javax.inject.Inject;

import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.media.sse.EventOutput;
import org.glassfish.jersey.media.sse.EventSource;
import org.glassfish.jersey.media.sse.InboundEvent;
import org.glassfish.jersey.media.sse.OutboundEvent;
import org.glassfish.jersey.media.sse.SseFeature;
import org.glassfish.jersey.osgi.test.util.Helper;
import org.glassfish.jersey.server.ResourceConfig;

import org.glassfish.grizzly.http.server.HttpServer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.osgi.framework.BundleContext;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;

/**
 * Basic test for SSE module OSGification.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
@RunWith(PaxExam.class)
public class SseTest {

    private static final String CONTEXT = "/jersey";

    private static final URI baseUri = UriBuilder
            .fromUri("http://localhost")
            .port(Helper.getPort())
            .path(CONTEXT).build();

    @Inject
    protected BundleContext bundleContext;

    @Configuration
    public static Option[] configuration() {
        List<Option> options = Helper.getCommonOsgiOptions();

        options.addAll(Helper.expandedList(
                // Jersey SSE dependencies
                mavenBundle().groupId("org.glassfish.jersey.media").artifactId("jersey-media-sse").versionAsInProject()));

        options = Helper.addPaxExamMavenLocalRepositoryProperty(options);
        return Helper.asArray(options);
    }

    @Path("/sse")
    public static class SseResource {

        @GET
        @Produces(SseFeature.SERVER_SENT_EVENTS)
        public EventOutput getIt() throws IOException {
            final EventOutput result = new EventOutput();
            result.write(new OutboundEvent.Builder().name("event1").data(String.class, "ping").build());
            result.write(new OutboundEvent.Builder().name("event2").data(String.class, "pong").build());
            result.close();
            return result;
        }
    }

    @Test
    public void testSse() throws Exception {
        final ResourceConfig resourceConfig = new ResourceConfig(SseResource.class, SseFeature.class);
        final HttpServer server = GrizzlyHttpServerFactory.createHttpServer(baseUri, resourceConfig);

        Client c = ClientBuilder.newClient();
        c.register(SseFeature.class);

        final List<String> data = new LinkedList<String>();
        final CountDownLatch latch = new CountDownLatch(2);

        final EventSource eventSource = new EventSource(c.target(baseUri).path("/sse")) {

            @Override
            public void onEvent(InboundEvent event) {
                try {
                    data.add(event.readData());
                    latch.countDown();
                } catch (ProcessingException e) {
                    // ignore
                }
            }
        };

        assertTrue(latch.await(2, TimeUnit.SECONDS));

        eventSource.close();
        assertEquals(2, data.size());

        server.shutdownNow();
    }
}
