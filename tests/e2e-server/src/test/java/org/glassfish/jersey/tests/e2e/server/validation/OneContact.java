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
@Constraint(validatedBy = OneContact.Validator.class)
public @interface OneContact {

    String message() default "none or more than one contact";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    public class Validator implements ConstraintValidator<OneContact, ContactBean> {

        @Override
        public void initialize(final OneContact nonRecursive) {
        }

        @Override
        public boolean isValid(final ContactBean contactBean, final ConstraintValidatorContext constraintValidatorContext) {
            if (contactBean.getEmail() == null && contactBean.getPhone() == null) {
                return false;
            }
            return !(contactBean.getEmail() != null && contactBean.getPhone() != null);
        }
    }
}
