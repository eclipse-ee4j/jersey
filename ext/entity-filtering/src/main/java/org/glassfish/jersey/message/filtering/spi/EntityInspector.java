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

import org.glassfish.jersey.spi.Contract;

/**
 * Responsible for inspecting entity classes. This class invokes all available {@link EntityProcessor entity processors} with
 * different {@link EntityProcessorContext contexts}.
 *
 * @author Michal Gajdos
 */
@Contract
public interface EntityInspector {

    /**
     * Inspect entity class and create/update {@link EntityGraph} for reader/writer. The entity graph will be used to create
     * entity-filtering object which is requested by {@code #createFilteringObject(...)}.
     * <p>
     * Method recursively inspects entity fields classes suitable for inspecting.
     * </p>
     * <p>
     * Method uses {@link EntityProcessor}s for inspecting.
     * </p>
     *
     * @param entityClass entity class to be examined.
     * @param forWriter flag determining whether the class should be examined for reader or writer.
     */
    public void inspect(final Class<?> entityClass, final boolean forWriter);
}
