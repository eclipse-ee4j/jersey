/*
 * Copyright (c) 2023 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.jersey.micrometer.server;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;

public interface AnnotationFinder {

    AnnotationFinder DEFAULT = new AnnotationFinder() {
    };

    /**
     * The default implementation performs a simple search for a declared annotation
     * matching the search type. Spring provides a more sophisticated annotation search
     * utility that matches on meta-annotations as well.
     * @param annotatedElement The element to search.
     * @param annotationType The annotation type class.
     * @param <A> Annotation type to search for.
     * @return A matching annotation.
     */
    @SuppressWarnings("unchecked")
    default <A extends Annotation> A findAnnotation(AnnotatedElement annotatedElement, Class<A> annotationType) {
        Annotation[] anns = annotatedElement.getDeclaredAnnotations();
        for (Annotation ann : anns) {
            if (ann.annotationType() == annotationType) {
                return (A) ann;
            }
        }
        return null;
    }

}
