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

package org.glassfish.jersey.client.rx.rxjava2;

import java.util.concurrent.ExecutorService;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.SyncInvoker;
import javax.ws.rs.core.GenericType;

import org.glassfish.jersey.client.AbstractRxInvoker;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;

/**
 * Implementation of Reactive Invoker for {@code Flowable}. If no executor service is provided the JAX-RS Async client is used
 * to retrieve data when a subscriber is subscribed. When an executor service is provided a sync call is invoked on a thread
 * provided on from this service.
 *
 * @author Michal Gajdos
 * @author Pavel Bucek (pavel.bucek at oracle.com)
 * @since 2.16
 */
final class JerseyRxFlowableInvoker extends AbstractRxInvoker<Flowable> implements RxFlowableInvoker {

    JerseyRxFlowableInvoker(SyncInvoker syncInvoker, ExecutorService executor) {
        super(syncInvoker, executor);
    }

    @Override
    public <T> Flowable<T> method(final String name, final Entity<?> entity, final Class<T> responseType) {
        return method(name, entity, new GenericType<T>(responseType) { });
    }

    @Override
    public <T> Flowable<T> method(final String name, final Entity<?> entity, final GenericType<T> responseType) {
        final Scheduler scheduler;

        if (getExecutorService() != null) {
            scheduler = Schedulers.from(getExecutorService());
        } else {
            // TODO: use JAX-RS client scheduler
            // TODO: https://java.net/jira/browse/JAX_RS_SPEC-523
            scheduler = Schedulers.io();
        }

        // Invoke as sync JAX-RS client request and subscribe/observe on a scheduler initialized with executor service.
        return Flowable.create(new FlowableOnSubscribe<T>() {
            @Override
            public void subscribe(FlowableEmitter<T> flowableEmitter) throws Exception {
                    try {
                        final T response = getSyncInvoker().method(name, entity, responseType);
                        flowableEmitter.onNext(response);
                        flowableEmitter.onComplete();
                    } catch (final Throwable throwable) {
                        flowableEmitter.onError(throwable);
                    }
            }
        }, BackpressureStrategy.DROP).subscribeOn(scheduler).observeOn(scheduler);
    }
}
