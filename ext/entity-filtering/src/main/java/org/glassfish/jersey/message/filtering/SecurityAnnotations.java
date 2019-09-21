/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.message.filtering;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;

import org.glassfish.jersey.internal.inject.AnnotationLiteral;

/**
 * Convenience utility methods for creating instances of security annotations.
 *
 * @author Michal Gajdos
 */
public final class SecurityAnnotations {

    /**
     * Create {@link RolesAllowed} annotation implementation for given set of roles.
     *
     * @param roles roles to be part of the annotation.
     * @return annotation implementation.
     */
    public static RolesAllowed rolesAllowed(final String... roles) {
        final List<String> list = new ArrayList<>(roles.length);
        for (final String role : roles) {
            if (role != null) {
                list.add(role);
            }
        }
        return new RolesAllowedImpl(list.toArray(new String[list.size()]));
    }

    /**
     * Create {@link PermitAll} annotation implementation.
     *
     * @return annotation implementation.
     */
    public static PermitAll permitAll() {
        return new PermitAllImpl();
    }

    /**
     * Create {@link DenyAll} annotation implementation.
     *
     * @return annotation implementation.
     */
    public static DenyAll denyAll() {
        return new DenyAllImpl();
    }

    /**
     * DenyAll annotation implementation.
     */
    @SuppressWarnings("ClassExplicitlyAnnotation")
    private static final class RolesAllowedImpl extends AnnotationLiteral<RolesAllowed> implements RolesAllowed {

        private final String[] roles;

        private RolesAllowedImpl(final String[] roles) {
            this.roles = roles;
        }

        @Override
        public String[] value() {
            return roles;
        }
    }

    /**
     * DenyAll annotation implementation.
     */
    @SuppressWarnings("ClassExplicitlyAnnotation")
    private static final class DenyAllImpl extends AnnotationLiteral<DenyAll> implements DenyAll {
    }

    /**
     * PermitAll annotation implementation.
     */
    @SuppressWarnings("ClassExplicitlyAnnotation")
    private static class PermitAllImpl extends AnnotationLiteral<PermitAll> implements PermitAll {
    }

    /**
     * Prevent instantiation.
     */
    private SecurityAnnotations() {
    }
}
