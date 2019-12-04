/*
 * Copyright (c) 2019 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.internal.config;

import static org.junit.Assert.assertEquals;

import com.google.common.reflect.ClassPath;

import java.io.IOException;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.glassfish.jersey.CommonProperties;
import org.glassfish.jersey.internal.util.PropertiesClass;
import org.glassfish.jersey.internal.util.Property;
import org.glassfish.jersey.test.TestProperties;
import org.junit.Test;

public class SystemPropertiesConfigurationModelTest {

    private static final String ROOT_PACKAGE = "org.glassfish.jersey";

    @Test
    public void allPropertyClassLoaded() throws IOException {
        Predicate<Class<?>> containsAnnotation = clazz -> clazz.getAnnotation(PropertiesClass.class) != null
                || clazz.getAnnotation(Property.class) != null;
        Predicate<Class<?>> notCommon = clazz -> clazz != CommonProperties.class;
        Predicate<Class<?>> notTestProperties = clazz -> clazz != TestProperties.class;
        List<String> propertyClasses = getClassesWithPredicate(ROOT_PACKAGE, notCommon, notTestProperties,
                containsAnnotation).stream().map(Class::getName).collect(Collectors.toList());
        propertyClasses.removeAll(SystemPropertiesConfigurationModel.PROPERTY_CLASSES);
        assertEquals("New properties have been found. "
                + "Make sure you add next classes in SystemPropertiesConfigurationModel.PROPERTY_CLASSES: "
                + propertyClasses, 0, propertyClasses.size());
    }

    private List<Class<?>> getClassesWithPredicate(String packageRoot, Predicate<Class<?>>... predicates)
            throws IOException {
        ClassPath classpath = ClassPath.from(Thread.currentThread().getContextClassLoader());
        Stream<Class<?>> steam = classpath.getTopLevelClassesRecursive(packageRoot).stream()
                .map(classInfo -> {
                    try {
                        return Class.forName(classInfo.getName());
                    } catch (ClassNotFoundException e) {
                        return Void.class;
                    }
                });
        for (Predicate<Class<?>> predicate : predicates) {
            steam = steam.filter(predicate);
        }
        return steam.collect(Collectors.toList());
    }

}
