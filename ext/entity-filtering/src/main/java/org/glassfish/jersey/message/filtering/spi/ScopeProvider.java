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

package org.glassfish.jersey.message.filtering.spi;

import java.lang.annotation.Annotation;
import java.util.Set;

import org.glassfish.jersey.message.filtering.EntityFiltering;
import org.glassfish.jersey.spi.Contract;

/**
 * Entry point for obtaining entity-filtering scopes used to process a request/response entity. Entity-filtering scopes are
 * obtained from (sorted by priority):
 * <ul>
 * <li>entity annotations - provided with entity when creating request/response</li>
 * <li>annotations stored under
 * {@value org.glassfish.jersey.message.filtering.EntityFilteringFeature#ENTITY_FILTERING_SCOPE} property obtained from
 * {@link javax.security.auth.login.Configuration configuration}
 * </li>
 * <li>entity-filtering annotations present on resource methods/classes (on server)</li>
 * </ul>
 * <p/>
 * Note: Definition of entity-filtering scopes can be found in {@link ScopeResolver}.
 *
 * @author Michal Gajdos
 * @see ScopeResolver
 */
@Contract
public interface ScopeProvider {

    /**
     * Default entity-filtering scope.
     * <p/>
     * Default scope is used in {@link ObjectGraph object graph} to retrieve a subgraph instance at the moment subgraph's entity
     * class does not define any entity-filtering scope the object graph was created for.
     * <p/>
     * This scope is created for an {@link EntityGraph entity graph} if no other entity-filtering / security annotation is present
     * on a class.
     */
    public static final String DEFAULT_SCOPE = EntityFiltering.class.getName();

    /**
     * Get entity-filtering scopes to be used to process an entity.
     *
     * @param entityAnnotations entity annotations provided with entity when creating request/response.
     * @param defaultIfNotFound flag determining whether the default entity-filtering scope should be returned if no other
     * scope can be obtained.
     * @return non-null entity-filtering scopes.
     */
    public Set<String> getFilteringScopes(final Annotation[] entityAnnotations, final boolean defaultIfNotFound);
}
