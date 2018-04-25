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

import java.util.Set;

import org.glassfish.jersey.spi.Contract;

/**
 * Provides {@link EntityGraph entity graph} and {@link ObjectGraph object graph} instances.
 *
 * @author Michal Gajdos
 */
@Contract
public interface EntityGraphProvider {

    /**
     * Get an entity graph for given class. New graph is created if no graph exists for given class.
     *
     * @param entityClass entity class the graph should be created for.
     * @param forWriter flag determining whether the graph should be created for writer/reader.
     * @return an entity graph.
     */
    public EntityGraph getOrCreateEntityGraph(final Class<?> entityClass, final boolean forWriter);

    /**
     * Get an empty entity graph for given class. New graph is created if the stored one is not an empty entity graph or no graph
     * exists for given class. This method overrides the graph created by {@link #getOrCreateEntityGraph(Class, boolean)} method.
     *
     * @param entityClass entity class the graph should be created for.
     * @param forWriter flag determining whether the graph should be created for writer/reader.
     * @return an empty entity graph.
     */
    public EntityGraph getOrCreateEmptyEntityGraph(final Class<?> entityClass, final boolean forWriter);

    /**
     * Determine whether an entity graph for given entity class has been created by this provider.
     *
     * @param entityClass entity class for which the graph should be checked.
     * @param forWriter flag determining whether the check should be in writer/reader graphs.
     * @return {@code true} if the entity graph already exists, {@code false} otherwise.
     */
    public boolean containsEntityGraph(final Class<?> entityClass, final boolean forWriter);

    /**
     * Create an {@code ObjectGraph} for given parameters. Every time this method is called a new instance of object graph is
     * created.
     *
     * @param entityClass entity class which the object graph should be created for.
     * @param filteringScopes entity-filtering scopes the graph should be created for.
     * @param forWriter flag determining whether the graph should be created for writer/reader.
     * @return an entity-filtering object graph instance.
     */
    public ObjectGraph createObjectGraph(final Class<?> entityClass, final Set<String> filteringScopes, final boolean forWriter);
}
