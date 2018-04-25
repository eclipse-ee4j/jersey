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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.glassfish.jersey.internal.inject.DisposableSupplier;

import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.reflection.ParameterizedTypeImpl;

/**
 * This class is able to find the {@link Supplier} of the particular type and use this {@code Supplier} to create a new
 * instance. If the {@code Supplier} is not found then {@code null} is returned.
 * <a>
 * If the found {@code Supplier} is a type of {@link DisposableSupplier} then this bridge can delegate
 * {@link Factory#dispose(Object)} invocation to {@link DisposableSupplier#dispose(Object)}.
 * <p>
 * It's recommended to register the instance of this class as a singleton and then the {@link #provide()} is called according to
 * a provided scope (for the created instance) during the binding process.
 *
 * @param <T> type which could be handled by {@code Supplier} and this bridge.
 */
public class SupplierFactoryBridge<T> implements Factory<T> {

    private ServiceLocator locator;
    private ParameterizedType beanType;
    private String beanName;
    private boolean disposable;

    // This bridge can create multiple instances using the method 'provide' therefore must map created suppliers because of
    // 'dispose' invocation later on.
    // TODO: Key as a WeakReference - prevent objects in scope which never dispose the objects such as PerLookup.
    private Map<Object, DisposableSupplier<T>> disposableSuppliers = new IdentityHashMap<>();

    /**
     * Constructor for a new bridge.
     *
     * @param locator    currently used locator, all factory invocations will be delegated to this locator.
     * @param beanType   generic type of a {@link Supplier} which is looked for in locator and on which the creation of
     *                   the new instance is delegated.
     * @param beanName   name of the bean that is provided by supplier.
     * @param disposable flag whether the bridge is set up for disposing the created object.
     */
    SupplierFactoryBridge(ServiceLocator locator, Type beanType, String beanName, boolean disposable) {
        this.locator = locator;
        this.beanType = new ParameterizedTypeImpl(Supplier.class, beanType);
        this.beanName = beanName;
        this.disposable = disposable;
    }

    @Override
    public T provide() {
        if (beanType != null) {
            Supplier<T> supplier = locator.getService(beanType, beanName);
            T instance = supplier.get();
            if (disposable) {
                disposableSuppliers.put(instance, (DisposableSupplier<T>) supplier);
            }
            return instance;
        } else {
            return null;
        }
    }

    @Override
    public void dispose(T instance) {
        if (disposable) {
            DisposableSupplier<T> disposableSupplier = disposableSuppliers.get(instance);
            disposableSupplier.dispose(instance);
            disposableSuppliers.remove(instance);
        }
    }
}
