/*
 * Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.jackson.internal;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.glassfish.jersey.message.filtering.spi.AbstractObjectProvider;
import org.glassfish.jersey.message.filtering.spi.ObjectGraph;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonObjectFormatVisitor;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.BeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.PropertyFilter;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;

/**
 * @author Michal Gajdos
 */
final class JacksonObjectProvider extends AbstractObjectProvider<FilterProvider> {

    @Override
    public FilterProvider transform(final ObjectGraph graph) {
        // Root entity.
        final FilteringPropertyFilter root = new FilteringPropertyFilter(graph.getEntityClass(),
                graph.getFields(),
                createSubfilters(graph.getEntityClass(), graph.getSubgraphs()));

        return new FilteringFilterProvider(root);
    }

    private Map<String, FilteringPropertyFilter> createSubfilters(final Class<?> entityClass,
                                                                  final Map<String, ObjectGraph> entitySubgraphs) {
        final Map<String, FilteringPropertyFilter> subfilters = new HashMap<>();

        for (final Map.Entry<String, ObjectGraph> entry : entitySubgraphs.entrySet()) {
            final String fieldName = entry.getKey();
            final ObjectGraph graph = entry.getValue();

            // Subgraph Fields.
            final Map<String, ObjectGraph> subgraphs = graph.getSubgraphs(fieldName);

            Map<String, FilteringPropertyFilter> subSubfilters = new HashMap<>();
            if (!subgraphs.isEmpty()) {
                final Class<?> subEntityClass = graph.getEntityClass();
                final Set<String> processed = Collections.singleton(subgraphIdentifier(entityClass, fieldName, subEntityClass));

                subSubfilters = createSubfilters(fieldName, subEntityClass, subgraphs, processed);
            }

            final FilteringPropertyFilter filter = new FilteringPropertyFilter(graph.getEntityClass(),
                    graph.getFields(fieldName), subSubfilters);

            subfilters.put(fieldName, filter);
        }

        return subfilters;
    }

    private Map<String, FilteringPropertyFilter> createSubfilters(final String parent, final Class<?> entityClass,
                                                                  final Map<String, ObjectGraph> entitySubgraphs,
                                                                  final Set<String> processed) {
        final Map<String, FilteringPropertyFilter> subfilters = new HashMap<>();

        for (final Map.Entry<String, ObjectGraph> entry : entitySubgraphs.entrySet()) {
            final String fieldName = entry.getKey();
            final ObjectGraph graph = entry.getValue();

            final String path = parent + "." + fieldName;

            // Subgraph Fields.
            final Map<String, ObjectGraph> subgraphs = graph.getSubgraphs(path);

            final Class<?> subEntityClass = graph.getEntityClass();
            final String processedSubgraph = subgraphIdentifier(entityClass, fieldName, subEntityClass);

            Map<String, FilteringPropertyFilter> subSubfilters = new HashMap<>();
            if (!subgraphs.isEmpty() && !processed.contains(processedSubgraph)) {
                // duplicate processed set so that elements in different subtrees aren't skipped (JERSEY-2892)
                final Set<String> subProcessed = immutableSetOf(processed, processedSubgraph);

                subSubfilters = createSubfilters(path, subEntityClass, subgraphs, subProcessed);
            }

            subfilters.put(fieldName, new FilteringPropertyFilter(graph.getEntityClass(), graph.getFields(path), subSubfilters));
        }

        return subfilters;
    }

    private static class FilteringFilterProvider extends FilterProvider {

        private final FilteringPropertyFilter root;
        private final Stack<FilteringPropertyFilter> stack = new Stack<>();

        public FilteringFilterProvider(final FilteringPropertyFilter root) {
            this.root = root;
        }

        @Override
        public BeanPropertyFilter findFilter(final Object filterId) {
            throw new UnsupportedOperationException("Access to deprecated filters not supported");
        }

        @Override
        public PropertyFilter findPropertyFilter(final Object filterId, final Object valueToFilter) {
            if (filterId instanceof String) {
                final String id = (String) filterId;

                // FilterId should represent a class only in case of root entity is marshalled.
                if (id.equals(root.getEntityClass().getName())) {
                    stack.clear();
                    return stack.push(root);
                }

                while (!stack.isEmpty()) {
                    final FilteringPropertyFilter peek = stack.peek();
                    final FilteringPropertyFilter subfilter = peek.findSubfilter(id);

                    if (subfilter != null) {
                        stack.push(subfilter);

                        // Need special handling for maps here - map keys can be filtered as well so we just say that every key is
                        // allowed.
                        if (valueToFilter instanceof Map) {
                            final Map<String, ?> map = (Map<String, ?>) valueToFilter;
                            return new FilteringPropertyFilter(Map.class, map.keySet(),
                                    Collections.<String, FilteringPropertyFilter>emptyMap());
                        }
                        return subfilter;
                    } else {
                        stack.pop();
                    }
                }
            }
            return SimpleBeanPropertyFilter.filterOutAllExcept();
        }
    }

    private static final class FilteringPropertyFilter implements PropertyFilter {

        private final Class<?> entityClass;

        private final Set<String> fields;
        private final Map<String, FilteringPropertyFilter> subfilters;

        private FilteringPropertyFilter(final Class<?> entityClass,
                                        final Set<String> fields, final Map<String, FilteringPropertyFilter> subfilters) {
            this.entityClass = entityClass;

            this.fields = fields;
            this.subfilters = subfilters;
        }

        private boolean include(final String fieldName) {
            return fields.contains(fieldName) || subfilters.containsKey(fieldName);
        }

        @Override
        public void serializeAsField(final Object pojo,
                                     final JsonGenerator jgen,
                                     final SerializerProvider prov,
                                     final PropertyWriter writer) throws Exception {
            if (include(writer.getName())) {
                writer.serializeAsField(pojo, jgen, prov);
            }
        }

        @Override
        public void serializeAsElement(final Object elementValue,
                                       final JsonGenerator jgen,
                                       final SerializerProvider prov,
                                       final PropertyWriter writer) throws Exception {
            if (include(writer.getName())) {
                writer.serializeAsElement(elementValue, jgen, prov);
            }
        }

        @Override
        public void depositSchemaProperty(final PropertyWriter writer,
                                          final ObjectNode propertiesNode,
                                          final SerializerProvider provider) throws JsonMappingException {
            if (include(writer.getName())) {
                writer.depositSchemaProperty(propertiesNode, provider);
            }
        }

        @Override
        public void depositSchemaProperty(final PropertyWriter writer,
                                          final JsonObjectFormatVisitor objectVisitor,
                                          final SerializerProvider provider) throws JsonMappingException {
            if (include(writer.getName())) {
                writer.depositSchemaProperty(objectVisitor, provider);
            }
        }

        public FilteringPropertyFilter findSubfilter(final String fieldName) {
            return subfilters.get(fieldName);
        }

        public Class<?> getEntityClass() {
            return entityClass;
        }
    }
}
