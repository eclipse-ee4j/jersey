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

import javax.ws.rs.client.AsyncInvoker;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.InvocationCallback;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import java.util.concurrent.CompletableFuture;

/*package*/ abstract class CompletableFutureAsyncInvoker
                extends AbstractNonSyncInvoker<CompletableFuture> implements AsyncInvoker {
    @Override
    public <R> CompletableFuture<R> get(InvocationCallback<R> callback) {
        return method("GET", callback);
    }

    @Override
    public <R> CompletableFuture<R> put(Entity<?> entity, InvocationCallback<R> callback) {
        return method("PUT", entity, callback);
    }

    @Override
    public <R> CompletableFuture<R> post(Entity<?> entity, InvocationCallback<R> callback) {
        return method("POST", entity, callback);
    }

    @Override
    public <R> CompletableFuture<R> delete(InvocationCallback<R> callback) {
        return method("DELETE", callback);
    }

    @Override
    public CompletableFuture<Response> head(InvocationCallback<Response> callback) {
        return method("HEAD", callback);
    }

    @Override
    public <R> CompletableFuture<R> options(InvocationCallback<R> callback) {
        return method("OPTIONS", callback);
    }

    @Override
    public <R> CompletableFuture<R> trace(InvocationCallback<R> callback) {
        return method("TRACE", callback);
    }

    @Override
    public abstract <R> CompletableFuture<R> method(String name, InvocationCallback<R> callback);

    @Override
    public abstract <R> CompletableFuture<R> method(String name, Entity<?> entity, InvocationCallback<R> callback);

    @Override
    public abstract <R> CompletableFuture method(String name, Entity<?> entity, Class<R> responseType);

    @Override
    public abstract <R> CompletableFuture method(String name, Entity<?> entity, GenericType<R> responseType);
}
