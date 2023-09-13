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

import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import io.micrometer.core.annotation.Timed;
import io.micrometer.core.annotation.TimedSet;

class TimedFinder {

    private final AnnotationFinder annotationFinder;

    TimedFinder(AnnotationFinder annotationFinder) {
        this.annotationFinder = annotationFinder;
    }

    Set<Timed> findTimedAnnotations(AnnotatedElement element) {
        Timed t = annotationFinder.findAnnotation(element, Timed.class);
        if (t != null) {
            return Collections.singleton(t);
        }

        TimedSet ts = annotationFinder.findAnnotation(element, TimedSet.class);
        if (ts != null) {
            return Arrays.stream(ts.value()).collect(Collectors.toSet());
        }

        return Collections.emptySet();
    }

}
