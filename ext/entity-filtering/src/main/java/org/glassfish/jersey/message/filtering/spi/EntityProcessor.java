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
 * Contract supposed to process entity classes for Entity Data Filtering. Implementations will be given a
 * {@link EntityProcessorContext context} providing necessary information to process particular
 * {@link EntityProcessorContext.Type context type}. Contexts are created for: class / properties / accessors.
 *
 * @author Michal Gajdos
 */
@Contract
public interface EntityProcessor {

    /**
     * Result type of processing an context.
     */
    public enum Result {

        /**
         * Processing of an context resulted in modification of the provided entity graph.
         */
        APPLY,

        /**
         * Entity processor didn't modify the provided entity graph.
         */
        SKIP,

        /**
         * Rollback every entity graph modification done in current context.
         */
        ROLLBACK
    }

    /**
     * Process given (class/property/accessor) {@link EntityProcessorContext context} by modifying provided {@link EntityGraph}.
     *
     * @param context context to be processed.
     * @return result of processing a context.
     */
    public Result process(final EntityProcessorContext context);
}
