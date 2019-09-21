/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.internal.inject;

import java.util.function.Supplier;

/**
 * Supplier extension which is able to call {@link #get()} method to create a new object and also call {@link #dispose(Object)}
 * to make some cleaning code regarding the instance and the specific {@link Supplier} instance.
 *
 * @param <T> type which is created by {@link DisposableSupplier}.
 * @author Petr Bouda
 */
public interface DisposableSupplier<T> extends Supplier<T> {

    /**
     * This method will dispose the provided object created by this {@code Supplier}.
     *
     * @param instance the instance to be disposed.
     */
    void dispose(T instance);

}
