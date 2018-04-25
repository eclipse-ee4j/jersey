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

package org.glassfish.jersey.ext.cdi1x.transaction.internal;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.transaction.TransactionalException;

import org.glassfish.jersey.ext.cdi1x.internal.JerseyVetoed;
import org.glassfish.jersey.spi.ExceptionMappers;
import org.glassfish.jersey.spi.ExtendedExceptionMapper;

/**
 * Helper class to handle exceptions thrown by JTA layer. If this mapper was not
 * registered, no {@link WebApplicationException} thrown from a transactional
 * CDI bean would get properly mapped to corresponding response.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
@ApplicationScoped
@JerseyVetoed
public class TransactionalExceptionMapper implements ExtendedExceptionMapper<TransactionalException> {

    @Inject
    @TransactionalExceptionInterceptorProvider.WaeQualifier
    private WebAppExceptionHolder waeHolder;

    @Inject
    private BeanManager beanManager;

    @Inject
    private Provider<ExceptionMappers> mappers;

    @Override
    public Response toResponse(TransactionalException exception) {
        final ExceptionMapper mapper = mappers.get().findMapping(exception);

        if (mapper != null && !TransactionalExceptionMapper.class.isAssignableFrom(mapper.getClass())) {
            return mapper.toResponse(exception);
        } else {
            if (waeHolder != null) {
                final WebApplicationException wae = waeHolder.getException();
                if (wae != null) {
                    return wae.getResponse();
                }
            }
            throw exception;
        }
    }

    @Override
    public boolean isMappable(TransactionalException exception) {
        return true;
    }
}
