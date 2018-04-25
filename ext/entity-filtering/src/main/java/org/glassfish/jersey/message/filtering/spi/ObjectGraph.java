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

package org.glassfish.jersey.message.filtering.spi;

import java.util.Map;
import java.util.Set;

/**
 * Read-only graph containing representations of an entity class that should be processed in entity-filtering. The
 * representations are twofolds: simple (primitive/non-filterable) fields and further-filterable fields (represented by
 * subgraphs).
 * <p/>
 * Object graph instances are created for entity-filtering scopes that are obtained from entity annotations, configuration or
 * scopes defined on resource methods / classes (on server).
 *
 * @author Michal Gajdos
 * @see ObjectGraphTransformer
 * @see ScopeResolver
 */
public interface ObjectGraph {

    /**
     * Get entity domain class of this graph.
     *
     * @return entity domain class.
     */
    public Class<?> getEntityClass();

    /**
     * Get a set of all simple (non-filterable) fields of entity class. Value of each of these fields is either primitive or
     * the entity-filtering feature cannot be applied to this field. Values of these fields can be directly processed.
     *
     * @return non-filterable fields.
     */
    public Set<String> getFields();

    /**
     * Get fields with the given parent path. The parent path, which may exist in the requested filtering scopes, is
     * used for context to match against the field at the subgraph level.
     *
     * @param parent name of parent field.
     * @return non-filterable fields.
     */
    public Set<String> getFields(String parent);

    /**
     * Get a map of all further-filterable fields of entity class. Mappings are represented as:
     * <pre>
     * &lt;field&gt; -&gt; &lt;object-graph&gt;</pre>
     * It is supposed that object graphs contained in this map would be processed further.
     *
     * @return further-filterable map of fields.
     */
    public Map<String, ObjectGraph> getSubgraphs();

    /**
     * Get subgraphs with the given parent path. The parent path, which may exist in the requested filtering scopes, is
     * used for context to match against the subgraph level.
     *
     * @param parent name of parent field.
     * @return further-filterable map of fields.
     *
     */
    public Map<String, ObjectGraph> getSubgraphs(String parent);
}
