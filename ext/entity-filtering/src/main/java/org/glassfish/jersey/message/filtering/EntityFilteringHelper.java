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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.glassfish.jersey.message.filtering.spi.FilteringHelper;

/**
 * Utility methods for Entity Data Filtering.
 *
 * @author Michal Gajdos
 */
final class EntityFilteringHelper {

    /**
     * Get entity-filtering scopes from given annotations. Scopes are only derived from entity-filtering annotations.
     *
     * @param annotations list of arbitrary annotations.
     * @return a set of entity-filtering scopes.
     */
    public static Set<String> getFilteringScopes(final Annotation[] annotations) {
        return getFilteringScopes(annotations, true);
    }

    /**
     * Get entity-filtering scopes from given annotations. Scopes are only derived from entity-filtering annotations.
     *
     * @param annotations list of arbitrary annotations.
     * @param filter {@code true} whether the given annotation should be reduced to only entity-filtering annotations,
     * {@code false} otherwise.
     * @return a set of entity-filtering scopes.
     */
    public static Set<String> getFilteringScopes(Annotation[] annotations, final boolean filter) {
        if (annotations.length == 0) {
            return Collections.emptySet();
        }

        final Set<String> contexts = new HashSet<>(annotations.length);

        annotations = filter ? getFilteringAnnotations(annotations) : annotations;
        for (final Annotation annotation : annotations) {
            contexts.add(annotation.annotationType().getName());
        }

        return contexts;
    }

    /**
     * Filter given annotations and return only entity-filtering ones.
     *
     * @param annotations list of arbitrary annotations.
     * @return entity-filtering annotations or an empty array.
     */
    public static Annotation[] getFilteringAnnotations(final Annotation[] annotations) {
        if (annotations == null || annotations.length == 0) {
            return FilteringHelper.EMPTY_ANNOTATIONS;
        }

        final List<Annotation> filteringAnnotations = new ArrayList<>(annotations.length);

        for (final Annotation annotation : annotations) {
            final Class<? extends Annotation> annotationType = annotation.annotationType();

            for (final Annotation metaAnnotation : annotationType.getDeclaredAnnotations()) {
                if (metaAnnotation instanceof EntityFiltering) {
                    filteringAnnotations.add(annotation);
                }
            }
        }

        return filteringAnnotations.toArray(new Annotation[filteringAnnotations.size()]);
    }

    public static <T extends Annotation> T getAnnotation(final Annotation[] annotations, final Class<T> clazz) {
        for (final Annotation annotation : annotations) {
            if (annotation.annotationType().getClass().isAssignableFrom(clazz)) {
                //noinspection unchecked
                return (T) annotation;
            }
        }
        return null;
    }

    /**
     * Prevent instantiation.
     */
    private EntityFilteringHelper() {
    }
}
