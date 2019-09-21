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

import javax.annotation.Priority;
import javax.inject.Singleton;

import org.glassfish.jersey.message.filtering.spi.AbstractEntityProcessor;
import org.glassfish.jersey.message.filtering.spi.EntityGraph;
import org.glassfish.jersey.message.filtering.spi.EntityProcessor;

/**
 * {@link EntityProcessor Entity processor} handling security annotations on model entity classes.
 *
 * @author Michal Gajdos
 */
@Singleton
@Priority(Integer.MAX_VALUE - 3000)
final class SecurityEntityProcessor extends AbstractEntityProcessor {

    @Override
    protected Result process(final String fieldName, final Class<?> fieldClass, final Annotation[] fieldAnnotations,
                             final Annotation[] annotations, final EntityGraph graph) {
        if (annotations.length > 0) {
            final Set<String> filteringScopes = SecurityHelper.getFilteringScopes(annotations);

            if (filteringScopes == null) {
                return EntityProcessor.Result.ROLLBACK;
            } else if (!filteringScopes.isEmpty()) {
                if (fieldName != null) {
                    // For field.
                    addFilteringScopes(fieldName, fieldClass, filteringScopes, graph);
                } else {
                    // For entire class into graph.
                    addGlobalScopes(filteringScopes, graph);
                }
            }
        }

        return EntityProcessor.Result.APPLY;
    }
}
