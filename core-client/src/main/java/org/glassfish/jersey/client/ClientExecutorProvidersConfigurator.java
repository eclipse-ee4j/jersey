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

import java.lang.reflect.Method;
import java.security.AccessController;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.jersey.internal.BootstrapBag;
import org.glassfish.jersey.internal.inject.Bindings;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.internal.inject.InstanceBinding;
import org.glassfish.jersey.internal.util.ReflectionHelper;
import org.glassfish.jersey.internal.util.collection.Value;
import org.glassfish.jersey.internal.util.collection.Values;
import org.glassfish.jersey.model.internal.ComponentBag;
import org.glassfish.jersey.model.internal.ManagedObjectsFinalizer;
import org.glassfish.jersey.process.internal.AbstractExecutorProvidersConfigurator;
import org.glassfish.jersey.spi.ExecutorServiceProvider;
import org.glassfish.jersey.spi.ScheduledExecutorServiceProvider;

/**
 * Configurator which initializes and register {@link ExecutorServiceProvider} and
 * {@link ScheduledExecutorServiceProvider}.
 *
 * @author Petr Bouda
 */
class ClientExecutorProvidersConfigurator extends AbstractExecutorProvidersConfigurator {

    private static final Logger LOGGER = Logger.getLogger(ClientExecutorProvidersConfigurator.class.getName());
    private static final ExecutorService MANAGED_EXECUTOR_SERVICE = lookupManagedExecutorService();

    private final ComponentBag componentBag;
    private final JerseyClient client;
    private final ExecutorService customExecutorService;
    private final ScheduledExecutorService customScheduledExecutorService;

    ClientExecutorProvidersConfigurator(ComponentBag componentBag, JerseyClient client,
                                        ExecutorService customExecutorService,
                                        ScheduledExecutorService customScheduledExecutorService) {
        this.componentBag = componentBag;
        this.client = client;
        this.customExecutorService = customExecutorService;
        this.customScheduledExecutorService = customScheduledExecutorService;
    }

    @Override
    public void init(InjectionManager injectionManager, BootstrapBag bootstrapBag) {
        Map<String, Object> runtimeProperties = bootstrapBag.getConfiguration().getProperties();
        ManagedObjectsFinalizer finalizer = bootstrapBag.getManagedObjectsFinalizer();

        ExecutorServiceProvider defaultAsyncExecutorProvider;
        ScheduledExecutorServiceProvider defaultScheduledExecutorProvider;

        final ExecutorService clientExecutorService = client.getExecutorService() == null
                // custom executor service can be also set via managed client config class, in that case, it ends up in the
                // customExecutorService field (similar for scheduled version)
                ? customExecutorService
                : client.getExecutorService();

        // if there is a users provided executor service, use it
        if (clientExecutorService != null) {
            defaultAsyncExecutorProvider = new ClientExecutorServiceProvider(clientExecutorService);
            // otherwise, check for ClientProperties.ASYNC_THREADPOOL_SIZE - if that is set, Jersey will create the
            // ExecutorService to be used. If not and running on Java EE container, ManagedExecutorService will be used.
            // Final fallback is DefaultClientAsyncExecutorProvider with defined default.
        } else {
            // Default async request executors support
            Integer asyncThreadPoolSize = ClientProperties
                    .getValue(runtimeProperties, ClientProperties.ASYNC_THREADPOOL_SIZE, Integer.class);

            if (asyncThreadPoolSize != null) {
                // TODO: Do we need to register DEFAULT Executor and ScheduledExecutor to InjectionManager?
                asyncThreadPoolSize = (asyncThreadPoolSize < 0) ? 0 : asyncThreadPoolSize;
                InstanceBinding<Integer> asyncThreadPoolSizeBinding = Bindings
                        .service(asyncThreadPoolSize)
                        .named("ClientAsyncThreadPoolSize");
                injectionManager.register(asyncThreadPoolSizeBinding);

                defaultAsyncExecutorProvider = new DefaultClientAsyncExecutorProvider(asyncThreadPoolSize);
            } else {
                if (MANAGED_EXECUTOR_SERVICE != null) {
                    defaultAsyncExecutorProvider = new ClientExecutorServiceProvider(MANAGED_EXECUTOR_SERVICE);
                } else {
                    defaultAsyncExecutorProvider = new DefaultClientAsyncExecutorProvider(0);
                }
            }
        }

        InstanceBinding<ExecutorServiceProvider> executorBinding = Bindings
                .service(defaultAsyncExecutorProvider)
                .to(ExecutorServiceProvider.class);

        injectionManager.register(executorBinding);
        finalizer.registerForPreDestroyCall(defaultAsyncExecutorProvider);

        final ScheduledExecutorService clientScheduledExecutorService = client.getScheduledExecutorService() == null
                // scheduled executor service set from {@link ClientConfig}.
                ? customScheduledExecutorService
                : client.getScheduledExecutorService();

        if (clientScheduledExecutorService != null) {
            defaultScheduledExecutorProvider =
                    new ClientScheduledExecutorServiceProvider(Values.of(clientScheduledExecutorService));
        } else {
            ScheduledExecutorService scheduledExecutorService = lookupManagedScheduledExecutorService();
            defaultScheduledExecutorProvider =
                scheduledExecutorService == null
                        // default client background scheduler disposes the executor service when client is closed.
                        // we don't need to do that for user provided (via ClientBuilder) or managed executor service.
                        ? new DefaultClientBackgroundSchedulerProvider()
                        : new ClientScheduledExecutorServiceProvider(Values.of(scheduledExecutorService));
        }

        InstanceBinding<ScheduledExecutorServiceProvider> schedulerBinding = Bindings
                .service(defaultScheduledExecutorProvider)
                .to(ScheduledExecutorServiceProvider.class);
        injectionManager.register(schedulerBinding);
        finalizer.registerForPreDestroyCall(defaultScheduledExecutorProvider);

        registerExecutors(injectionManager, componentBag, defaultAsyncExecutorProvider, defaultScheduledExecutorProvider);
    }

    private static ExecutorService lookupManagedExecutorService() {
        // Get the default ManagedExecutorService, if available
        try {
            // Android and some other environments don't have InitialContext class available.
            final Class<?> aClass =
                    AccessController.doPrivileged(ReflectionHelper.classForNamePA("javax.naming.InitialContext"));

            final Object initialContext = aClass.newInstance();

            final Method lookupMethod = aClass.getMethod("lookup", String.class);
            return (ExecutorService) lookupMethod.invoke(initialContext, "java:comp/DefaultManagedExecutorService");
        } catch (Exception e) {
            // ignore
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, e.getMessage(), e);
            }
        } catch (LinkageError error) {
            // ignore - JDK8 compact2 profile - http://openjdk.java.net/jeps/161
        }

        return null;
    }

    private ScheduledExecutorService lookupManagedScheduledExecutorService() {
        try {
            // Android and some other environments don't have InitialContext class available.
            final Class<?> aClass =
                    AccessController.doPrivileged(ReflectionHelper.classForNamePA("javax.naming.InitialContext"));
            final Object initialContext = aClass.newInstance();

            final Method lookupMethod = aClass.getMethod("lookup", String.class);
            return (ScheduledExecutorService) lookupMethod
                    .invoke(initialContext, "java:comp/DefaultManagedScheduledExecutorService");
        } catch (Exception e) {
            // ignore
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, e.getMessage(), e);
            }
        } catch (LinkageError error) {
            // ignore - JDK8 compact2 profile - http://openjdk.java.net/jeps/161
        }

        return null;
    }

    @ClientAsyncExecutor
    public static class ClientExecutorServiceProvider implements ExecutorServiceProvider {

        private final ExecutorService executorService;

        ClientExecutorServiceProvider(ExecutorService executorService) {
            this.executorService = executorService;
        }

        @Override
        public ExecutorService getExecutorService() {
            return executorService;
        }

        @Override
        public void dispose(ExecutorService executorService) {

        }
    }

    @ClientBackgroundScheduler
    public static class ClientScheduledExecutorServiceProvider implements ScheduledExecutorServiceProvider {

        private final Value<ScheduledExecutorService> executorService;

        ClientScheduledExecutorServiceProvider(Value<ScheduledExecutorService> executorService) {
            this.executorService = executorService;
        }

        @Override
        public ScheduledExecutorService getExecutorService() {
            return executorService.get();
        }

        @Override
        public void dispose(ExecutorService executorService) {

        }
    }
}
