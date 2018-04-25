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

package org.glassfish.jersey.inject.hk2;

import java.util.function.Supplier;

import org.glassfish.jersey.internal.inject.DisposableSupplier;

import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.api.ServiceLocator;

/**
 * This class is used as a bridge between {@link Factory HK2 Factory} and JDK {@link Supplier}. Using this class {@link Supplier}
 * is able to behave as a factory in service locator. The bridge just delegates all invocations to provided {@link ServiceLocator}
 * and therefore all operation should be in proper scope and context.
 * <p>
 * This bridge is dedicated to instance binding therefore underlying {@code supplier} is always only single instance.
 *
 * @param <T> type which could be handled by {@code Supplier} and this bridge.
 */
public class InstanceSupplierFactoryBridge<T> implements Factory<T> {

    private Supplier<T> supplier;
    private boolean disposable;

    /**
     * Constructor for a new bridge.
     *
     * @param supplier   type which will be looked for in locator.
     * @param disposable flag whether the bridge is set up for disposing the created object.
     */
    InstanceSupplierFactoryBridge(Supplier<T> supplier, boolean disposable) {
        this.supplier = supplier;
        this.disposable = disposable;
    }

    @Override
    public T provide() {
        return supplier.get();
    }

    @Override
    public void dispose(T instance) {
        if (disposable) {
            ((DisposableSupplier<T>) supplier).dispose(instance);
        }
    }
}
