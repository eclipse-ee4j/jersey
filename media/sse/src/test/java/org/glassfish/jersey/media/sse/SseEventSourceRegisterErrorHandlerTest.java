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

package org.glassfish.jersey.media.sse;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.sse.InboundSseEvent;
import jakarta.ws.rs.sse.Sse;
import jakarta.ws.rs.sse.SseEventSink;
import jakarta.ws.rs.sse.SseEventSource;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class SseEventSourceRegisterErrorHandlerTest extends JerseyTest {
    @Path("sse")
    public static class SseEventSourceRegisterTestSseEndpoint {

        @Path("hello")
        @GET
        @Produces(SseFeature.SERVER_SENT_EVENTS)
        public void hello(@Context SseEventSink output, @Context Sse sse) throws InterruptedException {
            output.send(sse.newEvent("HELLO"));
        }

        @Path("close")
        @GET
        @Produces(SseFeature.SERVER_SENT_EVENTS)
        public void close(@Context SseEventSink output, @Context Sse sse) throws InterruptedException {
            output.close();
        }

        @Path("500")
        @GET
        @Produces(SseFeature.SERVER_SENT_EVENTS)
        public void throw500(@Context SseEventSink output, @Context Sse sse) throws InterruptedException {
            throw new WebApplicationException();
        }

        @Path("400")
        @GET
        @Produces(SseFeature.SERVER_SENT_EVENTS)
        public void throw400(@Context SseEventSink output, @Context Sse sse) throws InterruptedException {
            throw new BadRequestException();
        }
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(SseEventSourceRegisterTestSseEndpoint.class);
    }

    private static final Consumer<InboundSseEvent> EMPTY = event -> {
    };

    @Test
    public void testConnection404() throws InterruptedException {
        WebTarget sseTarget = target("sse");
        AtomicReference<Throwable> throwable = new AtomicReference<>();
        CountDownLatch completeLatch = new CountDownLatch(1);

        SseEventSource eventSource = SseEventSource.target(sseTarget).build();
        eventSource.register(EMPTY, throwable::set, completeLatch::countDown);
        eventSource.open();
        completeLatch.await(10_000, TimeUnit.MILLISECONDS);
        MatcherAssert.assertThat(throwable.get(), Matchers.notNullValue());
        MatcherAssert.assertThat(throwable.get().getClass(), Matchers.is(NotFoundException.class));
    }

    @Test
    public void testError500() throws InterruptedException {
        WebTarget sseTarget = target("sse/500");
        AtomicReference<Throwable> throwable = new AtomicReference<>();
        CountDownLatch completeLatch = new CountDownLatch(1);

        SseEventSource eventSource = SseEventSource.target(sseTarget).build();
        eventSource.register(EMPTY, throwable::set, completeLatch::countDown);
        eventSource.open();
        completeLatch.await(10_000, TimeUnit.MILLISECONDS);
        MatcherAssert.assertThat(throwable.get(), Matchers.notNullValue());
        MatcherAssert.assertThat(throwable.get().getClass(), Matchers.is(InternalServerErrorException.class));
    }

    @Test
    public void testError400() throws InterruptedException {
        WebTarget sseTarget = target("sse/400");
        AtomicReference<Throwable> throwable = new AtomicReference<>();
        CountDownLatch completeLatch = new CountDownLatch(1);

        SseEventSource eventSource = SseEventSource.target(sseTarget).build();
        eventSource.register(EMPTY, throwable::set, completeLatch::countDown);
        eventSource.open();
        completeLatch.await(10_000, TimeUnit.MILLISECONDS);
        MatcherAssert.assertThat(throwable.get(), Matchers.notNullValue());
        MatcherAssert.assertThat(throwable.get().getClass(), Matchers.is(BadRequestException.class));
    }
}
