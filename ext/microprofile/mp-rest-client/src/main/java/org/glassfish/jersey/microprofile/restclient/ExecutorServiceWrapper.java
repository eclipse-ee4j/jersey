/*
 * Copyright (c) 2019 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.microprofile.restclient;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.eclipse.microprofile.rest.client.ext.AsyncInvocationInterceptor;

/**
 * Invokes all {@link AsyncInvocationInterceptor} for every new thread.
 *
 * @author David Kral
 */
class ExecutorServiceWrapper implements ExecutorService {

    static final ThreadLocal<List<AsyncInvocationInterceptor>> asyncInterceptors = new ThreadLocal<>();

    private final ExecutorService wrapped;

    ExecutorServiceWrapper(ExecutorService wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public void shutdown() {
        wrapped.shutdown();
    }

    @Override
    public List<Runnable> shutdownNow() {
        return wrapped.shutdownNow();
    }

    @Override
    public boolean isShutdown() {
        return wrapped.isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return wrapped.isTerminated();
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return wrapped.awaitTermination(timeout, unit);
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        return wrapped.submit(wrap(task));
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        return wrapped.submit(wrap(task), result);
    }

    @Override
    public Future<?> submit(Runnable task) {
        return wrapped.submit(wrap(task));
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        return wrapped.invokeAll(wrap(tasks));
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
            throws InterruptedException {
        return wrapped.invokeAll(wrap(tasks), timeout, unit);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        return wrapped.invokeAny(wrap(tasks));
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
        return wrapped.invokeAny(wrap(tasks), timeout, unit);
    }

    @Override
    public void execute(Runnable command) {
        wrapped.execute(wrap(command));
    }

    private static <T> Callable<T> wrap(Callable<T> task) {
        List<AsyncInvocationInterceptor> asyncInvocationInterceptors = asyncInterceptors.get();
        asyncInterceptors.remove();
        return () -> {
            applyContextOnInterceptors(asyncInvocationInterceptors);
            return task.call();
        };
    }

    private static Runnable wrap(Runnable task) {
        List<AsyncInvocationInterceptor> asyncInvocationInterceptors = asyncInterceptors.get();
        asyncInterceptors.remove();
        return () -> {
            applyContextOnInterceptors(asyncInvocationInterceptors);
            task.run();
        };
    }

    private static void applyContextOnInterceptors(List<AsyncInvocationInterceptor> asyncInvocationInterceptors) {
        if (asyncInvocationInterceptors != null) {
            //applyContext methods need to be called in reverse ordering of priority
            for (int i = asyncInvocationInterceptors.size(); i-- > 0; ) {
                asyncInvocationInterceptors.get(i).applyContext();
            }
        }
    }

    private static <T> Collection<? extends Callable<T>> wrap(Collection<? extends Callable<T>> tasks) {
        return tasks.stream()
                .map(ExecutorServiceWrapper::wrap)
                .collect(Collectors.toList());
    }
}
