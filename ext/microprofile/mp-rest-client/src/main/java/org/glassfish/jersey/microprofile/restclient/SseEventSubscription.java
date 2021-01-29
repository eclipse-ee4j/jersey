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

import org.glassfish.jersey.internal.jsr166.Flow;
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

    private final Subscriber subscriber;
    private final Flow.Subscription subscription;

    SseEventSubscription(Subscriber<T> subscriber, Flow.Subscription subscription) {
        this.subscriber = subscriber;
        this.subscription = subscription;
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
            subscription.request(n);
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
    public void cancel() {
        subscription.cancel();
    }

}
