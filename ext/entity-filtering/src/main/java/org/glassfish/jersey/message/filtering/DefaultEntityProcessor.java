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
import java.lang.reflect.Type;

import javax.annotation.Priority;
import javax.inject.Singleton;

import org.glassfish.jersey.internal.util.ReflectionHelper;
import org.glassfish.jersey.message.filtering.spi.AbstractEntityProcessor;
import org.glassfish.jersey.message.filtering.spi.EntityGraph;
import org.glassfish.jersey.message.filtering.spi.EntityProcessorContext;
import org.glassfish.jersey.message.filtering.spi.FilteringHelper;

/**
 * Default entity processor. Handles unannotated properties/accessors and adds them into default entity-filtering scope.
 *
 * @author Michal Gajdos
 */
@Singleton
@Priority(Integer.MAX_VALUE - 1000)
final class DefaultEntityProcessor extends AbstractEntityProcessor {

    @Override
    public Result process(final EntityProcessorContext context) {
        switch (context.getType()) {
            case CLASS_READER:
            case CLASS_WRITER:
                final EntityGraph graph = context.getEntityGraph();
                if (graph.getFilteringScopes().isEmpty()) {
                    graph.addFilteringScopes(FilteringHelper.getDefaultFilteringScope());
                }
                return Result.APPLY;

            case PROPERTY_READER:
            case PROPERTY_WRITER:
                final Field field = context.getField();
                process(context.getEntityGraph(), field.getName(), field.getGenericType());
                return Result.APPLY;

            case METHOD_READER:
            case METHOD_WRITER:
                final Method method = context.getMethod();
                process(context.getEntityGraph(), ReflectionHelper.getPropertyName(method), method.getGenericReturnType());
                return Result.APPLY;

            default:
                // NOOP.
        }
        return Result.SKIP;
    }

    private void process(final EntityGraph graph, final String fieldName, final Type fieldType) {
        if (!graph.presentInScopes(fieldName)) {
            addFilteringScopes(fieldName, FilteringHelper.getEntityClass(fieldType), graph.getClassFilteringScopes(), graph);
        }
    }
}
