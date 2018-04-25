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

package org.glassfish.jersey.tests.e2e.server.validation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import javax.validation.constraints.NotNull;

/**
 * @author Michal Gajdos
 */
@Retention(RetentionPolicy.RUNTIME)
@NotNull
@Constraint(validatedBy = CustomValidation.Validator.class)
public @interface CustomValidation {

    String message() default "client should never see this";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    public static class Validator implements ConstraintValidator<CustomValidation, CustomBean> {

        @Context
        private UriInfo uriInfo;

        @Override
        public void initialize(final CustomValidation constraintAnnotation) {
        }

        @Override
        public boolean isValid(final CustomBean bean, final ConstraintValidatorContext context) {
            // !bean.isValidate() - to make sure this validation passes for CustomConfigValidationTest#testTraversableResolver
            // (TraversableResolver is not invoked for ElementType.TYPE anymore).
            return !bean.isValidate() || uriInfo.getPathParameters().getFirst("path").equals(bean.getPath());
        }
    }
}
