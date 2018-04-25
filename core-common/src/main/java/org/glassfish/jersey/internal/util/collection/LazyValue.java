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

package org.glassfish.jersey.internal.util.collection;

/**
 * Lazily initialized {@link Value value}.
 * <p>
 * Instances of this interface are initialized lazily during the first call to their
 * {@link #get() value retrieval method}. Information about the initialization state
 * of a {@code LazyValue} instance is available via {@link #isInitialized()} method.
 * </p>
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public interface LazyValue<T> extends Value<T> {
    /**
     * Check if the lazy value has been initialized already (i.e. its {@link #get()} method
     * has already been called previously) or not.
     *
     * @return {@code true} if the lazy value has already been initialized, {@code false} otherwise.
     */
    boolean isInitialized();
}
