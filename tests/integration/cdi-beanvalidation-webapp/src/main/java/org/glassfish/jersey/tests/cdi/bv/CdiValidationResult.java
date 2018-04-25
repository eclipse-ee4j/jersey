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

import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.RequestScoped;
import javax.validation.ConstraintViolation;

/**
 * CDI implementation of validation result.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
@RequestScoped
public class CdiValidationResult implements ValidationResult {

    private Set<ConstraintViolation<?>> constraintViolationSet;

    public void setViolations(Set<ConstraintViolation<?>> violations) {
        this.constraintViolationSet = new HashSet<>(violations);
    }

    @Override
    public Set<ConstraintViolation<?>> getAllViolations() {
        return constraintViolationSet;
    }

    @Override
    public boolean isFailed() {
        return (constraintViolationSet != null) ? !constraintViolationSet.isEmpty() : false;
    }

    @Override
    public int getViolationCount() {
        return (constraintViolationSet != null) ? constraintViolationSet.size() : 0;
    }
}
