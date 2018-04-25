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

package org.glassfish.jersey.client.rx.guava;

import java.util.concurrent.ExecutorService;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.SyncInvoker;
import javax.ws.rs.core.GenericType;

import org.glassfish.jersey.client.AbstractRxInvoker;
import org.glassfish.jersey.internal.util.collection.LazyValue;
import org.glassfish.jersey.internal.util.collection.Value;
import org.glassfish.jersey.internal.util.collection.Values;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

/**
 * Implementation of Reactive Invoker for {@code ListenableFuture}.
 *
 * @author Michal Gajdos
 * @author Pavel Bucek (pavel.bucek at oracle.com)
 * @since 2.13
 */
final class JerseyRxListenableFutureInvoker extends AbstractRxInvoker<ListenableFuture> implements RxListenableFutureInvoker {

    private static final LazyValue<ListeningExecutorService> DEFAULT_EXECUTOR_SERVICE =
            Values.lazy(new Value<ListeningExecutorService>() {
                @Override
                public ListeningExecutorService get() {
                    return MoreExecutors.newDirectExecutorService();
                }
            });

    private final ListeningExecutorService service;

    JerseyRxListenableFutureInvoker(final SyncInvoker syncInvoker, final ExecutorService executor) {
        super(syncInvoker, executor);

        if (executor == null) {
            // TODO: use JAX-RS client scheduler
            // TODO: https://java.net/jira/browse/JAX_RS_SPEC-523
            service = DEFAULT_EXECUTOR_SERVICE.get();
        } else {
            service = MoreExecutors.listeningDecorator(executor);
        }
    }

    @Override
    public <T> ListenableFuture<T> method(final String name, final Entity<?> entity, final Class<T> responseType) {
        return service.submit(() -> getSyncInvoker().method(name, entity, responseType));
    }

    @Override
    public <T> ListenableFuture<T> method(final String name, final Entity<?> entity, final GenericType<T> responseType) {
        return service.submit(() -> getSyncInvoker().method(name, entity, responseType));
    }
}
