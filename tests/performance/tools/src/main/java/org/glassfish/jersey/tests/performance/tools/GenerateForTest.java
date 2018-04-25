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

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation to mark fields of a bean to be populated by
 * {@link org.glassfish.jersey.tests.performance.tools.TestValueGenerator} during the test data generation process.
 *
 * @author Adam Lindenthal (adam.lindenthal at oracle.com)
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface GenerateForTest {
    /** specifies how many elements should be created and added to an array/collection */
    int length() default 1;

    /** due to java type erasure, the type of the collection has to be explicitly specified */
    Class<?> collectionMemberType() default Object.class;

    /** if the bean contains a collection declared via its interface, the desired implementing class has to be specified */
    Class<?> implementingClass() default Object.class;
}
