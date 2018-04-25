/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.internal.util.collection;

/**
 * A generic value provider, similar to {@link Value}, but able to
 * throw an exception.
 *
 * @param <T> value type.
 * @param <E> exception type.
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public interface UnsafeValue<T, E extends Throwable> {
    /**
     * Get the stored value.
     *
     * @return stored value.
     * @throws E in case there was an error while computing the value.
     */
    public T get() throws E;
}
