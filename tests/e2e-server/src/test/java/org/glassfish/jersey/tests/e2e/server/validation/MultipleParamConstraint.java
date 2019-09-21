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
import javax.validation.Payload;
import javax.validation.constraints.Size;

/**
 * @author Michal Gajdos
 */
@Retention(value = RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {})
@ParamConstraint
@Size(max = 11)
public @interface MultipleParamConstraint {

    String message() default "invalid parameter";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
