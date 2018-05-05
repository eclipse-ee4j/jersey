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
// Portions Copyright [2018] [Payara Foundation and/or its affiliates]

package org.glassfish.jersey.client;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;
import javax.ws.rs.client.CompletionStageRxInvoker;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.GenericType;
import org.eclipse.microprofile.rest.client.ext.AsyncInvocationInterceptor;
import org.eclipse.microprofile.rest.client.ext.AsyncInvocationInterceptorFactory;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.internal.inject.Providers;
import org.glassfish.jersey.model.internal.RankedComparator;

/**
 * Implementation of Reactive Invoker for {@code CompletionStage}.
 *
 * @author Michal Gajdos
 * @since 2.26
 */
public class JerseyCompletionStageRxInvoker extends AbstractRxInvoker<CompletionStage> implements CompletionStageRxInvoker {

    private final InjectionManager injectionManager;

    JerseyCompletionStageRxInvoker(Invocation.Builder builder, ExecutorService executor, InjectionManager injectionManager) {
        super(builder, executor);
        this.injectionManager = injectionManager;
    }

    @Override
    public <T> CompletionStage<T> method(final String name, final Entity<?> entity, final Class<T> responseType) {
        final ExecutorService executorService = getExecutorService();
        List<AsyncInvocationInterceptor> asyncInvocationInterceptors = getAsyncInvocationInterceptors();

        Supplier invoker = () -> {
            asyncInvocationInterceptors.forEach(AsyncInvocationInterceptor::applyContext);
            return getSyncInvoker().method(name, entity, responseType);
        };

        return executorService == null
                ? CompletableFuture.supplyAsync(invoker)
                : CompletableFuture.supplyAsync(invoker, executorService);
    }

    @Override
    public <T> CompletionStage<T> method(final String name, final Entity<?> entity, final GenericType<T> responseType) {
        final ExecutorService executorService = getExecutorService();
        List<AsyncInvocationInterceptor> asyncInvocationInterceptors = getAsyncInvocationInterceptors();

        Supplier invoker = () -> {
            asyncInvocationInterceptors.forEach(AsyncInvocationInterceptor::applyContext);
            return getSyncInvoker().method(name, entity, responseType);
        };

        return executorService == null
                ? CompletableFuture.supplyAsync(invoker)
                : CompletableFuture.supplyAsync(invoker, executorService);
    }

    private List<AsyncInvocationInterceptor> getAsyncInvocationInterceptors() {
        RankedComparator<AsyncInvocationInterceptorFactory> comparator
                = new RankedComparator<>(RankedComparator.Order.DESCENDING);
        Iterable<AsyncInvocationInterceptorFactory> asyncInvocationInterceptorFactories
                = Providers.getAllProviders(getInjectionManager(), AsyncInvocationInterceptorFactory.class, comparator);

        List<AsyncInvocationInterceptor> asyncInvocationInterceptors = new ArrayList<>();
        asyncInvocationInterceptorFactories.forEach(factory -> {
            AsyncInvocationInterceptor asyncInvocationInterceptor = factory.newInterceptor();
            asyncInvocationInterceptors.add(asyncInvocationInterceptor);
            asyncInvocationInterceptor.prepareContext();
        });

        return asyncInvocationInterceptors;
}

    /**
     * Return injection manager this reactive invoker was initialized with.
     *
     * @return non-null injection manager.
     */
    protected InjectionManager getInjectionManager() {
        return injectionManager;
    }
}
