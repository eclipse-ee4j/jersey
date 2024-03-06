/*
 * Copyright (c) 2017, 2024 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.innate.inject;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import jakarta.ws.rs.core.GenericType;

import org.glassfish.jersey.internal.inject.Binder;
import org.glassfish.jersey.internal.inject.Binding;
import org.glassfish.jersey.internal.inject.InjectionResolver;

/**
 * Implementation of {@link Binder} interface dedicated to keep some level of code compatibility between previous HK2
 * implementation and new DI SPI.
 * <p>
 * Currently, there are supported only bind method and more complicated method where HK2 interfaces are required were omitted.
 *
 * @author Petr Bouda
 */
public abstract class InternalBinder extends BlindBinder {

    @Override
    public <T> ClassBinding<T> bind(Class<T> serviceType) {
        return (ClassBinding<T>) super.bind(serviceType);
    }

    @Override
    public <T> ClassBinding<T> bindAsContract(Class<T> serviceType) {
        return (ClassBinding<T>) super.bindAsContract(serviceType);
    }

    @Override
    public <T> ClassBinding<T> bindAsContract(GenericType<T> serviceType) {
        return (ClassBinding<T>) super.bindAsContract(serviceType);
    }

    @Override
    public ClassBinding<Object> bindAsContract(Type serviceType) {
        return (ClassBinding<Object>) super.bindAsContract(serviceType);
    }

    public <T> InstanceBinding<T> bind(T service) {
        return (InstanceBinding<T>) super.bind(service);
    }

    @Override
    public <T> SupplierClassBinding<T> bindFactory(
            Class<? extends Supplier<T>> supplierType, Class<? extends Annotation> supplierScope) {
        return (SupplierClassBinding<T>) super.bindFactory(supplierType, supplierScope);
    }

    @Override
    public <T> SupplierClassBinding<T> bindFactory(Class<? extends Supplier<T>> supplierType) {
        return (SupplierClassBinding<T>) super.bindFactory(supplierType);
    }

    @Override
    public <T> SupplierInstanceBinding<T> bindFactory(Supplier<T> factory) {
        return (SupplierInstanceBinding<T>) super.bindFactory(factory);
    }

    @Override
    public <T extends InjectionResolver> InjectionResolverBinding<T> bind(T resolver) {
        return (InjectionResolverBinding<T>) super.bind(resolver);
    }
}
