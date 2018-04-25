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

package org.glassfish.jersey.moxy.internal;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBException;

import org.glassfish.jersey.message.filtering.spi.AbstractObjectProvider;
import org.glassfish.jersey.message.filtering.spi.ObjectGraph;

import org.eclipse.persistence.jaxb.JAXBContext;
import org.eclipse.persistence.jaxb.JAXBContextFactory;
import org.eclipse.persistence.jaxb.JAXBHelper;
import org.eclipse.persistence.jaxb.Subgraph;
import org.eclipse.persistence.jaxb.TypeMappingInfo;

/**
 * @author Michal Gajdos
 */
final class MoxyObjectProvider extends AbstractObjectProvider<org.eclipse.persistence.jaxb.ObjectGraph> {

    private static final JAXBContext JAXB_CONTEXT;

    static {
        try {
            JAXB_CONTEXT = JAXBHelper.getJAXBContext(
                    JAXBContextFactory.createContext(new TypeMappingInfo[]{}, Collections.emptyMap(), null));
        } catch (final JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public org.eclipse.persistence.jaxb.ObjectGraph transform(final ObjectGraph graph) {
        return createObjectGraph(graph.getEntityClass(), graph);
    }

    private org.eclipse.persistence.jaxb.ObjectGraph createObjectGraph(final Class<?> entityClass,
                                                                       final ObjectGraph objectGraph) {
        final org.eclipse.persistence.jaxb.ObjectGraph graph = JAXB_CONTEXT.createObjectGraph(entityClass);
        final Set<String> fields = objectGraph.getFields();

        if (!fields.isEmpty()) {
            graph.addAttributeNodes(fields.toArray(new String[fields.size()]));
        }

        final Map<String, ObjectGraph> subgraphs = objectGraph.getSubgraphs();
        if (!subgraphs.isEmpty()) {
            createSubgraphs(graph, objectGraph.getEntityClass(), subgraphs);
        }

        return graph;
    }

    private void createSubgraphs(final org.eclipse.persistence.jaxb.ObjectGraph graph,
                                 final Class<?> entityClass, final Map<String, ObjectGraph> entitySubgraphs) {
        for (final Map.Entry<String, ObjectGraph> entry : entitySubgraphs.entrySet()) {
            final String fieldName = entry.getKey();

            final Subgraph subgraph = graph.addSubgraph(fieldName);
            final ObjectGraph entityGraph = entry.getValue();

            final Set<String> fields = entityGraph.getFields(fieldName);
            if (!fields.isEmpty()) {
                subgraph.addAttributeNodes(fields.toArray(new String[fields.size()]));
            }

            final Map<String, ObjectGraph> subgraphs = entityGraph.getSubgraphs(fieldName);
            if (!subgraphs.isEmpty()) {
                final Class<?> subEntityClass = entityGraph.getEntityClass();

                final Set<String> processed = Collections.singleton(subgraphIdentifier(entityClass, fieldName, subEntityClass));
                createSubgraphs(fieldName, subgraph, subEntityClass, subgraphs, processed);
            }
        }
    }

    private void createSubgraphs(final String parent, final Subgraph graph, final Class<?> entityClass,
                                 final Map<String, ObjectGraph> entitySubgraphs, final Set<String> processed) {
        for (final Map.Entry<String, ObjectGraph> entry : entitySubgraphs.entrySet()) {
            final String fieldName = entry.getKey();

            final Subgraph subgraph = graph.addSubgraph(fieldName);
            final ObjectGraph entityGraph = entry.getValue();

            final String path = parent + "." + fieldName;

            final Set<String> fields = entityGraph.getFields(path);
            if (!fields.isEmpty()) {
                subgraph.addAttributeNodes(fields.toArray(new String[fields.size()]));
            }

            final Map<String, ObjectGraph> subgraphs = entityGraph.getSubgraphs(path);
            final Class<?> subEntityClass = entityGraph.getEntityClass();
            final String processedSubgraph = subgraphIdentifier(entityClass, fieldName, subEntityClass);

            if (!subgraphs.isEmpty() && !processed.contains(processedSubgraph)) {
                // duplicate processed set so that elements in different subtrees aren't skipped (J-605)
                final Set<String> subProcessed = immutableSetOf(processed, processedSubgraph);
                createSubgraphs(path, subgraph, subEntityClass, subgraphs, subProcessed);
            }
        }
    }
}
