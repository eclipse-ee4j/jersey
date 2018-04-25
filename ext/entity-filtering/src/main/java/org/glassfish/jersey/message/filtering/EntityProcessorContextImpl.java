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

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.glassfish.jersey.message.filtering.spi.EntityGraph;
import org.glassfish.jersey.message.filtering.spi.EntityProcessorContext;

/**
 * Default {@link EntityProcessorContext entity processor context} implementation.
 *
 * @author Michal Gajdos
 */
final class EntityProcessorContextImpl implements EntityProcessorContext {

    private final Type type;

    private final Class<?> clazz;

    private final Field field;

    private final Method method;

    private final EntityGraph graph;

    /**
     * Create entity processor context for processing entity classes.
     *
     * @param type {@link Type#CLASS_READER} or {@link Type#CLASS_WRITER}.
     * @param clazz entity class.
     * @param graph entity-filtering graph associated with entity class.
     */
    public EntityProcessorContextImpl(final Type type, final Class<?> clazz, final EntityGraph graph) {
        this(type, clazz, null, null, graph);
    }

    /**
     * Create entity processor context for processing entity properties.
     *
     * @param type {@link Type#PROPERTY_READER} or {@link Type#PROPERTY_WRITER}.
     * @param field entity property field.
     * @param method entity property accessor.
     * @param graph entity-filtering graph associated with entity class.
     */
    public EntityProcessorContextImpl(final Type type, final Field field, final Method method, final EntityGraph graph) {
        this(type, null, field, method, graph);
    }

    /**
     * Create entity processor context for processing entity accessors.
     *
     * @param type {@link Type#METHOD_READER} or {@link Type#METHOD_WRITER}.
     * @param method entity property accessor.
     * @param graph entity-filtering graph associated with entity class.
     */
    public EntityProcessorContextImpl(final Type type, final Method method, final EntityGraph graph) {
        this(type, null, null, method, graph);
    }

    /**
     * Create entity processor context for processing entity accessors.
     *
     * @param type type on entity processor context.
     * @param clazz entity class.
     * @param field entity property field.
     * @param method entity property method.
     * @param graph entity-filtering graph associated with entity class.
     */
    public EntityProcessorContextImpl(final Type type, final Class<?> clazz, final Field field, final Method method,
                                      final EntityGraph graph) {
        this.type = type;
        this.clazz = clazz;
        this.field = field;
        this.method = method;
        this.graph = graph;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public Class<?> getEntityClass() {
        return clazz;
    }

    @Override
    public Field getField() {
        return field;
    }

    @Override
    public Method getMethod() {
        return method;
    }

    @Override
    public EntityGraph getEntityGraph() {
        return graph;
    }
}
