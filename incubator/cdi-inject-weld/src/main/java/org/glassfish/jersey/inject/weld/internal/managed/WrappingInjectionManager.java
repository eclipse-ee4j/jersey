/*
 * Copyright (c) 2021, 2022 Oracle and/or its affiliates. All rights reserved.
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

import org.glassfish.jersey.internal.inject.Binder;
import org.glassfish.jersey.internal.inject.Binding;
import org.glassfish.jersey.internal.inject.ForeignDescriptor;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.internal.inject.ServiceHolder;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Holder of an InjectionManager that is referenced by Beans created in bootstrap.
 * The original bootstrap injection manager is replaced by the proper injection manager
 * after the pre-initialization phase.
 */
class WrappingInjectionManager implements InjectionManager {

    private InjectionManager injectionManager;

    WrappingInjectionManager setInjectionManager(InjectionManager injectionManager) {
        this.injectionManager = injectionManager;
        return this;
    }

    @Override
    public void completeRegistration() {
        injectionManager.completeRegistration();
    }

    @Override
    public void shutdown() {
        injectionManager.shutdown();
    }

    @Override
    public boolean isShutdown() {
        return injectionManager.isShutdown();
    }

    @Override
    public void register(Binding binding) {
        injectionManager.register(binding);
    }

    @Override
    public void register(Iterable<Binding> descriptors) {
        injectionManager.register(descriptors);
    }

    @Override
    public void register(Binder binder) {
        injectionManager.register(binder);
    }

    @Override
    public void register(Object provider) throws IllegalArgumentException {
        injectionManager.register(provider);
    }

    @Override
    public boolean isRegistrable(Class<?> clazz) {
        return injectionManager.isRegistrable(clazz);
    }

    @Override
    public <T> T create(Class<T> createMe) {
        return injectionManager.create(createMe);
    }

    @Override
    public <T> T createAndInitialize(Class<T> createMe) {
        return injectionManager.createAndInitialize(createMe);
    }

    @Override
    public <T> List<ServiceHolder<T>> getAllServiceHolders(Class<T> contractOrImpl, Annotation... qualifiers) {
        return injectionManager.getAllServiceHolders(contractOrImpl, qualifiers);
    }

    @Override
    public <T> T getInstance(Class<T> contractOrImpl, Annotation... qualifiers) {
        return injectionManager.getInstance(contractOrImpl, qualifiers);
    }

    @Override
    public <T> T getInstance(Class<T> contractOrImpl, String classAnalyzer) {
        return injectionManager.getInstance(contractOrImpl, classAnalyzer);
    }

    @Override
    public <T> T getInstance(Class<T> contractOrImpl) {
        return injectionManager.getInstance(contractOrImpl);
    }

    @Override
    public <T> T getInstance(Type contractOrImpl) {
        return injectionManager.getInstance(contractOrImpl);
    }

    @Override
    public Object getInstance(ForeignDescriptor foreignDescriptor) {
        return injectionManager.getInstance(foreignDescriptor);
    }

    @Override
    public ForeignDescriptor createForeignDescriptor(Binding binding) {
        return injectionManager.createForeignDescriptor(binding);
    }

    @Override
    public <T> List<T> getAllInstances(Type contractOrImpl) {
        return injectionManager.getAllInstances(contractOrImpl);
    }

    @Override
    public void inject(Object injectMe) {
        injectionManager.inject(injectMe);
    }

    @Override
    public void inject(Object injectMe, String classAnalyzer) {
        injectionManager.inject(injectMe, classAnalyzer);
    }

    @Override
    public void preDestroy(Object preDestroyMe) {
        injectionManager.preDestroy(preDestroyMe);
    }
}
