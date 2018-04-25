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

/**
 * An extension contract for providing pluggable executor service providers to be used by
 * Jersey client or server runtime whenever a specific executor service is needed to execute a Jersey runtime processing task.
 * <p>
 * This mechanism allows Jersey to run in environments that have specific thread management and provisioning requirements,
 * such as application servers, cloud environments etc.
 * Dedicated Jersey extension modules or applications running in such environment may provide a custom
 * implementation of the {@code ExecutorServiceProvider} interface to customize the default
 * Jersey runtime thread management & provisioning strategy in order to comply with the threading requirements,
 * models and policies specific to each particular environment.
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
 * <p>
 * Jersey runtime expects that a concrete executor service provider implementation class is annotated with a
 * {@link javax.inject.Qualifier qualifier} annotation. This qualifier is then used to createAndInitialize a qualified injection point
 * for injecting the executor service instance provided by the annotated provider. {@link javax.inject.Named Named} providers
 * are also supported. For example:
 * </p>
 * <pre>
 * &#64;Named("my-executor")
 * public MyExecutorProvider implements ExecutorServiceProvider {
 *     ...
 * }
 *
 * ...
 *
 * // Injecting ExecutorService provided by the MyExecutorProvider
 * &#64;Inject &#64;Named("my-executor") ExecutorService myExecutor;
 * </pre>
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 * @see ScheduledExecutorServiceProvider
 * @see ThreadPoolExecutorProvider
 * @since 2.18
 */
@Contract
public interface ExecutorServiceProvider {

    /**
     * Get an executor service to be used by Jersey client or server runtime to execute specific tasks.
     * <p>
     * This method is <em>usually</em> invoked just once at either Jersey client or server application runtime initialization,
     * it <em>may</em> however be invoked multiple times. Once the instance of the provided executor service is not
     * needed anymore by Jersey application runtime, it will be {@link #dispose disposed}.
     * This typically happens in one of the following situations:
     * </p>
     * <ul>
     * <li>Jersey client instance is closed (client runtime is shut down).</li>
     * <li>Jersey container running a server-side Jersey application is shut down.</li>
     * <li>Jersey server-side application is un-deployed.</li>
     * </ul>
     *
     * @return an executor service. Must not return {@code null}.
     */
    public ExecutorService getExecutorService();

    /**
     * Invoked when Jersey runtime no longer requires use of the provided executor service.
     *
     * @param executorService executor service to be disposed.
     */
    public void dispose(ExecutorService executorService);
}
