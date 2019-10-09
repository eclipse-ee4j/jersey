/*
 * Copyright (c) 2017, 2019 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.client;

import org.glassfish.jersey.client.internal.LocalizationMessages;

import java.util.concurrent.ExecutorService;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.RxInvoker;
import javax.ws.rs.client.SyncInvoker;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;

/**
 * Default implementation of {@link javax.ws.rs.client.RxInvoker reactive invoker}. Extensions of this class are
 * supposed to implement {@link #method(String, Entity, Class)} and
 * {@link #method(String, Entity, GenericType)} methods to which implementations of the rest
 * of the methods from the contract delegate to.
 *
 * @param <T> the asynchronous/event-based completion aware type. The given type should be parametrized with the actual
 *            response type.
 * @author Michal Gajdos
 * @since 2.26
 */
public abstract class AbstractRxInvoker<T> extends AbstractNonSyncInvoker<T> implements RxInvoker<T> {

    private final ExecutorService executorService;
    private final SyncInvoker syncInvoker;

    public AbstractRxInvoker(final SyncInvoker syncInvoker, final ExecutorService executor) {
        if (syncInvoker == null) {
            throw new IllegalArgumentException(LocalizationMessages.NULL_INVOCATION_BUILDER());
        }
        this.syncInvoker = syncInvoker;
        this.executorService = executor;
    }

    /**
     * Return invocation builder this reactive invoker was initialized with.
     *
     * @return non-null invocation builder.
     */
    protected SyncInvoker getSyncInvoker() {
        return syncInvoker;
    }

    /**
     * Return executorService service this reactive invoker was initialized with.
     *
     * @return executorService service instance or {@code null}.
     */
    protected ExecutorService getExecutorService() {
        return executorService;
    }

    @Override
    public T method(final String name) {
        return method(name, Response.class);
    }

    @Override
    public <R> T method(final String name, final Class<R> responseType) {
        return method(name, null, responseType);
    }

    @Override
    public <R> T method(final String name, final GenericType<R> responseType) {
        return method(name, null, responseType);
    }

    @Override
    public T method(final String name, final Entity<?> entity) {
        return method(name, entity, Response.class);
    }

}
