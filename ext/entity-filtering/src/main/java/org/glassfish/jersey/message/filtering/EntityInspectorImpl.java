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
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Spliterator;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.internal.inject.Providers;
import org.glassfish.jersey.internal.util.ReflectionHelper;
import org.glassfish.jersey.message.filtering.spi.EntityGraph;
import org.glassfish.jersey.message.filtering.spi.EntityGraphProvider;
import org.glassfish.jersey.message.filtering.spi.EntityInspector;
import org.glassfish.jersey.message.filtering.spi.EntityProcessor;
import org.glassfish.jersey.message.filtering.spi.EntityProcessorContext;
import org.glassfish.jersey.message.filtering.spi.FilteringHelper;
import org.glassfish.jersey.model.internal.RankedComparator;

/**
 * Class responsible for inspecting entity classes. This class invokes all available {@link EntityProcessor entity processors} in
 * different {@link EntityProcessorContext contexts}.
 *
 * @author Michal Gajdos
 */
@Singleton
final class EntityInspectorImpl implements EntityInspector {

    private final List<EntityProcessor> entityProcessors;

    @Inject
    private EntityGraphProvider graphProvider;

    /**
     * Constructor expecting {@link InjectionManager} to be injected.
     *
     * @param injectionManager injection manager to be injected.
     */
    @Inject
    public EntityInspectorImpl(final InjectionManager injectionManager) {
        Spliterator<EntityProcessor> entities =
                Providers.getAllProviders(injectionManager, EntityProcessor.class, new RankedComparator<>()).spliterator();
        this.entityProcessors = StreamSupport.stream(entities, false).collect(Collectors.toList());
    }

    @Override
    public void inspect(final Class<?> entityClass, final boolean forWriter) {
        if (!graphProvider.containsEntityGraph(entityClass, forWriter)) {
            final EntityGraph graph = graphProvider.getOrCreateEntityGraph(entityClass, forWriter);
            final Set<Class<?>> inspect = new HashSet<>();

            // Class.
            if (!inspectEntityClass(entityClass, graph, forWriter)) {
                // Properties.
                final Map<String, Method> unmatchedAccessors = inspectEntityProperties(entityClass, graph, inspect, forWriter);

                // Setters/Getters without fields.
                inspectStandaloneAccessors(unmatchedAccessors, graph, forWriter);

                // Inspect new classes.
                for (final Class<?> clazz : inspect) {
                    inspect(clazz, forWriter);
                }
            }
        }
    }

    /**
     * Invoke available {@link EntityProcessor}s on given entity class.
     *
     * @param entityClass entity class to be examined.
     * @param graph entity graph to be modified by examination.
     * @param forWriter flag determining whether the class should be examined for reader or writer.
     * @return {@code true} if the inspecting should be roll-backed, {@code false} otherwise.
     */
    private boolean inspectEntityClass(final Class<?> entityClass, final EntityGraph graph, final boolean forWriter) {
        final EntityProcessorContextImpl context = new EntityProcessorContextImpl(
                forWriter ? EntityProcessorContext.Type.CLASS_WRITER : EntityProcessorContext.Type.CLASS_READER,
                entityClass, graph);

        for (final EntityProcessor processor : entityProcessors) {
            final EntityProcessor.Result result = processor.process(context);

            if (EntityProcessor.Result.ROLLBACK == result) {
                graphProvider.getOrCreateEmptyEntityGraph(entityClass, false);
                return true;
            }
        }
        return false;
    }

    /**
     * Invoke available {@link EntityProcessor}s on fields of given entity class. Method returns a map ({@code fieldName},
     * {@code method}) of unprocessed property accessors (getters/setters) and fills {@code inspect} set with entity classes
     * that should be further processed.
     *
     * @param entityClass entity class to obtain properties to be examined.
     * @param graph entity graph to be modified by examination.
     * @param inspect non-null set of classes to-be-examined.
     * @param forWriter flag determining whether the class should be examined for reader or writer.
     * @return map of unprocessed property accessors.
     */
    private Map<String, Method> inspectEntityProperties(final Class<?> entityClass, final EntityGraph graph,
                                                        final Set<Class<?>> inspect, final boolean forWriter) {
        final Field[] fields = AccessController.doPrivileged(ReflectionHelper.getAllFieldsPA(entityClass));
        final Map<String, Method> methods = FilteringHelper.getPropertyMethods(entityClass, forWriter);

        for (final Field field : fields) {
            // Ignore static fields.
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }

            final String name = field.getName();
            final Class<?> clazz = FilteringHelper.getEntityClass(field.getGenericType());
            final Method method = methods.remove(name);

            final EntityProcessorContextImpl context = new EntityProcessorContextImpl(
                    forWriter ? EntityProcessorContext.Type.PROPERTY_WRITER : EntityProcessorContext.Type.PROPERTY_READER,
                    field, method, graph);

            boolean rollback = false;
            for (final EntityProcessor processor : entityProcessors) {
                final EntityProcessor.Result result = processor.process(context);

                if (EntityProcessor.Result.ROLLBACK == result) {
                    rollback = true;
                    graph.remove(name);
                    break;
                }
            }

            if (!rollback && FilteringHelper.filterableEntityClass(clazz)) {
                inspect.add(clazz);
            }
        }

        return methods;
    }

    /**
     * Invoke available {@link EntityProcessor}s on accessors (getter/setter) that has no match in classes' fields.
     *
     * @param unprocessedAccessors map of unprocessed accessors.
     * @param graph entity graph to be modified by examination.
     * @param forWriter flag determining whether the class should be examined for reader or writer.
     */
    private void inspectStandaloneAccessors(final Map<String, Method> unprocessedAccessors, final EntityGraph graph,
                                            final boolean forWriter) {
        for (final Map.Entry<String, Method> entry : unprocessedAccessors.entrySet()) {
            final EntityProcessorContextImpl context = new EntityProcessorContextImpl(
                    forWriter ? EntityProcessorContext.Type.METHOD_WRITER : EntityProcessorContext.Type.METHOD_READER,
                    entry.getValue(), graph);

            for (final EntityProcessor processor : entityProcessors) {
                final EntityProcessor.Result result = processor.process(context);

                if (EntityProcessor.Result.ROLLBACK == result) {
                    graph.remove(entry.getKey());
                    break;
                }
            }
        }
    }
}
