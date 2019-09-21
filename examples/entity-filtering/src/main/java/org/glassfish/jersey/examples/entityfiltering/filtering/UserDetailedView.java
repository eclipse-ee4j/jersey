/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.entityfiltering.filtering;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.glassfish.jersey.internal.inject.AnnotationLiteral;
import org.glassfish.jersey.message.filtering.EntityFiltering;

/**
 * Entity-filtering annotation used to define detailed view on returned
 * {@link org.glassfish.jersey.examples.entityfiltering.domain.User} entities.
 *
 * @author Michal Gajdos
 */
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@EntityFiltering
public @interface UserDetailedView {

    /**
     * Factory class for creating instances of {@code UserDetailedView} annotation.
     */
    public static class Factory extends AnnotationLiteral<UserDetailedView> implements UserDetailedView {

        private Factory() {
        }

        public static UserDetailedView get() {
            return new Factory();
        }
    }
}
