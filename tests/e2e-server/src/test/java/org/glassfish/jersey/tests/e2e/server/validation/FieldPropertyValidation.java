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

package org.glassfish.jersey.tests.e2e.server.validation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.List;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;

/**
 * @author Michal Gajdos
 */
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = FieldPropertyValidation.Validator.class)
public @interface FieldPropertyValidation {

    String message() default "one or more fields are not valid";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    String[] elements() default {};

    public class Validator implements ConstraintValidator<FieldPropertyValidation, FieldPropertyValidationResource.SubResource> {

        private List<String> properties;

        @Override
        public void initialize(final FieldPropertyValidation annotation) {
            this.properties = Arrays.asList(annotation.elements());
        }

        @Override
        public boolean isValid(final FieldPropertyValidationResource.SubResource bean,
                               final ConstraintValidatorContext constraintValidatorContext) {
            boolean result = true;

            for (final String property : properties) {
                if ("fieldAndClass".equals(property)) {
                    result &= bean.fieldAndClass != null;
                } else if ("propertyAndClass".equals(property)) {
                    result &= bean.getPropertyAndClass() != null;
                } else if ("propertyGetterAndClass".equals(property)) {
                    result &= bean.getPropertyGetterAndClass() != null;
                }
            }

            return result;
        }
    }
}
