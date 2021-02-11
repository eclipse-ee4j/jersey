/*
 * Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2021 Payara Foundation and/or its affiliates. All rights reserved.
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

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.glassfish.jersey.internal.jsr166.Flow;

public class SseEventSuscriber<T> implements Flow.Subscriber<T> {

    private final Subscriber<T> subscriber;
    private Subscription subscription;

    public SseEventSuscriber(Subscriber<T> subscriber) {
        this.subscriber = subscriber;
    }

    @Override
    public void onSubscribe(final Flow.Subscription flowsubscription) {
        subscription = new SseEventSubscription<T>(subscriber, flowsubscription);
        subscriber.onSubscribe(subscription);
    }

    @Override
    public void onNext(final T item) {
        subscriber.onNext(item);
    }

    @Override
    public void onError(final Throwable t) {
        // As per Reactive Streams Rule 2.13, we need to throw a `java.lang.NullPointerException` if the `Throwable` is `null`
        if (t == null) {
            throw new NullPointerException("Reactive Streams Rule 2.13 violated: The received error is `null`");
        }
        subscriber.onError(t);
    }

    @Override
    public void onComplete() {
        subscriber.onComplete();
    }

    /**
     * Get reference to subscriber's {@link Flow.Subscription}.
     *
     * @return subscriber's {@code subscription}
     */
    Subscription getSubscription() {
        return this.subscription;
    }
}
