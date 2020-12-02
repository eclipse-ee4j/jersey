/*
 * Copyright (c) 2019, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.client.spi;

import org.glassfish.jersey.spi.ThreadPoolExecutorProvider;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Priority;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

public class PreInvocationInterceptorTest {
    private static final String URL = "http://localhost:8080";
    private static final String PROPERTY_NAME = "property_name";
    private static final String PROPERTY_VALUE = "property_value";
    private static final String EXECUTOR_THREAD_NAME = "custom-executor-name";

    private AtomicInteger counter;

    @Before
    public void setup() {
        counter = new AtomicInteger();
    }

    @Test
    public void testPreInvocationInterceptorExecutedWhenBuilderBuild() {
        final Invocation.Builder builder = ClientBuilder.newBuilder()
                .register(new CounterPreInvocationInterceptor(a -> a.get() == 0))
                .register(new CounterRequestFilter(a -> a.get() == 1))
                .register(AbortRequestFilter.class).build().target(URL).request();
        try (Response response = builder.build("GET").invoke()) {
            Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        }
    }

    @Test
    public void testPreInvocationInterceptorExecutedWhenMethodGET() {
        final Invocation.Builder builder = ClientBuilder.newBuilder()
                .register(new CounterPreInvocationInterceptor(a -> a.get() == 0))
                .register(new CounterRequestFilter(a -> a.get() == 1))
                .register(AbortRequestFilter.class).build().target(URL).request();
        try (Response response = builder.method("GET")) {
            Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        }
    }

    @Test
    public void testPreInvocationInterceptorPropertySet() {
        final Invocation.Builder builder = ClientBuilder.newBuilder()
                .register(new PropertyPreInvocationInterceptor(a -> PROPERTY_VALUE.equals(a.getProperty(PROPERTY_NAME))))
                .register(new PropertyRequestFilter(a -> PROPERTY_VALUE.equals(a.getProperty(PROPERTY_NAME))))
                .register(AbortRequestFilter.class).build().target(URL).request().property(PROPERTY_NAME, PROPERTY_VALUE);
        try (Response response = builder.method("GET")) {
            Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        }
    }

    @Test
    public void testPreInvocationInterceptorHasInjection() {
        final Invocation.Builder builder = ClientBuilder.newBuilder()
                .property(PROPERTY_NAME, PROPERTY_VALUE)
                .register(InjectedPreInvocationInterceptor.class)
                .register(AbortRequestFilter.class).build().target(URL).request();
        try (Response response = builder.method("GET")) {
            Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        }
    }

    @Test
    public void testPreInvocationInterceptorInTheSameThreadInAsync() throws ExecutionException, InterruptedException {
        final String currentThreadName = Thread.currentThread().getName();
        final Invocation.Builder builder = ClientBuilder.newBuilder()
                .executorService(new CustomClientExecutorProvider().getExecutorService())
                .register(new PropertyPreInvocationInterceptor(a -> currentThreadName.equals(Thread.currentThread().getName())))
                .register(new PropertyRequestFilter(a -> Thread.currentThread().getName().startsWith(EXECUTOR_THREAD_NAME)))
                .register(AbortRequestFilter.class).build().target(URL).request();
        try (Response response = builder.async().method("GET").get()) {
            Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        }
    }

    @Test
    public void testPreInvocationInterceptorInTheSameThreadInJerseyRx() throws ExecutionException, InterruptedException {
        final String currentThreadName = Thread.currentThread().getName();
        final Invocation.Builder builder = ClientBuilder.newBuilder()
                .executorService(new CustomClientExecutorProvider().getExecutorService())
                .register(new PropertyPreInvocationInterceptor(a -> currentThreadName.equals(Thread.currentThread().getName())))
                .register(new PropertyRequestFilter(a -> Thread.currentThread().getName().startsWith(EXECUTOR_THREAD_NAME)))
                .register(AbortRequestFilter.class).build().target(URL).request();
        try (Response response = builder.rx().method("GET").toCompletableFuture().get()) {
            Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        }
    }

    @Test
    public void testPreInvocationInterceptorAbortWith() {
        final Invocation.Builder builder = ClientBuilder.newBuilder()
                .executorService(new CustomClientExecutorProvider().getExecutorService())
                .register(new PropertyPreInvocationInterceptor(a -> {
                    a.abortWith(Response.noContent().build());
                    return true;
                }))
                .register(new PropertyRequestFilter(a -> {throw new IllegalStateException(); }))
                .register(AbortRequestFilter.class).build().target(URL).request();
        try (Response response = builder.get()) {
            Assert.assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());
        }
    }

    @Test
    public void testPreInvocationInterceptorAbortWithThrowsInMultiple() {
        final Invocation.Builder builder = ClientBuilder.newBuilder()
                .executorService(new CustomClientExecutorProvider().getExecutorService())
                .register(
                        new PropertyPreInvocationInterceptor(a -> {
                            a.abortWith(Response.noContent().build());
                            return true;
                        }) {},
                        200
                )
                .register(
                        new PropertyPreInvocationInterceptor(a -> {
                            a.abortWith(Response.accepted().build());
                            return true;
                        }) {},
                        100
                )
                .register(new PropertyRequestFilter(a -> {throw new IllegalStateException(); }))
                .register(AbortRequestFilter.class).build().target(URL).request();
        try (Response response = builder.get()) {
            Assert.fail();
        } catch (ProcessingException exception) {
            Assert.assertEquals(IllegalStateException.class, exception.getCause().getClass());
        }
    }

    @Test
    public void testPrioritiesOnPreInvocationInterceptor() {
        final Invocation.Builder builder = ClientBuilder.newBuilder()
                .executorService(new CustomClientExecutorProvider().getExecutorService())
                .register(new Priority200PreInvocationInterceptor(a -> a.get() < 2){})
                .register(new Priority100PreInvocationInterceptor(a -> a.getAndIncrement() == 2))
                .register(new Priority200PreInvocationInterceptor(a -> a.get() < 2){})
                .register(AbortRequestFilter.class).build().target(URL).request();
        try (Response response = builder.get()) {
            Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        }
    }

    @Test
    public void testMultiExceptionInPreInvocationInterceptor() {
        final Invocation.Builder builder = ClientBuilder.newBuilder()
                .executorService(new CustomClientExecutorProvider().getExecutorService())
                .register(new Priority200PreInvocationInterceptor(a -> {throw new RuntimeException("ONE"); }))
                .register(new Priority100PreInvocationInterceptor(a -> {throw new RuntimeException("TWO"); }))
                .register(AbortRequestFilter.class).build().target(URL).request();
        try (Response response = builder.get()) {
            Assert.fail();
        } catch (RuntimeException e) {
            Assert.assertEquals("ONE", e.getSuppressed()[0].getMessage());
            Assert.assertEquals("TWO", e.getSuppressed()[1].getMessage());
        }
    }

    @Test
    public void testPreInvocationInterceptorIsHitforEachRequest() {
        final Invocation.Builder builder = ClientBuilder.newBuilder()
                .register(new CounterPreInvocationInterceptor(counter -> true))
                .register(new AbortRequestFilter()).build().target(URL).request();
        for (int i = 1; i != 10; i++) {
            try (Response response = builder.get()) {
                Assert.assertEquals(200, response.getStatus());
                Assert.assertEquals(i, counter.get());
            }
        }
    }

    private static class AbortRequestFilter implements ClientRequestFilter {
        @Override
        public void filter(ClientRequestContext requestContext) throws IOException {
            requestContext.abortWith(Response.ok().build());
        }
    }

    private class CounterRequestFilter implements ClientRequestFilter {
        private final Predicate<AtomicInteger> consumer;

        private CounterRequestFilter(Predicate<AtomicInteger> consumer) {
            this.consumer = consumer;
        }

        @Override
        public void filter(ClientRequestContext requestContext) throws IOException {
            Assert.assertTrue(consumer.test(counter));
            counter.getAndIncrement();
        }
    }

    private class CounterPreInvocationInterceptor implements PreInvocationInterceptor {
        private final Predicate<AtomicInteger> predicate;

        private CounterPreInvocationInterceptor(Predicate<AtomicInteger> predicate) {
            this.predicate = predicate;
        }

        @Override
        public void beforeRequest(ClientRequestContext requestContext) {
            Assert.assertTrue(predicate.test(counter));
            counter.getAndIncrement();
        }
    }

    private static class PropertyRequestFilter implements ClientRequestFilter {
        private final Predicate<ClientRequestContext> predicate;

        private PropertyRequestFilter(Predicate<ClientRequestContext> predicate) {
            this.predicate = predicate;
        }

        @Override
        public void filter(ClientRequestContext requestContext) throws IOException {
            Assert.assertTrue(predicate.test(requestContext));
        }
    }

    private static class PropertyPreInvocationInterceptor implements PreInvocationInterceptor {
        private final Predicate<ClientRequestContext> predicate;

        private PropertyPreInvocationInterceptor(Predicate<ClientRequestContext> predicate) {
            this.predicate = predicate;
        }

        @Override
        public void beforeRequest(ClientRequestContext requestContext) {
            Assert.assertTrue(predicate.test(requestContext));
        }
    }

    private static class InjectedPreInvocationInterceptor implements PreInvocationInterceptor {
        @Context
        Configuration configuration;

        @Override
        public void beforeRequest(ClientRequestContext requestContext) {
            Assert.assertNotNull(configuration);
            Assert.assertEquals(PROPERTY_VALUE, configuration.getProperty(PROPERTY_NAME));
        }
    }

    @Priority(100)
    private class Priority100PreInvocationInterceptor extends CounterPreInvocationInterceptor {
        private Priority100PreInvocationInterceptor(Predicate<AtomicInteger> predicate) {
            super(predicate);
        }
    }

    @Priority(200)
    private class Priority200PreInvocationInterceptor extends CounterPreInvocationInterceptor {
        private Priority200PreInvocationInterceptor(Predicate<AtomicInteger> predicate) {
            super(predicate);
        }
    }

    private static class CustomClientExecutorProvider extends ThreadPoolExecutorProvider {
        CustomClientExecutorProvider() {
            super(EXECUTOR_THREAD_NAME);
        }
    }
}
