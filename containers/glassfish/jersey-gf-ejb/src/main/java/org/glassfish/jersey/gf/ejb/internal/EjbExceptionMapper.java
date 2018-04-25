/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.gf.ejb.internal;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import javax.ejb.EJBException;
import javax.inject.Inject;
import javax.inject.Provider;

import org.glassfish.jersey.spi.ExceptionMappers;
import org.glassfish.jersey.spi.ExtendedExceptionMapper;

/**
 * Helper class to handle exceptions wrapped by the EJB container with EJBException.
 * If this mapper was not registered, no {@link WebApplicationException}
 * would end up mapped to the corresponding response.
 *
 * @author Paul Sandoz
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
public class EjbExceptionMapper implements ExtendedExceptionMapper<EJBException> {

    private final Provider<ExceptionMappers> mappers;

    /**
     * Create new EJB exception mapper.
     *
     * @param mappers utility to find mapper delegate.
     */
    @Inject
    public EjbExceptionMapper(Provider<ExceptionMappers> mappers) {
        this.mappers = mappers;
    }

    @Override
    public Response toResponse(EJBException exception) {
        return causeToResponse(exception);
    }

    @Override
    public boolean isMappable(EJBException exception) {
        try {
            return (causeToResponse(exception) != null);
        } catch (Throwable ignored) {
            return false;
        }
    }

    private Response causeToResponse(EJBException exception) {

        final Exception cause = exception.getCausedByException();

        if (cause != null) {

            final ExceptionMapper mapper = mappers.get().findMapping(cause);
            if (mapper != null && mapper != this) {

                return mapper.toResponse(cause);

            } else if (cause instanceof WebApplicationException) {

                return ((WebApplicationException) cause).getResponse();
            }
        }
        return null;
    }
}
