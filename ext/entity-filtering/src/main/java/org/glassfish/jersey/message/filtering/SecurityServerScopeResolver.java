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
import java.util.Set;

import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.Priorities;
import javax.ws.rs.RuntimeType;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;

import javax.annotation.Priority;
import javax.inject.Singleton;

import org.glassfish.jersey.message.filtering.spi.ScopeResolver;

/**
 * Server-side {@link ScopeResolver scope provider} resolving entity-filtering scopes from security annotations
 * with respect to user's roles defined in {@link SecurityContext}.
 *
 * @author Michal Gajdos
 */
@Singleton
@Priority(Priorities.ENTITY_CODER + 100)
@ConstrainedTo(RuntimeType.SERVER)
final class SecurityServerScopeResolver implements ScopeResolver {

    @Context
    private SecurityContext securityContext;

    @Override
    public Set<String> resolve(final Annotation[] annotations) {
        return SecurityHelper.getFilteringScopes(securityContext, annotations);
    }
}
