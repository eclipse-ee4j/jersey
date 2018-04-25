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

package org.glassfish.jersey.server.mvc.beanvalidation;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import javax.inject.Singleton;
import javax.validation.ConstraintViolationException;

import org.glassfish.jersey.server.mvc.spi.AbstractErrorTemplateMapper;
import org.glassfish.jersey.server.validation.internal.ValidationHelper;

/**
 * {@link org.glassfish.jersey.spi.ExtendedExceptionMapper Exception mapper} providing validation errors that are passed to a
 * viewable in case a Bean Validation exception has been thrown during processing an request.
 *
 * @author Michal Gajdos
 * @since 2.3
 */
@Singleton
final class ValidationErrorTemplateExceptionMapper extends AbstractErrorTemplateMapper<ConstraintViolationException> {

    @Override
    protected Response.Status getErrorStatus(final ConstraintViolationException cve) {
        return ValidationHelper.getResponseStatus(cve);
    }

    @Override
    protected Object getErrorModel(final ConstraintViolationException cve) {
        return ValidationHelper.constraintViolationToValidationErrors(cve);
    }
}
