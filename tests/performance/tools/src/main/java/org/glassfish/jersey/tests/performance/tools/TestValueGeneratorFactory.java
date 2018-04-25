/*
 * Copyright (c) 2014, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.performance.tools;

/**
 * Factory for {@link org.glassfish.jersey.tests.performance.tools.TestValueGenerator} implementations.
 *
 * @author Adam Lindenthal (adam.lindenthal at oracle.com)
 */
public class TestValueGeneratorFactory {
    /**
     * Returns instance of {@link org.glassfish.jersey.tests.performance.tools.TestValueGenerator}
     * @param strategy data generation strategy
     * @return generator instance
     */
    public static TestValueGenerator getGenerator(TestDataGenerationStrategy strategy) {
        if (strategy == TestDataGenerationStrategy.CONSTANT) {
            return new ConstantTestValueGenerator();
        } else if (strategy == TestDataGenerationStrategy.RANDOM) {
            return new RandomTestValueGenerator();
        }
        throw new IllegalArgumentException("Requested TestDataGenerationStrategy does not exist.");
    }
}
