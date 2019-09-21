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

package org.glassfish.jersey.process.internal;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.glassfish.jersey.internal.BootstrapConfigurator;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.model.internal.ComponentBag;
import org.glassfish.jersey.spi.ExecutorServiceProvider;
import org.glassfish.jersey.spi.ScheduledExecutorServiceProvider;

/**
 * Abstract Configurator which initializes and register {@link ExecutorServiceProvider} and
 * {@link ScheduledExecutorServiceProvider}.
 *
 * @author Petr Bouda
 */
public abstract class AbstractExecutorProvidersConfigurator implements BootstrapConfigurator {

    private static final Function<Object, ExecutorServiceProvider> CAST_TO_EXECUTOR_PROVIDER =
            ExecutorServiceProvider.class::cast;

    private static final Function<Object, ScheduledExecutorServiceProvider> CAST_TO_SCHEDULED_EXECUTOR_PROVIDER =
            ScheduledExecutorServiceProvider.class::cast;

    /**
     * Retrieves registered {@link ExecutorServiceProvider} and {@link ScheduledExecutorServiceProvider} by an application and
     * adds the default implementations of those interfaces to binds them into {@link InjectionManager}.
     *
     * @param injectionManager                 injection manager used for binding selected executor service providers.
     * @param componentBag                     provides executor service providers registered by an application.
     * @param defaultAsyncExecutorProvider     default implementation of {@link ExecutorServiceProvider}.
     * @param defaultScheduledExecutorProvider default implementation of {@link ScheduledExecutorServiceProvider}.
     */
    protected void registerExecutors(
            InjectionManager injectionManager,
            ComponentBag componentBag,
            ExecutorServiceProvider defaultAsyncExecutorProvider,
            ScheduledExecutorServiceProvider defaultScheduledExecutorProvider) {

        List<ExecutorServiceProvider> customExecutors =
                Stream.concat(
                        componentBag.getClasses(ComponentBag.EXECUTOR_SERVICE_PROVIDER_ONLY).stream()
                                .map(injectionManager::createAndInitialize),
                        componentBag.getInstances(ComponentBag.EXECUTOR_SERVICE_PROVIDER_ONLY).stream())
                        .map(CAST_TO_EXECUTOR_PROVIDER)
                        .collect(Collectors.toList());
        customExecutors.add(defaultAsyncExecutorProvider);

        List<ScheduledExecutorServiceProvider> customScheduledExecutors =
                Stream.concat(
                        componentBag.getClasses(ComponentBag.SCHEDULED_EXECUTOR_SERVICE_PROVIDER_ONLY).stream()
                                .map(injectionManager::createAndInitialize),
                        componentBag.getInstances(ComponentBag.SCHEDULED_EXECUTOR_SERVICE_PROVIDER_ONLY).stream())
                        .map(CAST_TO_SCHEDULED_EXECUTOR_PROVIDER)
                        .collect(Collectors.toList());
        customScheduledExecutors.add(defaultScheduledExecutorProvider);

        ExecutorProviders.registerExecutorBindings(injectionManager, customExecutors, customScheduledExecutors);
    }
}
