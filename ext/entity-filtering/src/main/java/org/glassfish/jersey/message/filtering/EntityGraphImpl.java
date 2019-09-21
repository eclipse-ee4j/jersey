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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.glassfish.jersey.internal.guava.HashBasedTable;
import org.glassfish.jersey.internal.guava.HashMultimap;
import org.glassfish.jersey.internal.guava.Table;
import org.glassfish.jersey.message.filtering.spi.EntityGraph;
import org.glassfish.jersey.message.filtering.spi.ScopeProvider;

/**
 * Default implementation of {@link EntityGraph}.
 *
 * @author Michal Gajdos
 */
final class EntityGraphImpl implements EntityGraph {

    private final Class<?> entityClass;

    private final Set<String> globalScopes;
    private final Set<String> localScopes;

    // <FilteringScope, FieldName>
    private final HashMultimap<String, String> fields;
    // <FilteringScope, FieldName, Class>
    private final Table<String, String, Class<?>> subgraphs;

    /**
     * Create an entity graph for given class.
     *
     * @param entityClass entity class the graph should be created for.
     */
    public EntityGraphImpl(final Class<?> entityClass) {
        this.entityClass = entityClass;

        this.fields = HashMultimap.create();
        this.subgraphs = HashBasedTable.create();

        this.globalScopes = new HashSet<>();
        this.localScopes = new HashSet<>();
    }

    @Override
    public EntityGraphImpl addField(final String fieldName) {
        return addField(fieldName, globalScopes);
    }

    @Override
    public EntityGraphImpl addField(final String fieldName, final String... filteringScopes) {
        return addField(fieldName, Arrays.stream(filteringScopes).collect(Collectors.toSet()));
    }

    @Override
    public EntityGraphImpl addField(final String fieldName, final Set<String> filteringScopes) {
        for (final String filteringScope : filteringScopes) {
            createFilteringScope(filteringScope);
            fields.get(filteringScope).add(fieldName);
        }

        return this;
    }

    @Override
    public EntityGraphImpl addFilteringScopes(final Set<String> filteringScopes) {
        this.globalScopes.addAll(filteringScopes);
        return this;
    }

    @Override
    public EntityGraphImpl addSubgraph(final String fieldName, final Class<?> fieldClass) {
        return addSubgraph(fieldName, fieldClass, globalScopes);
    }

    @Override
    public EntityGraphImpl addSubgraph(final String fieldName, final Class<?> fieldClass, final String... filteringScopes) {
        return addSubgraph(fieldName, fieldClass, Arrays.stream(filteringScopes).collect(Collectors.toSet()));
    }

    @Override
    public EntityGraphImpl addSubgraph(final String fieldName, final Class<?> fieldClass, final Set<String> filteringScopes) {
        for (final String filteringScope : filteringScopes) {
            createFilteringScope(filteringScope);
            subgraphs.put(filteringScope, fieldName, fieldClass);
        }

        return this;
    }

    @Override
    public Class<?> getEntityClass() {
        return entityClass;
    }

    @Override
    public Set<String> getFields(final String filteringScope) {
        return fields.containsKey(filteringScope)
                ? Collections.unmodifiableSet(fields.get(filteringScope)) : Collections.<String>emptySet();
    }

    @Override
    public Set<String> getFields(final String... filteringScopes) {
        return filteringScopes.length == 0 ? Collections.<String>emptySet()
                : (filteringScopes.length == 1
                ? getFields(filteringScopes[0])
                : getFields(Arrays.stream(filteringScopes).collect(Collectors.toSet())));
    }

    @Override
    public Set<String> getFields(final Set<String> filteringScopes) {
        final Set<String> matched = new HashSet<>();

        for (final String filteringContext : filteringScopes) {
            matched.addAll(fields.get(filteringContext));
        }

        return matched;
    }

    @Override
    public Set<String> getFilteringScopes() {
        HashSet<String> strings = new HashSet<>(globalScopes);
        strings.addAll(localScopes);
        return Collections.unmodifiableSet(strings);
    }

    @Override
    public Set<String> getClassFilteringScopes() {
        return Collections.unmodifiableSet(globalScopes);
    }

    @Override
    public Map<String, Class<?>> getSubgraphs(final String filteringScope) {
        return subgraphs.containsRow(filteringScope)
                ? Collections.unmodifiableMap(subgraphs.row(filteringScope)) : Collections.<String, Class<?>>emptyMap();
    }

    @Override
    public Map<String, Class<?>> getSubgraphs(final String... filteringScopes) {
        return filteringScopes.length == 0
                ? Collections.<String, Class<?>>emptyMap()
                : (filteringScopes.length == 1
                           ? getSubgraphs(filteringScopes[0])
                           : getSubgraphs(Arrays.stream(filteringScopes).collect(Collectors.toSet())));
    }

    @Override
    public Map<String, Class<?>> getSubgraphs(final Set<String> filteringScopes) {
        final Map<String, Class<?>> matched = new HashMap<>();

        for (final String filteringContext : filteringScopes) {
            matched.putAll(subgraphs.row(filteringContext));
        }

        return matched;
    }

    @Override
    public boolean presentInScopes(final String name) {
        return fields.containsValue(name) || subgraphs.containsColumn(name);
    }

    @Override
    public boolean presentInScope(final String field, final String filteringScope) {
        return fields.containsEntry(filteringScope, field) || subgraphs.contains(filteringScope, field);
    }

    @Override
    public EntityGraphImpl remove(final String fieldName) {
        for (final String scope : getFilteringScopes()) {
            if (fields.containsEntry(scope, fieldName)) {
                fields.remove(scope, fieldName);
            }
            if (subgraphs.containsColumn(fieldName)) {
                subgraphs.remove(scope, fieldName);
            }
        }
        return this;
    }

    /**
     * Create a new entity-filtering scope based on the {@link ScopeProvider#DEFAULT_SCOPE default one}.
     *
     * @param filteringScope entity-filtering scope to be created.
     */
    private void createFilteringScope(final String filteringScope) {
        // Do not create a scope if it already exists.
        if (!getFilteringScopes().contains(filteringScope)) {
            // Copy contents of default scope into the new one.
            if (localScopes.contains(ScopeProvider.DEFAULT_SCOPE)) {
                fields.putAll(filteringScope, fields.get(ScopeProvider.DEFAULT_SCOPE));

                final Map<String, Class<?>> row = subgraphs.row(ScopeProvider.DEFAULT_SCOPE);
                for (final Map.Entry<String, Class<?>> entry : row.entrySet()) {
                    subgraphs.put(filteringScope, entry.getKey(), entry.getValue());
                }
            }
            localScopes.add(filteringScope);
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final EntityGraphImpl that = (EntityGraphImpl) o;

        return entityClass.equals(that.entityClass)
                && fields.equals(that.fields)
                && globalScopes.equals(that.globalScopes)
                && localScopes.equals(that.localScopes)
                && subgraphs.equals(that.subgraphs);
    }

    @Override
    public int hashCode() {
        int result = entityClass.hashCode();
        result = 53 * result + globalScopes.hashCode();
        result = 53 * result + localScopes.hashCode();
        result = 53 * result + fields.hashCode();
        result = 53 * result + subgraphs.hashCode();
        return result;
    }
}
