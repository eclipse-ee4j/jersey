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

package org.glassfish.jersey.server;

import org.glassfish.jersey.internal.BootstrapBag;
import org.glassfish.jersey.internal.inject.Bindings;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.internal.inject.InstanceBinding;
import org.glassfish.jersey.model.internal.ComponentBag;
import org.glassfish.jersey.model.internal.ManagedObjectsFinalizer;
import org.glassfish.jersey.process.internal.AbstractExecutorProvidersConfigurator;
import org.glassfish.jersey.spi.ExecutorServiceProvider;
import org.glassfish.jersey.spi.ScheduledExecutorServiceProvider;
import org.glassfish.jersey.spi.ScheduledThreadPoolExecutorProvider;
import org.glassfish.jersey.spi.ThreadPoolExecutorProvider;

/**
 * Configurator which initializes and register {@link org.glassfish.jersey.spi.ExecutorServiceProvider} and
 * {@link org.glassfish.jersey.spi.ScheduledExecutorServiceProvider}.
 *
 * @author Petr Bouda
 */
class ServerExecutorProvidersConfigurator extends AbstractExecutorProvidersConfigurator {

    @Override
    public void init(InjectionManager injectionManager, BootstrapBag bootstrapBag) {
        ServerBootstrapBag serverBag = (ServerBootstrapBag) bootstrapBag;
        ResourceConfig runtimeConfig = serverBag.getRuntimeConfig();
        ComponentBag componentBag = runtimeConfig.getComponentBag();
        ManagedObjectsFinalizer finalizer = serverBag.getManagedObjectsFinalizer();

        // TODO: Do we need to register DEFAULT Executor and ScheduledExecutor to InjectionManager?
        ScheduledExecutorServiceProvider defaultScheduledExecutorProvider = new DefaultBackgroundSchedulerProvider();
        InstanceBinding<ScheduledExecutorServiceProvider> schedulerBinding = Bindings
                .service(defaultScheduledExecutorProvider)
                .to(ScheduledExecutorServiceProvider.class)
                .qualifiedBy(BackgroundSchedulerLiteral.INSTANCE);
        injectionManager.register(schedulerBinding);
        finalizer.registerForPreDestroyCall(defaultScheduledExecutorProvider);

        ExecutorServiceProvider defaultAsyncExecutorProvider = new DefaultManagedAsyncExecutorProvider();
        InstanceBinding<ExecutorServiceProvider> executorBinding = Bindings
                .service(defaultAsyncExecutorProvider)
                .to(ExecutorServiceProvider.class);
        injectionManager.register(executorBinding);
        finalizer.registerForPreDestroyCall(defaultAsyncExecutorProvider);

        registerExecutors(injectionManager, componentBag, defaultAsyncExecutorProvider, defaultScheduledExecutorProvider);
    }

    /**
     * Default {@link ScheduledExecutorServiceProvider} used on the server side for providing the scheduled executor service that
     * runs background tasks.
     */
    @BackgroundScheduler
    private static class DefaultBackgroundSchedulerProvider extends ScheduledThreadPoolExecutorProvider {

        public DefaultBackgroundSchedulerProvider() {
            super("jersey-background-task-scheduler");
        }

        @Override
        protected int getCorePoolSize() {
            return 1;
        }
    }

    /**
     * Default {@link ExecutorServiceProvider} used on the server side for managed asynchronous request processing.
     */
    @ManagedAsyncExecutor
    private static class DefaultManagedAsyncExecutorProvider extends ThreadPoolExecutorProvider {

        /**
         * Create new instance for the default managed async executor provider.
         */
        public DefaultManagedAsyncExecutorProvider() {
            super("jersey-server-managed-async-executor");
        }
    }
}
