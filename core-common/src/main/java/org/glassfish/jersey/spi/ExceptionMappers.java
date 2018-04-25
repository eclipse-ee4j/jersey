/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

import javax.ws.rs.ext.ExceptionMapper;

/**
 * Provides lookup of {@link ExceptionMapper} instances that can be used
 * to map exceptions to responses.
 *
 * @author Paul Sandoz
 */
public interface ExceptionMappers {

    /**
     * Get an exception mapping provider for a particular class of exception.
     * Returns the provider whose generic type is the nearest superclass of
     * {@code type}.
     *
     * @param <T> type of the exception handled by the exception mapping provider.
     * @param type the class of exception.
     * @return an {@link ExceptionMapper} for the supplied type or {@code null}
     *     if none is found.
     */
    public <T extends Throwable> ExceptionMapper<T> find(Class<T> type);

    /**
     * Get an exception mapping provider for a particular exception instance.
     * <p>
     * This method is similar to method {@link #find(Class)}. In addition it takes
     * into an account the result of the {@link ExtendedExceptionMapper#isMappable(Throwable)}
     * of any mapper that implements Jersey {@link ExtendedExceptionMapper} API.
     * If an extended exception mapper returns {@code false} from {@code isMappable(Throwable)},
     * the mapper is disregarded from the search.
     * Exception mapping providers are checked one by one until a first provider returns
     * {@code true} from the {@code isMappable(Throwable)} method or until a first provider
     * is found which best supports the exception type and does not implement {@code ExtendedExceptionMapper}
     * API (i.e. it is a standard JAX-RS {@link ExceptionMapper}). The order in which the providers are
     * checked is determined by the distance of the declared exception mapper type and the actual exception
     * type.
     * </p>
     * <p>
     * Note that if an exception mapping provider does not implement {@link ExtendedExceptionMapper}
     * it is always considered applicable for a given exception instance.
     * </p>
     *
     * @param exceptionInstance exception to be handled by the exception mapping provider.
     * @param <T> type of the exception handled by the exception mapping provider.
     * @return an {@link ExceptionMapper} for the supplied exception instance type or {@code null} if none
     *          is found.
     */
    public <T extends Throwable> ExceptionMapper<T> findMapping(T exceptionInstance);
}
