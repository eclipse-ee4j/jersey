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

import java.util.Map;
import java.util.Set;

/**
 * Class available to {@link EntityProcessor entity-filtering processors} providing means to add/remove entity-filtering scopes
 * (e.g. based on annotations) for entity members.
 * <p/>
 * Differences between this class and {@link ObjectGraph object graph}:
 * <ul>
 * <li>{@code EntityGraph} can be modified, {@code ObjectGraph} is read-only.</li>
 * <li>{@code EntityGraph} contains information about all entity-filtering scopes found during inspecting an entity class,
 * {@code ObjectGraph} provides information about entity to create a filtering object for a subset of these scopes
 * (which are determined from the current context).</li>
 * </ul>
 * <p/>
 * Note: Definition of entity-filtering scopes can be found in {@link ScopeResolver}.
 *
 * @author Michal Gajdos
 * @see ScopeResolver
 */
public interface EntityGraph {

    /**
     * Add a field into this graph for all existing entity-filtering scopes.
     *
     * @param fieldName name of the field to be added.
     * @return an entity-filtering graph instance.
     */
    public EntityGraph addField(final String fieldName);

    /**
     * Add a field into this graph for given list of entity-filtering scopes.
     *
     * @param fieldName name of the field to be added.
     * @param filteringScopes entity-filtering scopes for the field.
     * @return an entity-filtering graph instance.
     */
    public EntityGraph addField(final String fieldName, final String... filteringScopes);

    /**
     * Add a field into this graph for given set of entity-filtering scopes.
     *
     * @param fieldName name of the field to be added.
     * @param filteringScopes entity-filtering scopes for the field.
     * @return an entity-filtering graph instance.
     */
    public EntityGraph addField(final String fieldName, final Set<String> filteringScopes);

    /**
     * Add a subgraph into this graph for all existing entity-filtering scopes.
     *
     * @param fieldName name of the subgraph field to be added.
     * @param fieldClass entity class representing the subgraph.
     * @return an entity-filtering graph instance.
     */
    public EntityGraph addSubgraph(final String fieldName, final Class<?> fieldClass);

    /**
     * Add a subgraph into this graph for given list of entity-filtering scopes.
     *
     * @param fieldName name of the subgraph field to be added.
     * @param fieldClass entity class representing the subgraph.
     * @param filteringScopes entity-filtering scopes for the subgraph.
     * @return an entity-filtering graph instance.
     */
    public EntityGraph addSubgraph(final String fieldName, final Class<?> fieldClass, final String... filteringScopes);

    /**
     * Add a subgraph into this graph for given set of entity-filtering scopes.
     *
     * @param fieldName name of the subgraph field to be added.
     * @param fieldClass entity class representing the subgraph.
     * @param filteringScopes entity-filtering scopes for the subgraph.
     * @return an entity-filtering graph instance.
     */
    public EntityGraph addSubgraph(final String fieldName, final Class<?> fieldClass, final Set<String> filteringScopes);

    /**
     * Add a set of entity-filtering scopes to this graph.
     *
     * @param filteringScopes entity-filtering scopes to be added.
     * @return an entity-filtering graph instance.
     */
    public EntityGraph addFilteringScopes(final Set<String> filteringScopes);

    /**
     * Determines whether a field/subgraph is present in ANY of the given scopes. If no scopes are given the return value
     * determines whether the field is present in any scope.
     *
     * @param field field to be checked.
     * @param filteringScope entity-filtering scope.
     * @return {@code true} if field is present in the given scope, {@code false} otherwise.
     */
    public boolean presentInScope(final String field, final String filteringScope);

    /**
     * Determines whether a field/subgraph is present in ANY of the existing scopes.
     *
     * @param field field to be checked.
     * @return {@code true} if field is present in ANY of the existing scopes, {@code false} otherwise.
     */
    public boolean presentInScopes(final String field);

    /**
     * Get an entity class this graph is created for.
     *
     * @return an entity class.
     */
    public Class<?> getEntityClass();

    /**
     * Get fields for given entity-filtering scope.
     *
     * @param filteringScope scope the returned fields have to be in.
     * @return set of fields present in given scope.
     */
    public Set<String> getFields(final String filteringScope);

    /**
     * Get fields for given entity-filtering scopes.
     *
     * @param filteringScopes scopes the returned fields have to be in.
     * @return set of fields present in given scopes.
     */
    public Set<String> getFields(final String... filteringScopes);

    /**
     * Get fields for given entity-filtering scopes.
     *
     * @param filteringScopes scopes the returned fields have to be in.
     * @return set of fields present in given scopes.
     */
    public Set<String> getFields(final Set<String> filteringScopes);

    /**
     * Get all available entity-filtering scopes.
     *
     * @return all available entity-filtering scopes.
     */
    public Set<String> getFilteringScopes();

    /**
     * Get all available entity-filtering scopes defined on a class.
     *
     * @return all available entity-filtering scopes.
     */
    public Set<String> getClassFilteringScopes();

    /**
     * Get subgraphs for given entity-filtering scope.
     *
     * @param filteringScope scope the returned subgraphs have to be in.
     * @return map of subgraphs present in given scope.
     */
    public Map<String, Class<?>> getSubgraphs(final String filteringScope);

    /**
     * Get subgraphs for given entity-filtering scopes.
     *
     * @param filteringScopes scopes the returned subgraphs have to be in.
     * @return map of subgraphs present in given scopes.
     */
    public Map<String, Class<?>> getSubgraphs(final String... filteringScopes);

    /**
     * Get subgraphs for given entity-filtering scopes.
     *
     * @param filteringScopes scopes the returned subgraphs have to be in.
     * @return map of subgraphs present in given scopes.
     */
    public Map<String, Class<?>> getSubgraphs(final Set<String> filteringScopes);

    /**
     * Remove a field/subgraph from the graph (all entity-filtering scopes).
     *
     * @param name name of the field/subgraph to be removed.
     * @return an entity-filtering graph instance.
     */
    public EntityGraph remove(final String name);
}
