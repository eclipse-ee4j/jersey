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

package org.glassfish.jersey.inject.weld.internal.managed;

import org.glassfish.jersey.inject.weld.internal.inject.ClassListBinding;
import org.glassfish.jersey.inject.weld.internal.inject.InstanceListBinding;
import org.glassfish.jersey.internal.inject.ClassBinding;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.internal.inject.InstanceBinding;
import org.glassfish.jersey.internal.inject.ServiceHolder;

import jakarta.ws.rs.RuntimeType;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.client.ClientResponseFilter;
import jakarta.ws.rs.client.RxInvokerProvider;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.container.DynamicFeature;
import jakarta.ws.rs.ext.MessageBodyReader;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.ext.ParamConverterProvider;
import jakarta.ws.rs.ext.ReaderInterceptor;
import jakarta.ws.rs.ext.WriterInterceptor;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class ProviderBindings {
    private final Map<Type, InstanceListBinding<?>> userInstanceBindings = new HashMap<>();
    private final Map<Type, ClassListBinding<?>> userClassBindings = new HashMap<>();
    private final InjectionManager injectionManager;

    ProviderBindings(RuntimeType runtimeType, InjectionManager injectionManager) {
        this.injectionManager = injectionManager;

        if (runtimeType == RuntimeType.CLIENT) {
            init(ClientRequestFilter.class);
            init(ClientResponseFilter.class);
            init(RxInvokerProvider.class);
        } else if (runtimeType == RuntimeType.SERVER) {
            init(ContainerResponseFilter.class);
            init(ContainerRequestFilter.class);
            init(DynamicFeature.class);
        }
        init(ParamConverterProvider.class);
        init(WriterInterceptor.class);
        init(ReaderInterceptor.class);
        init(MessageBodyReader.class);
        init(MessageBodyWriter.class);
    }

    private <T> void init(Class<T> contract) {
        userInstanceBindings.put(contract, new InstanceListBinding<T>(contract));
        userClassBindings.put(contract, new ClassListBinding<T>(contract, injectionManager));
    }

    boolean init(InstanceBinding<?> userBinding) {
        boolean init = false;
        for (Type contract : userBinding.getContracts()) {
            InstanceListBinding<?> binding = userInstanceBindings.get(contract);
            if (binding != null) {
                init = true;
                binding.init(userBinding);
                break;
            }
        }
        return init;
    }

    boolean init(ClassBinding<?> userBinding) {
        boolean init = false;
        for (Type contract : userBinding.getContracts()) {
            ClassListBinding<?> binding = userClassBindings.get(contract);
            if (binding != null) {
                init = true;
                binding.init(userBinding);
                break;
            }
        }
        return init;
    }

    <T> List<ServiceHolder<T>> getServiceHolders(Type contract) {
        List<ServiceHolder<T>> list = new ArrayList<>();

        InstanceListBinding<T> instanceBinding = (InstanceListBinding<T>) userInstanceBindings.get(contract);
        if (instanceBinding != null) {
            list.addAll(instanceBinding.getServiceHolders());
        }

        ClassListBinding<T> classBinding = (ClassListBinding<T>) userClassBindings.get(contract);
        if (classBinding != null) {
            list.addAll(classBinding.getServiceHolders());
        }

        return list;
    }

    <T> List<T> getServices(Type contract) {
        List<T> list = new ArrayList<>();

        InstanceListBinding<T> instanceBinding = (InstanceListBinding<T>) userInstanceBindings.get(contract);
        if (instanceBinding != null) {
            list.addAll(instanceBinding.getServices());
        }

        ClassListBinding<T> classBinding = (ClassListBinding<T>) userClassBindings.get(contract);
        if (classBinding != null) {
            list.addAll(classBinding.getServices());
        }

        return list;
    }
}
