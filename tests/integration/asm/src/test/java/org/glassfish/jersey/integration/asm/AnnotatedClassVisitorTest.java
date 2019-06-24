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
package org.glassfish.jersey.integration.asm;

import jersey.repackaged.org.objectweb.asm.ClassVisitor;
import org.glassfish.jersey.server.internal.scanning.AnnotationAcceptingListener;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

public class AnnotatedClassVisitorTest {

    @Test
    public void testInheritedMethodsFromClassVisitor() {
        Class<?> annotatedClassVisitorClass = null;
        final Class<?> classVisitorClass = ClassVisitor.class;

        final Class<?>[] listenerClasses = AnnotationAcceptingListener.class.getDeclaredClasses();

        for (Class<?> c : listenerClasses) {
            if (c.getName().contains("AnnotatedClassVisitor")) {
                annotatedClassVisitorClass = c;
                break;
            }
        }

        final List<Method> classVisitorMethods = Arrays.asList(classVisitorClass.getDeclaredMethods());
        final List<Method> annotatedClassVisitorMethods = Arrays.asList(annotatedClassVisitorClass.getDeclaredMethods());
        boolean containsAllMethods = true;
        for (Method classVisitorMethod : classVisitorMethods) {
            boolean foundClassVisitorMethod = false;
            for (Method annotatedClassVisitorMethod : annotatedClassVisitorMethods) {
                if (annotatedClassVisitorMethod.getName().equals(classVisitorMethod.getName())
                        && annotatedClassVisitorMethod.getReturnType() == classVisitorMethod.getReturnType()
                        && annotatedClassVisitorMethod.getParameterCount() == classVisitorMethod.getParameterCount()) {
                    final Class<?>[] annotatedClassVisitorTypes = annotatedClassVisitorMethod.getParameterTypes();
                    final Class<?>[] classVisitorTypes = classVisitorMethod.getParameterTypes();
                    boolean typesMatch = true;
                    for (int i = 0; i != annotatedClassVisitorTypes.length; i++) {
                        if (annotatedClassVisitorTypes[i] != classVisitorTypes[i]) {
                            typesMatch = false;
                            break;
                        }
                    }
                    if (typesMatch) {
                        foundClassVisitorMethod = true;
                        //System.out.println("found method " + classVisitorMethod.getName());
                        break;
                    }
                }
            }
            if (!foundClassVisitorMethod) {
                containsAllMethods = false;
                System.out.append("Method ")
                        .append(classVisitorMethod.getName())
                        .println(" not implemented by AnnotationAcceptingListener.AnnotatedClassVisitor");
            }
        }
        Assert.assertThat(containsAllMethods, Matchers.is(true));
    }
}
