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

import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.function.BiConsumer;

/**
 * Factory creating JDK8 compatible SubmissionPublisher (Jdk8SubmissionPublisher) or JDK 9+ SubmissionPublisher
 */
public class SubmissionPublisherFactory {

    /**
     * Creates a new SubmissionPublisher using the {@link
     * ForkJoinPool#commonPool()} for async delivery to subscribers
     * (unless it does not support a parallelism level of at least two,
     * in which case, a new Thread is created to run each task), with
     * maximum buffer capacity of {@link Flow#defaultBufferSize}, and no
     * handler for Subscriber exceptions in method {@link
     * Flow.Subscriber#onNext(Object) onNext}.
     */
    public static <T> SubmittableFlowPublisher<T> createSubmissionPublisher() {
        return new SubmissionPublisher<T>();
    }

    public static <T> SubmittableFlowPublisher<T> createSubmissionPublisher(Executor executor,
                                                                            int maxBufferCapacity) {
        return new SubmissionPublisher<T>(executor, maxBufferCapacity);
    }

    public static <T> SubmittableFlowPublisher<T> createSubmissionPublisher(Executor executor,
                                                                            int maxBufferCapacity,
                                                                            BiConsumer<? super Flow.Subscriber<? super T>,
                                                                                    ? super Throwable> handler) {
        return new SubmissionPublisher<T>(executor, maxBufferCapacity, handler);
    }

}
