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

package org.glassfish.jersey.spi;

import javax.ws.rs.ext.ExceptionMapper;

/**
 * Extension of a {@link ExceptionMapper exception mapper interface}. The exception mapping
 * providers can extend from this interface to add jersey specific functionality to these
 * providers.
 *
 * @author Miroslav Fuksa
 *
 * @param <T> A type of the exception processed by the exception mapper.
 */
public interface ExtendedExceptionMapper<T extends Throwable> extends ExceptionMapper<T> {
    /**
     * Determine whether this provider is able to process a supplied exception instance.
     * <p>
     * This method is called only on those exception mapping providers that are able to
     * process the type of the {@code exception} as defined by the JAX-RS
     * {@link ExceptionMapper} contract. By returning {@code false} this method can reject
     * any given exception instance and change the default JAX-RS exception mapper
     * selection behaviour.
     * </p>
     *
     * @param exception exception instance which should be processed.
     * @return {@code true} if the mapper is able to map the particular exception instance,
     *         {@code false} otherwise.
     */
    public boolean isMappable(T exception);
}
