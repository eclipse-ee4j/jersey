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

package org.glassfish.jersey.message.filtering;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.glassfish.jersey.message.filtering.spi.ObjectGraph;

/**
 * Object graph representing empty domain classes.
 *
 * @author Michal Gajdos
 */
final class EmptyObjectGraph implements ObjectGraph {

    private final Class<?> entityClass;

    EmptyObjectGraph(final Class<?> entityClass) {
        this.entityClass = entityClass;
    }

    @Override
    public Class<?> getEntityClass() {
        return entityClass;
    }

    @Override
    public Set<String> getFields() {
        return Collections.emptySet();
    }

    @Override
    public Set<String> getFields(final String parent) {
        return Collections.emptySet();
    }

    @Override
    public Map<String, ObjectGraph> getSubgraphs() {
        return Collections.emptyMap();
    }

    @Override
    public Map<String, ObjectGraph> getSubgraphs(final String parent) {
        return Collections.emptyMap();
    }
}
