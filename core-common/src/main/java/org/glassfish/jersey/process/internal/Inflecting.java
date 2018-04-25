/*
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.process.internal;

import org.glassfish.jersey.process.Inflector;

/**
 * Interface that is used to indicate that the instance provides an {@link Inflector}.
 * <p>
 * A typical use case is a terminal (leaf) request processing stage
 * node that transforms a request into a response.
 * </p>
 *
 * @param <DATA> data type transformable by the provided inflector.
 * @param <RESULT> type of a result produced by the provided inflector on success.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public interface Inflecting<DATA, RESULT> {

    /**
     * Get the inflector capable of transforming supplied data into a result.
     *
     * @return data to result transformation.
     */
    Inflector<DATA, RESULT> inflector();
}
