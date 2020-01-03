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

package org.glassfish.jersey.client;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;

/* package */ abstract class AbstractNonSyncInvoker<T> {

    public T get() {
        return method("GET");
    }

    public <R> T get(final Class<R> responseType) {
        return method("GET", responseType);
    }

    public <R> T get(final GenericType<R> responseType) {
        return method("GET", responseType);
    }

    public T put(final Entity<?> entity) {
        return method("PUT", entity);
    }

    public <R> T put(final Entity<?> entity, final Class<R> clazz) {
        return method("PUT", entity, clazz);
    }

    public <R> T put(final Entity<?> entity, final GenericType<R> type) {
        return method("PUT", entity, type);
    }

    public T post(final Entity<?> entity) {
        return method("POST", entity);
    }

    public <R> T post(final Entity<?> entity, final Class<R> clazz) {
        return method("POST", entity, clazz);
    }

    public <R> T post(final Entity<?> entity, final GenericType<R> type) {
        return method("POST", entity, type);
    }

    public T delete() {
        return method("DELETE");
    }

    public <R> T delete(final Class<R> responseType) {
        return method("DELETE", responseType);
    }

    public <R> T delete(final GenericType<R> responseType) {
        return method("DELETE", responseType);
    }

    public T head() {
        return method("HEAD");
    }

    public T options() {
        return method("OPTIONS");
    }

    public <R> T options(final Class<R> responseType) {
        return method("OPTIONS", responseType);
    }

    public <R> T options(final GenericType<R> responseType) {
        return method("OPTIONS", responseType);
    }

    public T trace() {
        return method("TRACE");
    }

    public <R> T trace(final Class<R> responseType) {
        return method("TRACE", responseType);
    }

    public <R> T trace(final GenericType<R> responseType) {
        return method("TRACE", responseType);
    }

    public abstract T method(final String name);

    public abstract <R> T method(final String name, final Class<R> responseType);

    public abstract <R> T method(final String name, final GenericType<R> responseType);

    public abstract T method(final String name, final Entity<?> entity);

    public abstract <R> T method(final String name, final Entity<?> entity, final Class<R> responseType);

    public abstract <R> T method(final String name, final Entity<?> entity, final GenericType<R> responseType);
}
