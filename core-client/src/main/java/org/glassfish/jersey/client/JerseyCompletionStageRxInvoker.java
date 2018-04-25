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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;

import javax.ws.rs.client.CompletionStageRxInvoker;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.GenericType;

/**
 * Implementation of Reactive Invoker for {@code CompletionStage}.
 *
 * @author Michal Gajdos
 * @since 2.26
 */
public class JerseyCompletionStageRxInvoker extends AbstractRxInvoker<CompletionStage> implements CompletionStageRxInvoker {

    JerseyCompletionStageRxInvoker(Invocation.Builder builder, ExecutorService executor) {
        super(builder, executor);
    }

    @Override
    public <T> CompletionStage<T> method(final String name, final Entity<?> entity, final Class<T> responseType) {
        final ExecutorService executorService = getExecutorService();

        return executorService == null
                ? CompletableFuture.supplyAsync(() -> getSyncInvoker().method(name, entity, responseType))
                : CompletableFuture.supplyAsync(() -> getSyncInvoker().method(name, entity, responseType), executorService);
    }

    @Override
    public <T> CompletionStage<T> method(final String name, final Entity<?> entity, final GenericType<T> responseType) {
        final ExecutorService executorService = getExecutorService();

        return executorService == null
                ? CompletableFuture.supplyAsync(() -> getSyncInvoker().method(name, entity, responseType))
                : CompletableFuture.supplyAsync(() -> getSyncInvoker().method(name, entity, responseType), executorService);
    }
}
