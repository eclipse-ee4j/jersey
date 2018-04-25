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

import java.lang.annotation.Annotation;
import java.util.function.Supplier;

/**
 * Injection binding description of a bean bound indirectly via a supplier class producing instances of the bound type.
 *
 * @param <T> type of the bean described by this injection binding descriptor.
 * @author Petr Bouda
 */
public class SupplierClassBinding<T> extends Binding<Supplier<T>, SupplierClassBinding<T>> {

    private final Class<? extends Supplier<T>> supplierClass;
    private final Class<? extends Annotation> supplierScope;

    /**
     * Creates a service as a class.
     *
     * @param supplierClass factory's class.
     * @param scope        factory's scope.
     */
    SupplierClassBinding(Class<? extends Supplier<T>> supplierClass, Class<? extends Annotation> scope) {
        this.supplierClass = supplierClass;
        this.supplierScope = scope;
    }

    /**
     * Gets supplier's class.
     *
     * @return supplier's class.
     */
    public Class<? extends Supplier<T>> getSupplierClass() {
        return supplierClass;
    }

    /**
     * Gets supplier's scope.
     *
     * @return supplier's scope.
     */
    public Class<? extends Annotation> getSupplierScope() {
        return supplierScope;
    }
}
