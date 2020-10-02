/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.media.sse.internal;

import org.glassfish.jersey.internal.jsr166.Flow;

public class JerseyFlowAdapters {

    /**
     * Adapt {@link org.glassfish.jersey.internal.jsr166.Flow.Subscriber} to
     * {@link org.reactivestreams.Subscriber}.
     *
     * @param jerseySubscriber Jersey's repackaged {@link org.glassfish.jersey.internal.jsr166.Flow.Subscriber}
     * @param <T>              payload type
     * @return Reactive Streams's {@link org.reactivestreams.Subscriber}
     */
    static <T> org.reactivestreams.Subscriber<T> toSubscriber(Flow.Subscriber<T> jerseySubscriber) {
        return new AdaptedSubscriber<T>(jerseySubscriber);
    }

    public static class AdaptedSubscriber<T> implements org.reactivestreams.Subscriber<T> {

        public final Flow.Subscriber<T> jerseySubscriber;

        public AdaptedSubscriber(Flow.Subscriber<T> jerseySubscriber) {
            this.jerseySubscriber = jerseySubscriber;
        }

        @Override
        public void onSubscribe(final org.reactivestreams.Subscription subscription) {
            jerseySubscriber.onSubscribe(new Flow.Subscription() {
                @Override
                public void request(final long n) {
                    subscription.request(n);
                }

                @Override
                public void cancel() {
                    subscription.cancel();
                }
            });
        }

        @Override
        public void onNext(final T t) {
            jerseySubscriber.onNext(t);
        }

        @Override
        public void onError(final Throwable throwable) {
            jerseySubscriber.onError(throwable);
        }

        @Override
        public void onComplete() {
            jerseySubscriber.onComplete();
        }
    }
}
