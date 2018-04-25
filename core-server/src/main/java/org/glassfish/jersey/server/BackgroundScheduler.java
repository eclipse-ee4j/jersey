/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.inject.Qualifier;

/**
 * Injection qualifier that can be used to inject a {@link java.util.concurrent.ScheduledExecutorService}
 * instance used by Jersey to execute background timed/scheduled tasks.
 * <p>
 * A scheduled executor service instance injected using this injection qualifier can be customized by registering
 * a custom {@link org.glassfish.jersey.spi.ScheduledExecutorServiceProvider} implementation that is itself annotated
 * with the {@code &#64;BackgroundScheduler} annotation.
 * </p>
 * <p>
 * Typically, when facing a need to execute a scheduled background task, you would be creating a new
 * standalone executor service that would be using a new standalone thread pool. This would however break
 * the ability of Jersey to run in environments that have specific thread management and provisioning
 * requirements. In order to simplify and unify programming model for scheduling background tasks in
 * Jersey runtime, Jersey provides an this qualifier to inject a common, task scheduler that is properly
 * configured to support customizable runtime thread .
 * </p>
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 * @see BackgroundSchedulerLiteral
 * @since 2.18
 */
@Target({ElementType.PARAMETER, ElementType.FIELD, ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Qualifier
public @interface BackgroundScheduler {

}
