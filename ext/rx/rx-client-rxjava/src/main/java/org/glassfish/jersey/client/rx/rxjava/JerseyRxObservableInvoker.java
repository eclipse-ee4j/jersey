/*
 * Copyright (c) 2014, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.client.rx.rxjava;

import java.util.concurrent.ExecutorService;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.SyncInvoker;
import javax.ws.rs.core.GenericType;

import org.glassfish.jersey.client.AbstractRxInvoker;

import rx.Observable;
import rx.Scheduler;
import rx.schedulers.Schedulers;

/**
 * Implementation of Reactive Invoker for {@code Observable}. If no executor service is provided the JAX-RS Async client is used
 * to retrieve data when a subscriber is subscribed. When an executor service is provided a sync call is invoked on a thread
 * provided on from this service.
 *
 * @author Michal Gajdos
 * @since 2.13
 */
final class JerseyRxObservableInvoker extends AbstractRxInvoker<Observable> implements RxObservableInvoker {

    JerseyRxObservableInvoker(final SyncInvoker syncInvoker, final ExecutorService executor) {
        super(syncInvoker, executor);
    }

    @Override
    public <T> Observable<T> method(final String name, final Entity<?> entity, final Class<T> responseType) {
        return method(name, entity, new GenericType<T>(responseType) { });
    }

    @Override
    public <T> Observable<T> method(final String name, final Entity<?> entity, final GenericType<T> responseType) {
        final Scheduler scheduler;

        if (getExecutorService() != null) {
            scheduler = Schedulers.from(getExecutorService());
        } else {
            // TODO: use JAX-RS client scheduler
            // TODO: https://java.net/jira/browse/JAX_RS_SPEC-523
            scheduler = Schedulers.io();
        }

        // Invoke as sync JAX-RS client request and subscribe/observe on a scheduler initialized with executor service.
        return Observable.create((Observable.OnSubscribe<T>) subscriber -> {
            if (!subscriber.isUnsubscribed()) {
                try {
                    final T response = getSyncInvoker().method(name, entity, responseType);

                    if (!subscriber.isUnsubscribed()) {
                        subscriber.onNext(response);
                    }
                    if (!subscriber.isUnsubscribed()) {
                        subscriber.onCompleted();
                    }
                } catch (final Throwable throwable) {
                    if (!subscriber.isUnsubscribed()) {
                        subscriber.onError(throwable);
                    }
                }
            }
        }).subscribeOn(scheduler).observeOn(scheduler);
    }
}
