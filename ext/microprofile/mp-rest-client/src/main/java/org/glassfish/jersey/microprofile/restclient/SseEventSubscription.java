package org.glassfish.jersey.microprofile.restclient;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

/**
 * A {@link SseEventSubscription} represents a one-to-one life-cycle of a
 * {@link Subscriber} subscribing to a {@link SseEventPublisher}.
 *
 * @param <T> the type of event
 */
public class SseEventSubscription<T> implements Subscription {

    private static final Logger LOG = Logger.getLogger(SseEventSubscription.class.getName());

    private static final Runnable EMPTY_ACTION = () -> {
    };

    private final AtomicLong requested = new AtomicLong();
    private final Subscriber<T> subscriber;

    private final Queue<T> events;
    private final int bufferSize;
    private volatile Throwable closedException;
    private volatile boolean closed;
    private final AtomicInteger wipCounter = new AtomicInteger();
    private final AtomicReference<Runnable> onTerminationAction;

    SseEventSubscription(Subscriber<T> subscriber, int bufferSize) {
        this.subscriber = subscriber;
        this.bufferSize = bufferSize;
        this.events = new ArrayBlockingQueue<>(bufferSize);
        this.onTerminationAction = new AtomicReference<>();
    }

    /**
     * Subscription receiving SSE elements from the source and dealing with
     * subscriber requests.
     *
     * The events are buffers, and older events are dropped if the buffer is
     * full.
     *
     * {@code null} element not allowed
     * (<a href="https://github.com/reactive-streams/reactive-streams-jvm#2.13">Reactive
     * Streams Rule 2.13</a>) as parameters to {@link #emit(Throwable)}.
     *
     * @param element The received element from the source
     */
    void emit(T element) {
        if (closed || isCancelled()) {
            return;
        }

        // As per Reactive Streams Rule 2.13, we need to throw a `java.lang.NullPointerException` if the `element` is `null`
        if (element == null) {
            throw new NullPointerException("Reactive Streams Rule 2.13 violated: The received element is `null`");
        }

        if (events.size() == bufferSize) {
            LOG.log(
                    Level.INFO,
                    "Dropping SSE element '%s' due to lack of subscriber requests",
                    events.poll()
            );
        }
        events.offer(element);

        drain();
    }

    /**
     * No events will be sent by a {@link SseEventPublisher} until demand is
     * signaled via {@link SseEventSubscription#request} method.
     *
     * @param n the strictly positive number of elements to requests to the
     * {@link SseEventPublisher}
     */
    @Override
    public void request(long n) {
        if (n > 0) {
            addSubscriptionRequest(requested, n);
            drain();
        } else {
            cancel();
            subscriber.onError(
                    new IllegalArgumentException(
                            "Request must be positive number " + n
                    )
            );
        }
    }

    /**
     * Request the {@link SseEventPublisher} to stop sending data and clean up
     * resources.
     *
     * Data may still be sent to meet previously signaled demand after calling
     * cancel.
     */
    @Override
    public final void cancel() {
        cleanup();
    }

    private boolean isCancelled() {
        return onTerminationAction.get() == EMPTY_ACTION;
    }

    /**
     * Successful terminal state.
     *
     * Method invoked when it is known that no further events will be sent even
     * if {@link SseEventSubscription#request(long)} is invoked again and no
     * additional Subscriber method invocations will occur for a Subscription
     * that is not already terminated by error.
     *
     */
    void onCompletion() {
        closed = true;
        drain();
    }

    /**
     * Failed terminal state.
     *
     * Method invoked upon an unrecoverable error encountered by a Publisher or
     * Subscription, after which no other Subscriber methods are invoked by the
     * Subscription.
     *
     * {@code null} error not allowed
     * (<a href="https://github.com/reactive-streams/reactive-streams-jvm#2.13">Reactive
     * Streams Rule 2.13</a>) as parameters to {@link #onError(Throwable)}.
     *
     * @param t the throwable signaled
     */
    void onError(Throwable t) {
        if (closed || isCancelled()) {
            return;
        }

        // As per Reactive Streams Rule 2.13, we need to throw a `java.lang.NullPointerException` if the `Throwable` is `null`
        if (t == null) {
            throw new NullPointerException("Reactive Streams Rule 2.13 violated: The received error is `null`");
        }

        this.closedException = t;
        closed = true;

        drain();
    }

    private void cleanup() {
        Runnable action = onTerminationAction.getAndSet(EMPTY_ACTION);
        if (action != null && action != EMPTY_ACTION) {
            action.run();
        }
    }

    private void sendCompletionToSubscriber() {
        if (isCancelled()) {
            return;
        }
        try {
            subscriber.onComplete();
        } finally {
            cleanup();
        }
    }

    private void sendErrorToSubscriber(Throwable t) {
        // As per Reactive Streams Rule 2.13, we need to throw a `java.lang.NullPointerException` if the signaled error is `null`
        if (t == null) {
            throw new NullPointerException("Reactive Streams Rule 2.13 violated, The received error is `null`");
        }
        if (isCancelled()) {
            return;
        }
        try {
            subscriber.onError(t);
        } finally {
            cleanup();
        }
    }

    /**
     * Atomically adds the positive value n to the requested value in the
     * AtomicLong and caps the result at Long.MAX_VALUE and returns the previous
     * value.
     *
     * @param requested the AtomicLong holding the current requested value
     * @param amount the value to add, must be positive (not verified)
     * @return the original value before the add
     */
    private long addSubscriptionRequest(AtomicLong requested, long amount) {
        long current;
        long updated;
        do {
            current = requested.get();
            if (current == Long.MAX_VALUE) {
                return Long.MAX_VALUE;
            }
            updated = current + amount;
            if (updated < 0) {
                updated = Long.MAX_VALUE;
            }
        } while (!requested.compareAndSet(current, updated));
        return current;
    }

    /**
     * Concurrent subtraction bound to 0, used to decrement a request tracker by
     * the amount produced by the publisher.
     *
     * @param requested the atomic long keeping track of requests
     * @param emitted the emitted request to subtract
     * @return value after subtraction or zero
     */
    private long producedSubscriptionRequest(AtomicLong requested, long emitted) {
        long current;
        long updated;
        do {
            current = requested.get();
            if (current == 0 || current == Long.MAX_VALUE) {
                return current;
            }
            updated = current - emitted;
            if (updated < 0) {
                updated = 0;
            }
        } while (!requested.compareAndSet(current, updated));

        return updated;
    }

    private void drain() {
        if (wipCounter.getAndIncrement() != 0) {
            return;
        }

        int missed = 1;

        do {
            long requests = requested.get();
            long emitted = 0L;

            while (emitted != requests) {
                // Clear the queue after cancellation or termination of subscription.
                if (isCancelled()) {
                    events.clear();
                    return;
                }

                T element = events.poll();
                boolean empty = element == null;

                if (closed && empty) {
                    if (closedException != null) {
                        sendErrorToSubscriber(closedException);
                    } else {
                        sendCompletionToSubscriber();
                    }
                    return;
                }

                if (empty) {
                    break;
                }

                // Pass the element to subscriber and increment the emitted element counter.
                try {
                    subscriber.onNext(element);
                } catch (Throwable t) {
                    cancel();
                }
                emitted++;
            }

            if (emitted == requests) {
                // Clear the queue after cancellation or termination.
                if (isCancelled()) {
                    events.clear();
                    return;
                }

                if (closed && events.isEmpty()) {
                    if (closedException != null) {
                        sendErrorToSubscriber(closedException);
                    } else {
                        sendCompletionToSubscriber();
                    }
                    return;
                }
            }

            if (emitted > 0) {
                producedSubscriptionRequest(requested, emitted);
            }

            missed = wipCounter.addAndGet(-missed);
        } while (missed != 0);
    }

}
