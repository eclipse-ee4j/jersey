/*
 * Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.e2e.common.process.internal;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Qualifier;
import javax.inject.Singleton;

import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.internal.inject.Injections;
import org.glassfish.jersey.internal.util.Producer;
import org.glassfish.jersey.process.internal.ExecutorProviders;
import org.glassfish.jersey.spi.ExecutorServiceProvider;
import org.glassfish.jersey.spi.ScheduledExecutorServiceProvider;
import org.glassfish.jersey.spi.ScheduledThreadPoolExecutorProvider;
import org.glassfish.jersey.spi.ThreadPoolExecutorProvider;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * ExecutorProviders unit tests.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public class ExecutorProvidersTest extends AbstractBinder {

    /**
     * Custom scheduler injection qualifier.
     */
    @Qualifier
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface CustomScheduler {

    }

    /**
     * Custom scheduler provider.
     */
    @CustomScheduler
    public static class CustomSchedulerProvider extends ScheduledThreadPoolExecutorProvider {

        /**
         * Create a new instance of the scheduled thread pool executor provider.
         */
        public CustomSchedulerProvider() {
            super("custom-scheduler");
        }
    }

    /**
     * Custom named scheduler provider.
     */
    @Named("custom-scheduler")
    public static class CustomNamedSchedulerProvider extends ScheduledThreadPoolExecutorProvider {

        /**
         * Create a new instance of the scheduled thread pool executor provider.
         */
        public CustomNamedSchedulerProvider() {
            super("custom-named-scheduler");
        }
    }

    /**
     * Custom executor injection qualifier.
     */
    @Qualifier
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface CustomExecutor {

    }

    /**
     * Custom executor provider.
     */
    @CustomExecutor
    public static class CustomExecutorProvider extends ThreadPoolExecutorProvider {

        /**
         * Create a new instance of the thread pool executor provider.
         */
        public CustomExecutorProvider() {
            super("custom-executor");
        }
    }

    /**
     * Custom named executor provider.
     */
    @Named("custom-executor")
    public static class CustomNamedExecutorProvider extends ThreadPoolExecutorProvider {

        /**
         * Create a new instance of the thread pool executor provider.
         */
        public CustomNamedExecutorProvider() {
            super("custom-named-executor");
        }
    }

    /**
     * A task to retrieve the current thread name.
     */
    public static class CurrentThreadNameRetrieverTask implements Producer<String> {

        @Override
        public String call() {
            return Thread.currentThread().getName();
        }
    }

    /**
     * Notifier of pre-destroy method invocation.
     */
    public static class PreDestroyNotifier {

        private final CountDownLatch latch = new CountDownLatch(1);

        @PreDestroy
        public void preDestroy() {
            latch.countDown();
        }

        public boolean await(final long timeout, final TimeUnit unit) throws InterruptedException {
            return latch.await(timeout, unit);
        }
    }

    /**
     * Injectable executor client class.
     */
    public static class InjectedExecutorClient {

        @Inject
        private PreDestroyNotifier preDestroyNotifier;

        @Inject
        @CustomExecutor
        private ExecutorService customExecutor;

        @Inject
        @Named("custom-executor")
        private ExecutorService customNamedExecutor;

        @Inject
        @CustomScheduler
        private ScheduledExecutorService customScheduler;

        @Inject
        @CustomScheduler
        private ExecutorService customSchedulerAsExecutor;

        @Inject
        @Named("custom-scheduler")
        private ScheduledExecutorService customNamedScheduler;

        @Inject
        @Named("custom-scheduler")
        private ScheduledExecutorService customNamedSchedulerAsExecutor;

    }

    private InjectionManager injectionManager;

    @Override
    protected void configure() {



        bind(CustomExecutorProvider.class).to(ExecutorServiceProvider.class).in(Singleton.class);
        bind(CustomNamedExecutorProvider.class).to(ExecutorServiceProvider.class).in(Singleton.class);
        bind(CustomSchedulerProvider.class).to(ScheduledExecutorServiceProvider.class).in(Singleton.class);
        bind(CustomNamedSchedulerProvider.class).to(ScheduledExecutorServiceProvider.class).in(Singleton.class);
        bindAsContract(PreDestroyNotifier.class).in(Singleton.class);
    }

    /**
     * Set-up the tests.
     */
    @Before
    public void setup() {
        injectionManager = Injections.createInjectionManager(this);
        ExecutorProviders.registerExecutorBindings(injectionManager);
        injectionManager.completeRegistration();
    }

    /**
     * Test executor and scheduler injection as well as the proper shutdown when injection manager is closed.
     *
     * @throws Exception in case of a test error.
     */
    @Test
    public void testExecutorInjectionAndReleasing() throws Exception {
        final InjectedExecutorClient executorClient = Injections.getOrCreate(injectionManager, InjectedExecutorClient.class);

        assertThat(executorClient.customExecutor, Matchers.notNullValue());
        assertThat(executorClient.customNamedExecutor, Matchers.notNullValue());

        assertThat(executorClient.customScheduler, Matchers.notNullValue());
        assertThat(executorClient.customNamedScheduler, Matchers.notNullValue());
        assertThat(executorClient.customSchedulerAsExecutor, Matchers.notNullValue());
        assertThat(executorClient.customNamedSchedulerAsExecutor, Matchers.notNullValue());

        CurrentThreadNameRetrieverTask nameRetrieverTask = new CurrentThreadNameRetrieverTask();

        // Test authenticity of injected executors
        assertThat(executorClient.customExecutor.submit(nameRetrieverTask).get(),
                Matchers.startsWith("custom-executor-"));
        assertThat(executorClient.customNamedExecutor.submit(nameRetrieverTask).get(),
                Matchers.startsWith("custom-named-executor-"));

        // Test authenticity of injected schedulers
        assertThat(executorClient.customScheduler.submit(nameRetrieverTask).get(),
                Matchers.startsWith("custom-scheduler-"));
        assertThat(executorClient.customNamedScheduler.submit(nameRetrieverTask).get(),
                Matchers.startsWith("custom-named-scheduler-"));
        assertThat(executorClient.customSchedulerAsExecutor.submit(nameRetrieverTask).get(),
                Matchers.startsWith("custom-scheduler-"));
        assertThat(executorClient.customNamedSchedulerAsExecutor.submit(nameRetrieverTask).get(),
                Matchers.startsWith("custom-named-scheduler-"));

        // Test proper executor shutdown when locator is shut down.
        injectionManager.shutdown();

        assertThat("Waiting for pre-destroy timed out.",
                executorClient.preDestroyNotifier.await(3, TimeUnit.SECONDS), Matchers.is(true));

        testShutDown("customExecutor", executorClient.customExecutor);
        testShutDown("customNamedExecutor", executorClient.customNamedExecutor);
        testShutDown("customScheduler", executorClient.customScheduler);
        testShutDown("customNamedScheduler", executorClient.customNamedScheduler);
        testShutDown("customSchedulerAsExecutor", executorClient.customSchedulerAsExecutor);
        testShutDown("customNamedSchedulerAsExecutor", executorClient.customNamedSchedulerAsExecutor);
    }

    private void testShutDown(String name, ExecutorService executorService) throws InterruptedException {
        assertTrue(name + " not shutdown", executorService.isShutdown());
        assertTrue(name + " not terminated", executorService.isTerminated());
    }

}
