/*
 * Copyright (c) 2014, 2024 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.test.spi;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.DynamicTest;
import org.junit.platform.commons.support.HierarchyTraversalMode;
import org.junit.platform.commons.support.ReflectionSupport;

/**
 * Helper class for Jersey Test Framework.
 *
 * @author Michal Gajdos
 */
public final class TestHelper {

    /**
     * Create a human readable string from given URI. This method replaces {@code 0} port (start container at first available
     * port) in given URI with string {@code <AVAILABLE-PORT>}.
     *
     * @param uri an URI.
     * @return stringified URI.
     */
    public static String zeroPortToAvailablePort(final URI uri) {
        return uri.getPort() != 0
                ? uri.toString()
                : uri.toString().replaceFirst(":0", ":<AVAILABLE-PORT>");
    }

    /**
     * Prevent instantiation.
     */
    private TestHelper() {
    }

    public static DynamicContainer toTestContainer(Object test, String displayName) {
        Class<?> klass = test.getClass();
        List<Method> testMethods = ReflectionSupport.findMethods(klass,
                method -> method.isAnnotationPresent(org.junit.jupiter.api.Test.class)
                && !method.isAnnotationPresent(org.junit.jupiter.api.Disabled.class),
                HierarchyTraversalMode.TOP_DOWN);
        List<Method> beforeEachMethods = ReflectionSupport.findMethods(klass,
                method -> method.isAnnotationPresent(org.junit.jupiter.api.BeforeEach.class),
                HierarchyTraversalMode.TOP_DOWN);
        List<Method> afterEachMethods = ReflectionSupport.findMethods(klass,
                method -> method.isAnnotationPresent(org.junit.jupiter.api.AfterEach.class),
                HierarchyTraversalMode.TOP_DOWN);
        Collection<DynamicTest> children = new ArrayList<>();
        for (Method method : testMethods) {
            children.add(DynamicTest.dynamicTest(method.getName(), () -> {
                try {
                    for (Method beforeEachMethod : beforeEachMethods) {
                        beforeEachMethod.invoke(test);
                    }
                    method.invoke(test);
                } finally {
                    for (Method afterEachMethod : afterEachMethods) {
                        afterEachMethod.invoke(test);
                    }
                }
            }));
        }
        return DynamicContainer.dynamicContainer(displayName, children);
    }
}
