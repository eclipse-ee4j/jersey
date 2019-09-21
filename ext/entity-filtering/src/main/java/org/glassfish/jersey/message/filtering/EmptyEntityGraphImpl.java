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

import org.glassfish.jersey.message.filtering.spi.EntityGraph;

/**
 * {@link EntityGraph} implementation that does not contain any fields/subgraphs. Methods that are supposed to modify the graph
 * would throw an {@link UnsupportedOperationException}.
 *
 * @author Michal Gajdos
 */
final class EmptyEntityGraphImpl implements EntityGraph {

    private final Class<?> clazz;

    @SuppressWarnings("JavaDoc")
    EmptyEntityGraphImpl(final Class<?> clazz) {
        this.clazz = clazz;
    }

    @Override
    public EntityGraph addField(final String fieldName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public EntityGraph addField(final String fieldName, final String... filteringScopes) {
        throw new UnsupportedOperationException();
    }

    @Override
    public EntityGraph addField(final String fieldName, final Set<String> filteringScopes) {
        throw new UnsupportedOperationException();
    }

    @Override
    public EntityGraph addSubgraph(final String fieldName, final Class<?> fieldClass) {
        throw new UnsupportedOperationException();
    }

    @Override
    public EntityGraph addSubgraph(final String fieldName, final Class<?> fieldClass, final String... filteringScopes) {
        throw new UnsupportedOperationException();
    }

    @Override
    public EntityGraph addSubgraph(final String fieldName, final Class<?> fieldClass, final Set<String> filteringScopes) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Class<?> getEntityClass() {
        return clazz;
    }

    @Override
    public Set<String> getFields(final String filteringScope) {
        return Collections.emptySet();
    }

    @Override
    public Set<String> getFields(final String... filteringScopes) {
        return Collections.emptySet();
    }

    @Override
    public Set<String> getFields(final Set<String> filteringScopes) {
        return Collections.emptySet();
    }

    @Override
    public Map<String, Class<?>> getSubgraphs(final String filteringScope) {
        return Collections.emptyMap();
    }

    @Override
    public Map<String, Class<?>> getSubgraphs(final String... filteringScopes) {
        return Collections.emptyMap();
    }

    @Override
    public Map<String, Class<?>> getSubgraphs(final Set<String> filteringScopes) {
        return Collections.emptyMap();
    }

    @Override
    public boolean presentInScopes(final String field) {
        return false;
    }

    @Override
    public boolean presentInScope(final String field, String filteringScope) {
        return false;
    }

    @Override
    public EntityGraph remove(final String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<String> getFilteringScopes() {
        return Collections.emptySet();
    }

    @Override
    public Set<String> getClassFilteringScopes() {
        return Collections.emptySet();
    }

    @Override
    public EntityGraph addFilteringScopes(final Set<String> filteringScopes) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final EmptyEntityGraphImpl that = (EmptyEntityGraphImpl) o;

        return clazz.equals(that.clazz);

    }

    @Override
    public int hashCode() {
        return clazz.hashCode();
    }
}
