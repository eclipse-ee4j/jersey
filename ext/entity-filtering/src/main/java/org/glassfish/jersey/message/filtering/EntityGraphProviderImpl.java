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
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import org.glassfish.jersey.internal.util.collection.DataStructures;
import org.glassfish.jersey.message.filtering.spi.EntityGraph;
import org.glassfish.jersey.message.filtering.spi.EntityGraphProvider;
import org.glassfish.jersey.message.filtering.spi.ObjectGraph;

/**
 * Provides {@link EntityGraph entity graph} and {@link ObjectGraph object graph} instances.
 *
 * @author Michal Gajdos
 */
final class EntityGraphProviderImpl implements EntityGraphProvider {

    private final ConcurrentMap<Class<?>, EntityGraph> writerClassToGraph = DataStructures.createConcurrentMap();
    private final ConcurrentMap<Class<?>, EntityGraph> readerClassToGraph = DataStructures.createConcurrentMap();

    @Override
    public EntityGraph getOrCreateEntityGraph(final Class<?> entityClass, final boolean forWriter) {
        final ConcurrentMap<Class<?>, EntityGraph> classToGraph = forWriter ? writerClassToGraph : readerClassToGraph;

        if (!classToGraph.containsKey(entityClass)) {
            classToGraph.putIfAbsent(entityClass, new EntityGraphImpl(entityClass));
        }
        return classToGraph.get(entityClass);
    }

    @Override
    public EntityGraph getOrCreateEmptyEntityGraph(final Class<?> entityClass, final boolean forWriter) {
        final ConcurrentMap<Class<?>, EntityGraph> classToGraph = forWriter ? writerClassToGraph : readerClassToGraph;

        if (!classToGraph.containsKey(entityClass)
                || !(classToGraph.get(entityClass) instanceof EmptyEntityGraphImpl)) {
            classToGraph.put(entityClass, new EmptyEntityGraphImpl(entityClass));
        }
        return classToGraph.get(entityClass);
    }

    /**
     * Return an unmodifiable map of entity graphs for reader/writer.
     *
     * @param forWriter flag determining whether the returned map should be for writer/reader.
     * @return an unmodifiable map of entity graphs.
     */
    public Map<Class<?>, EntityGraph> asMap(final boolean forWriter) {
        return Collections.unmodifiableMap(forWriter ? writerClassToGraph : readerClassToGraph);
    }

    @Override
    public boolean containsEntityGraph(final Class<?> entityClass, final boolean forWriter) {
        return forWriter ? writerClassToGraph.containsKey(entityClass) : readerClassToGraph.containsKey(entityClass);
    }

    @Override
    public ObjectGraph createObjectGraph(final Class<?> entityClass, final Set<String> filteringScopes,
                                         final boolean forWriter) {
        final Map<Class<?>, EntityGraph> classToGraph = forWriter ? writerClassToGraph : readerClassToGraph;
        final EntityGraph entityGraph = classToGraph.get(entityClass);

        return entityGraph == null
                ? new EmptyObjectGraph(entityClass)
                : new ObjectGraphImpl(classToGraph, entityGraph, filteringScopes);
    }
}
