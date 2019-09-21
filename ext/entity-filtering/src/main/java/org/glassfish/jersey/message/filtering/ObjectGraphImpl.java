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

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.glassfish.jersey.internal.util.collection.Views;
import org.glassfish.jersey.message.filtering.spi.EntityGraph;
import org.glassfish.jersey.message.filtering.spi.ObjectGraph;
import org.glassfish.jersey.message.filtering.spi.ScopeProvider;

/**
 * Default implementation of {@link ObjectGraph}.
 *
 * @author Michal Gajdos
 */
final class ObjectGraphImpl implements ObjectGraph {

    private final Set<String> filteringScopes;

    private final Map<Class<?>, EntityGraph> classToGraph;
    private final EntityGraph graph;

    private Set<String> fields;
    private Map<String, ObjectGraph> subgraphs;

    ObjectGraphImpl(final Map<Class<?>, EntityGraph> classToGraph, final EntityGraph graph, final Set<String> filteringScopes) {
        this.filteringScopes = filteringScopes;

        this.classToGraph = classToGraph;
        this.graph = graph;
    }

    @Override
    public Class<?> getEntityClass() {
        return graph.getEntityClass();
    }

    @Override
    public Set<String> getFields() {
        return getFields(null);
    }

    @Override
    public Set<String> getFields(final String parent) {
        final Set<String> childFilteringScopes = getFilteringScopes(parent);
        if (fields == null) {
            fields = graph.getFields(
                    Views.setUnionView(
                            childFilteringScopes,
                            Collections.singleton(ScopeProvider.DEFAULT_SCOPE)));
        }
        return fields;
    }

    @Override
    public Map<String, ObjectGraph> getSubgraphs() {
        return getSubgraphs(null);
    }

    @Override
    public Map<String, ObjectGraph> getSubgraphs(final String parent) {
        final Set<String> childFilteringScopes = getFilteringScopes(parent);

        if (subgraphs == null) {
            final Map<String, Class<?>> contextSubgraphs = graph.getSubgraphs(childFilteringScopes);
            contextSubgraphs.putAll(graph.getSubgraphs(ScopeProvider.DEFAULT_SCOPE));

            subgraphs = Views.mapView(contextSubgraphs, new Function<Class<?>, ObjectGraph>() {

                @Override
                public ObjectGraph apply(final Class<?> clazz) {
                    final EntityGraph entityGraph = classToGraph.get(clazz);

                    return entityGraph == null
                        ? new EmptyObjectGraph(clazz)
                        : new ObjectGraphImpl(classToGraph, entityGraph, filteringScopes);
                }
            });
        }
        return subgraphs;
    }

    private Set<String> getFilteringScopes(final String parent) {
        Set<String> childFilteringScopes = new HashSet<>();
        if (filteringScopes.contains(SelectableScopeResolver.DEFAULT_SCOPE) || parent == null) {
            childFilteringScopes = filteringScopes;
        } else {
            for (final String filteringScope : filteringScopes) {
                final Pattern p = Pattern.compile(SelectableScopeResolver.PREFIX + parent + "\\.(\\w+)(\\.\\w+)*$");
                final Matcher m = p.matcher(filteringScope);
                if (m.matches()) {
                    childFilteringScopes.add(SelectableScopeResolver.PREFIX + m.group(1));
                } else {
                    childFilteringScopes.add(filteringScope);
                }
            }
        }
        return childFilteringScopes;
    }

}
