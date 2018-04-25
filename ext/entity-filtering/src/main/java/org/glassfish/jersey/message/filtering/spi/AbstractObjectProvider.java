/*
 * Copyright (c) 2014, 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.lang.annotation.Annotation;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import org.glassfish.jersey.internal.guava.Cache;
import org.glassfish.jersey.internal.guava.CacheBuilder;

/**
 * Common implementation of {@link ObjectProvider object provider} and {@link ObjectGraphTransformer object graph transformer}.
 * <p>
 * Extensions of this class must provide a type of entity-filtering object (via generic type parameter) the requesting provider
 * (e.g. message body worker) is familiar with and an implementation of
 * {@link ObjectGraphTransformer#transform(ObjectGraph)} method for this type.
 * </p>
 *
 * @param <T> representation of entity data filtering requested by provider.
 * @author Michal Gajdos
 */
public abstract class AbstractObjectProvider<T> implements ObjectProvider<T>, ObjectGraphTransformer<T> {

    private static final int PROVIDER_CACHE_SIZE = 1000;

    private final Cache<EntityContext, T> filteringObjects = CacheBuilder.newBuilder().maximumSize(PROVIDER_CACHE_SIZE).build();

    @Inject
    private ScopeProvider scopeProvider;

    @Inject
    private EntityInspector entityInspector;

    @Inject
    private EntityGraphProvider graphProvider;

    @Override
    public final T getFilteringObject(final Type genericType, final boolean forWriter, final Annotation... annotations) {
        return getFilteringObject(FilteringHelper.getEntityClass(genericType), forWriter, annotations);
    }

    private T getFilteringObject(final Class<?> entityClass, final boolean forWriter, final Annotation... annotations) {
        if (FilteringHelper.filterableEntityClass(entityClass)) {
            // Inspect.
            entityInspector.inspect(entityClass, forWriter);

            // Obtain runtime/resource scope.
            final Set<String> filteringScope = scopeProvider.getFilteringScopes(getEntityAnnotations(annotations), true);

            // Look into the cache.
            final EntityContext entityContext = new EntityContext(entityClass, filteringScope);
            T filteringObject = filteringObjects.getIfPresent(entityContext);

            // Create new if not available.
            if (filteringObject == null) {
                filteringObject = createFilteringObject(entityClass, filteringScope, forWriter);
                filteringObjects.put(entityContext, filteringObject);
            }

            return filteringObject;
        }
        return null;
    }

    /**
     * Get entity annotations passed to request/response. This method filters annotations that are proxy instances (proxy
     * annotations are taken from resource method and passed in this list).
     *
     * @param annotations annotations obtained from provider.
     * @return annoations passed to request/response.
     */
    private Annotation[] getEntityAnnotations(final Annotation[] annotations) {
        final ArrayList<Annotation> entityAnnotations = new ArrayList<>();

        for (final Annotation annotation : annotations) {
            if (!(annotation instanceof Proxy)) {
                entityAnnotations.add(annotation);
            }
        }

        return entityAnnotations.toArray(new Annotation[entityAnnotations.size()]);
    }

    /**
     * Create entity-filtering object after this object has not been found in the cache.
     *
     * @param entityClass     entity class the entity-filtering object should be created for.
     * @param filteringScopes entity-filtering scopes to create the entity-filtering object for.
     * @param forWriter       flag determining whether the class should be examined for reader or writer.
     * @return entity-filtering object.
     */
    private T createFilteringObject(final Class<?> entityClass, final Set<String> filteringScopes, final boolean forWriter) {
        // Obtain the filtering object.
        return transform(graphProvider.createObjectGraph(entityClass, filteringScopes, forWriter));
    }

    /**
     * A helper method for a creation of an immutable set based on a provided set together with a given item.
     *
     * @param set  The set to create the immutable set from.
     * @param item The item to add to the set before it's made immutable.
     * @return The immutable set from given set and item.
     */
    protected Set<String> immutableSetOf(final Set<String> set, final String item) {
        final Set<String> duplicate = new HashSet<>(set);
        duplicate.add(item);
        return Collections.unmodifiableSet(duplicate);
    }

    /**
     * Creates a string identifier of a sub-graph.
     *
     * @param parent     The parent class.
     * @param field      The field name.
     * @param fieldClass The class of the field.
     * @return The string identifier of the sub-graph.
     */
    protected String subgraphIdentifier(final Class<?> parent, final String field, final Class<?> fieldClass) {
        return parent.getName() + "_" + field + "_" + fieldClass.getName();
    }

    /**
     * Class to be used as a key in cache ({@code EntityContext} -&gt; filtering object) when processing similar requests.
     */
    private static final class EntityContext {

        private final Class<?> entityClass;

        private final Set<String> filteringContext;

        /**
         * Create entity context class for given entity class and set of entity-filtering scopes.
         *
         * @param entityClass     entity class.
         * @param filteringScopes entity-filtering scopes.
         */
        private EntityContext(final Class<?> entityClass, final Set<String> filteringScopes) {
            this.entityClass = entityClass;
            this.filteringContext = filteringScopes;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof EntityContext)) {
                return false;
            }

            final EntityContext that = (EntityContext) o;

            return entityClass.equals(that.entityClass) && filteringContext.equals(that.filteringContext);
        }

        @Override
        public int hashCode() {
            int result = entityClass.hashCode();
            result = 47 * result + filteringContext.hashCode();
            return result;
        }
    }
}
