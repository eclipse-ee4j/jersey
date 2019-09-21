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

package org.glassfish.jersey.server.internal.inject;

import javax.validation.ConstraintViolationException;
import javax.validation.Validator;

import org.glassfish.jersey.server.model.Invocable;
import org.glassfish.jersey.spi.Contract;

/**
 * Configured validator for Jersey validation purposes.
 *
 * @author Michal Gajdos
 */
@Contract
public interface ConfiguredValidator extends Validator {

    /**
     * Validates resource class instance and input parameters of the {@code method}. {@link ConstraintViolationException} raised
     * from this method should be mapped to HTTP 400 status.
     *
     * @param resource resource class instance.
     * @param resourceMethod invocable containing handling and validation methods.
     * @param args input method parameters.
     * @throws ConstraintViolationException if {@link javax.validation.ConstraintViolation} occurs (should be mapped to HTTP
     * 400 status).
     */
    void validateResourceAndInputParams(final Object resource, final Invocable resourceMethod, final Object[] args)
            throws ConstraintViolationException;

    /**
     * Validates response instance / response entity of the {@code method}. {@link ConstraintViolationException} raised
     * from this method should be mapped to HTTP 500 status.
     *
     * @param resource resource class instance.
     * @param resourceMethod invocable containing handling and validation methods.
     * @param result response entity.
     * @throws ConstraintViolationException if {@link javax.validation.ConstraintViolation} occurs (should be mapped to HTTP
     * 500 status).
     */
    void validateResult(final Object resource, final Invocable resourceMethod, final Object result)
            throws ConstraintViolationException;
}
