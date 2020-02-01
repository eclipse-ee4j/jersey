/*
 * Copyright (c) 2013, 2020 Oracle and/or its affiliates. All rights reserved.
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
import java.util.HashSet;
import java.util.Set;

import jakarta.ws.rs.ConstrainedTo;
import jakarta.ws.rs.RuntimeType;
import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.SecurityContext;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.glassfish.jersey.internal.inject.InjectionManager;

/**
 * @author Michal Gajdos
 */
@Singleton
@ConstrainedTo(RuntimeType.SERVER)
final class SecurityServerScopeProvider extends ServerScopeProvider {

    @Context
    private SecurityContext securityContext;

    @Inject
    public SecurityServerScopeProvider(final Configuration config, final InjectionManager injectionManager) {
        super(config, injectionManager);
    }

    @Override
    public Set<String> getFilteringScopes(final Annotation[] entityAnnotations, final boolean defaultIfNotFound) {
        Set<String> filteringScope = super.getFilteringScopes(entityAnnotations, false);

        if (filteringScope.isEmpty()) {
            filteringScope = new HashSet<>();

            // Get all roles collected from entities and check with current security context.
            for (final String role : SecurityHelper.getProcessedRoles()) {
                if (securityContext.isUserInRole(role)) {
                    filteringScope.add(SecurityHelper.getRolesAllowedScope(role));
                }
            }
        }

        // Use default scope if not in other scope.
        return returnFilteringScopes(filteringScope, defaultIfNotFound);
    }
}
