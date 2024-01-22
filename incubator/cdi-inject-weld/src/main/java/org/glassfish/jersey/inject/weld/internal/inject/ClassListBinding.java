/*
 * Copyright (c) 2024 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.inject.weld.internal.inject;

import org.glassfish.jersey.internal.inject.ClassBinding;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.internal.inject.ServiceHolder;
import org.glassfish.jersey.internal.inject.ServiceHolderImpl;
import org.glassfish.jersey.internal.util.collection.LazyValue;
import org.glassfish.jersey.internal.util.collection.Value;
import org.glassfish.jersey.internal.util.collection.Values;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ClassListBinding<T> {
    private final Class<T> serviceType;
    private final List<ClassBinding<T>> services = new ArrayList<>();
    private final InjectionManager injectionManager;
    private final LazyValue<List<ServiceHolder<T>>> instances;

    public ClassListBinding(Class<T> serviceType, InjectionManager injectionManager) {
        this.serviceType = serviceType;
        this.injectionManager = injectionManager;
        instances = Values.lazy((Value<List<ServiceHolder<T>>>) () -> services.stream()
                .map(binding -> new ServiceHolderImpl(
                        create(binding),
                        binding.getService().getClass(),
                        binding.getContracts(),
                        binding.getRank() == null ? 0 : binding.getRank()))
                .map(sh -> (ServiceHolder<T>) sh).collect(Collectors.toList()));
    }

    private T create(ClassBinding<T> binding) {
        T t = injectionManager.create(binding.getService());
        injectionManager.inject(t);
        return t;
    }

    public void init(ClassBinding<?> service) {
        services.add((ClassBinding<T>) service);
    }

    public List<T> getServices() {
        return instances.get().stream().map(sh -> ((ServiceHolder<T>) sh).getInstance()).collect(Collectors.toList());
    }

    public List<ServiceHolder<T>> getServiceHolders() {
        return instances.get();
    }}
