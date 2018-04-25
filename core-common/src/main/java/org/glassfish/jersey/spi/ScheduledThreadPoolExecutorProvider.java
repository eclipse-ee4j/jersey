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

package org.glassfish.jersey.spi;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;

import javax.annotation.PreDestroy;

/**
 * Default implementation of the Jersey {@link org.glassfish.jersey.spi.ScheduledExecutorServiceProvider
 * scheduled executor service provider SPI}.
 * <p>
 * This provider creates and provisions a shared {@link java.util.concurrent.ScheduledThreadPoolExecutor} instance
 * using the customizable {@link #getCorePoolSize() core threads}, {@link #getBackingThreadFactory() backing thread factory}
 * and {@link #getRejectedExecutionHandler() rejected task handler} values. Subclasses may override the respective methods
 * to customize the parameters of the provisioned scheduler.
 * </p>
 * <p>
 * When Jersey runtime no longer requires the use of a provided executor service instance, it invokes the provider's
 * {@link #dispose} method to signal the provider that the executor service instance can be disposed of. In this method,
 * provider is free to implement the proper shut-down logic for the disposed executor service instance and perform other
 * necessary cleanup. Yet, some providers may wish to implement a shared executor service strategy. In such case,
 * it may not be desirable to shut down the released executor service in the {@link #dispose} method. Instead, to perform the
 * eventual shut-down procedure, the provider may either rely on an explicit invocation of it's specific clean-up method.
 * Since all Jersey providers operate in a <em>container</em> environment, a good clean-up strategy for a shared executor
 * service provider implementation is to expose a {@link javax.annotation.PreDestroy &#64;PreDestroy}-annotated method
 * that will be invoked for all instances managed by the container, before the container shuts down.
 * </p>
 * <p>
 * IMPORTANT: Please note that any pre-destroy methods may not be invoked for instances created outside of the container
 * and later registered within the container. Pre-destroy methods are only guaranteed to be invoked for those instances
 * that are created and managed by the container.
 * </p>
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 * @since 2.18
 */
public class ScheduledThreadPoolExecutorProvider extends AbstractThreadPoolProvider<ScheduledThreadPoolExecutor>
        implements ScheduledExecutorServiceProvider {

    /**
     * Create a new instance of the scheduled thread pool executor provider.
     *
     * @param name provider name. The name will be used to name the threads created & used by the
     *             provisioned scheduled thread pool executor.
     */
    public ScheduledThreadPoolExecutorProvider(final String name) {
        super(name);
    }

    @Override
    public ScheduledExecutorService getExecutorService() {
        return super.getExecutor();
    }

    @Override
    protected ScheduledThreadPoolExecutor createExecutor(
            final int corePoolSize, final ThreadFactory threadFactory, final RejectedExecutionHandler handler) {
        return new ScheduledThreadPoolExecutor(corePoolSize, threadFactory, handler);
    }

    @Override
    public void dispose(final ExecutorService executorService) {
        // NO-OP.
    }

    /**
     * Container pre-destroy handler method.
     * <p>
     * Invoking the method {@link #close() closes} this provider.
     * </p>
     */
    @PreDestroy
    public void preDestroy() {
        close();
    }

}
