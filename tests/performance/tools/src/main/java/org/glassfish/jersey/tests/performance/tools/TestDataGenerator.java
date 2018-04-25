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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

/**
 * Instantiates and populates a bean with testing data.
 *
 * @author Adam Lindenthal (adam.lindenthal at oracle.com)
 */
public class TestDataGenerator {

    /**
     *
     * @param bean bean to be populated
     * @param <T> type of the testing bean
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws InstantiationException
     */
    public static <T> void populateBeanByAnnotations(T bean) throws ReflectiveOperationException {
        Field[] fields = bean.getClass().getDeclaredFields();
        TestValueGenerator generator = TestValueGeneratorFactory.getGenerator(TestDataGenerationStrategy.RANDOM);
        for (Field field : fields) {
            GenerateForTest annotation = field.getAnnotation(GenerateForTest.class);
            if (annotation != null) {
                field.setAccessible(true);
                field.set(bean, generator.getValueForType(field.getType(), annotation));
            }
        }
    }
}
