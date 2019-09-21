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

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.RxInvoker;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;

import rx.Observable;

/**
 * Reactive invoker providing support for {@link rx.Observable observable} from RxJava.
 * <p/>
 * Requests are by default invoked on a separate thread (as JAX-RS Async client requests). This behavior can be overridden by
 * providing a {@link java.util.concurrent.ExecutorService executor service} when client extension is being created
 * (in {@link org.glassfish.jersey.client.rx.rxjava.RxObservable RxObservable}).
 * <p/>
 * The observables produced by method calls are cold observables. That means that request to the service is invoked only when a
 * subscriber is subscribed to the observable.
 *
 * @author Michal Gajdos
 * @since 2.13
 */
public interface RxObservableInvoker extends RxInvoker<Observable> {

    @Override
    public Observable<Response> get();

    @Override
    public <T> Observable<T> get(Class<T> responseType);

    @Override
    public <T> Observable<T> get(GenericType<T> responseType);

    @Override
    public Observable<Response> put(Entity<?> entity);

    @Override
    public <T> Observable<T> put(Entity<?> entity, Class<T> clazz);

    @Override
    public <T> Observable<T> put(Entity<?> entity, GenericType<T> type);

    @Override
    public Observable<Response> post(Entity<?> entity);

    @Override
    public <T> Observable<T> post(Entity<?> entity, Class<T> clazz);

    @Override
    public <T> Observable<T> post(Entity<?> entity, GenericType<T> type);

    @Override
    public Observable<Response> delete();

    @Override
    public <T> Observable<T> delete(Class<T> responseType);

    @Override
    public <T> Observable<T> delete(GenericType<T> responseType);

    @Override
    public Observable<Response> head();

    @Override
    public Observable<Response> options();

    @Override
    public <T> Observable<T> options(Class<T> responseType);

    @Override
    public <T> Observable<T> options(GenericType<T> responseType);

    @Override
    public Observable<Response> trace();

    @Override
    public <T> Observable<T> trace(Class<T> responseType);

    @Override
    public <T> Observable<T> trace(GenericType<T> responseType);

    @Override
    public Observable<Response> method(String name);

    @Override
    public <T> Observable<T> method(String name, Class<T> responseType);

    @Override
    public <T> Observable<T> method(String name, GenericType<T> responseType);

    @Override
    public Observable<Response> method(String name, Entity<?> entity);

    @Override
    public <T> Observable<T> method(String name, Entity<?> entity, Class<T> responseType);

    @Override
    public <T> Observable<T> method(String name, Entity<?> entity, GenericType<T> responseType);
}
