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

package org.glassfish.jersey.tests.performance.benchmark.entity.json;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.glassfish.jersey.internal.inject.AnnotationLiteral;
import org.glassfish.jersey.message.filtering.EntityFiltering;

/**
 * Entity-filtering annotation used to define detailed view on returned {@link Project} entities.
 *
 * @author Michal Gajdos
 */
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@EntityFiltering
public @interface ProjectDetailedView {

    /**
     * Factory class for creating instances of {@code ProjectDetailedView} annotation.
     */
    public static class Factory extends AnnotationLiteral<ProjectDetailedView> implements ProjectDetailedView {

        private Factory() {
        }

        public static ProjectDetailedView get() {
            return new Factory();
        }
    }
}
