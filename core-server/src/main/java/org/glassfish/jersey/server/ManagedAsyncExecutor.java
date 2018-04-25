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

package org.glassfish.jersey.server;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.inject.Qualifier;

/**
 * Injection qualifier that can be used to inject an {@link java.util.concurrent.ExecutorService}
 * instance used by Jersey to execute {@link org.glassfish.jersey.server.ManagedAsync managed asynchronous requests}.
 * <p>
 * The managed asynchronous request executor service instance injected using this injection qualifier can be customized
 * by registering a custom {@link org.glassfish.jersey.spi.ExecutorServiceProvider} implementation that is itself annotated
 * with the {@code &#64;ManagedAsyncExecutor} annotation.
 * </p>
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 * @see ManagedAsyncExecutorLiteral
 * @since 2.18
 */
@Target({ElementType.PARAMETER, ElementType.FIELD, ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Qualifier
public @interface ManagedAsyncExecutor {

}
