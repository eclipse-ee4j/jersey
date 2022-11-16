/*
 * Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved.
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

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.ws.rs.RuntimeType;

import org.glassfish.jersey.inject.weld.internal.bean.JerseyBean;
import org.glassfish.jersey.inject.weld.internal.inject.InitializableInstanceBinding;
import org.glassfish.jersey.inject.weld.internal.inject.InitializableSupplierInstanceBinding;
import org.glassfish.jersey.inject.weld.internal.inject.MatchableBinding;
import org.glassfish.jersey.inject.weld.internal.injector.JerseyClientCreationalContext;
import org.glassfish.jersey.internal.inject.Binder;
import org.glassfish.jersey.internal.inject.Binding;
import org.glassfish.jersey.internal.inject.Bindings;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.internal.inject.InstanceBinding;
import org.glassfish.jersey.internal.inject.SupplierClassBinding;
import org.glassfish.jersey.internal.inject.SupplierInstanceBinding;
import org.jboss.weld.contexts.CreationalContextImpl;

import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Each Client Runtime has a unique CdiClientInjectionManager, which passes proper {@link Binding} to the Weld.
 */
public class CdiClientInjectionManager extends CdiInjectionManager {

    private Map<InitializableInstanceBinding, InitializableInstanceBinding> clientInstanceBindings = new IdentityHashMap<>();
    private Map<InitializableSupplierInstanceBinding, InitializableSupplierInstanceBinding> clientSupplierInstanceBindings
            = new IdentityHashMap<>();
    private Map<SupplierClassBinding, SupplierClassBinding> clientSupplierClassBinding = new IdentityHashMap<>();

    public CdiClientInjectionManager(BeanManager beanManager, Binder bindings) {
        super(beanManager, bindings);
    }

    @Override
    public void register(Binding binding) {
        if (InstanceBinding.class.isInstance(binding)) {
            final Collection<Binding> preBindings = getBindings().getBindings();
            MatchableBinding.Matching<InitializableInstanceBinding> matching = MatchableBinding.Matching.noneMatching();
            for (Binding preBinding : preBindings) {
                if (InitializableInstanceBinding.class.isInstance(preBinding)) {
                    matching = matching.better(((InitializableInstanceBinding) preBinding).matches((InstanceBinding) binding));
                    if (matching.isBest()) {
                        break;
                    }
                }
            }
            if (matching.matches()) {
                final InitializableInstanceBinding clone = matching.getBinding().clone();
                clone.init(((InstanceBinding) binding).getService());
                clientInstanceBindings.put(matching.getBinding(), clone);
            } else {
                throw new IllegalStateException("Not initialized " + ((InstanceBinding<?>) binding).getService());
            }
        } else if (SupplierInstanceBinding.class.isInstance(binding)) {
            final Collection<Binding> preBindings = getBindings().getBindings();
            MatchableBinding.Matching<InitializableSupplierInstanceBinding> matching = MatchableBinding.Matching.noneMatching();
            for (Binding preBinding : preBindings) {
                if (InitializableSupplierInstanceBinding.class.isInstance(preBinding)) {
                    matching = matching.better(((InitializableSupplierInstanceBinding) preBinding).matchesContracts(binding));
                    if (matching.isBest()) {
                        break;
                    }
                }
            }
            if (matching.matches()) {
                final InitializableSupplierInstanceBinding clone = matching.getBinding().clone();
                clone.init(((SupplierInstanceBinding) binding).getSupplier());
                clientSupplierInstanceBindings.put(matching.getBinding(), clone);
            } else {
                throw new IllegalStateException("Not initialized " + ((SupplierInstanceBinding<?>) binding).getSupplier());
            }
//        } else if (SupplierClassBinding.class.isInstance(binding)) {
//            final Collection<Binding> preBindings = getBindings().getBindings();
//            BindingMatching.Matching<SupplierClassBinding> matching = BindingMatching.Matching.noneMatching();
//            for (Binding preBinding : preBindings) {
//                if (SupplierClassBinding.class.isInstance(preBinding)) {
//                    matching = matching.better(BindingMatching.matches(preBinding, binding));
//                    if (matching.isBest()) {
//                        break;
//                    }
//                }
//            }
//            if (matching.matches()) {
//                final SupplierClassBinding clone = BindingCloner.clone((SupplierClassBinding) binding);
//                clientSupplierClassBinding.put(matching.getBinding(), clone);
//            } else {
//                throw new IllegalStateException("Not initialized " + ((SupplierInstanceBinding<?>) binding).getSupplier());
//            }
        }
    }

    public InitializableInstanceBinding getInjectionManagerBinding(InitializableInstanceBinding binding) {
        InitializableInstanceBinding clientBinding = clientInstanceBindings.get(binding);
        return clientBinding != null ? clientBinding : binding;
    }

    public InitializableSupplierInstanceBinding getInjectionManagerBinding(InitializableSupplierInstanceBinding binding) {
        InitializableSupplierInstanceBinding clientBinding = clientSupplierInstanceBindings.get(binding);
        return clientBinding != null ? clientBinding : binding;
    }

    public SupplierClassBinding getInjectionManagerBinding(SupplierClassBinding binding) {
        SupplierClassBinding clientBinding = clientSupplierClassBinding.get(binding);
        return clientBinding;
    }

    @Override
    public void shutdown() {
        clientInstanceBindings.clear();
    }

    @Override
    protected <T> CreationalContext<T> createCreationalContext(Bean<T> bean) {
        final CreationalContext<T> ctx = new JerseyClientCreationalContext<T>(
                (CreationalContextImpl<T>) super.createCreationalContext(bean)).setInjectionManager(this);
        return ctx;
    }

    @Override
    public void completeRegistration() throws IllegalStateException {
        register(Bindings.service(this).to(InjectionManager.class));
    }

    @Override
    protected boolean isRuntimeTypeBean(Bean<?> bean) {
        return !JerseyBean.class.isInstance(bean) || ((JerseyBean) bean).getRutimeType() == RuntimeType.CLIENT;
    }

}
