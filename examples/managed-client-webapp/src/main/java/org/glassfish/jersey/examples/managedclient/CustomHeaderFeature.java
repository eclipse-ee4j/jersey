/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.managedclient;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;

/**
 * Dynamic feature that appends a properly configured {@link CustomHeaderFilter} instance
 * to every method that is annotated with {@link org.glassfish.jersey.examples.managedclient.CustomHeaderFeature.Require &#64;Require} internal feature
 * annotation.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public class CustomHeaderFeature implements DynamicFeature {

    /**
     * A method annotation to be placed on those resource methods to which a validating
     * {@link CustomHeaderFilter} instance should be added.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @Target(ElementType.METHOD)
    public static @interface Require {
        /**
         * Expected custom header name to be validated by the {@link CustomHeaderFilter}.
         */
        public String headerName();

        /**
         * Expected custom header value to be validated by the {@link CustomHeaderFilter}.
         */
        public String headerValue();
    }

    @Override
    public void configure(ResourceInfo resourceInfo, FeatureContext context) {
        final Require va = resourceInfo.getResourceMethod().getAnnotation(Require.class);
        if (va != null) {
            context.register(new CustomHeaderFilter(va.headerName(), va.headerValue()));
        }
    }
}
