/*
 * Copyright (c) 2025 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.server.validation;

import jakarta.validation.ValidatorContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

public class ConfigurationCoverageTest {
    @Test
    public void testAllConfigurationPossibilitiesAreCovered() {
        String[] methodNames = {"messageInterpolator", "traversableResolver",
                "constraintValidatorFactory", "parameterNameProvider", "addValueExtractor", "clockProvider",
                // exclude method
                "getValidator"};
        Method[] methods = ValidatorContext.class.getDeclaredMethods();
        boolean passed =  true;
        methodLoop:
        for (Method method : methods) {
            for (String methodName : methodNames) {
                if (methodName.equals(method.getName())) {
                    continue methodLoop;
                }
            }
            passed = false;
            System.err.append(ValidationConfig.class.getName())
                    .append(" contains no getter for method: ")
                    .println(method.getName());
        }
        Assertions.assertTrue(passed);
    }
}
