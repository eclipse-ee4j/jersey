/*
 * Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.e2e.server;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import javax.inject.Inject;

import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.internal.inject.DisposableSupplier;
import org.glassfish.jersey.process.internal.RequestScoped;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * JERSEY-2677 reproducer - test, that {@code Factory.dispose()} is correctly called for both sync and async cases.
 *
 * @author Adam Lindenthal (adam.lindenthal at oracle.com)
 */
public class RequestScopedAndAsyncTest extends JerseyTest {

    private static final Logger LOGGER = Logger.getLogger(RequestScopedAndAsyncTest.class.getName());

    // latch to prevent, that the balance is checked before dispose() is called
    private static CountDownLatch cdl;

    public static class Injectable {
        private String message = "Hello";
        public String getMessage() {
            return message;
        }
    }

    public static class InjectableFactory implements DisposableSupplier<Injectable> {
        private static AtomicInteger provided = new AtomicInteger(0);
        private static AtomicInteger balance = new AtomicInteger(0);

        @Override
        public Injectable get() {
            LOGGER.fine("Factory provide() called.");
            provided.incrementAndGet();
            balance.incrementAndGet();
            return new Injectable();
        }

        @Override
        public void dispose(Injectable i) {
            LOGGER.fine("Factory dispose() called. ");
            balance.decrementAndGet();
            cdl.countDown();
        }

        public static void reset() {
            LOGGER.fine("Factory reset() called.");
            provided.set(0);
            balance.set(0);
            cdl = new CountDownLatch(1);
        }

        public static int getProvidedCount() {
            return provided.intValue();
        }

        public static int getBalanceValue() {
            return balance.intValue();
        }
    }

    @Path("test")
    public static class TestResource {

        @Inject
        private Injectable injectable;

        @GET
        @Path("sync")
        public Response sync() {
            LOGGER.fine("Injected message: " + injectable.getMessage());
            return Response.noContent().build();
        }

        @GET
        @Path("async")
        public void async(@Suspended AsyncResponse ar) {
            LOGGER.fine("Injected message: " + injectable.getMessage());
            ar.resume(Response.noContent().build());
        }
    }

    @Before
    public void resetCounters() {
        InjectableFactory.reset();
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(TestResource.class)
                .register(new AbstractBinder() {
                    @Override
                    protected void configure() {
                        bindFactory(InjectableFactory.class)
                                .to(Injectable.class)
                                .in(RequestScoped.class);
                    }
                });
    }

    @Test
    public void testInstanceReleaseAsync() throws ExecutionException, InterruptedException {
        Future<Response> future = target("/test/async").request().async().get();
        Response response = future.get();

        assertEquals(204, response.getStatus());
        assertEquals(1, InjectableFactory.getProvidedCount());
        try {
            cdl.await(500, TimeUnit.MILLISECONDS);
        } catch (final InterruptedException e) {
            LOGGER.log(Level.INFO, "CountDownLatch interrupted: ", e);
        }
        assertEquals(0, InjectableFactory.getBalanceValue());
    }

    @Test
    public void testInstanceReleaseSync() {
        assertEquals(204, target("/test/sync").request().get().getStatus());
        assertEquals(1, InjectableFactory.getProvidedCount());
        assertEquals(0, InjectableFactory.getBalanceValue());
    }

    @Test
    public void shouldProvideAndDisposeSync2() {
        assertEquals(204, target("/test/sync").request().get().getStatus());
        assertEquals(1, InjectableFactory.getProvidedCount());
        assertEquals(0, InjectableFactory.getBalanceValue());
    }

    @Test
    public void shouldProvideAndDisposeAsync2() throws ExecutionException, InterruptedException {
        assertEquals(204, target("/test/async").request().get().getStatus());
        assertEquals(1, InjectableFactory.getProvidedCount());
        try {
            cdl.await(500, TimeUnit.MILLISECONDS);
        } catch (final InterruptedException e) {
            LOGGER.log(Level.INFO, "CountDownLatch interrupted: ", e);
        }
        assertEquals(0, InjectableFactory.getBalanceValue());
    }
}
