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

package org.glassfish.jersey.internal.jsr166;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

/**
 * A {@link Flow.Publisher} that asynchronously issues submitted
 * (non-null) items to current subscribers until it is closed.  Each
 * current subscriber receives newly submitted items in the same order
 * unless drops or exceptions are encountered.  Using a
 * SubmissionPublisher allows item generators to act as compliant <a
 * href="http://www.reactive-streams.org/"> reactive-streams</a>
 * Publishers relying on drop handling and/or blocking for flow
 * control.
 * <p>
 * <p>A SubmissionPublisher uses the {@link Executor} supplied in its
 * constructor for delivery to subscribers. The best choice of
 * Executor depends on expected usage. If the generator(s) of
 * submitted items run in separate threads, and the number of
 * subscribers can be estimated, consider using a {@link
 * Executors#newFixedThreadPool}. Otherwise consider using the
 * default, normally the {@link ForkJoinPool#commonPool}.
 * <p>
 * <p>Buffering allows producers and consumers to transiently operate
 * at different rates.  Each subscriber uses an independent buffer.
 * Buffers are created upon first use and expanded as needed up to the
 * given maximum. (The enforced capacity may be rounded up to the
 * nearest power of two and/or bounded by the largest value supported
 * by this implementation.)  Invocations of {@link
 * Flow.Subscription#request(long) request} do not directly result in
 * buffer expansion, but risk saturation if unfilled requests exceed
 * the maximum capacity.  The default value of {@link
 * Flow#defaultBufferSize()} may provide a useful starting point for
 * choosing a capacity based on expected rates, resources, and usages.
 * <p>
 * <p>Publication methods support different policies about what to do
 * when buffers are saturated. Method {@link #submit(Object) submit}
 * blocks until resources are available. This is simplest, but least
 * responsive.  The {@code offer} methods may drop items (either
 * immediately or with bounded timeout), but provide an opportunity to
 * interpose a handler and then retry.
 * <p>
 * <p>If any Subscriber method throws an exception, its subscription
 * is cancelled.  If a handler is supplied as a constructor argument,
 * it is invoked before cancellation upon an exception in method
 * {@link Flow.Subscriber#onNext onNext}, but exceptions in methods
 * {@link Flow.Subscriber#onSubscribe onSubscribe},
 * {@link Flow.Subscriber#onError(Throwable) onError} and
 * {@link Flow.Subscriber#onComplete() onComplete} are not recorded or
 * handled before cancellation.  If the supplied Executor throws
 * {@link RejectedExecutionException} (or any other RuntimeException
 * or Error) when attempting to execute a task, or a drop handler
 * throws an exception when processing a dropped item, then the
 * exception is rethrown. In these cases, not all subscribers will
 * have been issued the published item. It is usually good practice to
 * {@link #closeExceptionally closeExceptionally} in these cases.
 * <p>
 * <p>Method {@link #consume(Consumer)} simplifies support for a
 * common case in which the only action of a subscriber is to request
 * and process all items using a supplied function.
 * <p>
 * <p>This class may also serve as a convenient base for subclasses
 * that generate items, and use the methods in this class to publish
 * them.  For example here is a class that periodically publishes the
 * items generated from a supplier. (In practice you might add methods
 * to independently start and stop generation, to share Executors
 * among publishers, and so on, or use a SubmissionPublisher as a
 * component rather than a superclass.)
 * <p>
 * <pre> {@code
 * class PeriodicPublisher<T> extends SubmissionPublisher<T> {
 *   final ScheduledFuture<?> periodicTask;
 *   final ScheduledExecutorService scheduler;
 *   PeriodicPublisher(Executor executor, int maxBufferCapacity,
 *                     Supplier<? extends T> supplier,
 *                     long period, TimeUnit unit) {
 *     super(executor, maxBufferCapacity);
 *     scheduler = new ScheduledThreadPoolExecutor(1);
 *     periodicTask = scheduler.scheduleAtFixedRate(
 *       () -> submit(supplier.get()), 0, period, unit);
 *   }
 *   public void close() {
 *     periodicTask.cancel(false);
 *     scheduler.shutdown();
 *     super.close();
 *   }
 * }}</pre>
 * <p>
 * <p>Here is an example of a {@link Flow.Processor} implementation.
 * It uses single-step requests to its publisher for simplicity of
 * illustration. A more adaptive version could monitor flow using the
 * lag estimate returned from {@code submit}, along with other utility
 * methods.
 * <p>
 * <pre> {@code
 * class TransformProcessor<S,T> extends SubmissionPublisher<T>
 *   implements Flow.Processor<S,T> {
 *   final Function<? super S, ? extends T> function;
 *   Flow.Subscription subscription;
 *   TransformProcessor(Executor executor, int maxBufferCapacity,
 *                      Function<? super S, ? extends T> function) {
 *     super(executor, maxBufferCapacity);
 *     this.function = function;
 *   }
 *   public void onSubscribe(Flow.Subscription subscription) {
 *     (this.subscription = subscription).request(1);
 *   }
 *   public void onNext(S item) {
 *     subscription.request(1);
 *     submit(function.apply(item));
 *   }
 *   public void onError(Throwable ex) { closeExceptionally(ex); }
 *   public void onComplete() { close(); }
 * }}</pre>
 *
 * @param <T> the published item type
 * @author Doug Lea
 * @since 9
 */
public class SubmissionPublisher<T> implements SubmittableFlowPublisher<T> {

    private final java.util.concurrent.SubmissionPublisher<T> publisher;

    /**
     * Creates a new SubmissionPublisher using the given Executor for
     * async delivery to subscribers, with the given maximum buffer size
     * for each subscriber, and, if non-null, the given handler invoked
     * when any Subscriber throws an exception in method {@link
     * Flow.Subscriber#onNext(Object) onNext}.
     *
     * @param executor          the executor to use for async delivery,
     *                          supporting creation of at least one independent thread
     * @param maxBufferCapacity the maximum capacity for each
     *                          subscriber's buffer (the enforced capacity may be rounded up to
     *                          the nearest power of two and/or bounded by the largest value
     *                          supported by this implementation; method {@link #getMaxBufferCapacity}
     *                          returns the actual value)
     * @param handler           if non-null, procedure to invoke upon exception
   *                          thrown in method {@code onNext}
     * @throws NullPointerException     if executor is null
     * @throws IllegalArgumentException if maxBufferCapacity not
     *                                  positive
     */
    public SubmissionPublisher(Executor executor, int maxBufferCapacity,
                               BiConsumer<? super Flow.Subscriber<? super T>, ? super Throwable> handler) {
        publisher = new java.util.concurrent.SubmissionPublisher<T>(executor, maxBufferCapacity, convertConsumer(handler));
    }

    /**
     * Creates a new SubmissionPublisher using the given Executor for
     * async delivery to subscribers, with the given maximum buffer size
     * for each subscriber, and no handler for Subscriber exceptions in
     * method {@link Flow.Subscriber#onNext(Object) onNext}.
     *
     * @param executor          the executor to use for async delivery,
     *                          supporting creation of at least one independent thread
     * @param maxBufferCapacity the maximum capacity for each
     *                          subscriber's buffer (the enforced capacity may be rounded up to
     *                          the nearest power of two and/or bounded by the largest value
     *                          supported by this implementation; method {@link #getMaxBufferCapacity}
     *                          returns the actual value)
     * @throws NullPointerException     if executor is null
     * @throws IllegalArgumentException if maxBufferCapacity not
     *                                  positive
     */
    public SubmissionPublisher(Executor executor, int maxBufferCapacity) {
        publisher = new java.util.concurrent.SubmissionPublisher<T>(executor, maxBufferCapacity);
    }

    /**
     * Creates a new SubmissionPublisher using the {@link
     * ForkJoinPool#commonPool()} for async delivery to subscribers
     * (unless it does not support a parallelism level of at least two,
     * in which case, a new Thread is created to run each task), with
     * maximum buffer capacity of {@link Flow#defaultBufferSize}, and no
     * handler for Subscriber exceptions in method {@link
     * Flow.Subscriber#onNext(Object) onNext}.
     */
    public SubmissionPublisher() {
        publisher = new java.util.concurrent.SubmissionPublisher<T>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<Void> consume(Consumer<? super T> consumer) {
        return publisher.consume(consumer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        publisher.close();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void closeExceptionally(Throwable error) {
        publisher.closeExceptionally(error);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long estimateMinimumDemand() {
        return publisher.estimateMinimumDemand();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int estimateMaximumLag() {
        return publisher.estimateMaximumLag();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Throwable getClosedException() {
        return publisher.getClosedException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getMaxBufferCapacity() {
        return publisher.getMaxBufferCapacity();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int offer(T item, long timeout, TimeUnit unit, BiPredicate<Flow.Subscriber<? super T>, ? super T> onDrop) {
        return publisher.offer(item, timeout, unit, convertPredicate(onDrop));
    }

    /**
     * {@inheritDoc}
     */
    public int offer(T item, BiPredicate<Flow.Subscriber<? super T>, ? super T> onDrop) {
        return publisher.offer(item, convertPredicate(onDrop));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int submit(T item) {
        return publisher.submit(item);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void subscribe(Flow.Subscriber<? super T> subscriber) {
        publisher.subscribe(convertSubscriber(subscriber));
    }

    private static <T> BiConsumer<? super java.util.concurrent.Flow.Subscriber<? super T>, ? super Throwable> convertConsumer(
            BiConsumer<? super Flow.Subscriber<? super T>, ? super Throwable> consumer) {
        return new BiConsumer<java.util.concurrent.Flow.Subscriber<? super T>, Throwable>() {
            @Override
            public void accept(java.util.concurrent.Flow.Subscriber<? super T> subscriber, Throwable throwable) {
                consumer.accept(convertSubscriber(subscriber), throwable);
            }
        };
    }

    private static <T> BiPredicate<java.util.concurrent.Flow.Subscriber<? super T>, ? super T> convertPredicate(
            BiPredicate<Flow.Subscriber<? super T>, ? super T> predicate) {
        return new BiPredicate<java.util.concurrent.Flow.Subscriber<? super T>, T>() {
            @Override
            public boolean test(java.util.concurrent.Flow.Subscriber<? super T> subscriber, T t) {
                return predicate.test(convertSubscriber(subscriber), t);
            }
        };
    }

    private static <T> Flow.Subscriber<? super T> convertSubscriber(java.util.concurrent.Flow.Subscriber<? super T> subscriber) {
        return new Flow.Subscriber<T>() {

            @Override
            public void onSubscribe(Flow.Subscription subscription) {
                subscriber.onSubscribe(convertSubscription(subscription));
            }

            @Override
            public void onNext(T item) {
                subscriber.onNext(item);
            }

            @Override
            public void onError(Throwable throwable) {
                subscriber.onError(throwable);
            }

            @Override
            public void onComplete() {
                subscriber.onComplete();
            }
        };
    }

    private static <T> java.util.concurrent.Flow.Subscriber<T> convertSubscriber(Flow.Subscriber<T> subscriber) {
        return new java.util.concurrent.Flow.Subscriber<T>() {

            @Override
            public void onSubscribe(java.util.concurrent.Flow.Subscription subscription) {
                subscriber.onSubscribe(convertSubscription(subscription));
            }

            @Override
            public void onNext(T item) {
                subscriber.onNext(item);
            }

            @Override
            public void onError(Throwable throwable) {
                subscriber.onError(throwable);
            }

            @Override
            public void onComplete() {
                subscriber.onComplete();
            }
        };
    }

    private static java.util.concurrent.Flow.Subscription convertSubscription(Flow.Subscription subscription) {
        return new java.util.concurrent.Flow.Subscription() {
            @Override
            public void request(long n) {
                subscription.request(n);
            }

            @Override
            public void cancel() {
                subscription.cancel();
            }
        };
    }

    private static Flow.Subscription convertSubscription(java.util.concurrent.Flow.Subscription subscription) {
        return new Flow.Subscription() {
            @Override
            public void request(long n) {
                subscription.request(n);
            }

            @Override
            public void cancel() {
                subscription.cancel();
            }
        };
    }
}
