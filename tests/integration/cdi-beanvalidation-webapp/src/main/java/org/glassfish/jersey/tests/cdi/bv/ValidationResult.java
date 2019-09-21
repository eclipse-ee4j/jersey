/*
 * Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.cdi.bv;

import java.util.Set;

import javax.validation.ConstraintViolation;

/**
 * An interface to be utilized when resource method validation issues
 * are to be handled within actual resource method.
 */
public interface ValidationResult {

    /**
     * Returns an immutable set of all constraint violations detected.
     *
     * @return All constraint violations detected
     */
    public Set<ConstraintViolation<?>> getAllViolations();

    /**
     * Returns <code>true</code> if there is at least one constraint violation.
     * Same as checking whether {@link #getViolationCount()} is greater than zero.
     *
     * @return <code>true</code> if there is at least one constraint violation.
     */
    public boolean isFailed();

    /**
     * Returns the total number of constraint violations detected. Same as calling
     * <code>getAllViolations().size()</code>.
     *
     * @return The number of constraint violations
     */
    int getViolationCount();
}
