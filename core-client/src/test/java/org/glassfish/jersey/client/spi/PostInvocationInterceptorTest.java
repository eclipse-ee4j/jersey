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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.InvocationCallback;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public class PostInvocationInterceptorTest {
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
    public void testSyncNoConnectionPostInvocationInterceptor() {
        final Invocation.Builder builder = ClientBuilder.newBuilder()
                .register(new CounterPostInvocationInterceptor((a, b) -> false, (a, b) -> true))
                .build().target(URL).request();
        try (Response r = builder.get()) {
            Assert.fail();
        } catch (ProcessingException pe) {
            Assert.assertEquals(1000, counter.get());
            Assert.assertEquals(ConnectException.class, pe.getCause().getClass());
        }
    }

    @Test
    public void testPreThrowsPostFixes() {
        final Invocation.Builder builder = ClientBuilder.newBuilder()
                .register(new CounterPreInvocationInterceptor(a -> { throw new IllegalStateException(); }))
                .register(new CounterPostInvocationInterceptor(
                        (a, b) -> false,
                        (a, b) -> {
                            b.resolve(Response.accepted().build());
                            return true;
                        }))
                .build().target(URL).request();
        try (Response response = builder.get()) {
            Assert.assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatus());
            Assert.assertEquals(1000, counter.get()); // counter.increment would be after ISE
        }
    }

    @Test
    public void testPreThrowsPostFixesAsync() throws ExecutionException, InterruptedException {
        final Invocation.Builder builder = ClientBuilder.newBuilder()
                .register(new CounterPreInvocationInterceptor(a -> { throw new IllegalStateException(); }))
                .register(new CounterPostInvocationInterceptor(
                        (a, b) -> false,
                        (a, b) -> {
                            b.resolve(Response.accepted().build());
                            return true;
                        }))
                .build().target(URL).request();
        try (Response response = builder.async().get().get()) {
            Assert.assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatus());
            Assert.assertEquals(1000, counter.get()); // counter.increment would be after ISE
        }
    }

    @Test
    public void testFilterThrowsPostFixesAsync() throws ExecutionException, InterruptedException {
        final ClientRequestFilter filter = (requestContext) -> {throw new IllegalStateException(); };
        final Invocation.Builder builder = ClientBuilder.newBuilder()
                .register(filter)
                .register(new CounterPostInvocationInterceptor(
                        (a, b) -> false,
                        (a, b) -> {
                            b.resolve(Response.accepted().build());
                            return true;
                        }))
                .build().target(URL).request();
        try (Response response = builder.async()
                .get(new TestInvocationCallback(a -> a.getStatus() == Response.Status.ACCEPTED.getStatusCode(), a -> false))
                .get()) {
            Assert.assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatus());
            Assert.assertEquals(1000, counter.get()); // counter.increment would be after ISE
        }
    }

    @Test
    public void testPostThrowsFixesThrowsFixes() {
        final Invocation.Builder builder = ClientBuilder.newBuilder()
                .register(AbortRequestFilter.class)
                .register(new CounterPostInvocationInterceptor(
                        (a, b) -> { throw new IllegalStateException(); },
                        (a, b) -> false) {},
                        100)
                .register(new CounterPostInvocationInterceptor(
                        (a, b) -> false,
                        (a, b) -> { if (b.getThrowables().getFirst().getClass() == IllegalStateException.class) {
                                        b.resolve(Response.accepted().build());
                                    }
                                    return true; }) {},
                        200)
                .register(new CounterPostInvocationInterceptor(
                        (a, b) -> { if (b.getStatus() == Response.Status.ACCEPTED.getStatusCode()) {
                                        throw new IllegalArgumentException();
                                    }
                                    return false; },
                        (a, b) -> false) {},
                        300)
                .register(new CounterPostInvocationInterceptor(
                        (a, b) -> false,
                        (a, b) -> { if (b.getThrowables().getFirst().getClass() == IllegalArgumentException.class) {
                                        b.resolve(Response.noContent().build());
                                    }
                                    return true; }) {},
                        400)
                .build().target(URL).request();
        try (Response response = builder.get()) {
            Assert.assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());
            Assert.assertEquals(2000, counter.get());
        }
    }

    @Test
    public void testMultipleResolvesThrows() {
        final Invocation.Builder builder = ClientBuilder.newBuilder()
                .register(AbortRequestFilter.class)
                .register(new CounterPostInvocationInterceptor(
                        (a, b) -> { throw new IllegalStateException(); },
                        (a, b) -> false) {},
                        100)
                .register(new CounterPostInvocationInterceptor(
                        (a, b) -> false,
                        (a, b) -> {
                          b.resolve(Response.ok().build());
                          b.resolve(Response.ok().build());
                          return true;
                        }) {},
                        200)
                .build().target(URL).request();
        try (Response response = builder.get()) {
            Assert.fail();
        } catch (IllegalStateException pe) {
           // expected
        }
    }

    @Test
    public void testPostChangesStatusAndEntity() {
        final Invocation.Builder builder = ClientBuilder.newBuilder()
                .register(AbortRequestFilter.class)
                .register(new CounterPostInvocationInterceptor(
                        (a, b) -> {
                            b.setStatus(Response.Status.CONFLICT.getStatusCode());
                            b.setEntityStream(new ByteArrayInputStream("HELLO".getBytes()));
                            return true;
                        },
                        (a, b) -> false))
                .build().target(URL).request();
        try (Response response = builder.get()) {
            Assert.assertEquals(Response.Status.CONFLICT.getStatusCode(), response.getStatus());
            Assert.assertEquals(1, counter.get());
            Assert.assertEquals("HELLO", response.readEntity(String.class));
        }
    }

    @Test
    public void testPostOnExceptionWhenNoThrowableAndNoResponseContext() {
        final Invocation.Builder builder = ClientBuilder.newBuilder()
                .register(new CounterPostInvocationInterceptor(
                                  (a, b) -> false,
                                  (a, b) -> {
                                      b.getThrowables().clear();
                                      return true;
                                  }) {
                          },
                        200)
                .register(new CounterPostInvocationInterceptor(
                                  (a, b) -> false,
                                  (a, b) -> {
                                      b.resolve(Response.accepted().build());
                                      return true;
                                  }) {
                          },
                        300)
                .build().target(URL).request();
        try (Response response = builder.get()) {
            Assert.assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatus());
            Assert.assertEquals(2000, counter.get());
        }
    }

    @Test
    public void testAsyncNoConnectionPostInvocationInterceptor() throws InterruptedException {
        final Invocation.Builder builder = ClientBuilder.newBuilder()
                .register(new CounterPostInvocationInterceptor((a, b) -> false, (a, b) -> true))
                .build().target(URL).request();
        try (Response r = builder.async().get(new TestInvocationCallback(a -> false, a -> true)).get()) {
            Assert.fail();
        } catch (ExecutionException ee) {
            Assert.assertEquals(1000, counter.get());
            Assert.assertEquals(ProcessingException.class, ee.getCause().getClass());
            Assert.assertEquals(ConnectException.class, ee.getCause().getCause().getClass());
        }
    }

    @Test
    public void testPreThrowsPostResolves() {
        final Invocation.Builder builder = ClientBuilder.newBuilder()
                .register(new CounterPreInvocationInterceptor(a -> { throw new IllegalArgumentException(); }) {})
                .register(new CounterPreInvocationInterceptor(a -> { throw new IllegalStateException(); }) {})
                .register(new CounterPostInvocationInterceptor(
                        (a, b) -> false,
                        (a, b) -> {
                            b.resolve(Response.accepted().build());
                            return b.getThrowables().size() == 2;
                        }))
                .build().target(URL).request();
        try (Response response = builder.get()) {
            Assert.assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatus());
        }
    }

    @Test
    public void testPostInvocationInterceptorIsHitforEachRequest() {
        final Invocation.Builder builder = ClientBuilder.newBuilder()
                .register(new CounterPostInvocationInterceptor((a, b) -> true, (a, b) -> false))
                .register(new AbortRequestFilter()).build().target(URL).request();
        for (int i = 1; i != 10; i++) {
            try (Response response = builder.get()) {
                Assert.assertEquals(200, response.getStatus());
                Assert.assertEquals(i, counter.get());
            }
        }
    }

    private static class TestInvocationCallback implements InvocationCallback<Response> {
        private final Predicate<Response> responsePredicate;
        private final Predicate<Throwable> throwablePredicate;

        private TestInvocationCallback(Predicate<Response> responsePredicate, Predicate<Throwable> throwablePredicate) {
            this.responsePredicate = responsePredicate;
            this.throwablePredicate = throwablePredicate;
        }

        @Override
        public void completed(Response response) {
            Assert.assertTrue(responsePredicate.test(response));
        }

        @Override
        public void failed(Throwable throwable) {
            Assert.assertTrue(throwablePredicate.test(throwable));
        }
    }

    private class CounterPostInvocationInterceptor implements PostInvocationInterceptor {
        private final BiPredicate<ClientRequestContext, ClientResponseContext> afterRequest;
        private final BiPredicate<ClientRequestContext, ExceptionContext> onException;

        private CounterPostInvocationInterceptor(BiPredicate<ClientRequestContext, ClientResponseContext> afterRequest,
                                                 BiPredicate<ClientRequestContext, ExceptionContext> onException) {
            this.afterRequest = afterRequest;
            this.onException = onException;
        }

        @Override
        public void afterRequest(ClientRequestContext requestContext, ClientResponseContext responseContext) {
            Assert.assertTrue(afterRequest.test(requestContext, responseContext));
            counter.getAndIncrement();
        }

        @Override
        public void onException(ClientRequestContext requestContext, ExceptionContext exceptionContext) {
            Assert.assertTrue(onException.test(requestContext, exceptionContext));
            counter.addAndGet(1000);
        }
    }

    private class CounterPreInvocationInterceptor implements PreInvocationInterceptor {
        private final Predicate<ClientRequestContext> predicate;

        private CounterPreInvocationInterceptor(Predicate<ClientRequestContext> predicate) {
            this.predicate = predicate;
        }

        @Override
        public void beforeRequest(ClientRequestContext requestContext) {
            Assert.assertTrue(predicate.test(requestContext));
            counter.incrementAndGet();
        }
    }

    private static class AbortRequestFilter implements ClientRequestFilter {
        @Override
        public void filter(ClientRequestContext requestContext) throws IOException {
            requestContext.abortWith(Response.ok().build());
        }
    }
}
