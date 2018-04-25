/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Test;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author Pavel Bucek (pavel.bucek at oracle.com)
 */
public class CompletionStageTest extends JerseyTest {

    static final String ENTITY = "entity";
    // delay of async operations in seconds.
    static final int DELAY = 1;

    @Override
    protected Application configure() {
        return new ResourceConfig(CompletionStageResource.class);
    }

    @Test
    public void testGetCompleted() {
        Response response = target("cs/completed").request().get();

        assertThat(response.getStatus(), is(200));
        assertThat(response.readEntity(String.class), is(ENTITY));
    }

    @Test
    public void testGetException400() {
        Response response = target("cs/exception400").request().get();

        assertThat(response.getStatus(), is(400));
    }

    @Test
    public void testGetException405() {
        Response response = target("cs/exception405").request().get();

        assertThat(response.getStatus(), is(405));
    }

    @Test
    public void testGetCancelled() {
        Response response = target("cs/cancelled").request().get();

        assertThat(response.getStatus(), is(503));
    }

    @Test
    public void testGetCompletedAsync() {
        Response response = target("cs/completedAsync").request().get();

        assertThat(response.getStatus(), is(200));
        assertThat(response.readEntity(String.class), is(ENTITY));
    }

    @Test
    public void testGetException400Async() {
        Response response = target("cs/exception400Async").request().get();

        assertThat(response.getStatus(), is(400));
    }

    @Test
    public void testGetException405Async() {
        Response response = target("cs/exception405Async").request().get();

        assertThat(response.getStatus(), is(405));
    }

    @Test
    public void testGetCancelledAsync() {
        Response response = target("cs/cancelledAsync").request().get();

        assertThat(response.getStatus(), is(503));
    }

    @Test
    public void testGetCustomCompleted() {
        Response response = target("cs/custom").request().get();

        assertThat(response.getStatus(), is(200));
        assertThat(response.readEntity(String.class), is(ENTITY));
    }

    @Test
    public void testGetCustomAsync() {
        Response response = target("cs/customAsync").request().get();

        assertThat(response.getStatus(), is(200));
        assertThat(response.readEntity(String.class), is(ENTITY));
    }

    @Path("/cs")
    public static class CompletionStageResource {

        private static final ExecutorService EXECUTOR_SERVICE = Executors.newCachedThreadPool();

        @GET
        @Path("/completed")
        public CompletionStage<String> getCompleted() {
            return CompletableFuture.completedFuture(ENTITY);
        }

        @GET
        @Path("/exception400")
        public CompletionStage<String> getException400() {
            CompletableFuture<String> cs = new CompletableFuture<>();
            cs.completeExceptionally(new WebApplicationException(400));

            return cs;
        }

        @GET
        @Path("/exception405")
        public CompletionStage<String> getException405() {
            CompletableFuture<String> cs = new CompletableFuture<>();
            cs.completeExceptionally(new WebApplicationException(405));

            return cs;
        }

        @GET
        @Path("/cancelled")
        public CompletionStage<String> getCancelled() {
            CompletableFuture<String> cs = new CompletableFuture<>();
            cs.cancel(true);

            return cs;
        }

        @GET
        @Path("/completedAsync")
        public CompletionStage<String> getCompletedAsync() {
            CompletableFuture<String> cs = new CompletableFuture<>();
            delaySubmit(() -> cs.complete(ENTITY));
            return cs;
        }

        @GET
        @Path("/exception400Async")
        public CompletionStage<String> getException400Async() {
            CompletableFuture<String> cs = new CompletableFuture<>();
            delaySubmit(() -> cs.completeExceptionally(new WebApplicationException(400)));

            return cs;
        }

        @GET
        @Path("/exception405Async")
        public CompletionStage<String> getException405Async() {
            CompletableFuture<String> cs = new CompletableFuture<>();
            delaySubmit(() -> cs.completeExceptionally(new WebApplicationException(405)));

            return cs;
        }

        @GET
        @Path("/cancelledAsync")
        public CompletionStage<String> getCancelledAsync() {
            CompletableFuture<String> cs = new CompletableFuture<>();
            delaySubmit(() -> cs.cancel(true));

            return cs;
        }

        /**
         * Return completed CompletionStage which doesn't support #toCompletableFuture().
         *
         * @return CompletionStage which doesn't support #toCompletableFuture().
         */
        @GET
        @Path("/custom")
        public CompletionStage<String> getCustomCompletionStage() {
            return new CustomCompletionStage<>(CompletableFuture.completedFuture(ENTITY));
        }

        /**
         * Return uncompleted CompletionStage which doesn't support #toCompletableFuture().
         *
         * @return CompletionStage which doesn't support #toCompletableFuture().
         */
        @GET
        @Path("/customAsync")
        public CompletionStage<String> getCustomCompletionStageAsync() {
            CompletableFuture<String> cf = new CompletableFuture<>();
            CustomCompletionStage<String> cs = new CustomCompletionStage<>(cf);
            delaySubmit(() -> cf.complete(ENTITY));

            return cs;
        }

        private void delaySubmit(Runnable runnable) {
            EXECUTOR_SERVICE.submit(() -> {
                try {
                    Thread.sleep(DELAY * 1000);
                } catch (InterruptedException e) {
                    // ignore
                }

                runnable.run();
            });
        }
    }

    private static class CustomCompletionStage<T> implements CompletionStage<T> {

        private final CompletionStage<T> completedFuture;

        CustomCompletionStage(CompletionStage<T> completedFuture) {
            this.completedFuture = completedFuture;
        }

        @Override
        public <U> CompletionStage<U> thenApply(Function<? super T, ? extends U> fn) {
            return completedFuture.thenApply(fn);
        }

        @Override
        public <U> CompletionStage<U> thenApplyAsync(Function<? super T, ? extends U> fn) {
            return completedFuture.thenApplyAsync(fn);
        }

        @Override
        public <U> CompletionStage<U> thenApplyAsync(Function<? super T, ? extends U> fn, Executor executor) {
            return completedFuture.thenApplyAsync(fn, executor);
        }

        @Override
        public CompletionStage<Void> thenAccept(Consumer<? super T> action) {
            return completedFuture.thenAccept(action);
        }

        @Override
        public CompletionStage<Void> thenAcceptAsync(Consumer<? super T> action) {
            return completedFuture.thenAcceptAsync(action);
        }

        @Override
        public CompletionStage<Void> thenAcceptAsync(Consumer<? super T> action, Executor executor) {
            return completedFuture.thenAcceptAsync(action, executor);
        }

        @Override
        public CompletionStage<Void> thenRun(Runnable action) {
            return completedFuture.thenRun(action);
        }

        @Override
        public CompletionStage<Void> thenRunAsync(Runnable action) {
            return completedFuture.thenRunAsync(action);
        }

        @Override
        public CompletionStage<Void> thenRunAsync(Runnable action, Executor executor) {
            return completedFuture.thenRunAsync(action, executor);
        }

        @Override
        public <U, V> CompletionStage<V> thenCombine(CompletionStage<? extends U> other,
                                                     BiFunction<? super T, ? super U, ? extends V> fn) {
            return completedFuture.thenCombine(other, fn);
        }

        @Override
        public <U, V> CompletionStage<V> thenCombineAsync(CompletionStage<? extends U> other,
                                                          BiFunction<? super T, ? super U, ? extends V> fn) {
            return completedFuture.thenCombineAsync(other, fn);
        }

        @Override
        public <U, V> CompletionStage<V> thenCombineAsync(CompletionStage<? extends U> other,
                                                          BiFunction<? super T, ? super U, ? extends V> fn, Executor executor) {
            return completedFuture.thenCombineAsync(other, fn, executor);
        }

        @Override
        public <U> CompletionStage<Void> thenAcceptBoth(CompletionStage<? extends U> other,
                                                        BiConsumer<? super T, ? super U> action) {
            return completedFuture.thenAcceptBoth(other, action);
        }

        @Override
        public <U> CompletionStage<Void> thenAcceptBothAsync(CompletionStage<? extends U> other,
                                                             BiConsumer<? super T, ? super U> action) {
            return completedFuture.thenAcceptBothAsync(other, action);
        }

        @Override
        public <U> CompletionStage<Void> thenAcceptBothAsync(CompletionStage<? extends U> other,
                                                             BiConsumer<? super T, ? super U> action, Executor executor) {
            return completedFuture.thenAcceptBothAsync(other, action, executor);
        }

        @Override
        public CompletionStage<Void> runAfterBoth(CompletionStage<?> other, Runnable action) {
            return completedFuture.runAfterBoth(other, action);
        }

        @Override
        public CompletionStage<Void> runAfterBothAsync(CompletionStage<?> other, Runnable action) {
            return completedFuture.runAfterBothAsync(other, action);
        }

        @Override
        public CompletionStage<Void> runAfterBothAsync(CompletionStage<?> other, Runnable action, Executor executor) {
            return completedFuture.runAfterBothAsync(other, action, executor);
        }

        @Override
        public <U> CompletionStage<U> applyToEither(CompletionStage<? extends T> other, Function<? super T, U> fn) {
            return completedFuture.applyToEither(other, fn);
        }

        @Override
        public <U> CompletionStage<U> applyToEitherAsync(CompletionStage<? extends T> other, Function<? super T, U> fn) {
            return completedFuture.applyToEitherAsync(other, fn);
        }

        @Override
        public <U> CompletionStage<U> applyToEitherAsync(CompletionStage<? extends T> other,
                                                         Function<? super T, U> fn,
                                                         Executor executor) {
            return completedFuture.applyToEitherAsync(other, fn, executor);
        }

        @Override
        public CompletionStage<Void> acceptEither(CompletionStage<? extends T> other, Consumer<? super T> action) {
            return completedFuture.acceptEither(other, action);
        }

        @Override
        public CompletionStage<Void> acceptEitherAsync(CompletionStage<? extends T> other, Consumer<? super T> action) {
            return completedFuture.acceptEitherAsync(other, action);
        }

        @Override
        public CompletionStage<Void> acceptEitherAsync(CompletionStage<? extends T> other,
                                                       Consumer<? super T> action,
                                                       Executor executor) {
            return completedFuture.acceptEitherAsync(other, action, executor);
        }

        @Override
        public CompletionStage<Void> runAfterEither(CompletionStage<?> other, Runnable action) {
            return completedFuture.runAfterEither(other, action);
        }

        @Override
        public CompletionStage<Void> runAfterEitherAsync(CompletionStage<?> other, Runnable action) {
            return completedFuture.runAfterEitherAsync(other, action);
        }

        @Override
        public CompletionStage<Void> runAfterEitherAsync(CompletionStage<?> other, Runnable action, Executor executor) {
            return completedFuture.runAfterEitherAsync(other, action, executor);
        }

        @Override
        public <U> CompletionStage<U> thenCompose(Function<? super T, ? extends CompletionStage<U>> fn) {
            return completedFuture.thenCompose(fn);
        }

        @Override
        public <U> CompletionStage<U> thenComposeAsync(Function<? super T, ? extends CompletionStage<U>> fn) {
            return completedFuture.thenComposeAsync(fn);
        }

        @Override
        public <U> CompletionStage<U> thenComposeAsync(Function<? super T, ? extends CompletionStage<U>> fn,
                                                       Executor executor) {
            return completedFuture.thenComposeAsync(fn, executor);
        }

        @Override
        public CompletionStage<T> exceptionally(Function<Throwable, ? extends T> fn) {
            return completedFuture.exceptionally(fn);
        }

        @Override
        public CompletionStage<T> whenComplete(BiConsumer<? super T, ? super Throwable> action) {
            return completedFuture.whenComplete(action);
        }

        @Override
        public CompletionStage<T> whenCompleteAsync(BiConsumer<? super T, ? super Throwable> action) {
            return completedFuture.whenCompleteAsync(action);
        }

        @Override
        public CompletionStage<T> whenCompleteAsync(BiConsumer<? super T, ? super Throwable> action, Executor executor) {
            return completedFuture.whenCompleteAsync(action, executor);
        }

        @Override
        public <U> CompletionStage<U> handle(BiFunction<? super T, Throwable, ? extends U> fn) {
            return completedFuture.handle(fn);
        }

        @Override
        public <U> CompletionStage<U> handleAsync(BiFunction<? super T, Throwable, ? extends U> fn) {
            return completedFuture.handleAsync(fn);
        }

        @Override
        public <U> CompletionStage<U> handleAsync(BiFunction<? super T, Throwable, ? extends U> fn, Executor executor) {
            return completedFuture.handleAsync(fn, executor);
        }

        @Override
        public CompletableFuture<T> toCompletableFuture() {
            throw new UnsupportedOperationException();
        }
    }
}
