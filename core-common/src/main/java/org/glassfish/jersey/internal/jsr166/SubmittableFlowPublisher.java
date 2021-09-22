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
import java.util.concurrent.TimeUnit;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RejectedExecutionException;


/**
 * A {@link Flow.Publisher} that asynchronously issues submitted
 * (non-null) items to current subscribers until it is closed.  Each
 * current subscriber receives newly submitted items in the same order
 * unless drops or exceptions are encountered.  Using a
 * SubmittableFlowPublisher allows item generators to act as compliant <a
 * href="http://www.reactive-streams.org/"> reactive-streams</a>
 * Publishers relying on drop handling and/or blocking for flow
 * control.
 * <p>
 * <p>An implementation of SubmittableFlowPublisher uses the {@link Executor} supplied in its
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
 *
 * @param <T> the published item type
 */
public interface SubmittableFlowPublisher<T> extends Flow.Publisher<T>, AutoCloseable {

    /**
     * Processes all published items using the given Consumer function.
     * Returns a CompletableFuture that is completed normally when this
     * publisher signals {@link Flow.Subscriber#onComplete()
     * onComplete}, or completed exceptionally upon any error, or an
     * exception is thrown by the Consumer, or the returned
     * CompletableFuture is cancelled, in which case no further items
     * are processed.
     *
     * @param consumer the function applied to each onNext item
     * @return a CompletableFuture that is completed normally
     * when the publisher signals onComplete, and exceptionally
     * upon any error or cancellation
     * @throws NullPointerException if consumer is null
     */
    CompletableFuture<Void> consume(final Consumer<? super T> consumer);

    /**
     * Unless already closed, issues {@link
     * Flow.Subscriber#onComplete() onComplete} signals to current
     * subscribers, and disallows subsequent attempts to publish.
     * Upon return, this method does <em>NOT</em> guarantee that all
     * subscribers have yet completed.
     */
    void close();

    /**
     * Unless already closed, issues {@link
     * Flow.Subscriber#onError(Throwable) onError} signals to current
     * subscribers with the given error, and disallows subsequent
     * attempts to publish.  Future subscribers also receive the given
     * error. Upon return, this method does <em>NOT</em> guarantee
     * that all subscribers have yet completed.
     *
     * @param error the {@code onError} argument sent to subscribers
     * @throws NullPointerException if error is null
     */
    void closeExceptionally(final Throwable error);

    /**
     * Returns an estimate of the minimum number of items requested
     * (via {@link Flow.Subscription#request(long) request}) but not
     * yet produced, among all current subscribers.
     *
     * @return the estimate, or zero if no subscribers
     */
    long estimateMinimumDemand();

    /**
     * Returns an estimate of the maximum number of items produced but
     * not yet consumed among all current subscribers.
     *
     * @return the estimate
     */
    int estimateMaximumLag();

    /**
     * Returns the exception associated with {@link
     * #closeExceptionally(Throwable) closeExceptionally}, or null if
     * not closed or if closed normally.
     *
     * @return the exception, or null if none
     */
    Throwable getClosedException();

    /**
     * Returns the maximum per-subscriber buffer capacity.
     *
     * @return the maximum per-subscriber buffer capacity
     */
    int getMaxBufferCapacity();

    /**
     * Publishes the given item, if possible, to each current subscriber
     * by asynchronously invoking its {@link
     * Flow.Subscriber#onNext(Object) onNext} method, blocking while
     * resources for any subscription are unavailable, up to the
     * specified timeout or until the caller thread is interrupted, at
     * which point the given handler (if non-null) is invoked, and if it
     * returns true, retried once. (The drop handler may distinguish
     * timeouts from interrupts by checking whether the current thread
     * is interrupted.)  Other calls to methods in this class by other
     * threads are blocked while the handler is invoked.  Unless
     * recovery is assured, options are usually limited to logging the
     * error and/or issuing an {@link Flow.Subscriber#onError(Throwable)
     * onError} signal to the subscriber.
     * <p>
     * <p>This method returns a status indicator: If negative, it
     * represents the (negative) number of drops (failed attempts to
     * issue the item to a subscriber). Otherwise it is an estimate of
     * the maximum lag (number of items submitted but not yet
     * consumed) among all current subscribers. This value is at least
     * one (accounting for this submitted item) if there are any
     * subscribers, else zero.
     * <p>
     * <p>If the Executor for this publisher throws a
     * RejectedExecutionException (or any other RuntimeException or
     * Error) when attempting to asynchronously notify subscribers, or
     * the drop handler throws an exception when processing a dropped
     * item, then this exception is rethrown.
     *
     * @param item    the (non-null) item to publish
     * @param timeout how long to wait for resources for any subscriber
     *                before giving up, in units of {@code unit}
     * @param unit    a {@code TimeUnit} determining how to interpret the
     *                {@code timeout} parameter
     * @param onDrop  if non-null, the handler invoked upon a drop to a
     *                subscriber, with arguments of the subscriber and item; if it
     *                returns true, an offer is re-attempted (once)
     * @return if negative, the (negative) number of drops; otherwise
     * an estimate of maximum lag
     * @throws IllegalStateException      if closed
     * @throws NullPointerException       if item is null
     * @throws RejectedExecutionException if thrown by Executor
     */
    int offer(T item, long timeout, TimeUnit unit, BiPredicate<Flow.Subscriber<? super T>, ? super T> onDrop);

    /**
     * Publishes the given item, if possible, to each current subscriber
     * by asynchronously invoking its {@link
     * Flow.Subscriber#onNext(Object) onNext} method. The item may be
     * dropped by one or more subscribers if resource limits are
     * exceeded, in which case the given handler (if non-null) is
     * invoked, and if it returns true, retried once.  Other calls to
     * methods in this class by other threads are blocked while the
     * handler is invoked.  Unless recovery is assured, options are
     * usually limited to logging the error and/or issuing an {@link
     * Flow.Subscriber#onError(Throwable) onError} signal to the
     * subscriber.
     * <p>
     * <p>This method returns a status indicator: If negative, it
     * represents the (negative) number of drops (failed attempts to
     * issue the item to a subscriber). Otherwise it is an estimate of
     * the maximum lag (number of items submitted but not yet
     * consumed) among all current subscribers. This value is at least
     * one (accounting for this submitted item) if there are any
     * subscribers, else zero.
     * <p>
     * <p>If the Executor for this publisher throws a
     * RejectedExecutionException (or any other RuntimeException or
     * Error) when attempting to asynchronously notify subscribers, or
     * the drop handler throws an exception when processing a dropped
     * item, then this exception is rethrown.
     *
     * @param item   the (non-null) item to publish
     * @param onDrop if non-null, the handler invoked upon a drop to a
     *               subscriber, with arguments of the subscriber and item; if it
     *               returns true, an offer is re-attempted (once)
     * @return if negative, the (negative) number of drops; otherwise
     * an estimate of maximum lag
     * @throws IllegalStateException      if closed
     * @throws NullPointerException       if item is null
     * @throws RejectedExecutionException if thrown by Executor
     */
    int offer(T item, BiPredicate<Flow.Subscriber<? super T>, ? super T> onDrop);

    /**
     * Publishes the given item to each current subscriber by
     * asynchronously invoking its {@link Flow.Subscriber#onNext(Object)
     * onNext} method, blocking uninterruptibly while resources for any
     * subscriber are unavailable. This method returns an estimate of
     * the maximum lag (number of items submitted but not yet consumed)
     * among all current subscribers. This value is at least one
     * (accounting for this submitted item) if there are any
     * subscribers, else zero.
     * <p>
     * <p>If the Executor for this publisher throws a
     * RejectedExecutionException (or any other RuntimeException or
     * Error) when attempting to asynchronously notify subscribers,
     * then this exception is rethrown, in which case not all
     * subscribers will have been issued this item.
     *
     * @param item the (non-null) item to publish
     * @return the estimated maximum lag among subscribers
     * @throws IllegalStateException      if closed
     * @throws NullPointerException       if item is null
     * @throws RejectedExecutionException if thrown by Executor
     */
    int submit(T item);

    /**
     * Adds the given Subscriber unless already subscribed.  If already
     * subscribed, the Subscriber's {@link
     * Flow.Subscriber#onError(Throwable) onError} method is invoked on
     * the existing subscription with an {@link IllegalStateException}.
     * Otherwise, upon success, the Subscriber's {@link
     * Flow.Subscriber#onSubscribe onSubscribe} method is invoked
     * asynchronously with a new {@link Flow.Subscription}.  If {@link
     * Flow.Subscriber#onSubscribe onSubscribe} throws an exception, the
     * subscription is cancelled. Otherwise, if this SubmittableFlowPublisher
     * was closed exceptionally, then the subscriber's {@link
     * Flow.Subscriber#onError onError} method is invoked with the
     * corresponding exception, or if closed without exception, the
     * subscriber's {@link Flow.Subscriber#onComplete() onComplete}
     * method is invoked.  Subscribers may enable receiving items by
     * invoking the {@link Flow.Subscription#request(long) request}
     * method of the new Subscription, and may unsubscribe by invoking
     * its {@link Flow.Subscription#cancel() cancel} method.
     *
     * @param subscriber the subscriber
     * @throws NullPointerException if subscriber is null
     */
     void subscribe(Flow.Subscriber<? super T> subscriber);
}
