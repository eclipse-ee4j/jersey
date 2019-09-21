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

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.core.SecurityContext;

import javax.annotation.security.RolesAllowed;

import org.glassfish.jersey.internal.inject.CustomAnnotationLiteral;
import org.glassfish.jersey.message.filtering.spi.FilteringHelper;

import org.junit.Test;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * {@link org.glassfish.jersey.message.filtering.SecurityHelper} unit tests.
 *
 * @author Michal Gajdos
 */
public class SecurityHelperTest {

    @Test
    public void testFilteringScopes() throws Exception {
        Annotation[] annotations;
        Set<String> expected;

        // Empty annotations.
        annotations = new Annotation[0];
        assertThat(SecurityHelper.getFilteringScopes(annotations), equalTo(Collections.<String>emptySet()));

        // Not security annotations.
        annotations = new Annotation[] {CustomAnnotationLiteral.INSTANCE, CustomAnnotationLiteral.INSTANCE};
        assertThat(SecurityHelper.getFilteringScopes(annotations), equalTo(Collections.<String>emptySet()));

        // Mixed.
        annotations = new Annotation[] {CustomAnnotationLiteral.INSTANCE, SecurityAnnotations
                .rolesAllowed("manager"), CustomAnnotationLiteral.INSTANCE};
        expected = Collections.singleton(RolesAllowed.class.getName() + "_manager");
        assertThat(SecurityHelper.getFilteringScopes(annotations), equalTo(expected));

        // Multiple.
        annotations = new Annotation[] {SecurityAnnotations.rolesAllowed("manager", "user")};
        expected = Arrays.asList(RolesAllowed.class.getName() + "_manager", RolesAllowed.class.getName() + "_user")
                         .stream()
                         .collect(Collectors.toSet());
        assertThat(SecurityHelper.getFilteringScopes(annotations), equalTo(expected));

        // PermitAll weirdo.
        annotations = new Annotation[] {SecurityAnnotations.permitAll()};
        assertThat(SecurityHelper.getFilteringScopes(annotations), equalTo(FilteringHelper.getDefaultFilteringScope()));

        // DenyAll weirdo.
        annotations = new Annotation[] {SecurityAnnotations.denyAll()};
        assertThat(SecurityHelper.getFilteringScopes(annotations), equalTo(null));
    }

    @Test
    public void testFilteringScopesWithContext() throws Exception {
        final SecurityContext context = new TestSecurityContext();

        Annotation[] annotations;
        Set<String> expected;

        // Empty annotations.
        annotations = new Annotation[0];
        assertThat(SecurityHelper.getFilteringScopes(context, annotations), equalTo(Collections.<String>emptySet()));

        // Not security annotations.
        annotations = new Annotation[] {CustomAnnotationLiteral.INSTANCE, CustomAnnotationLiteral.INSTANCE};
        assertThat(SecurityHelper.getFilteringScopes(context, annotations), equalTo(Collections.<String>emptySet()));

        // Mixed.
        annotations = new Annotation[] {CustomAnnotationLiteral.INSTANCE, SecurityAnnotations
                .rolesAllowed("manager"), CustomAnnotationLiteral.INSTANCE};
        expected = Collections.singleton(RolesAllowed.class.getName() + "_manager");
        assertThat(SecurityHelper.getFilteringScopes(context, annotations), equalTo(expected));

        // Multiple.
        annotations = new Annotation[] {SecurityAnnotations.rolesAllowed("client", "user")};
        expected = Collections.singleton(RolesAllowed.class.getName() + "_user");
        assertThat(SecurityHelper.getFilteringScopes(context, annotations), equalTo(expected));

        // PermitAll weirdo.
        annotations = new Annotation[] {SecurityAnnotations.permitAll()};
        assertThat(SecurityHelper.getFilteringScopes(context, annotations), equalTo(FilteringHelper.getDefaultFilteringScope()));

        // DenyAll weirdo.
        annotations = new Annotation[] {SecurityAnnotations.denyAll()};
        assertThat(SecurityHelper.getFilteringScopes(context, annotations), equalTo(null));
    }

}
