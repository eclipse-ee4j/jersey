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

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.RxInvoker;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;

import io.reactivex.Flowable;


/**
 * Reactive invoker providing support for {@link Flowable flowable} from RxJava.
 * <p/>
 * Requests are by default invoked on a separate thread (as JAX-RS Async client requests). This behavior can be overridden by
 * providing a {@link java.util.concurrent.ExecutorService executor service} when client extension is being created.
 *
 * @author Pavel Bucek (pavel.bucek at oracle.com)
 * @author Michal Gajdos
 * @since 2.26
 */
public interface RxFlowableInvoker extends RxInvoker<Flowable> {

    @Override
    Flowable<Response> get();

    @Override
    <R> Flowable<R> get(Class<R> responseType);

    @Override
    <R> Flowable<R> get(GenericType<R> responseType);

    @Override
    Flowable<Response> put(Entity<?> entity);

    @Override
    <R> Flowable<R> put(Entity<?> entity, Class<R> responseType);

    @Override
    <R> Flowable<R> put(Entity<?> entity, GenericType<R> responseType);

    @Override
    Flowable<Response> post(Entity<?> entity);

    @Override
    <R> Flowable<R> post(Entity<?> entity, Class<R> responseType);

    @Override
    <R> Flowable<R> post(Entity<?> entity, GenericType<R> responseType);

    @Override
    Flowable<Response> delete();

    @Override
    <R> Flowable<R> delete(Class<R> responseType);

    @Override
    <R> Flowable<R> delete(GenericType<R> responseType);

    @Override
    Flowable<Response> head();

    @Override
    Flowable<Response> options();

    @Override
    <R> Flowable<R> options(Class<R> responseType);

    @Override
    <R> Flowable<R> options(GenericType<R> responseType);

    @Override
    Flowable<Response> trace();

    @Override
    <R> Flowable<R> trace(Class<R> responseType);

    @Override
    <R> Flowable<R> trace(GenericType<R> responseType);

    @Override
    Flowable<Response> method(String name);

    @Override
    <R> Flowable<R> method(String name, Class<R> responseType);

    @Override
    <R> Flowable<R> method(String name, GenericType<R> responseType);

    @Override
    Flowable<Response> method(String name, Entity<?> entity);

    @Override
    <R> Flowable<R> method(String name, Entity<?> entity, Class<R> responseType);

    @Override
    <R> Flowable<R> method(String name, Entity<?> entity, GenericType<R> responseType);
}
