/*
 * Copyright (c) 2017, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.client;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.RxInvokerProvider;
import javax.ws.rs.client.SyncInvoker;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import org.glassfish.jersey.internal.guava.ThreadFactoryBuilder;

import org.glassfish.jersey.spi.ExecutorServiceProvider;
import org.hamcrest.core.AllOf;
import org.hamcrest.core.StringContains;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Sanity test for {@link Invocation.Builder#rx()} methods.
 *
 * @author Pavel Bucek
 */
public class ClientRxTest {

    private static final ExecutorService EXECUTOR_SERVICE =
            Executors.newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat("rxTest-%d").build());

    private final Client CLIENT;
    private final Client CLIENT_WITH_EXECUTOR;

    public ClientRxTest() {
        CLIENT = ClientBuilder.newClient();
        CLIENT_WITH_EXECUTOR = ClientBuilder.newBuilder().executorService(EXECUTOR_SERVICE).build();
    }

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @After
    public void afterTest() {
        CLIENT.close();
        CLIENT_WITH_EXECUTOR.close();
    }

    @AfterClass
    public static void afterClass() {
        EXECUTOR_SERVICE.shutdownNow();
    }

    @Test
    public void testRxInvoker() {
        // explicit register is not necessary, but it can be used.
        CLIENT.register(TestRxInvokerProvider.class, RxInvokerProvider.class);

        String s = target(CLIENT).request().rx(TestRxInvoker.class).get();

        assertTrue("Provided RxInvoker was not used.", s.startsWith("rxTestInvoker"));
    }

    @Test
    public void testRxInvokerWithExecutor() {
        // implicit register (not saying that the contract is RxInvokerProvider).
        String s = target(CLIENT_WITH_EXECUTOR).register(TestRxInvokerProvider.class).request().rx(TestRxInvoker.class).get();

        assertTrue("Provided RxInvoker was not used.", s.startsWith("rxTestInvoker"));
        assertTrue("Executor Service was not passed to RxInvoker", s.contains("rxTest-"));
    }

    @Test
    public void testDefaultRxInvokerWithExecutor() throws ExecutionException, InterruptedException {
        AtomicReference<String> threadName = new AtomicReference<>();
        ClientRequestFilter threadFilter = (f) -> { threadName.set(Thread.currentThread().getName()); };
        ClientRequestFilter abortFilter = (f) -> { f.abortWith(Response.ok().build()); };
        try (Response r = target(CLIENT_WITH_EXECUTOR)
                .register(threadFilter, 100)
                .register(abortFilter, 200)
                .request().rx().get().toCompletableFuture().get()) {

            assertEquals(200, r.getStatus());
            assertTrue("Executor Service was not passed to RxInvoker", threadName.get().contains("rxTest-"));
        }
    }

    @Test
    public void testRxInvokerWithExecutorServiceProvider() {
        AtomicReference<String> threadName = new AtomicReference<>();
        String s = target(CLIENT)
                .register(TestRxInvokerProvider.class, 200)
                .register(TestExecutorServiceProvider.class)
                .request().rx(TestRxInvoker.class).get();

        assertTrue("Provided RxInvoker was not used.", s.startsWith("rxTestInvoker"));
        assertTrue("Executor Service was not passed to RxInvoker", s.contains("rxTest-"));
    }

    @Test
    public void testDefaultRxInvokerWithExecutorServiceProvider() throws ExecutionException, InterruptedException {
        AtomicReference<String> threadName = new AtomicReference<>();
        ClientRequestFilter threadFilter = (f) -> { threadName.set(Thread.currentThread().getName()); };
        ClientRequestFilter abortFilter = (f) -> { f.abortWith(Response.ok().build()); };
        try (Response r = target(CLIENT)
                .register(threadFilter, 100)
                .register(abortFilter, 200)
                .register(TestExecutorServiceProvider.class)
                .request().rx().get().toCompletableFuture().get()) {

            assertEquals(200, r.getStatus());
            assertTrue("Executor Service was not passed to RxInvoker", threadName.get().contains("rxTest-"));
        }
    }

    @Test
    public void testRxInvokerInvalid() {
        Invocation.Builder request = target(CLIENT).request();
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage(AllOf.allOf(new StringContains("null"), new StringContains("clazz")));
        request.rx(null).get();
    }

    @Test
    public void testRxInvokerNotRegistered() {
        Invocation.Builder request = target(CLIENT).request();
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage(AllOf.allOf(
                new StringContains("TestRxInvoker"),
                new StringContains("not registered"),
                new StringContains("RxInvokerProvider")));
        request.rx(TestRxInvoker.class).get();
    }

    private WebTarget target(Client client) {
        // Uri is not relevant, the call won't be ever executed.
        return client.target("http://localhost:9999");
    }

    @Provider
    public static class TestRxInvokerProvider implements RxInvokerProvider<TestRxInvoker> {
        @Override
        public TestRxInvoker getRxInvoker(SyncInvoker syncInvoker, ExecutorService executorService) {
            return new TestRxInvoker(syncInvoker, executorService);
        }

        @Override
        public boolean isProviderFor(Class<?> clazz) {
            return TestRxInvoker.class.equals(clazz);
        }
    }

    private static class TestRxInvoker extends AbstractRxInvoker<String> {

        private TestRxInvoker(SyncInvoker syncInvoker, ExecutorService executor) {
            super(syncInvoker, executor);
        }

        @Override
        public <R> String method(String name, Entity<?> entity, Class<R> responseType) {
            return "rxTestInvoker" + (getExecutorService() == null ? "" : " rxTest-");
        }

        @Override
        public <R> String method(String name, Entity<?> entity, GenericType<R> responseType) {
            return "rxTestInvoker" + (getExecutorService() == null ? "" : " rxTest-");
        }
    }

    private static class TestExecutorServiceProvider implements ExecutorServiceProvider {

        @Override
        public ExecutorService getExecutorService() {
            return EXECUTOR_SERVICE;
        }

        @Override
        public void dispose(ExecutorService executorService) {
            //@After
        }
    }
}
