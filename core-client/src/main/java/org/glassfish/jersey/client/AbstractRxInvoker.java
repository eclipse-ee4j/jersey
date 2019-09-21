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

package org.glassfish.jersey.client;

import java.util.concurrent.ExecutorService;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.RxInvoker;
import javax.ws.rs.client.SyncInvoker;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;

/**
 * Default implementation of {@link javax.ws.rs.client.rx.RxInvoker reactive invoker}. Extensions of this class are
 * supposed to implement {@link #method(String, Entity, Class)} and
 * {@link #method(String, Entity, GenericType)} methods to which implementations of the rest
 * of the methods from the contract delegate to.
 *
 * @param <T> the asynchronous/event-based completion aware type. The given type should be parametrized with the actual
 *            response type.
 * @author Michal Gajdos
 * @since 2.26
 */
public abstract class AbstractRxInvoker<T> implements RxInvoker<T> {

    private final SyncInvoker syncInvoker;
    private final ExecutorService executorService;

    public AbstractRxInvoker(final SyncInvoker syncInvoker, final ExecutorService executor) {
        if (syncInvoker == null) {
            throw new IllegalArgumentException("Invocation builder cannot be null.");
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
    public T get() {
        return method("GET");
    }

    @Override
    public <R> T get(final Class<R> responseType) {
        return method("GET", responseType);
    }

    @Override
    public <R> T get(final GenericType<R> responseType) {
        return method("GET", responseType);
    }

    @Override
    public T put(final Entity<?> entity) {
        return method("PUT", entity);
    }

    @Override
    public <R> T put(final Entity<?> entity, final Class<R> clazz) {
        return method("PUT", entity, clazz);
    }

    @Override
    public <R> T put(final Entity<?> entity, final GenericType<R> type) {
        return method("PUT", entity, type);
    }

    @Override
    public T post(final Entity<?> entity) {
        return method("POST", entity);
    }

    @Override
    public <R> T post(final Entity<?> entity, final Class<R> clazz) {
        return method("POST", entity, clazz);
    }

    @Override
    public <R> T post(final Entity<?> entity, final GenericType<R> type) {
        return method("POST", entity, type);
    }

    @Override
    public T delete() {
        return method("DELETE");
    }

    @Override
    public <R> T delete(final Class<R> responseType) {
        return method("DELETE", responseType);
    }

    @Override
    public <R> T delete(final GenericType<R> responseType) {
        return method("DELETE", responseType);
    }

    @Override
    public T head() {
        return method("HEAD");
    }

    @Override
    public T options() {
        return method("OPTIONS");
    }

    @Override
    public <R> T options(final Class<R> responseType) {
        return method("OPTIONS", responseType);
    }

    @Override
    public <R> T options(final GenericType<R> responseType) {
        return method("OPTIONS", responseType);
    }

    @Override
    public T trace() {
        return method("TRACE");
    }

    @Override
    public <R> T trace(final Class<R> responseType) {
        return method("TRACE", responseType);
    }

    @Override
    public <R> T trace(final GenericType<R> responseType) {
        return method("TRACE", responseType);
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
