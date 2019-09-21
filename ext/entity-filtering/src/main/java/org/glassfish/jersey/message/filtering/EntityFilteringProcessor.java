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
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Priority;
import javax.inject.Singleton;

import org.glassfish.jersey.message.filtering.spi.AbstractEntityProcessor;
import org.glassfish.jersey.message.filtering.spi.EntityGraph;
import org.glassfish.jersey.message.filtering.spi.EntityProcessorContext;

/**
 * Entity processor handling entity-filtering annotations.
 *
 * @author Michal Gajdos
 */
@Singleton
@Priority(Integer.MAX_VALUE - 2000)
final class EntityFilteringProcessor extends AbstractEntityProcessor {

    @Override
    public Result process(final EntityProcessorContext context) {
        switch (context.getType()) {
            case CLASS_READER:
            case CLASS_WRITER:
                addGlobalScopes(EntityFilteringHelper.getFilteringScopes(context.getEntityClass().getDeclaredAnnotations()),
                        context.getEntityGraph());
                break;
            default:
                // NOOP.
                break;
        }
        return super.process(context);
    }

    @Override
    protected Result process(final String field, final Class<?> fieldClass, final Annotation[] fieldAnnotations,
                             final Annotation[] annotations, final EntityGraph graph) {
        final Set<String> filteringScopes = new HashSet<>();

        if (fieldAnnotations.length > 0) {
            filteringScopes.addAll(EntityFilteringHelper.getFilteringScopes(fieldAnnotations));
        }
        if (annotations.length > 0) {
            filteringScopes.addAll(EntityFilteringHelper.getFilteringScopes(annotations));
        }

        if (!filteringScopes.isEmpty()) {
            if (field != null) {
                addFilteringScopes(field, fieldClass, filteringScopes, graph);
            } else {
                addGlobalScopes(filteringScopes, graph);
            }
            return Result.APPLY;
        } else {
            return Result.SKIP;
        }
    }
}
