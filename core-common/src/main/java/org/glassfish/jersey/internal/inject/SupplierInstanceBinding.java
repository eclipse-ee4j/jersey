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
 * Injection binding description of a bean bound indirectly via an supplier producing instances of the bound type.
 *
 * @param <T> type of the bean described by this injection binding descriptor.
 * @author Petr Bouda
 */
public class SupplierInstanceBinding<T> extends Binding<Supplier<T>, SupplierInstanceBinding<T>> {

    private final Supplier<T> supplier;

    /**
     * Creates a supplier as an instance.
     *
     * @param supplier service's instance.
     */
    SupplierInstanceBinding(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    /**
     * Gets supplier's instance.
     *
     * @return supplier's instance.
     */
    public Supplier<T> getSupplier() {
        return supplier;
    }
}
