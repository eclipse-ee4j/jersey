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

import java.util.concurrent.ScheduledExecutorService;

/**
 * An extension contract for providing pluggable scheduled executor service providers to be used by
 * Jersey client or server runtime whenever a specific scheduler is needed to schedule execution of a
 * Jersey runtime processing task.
 * <p>
 * This mechanism allows Jersey to run in environments that have specific thread management and provisioning requirements,
 * such as application servers, cloud environments etc.
 * Dedicated Jersey extension modules or applications running in such environment may provide a custom
 * implementation of the {@code ScheduledExecutorServiceProvider} interface to customize the default
 * Jersey runtime thread management & provisioning strategy in order to comply with the threading requirements,
 * models and policies specific to each particular environment.
 * </p>
 * Jersey runtime expects that a concrete scheduled executor service provider implementation class is annotated with a
 * {@link javax.inject.Qualifier qualifier} annotation. This qualifier is then used to createAndInitialize a qualified injection point
 * for injecting the scheduled executor service instance provided by the annotated provider. {@link javax.inject.Named Named}
 * providers are also supported. For example:
 * </p>
 * <pre>
 * &#64;Named("my-scheduler")
 * public MySchedulerProvider implements ScheduledExecutorServiceProvider {
 *     ...
 * }
 *
 * ...
 *
 * // Injecting ScheduledExecutorService provided by the MySchedulerProvider
 * &#64;Inject &#64;Named("my-scheduler") ScheduledExecutorService myScheduler;
 * </pre>
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 * @see ExecutorServiceProvider
 * @see ScheduledThreadPoolExecutorProvider
 * @since 2.18
 */
@Contract
public interface ScheduledExecutorServiceProvider extends ExecutorServiceProvider {

    /**
     * Get a scheduled executor service to be used by Jersey client or server runtime to schedule execution of
     * specific tasks.
     * <p>
     * <p>
     * This method is <em>usually</em> invoked just once at either Jersey client or server application runtime initialization,
     * it <em>may</em> however be invoked multiple times. Once the instance of the provided scheduled executor service is not
     * needed anymore by Jersey application runtime, it will be {@link #dispose disposed}.
     * This typically happens in one of the following situations:
     * </p>
     * <ul>
     * <li>Jersey client instance is closed (client runtime is shut down).</li>
     * <li>Jersey container running a server-side Jersey application is shut down.</li>
     * <li>Jersey server-side application is un-deployed.</li>
     * </ul>
     *
     * @return a scheduled executor service. Must not return {@code null}.
     */
    @Override
    public ScheduledExecutorService getExecutorService();

}
