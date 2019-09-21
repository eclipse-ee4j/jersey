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

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.RxInvoker;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;

import com.google.common.util.concurrent.ListenableFuture;

/**
 * Reactive invoker providing support for {@link com.google.common.util.concurrent.ListenableFuture ListenableFuture} from Guava.
 *
 * @author Michal Gajdos
 * @since 2.13
 */
public interface RxListenableFutureInvoker extends RxInvoker<ListenableFuture> {

    @Override
    public ListenableFuture<Response> get();

    @Override
    public <T> ListenableFuture<T> get(Class<T> responseType);

    @Override
    public <T> ListenableFuture<T> get(GenericType<T> responseType);

    @Override
    public ListenableFuture<Response> put(Entity<?> entity);

    @Override
    public <T> ListenableFuture<T> put(Entity<?> entity, Class<T> clazz);

    @Override
    public <T> ListenableFuture<T> put(Entity<?> entity, GenericType<T> type);

    @Override
    public ListenableFuture<Response> post(Entity<?> entity);

    @Override
    public <T> ListenableFuture<T> post(Entity<?> entity, Class<T> clazz);

    @Override
    public <T> ListenableFuture<T> post(Entity<?> entity, GenericType<T> type);

    @Override
    public ListenableFuture<Response> delete();

    @Override
    public <T> ListenableFuture<T> delete(Class<T> responseType);

    @Override
    public <T> ListenableFuture<T> delete(GenericType<T> responseType);

    @Override
    public ListenableFuture<Response> head();

    @Override
    public ListenableFuture<Response> options();

    @Override
    public <T> ListenableFuture<T> options(Class<T> responseType);

    @Override
    public <T> ListenableFuture<T> options(GenericType<T> responseType);

    @Override
    public ListenableFuture<Response> trace();

    @Override
    public <T> ListenableFuture<T> trace(Class<T> responseType);

    @Override
    public <T> ListenableFuture<T> trace(GenericType<T> responseType);

    @Override
    public ListenableFuture<Response> method(String name);

    @Override
    public <T> ListenableFuture<T> method(String name, Class<T> responseType);

    @Override
    public <T> ListenableFuture<T> method(String name, GenericType<T> responseType);

    @Override
    public ListenableFuture<Response> method(String name, Entity<?> entity);

    @Override
    public <T> ListenableFuture<T> method(String name, Entity<?> entity, Class<T> responseType);

    @Override
    public <T> ListenableFuture<T> method(String name, Entity<?> entity, GenericType<T> responseType);
}
