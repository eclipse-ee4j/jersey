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

    private final ExecutorService wrapped;
    private final List<AsyncInvocationInterceptor> asyncInterceptors;

    ExecutorServiceWrapper(ExecutorService wrapped,
                           List<AsyncInvocationInterceptor> asyncInterceptors) {
        this.wrapped = wrapped;
        this.asyncInterceptors = asyncInterceptors;
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

    private <T> Callable<T> wrap(Callable<T> task) {
        return () -> {
            asyncInterceptors.forEach(AsyncInvocationInterceptor::applyContext);
            return task.call();
        };
    }

    private Runnable wrap(Runnable task) {
        return () -> {
            asyncInterceptors.forEach(AsyncInvocationInterceptor::applyContext);
            task.run();
        };
    }


    private <T> Collection<? extends Callable<T>> wrap(Collection<? extends Callable<T>> tasks) {
        return tasks.stream()
                .map(this::wrap)
                .collect(Collectors.toList());
    }
}
