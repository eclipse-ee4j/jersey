/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.process;

/**
 * A generic interface for transforming data into a result.
 *
 * @param <DATA> transformable data type.
 * @param <RESULT> type of result produced by a successful inflector data transformation.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public interface Inflector<DATA, RESULT> {

    /**
     * Transform data of a given type into a result of the different type.
     *
     * @param data data to be transformed into a result.
     * @return data transformation result. Return value must not be {@code null}.
     */
    public RESULT apply(DATA data);
}
