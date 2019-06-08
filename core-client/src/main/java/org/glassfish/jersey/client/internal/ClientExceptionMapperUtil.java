/*
 * Copyright (c) 2019 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.client.internal;

import org.glassfish.jersey.spi.ExceptionMappers;

import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.RuntimeType;
import javax.ws.rs.ext.ExceptionMapper;

public abstract class ClientExceptionMapperUtil {
    private ClientExceptionMapperUtil() {
        throw new IllegalStateException("Cannot instantiate");
    }

    /**
     * Searches for a mapping of {@link ExceptionMapper} of a given Exception by calling
     * {@link ExceptionMappers#findMapping(Throwable)}. If not found, it searches recursively for
     * an exception mapper of a cause of the Exception. For instance, for a
     * {@link javax.ws.rs.ProcessingException ProcessingException} an
     * {@code ExceptionMapper<ProcessingException>} would work, but an {@code ExceptionMapper}
     * for a cause of the {@code ProcessingException} would work, too.
     * <p/>
     * Note that the {@code ExceptionMapper} needs to be annotated with
     * {@link ConstrainedTo ConstrainedTo(RuntimeType.Client)}
     *
     * @param exceptionMappers the {@link ExceptionMappers} instance
     * @param exceptionInstance the actual exception for which the {@link ExceptionMapper} is to be find
     * @return A pair of an {@link ExceptionMapper} and the corresponding exception (or cause),
     * or {@code null} when none found.
     */
    public static <T extends Throwable> ExceptionCauseMapper findMappingIncludingCause(final ExceptionMappers exceptionMappers,
                                                                                       final T exceptionInstance) {
        final ExceptionMapper<T> mapper = exceptionMappers.findMapping(exceptionInstance);
        if (mapper != null) {
            final ConstrainedTo annotation = mapper.getClass().getAnnotation(ConstrainedTo.class);
            if (annotation != null && annotation.value().equals(RuntimeType.CLIENT)) {
                return new ExceptionCauseMapper(mapper, exceptionInstance);
            }
        }
        return exceptionInstance.getCause() == null
                ? null : findMappingIncludingCause(exceptionMappers, exceptionInstance.getCause());
    }

    /**
     * A holder class for for a pair of {@link ExceptionMapper} and the corresponding {@code Exception}
     *
     * @param <EXCEPTION> a {@link Throwable}
     */
    public static class ExceptionCauseMapper<EXCEPTION extends Throwable> {
        public ExceptionMapper<EXCEPTION> exceptionMapper;
        public EXCEPTION exception;

        public ExceptionCauseMapper(ExceptionMapper<EXCEPTION> exceptionMapper, EXCEPTION exception) {
            this.exceptionMapper = exceptionMapper;
            this.exception = exception;
        }
    }

}
